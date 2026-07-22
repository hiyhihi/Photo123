package com.example.photofilter.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Sends the current photo to a Gemini image-editing model and returns the
 * enhanced result. Pure network I/O — every method blocks and must be
 * called from a background thread.
 *
 * <p>MODEL_NAME reflects the image-editing model available at the time this
 * was written. If Google renames or retires it, swap in whatever the AI
 * Studio model picker currently lists under "image generation/editing".
 */
public class GeminiEnhanceRepository {

    private static final String MODEL_NAME = "gemini-2.5-flash-image";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent";
    private static final String ENHANCE_PROMPT =
            "Enhance this photo: improve sharpness, lighting and color balance while keeping the original composition.";

    public Bitmap enhance(Bitmap source, String apiKey) throws IOException {
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("REPLACE_WITH")) {
            throw new IOException("Chưa cấu hình Gemini API key trong local.properties");
        }

        String requestBody = buildRequestJson(encodeToBase64(source));
        HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT + "?key=" + apiKey).openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(60_000);

            try (OutputStream out = connection.getOutputStream()) {
                out.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            boolean ok = status >= 200 && status < 300;
            String responseText = readStream(ok ? connection.getInputStream() : connection.getErrorStream());
            if (!ok) {
                throw new IOException("Gemini API lỗi (" + status + "): " + responseText);
            }
            return parseImageFromResponse(responseText);
        } finally {
            connection.disconnect();
        }
    }

    private static String encodeToBase64(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
            throw new IOException("Không thể mã hoá ảnh để gửi lên AI");
        }
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    private static String buildRequestJson(String base64Image) throws IOException {
        try {
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", "image/jpeg");
            inlineData.put("data", base64Image);

            JSONObject imagePart = new JSONObject();
            imagePart.put("inlineData", inlineData);

            JSONObject textPart = new JSONObject();
            textPart.put("text", ENHANCE_PROMPT);

            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(textPart).put(imagePart));

            JSONObject root = new JSONObject();
            root.put("contents", new JSONArray().put(content));
            return root.toString();
        } catch (JSONException e) {
            throw new IOException("Không thể tạo yêu cầu gửi AI", e);
        }
    }

    private static Bitmap parseImageFromResponse(String responseText) throws IOException {
        try {
            JSONObject root = new JSONObject(responseText);
            JSONArray candidates = root.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                throw new IOException("AI không trả về kết quả");
            }
            JSONArray parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts");
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                JSONObject inlineData = part.optJSONObject("inlineData");
                if (inlineData == null) {
                    inlineData = part.optJSONObject("inline_data");
                }
                if (inlineData != null) {
                    String base64Data = inlineData.optString("data", null);
                    if (base64Data != null) {
                        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
                        Bitmap result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
            throw new IOException("AI không trả về ảnh (model có thể không hỗ trợ chỉnh sửa ảnh)");
        } catch (JSONException e) {
            throw new IOException("Không đọc được phản hồi từ AI", e);
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toString("UTF-8");
    }
}

# Việc của Tú Anh — chỉ rõ từng đoạn code kèm giải thích

> File này là bản **kỹ thuật**, đi kèm với [`NHIEM_VU_TU_ANH.md`](NHIEM_VU_TU_ANH.md) (bản giải thích bằng lời thường). Ở đây trích nguyên code thật trong repo, chỉ rõ từng đoạn, giải thích ngay bên dưới — dùng để trả lời vấn đáp khi thầy hỏi "code chỗ này làm gì".

---

## 1. Cái khuôn chung: `Filter.java` + `BaseFilter.java`

**File:** `app/src/main/java/com/example/photofilter/domain/filter/Filter.java`

```java
package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;

public interface Filter {
    Bitmap apply(Bitmap source);
}
```

**Giải thích:** Đây là *interface* — chỉ khai báo "1 bộ lọc phải có khả năng gì" (nhận vào 1 `Bitmap`, trả về 1 `Bitmap` khác), không viết code xử lý gì ở đây cả. Bất kỳ class nào implement interface này đều được app coi là "1 bộ lọc hợp lệ" — đây là chỗ đáp ứng yêu cầu bắt buộc "phải có interface" của đề bài.

**File:** `app/src/main/java/com/example/photofilter/domain/filter/BaseFilter.java`

```java
package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public abstract class BaseFilter implements Filter {

    @Override
    public final Bitmap apply(Bitmap source) {
        if (source == null || source.isRecycled()) {
            throw new IllegalArgumentException("Source bitmap không hợp lệ hoặc đã bị recycle");
        }
        return process(source);
    }

    protected abstract Bitmap process(Bitmap source);

    protected final Bitmap applyColorMatrix(Bitmap source, ColorMatrix matrix) {
        Bitmap.Config config = source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), config);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(source, 0, 0, paint);
        return output;
    }
}
```

**Giải thích — đây là "Template Method pattern" (đề bài yêu cầu phải có abstract class):**
- `apply(source)` được đánh dấu `final` — nghĩa là **không class con nào được ghi đè lại bước kiểm tra ảnh hỏng**. Mọi bộ lọc đều tự động được bảo vệ khỏi crash nếu lỡ truyền vào ảnh `null` hoặc đã bị `recycle()`.
- `process(source)` là `abstract` — đây là chỗ **mỗi bộ lọc con bắt buộc phải tự viết riêng**, quyết định bộ lọc đó thực sự làm gì.
- `applyColorMatrix(...)` là hàm dùng chung có sẵn: dựng 1 `Bitmap` mới cùng kích thước, vẽ ảnh gốc lên bằng `Canvas` với 1 `Paint` có gắn `ColorMatrixColorFilter` — bất kỳ bộ lọc nào chỉ cần *biến đổi màu theo công thức tuyến tính* (không cần biết vị trí pixel) đều gọi thẳng hàm này, khỏi viết lại đoạn `Canvas`/`Paint` này 13 lần.

---

## 2. Ba bộ lọc bắt buộc theo đề bài

**File:** `OriginalFilter.java`

```java
public class OriginalFilter extends BaseFilter {
    @Override
    protected Bitmap process(Bitmap source) {
        return applyColorMatrix(source, new ColorMatrix());
    }
}
```
`new ColorMatrix()` không truyền tham số = ma trận đơn vị (identity) — tức là "không đổi gì cả". Bộ lọc đơn giản nhất, dùng để test cái khuôn `BaseFilter` hoạt động đúng trước khi làm bộ phức tạp hơn.

**File:** `GrayscaleFilter.java`

```java
public class GrayscaleFilter extends BaseFilter {
    @Override
    protected Bitmap process(Bitmap source) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        return applyColorMatrix(source, matrix);
    }
}
```
`setSaturation(0f)` là hàm có sẵn của Android — đặt độ bão hoà màu về 0 nghĩa là rút hết màu, chỉ còn lại độ sáng-tối (đen trắng).

**File:** `NegativeFilter.java`

```java
public class NegativeFilter extends BaseFilter {
    private static final float[] NEGATIVE_MATRIX = {
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
    };
    @Override
    protected Bitmap process(Bitmap source) {
        return applyColorMatrix(source, new ColorMatrix(NEGATIVE_MATRIX));
    }
}
```
Ma trận 4×5 là cách Android biểu diễn phép biến đổi màu: mỗi hàng ứng với 1 kênh màu đầu ra (R, G, B, A), 4 số đầu là hệ số nhân cho (R, G, B, A) đầu vào, số thứ 5 là số cộng thêm. Ở đây: `output_R = -1 × input_R + 255` — tức là `255 - R`, đúng công thức đảo màu (âm bản). Áp dụng y hệt cho G và B, kênh Alpha (độ trong suốt) giữ nguyên (`1f` ở cột thứ 4, dòng cuối).

---

## 3. Các bộ lọc pha màu khác (cùng kỹ thuật ColorMatrix)

Tất cả các bộ dưới đây **dùng chung 1 kỹ thuật** với `NegativeFilter` ở trên (chỉ khác con số trong ma trận) — hiểu 1 cái là hiểu được cả nhóm:

**File:** `SepiaFilter.java`
```java
private static final float[] SEPIA_MATRIX = {
        0.393f, 0.769f, 0.189f, 0f, 0f,
        0.349f, 0.686f, 0.168f, 0f, 0f,
        0.272f, 0.534f, 0.131f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
};
```
Khác với Negative (mỗi kênh chỉ phụ thuộc chính nó), ở đây **mỗi kênh đầu ra là tổ hợp của cả 3 kênh R+G+B đầu vào** (theo đúng công thức sepia kinh điển) — đây là lý do ảnh ngả nâu vàng đồng đều thay vì lệch hẳn về 1 màu.

**File:** `ColorToneFilter.java` (dùng cho cả nút "Ấm" và "Lạnh")
```java
public class ColorToneFilter extends BaseFilter {
    public enum Tone { WARM, COOL }
    private static final float OFFSET = 30f;
    private final Tone tone;

    public ColorToneFilter(Tone tone) { this.tone = tone; }

    @Override
    protected Bitmap process(Bitmap source) {
        float redOffset = tone == Tone.WARM ? OFFSET : -OFFSET;
        float blueOffset = tone == Tone.WARM ? -OFFSET : OFFSET;
        float[] matrix = {
                1f, 0f, 0f, 0f, redOffset,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, blueOffset,
                0f, 0f, 0f, 1f, 0f
        };
        return applyColorMatrix(source, new ColorMatrix(matrix));
    }
}
```
1 class dùng chung cho 2 nút bấm khác nhau ("Ấm" và "Lạnh") nhờ tham số `enum Tone` truyền vào lúc khởi tạo — thay vì viết 2 file gần như giống hệt nhau. **Ấm** = cộng thêm cho kênh đỏ, trừ ở kênh xanh dương; **Lạnh** thì ngược lại — dấu `?:` (ternary) đảo dấu `OFFSET` theo `tone`.

**File:** `BrightnessContrastFilter.java` (dùng cho nút "Sáng")
```java
public class BrightnessContrastFilter extends BaseFilter {
    private final float brightness;
    private final float contrast;

    public BrightnessContrastFilter(float brightness, float contrast) {
        this.brightness = brightness;
        this.contrast = contrast;
    }

    @Override
    protected Bitmap process(Bitmap source) {
        float translate = brightness + (1f - contrast) * 127.5f;
        float[] matrix = {
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
        };
        return applyColorMatrix(source, new ColorMatrix(matrix));
    }
}
```
Công thức chuẩn của nhiếp ảnh số: `output = input × contrast + translate`. Vì sao `translate` phải cộng thêm `(1 - contrast) × 127.5`? Vì nếu chỉ nhân `contrast` không thôi, tăng tương phản sẽ vô tình làm cả ảnh tối đi hoặc sáng lên theo — phải "neo" phép tính quanh điểm giữa xám (127.5 trong thang 0–255) để tăng tương phản mà không làm đổi độ sáng trung bình.

**File:** `FilmFilter.java`, `MonoFilter.java`, `RetroFilter.java`, `VintageFilter.java` — 4 file này đều theo đúng khuôn: gọi `matrix.setSaturation(x)` trước, rồi `matrix.postConcat(...)` ghép thêm 1 ma trận cố định (số liệu khác nhau cho từng phong cách):

```java
// FilmFilter — giảm bão hoà nhẹ, ngả vàng-xanh lá, hạ tương phản
matrix.setSaturation(0.85f);
matrix.postConcat(new ColorMatrix(new float[]{
        0.92f, 0f, 0f, 0f, 12f,
        0f, 0.94f, 0f, 0f, 14f,
        0f, 0f, 0.90f, 0f, 6f,
        0f, 0f, 0f, 1f, 0f
}));

// MonoFilter — khử màu hoàn toàn (khác Grayscale), rồi tăng tương phản + ngả lạnh nhẹ
matrix.setSaturation(0f);
matrix.postConcat(new ColorMatrix(new float[]{
        1.15f, 0f, 0f, 0f, -10f,
        0f, 1.15f, 0f, 0f, -8f,
        0f, 0f, 1.18f, 0f, 2f,
        0f, 0f, 0f, 1f, 0f
}));

// RetroFilter — tăng bão hoà, ngả đỏ/hồng, giảm kênh xanh dương
matrix.setSaturation(1.1f);
matrix.postConcat(new ColorMatrix(new float[]{
        1.1f, 0.05f, 0f, 0f, 10f,
        0f, 0.95f, 0f, 0f, 0f,
        0.05f, 0f, 0.8f, 0f, 15f,
        0f, 0f, 0f, 1f, 0f
}));

// VintageFilter — giảm bão hoà nhiều, nâng tối thiểu (lifted shadow), ngả vàng
matrix.setSaturation(0.7f);
matrix.postConcat(new ColorMatrix(new float[]{
        0.9f, 0f, 0f, 0f, 20f,
        0f, 0.88f, 0f, 0f, 10f,
        0f, 0f, 0.8f, 0f, -10f,
        0f, 0f, 0f, 1f, 0f
}));
```

`postConcat(...)` = "ghép thêm 1 phép biến đổi nữa vào sau phép biến đổi đã có" — nhờ vậy 1 bộ lọc có thể vừa đổi độ bão hoà (`setSaturation`) vừa đổi tông màu (ma trận riêng) trong cùng 1 lượt, không cần vẽ `Canvas` 2 lần.

---

## 4. Bộ lọc vị trí-phụ-thuộc (không dùng được ColorMatrix)

`ColorMatrix` **chỉ biết biến đổi theo kênh màu**, không biết pixel đang ở toạ độ nào trên ảnh — nên 2 bộ lọc dưới đây phải viết tay bằng cách khác.

**File:** `VignetteFilter.java` (làm tối 4 góc)

```java
public class VignetteFilter extends BaseFilter {
    @Override
    protected Bitmap process(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap.Config config = source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap output = Bitmap.createBitmap(width, height, config);

        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(source, 0, 0, null);

        float radius = Math.max(width, height) * 0.75f;
        RadialGradient gradient = new RadialGradient(
                width / 2f, height / 2f, radius,
                new int[]{0x00000000, 0x00000000, 0x99000000},
                new float[]{0f, 0.55f, 1f},
                Shader.TileMode.CLAMP);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(gradient);
        canvas.drawRect(0f, 0f, width, height, paint);

        return output;
    }
}
```
Không gọi `applyColorMatrix` nữa mà tự vẽ 2 lớp lên `Canvas`: lớp 1 là ảnh gốc, lớp 2 là 1 `RadialGradient` (gradient toả tròn từ tâm ảnh) — trong suốt hoàn toàn (`0x00000000`) ở 55% bán kính đầu, rồi tối dần tới đen mờ 60% (`0x99000000`, `99` hex = ~60% độ đục) ở rìa ngoài. Vẽ chồng gradient này lên trên ảnh gốc tạo hiệu ứng 4 góc tối dần, giữa ảnh vẫn rõ.

**File:** `BlurFilter.java` (làm mờ nhẹ)

```java
public class BlurFilter extends BaseFilter {
    private static final int RADIUS = 6;

    @Override
    protected Bitmap process(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int[] horizontal = boxBlurPass(pixels, width, height, RADIUS, true);
        int[] result = boxBlurPass(horizontal, width, height, RADIUS, false);

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        output.setPixels(result, 0, width, 0, 0, width, height);
        return output;
    }

    private static int[] boxBlurPass(int[] pixels, int width, int height, int radius, boolean horizontal) {
        // với mỗi pixel: lấy trung bình cộng của các pixel trong bán kính `radius`
        // theo 1 chiều (ngang nếu horizontal=true, dọc nếu false)
        ...
    }
}
```
Đây là bộ lọc duy nhất **đọc trực tiếp mảng số nguyên của từng pixel** (`getPixels()`/`setPixels()`) thay vì dùng `Canvas`. Kỹ thuật "box blur 2 lượt" (chạy ngang trước, rồi chạy dọc kết quả đó) cho ra hiệu ứng giống Gaussian blur nhưng tính toán rẻ hơn nhiều — mỗi lượt chỉ là phép trung bình cộng theo 1 chiều.

---

## 5. `ColorAdjustFilter` — phần phức tạp nhất, đứng sau tab "Tuỳ chỉnh"

**File:** `app/src/main/java/com/example/photofilter/domain/filter/ColorAdjustFilter.java`

```java
public class ColorAdjustFilter extends BaseFilter {

    private static final float LUM_R = 0.213f;
    private static final float LUM_G = 0.715f;
    private static final float LUM_B = 0.072f;

    private final float brightness;
    private final float contrast;
    private final float saturation;
    private final float hue;
    private final float exposure;

    public ColorAdjustFilter(float brightness, float contrast, float saturation, float hue, float exposure) {
        this.brightness = brightness;
        this.contrast = contrast;
        this.saturation = saturation;
        this.hue = hue;
        this.exposure = exposure;
    }

    @Override
    protected Bitmap process(Bitmap source) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(saturation / 100f);

        if (hue != 0f) {
            matrix.postConcat(hueRotationMatrix(hue));
        }

        float contrastScale = contrast / 100f;
        float brightnessOffset = brightness / 100f * 255f;
        float translate = brightnessOffset + (1f - contrastScale) * 127.5f;
        float[] brightnessContrast = {
                contrastScale, 0f, 0f, 0f, translate,
                0f, contrastScale, 0f, 0f, translate,
                0f, 0f, contrastScale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
        };
        matrix.postConcat(new ColorMatrix(brightnessContrast));

        if (exposure != 0f) {
            float exposureScale = (float) Math.pow(2f, exposure / 100f);
            float[] exposureMatrix = {
                    exposureScale, 0f, 0f, 0f, 0f,
                    0f, exposureScale, 0f, 0f, 0f,
                    0f, 0f, exposureScale, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
            };
            matrix.postConcat(new ColorMatrix(exposureMatrix));
        }

        return applyColorMatrix(source, matrix);
    }

    private static ColorMatrix hueRotationMatrix(float degrees) {
        float radians = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float[] m = {
                LUM_R + cos * (1 - LUM_R) - sin * LUM_R, LUM_G - cos * LUM_G - sin * LUM_G, LUM_B - cos * LUM_B + sin * (1 - LUM_B), 0f, 0f,
                LUM_R - cos * LUM_R + sin * 0.143f, LUM_G + cos * (1 - LUM_G) + sin * 0.140f, LUM_B - cos * LUM_B - sin * 0.283f, 0f, 0f,
                LUM_R - cos * LUM_R - sin * (1 - LUM_R), LUM_G - cos * LUM_G + sin * LUM_G, LUM_B + cos * (1 - LUM_B) + sin * LUM_B, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
        };
        return new ColorMatrix(m);
    }
}
```

**Giải thích từng phần (đây là bộ lọc hay bị hỏi kỹ nhất vì phức tạp nhất):**

1. **5 tham số, 2 thang đo khác nhau** — `brightness`/`hue`/`exposure` dùng thang `-100..100` (0 = không đổi gì); `contrast`/`saturation` dùng thang `0..200` (100 = không đổi gì). Lý do 2 thang khác nhau: khớp với cách UI đặt vị trí mặc định của thanh trượt (SeekBar) — thanh Sáng/Sắc độ/Phơi sáng để mặc định ở *giữa*, còn Tương phản/Bão hoà cũng để ở giữa nhưng số hiển thị lại quen thuộc hơn ở dạng "100%".

2. **Thứ tự ghép ma trận quan trọng:** `setSaturation` → xoay hue (nếu có) → sáng/tương phản → phơi sáng (nếu có). Đây là 4 bước xử lý riêng biệt được `postConcat` nối tiếp nhau vào **cùng 1** `ColorMatrix`, rồi chỉ vẽ `Canvas` **đúng 1 lần** ở cuối (qua `applyColorMatrix`) — thay vì vẽ 4 lần, tiết kiệm rất nhiều chi phí tính toán.

3. **`hueRotationMatrix(degrees)`** — công thức xoay màu sắc (hue) chuẩn, dựa trên `cos`/`sin` của góc xoay. Điểm hay: công thức này **giữ nguyên độ sáng cảm nhận của mắt người** nhờ 3 hằng số `LUM_R/LUM_G/LUM_B` (0.213/0.715/0.072 — hệ số chuẩn ITU-R BT.601 đo độ nhạy mắt người với từng màu, mắt nhạy với xanh lá nhất nên hệ số G lớn nhất). Nếu không có 3 hằng số này, xoay hue sẽ làm ảnh chỗ sáng chỗ tối bất thường.

4. **`exposure`** — mô phỏng khái niệm "exposure stop" trong nhiếp ảnh: công thức `2^(exposure/100)` nghĩa là mỗi +100 đơn vị = sáng gấp đôi (giống +1 EV khi chụp ảnh thật), nhân đều lên cả 3 kênh màu.

---

## 6. Tự kiểm tra bài — ví dụ test thật

**File:** `app/src/test/java/com/example/photofilter/domain/filter/GrayscaleFilterTest.java`

```java
@RunWith(RobolectricTestRunner.class)
public class GrayscaleFilterTest {
    @Test
    public void desaturatesToEqualChannels() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(200, 40, 90));

        Bitmap result = new GrayscaleFilter().apply(source);
        int pixel = result.getPixel(0, 0);
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);

        assertEquals(r, g);
        assertEquals(g, b);
        assertTrue("Kết quả không nên là màu tuyệt đối đen hoặc trắng", r > 0 && r < 255);
    }
}
```
**Cách nghĩ khi viết test cho 1 bộ lọc ảnh:** không cần ảnh thật to — tạo 1 `Bitmap` bé tí (1×1 pixel!) với 1 màu biết trước (`Color.rgb(200, 40, 90)`), chạy bộ lọc, rồi kiểm tra pixel kết quả có đúng tính chất mong đợi không. Với Grayscale: đặc điểm của "đen trắng" là 3 kênh R/G/B phải bằng nhau — test không kiểm tra con số cụ thể, chỉ kiểm tra *tính chất* đó đúng.

**File:** `app/src/test/java/com/example/photofilter/domain/filter/ColorAdjustFilterTest.java` — 4 test tiêu biểu cho bộ lọc phức tạp nhất:

```java
@Test
public void neutralValues_leavePixelUnchanged() {
    // brightness=0, contrast=100, saturation=100, hue=0, exposure=0 → không đổi gì cả
    Bitmap result = new ColorAdjustFilter(0f, 100f, 100f, 0f, 0f).apply(source);
    assertEquals(120, Color.red(pixel));   // giữ nguyên y hệt input
}

@Test
public void positiveBrightness_lightensPixel() {
    // brightness=50 → pixel phải sáng hơn ban đầu
    assertTrue(Color.red(pixel) > 100);
}

@Test
public void positiveExposure_lightensPixel() { ... }  // tương tự, cho exposure

@Test
public void hueRotation_shiftsColor() {
    // xoay hue 120 độ → màu phải đổi, không được giữ y hệt màu đỏ ban đầu
    assertTrue(Color.red(pixel) != 200 || Color.green(pixel) != 60 || Color.blue(pixel) != 60);
}
```
**Điểm hay:** test đầu tiên (`neutralValues_leavePixelUnchanged`) là test "về 0" — kiểm tra khi mọi tham số ở giá trị mặc định thì ảnh phải giữ nguyên y hệt, không lệch dù 1 đơn vị màu. Đây là cách kiểm tra rất hiệu quả cho code có nhiều tham số: đảm bảo "không bật gì thì không đổi gì" trước khi tin tưởng các trường hợp phức tạp hơn.

Cùng khuôn mẫu này được lặp lại cho 6 file test còn lại của Tú Anh: `NegativeFilterTest`, `FilmFilterTest`, `MonoFilterTest`, `RetroFilterTest`, `VintageFilterTest`, `VignetteFilterTest`, `BlurFilterTest` — mỗi file kiểm tra đúng 1 tính chất đặc trưng của bộ lọc đó (ví dụ `BlurFilterTest` kiểm tra pixel sau khi mờ phải "gần" giá trị trung bình của các pixel lân cận, không kiểm tra từng con số tuyệt đối).

---

## 7. README.md — tài liệu giới thiệu (Tú Anh viết với vai trò trưởng nhóm)

Trích đúng phần Tú Anh khai trong `README.md`, mục "Phân công thành viên":

```markdown
### Tú Anh — Trưởng nhóm · Domain layer (Filter engine)

- Thiết kế `interface Filter` + `abstract class BaseFilter` theo mẫu Template Method —
  đáp ứng yêu cầu bắt buộc về khả năng mở rộng (OOP)
- Triển khai toàn bộ 13 bộ lọc: Original, Grayscale, Negative (3 bộ bắt buộc), Sepia,
  ColorTone (Ấm/Lạnh), BrightnessContrast, Vintage, Vignette (Canvas + RadialGradient),
  Blur (xử lý mảng pixel thủ công), Film, Mono, Retro
- `ColorAdjustFilter`: chỉnh Brightness/Contrast/Saturation/Hue/Exposure thời gian thực,
  bao gồm công thức xoay sắc độ (hue rotation matrix)
- Viết unit test Robolectric cho toàn bộ filter kể trên
- Viết README, rà soát code và chạy lại test trước khi nộp bài, phụ trách merge nhánh
  của cả nhóm cuối mỗi ngày
```

> ⚠️ **Lưu ý khi đọc lại README:** file này viết trước "Ngày 7" nên vài chỗ khác ở phần của Phan Lê Huy đã cũ so với code hiện tại — ví dụ mục "Chỉnh ảnh" ghi "Đổi cỡ (75–200%)" nhưng tính năng này **đã bị gỡ bỏ** ở Ngày 7, và mục Đăng nhập ghi "Firebase Authentication" nhưng thực tế `AuthRepository` dùng **SQLite thuần cục bộ** (không phải Firebase). Nếu thầy hỏi dựa trên README, nên trả lời theo code thật (đã mô tả ở các mục trên), không theo đúng câu chữ README.

---

## 8. Tài liệu đặc tả (spec/plan) — Ngày 7

Trước khi cả nhóm code 2 tính năng lớn gần nhất, Tú Anh viết trước tài liệu mô tả kế hoạch, nằm ở:

- `docs/superpowers/specs/2026-07-28-editor-history-apply-cancel-design.md` — đặc tả tính năng Undo/Redo + Apply/Cancel
- `docs/superpowers/plans/2026-07-28-editor-history-apply-cancel.md` — kế hoạch triển khai chia nhỏ từng bước cho tính năng trên
- `docs/superpowers/specs/2026-07-29-home-intro-design.md` — đặc tả màn Chào mừng (Intro)
- `docs/superpowers/plans/2026-07-29-home-intro.md` — kế hoạch triển khai màn Intro
- `docs/superpowers/specs/2026-07-29-sticker-overlay-design.md` — đặc tả tính năng Sticker

**Vì sao viết trước rồi mới code?** Đây là cách làm "thiết kế trước, code sau" — với tính năng phức tạp như Undo/Redo (đụng vào toàn bộ 5 tab chỉnh sửa cùng lúc), viết rõ trước "sẽ làm gì, theo thứ tự nào, ai kiểm tra lại" giúp tránh vừa code vừa đổi ý giữa chừng, và nếu có nhiều người cùng làm thì ai cũng đọc chung 1 bản kế hoạch, không hiểu sai ý nhau.

---

*File này đi cùng cặp với `NHIEM_VU_TU_ANH.md` — đọc file đó trước nếu muốn hiểu ý nghĩa tổng quan, đọc file này nếu cần trích dẫn code cụ thể khi vấn đáp.*

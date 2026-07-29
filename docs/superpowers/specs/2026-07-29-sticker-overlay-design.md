# Tab "Sticker" — dán 1 sticker lên ảnh (kéo/pinch-zoom/xoay)

Ngày: 2026-07-29
Trạng thái: Chờ user duyệt spec

## Bối cảnh

App đã có sẵn 15 sticker PNG (nền trong suốt, 618×618, giấy phép OpenMoji CC BY-SA 4.0) ở `app/src/main/assets/stickers/`, và có sẵn mô hình draft → Apply/Cancel + Undo/Redo toàn cục cho 4 tab (Bộ lọc/Cắt/Tuỳ chỉnh/AI) vừa hoàn thiện. Muốn thêm tab thứ 5 "Sticker": chọn 1 sticker từ danh sách, kéo/phóng to-nhỏ/xoay bằng cử chỉ 2 ngón (kiểu Instagram/Snapchat), Áp dụng sẽ "in" sticker lên ảnh thật và ghi vào lịch sử Undo/Redo như mọi tool khác.

## Mục tiêu

- Tab mới "Sticker" trong `bottomNavBar` (sau AI), panel gồm 1 hàng ngang cuộn 15 thumbnail sticker.
- Chạm 1 sticker → hiện giữa ảnh, kích thước mặc định = 30% chiều ngang ảnh, góc xoay 0.
- Kéo 1 ngón = di chuyển; 2 ngón = vừa phóng to/nhỏ vừa xoay cùng lúc (chuẩn multi-touch: theo dõi khoảng cách + góc giữa 2 điểm chạm qua từng frame).
- Chạm sticker khác trong lúc đang chỉnh: đổi hình, giữ nguyên vị trí/tỉ lệ/góc xoay hiện tại.
- Xác nhận: "in" sticker (đúng như đang hiển thị) lên ảnh ở độ phân giải đầy đủ, commit thành 1 bước trong `EditHistory` — tham gia Undo/Redo bình thường.
- Huỷ (hoặc rời tab mà không Xác nhận): không đổi gì, giống mọi tab khác.
- Đúng 1 sticker tại 1 thời điểm (không chồng nhiều sticker) — chọn/dán sticker mới ở lần mở tab sau sẽ đè lên ảnh đã có (kể cả ảnh đã có sticker trước đó từ 1 lần Áp dụng khác), không phải "nhiều lớp" trong cùng 1 phiên.

## Ngoài phạm vi (non-goal)

- Không hỗ trợ nhiều sticker cùng lúc trong 1 phiên chỉnh.
- Không có nút xoá sticker riêng — Huỷ đã đảm nhiệm việc "không muốn sticker này".
- Không thêm được sticker mới ngoài 15 cái có sẵn (không có UI tải sticker từ nguồn ngoài).
- Không tính toán lại bitmap thật liên tục lúc đang kéo/xoay (chỉ là overlay hiển thị trên màn hình — xem phần Kiến trúc) — tránh giật/lag khi thao tác.

## Kiến trúc

### 1. `StickerRepository` (mới, `data/` layer)

Danh sách 15 tên file cố định (giống cách `FilterRepository` liệt kê filter có sẵn), đọc bitmap từ `AssetManager`:

```java
public class StickerRepository {
    private static final String[] STICKER_ASSET_NAMES = {
        "sticker_heart", "sticker_star", "sticker_fire", "sticker_sunglasses",
        "sticker_laugh_tears", "sticker_party_popper", "sticker_thumbs_up",
        "sticker_rainbow", "sticker_sparkles", "sticker_crown", "sticker_balloon",
        "sticker_camera", "sticker_kiss", "sticker_clap", "sticker_heart_eyes"
    };

    public List<String> listStickerNames() {
        return Collections.unmodifiableList(Arrays.asList(STICKER_ASSET_NAMES));
    }

    /** @return null nếu không đọc được asset (không nên xảy ra vì đây là asset đóng gói sẵn). */
    public Bitmap loadSticker(Context context, String name) {
        try (InputStream in = context.getAssets().open("stickers/" + name + ".png")) {
            return BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            return null;
        }
    }
}
```

### 2. `StickerOverlayView` (mới, `ui/` layer) — hiển thị & thao tác, KHÔNG đụng bitmap thật

Tương tự tinh thần `CropOverlayView`: nhận vùng ảnh hiển thị thật qua `setImageBounds(RectF)` (bù trừ letterbox của `fitCenter`, tái dùng đúng `MainActivity.computeImageDisplayBounds()` đã có). Khác `CropOverlayView` ở chỗ nó không kéo 4 góc 1 hình chữ nhật, mà kéo/xoay/scale 1 bitmap:

```java
final class StickerOverlayView extends View {
    private Bitmap stickerBitmap;
    private RectF imageBounds;
    private float centerX, centerY;      // toạ độ pixel trong hệ view, đã có giá trị mặc định khi setSticker() lần đầu
    private float scale = 1f;             // hệ số nhân lên kích thước gốc bitmap
    private float rotationDegrees = 0f;

    void setImageBounds(RectF bounds) { imageBounds = bounds; }

    /** Đổi hình; nếu đã có sticker trước đó thì GIỮ NGUYÊN centerX/centerY/scale/rotationDegrees. */
    void setSticker(Bitmap bitmap) {
        boolean firstTime = stickerBitmap == null;
        stickerBitmap = bitmap;
        if (firstTime && imageBounds != null) {
            centerX = imageBounds.centerX();
            centerY = imageBounds.centerY();
            scale = (imageBounds.width() * 0.3f) / bitmap.getWidth(); // mac dinh 30% chieu ngang anh
        }
        invalidate();
    }

    // onTouchEvent(): theo dõi 1 ngón (ACTION_MOVE của 1 pointer -> cộng dồn vào centerX/centerY)
    // và 2 ngón (ACTION_POINTER_DOWN trở lên -> ghi nhận khoảng cách D0 và góc A0 ban đầu giữa 2 pointer;
    // moi ACTION_MOVE tiep theo: D1/A1 moi -> scale *= D1/D0; rotationDegrees += (A1 - A0); roi cap nhat D0=D1, A0=A1
    // cho frame ke tiep). Chuan multi-touch translate+scale+rotate, khong dung thu vien ngoai.

    @Override
    protected void onDraw(Canvas canvas) {
        if (stickerBitmap == null) return;
        Matrix matrix = new Matrix();
        matrix.postTranslate(-stickerBitmap.getWidth() / 2f, -stickerBitmap.getHeight() / 2f);
        matrix.postScale(scale, scale);
        matrix.postRotate(rotationDegrees);
        matrix.postTranslate(centerX, centerY);
        canvas.drawBitmap(stickerBitmap, matrix, null);
    }

    /** @return normalized theo imageBounds — dùng để "in" lên bitmap thật ở độ phân giải đầy đủ lúc Áp dụng. */
    StickerPlacement getNormalizedPlacement() {
        float cxFraction = (centerX - imageBounds.left) / imageBounds.width();
        float cyFraction = (centerY - imageBounds.top) / imageBounds.height();
        float scaleFraction = (scale * stickerBitmap.getWidth()) / imageBounds.width();
        return new StickerPlacement(cxFraction, cyFraction, scaleFraction, rotationDegrees);
    }
}
```

(`StickerPlacement` là 1 record/POJO 4 field float — không cần tách file riêng, để nested trong `StickerOverlayView` hoặc `EditorContract`.)

### 3. `EditorContract` / `EditorPresenter` — 1 method mới, không đụng `onApplyRequested()` chung

```java
void onStickerApplyRequested(Bitmap stickerBitmap, float centerXFraction, float centerYFraction,
                              float scaleFraction, float rotationDegrees);
```

`EditorPresenter` triển khai: lấy `history.current()` làm nền, tính toạ độ pixel thật từ 4 số normalized trên (nhân với width/height thật của ảnh nền), vẽ sticker đè lên bằng `Canvas` + `Matrix` (translate–scale–rotate, cùng công thức như `onDraw()` ở trên nhưng trên bitmap thật thay vì canvas màn hình), rồi `history.commit(result, label)` + `afterHistoryChange()` — **giống hệt đuôi của mọi Apply khác**, chỉ khác ở chỗ tính `result` như thế nào.

### 4. `MainActivity` — rẽ nhánh đúng 1 chỗ (nút Xác nhận), theo đúng tiền lệ đang có cho Cắt tự do

```java
findViewById(R.id.toolApplyButton).setOnClickListener(v -> {
    if (activeTab == TAB_STICKER && stickerOverlayView.hasSticker()) {
        StickerPlacement p = stickerOverlayView.getNormalizedPlacement();
        presenter.onStickerApplyRequested(stickerOverlayView.getStickerBitmap(),
                p.centerXFraction, p.centerYFraction, p.scaleFraction, p.rotationDegrees);
    } else if (activeTab != TAB_STICKER) {
        presenter.onApplyRequested();
    }
    // activeTab == TAB_STICKER && !hasSticker(): coi như Huỷ, không gọi gì thêm
    if (customCropActive) cancelCustomCrop();
    closeSheet();
});
```

Panel Sticker: 1 `RecyclerView` ngang, adapter đơn giản (danh sách tên + bitmap preview đọc qua `StickerRepository`, decode 1 lần khi mở tab lần đầu, cache lại — không decode lại mỗi lần mở tab). Chạm 1 item → `stickerOverlayView.setSticker(bitmap)` trực tiếp, **không qua Presenter** (thuần UI, không có logic nghiệp vụ nào ở bước chọn/xem trước).

## Trường hợp biên

- **Chọn tab Sticker nhưng không chạm sticker nào rồi bấm Xác nhận:** coi như Huỷ (không commit gì) — nhất quán với `onApplyRequested_withNoChange_doesNotEnableUndo` của các tab khác.
- **Sticker kéo ra ngoài vùng ảnh một phần:** cho phép (giống Instagram) — không giới hạn `centerX`/`centerY` trong `imageBounds`, chỉ khi "in" lên bitmap thật thì phần nằm ngoài biên bị Android `Canvas` tự cắt (clip) một cách tự nhiên, không cần code riêng.
- **Xoay/scale không giới hạn:** không đặt min/max cho `scale` — tránh phức tạp hoá, người dùng có thể phóng quá to hoặc quá nhỏ nếu muốn (giống hầu hết app dán sticker thật).

## Testing

- `StickerRepository`: unit test Robolectric — `listStickerNames()` trả đúng 15 tên, `loadSticker()` cho mỗi tên trả về bitmap khác `null` (mirror `FilterRepositoryTest` nếu có, hoặc pattern test đơn giản của các Repository khác).
- `EditorPresenter.onStickerApplyRequested()`: unit test được — đây là phép toán bitmap thuần (`Canvas`/`Matrix`), giống cách `CropUtilsTest` test `customCrop()`. Test: tạo ảnh nền known-size, "dán" sticker ở toạ độ normalized đã biết, assert ảnh kết quả có đúng kích thước ảnh nền (không đổi kích thước ảnh gốc) và không bị `null`/crash.
- `StickerOverlayView` (touch/gesture): **không viết test tự động** — giống `CropOverlayView` hiện tại không có test cho phần kéo-thả tương tác, chỉ xác minh thủ công trên emulator ở task cuối cùng của plan.

## File thay đổi

- Thêm: `StickerRepository.java`, `StickerRepositoryTest.java`, `StickerOverlayView.java`, `layout/item_sticker_thumbnail.xml` (hoặc tái dùng layout thumbnail filter nếu giống hệt), `drawable/ic_tab_sticker.xml`.
- Sửa: `EditorContract.java`, `EditorPresenter.java`, `EditorPresenterTest.java` (thêm test cho `onStickerApplyRequested`), `MainActivity.java`, `activity_main.xml` (thêm nav icon thứ 5 + panel Sticker + `StickerOverlayView`), `strings.xml` (nhãn tab, content description).

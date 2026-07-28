# Editor: Undo/Redo lịch sử chỉnh sửa + Apply/Cancel theo từng tab

Ngày: 2026-07-28
Trạng thái: Chờ user duyệt spec

## Bối cảnh & vấn đề

Trong màn hình chỉnh sửa (`MainActivity` + `EditorPresenter`), phát hiện qua review so với img.ly PESDK:

1. **Bug:** nút "Original" trong tab Cắt không đưa ảnh về gốc được.
   `EditorPresenter.onCropRequested()` khi `ratio == CropRatio.ORIGINAL` chỉ `return` — no-op tuyệt đối (kể cả có 1 unit test đang coi hành vi này là "đúng": `onCropRequested_withOriginalRatio_doesNothing`).
   Gốc rễ sâu hơn: `originalBitmap` bị **ghi đè + recycle() ngay** mỗi lần Rotate/Flip/Crop/Resize (`replaceOriginalBitmap()`), nên ảnh gốc thật sự (lúc mới chọn/mới chụp) không được giữ lại ở đâu cả — không có gì để "quay về" dù có sửa nhánh `return` kia.

2. **Thiếu Apply/Cancel nhất quán + không có Undo/Redo:** chỉ riêng Cắt tự do (kéo góc) có Cancel/Xác nhận. Mọi thao tác khác (tỷ lệ Cắt có sẵn, Xoay, Lật, Resize, 5 thanh Tuỳ chỉnh, chọn Bộ lọc, 4 công cụ AI) áp dụng ngay và vĩnh viễn khi bấm/kéo. Không có bất kỳ Undo/Redo nào trong toàn bộ codebase.

Đã được user xác nhận chọn phương án lớn nhất: sửa bug + thêm Undo/Redo toàn cục + Apply/Cancel cho **cả 4 tab** (Bộ lọc/Cắt/Tuỳ chỉnh/AI).

## Mục tiêu

- "Cắt → Original" luôn đưa ảnh về đúng ảnh gốc lúc mới chọn/chụp, bất kể đã chỉnh sửa bao nhiêu bước trước đó.
- Mọi tab (Bộ lọc/Cắt/Tuỳ chỉnh/AI) đều: mở tab = vào chế độ xem trước (nháp); Apply = chốt; Cancel (hoặc rời tab mà không bấm gì) = huỷ nháp, không đổi gì.
- Undo/Redo toàn cục, hoạt động cho mọi loại thao tác đã Apply (kể cả AI).
- Không đổi cách 13 bộ lọc / AI tools / SQLite / Auth hoạt động — chỉ đổi tầng điều phối trạng thái ảnh trong Presenter + UI tương ứng.

## Ngoài phạm vi (non-goal)

- Không làm undo/redo bền vững qua việc thoát app (chỉ tồn tại trong phiên chỉnh sửa hiện tại, giống hầu hết app ảnh mobile).
- Không đổi thuật toán bên trong các Filter/AI repository.
- Thumbnail bộ lọc vẫn tính từ ảnh nền hiện tại (không tính chồng filter đang chọn lên chính nó) — giữ nguyên hành vi cũ, chỉ mở rộng thời điểm tính lại (xem phần Thumbnail bên dưới).

## Kiến trúc: `EditHistory`

Lớp mới `com.example.photofilter.presenter.EditHistory`, do `EditorPresenter` sở hữu và gọi (không lộ ra `EditorContract`).

```java
class EditHistory {
    private static final int MAX_ENTRIES = 15; // không tính pristineOriginal

    private Bitmap pristineOriginal;   // ảnh gốc lúc mới chọn/chụp — không bao giờ bị evict
    private Entry current;             // trạng thái đã chốt (Apply) gần nhất
    private final Deque<Entry> undoStack = new ArrayDeque<>();
    private final Deque<Entry> redoStack = new ArrayDeque<>();

    static final class Entry {
        final Bitmap bitmap;
        final String label; // tên hiển thị khi lưu (vd "Sepia", "Tuỳ chỉnh", "Xoá nền")
    }

    void reset(Bitmap freshBitmap, String label);      // ảnh mới được chọn/chụp: recycle hết, pristineOriginal = freshBitmap, current = Entry(freshBitmap, label)
    Bitmap current();
    String currentLabel();
    Bitmap pristineOriginal();
    boolean canUndo();
    boolean canRedo();
    void commit(Bitmap newBitmap, String label);        // đẩy `current` cũ vào undoStack (trim MAX_ENTRIES, recycle bitmap bị đẩy ra), current = Entry mới, clear+recycle redoStack
    Bitmap undo();                                        // current -> redoStack, pop undoStack -> current
    Bitmap redo();                                        // current -> undoStack, pop redoStack -> current
    void clearAll();                                      // gọi ở detachView/onDestroy, recycle sạch
}
```

**Vì sao lưu bitmap thay vì replay thao tác:** kết quả AI (Enhance/Sharpen/Khử nhiễu/Tăng độ phân giải/Xoá nền) không phải hàm thuần tuý của tham số — không thể "phát lại" từ danh sách thao tác. Phải lưu bitmap kết quả. Ảnh trong app đã downsample theo kích thước màn hình khi load nên 15 bản không nặng bất thường; `pristineOriginal` giữ riêng, không tính vào giới hạn 15, nên nút "Original" luôn đúng dù đã Undo/Redo/Apply bao nhiêu lần.

## Mô hình nháp (draft) thống nhất cho 4 tab

`EditorPresenter` thêm state: `Bitmap draftBaseBitmap`, `Bitmap draftBitmap`, `String draftLabel`.

- **Mở tab** (`onToolTabOpened()`): `draftBaseBitmap = history.current()`, `draftBitmap = draftBaseBitmap` (chưa copy — chỉ copy khi thao tác đầu tiên thực sự sinh bitmap mới), `view.showImage(draftBitmap)`.
- **Apply** (`onApplyRequested()`): nếu `draftBitmap != draftBaseBitmap` (có thay đổi thật) → `history.commit(draftBitmap, draftLabel)`, `lastSavedUri = null`, cập nhật Undo/Redo state, regenerate thumbnails từ `history.current()`. Nếu không có gì đổi (user mở tab rồi Apply luôn) → coi như Cancel, không tạo bước lịch sử rác.
- **Cancel** (`onCancelRequested()`), hoặc chuyển tab / thu gọn sheet mà chưa bấm Apply/Cancel: recycle `draftBitmap` nếu khác `draftBaseBitmap`/`history.current()`, `view.showImage(history.current())`.

**Hai kiểu tab khác nhau** (đã ngụ ý trong câu trả lời trước của bạn — Cắt "gộp chung 1 phiên nháp" tỷ lệ/tự do/xoay/lật/resize):

- **Không tích luỹ** (Bộ lọc, Tuỳ chỉnh): mỗi thao tác tính lại **từ `draftBaseBitmap`** (ảnh lúc mở tab), không chồng lên thao tác trước trong cùng phiên. Khớp code hiện tại (đổi filter/kéo slider luôn áp lại từ đầu, không cộng dồn).
- **Tích luỹ** (Cắt, AI): mỗi thao tác tính lại **từ `draftBitmap` hiện tại**, cho phép gộp nhiều bước trong 1 lần Apply (vd Xoay rồi Cắt vuông rồi Resize = 1 bước lịch sử). Riêng nút **"Original"** trong Cắt luôn là ngoại lệ: bỏ qua chuỗi tích luỹ, `draftBitmap = copy(history.pristineOriginal())` — đây chính là chỗ sửa bug.

## Undo/Redo toàn cục

- 2 nút icon trên top bar cạnh nút Lưu (thêm `ic_undo.xml`, `ic_redo.xml`, string `action_undo`="Hoàn tác", `action_redo`="Làm lại").
- Enable/disable theo `history.canUndo()`/`canRedo()`, cập nhật qua `EditorContract.View.showUndoRedoAvailability(boolean canUndo, boolean canRedo)`.
- Apply mới sau khi Undo → `history.commit()` tự clear redoStack (chuẩn hành vi undo/redo thông thường).
- Chọn ảnh mới (`onImagePicked`) → `history.reset(...)`, xoá sạch lịch sử cũ.

## Thay đổi Contract

**`EditorContract.Presenter`** — thêm:

```java
void onToolTabOpened();
void onApplyRequested();
void onCancelRequested();
void onUndoRequested();
void onRedoRequested();
```

Các method theo-tool hiện có (`onFilterSelected`, `onAdjustValuesChanged`, `onCropRequested`, `onCustomCropRequested`, `onRotateRequested`, `onFlipRequested`, `onResizeRequested`, `onSharpenRequested`, `onRemoveNoiseRequested`, `onUpscaleRequested`, `onBackgroundRemovalRequested`) giữ nguyên chữ ký, nhưng bên trong giờ chỉ sửa `draftBitmap` + gọi `view.showImage(...)`, không commit thẳng nữa.

**`EditorContract.View`** — gộp `showOriginalImage`/`showFilteredImage` thành một `void showImage(Bitmap bitmap)` (không còn khái niệm "ảnh gốc" khác "ảnh đã lọc" ở tầng View — chỉ còn "ảnh đang hiển thị"). Thêm `void showUndoRedoAvailability(boolean canUndo, boolean canRedo)`.

## Thay đổi UI (`MainActivity` + `activity_main.xml`)

- Thay `cropCustomActionsRow` (đang chỉ dùng cho Cắt tự do) bằng **1 hàng Apply/Cancel dùng chung**, đặt dưới `NestedScrollView` chứa 4 panel, luôn hiện khi sheet đang mở — tái dùng string `action_confirm`/`action_cancel` sẵn có. Nút "Xác nhận"/"Huỷ" riêng của khung kéo-góc Cắt tự do (`cropOverlayView`) **giữ nguyên** như một bước con bên trong phiên nháp Cắt (chốt vùng chọn vào draft), khác với hàng Apply/Cancel ngoài cùng (chốt cả phiên Cắt vào lịch sử).
- `onTabTapped`: khi mở tab mới → gọi `presenter.onToolTabOpened()`; khi chuyển tab/thu sheet mà chưa Apply → gọi `presenter.onCancelRequested()` (tổng quát hoá pattern `cancelCustomCrop()` đang có).
- Thêm `undoTopBarButton`/`redoTopBarButton` cạnh `saveTopBarButton`.

## Kế hoạch test (`EditorPresenterTest`)

Viết lại các test đang giả định "commit ngay lập tức" sang luồng `onToolTabOpened → act → onApplyRequested → assert`. Thêm:

- `onCropRequested_original_afterPriorCrop_restoresPristineDimensions` — test hồi quy đúng cho bug đã báo.
- `onCancelRequested_afterFilterSelected_leavesHistoryUnchanged`.
- `onApplyRequested_withNoChange_doesNotPushHistoryEntry`.
- `onUndoRequested_afterApply_restoresPreviousBitmap` / `onRedoRequested_afterUndo_reappliesChange`.
- `onApplyRequested_afterUndo_clearsRedoStack`.
- Test giới hạn 15 bước: commit 16 lần, entry cũ nhất (không phải pristine) bị recycle; `pristineOriginal` vẫn dùng được qua "Original".

## Chi tiết nhỏ ăn theo

- `EditHistory.reset()` gán label khởi tạo = `R.string.filter_original` ("Màu gốc"). Nhờ vậy `EditorPresenter.performSave()` không cần nhánh fallback `currentFilterLabel != null ? ... : filter_original` như hiện tại — luôn có `history.currentLabel()` hợp lệ.
- Thứ tự triển khai gợi ý (sẽ chốt cụ thể hơn ở bước viết plan): (1) `EditHistory` + unit test riêng, (2) refactor `EditorContract`/`EditorPresenter` dùng `EditHistory` + viết lại `EditorPresenterTest`, (3) UI (`MainActivity`, `activity_main.xml`, icon/string mới), (4) chạy thử thủ công trên app thật.

## Việc KHÔNG đổi

- Thuật toán filter/AI, SQLite (History/Favorite/User), Auth, màn Home.
- Giới hạn Undo = 15 bước là hằng số hard-code, không cần cấu hình UI.

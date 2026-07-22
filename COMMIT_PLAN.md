# Kế hoạch commit theo ngày — HATFilter (6 ngày, có nhánh)

Nhóm trưởng: **Tú Anh**. Thành viên: **Trần Tú**, **Phan Lê Huy**.

## Quy ước nhánh

Mỗi người có **1 nhánh riêng dùng xuyên suốt cả 6 ngày** (tạo 1 lần, dùng lại mỗi ngày):

```bash
git checkout -b nhanh-tuanh     # Tú Anh tạo 1 lần duy nhất
git checkout -b nhanh-trantu    # Trần Tú tạo 1 lần duy nhất
git checkout -b nhanh-huy       # Phan Lê Huy tạo 1 lần duy nhất
```

Quy trình mỗi ngày:

1. Mỗi người `git checkout nhanh-<ten-minh>`, đổi danh tính git, commit phần việc trong ngày (lệnh cụ thể ở dưới).
2. Cuối ngày, **1 người (gợi ý: Tú Anh, vì là nhóm trưởng)** merge cả 3 nhánh vào `main`:

   ```bash
   git checkout main
   git pull origin main          # nếu server đã có gì thì cập nhật trước
   git merge nhanh-tuanh --no-ff
   git merge nhanh-trantu --no-ff
   git merge nhanh-huy --no-ff
   git push origin main
   ```

   Vì 3 người luôn động vào các file khác nhau trong cùng 1 ngày nên merge sẽ không bị conflict.
3. Hôm sau, mỗi người quay lại đúng nhánh cũ của mình (`git checkout nhanh-<ten-minh>`) và làm tiếp — **không tạo nhánh mới mỗi ngày**.

---

## Ngày 1 — Khởi tạo (Phan Lê Huy) — ĐÃ XONG ✅

Đã commit thẳng lên `main`: cấu hình Gradle/AGP, `.gitignore`, icon/theme mặc định của Android Studio, khung test mẫu. Không cần làm lại, không cần nhánh cho phần này (coi như điểm xuất phát chung).

**Trước khi sang Ngày 2, tạo 3 nhánh ở trên (mỗi người tạo nhánh của mình từ `main` hiện tại).**

---

## Ngày 2 — Lõi filter bắt buộc + nền dữ liệu

**Chủ đề:** dựng interface/abstract class (yêu cầu bắt buộc của đề bài) và đường dẫn đọc/ghi ảnh cơ bản.

### Tú Anh — 7 commit

```bash
git checkout nhanh-tuanh
git config user.name "Tu Anh"
git config user.email "<email_tuanh>"

git add app/src/main/java/com/example/photofilter/domain/filter/Filter.java
git commit -m "feat(domain): them interface Filter"

git add app/src/main/java/com/example/photofilter/domain/filter/BaseFilter.java
git commit -m "feat(domain): them abstract class BaseFilter (template method)"

git add app/src/main/java/com/example/photofilter/domain/filter/OriginalFilter.java
git commit -m "feat(domain): trien khai OriginalFilter (Mau goc)"

git add app/src/main/java/com/example/photofilter/domain/filter/GrayscaleFilter.java
git commit -m "feat(domain): trien khai GrayscaleFilter (Trang den)"

git add app/src/main/java/com/example/photofilter/domain/filter/NegativeFilter.java
git commit -m "feat(domain): trien khai NegativeFilter (Am ban)"

git add app/src/test/java/com/example/photofilter/domain/filter/GrayscaleFilterTest.java app/src/test/resources/robolectric.properties
git commit -m "test(domain): them unit test cho GrayscaleFilter, cau hinh Robolectric native graphics"

git add app/src/test/java/com/example/photofilter/domain/filter/NegativeFilterTest.java
git commit -m "test(domain): them unit test cho NegativeFilter"
```

### Phan Lê Huy — 1 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/build.gradle.kts
git commit -m "chore(gradle): them dependency RecyclerView, Glide, ExifInterface, Robolectric, Mockito"
```

### Trần Tú — 3 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/res/values/strings.xml
git commit -m "feat(resource): them chuoi ten filter va nhan giao dien co ban"

git add app/src/main/java/com/example/photofilter/data/FilterItem.java app/src/main/java/com/example/photofilter/data/FilterRepository.java
git commit -m "feat(data): them FilterItem va FilterRepository quan ly danh sach filter"

git add app/src/main/java/com/example/photofilter/data/ImageRepository.java
git commit -m "feat(data): them ImageRepository - downsample anh, xoay theo EXIF, luu qua MediaStore"
```

### Cuối Ngày 2 — merge (Tú Anh)

```bash
git checkout main
git merge nhanh-tuanh --no-ff
git merge nhanh-trantu --no-ff
git merge nhanh-huy --no-ff
git push origin main
```

---

## Ngày 3 — Kiến trúc MVP + giao diện chính

**Chủ đề:** nối Presenter/Contract (tách View khỏi business logic) và dựng màn hình chỉnh sửa, theo hệ màu **glassmorphism xanh dương/cyan** làm chuẩn luôn từ đầu.

### Tú Anh — 3 commit

```bash
git checkout nhanh-tuanh
git config user.name "Tu Anh"
git config user.email "<email_tuanh>"

git add app/src/main/java/com/example/photofilter/domain/filter/SepiaFilter.java
git commit -m "feat(domain): trien khai SepiaFilter"

git add app/src/main/java/com/example/photofilter/domain/filter/ColorToneFilter.java
git commit -m "feat(domain): trien khai ColorToneFilter (tong Am/Lanh)"

git add app/src/main/java/com/example/photofilter/domain/filter/BrightnessContrastFilter.java
git commit -m "feat(domain): trien khai BrightnessContrastFilter"
```

### Trần Tú — 4 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/java/com/example/photofilter/data/FilterRepository.java
git commit -m "feat(data): dang ky filter Sepia, Am, Lanh, Sang vao FilterRepository"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/FilterThumbnail.java
git commit -m "feat(presenter): them EditorContract dinh nghia hop dong MVP va FilterThumbnail view-model"

git add app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java
git commit -m "feat(presenter): them EditorPresenter dieu phoi luong nen va quan ly vong doi Bitmap"

git add app/src/test/java/com/example/photofilter/presenter/FakeView.java app/src/test/java/com/example/photofilter/presenter/ImmediateExecutorService.java app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "test(presenter): them FakeView, ImmediateExecutorService va unit test EditorPresenter"
```

### Phan Lê Huy — 5 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/font/ app/src/main/res/values/colors.xml app/src/main/res/values/themes.xml
git commit -m "feat(ui): thiet lap font Sora/Inter va bang mau glassmorphism xanh duong"

git add app/src/main/res/drawable/bg_button_ghost.xml app/src/main/res/drawable/bg_button_primary.xml app/src/main/res/drawable/bg_button_solid.xml app/src/main/res/drawable/bg_card_surface.xml app/src/main/res/drawable/bg_filter_thumbnail.xml app/src/main/res/drawable/bg_sheet_top_rounded.xml app/src/main/res/drawable/bg_glass_backdrop.xml app/src/main/res/drawable/selector_filter_selected.xml app/src/main/res/drawable/dot_accent.xml app/src/main/res/color/filter_name_text_color.xml
git commit -m "feat(ui): them cac drawable nen kinh mo, gradient nut va vien chon filter"

git add app/src/main/res/layout/activity_main.xml app/src/main/res/layout/item_filter_thumbnail.xml
git commit -m "feat(ui): them layout man hinh chinh va item filter thumbnail"

git add app/src/main/java/com/example/photofilter/ui/FilterAdapter.java app/src/main/java/com/example/photofilter/ui/GradientTextHelper.java
git commit -m "feat(ui): them FilterAdapter va hieu ung gradient cho wordmark"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java app/src/main/AndroidManifest.xml
git commit -m "feat(ui): them MainActivity - chon anh, ap filter, luu va chia se"
```

### Cuối Ngày 3 — merge (Tú Anh)

```bash
git checkout main
git merge nhanh-tuanh --no-ff
git merge nhanh-trantu --no-ff
git merge nhanh-huy --no-ff
git push origin main
```

---

## Ngày 4 — Lịch sử chỉnh sửa + Bộ lọc yêu thích (SQLite)

**Chủ đề:** thêm 2 database SQLite thuần (không dùng Room) và màn hình Lịch sử.

### Tú Anh — 6 commit

```bash
git checkout nhanh-tuanh
git config user.name "Tu Anh"
git config user.email "<email_tuanh>"

git add app/src/main/java/com/example/photofilter/domain/filter/VintageFilter.java
git commit -m "feat(domain): trien khai VintageFilter (Co dien)"

git add app/src/main/java/com/example/photofilter/domain/filter/VignetteFilter.java
git commit -m "feat(domain): trien khai VignetteFilter (Vien toi) - dung Canvas + RadialGradient"

git add app/src/main/java/com/example/photofilter/domain/filter/BlurFilter.java
git commit -m "feat(domain): trien khai BlurFilter (Mo nhe) - xu ly truc tiep mang pixel"

git add app/src/test/java/com/example/photofilter/domain/filter/VintageFilterTest.java
git commit -m "test(domain): them unit test cho VintageFilter"

git add app/src/test/java/com/example/photofilter/domain/filter/VignetteFilterTest.java
git commit -m "test(domain): them unit test cho VignetteFilter"

git add app/src/test/java/com/example/photofilter/domain/filter/BlurFilterTest.java
git commit -m "test(domain): them unit test cho BlurFilter"
```

### Trần Tú — 6 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/java/com/example/photofilter/data/FilterRepository.java
git commit -m "feat(data): dang ky filter Co dien, Vien toi, Mo nhe vao FilterRepository"

git add app/src/main/java/com/example/photofilter/data/HistoryDbHelper.java app/src/main/java/com/example/photofilter/data/HistoryEntry.java app/src/main/java/com/example/photofilter/data/HistoryRepository.java
git commit -m "feat(data): them HistoryDbHelper (SQLite thuan), HistoryEntry va HistoryRepository"

git add app/src/main/java/com/example/photofilter/data/FavoriteDbHelper.java app/src/main/java/com/example/photofilter/data/FavoriteRepository.java
git commit -m "feat(data): them FavoriteDbHelper (SQLite thuan) va FavoriteRepository"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java
git commit -m "feat(presenter): mo rong EditorContract/EditorPresenter - ghi lich su khi luu anh, xu ly onFavoriteToggled"

git add app/src/test/java/com/example/photofilter/data/HistoryRepositoryTest.java app/src/test/java/com/example/photofilter/data/FavoriteRepositoryTest.java
git commit -m "test(data): them unit test cho HistoryRepository va FavoriteRepository"

git add app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "test(presenter): them test cho attachView voi favorite"
```

### Phan Lê Huy — 4 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/menu/menu_main.xml app/src/main/AndroidManifest.xml
git commit -m "feat(ui): them menu Lich su tren toolbar va khai bao HistoryActivity"

git add app/src/main/res/layout/activity_history.xml app/src/main/res/layout/item_history_entry.xml
git commit -m "feat(ui): them layout man hinh Lich su chinh sua"

git add app/src/main/java/com/example/photofilter/ui/HistoryActivity.java app/src/main/java/com/example/photofilter/ui/HistoryAdapter.java
git commit -m "feat(ui): them HistoryActivity hien thi danh sach lich su"

git add app/src/main/res/layout/item_filter_thumbnail.xml app/src/main/java/com/example/photofilter/ui/FilterAdapter.java
git commit -m "feat(ui): them huy hieu yeu thich tren filter thumbnail (long-press)"
```

### Cuối Ngày 4 — merge (Tú Anh)

```bash
git checkout main
git merge nhanh-tuanh --no-ff
git merge nhanh-trantu --no-ff
git merge nhanh-huy --no-ff
git push origin main
```

---

## Ngày 5 — Camera, Xoay, Cắt, Tuỳ chỉnh màu thời gian thực

**Chủ đề:** hoàn thiện bộ công cụ chỉnh sửa nâng cao.

### Tú Anh — 2 commit

```bash
git checkout nhanh-tuanh
git config user.name "Tu Anh"
git config user.email "<email_tuanh>"

git add app/src/main/java/com/example/photofilter/domain/filter/ColorAdjustFilter.java
git commit -m "feat(domain): them ColorAdjustFilter cho chinh Brightness/Contrast/Saturation thoi gian thuc"

git add app/src/test/java/com/example/photofilter/domain/filter/ColorAdjustFilterTest.java
git commit -m "test(domain): them unit test cho ColorAdjustFilter"
```

### Trần Tú — 3 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/java/com/example/photofilter/data/ImageRepository.java
git commit -m "feat(data): them createCameraOutputUri tao noi luu anh chup qua MediaStore"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java
git commit -m "feat(presenter): them CropRatio, onAdjustValuesChanged/onRotateRequested/onCropRequested va trien khai adjust/rotate/crop"

git add app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "test(presenter): them test cho adjust va rotate"
```

### Phan Lê Huy — 2 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/layout/activity_main.xml
git commit -m "feat(ui): them hang nut Xoay/Cat/Tuy chinh va panel SeekBar dieu chinh mau"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): noi Camera (dialog chon nguon anh), Rotate, Crop va SeekBar vao MainActivity"
```

### Cuối Ngày 5 — merge (Tú Anh)

```bash
git checkout main
git merge nhanh-tuanh --no-ff
git merge nhanh-trantu --no-ff
git merge nhanh-huy --no-ff
git push origin main
```

---

## Ngày 6 — Tăng cường bằng AI (Gemini) + Đổi thương hiệu HATFilter

**Chủ đề:** tính năng điểm cộng (AI), hoàn thiện nhận diện thương hiệu, tài liệu và rà soát trước khi nộp.

### Trần Tú — 3 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/build.gradle.kts
git commit -m "chore(gradle): doc Gemini API key tu local.properties, expose qua BuildConfig"

git add app/src/main/java/com/example/photofilter/data/GeminiEnhanceRepository.java
git commit -m "feat(data): them GeminiEnhanceRepository goi Gemini API tang cuong anh"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java app/src/main/AndroidManifest.xml
git commit -m "feat(presenter): them onAiEnhanceRequested, goi AI tren luong nen, khai bao quyen INTERNET"
```

> Trước khi build, mỗi người tự tạo `local.properties` (không commit) với dòng `gemini.api.key=<key thật>` — key KHÔNG được để trong bất kỳ file nào add vào git.

### Phan Lê Huy — 3 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/values/strings.xml
git commit -m "feat(resource): doi ten app thanh HATFilter, them chuoi cho tinh nang AI"

git add app/src/main/res/drawable/ic_launcher_background.xml app/src/main/res/drawable/ic_launcher_foreground.xml
git commit -m "feat(ui): thiet ke lai icon ung dung theo logo thuong hieu moi"

git add app/src/main/res/layout/activity_main.xml app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): them nut AI tang cuong tren man hinh chinh"
```

### Tú Anh — 2 commit (viết tài liệu + rà soát cuối, đúng vai trò trưởng nhóm)

```bash
git checkout nhanh-tuanh
git config user.name "Tu Anh"
git config user.email "<email_tuanh>"

git add README.md
git commit -m "docs: them README gioi thieu project, kien truc MVP va huong dan chay"

# Chạy trước khi commit cuối:
#   ./gradlew testDebugUnitTest assembleDebug
git add -A
git commit -m "chore: ra soat cuoi, chay lai toan bo unit test truoc khi nop bai"
```

> `README.md` chưa có sẵn — Tú Anh viết ngắn gọn: tên app, mô tả, kiến trúc (domain/data/presenter/ui), cách build (`./gradlew assembleDebug`), cách chạy test (`./gradlew testDebugUnitTest`), danh sách tính năng.

### Cuối Ngày 6 — merge lần cuối + đẩy lên server (Tú Anh)

```bash
git checkout main
git merge nhanh-tuanh --no-ff
git merge nhanh-trantu --no-ff
git merge nhanh-huy --no-ff
git push origin main
```

---

## Sau khi xong (chỉ cần làm 1 lần, ở Ngày 2 hoặc bất kỳ lúc nào trước merge đầu tiên)

```bash
git remote add origin <URL server của lớp>
git push -u origin main
```

**Tổng kết theo người (6 ngày) — đã chia đều:**

| Người | Số commit | Vai trò xuyên suốt |
| --- | --- | --- |
| Tú Anh (nhóm trưởng) | 20 (7+3+6+2+2) | Filter engine (interface/abstract class), README, rà soát & test cuối, người merge nhánh mỗi ngày |
| Trần Tú | 19 (3+4+6+3+3) | Data/Presenter, SQLite (History + Favorite), tích hợp AI |
| Phan Lê Huy | 15 (+3 Ngày 1) = 18 (1+5+4+2+3) | Gradle scaffold, UI/theme, màn Lịch sử, thương hiệu HATFilter |

Giờ đã khá đều: 20 / 19 / 18. Những file bị sửa lại nhiều lần (EditorContract, EditorPresenter, FilterRepository) được gộp chung với phần code mới trong cùng 1 commit thay vì tách quá nhỏ, cho gần với thực tế commit chuyên nghiệp hơn.

**File này chỉ để tham khảo khi commit**, có thể xoá khỏi repo trước khi nộp bài nếu muốn cây thư mục gọn hơn.

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

## Ngày 3 — Kiến trúc MVP + giao diện chính + màn Home — ĐÃ COMMIT ✅

**Chủ đề:** nối Presenter/Contract (tách View khỏi business logic), dựng màn hình chỉnh sửa dạng "accordion" (bấm mục lớn Bộ lọc/Cắt để xổ ra panel con — Cắt gồm cả Xoay/Lật), thêm màn Home chào mừng, theo hệ màu **nền đen + nút vàng** làm chuẩn luôn từ đầu.

**Cả 3 người đã commit xong phần này** (Tú Anh 3 commit, Trần Tú 4 commit, Phan Lê Huy 7 commit ở dưới). Phần **"Bổ sung Ngày 3"** và **"Bổ sung Ngày 3 (lần 2)"** ngay sau đây là redesign phát sinh thêm cùng ngày — **CHƯA commit**, làm tiếp theo thứ tự bên dưới.

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

### Trần Tú — 5 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/java/com/example/photofilter/data/FilterRepository.java
git commit -m "feat(data): dang ky filter Sepia, Am, Lanh, Sang vao FilterRepository"

git add app/src/main/java/com/example/photofilter/data/CropRatio.java app/src/main/java/com/example/photofilter/data/CropUtils.java
git commit -m "feat(data): them CropRatio va CropUtils (rotate, lat ngang, center-crop theo ty le)"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/FilterThumbnail.java
git commit -m "feat(presenter): them EditorContract dinh nghia hop dong MVP (filter, rotate, flip, crop) va FilterThumbnail view-model"

git add app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java
git commit -m "feat(presenter): them EditorPresenter dieu phoi luong nen, xu ly rotate/flip/crop qua CropUtils, quan ly vong doi Bitmap"

git add app/src/test/java/com/example/photofilter/presenter/FakeView.java app/src/test/java/com/example/photofilter/presenter/ImmediateExecutorService.java app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "test(presenter): them FakeView, ImmediateExecutorService va unit test EditorPresenter (gom rotate/flip/crop)"
```

### Phan Lê Huy — 4 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/font/ app/src/main/res/values/colors.xml app/src/main/res/values/themes.xml app/src/main/res/drawable/bg_button_ghost.xml app/src/main/res/drawable/bg_button_primary.xml app/src/main/res/drawable/bg_button_solid.xml app/src/main/res/drawable/bg_card_surface.xml app/src/main/res/drawable/bg_filter_thumbnail.xml app/src/main/res/drawable/bg_sheet_top_rounded.xml app/src/main/res/drawable/bg_glass_backdrop.xml app/src/main/res/drawable/bg_home_hero_glow.xml app/src/main/res/drawable/bg_icon_chip_circle.xml app/src/main/res/drawable/bg_ring_outline.xml app/src/main/res/drawable/selector_filter_selected.xml app/src/main/res/drawable/dot_accent.xml app/src/main/res/color/filter_name_text_color.xml app/src/main/res/drawable/ic_ratio_original.xml app/src/main/res/drawable/ic_ratio_square.xml app/src/main/res/drawable/ic_ratio_four_three.xml app/src/main/res/drawable/ic_ratio_sixteen_nine.xml app/src/main/res/drawable/ic_rotate.xml app/src/main/res/drawable/ic_flip.xml
git commit -m "feat(ui): thiet lap font/mau nen den nhan vang, cac drawable nen/chip/vong tron va icon rieng cho tung tuy chon Cat"

git add app/src/main/res/layout/activity_main.xml app/src/main/res/layout/item_filter_thumbnail.xml app/src/main/res/menu/menu_main.xml app/src/main/res/values/strings.xml app/src/main/java/com/example/photofilter/ui/FilterAdapter.java app/src/main/java/com/example/photofilter/ui/GradientTextHelper.java
git commit -m "feat(ui): them layout man hinh chinh dang accordion (Bo loc/Cat), item filter thumbnail, menu Luu/Lich su, FilterAdapter va hieu ung gradient cho wordmark"

git add app/src/main/res/layout/activity_home.xml app/src/main/java/com/example/photofilter/ui/HomeActivity.java
git commit -m "feat(ui): them man Home chao mung (anh minh hoa + loi tat Chon anh/Chup anh/Lich su)"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java app/src/main/AndroidManifest.xml
git commit -m "feat(ui): them MainActivity - chon anh, ap filter, cat/xoay/lat, luu va chia se; HomeActivity la man launcher"
```

### Bổ sung Ngày 3 — redesign thanh công cụ dạng bottom sheet icon-only (theo yêu cầu mới, cùng ngày) — CHƯA COMMIT ⏳

**Lý do:** trong lúc code Ngày 3, nhóm quyết định đổi từ accordion 3 nút sang bottom sheet 5 icon (Bộ lọc/Cắt/Tuỳ chỉnh/AI/Xuất) kiểu Lightroom/Snapseed, đồng thời mở rộng: thêm 3 filter (Film/Mono/Retro), Adjust có thêm Hue/Exposure, AI có thêm Sharpen/Khử nhiễu/Tăng độ phân giải (xử lý ảnh thuần) và Xoá nền (ML Kit Selfie Segmentation thật), Crop có thêm Resize.

#### Tú Anh — 3 commit

```bash
git checkout nhanh-tuanh
git config user.name "Tu Anh"
git config user.email "<email_tuanh>"

git add app/src/main/java/com/example/photofilter/domain/filter/FilmFilter.java app/src/main/java/com/example/photofilter/domain/filter/MonoFilter.java app/src/main/java/com/example/photofilter/domain/filter/RetroFilter.java
git commit -m "feat(domain): trien khai FilmFilter, MonoFilter, RetroFilter"

git add app/src/test/java/com/example/photofilter/domain/filter/FilmFilterTest.java app/src/test/java/com/example/photofilter/domain/filter/MonoFilterTest.java app/src/test/java/com/example/photofilter/domain/filter/RetroFilterTest.java
git commit -m "test(domain): them unit test cho FilmFilter, MonoFilter, RetroFilter"

git add app/src/main/java/com/example/photofilter/domain/filter/ColorAdjustFilter.java app/src/test/java/com/example/photofilter/domain/filter/ColorAdjustFilterTest.java
git commit -m "feat(domain): mo rong ColorAdjustFilter them Hue (xoay mau sac) va Exposure (phoi sang)"
```

#### Trần Tú — 3 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/java/com/example/photofilter/data/CropUtils.java app/src/main/java/com/example/photofilter/data/FilterRepository.java
git commit -m "feat(data): them CropUtils.resize va dang ky filter Film/Mono/Retro"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java
git commit -m "feat(presenter): mo rong contract cho Resize, Hue/Exposure"

git add app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "test(presenter): them test cho Resize"
```

#### Phan Lê Huy — 4 commit

> **Chú thích:** từ vòng này, toàn bộ mảng **AI** (xử lý ảnh thuần Sharpen/Khử nhiễu/Tăng độ phân giải, tích hợp ML Kit Xoá nền, và sau này cả Gemini AI Enhance ở Ngày 6) được chuyển hết cho **Phan Lê Huy** phụ trách xuyên suốt — không còn tách cho Trần Tú nữa. `EditorContract.java`/`EditorPresenter.java` vì vậy được cả Trần Tú (Resize/Hue/Exposure, commit ở trên) và Phan Lê Huy (4 công cụ AI, commit thứ 2 dưới đây) cùng chỉnh nối tiếp nhau trong cùng buổi.

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/java/com/example/photofilter/data/AiToolsRepository.java gradle/libs.versions.toml app/build.gradle.kts app/src/main/java/com/example/photofilter/data/BackgroundRemovalRepository.java
git commit -m "feat(data): them AiToolsRepository (Sharpen, Remove Noise, Upscale) va tich hop ML Kit Selfie Segmentation cho Xoa nen"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "feat(presenter): mo rong contract cho 4 cong cu AI moi (applyAiTool dung chung) va unit test"

git add app/src/main/res/drawable/ic_tab_filters.xml app/src/main/res/drawable/ic_tab_adjust.xml app/src/main/res/drawable/ic_tab_ai.xml app/src/main/res/drawable/ic_tab_export.xml app/src/main/res/drawable/ic_resize.xml app/src/main/res/layout/item_tool_icon.xml app/src/main/res/drawable/ic_adjust_contrast.xml app/src/main/res/drawable/ic_adjust_saturation.xml app/src/main/res/drawable/ic_adjust_hue.xml app/src/main/res/drawable/ic_adjust_exposure.xml app/src/main/res/drawable/ic_ai_sharpen.xml app/src/main/res/drawable/ic_ai_denoise.xml app/src/main/res/drawable/ic_ai_upscale.xml app/src/main/res/drawable/ic_ai_bg_removal.xml app/src/main/res/drawable/ic_export_save.xml app/src/main/res/drawable/ic_export_share.xml app/src/main/res/values/strings.xml app/src/main/res/menu/menu_main.xml
git commit -m "feat(ui): them icon thanh dieu huong duoi, icon cho tung cong cu Adjust/AI/Export va chuoi lien quan"

git add app/src/main/res/layout/activity_main.xml app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): viet lai activity_main + MainActivity thanh CoordinatorLayout + BottomSheetBehavior, thanh dieu huong 5 icon, bo het nut chu"
```

> **Lưu ý khi build:** tính năng Xoá nền cần tải model ML Kit lần đầu chạy (cần Internet, có thể chậm trên máy yếu/emulator) — đã test Enhance/Sharpen/Khử nhiễu/Resize/Crop/Save/Share trực tiếp trên emulator, riêng Xoá nền chưa test được đầy đủ vì giới hạn thời gian tải model.

### Bổ sung Ngày 3 (lần 2) — màn Home cao cấp kiểu VSCO/Lightroom (theo yêu cầu mới, cùng ngày) — CHƯA COMMIT ⏳

**Lý do:** thiết kế lại toàn bộ màn Home: hero card 40% màn hình (gradient trừu tượng xanh dương/cyan thay ảnh người thật — chưa có nguồn ảnh, xem ghi chú cuối), tiêu đề "HatFilter", 4 thẻ Gallery/Camera/AI Enhance/History, dải "Recent Photos" lấy dữ liệu thật từ lịch sử đã lưu, cùng hoạt ảnh Ken Burns, fade-in, hạt sáng trôi, glow nhấp nháy, ripple và shared element transition sang màn Editor. **Toàn bộ việc này nằm trong package `ui` (không đụng domain/data/presenter)** nên dồn hết vào Phan Lê Huy — không chia giả cho công bằng.

#### Phan Lê Huy — 4 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/values/colors.xml app/src/main/res/values/themes.xml app/src/main/res/drawable/bg_hero_gradient_base.xml app/src/main/res/drawable/bg_glow_blue.xml app/src/main/res/drawable/bg_glow_cyan.xml app/src/main/res/drawable/bg_hero_scrim.xml app/src/main/res/drawable/bg_card_ripple.xml app/src/main/res/drawable/bg_icon_glow_circle.xml
git commit -m "feat(ui): them bang mau/ShapeAppearance rieng cho man Home va drawable gradient/glow cho hero + card"

git add app/src/main/java/com/example/photofilter/ui/ParticleView.java app/src/main/java/com/example/photofilter/ui/RecentPhotoAdapter.java app/src/main/res/layout/item_home_card.xml app/src/main/res/layout/item_recent_photo.xml app/src/main/res/values/strings.xml
git commit -m "feat(ui): them ParticleView (hat sang troi), RecentPhotoAdapter, layout item dung chung va chuoi cho man Home cao cap"

git add app/src/main/res/layout/activity_home.xml app/src/main/java/com/example/photofilter/ui/HomeActivity.java
git commit -m "feat(ui): viet lai activity_home + HomeActivity - hero 40% man hinh bo goc, Ken Burns zoom, fade-in, glow, 4 the 2x2, Recent Photos tu SQLite"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java app/src/main/res/layout/activity_main.xml
git commit -m "feat(ui): them shared element transition tu the Home sang anh Editor, auto-mo tab AI khi bam the AI Enhance"
```

> **Ghi chú quan trọng:** yêu cầu gốc cần "ảnh hero chân dung người thật, chỉnh màu điện ảnh" — mình không có công cụ tạo ảnh/kho ảnh stock nên đã thay bằng gradient + hoạ tiết trừu tượng (không phải logo, không phải ảnh giả). Muốn dùng ảnh thật: thay nội dung `heroBackgroundView` trong `activity_home.xml` (dòng có `android:id="@+id/heroBackgroundView"`) từ `<View>` thành `<ImageView android:src="@drawable/ten_anh_ban_them">`, đặt file ảnh vào `res/drawable/`.

### Cuối Ngày 3 — merge (Tú Anh)

> Chạy merge này **sau khi** cả phần chính lẫn 2 phần "Bổ sung Ngày 3" ở trên đã commit xong hết (vẫn cùng 3 nhánh `nhanh-tuanh`/`nhanh-trantu`/`nhanh-huy` nên chỉ cần merge 1 lần).

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

## Ngày 5 — Chụp ảnh (Camera) + Tuỳ chỉnh màu thời gian thực

**Chủ đề:** hoàn thiện bộ công cụ chỉnh sửa nâng cao. (Xoay/Cắt/Lật đã có từ Ngày 3 trong panel "Cắt" — Ngày 5 chỉ thêm Camera và panel "Tuỳ chỉnh" màu.)

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
git commit -m "feat(presenter): them onAdjustValuesChanged va tich hop ColorAdjustFilter thoi gian thuc"

git add app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "test(presenter): them test cho onAdjustValuesChanged"
```

### Phan Lê Huy — 2 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/layout/activity_main.xml
git commit -m "feat(ui): them nut Tuy chinh (accordion thu 3) va panel SeekBar dieu chinh mau"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): noi Camera (dialog chon nguon anh) va SeekBar Tuy chinh vao MainActivity"
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

**Chủ đề:** tính năng điểm cộng (AI), hoàn thiện nhận diện thương hiệu, tài liệu và rà soát trước khi nộp. Trần Tú không có phần riêng ở Ngày 6 — mảng AI (kể cả Gemini) đã chuyển hết cho Phan Lê Huy phụ trách xuyên suốt, xem ghi chú ở "Bổ sung Ngày 3".

### Phan Lê Huy — 4 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/build.gradle.kts app/src/main/java/com/example/photofilter/data/GeminiEnhanceRepository.java
git commit -m "feat(data): doc Gemini API key tu local.properties (BuildConfig) va them GeminiEnhanceRepository goi Gemini API"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java app/src/main/AndroidManifest.xml
git commit -m "feat(presenter): them onAiEnhanceRequested, goi AI tren luong nen, khai bao quyen INTERNET"

git add app/src/main/res/values/strings.xml app/src/main/res/drawable/ic_launcher_background.xml app/src/main/res/drawable/ic_launcher_foreground.xml
git commit -m "feat(resource): doi ten app thanh HATFilter, thiet ke lai icon ung dung theo logo thuong hieu moi"

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

## Bổ sung sau Ngày 6 — Cắt tự do (kéo-thả) + cuộn cho bottom sheet nhiều chức năng

**Lý do:** sau khi nộp bản 6 ngày, nhóm được yêu cầu thêm công cụ cắt ảnh tuỳ chỉnh (kéo 4 góc chọn vùng tự do, có lưới rule-of-thirds) trong panel "Cắt", và bọc toàn bộ nội dung bottom sheet trong `NestedScrollView` để tránh tràn màn hình khi một panel có nhiều tuỳ chọn. Đồng thời tăng timeout mạng trong `gradle.properties` để sync ổn định hơn trên máy dùng VPN/proxy công ty.

#### Trần Tú — 3 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/java/com/example/photofilter/data/CropUtils.java
git commit -m "feat(data): them CropUtils.customCrop - cat anh theo vung tuy chinh (RectF chuan hoa)"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java
git commit -m "feat(presenter): them onCustomCropRequested, tai su dung applyGeometryChange cho cat tu do"

git add app/src/test/java/com/example/photofilter/data/CropUtilsTest.java
git commit -m "test(data): them unit test cho CropUtils.customCrop"
```

#### Phan Lê Huy — 3 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/java/com/example/photofilter/ui/CropOverlayView.java app/src/main/res/drawable/ic_crop_custom.xml app/src/main/res/values/strings.xml
git commit -m "feat(ui): them CropOverlayView - keo tha 4 goc chon vung cat tu do, luoi rule-of-thirds, chuoi Tu do/Xac nhan/Huy"

git add app/src/main/res/layout/activity_main.xml app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): them nut Tu do vao panel Cat, noi CropOverlayView vao MainActivity, boc NestedScrollView de cuon khi nhieu tuy chon"

git add gradle.properties
git commit -m "chore(gradle): tang network timeout cho mang cong ty/VPN khi sync lan dau"
```

### Merge (Tú Anh)

```bash
git checkout main
git merge nhanh-trantu --no-ff
git merge nhanh-huy --no-ff
git push origin main
```

---

## Bổ sung sau Ngày 6 (lần 2) — hoàn thiện toolbar Lưu/Chia sẻ, sửa lỗi hiển thị

**Lý do:** sau khi dùng thử bản build thật, nhóm tiếp tục sửa nhiều lỗi/UX nhỏ phát sinh: sửa crash mở Camera trên Android cũ, sửa gradient góc không hợp lệ làm crash màn Home, gộp nút Lưu/Chia sẻ thành 1 icon trên toolbar (bỏ 2 nút Chọn ảnh/Lịch sử cũ), thêm màn "Đã lưu ảnh" hiển thị kết quả sau khi lưu, cho ảnh chỉnh sửa tràn viền toàn màn hình, và sửa khoảng cách/bo góc ở panel Bộ lọc, màn Home, dải Recent Photos. **Toàn bộ nằm trong package `ui` + resource, không đụng domain/data** nên dồn hết vào Phan Lê Huy, giống 2 đợt bổ sung UI trước.

#### Phan Lê Huy — 4 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/drawable/bg_hero_gradient_base.xml app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "fix: sua android:angle khong hop le trong bg_hero_gradient_base.xml (crash man Home) va xin quyen WRITE_EXTERNAL_STORAGE truoc khi mo Camera tren Android < 10"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java app/src/main/res/layout/activity_main.xml app/src/main/res/menu/menu_main.xml app/src/main/java/com/example/photofilter/ui/SaveResultActivity.java app/src/main/res/layout/activity_save_result.xml app/src/main/AndroidManifest.xml app/src/main/res/values/strings.xml
git commit -m "feat(ui): gop nut Luu/Chia se thanh 1 icon o toolbar, bo 2 nut Chon anh/Lich su, them man Da luu anh"

git add app/src/main/res/layout/activity_main.xml app/src/main/res/layout/item_filter_thumbnail.xml app/src/main/res/values/themes.xml
git commit -m "fix(ui): anh chinh sua tran vien toan man hinh, sua khoang cach/chieu cao panel Bo loc va bo goc anh xem truoc khop vien chon"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java app/src/main/res/layout/activity_home.xml app/src/main/res/layout/item_recent_photo.xml
git commit -m "feat(ui): bo popup hoi Luu/Chia se (da co man Da luu anh), them khoang cach cho 4 the Home va Recent Photos"
```

### Merge (Tú Anh)

```bash
git checkout main
git merge nhanh-huy --no-ff
git push origin main
```

---

## Bổ sung sau Ngày 6 (lần 3) — Đăng nhập/Đăng ký/Đăng xuất (SQLite cục bộ) — CHƯA COMMIT ⏳

**Lý do:** thêm yêu cầu xác thực người dùng. Ban đầu định dùng Firebase Authentication nhưng đổi sang **SQLite thuần cục bộ** (giống History/Favorite) để khỏi cần server/API key/setup Firebase Console — mật khẩu băm SHA-256, phiên đăng nhập lưu qua `SharedPreferences`. `LoginActivity` thành màn khởi động mới (thay `HomeActivity`), `RegisterActivity` cho đăng ký, nút đăng xuất ở màn Home.

#### Trần Tú — 2 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/java/com/example/photofilter/data/UserDbHelper.java app/src/main/java/com/example/photofilter/data/UserRepository.java app/src/main/java/com/example/photofilter/data/AuthRepository.java
git commit -m "feat(data): them UserDbHelper/UserRepository (SQLite thuan) va AuthRepository dieu phoi dang nhap/dang ky/dang xuat"

git add app/src/test/java/com/example/photofilter/data/UserRepositoryTest.java app/src/test/java/com/example/photofilter/data/AuthRepositoryTest.java
git commit -m "test(data): them unit test cho UserRepository va AuthRepository"

git add app/src/main/java/com/example/photofilter/data/BackgroundRemovalRepository.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java
git commit -m "fix: scale mask ML Kit ve dung kich thuoc anh goc truoc khi ap dung (Xoa nen chi xu ly dung 1 goc anh), hien loi that tu AI thay vi thong bao chung chung"
```

#### Phan Lê Huy — 3 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/java/com/example/photofilter/ui/LoginActivity.java app/src/main/res/layout/activity_login.xml app/src/main/java/com/example/photofilter/ui/RegisterActivity.java app/src/main/res/layout/activity_register.xml app/src/main/res/drawable/bg_input_field.xml app/src/main/AndroidManifest.xml
git commit -m "feat(ui): them man Dang nhap/Dang ky, doi LoginActivity thanh man khoi dong (kiem tra phien dang nhap truoc khi vao Home)"

git add app/src/main/res/values/strings.xml
git commit -m "feat(resource): them chuoi cho Dang nhap/Dang ky/Dang xuat"

git add app/src/main/res/drawable/ic_logout.xml app/src/main/res/layout/activity_home.xml app/src/main/java/com/example/photofilter/ui/HomeActivity.java
git commit -m "feat(ui): them nut dang xuat o man Home (dialog xac nhan, quay ve LoginActivity)"
```

### Merge (Tú Anh)

```bash
git checkout main
git merge nhanh-trantu --no-ff
git merge nhanh-huy --no-ff
git push origin main
```

---

## Ngày 7 — Undo/Redo + Apply/Cancel toàn cục, màn Intro chào mừng, tab Sticker, dọn UI

**Chủ đề:** sửa bug "Cắt → Gốc" không đưa ảnh về đúng gốc; thêm Undo/Redo + Apply/Cancel nhất quán cho cả 4 tab (Bộ lọc/Cắt/Tuỳ chỉnh/AI); gỡ tính năng AI Enhance (Gemini) vì không dùng được (kèm gỡ luôn đoạn code bypass SSL không an toàn còn sót lại); thêm màn Intro chào mừng (giọng nói "Welcome to H.A.T" + Ken Burns zoom) hiện đúng 1 lần/máy sau lần đăng nhập/đăng ký đầu tiên, có nút xem lại trong menu tài khoản để tiện demo; thêm tab Sticker (dán/kéo/pinch-zoom/xoay sticker lên ảnh, 15 sticker OpenMoji CC BY-SA 4.0); đổi nhãn tab "AI" thành "Công cụ" cho đúng bản chất (3/4 công cụ không dùng ML thật); gỡ 5 nút Đổi cỡ (75–200%) không cần dùng; và một loạt sửa lỗi UI (icon Undo/Redo bị cắt mép, 2 hàng Huỷ/Xác nhận chồng nhau ở Cắt tự do, khoảng cách 4 thẻ Home).

### Trần Tú — 6 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/java/com/example/photofilter/presenter/EditHistory.java app/src/test/java/com/example/photofilter/presenter/EditHistoryTest.java
git commit -m "feat(presenter): them EditHistory - undo/redo stack voi pristine-original ghim rieng"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java app/src/test/java/com/example/photofilter/presenter/FakeView.java app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "feat(presenter): chuyen EditorPresenter sang mo hinh draft + EditHistory, sua bug Crop Original"

git add app/src/main/java/com/example/photofilter/data/AuthRepository.java
git commit -m "feat(data): them co intro_shown vao AuthRepository (theo thiet bi, khong theo tai khoan)"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "feat(presenter): them onStickerApplyRequested - in sticker len anh va commit vao lich su Undo/Redo"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java app/src/main/java/com/example/photofilter/data/CropUtils.java
git commit -m "refactor(presenter): go onResizeRequested va CropUtils.resize() - khong con noi nao goi sau khi bo nut Doi co"

git add app/src/main/java/com/example/photofilter/data/AuthRepository.java
git commit -m "feat(data): them resetIntroSeen() cho phep xem lai man chao mung phuc vu demo"
```

### Phan Lê Huy — 17 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/build.gradle.kts app/src/main/AndroidManifest.xml
git commit -m "fix: go tinh nang AI Enhance (Gemini) do khong dung duoc - xoa GeminiEnhanceRepository, bo quyen INTERNET khong can, bo doan code bypass SSL khong an toan"

git add app/src/main/res/values/strings.xml app/src/main/res/drawable/ic_undo.xml app/src/main/res/drawable/ic_redo.xml
git commit -m "feat(resource): them chuoi va icon cho Hoan tac/Lam lai"

git add app/src/main/res/layout/activity_main.xml
git commit -m "feat(ui): them nut Hoan tac/Lam lai tren top bar va hang Ap dung/Huy dung chung cho ca 4 tab"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): noi Undo/Redo va hang Ap dung/Huy dung chung vao MainActivity"

git add app/src/main/res/layout/activity_main.xml
git commit -m "fix(ui): sua thu tu 3 nut Hoan tac/Lam lai/Luu tren toolbar bi lech do MaterialToolbar xep view end-gravity theo thu tu khai bao"

git add app/src/main/res/layout/activity_home.xml
git commit -m "refactor(ui): doi 2 LinearLayout hang cua 4 the Home sang GridLayout 2 cot - 4 the doc lap, tang khoang cach 16dp->20dp"

git add app/src/main/res/drawable-nodpi/sticker_*.png README.md
git commit -m "feat(resource): them 15 sticker OpenMoji (CC BY-SA 4.0), ghi ro attribution trong README"

git add app/src/main/res/values/strings.xml
git commit -m "fix(ui): doi nhan tab AI va the AI Tools o Home thanh Cong cu cho dung ban chat (3/4 cong cu khong dung ML)"

git add app/src/main/java/com/example/photofilter/ui/StickerOverlayView.java app/src/main/res/drawable/ic_tab_sticker.xml
git commit -m "feat(ui): them StickerOverlayView - keo/pinch-zoom/xoay sticker bang cu chi 2 ngon"

git add app/src/main/res/layout/activity_main.xml app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): noi tab Sticker (icon thu 5, panel 15 sticker, StickerOverlayView) vao MainActivity"

git add app/src/main/res/layout/activity_intro.xml app/src/main/java/com/example/photofilter/ui/IntroActivity.java app/src/main/res/raw/welcome_hat.mp3 app/src/main/res/drawable-nodpi/img_intro_bubbles.jpg app/src/main/AndroidManifest.xml
git commit -m "feat(ui): them IntroActivity - man chao mung phat giong noi Welcome to H.A.T tren nen anh bong bong (Ken Burns zoom)"

git add app/src/main/java/com/example/photofilter/ui/LoginActivity.java app/src/main/java/com/example/photofilter/ui/RegisterActivity.java
git commit -m "feat(ui): dieu huong qua IntroActivity truoc Home neu chua xem intro, tu Login va Register"

git add app/src/main/res/drawable/ic_undo.xml app/src/main/res/drawable/ic_redo.xml
git commit -m "fix(ui): ve lai icon Undo/Redo bang polygon an toan, sua loi icon bi cat mep do arc vuot viewport"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java app/src/main/res/layout/activity_main.xml
git commit -m "fix(ui): sua loi hien 2 hang Huy/Xac nhan chong nhau khi bat Cat tu do; go 5 nut Doi co (75-200%) khong can dung"

git add app/src/main/res/values/strings.xml app/src/main/res/drawable/ic_resize.xml
git commit -m "chore(resource): xoa string va icon cua tinh nang Doi co da go"

git add app/src/main/java/com/example/photofilter/ui/IntroActivity.java
git commit -m "fix(ui): bo co NEW_TASK/CLEAR_TASK thua, delay goToHome() qua Handler khi bam Back de tranh xung dot voi back-navigation cua he thong"

git add app/src/main/java/com/example/photofilter/ui/HomeActivity.java app/src/main/res/values/strings.xml
git commit -m "feat(ui): them nut Xem lai man chao mung trong menu tai khoan, phuc vu demo nhieu lan"
```

### Tú Anh — 3 commit (đặc tả, rà soát cuối, merge)

```bash
git checkout nhanh-tuanh
git config user.name "Tu Anh"
git config user.email "<email_tuanh>"

git add docs/superpowers/specs/2026-07-28-editor-history-apply-cancel-design.md docs/superpowers/plans/2026-07-28-editor-history-apply-cancel.md
git commit -m "docs: them spec/plan cho Undo-Redo + Apply/Cancel"

git add docs/superpowers/specs/2026-07-29-home-intro-design.md docs/superpowers/plans/2026-07-29-home-intro.md docs/superpowers/specs/2026-07-29-sticker-overlay-design.md
git commit -m "docs: them spec/plan cho man Intro va tab Sticker"

# Chạy trước khi commit cuối:
#   ./gradlew testDebugUnitTest assembleDebug
git add -A
git commit -m "chore: ra soat cuoi, chay lai toan bo unit test truoc khi merge"
```

### Cuối Ngày 7 — merge (Tú Anh)

```bash
git checkout main
git merge nhanh-trantu --no-ff
git merge nhanh-huy --no-ff
git merge nhanh-tuanh --no-ff
git push origin main
```

> **Ghi chú:** đợt này làm trực tiếp trên `main` (không tách nhánh riêng theo thời điểm code), commit ở trên viết theo đúng quy ước 3 nhánh để bạn áp dụng y hệt khi làm lại trên máy công ty (code ở đó đang cũ, chưa có toàn bộ Ngày 7 này) — chỉ cần theo đúng thứ tự file/commit message bên trên là ra kết quả tương đương.

---

## Sau khi xong (chỉ cần làm 1 lần, ở Ngày 2 hoặc bất kỳ lúc nào trước merge đầu tiên)

```bash
git remote add origin <URL server của lớp>
git push -u origin main
```

**Tổng kết theo người (6 ngày + 3 đợt bổ sung sau Ngày 6):**

| Người | Số commit | Vai trò xuyên suốt |
| --- | --- | --- |
| Tú Anh (nhóm trưởng) | 23 (7+3+3+6+2+2) | Filter engine (interface/abstract class), README, rà soát & test cuối, người merge nhánh mỗi ngày |
| Trần Tú | 26 (3+5+3+6+3+3+3) | Data/Presenter, CropUtils (kể cả cắt tự do), SQLite (History + Favorite + tài khoản), `AuthRepository`, sửa lỗi Xoá nền/AI Enhance |
| Phan Lê Huy | 33 (+3 Ngày 1) = 36 (1+4+4+4+4+2+4+3+4+3) | Gradle scaffold, UI/theme, bottom sheet 5 icon, màn Home cao cấp (hero/particle/shared transition), Lịch sử, thương hiệu HATFilter, CropOverlayView (cắt tự do), **toàn bộ mảng AI** (Sharpen/Khử nhiễu/Tăng độ phân giải, ML Kit Xoá nền, Gemini AI Enhance), sửa lỗi + hoàn thiện UI sau khi test bản build thật, màn Đăng nhập/Đăng ký/Đăng xuất |

**Cập nhật:** mảng AI trước đây chia cho Trần Tú, nay chuyển hết sang Phan Lê Huy phụ trách xuyên suốt (bao gồm cả Gemini ở Ngày 6) theo yêu cầu điều chỉnh phân công; đồng thời thêm tính năng Đăng nhập/Đăng ký/Đăng xuất — ban đầu định dùng Firebase Authentication nhưng đổi sang SQLite cục bộ (`AuthRepository`/`UserRepository` cho Trần Tú giữ vai trò Data, `LoginActivity`/`RegisterActivity`/nút đăng xuất cho Phan Lê Huy giữ vai trò UI — đúng quy ước chia theo tầng đã dùng xuyên suốt). Số commit của Phan Lê Huy vẫn cao nhất (36 so với 23/26) vì gánh cả UI lẫn AI xuyên suốt — đã gộp bớt các commit nhỏ liên quan nhau trong từng đợt (gộp icon/drawable cùng loại, gộp layout+activity đi cùng nhau) để không vênh quá xa so với 2 bạn còn lại, dù thực tế khối lượng file UI vẫn nhiều hơn. Ngày 1–2 đã push thật nên giữ nguyên không đổi.

**File này chỉ để tham khảo khi commit**, có thể xoá khỏi repo trước khi nộp bài nếu muốn cây thư mục gọn hơn.

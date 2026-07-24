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

### Trần Tú — 4 commit

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

### Phan Lê Huy — 7 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/font/ app/src/main/res/values/colors.xml app/src/main/res/values/themes.xml
git commit -m "feat(ui): thiet lap font Sora/Inter va bang mau nen den, nhan vang"

git add app/src/main/res/drawable/bg_button_ghost.xml app/src/main/res/drawable/bg_button_primary.xml app/src/main/res/drawable/bg_button_solid.xml app/src/main/res/drawable/bg_card_surface.xml app/src/main/res/drawable/bg_filter_thumbnail.xml app/src/main/res/drawable/bg_sheet_top_rounded.xml app/src/main/res/drawable/bg_glass_backdrop.xml app/src/main/res/drawable/bg_home_hero_glow.xml app/src/main/res/drawable/bg_icon_chip_circle.xml app/src/main/res/drawable/bg_ring_outline.xml app/src/main/res/drawable/selector_filter_selected.xml app/src/main/res/drawable/dot_accent.xml app/src/main/res/color/filter_name_text_color.xml
git commit -m "feat(ui): them cac drawable nen, gradient nut vang, chip icon va vong tron trang tri"

git add app/src/main/res/drawable/ic_ratio_original.xml app/src/main/res/drawable/ic_ratio_square.xml app/src/main/res/drawable/ic_ratio_four_three.xml app/src/main/res/drawable/ic_ratio_sixteen_nine.xml app/src/main/res/drawable/ic_rotate.xml app/src/main/res/drawable/ic_flip.xml
git commit -m "feat(ui): them icon rieng cho tung tuy chon Cat (thay the nut chu bang icon tron nhieu mau)"

git add app/src/main/res/layout/activity_main.xml app/src/main/res/layout/item_filter_thumbnail.xml app/src/main/res/menu/menu_main.xml app/src/main/res/values/strings.xml
git commit -m "feat(ui): them layout man hinh chinh dang accordion (Bo loc/Cat), item filter thumbnail va menu Luu/Lich su"

git add app/src/main/java/com/example/photofilter/ui/FilterAdapter.java app/src/main/java/com/example/photofilter/ui/GradientTextHelper.java
git commit -m "feat(ui): them FilterAdapter va hieu ung gradient cho wordmark"

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

#### Trần Tú — 5 commit

```bash
git checkout nhanh-trantu
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/java/com/example/photofilter/data/AiToolsRepository.java
git commit -m "feat(data): them AiToolsRepository - Sharpen, Remove Noise (median filter), Upscale"

git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/java/com/example/photofilter/data/BackgroundRemovalRepository.java
git commit -m "feat(data): tich hop ML Kit Selfie Segmentation cho tinh nang Xoa nen"

git add app/src/main/java/com/example/photofilter/data/CropUtils.java app/src/main/java/com/example/photofilter/data/FilterRepository.java
git commit -m "feat(data): them CropUtils.resize va dang ky filter Film/Mono/Retro"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java
git commit -m "feat(presenter): mo rong contract cho Resize, Hue/Exposure va 4 cong cu AI moi (applyAiTool dung chung)"

git add app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "test(presenter): them test cho Resize, Sharpen, Remove Noise, Upscale"
```

#### Phan Lê Huy — 5 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/drawable/ic_tab_filters.xml app/src/main/res/drawable/ic_tab_adjust.xml app/src/main/res/drawable/ic_tab_ai.xml app/src/main/res/drawable/ic_tab_export.xml app/src/main/res/drawable/ic_resize.xml app/src/main/res/layout/item_tool_icon.xml
git commit -m "feat(ui): them icon thanh dieu huong duoi va layout item_tool_icon dung chung"

git add app/src/main/res/drawable/ic_adjust_contrast.xml app/src/main/res/drawable/ic_adjust_saturation.xml app/src/main/res/drawable/ic_adjust_hue.xml app/src/main/res/drawable/ic_adjust_exposure.xml app/src/main/res/drawable/ic_ai_sharpen.xml app/src/main/res/drawable/ic_ai_denoise.xml app/src/main/res/drawable/ic_ai_upscale.xml app/src/main/res/drawable/ic_ai_bg_removal.xml app/src/main/res/drawable/ic_export_save.xml app/src/main/res/drawable/ic_export_share.xml
git commit -m "feat(ui): them icon cho tung cong cu Adjust/AI/Export"

git add app/src/main/res/values/strings.xml app/src/main/res/menu/menu_main.xml
git commit -m "feat(resource): them chuoi cho filter/cong cu moi, doi menu top bar chi con Chon anh + Lich su"

git add app/src/main/res/layout/activity_main.xml
git commit -m "feat(ui): viet lai activity_main thanh CoordinatorLayout + BottomSheetBehavior, thanh dieu huong 5 icon, bo het nut chu"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): viet lai MainActivity dieu khien bottom sheet (mot tab mo tai 1 thoi diem, chuyen tab muot)"
```

> **Lưu ý khi build:** tính năng Xoá nền cần tải model ML Kit lần đầu chạy (cần Internet, có thể chậm trên máy yếu/emulator) — đã test Enhance/Sharpen/Khử nhiễu/Resize/Crop/Save/Share trực tiếp trên emulator, riêng Xoá nền chưa test được đầy đủ vì giới hạn thời gian tải model.

### Bổ sung Ngày 3 (lần 2) — màn Home cao cấp kiểu VSCO/Lightroom (theo yêu cầu mới, cùng ngày) — CHƯA COMMIT ⏳

**Lý do:** thiết kế lại toàn bộ màn Home: hero card 40% màn hình (gradient trừu tượng xanh dương/cyan thay ảnh người thật — chưa có nguồn ảnh, xem ghi chú cuối), tiêu đề "HatFilter", 4 thẻ Gallery/Camera/AI Enhance/History, dải "Recent Photos" lấy dữ liệu thật từ lịch sử đã lưu, cùng hoạt ảnh Ken Burns, fade-in, hạt sáng trôi, glow nhấp nháy, ripple và shared element transition sang màn Editor. **Toàn bộ việc này nằm trong package `ui` (không đụng domain/data/presenter)** nên dồn hết vào Phan Lê Huy — không chia giả cho công bằng.

#### Phan Lê Huy — 7 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/values/colors.xml app/src/main/res/values/themes.xml
git commit -m "feat(ui): them bang mau va ShapeAppearance rieng cho man Home, bat windowContentTransitions"

git add app/src/main/res/drawable/bg_hero_gradient_base.xml app/src/main/res/drawable/bg_glow_blue.xml app/src/main/res/drawable/bg_glow_cyan.xml app/src/main/res/drawable/bg_hero_scrim.xml app/src/main/res/drawable/bg_card_ripple.xml app/src/main/res/drawable/bg_icon_glow_circle.xml
git commit -m "feat(ui): them drawable gradient/glow cho hero va ripple/glow cho card Home"

git add app/src/main/java/com/example/photofilter/ui/ParticleView.java app/src/main/java/com/example/photofilter/ui/RecentPhotoAdapter.java app/src/main/res/layout/item_home_card.xml app/src/main/res/layout/item_recent_photo.xml
git commit -m "feat(ui): them ParticleView (hat sang troi), RecentPhotoAdapter va layout item dung chung"

git add app/src/main/res/values/strings.xml
git commit -m "feat(resource): them chuoi cho man Home cao cap (tieu de, 4 the, Recent Photos)"

git add app/src/main/res/layout/activity_home.xml
git commit -m "feat(ui): viet lai activity_home - hero 40 phan tram man hinh bo goc, 4 the 2x2, Recent Photos cuon ngang"

git add app/src/main/java/com/example/photofilter/ui/HomeActivity.java
git commit -m "feat(ui): viet lai HomeActivity - Ken Burns zoom, fade-in tuan tu, glow nhap nhay, tai Recent Photos tu SQLite"

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

#### Phan Lê Huy — 5 commit

```bash
git checkout nhanh-huy
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/java/com/example/photofilter/ui/CropOverlayView.java app/src/main/res/drawable/ic_crop_custom.xml
git commit -m "feat(ui): them CropOverlayView - keo tha 4 goc chon vung cat tu do, luoi rule-of-thirds"

git add app/src/main/res/values/strings.xml
git commit -m "feat(resource): them chuoi cho nut Tu do/Xac nhan/Huy trong panel Cat"

git add app/src/main/res/layout/activity_main.xml
git commit -m "feat(ui): them nut Tu do vao panel Cat, boc NestedScrollView cho toan bo bottom sheet de cuon khi nhieu tuy chon"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): noi CropOverlayView vao MainActivity - bat/tat che do cat tu do, xac nhan/huy"

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

## Sau khi xong (chỉ cần làm 1 lần, ở Ngày 2 hoặc bất kỳ lúc nào trước merge đầu tiên)

```bash
git remote add origin <URL server của lớp>
git push -u origin main
```

**Tổng kết theo người (6 ngày + bổ sung cắt tự do):**

| Người | Số commit | Vai trò xuyên suốt |
| --- | --- | --- |
| Tú Anh (nhóm trưởng) | 23 (7+3+3+6+2+2) | Filter engine (interface/abstract class), README, rà soát & test cuối, người merge nhánh mỗi ngày |
| Trần Tú | 28 (3+5+5+6+3+3+3) | Data/Presenter, CropUtils (kể cả cắt tự do), SQLite (History + Favorite), tích hợp AI (Gemini + ML Kit) |
| Phan Lê Huy | 34 (+3 Ngày 1) = 37 (1+7+5+7+4+2+3+5) | Gradle scaffold, UI/theme, bottom sheet 5 icon, màn Home cao cấp (hero/particle/shared transition), Lịch sử, thương hiệu HATFilter, CropOverlayView (cắt tự do) |

Ngày 3 phình to nhất vì 2 đợt redesign liên tiếp trong cùng ngày (bottom sheet + màn Home) đều là việc UI thuần, không đụng domain/data/presenter nên không có gì hợp lý để chia cho Tú Anh/Trần Tú; đợt bổ sung cắt tự do sau Ngày 6 cũng nghiêng về Phan Lê Huy vì phần lớn là `CropOverlayView` (UI) trong khi Trần Tú giữ phần `CropUtils`/presenter/test. Nếu muốn cân lại cho đều: có thể chuyển 2-3 commit thuần tài nguyên (ví dụ commit `colors.xml`/`themes.xml` hoặc commit `strings.xml`) sang cho Trần Tú hoặc Tú Anh đứng tên, vì đây chỉ là khai báo dữ liệu, không đòi hỏi hiểu sâu về UI. Ngày 1–2 đã push thật nên giữ nguyên không đổi.

**File này chỉ để tham khảo khi commit**, có thể xoá khỏi repo trước khi nộp bài nếu muốn cây thư mục gọn hơn.

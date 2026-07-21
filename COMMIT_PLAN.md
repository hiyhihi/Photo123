# Kế hoạch commit — PhotoFilter

Thứ tự bắt buộc theo chiều phụ thuộc biên dịch: **Scaffold → Domain (Tú Anh) → Data/Presenter (Trần Tú) → UI (Phan Lê Huy)**.
Vài commit ở giữa sẽ chưa build được ngay (ví dụ sau khối Scaffold chưa có Activity nào) — bình thường, không sao, chỉ cần commit **cuối cùng** build được là đủ.

Chạy toàn bộ các lệnh dưới đây tại thư mục gốc project (`PhotoFilter/`), theo đúng thứ tự. Trước mỗi khối, đổi danh tính git sang đúng người phụ trách.

---

## Khối 0 — Scaffold (Phan Lê Huy)

```bash
git init
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add .gitignore app/.gitignore build.gradle.kts settings.gradle.kts gradle.properties gradlew gradlew.bat gradle/
git commit -m "chore(gradle): khoi tao project Android Studio (Java), cau hinh AGP va dependencies"

git add app/build.gradle.kts app/proguard-rules.pro
git commit -m "chore(gradle): cau hinh module app - Glide, RecyclerView, ExifInterface, Robolectric"

git add app/src/main/res/mipmap-anydpi-v26 app/src/main/res/mipmap-hdpi app/src/main/res/mipmap-mdpi app/src/main/res/mipmap-xhdpi app/src/main/res/mipmap-xxhdpi app/src/main/res/mipmap-xxxhdpi app/src/main/res/drawable/ic_launcher_background.xml app/src/main/res/drawable/ic_launcher_foreground.xml app/src/main/res/values/themes.xml app/src/main/res/values-night/themes.xml app/src/main/res/xml/backup_rules.xml app/src/main/res/xml/data_extraction_rules.xml app/src/androidTest app/src/test/java/com/example/photofilter/ExampleUnitTest.java
git commit -m "chore(resource): them icon ung dung, theme mac dinh va test mau"
```

---

## Khối 1 — Domain / Filter engine (Tú Anh)

```bash
git config user.name "Tu Anh"
git config user.email "<email_tuanh>"

git add app/src/main/java/com/example/photofilter/domain/filter/Filter.java app/src/main/java/com/example/photofilter/domain/filter/BaseFilter.java
git commit -m "feat(domain): them interface Filter va abstract class BaseFilter (template method)"

git add app/src/main/java/com/example/photofilter/domain/filter/OriginalFilter.java app/src/main/java/com/example/photofilter/domain/filter/GrayscaleFilter.java app/src/main/java/com/example/photofilter/domain/filter/NegativeFilter.java
git commit -m "feat(domain): trien khai 3 filter bat buoc - Original, Grayscale, Negative"

git add app/src/main/java/com/example/photofilter/domain/filter/SepiaFilter.java app/src/main/java/com/example/photofilter/domain/filter/ColorToneFilter.java app/src/main/java/com/example/photofilter/domain/filter/BrightnessContrastFilter.java
git commit -m "feat(domain): them filter mo rong - Sepia, ColorTone (Am/Lanh), BrightnessContrast"

git add app/src/test/java/com/example/photofilter/domain/filter/GrayscaleFilterTest.java app/src/test/java/com/example/photofilter/domain/filter/NegativeFilterTest.java app/src/test/resources/robolectric.properties
git commit -m "test(domain): them unit test Robolectric cho Grayscale va Negative filter"
```

---

## Khối 2 — Data + Presenter (Trần Tú)

```bash
git config user.name "Tran Tu"
git config user.email "<email_trantu>"

git add app/src/main/res/values/strings.xml
git commit -m "feat(resource): them chuoi tieng Viet cho ten filter va giao dien"

git add app/src/main/java/com/example/photofilter/data/FilterItem.java app/src/main/java/com/example/photofilter/data/FilterRepository.java
git commit -m "feat(data): them FilterItem va FilterRepository quan ly danh sach filter"

git add app/src/main/java/com/example/photofilter/data/ImageRepository.java
git commit -m "feat(data): them ImageRepository xu ly downsample anh, xoay theo EXIF va luu qua MediaStore"

git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/FilterThumbnail.java
git commit -m "feat(presenter): them EditorContract dinh nghia hop dong MVP giua View va Presenter"

git add app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java
git commit -m "feat(presenter): them EditorPresenter dieu phoi luong nen va quan ly vong doi Bitmap"

git add app/src/test/java/com/example/photofilter/presenter/FakeView.java app/src/test/java/com/example/photofilter/presenter/ImmediateExecutorService.java app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "test(presenter): them unit test EditorPresenter voi FakeView va ImmediateExecutorService"
```

---

## Khối 3 — UI (Phan Lê Huy, tiếp)

```bash
git config user.name "Phan Le Huy"
git config user.email "<email_huy>"

git add app/src/main/res/layout/activity_main.xml app/src/main/res/layout/item_filter_thumbnail.xml app/src/main/res/drawable/bg_filter_thumbnail.xml app/src/main/res/drawable/selector_filter_selected.xml app/src/main/res/values/colors.xml app/src/main/res/values-night/colors.xml
git commit -m "feat(ui): them layout man hinh chinh va item filter thumbnail"

git add app/src/main/java/com/example/photofilter/ui/FilterAdapter.java
git commit -m "feat(ui): them FilterAdapter hien thi dai filter cuon ngang"

git add app/src/main/java/com/example/photofilter/ui/MainActivity.java app/src/main/AndroidManifest.xml
git commit -m "feat(ui): them MainActivity - chon anh, ap filter, luu va chia se"
```

---

## Sau khi xong

```bash
git remote add origin <URL server cua lop>
git push -u origin main
```

**Tổng kết:** Phan Lê Huy 5 commit, Tú Anh 4 commit, Trần Tú 6 commit — khá đều nhau, mỗi khối gắn với đúng 1 gạch đầu dòng trong yêu cầu của thầy (Tú Anh = OOP nâng cao, Trần Tú = kiến trúc/bộ nhớ, Phan Lê Huy = UX). Nếu muốn tách nhỏ hơn nữa (ví dụ mỗi file 1 commit) hoàn toàn có thể chia thêm — thứ tự tổng thể giữ nguyên.

File này chỉ để tham khảo khi commit, có thể xoá khỏi repo trước khi nộp bài nếu muốn cây thư mục gọn hơn.

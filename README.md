# HATFilter

Ứng dụng chỉnh sửa ảnh trên Android (Java) — đồ án môn Lập trình Android.

## Tính năng chính

### Bộ lọc (13 bộ lọc)

Màu gốc, Trắng đen, Âm bản *(3 bộ lọc bắt buộc theo đề bài)*, Sepia, Ấm, Lạnh, Sáng, Cổ điển, Viền tối, Mờ nhẹ, Film, Mono, Retro.

### Chỉnh ảnh

- Cắt theo tỷ lệ (Gốc/1:1/4:3/16:9), Xoay, Lật, Đổi cỡ (75–200%)
- Tuỳ chỉnh thời gian thực: Độ sáng, Tương phản, Bão hoà, Sắc độ (Hue), Phơi sáng (Exposure)

### AI

- Làm nét, Khử nhiễu, Tăng độ phân giải (xử lý ảnh cục bộ)
- Xoá nền bằng ML Kit Selfie Segmentation (on-device)

### Tài khoản

- Đăng nhập / Đăng ký bằng email + mật khẩu — lưu cục bộ bằng **SQLite thuần** (bảng `users`, mật khẩu băm SHA-256, không lưu plain text)
- Đăng xuất từ màn Home (icon góc phải trên hero card)
- Phiên đăng nhập lưu qua `SharedPreferences` — mở lại app không cần đăng nhập lại. Không cần server/Internet, không cần cấu hình gì thêm để build

### Khác

- Chụp ảnh trực tiếp từ camera hoặc chọn từ thư viện
- Lưu ảnh vào thư viện thiết bị + chia sẻ sang ứng dụng khác
- Lịch sử chỉnh sửa và đánh dấu bộ lọc yêu thích (lưu bằng SQLite)
- Màn Home dạng dashboard: hero card hiệu ứng Ken Burns, 4 lối tắt (Gallery/Camera/AI Enhance/History), dải "Recent Photos" lấy từ lịch sử đã lưu

## Kiến trúc

Mô hình **MVP** (Model – View – Presenter), tách biệt rõ giao diện khỏi nghiệp vụ:

```text
domain/filter/   Interface Filter + abstract BaseFilter (Template Method) + các bộ lọc cụ thể
data/            Repository: đọc/ghi ảnh, SQLite (lịch sử, yêu thích, tài khoản), gọi AI (ML Kit)
presenter/       EditorContract (hợp đồng MVP) + EditorPresenter (điều phối nghiệp vụ, luồng nền)
ui/              Activity/Adapter — chỉ hiển thị, không chứa logic xử lý ảnh
```

`LoginActivity`/`RegisterActivity` không dùng Contract/Presenter riêng (theo đúng quy ước "màn đơn giản, không có nghiệp vụ phức tạp thì gọi Repository trực tiếp" — giống `HistoryActivity`) — validate input ở View, tự quản lý executor nền, gọi `AuthRepository` (đồng bộ, chặn luồng — giống `HistoryRepository`).

Bộ nhớ được quản lý chủ động: mọi `Bitmap` trung gian đều được `recycle()` ngay khi không còn dùng (đổi ảnh, đổi bộ lọc, thoát màn hình).

## Công nghệ sử dụng

- Java, Android SDK (minSdk 24, target/compileSdk 34)
- `android.graphics.ColorMatrix` và xử lý mảng pixel trực tiếp (không dùng RenderScript/NDK)
- SQLite thuần (`SQLiteOpenHelper`) — không dùng Room
- Material Components 3 (BottomSheetBehavior, MaterialCardView)
- Glide (tải ảnh), androidx.exifinterface (đọc hướng ảnh)
- ML Kit Selfie Segmentation (xoá nền)
- Robolectric + JUnit + Mockito (unit test)

## Bản quyền tài nguyên bên thứ ba

Sticker trong `app/src/main/res/drawable-nodpi/` (tên bắt đầu bằng `sticker_`) lấy từ [OpenMoji](https://openmoji.org/) — dự án emoji/icon mã nguồn mở, giấy phép [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/).

## Build & chạy

```bash
./gradlew assembleDebug        # build APK debug
./gradlew testDebugUnitTest    # chạy unit test
```

Đăng nhập/Đăng ký không cần cấu hình gì thêm — chạy được ngay sau khi build, không cần server hay API key riêng (dữ liệu tài khoản lưu cục bộ trong SQLite trên máy).

## Phân công thành viên

Chia theo tầng kiến trúc — mỗi người sở hữu trọn vẹn một tầng để dễ làm việc song song và dễ tách commit theo tên.

### Tú Anh — Trưởng nhóm · Domain layer (Filter engine)

- Thiết kế `interface Filter` + `abstract class BaseFilter` theo mẫu Template Method — đáp ứng yêu cầu bắt buộc về khả năng mở rộng (OOP)
- Triển khai toàn bộ 13 bộ lọc: Original, Grayscale, Negative *(3 bộ bắt buộc)*, Sepia, ColorTone (Ấm/Lạnh), BrightnessContrast, Vintage, Vignette (Canvas + RadialGradient), Blur (xử lý mảng pixel thủ công), Film, Mono, Retro
- `ColorAdjustFilter`: chỉnh Brightness/Contrast/Saturation/Hue/Exposure thời gian thực, bao gồm công thức xoay sắc độ (hue rotation matrix)
- Viết unit test Robolectric cho toàn bộ filter kể trên
- Viết README, rà soát code và chạy lại test trước khi nộp bài, phụ trách merge nhánh của cả nhóm cuối mỗi ngày

### Trần Tú — Data & Presenter layer (Nghiệp vụ)

- **Data:** `ImageRepository` (đọc/ghi MediaStore, downsample, sửa hướng ảnh theo EXIF, tạo URI cho camera), `FilterRepository`, `CropUtils`/`CropRatio` (cắt/xoay/lật/đổi cỡ), 3 database SQLite thuần: `HistoryDbHelper`/`HistoryRepository` (lịch sử), `FavoriteDbHelper`/`FavoriteRepository` (yêu thích) và `UserDbHelper`/`UserRepository` (tài khoản — bảng `users`, mật khẩu băm SHA-256); `AuthRepository` điều phối đăng nhập/đăng ký/đăng xuất + phiên đăng nhập (`SharedPreferences`) trên nền `UserRepository`
- **Presenter:** `EditorContract` + `EditorPresenter` — toàn bộ điều phối nghiệp vụ: xử lý bất đồng bộ trên luồng nền, quản lý vòng đời Bitmap (originalBitmap/currentFilteredBitmap), chống race-condition (requestId), tích hợp mọi tính năng AI qua một luồng dùng chung (`applyAiTool`)
- Viết unit test cho tầng Data (History, Favorite) và Presenter (EditorPresenterTest — filter, crop, adjust, AI)

### Phan Lê Huy — UI layer + AI (Giao diện, trải nghiệm & tính năng AI)

- Khởi tạo project: cấu hình Gradle/AGP, phiên bản thư viện, `.gitignore`
- `HomeActivity`: màn Home dạng dashboard — hero card hiệu ứng Ken Burns, hạt sáng trôi (`ParticleView`), 4 thẻ lối tắt, dải Recent Photos, shared element transition sang màn chỉnh sửa, nút đăng xuất
- `MainActivity`: màn chỉnh sửa dạng bottom sheet icon (Bộ lọc/Cắt/Tuỳ chỉnh/AI — Material `BottomSheetBehavior`), `HistoryActivity`, `SaveResultActivity` (màn kết quả sau khi lưu)
- `LoginActivity`/`RegisterActivity`: màn đăng nhập/đăng ký (Firebase Authentication)
- **Toàn bộ mảng AI:** `AiToolsRepository` (Làm nét/Khử nhiễu/Tăng độ phân giải), `BackgroundRemovalRepository` (tích hợp ML Kit Selfie Segmentation), `GeminiEnhanceRepository` (gọi Gemini API) — phần logic AI này đặt trong `data/` nhưng do Phan Lê Huy phụ trách xuyên suốt, không phải Trần Tú
- `FilterAdapter`, `RecentPhotoAdapter`, `HistoryAdapter`
- Toàn bộ hệ màu, ~30 icon vector tự vẽ, theme, thương hiệu HATFilter

> File này có thể xoá khỏi repo trước khi nộp bài nếu muốn cây thư mục gọn hơn — không ảnh hưởng đến việc build ứng dụng.

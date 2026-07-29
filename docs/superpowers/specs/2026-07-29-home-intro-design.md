# Màn Intro chào mừng (giọng nói "Welcome to H.A.T") trước khi vào Home lần đầu

Ngày: 2026-07-29
Trạng thái: Chờ user duyệt spec

## Bối cảnh

Có sẵn 1 file voice "Welcome to H.A.T" (`welcome_to_h_a_t_4297a736-0c80-484a-9305-12edca1b15d9.mp3`, ~12KB, để ở gốc repo, chưa dùng ở đâu) và 1 ảnh minh hoạ (bong bóng xà phòng bay giữa trời xanh, tán cây, phong cách anime — `E:\DownloadFromEdge\da950fa178a1d51bd1d4dd362f125007.jpg`). Muốn ghép 2 thứ này thành 1 màn hình intro chào mừng, phát 1 lần duy nhất trên mỗi thiết bị, ngay sau khi đăng nhập/đăng ký lần đầu, trước khi vào `HomeActivity`.

## Mục tiêu

- Thêm `IntroActivity` mới: full-màn-hình, nền là ảnh bong bóng (Ken Burns zoom nhẹ giống `HomeActivity`), chữ "Welcome to H.A.T" fade-in, phát giọng nói ngay khi vào màn hình.
- Chỉ hiện **đúng 1 lần trên mỗi thiết bị**, bất kể sau đó đăng nhập bằng tài khoản nào (cờ lưu ở `SharedPreferences` có sẵn, không phân biệt theo user).
- Tự động chuyển sang `HomeActivity` khi giọng nói phát xong. Không có nút/chữ "Bỏ qua". Bấm Back hệ thống trong lúc phát = coi như bỏ qua, chuyển thẳng sang Home luôn (không chặn Back).
- Cả 2 lối vào đang dẫn tới `HomeActivity` đều phải qua điểm quyết định này: `LoginActivity` (cả nhánh đăng nhập thành công lẫn nhánh "đã có phiên, bỏ qua form") và `RegisterActivity` (đăng ký thành công).

## Ngoài phạm vi (non-goal)

- Không làm intro theo từng tài khoản riêng (đăng xuất rồi đăng nhập tài khoản khác sẽ **không** thấy lại intro).
- Không thêm hiệu ứng bong bóng bay động (particle) — chỉ Ken Burns zoom tĩnh trên ảnh nền, tái dùng đúng kỹ thuật đang có ở `HomeActivity.startKenBurnsZoom()`.
- Không thêm nút bỏ qua tường minh.
- Không cần đồng bộ animation theo mốc thời gian cụ thể trong file audio (không có phụ đề/timing riêng cho từng từ).

## Kiến trúc

### 1. Cờ "đã xem intro" — thêm vào `AuthRepository`

`AuthRepository` đã quản lý 1 `SharedPreferences` tên `auth_session` (lưu session đăng nhập theo thiết bị) — đây đúng là nơi hợp lý để thêm cờ này, vì cùng là trạng thái "theo thiết bị", không phải theo tài khoản.

```java
private static final String KEY_INTRO_SHOWN = "intro_shown";

public boolean hasSeenIntro() {
    return prefs.getBoolean(KEY_INTRO_SHOWN, false);
}

public void markIntroSeen() {
    prefs.edit().putBoolean(KEY_INTRO_SHOWN, true).apply();
}
```

### 2. Điểm quyết định điều hướng — sửa 3 chỗ đang trỏ thẳng tới `HomeActivity`

- `LoginActivity.onCreate()`, nhánh `authRepository.isLoggedIn()` (đã có phiên, bỏ qua form đăng nhập) → hiện gọi `goToHome()` thẳng.
- `LoginActivity.attemptLogin()`, sau khi `signIn()` trả về `null` (thành công) → hiện gọi `goToHome()`.
- `RegisterActivity.attemptRegister()`, sau khi `signUp()` trả về `null` (thành công) → hiện tạo `Intent` tới `HomeActivity` trực tiếp.

Cả 3 chỗ đổi thành cùng 1 pattern nhỏ (không tách helper dùng chung — chỉ 3 chỗ, 4-5 dòng mỗi chỗ, tách ra thêm 1 lớp/hàm static chỉ để dùng 3 lần là thừa):

```java
Class<?> target = authRepository.hasSeenIntro() ? HomeActivity.class : IntroActivity.class;
Intent intent = new Intent(this, target);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
finish();
```

`LoginActivity.goToHome()` được đổi tên/nội dung theo pattern trên (giữ nguyên tên method để đỡ phải sửa call site, chỉ đổi thân hàm); `RegisterActivity` áp dụng tương tự tại chỗ tạo `Intent`.

### 3. `IntroActivity` (mới)

**File:** `app/src/main/java/com/example/photofilter/ui/IntroActivity.java`
**Layout:** `app/src/main/res/layout/activity_intro.xml`
**Manifest:** khai báo trong `AndroidManifest.xml`, `android:screenOrientation="portrait"` (khớp mọi activity khác), không cần export.

Layout: `FrameLayout` full màn hình —
- Lớp dưới: `ImageView` (`scaleType="centerCrop"`) nguồn `@drawable/img_intro_bubbles`, id `introBackgroundView`.
- Lớp trên: `TextView` "Welcome to H.A.T" căn giữa màn hình, font `sora_extrabold`, màu `accent_yellow`, alpha khởi tạo = 0.

```java
public class IntroActivity extends AppCompatActivity {

    private static final int FALLBACK_DELAY_MS = 2500; // dùng khi không tạo được MediaPlayer

    private AuthRepository authRepository;
    private MediaPlayer mediaPlayer;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        authRepository = new AuthRepository(getApplicationContext());

        startKenBurnsZoom(findViewById(R.id.introBackgroundView));
        findViewById(R.id.introWelcomeText).animate().alpha(1f).setStartDelay(300).setDuration(600).start();

        mediaPlayer = MediaPlayer.create(this, R.raw.welcome_hat);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> goToHome());
            mediaPlayer.start();
        } else {
            mainHandler.postDelayed(this::goToHome, FALLBACK_DELAY_MS);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { goToHome(); }
        });
    }

    private void goToHome() {
        if (navigated) return; // completion listener + back-press + fallback delay không được gọi 2 lần
        navigated = true;
        authRepository.markIntroSeen();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    // startKenBurnsZoom(): copy nguyên logic từ HomeActivity (ValueAnimator 1f→1.15f, 9000ms, REVERSE, INFINITE)
}
```

Điểm quan trọng: `navigated` guard chống gọi `goToHome()` 2 lần (vd: nhạc phát xong đúng lúc user bấm Back) — tránh `startActivity` + `finish()` bị gọi lặp.

### 4. Asset

- Ảnh: copy `E:\DownloadFromEdge\da950fa178a1d51bd1d4dd362f125007.jpg` → `app/src/main/res/drawable-nodpi/img_intro_bubbles.jpg` (thư mục `-nodpi` để giữ nguyên ảnh, không bị hệ thống scale theo mật độ màn hình — đây là ảnh minh hoạ full-bleed, không phải icon).
- Audio: copy `welcome_to_h_a_t_4297a736-0c80-484a-9305-12edca1b15d9.mp3` → `app/src/main/res/raw/welcome_hat.mp3` (đổi tên vì tên resource Android không được chứa dấu gạch ngang/UUID).
- File mp3 gốc ở thư mục gốc repo sẽ bị xoá sau khi copy vào `res/raw/` (tránh rác thư mục gốc, và nó hiện đang là untracked file gây nhiễu `git status`).

## Trường hợp biên

- **`MediaPlayer.create()` trả về `null`** (file lỗi/thiết bị không giải mã được): fallback `postDelayed` 2.5s rồi vào Home, tránh treo màn hình vĩnh viễn.
- **Xoay màn hình / cấu hình thay đổi:** không xảy ra vì đã khoá `portrait`.
- **Process chết giữa chừng / app bị kill:** không có gì để khôi phục — lần mở app tiếp theo, `hasSeenIntro()` vẫn `false` nên sẽ hiện lại intro (chấp nhận được, vì đây không phải dữ liệu quan trọng).
- **Không kiểm thử tự động cho phần audio/animation** (giống `HomeActivity` hiện tại — Robolectric không giả lập tốt `MediaPlayer` thật). Chỉ thêm unit test cho 2 hàm mới `hasSeenIntro()`/`markIntroSeen()` trong `AuthRepositoryTest` (theo đúng pattern test hiện có của lớp này), phần còn lại xác minh thủ công trên emulator.

## File thay đổi

- Sửa: `AuthRepository.java` (thêm cờ), `AuthRepositoryTest.java` (thêm 2 test), `LoginActivity.java`, `RegisterActivity.java`, `AndroidManifest.xml`.
- Thêm: `IntroActivity.java`, `activity_intro.xml`, `res/raw/welcome_hat.mp3`, `res/drawable-nodpi/img_intro_bubbles.jpg`.
- Xoá: file mp3 gốc ở root repo.

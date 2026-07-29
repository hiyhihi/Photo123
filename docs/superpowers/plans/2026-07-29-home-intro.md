# Màn Intro chào mừng (Welcome to H.A.T) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Thêm màn `IntroActivity` phát giọng nói "Welcome to H.A.T" trên nền ảnh bong bóng (Ken Burns zoom), hiện đúng 1 lần trên mỗi thiết bị ngay sau lần đăng nhập/đăng ký đầu tiên, trước khi vào `HomeActivity`.

**Architecture:** Cờ `intro_shown` thêm vào `SharedPreferences` sẵn có của `AuthRepository` (theo thiết bị, không theo tài khoản). `LoginActivity`/`RegisterActivity` kiểm tra cờ này tại đúng 3 điểm hiện đang trỏ tới `HomeActivity`, đổi đích sang `IntroActivity` nếu chưa xem. `IntroActivity` tự đánh dấu đã xem rồi mới chuyển tiếp sang `HomeActivity` khi: giọng nói phát xong, hoặc bấm Back, hoặc `MediaPlayer` tạo lỗi (fallback timer).

**Tech Stack:** Java, Android SDK, `MediaPlayer` cho audio, `ValueAnimator` cho Ken Burns (đúng kỹ thuật đang dùng ở `HomeActivity`), Robolectric + JUnit cho phần test được (chỉ `AuthRepository`).

## Global Constraints

- Cờ "đã xem intro" lưu theo **thiết bị**, không theo tài khoản (dùng chung 1 `SharedPreferences` tên `auth_session` đã có trong `AuthRepository`).
- Không có nút/chữ "Bỏ qua" tường minh; bấm Back = coi như bỏ qua, chuyển thẳng Home (không chặn Back).
- Không thêm hiệu ứng bong bóng bay động — chỉ Ken Burns zoom tĩnh (`1f → 1.15f`, 9000ms, `REVERSE`, `INFINITE`, `AccelerateDecelerateInterpolator`), sao chép đúng thông số từ `HomeActivity.startKenBurnsZoom()`.
- Không thêm thư viện mới.
- Ảnh nền: `app/src/main/res/drawable-nodpi/img_intro_bubbles.jpg` (thư mục `-nodpi` vì đây là ảnh full-bleed, không phải icon cần scale theo mật độ màn hình).
- Audio: `app/src/main/res/raw/welcome_hat.mp3` (đổi tên từ file gốc có UUID/dấu gạch ngang — tên resource Android không được chứa ký tự đó).
- Mọi `Activity` mới đều khoá `android:screenOrientation="portrait"` và `android:exported="false"`, khớp toàn bộ activity hiện có.
- Không viết test tự động cho audio/animation (giống `HomeActivity` hiện tại) — chỉ unit test 2 hàm mới của `AuthRepository`; phần còn lại xác minh thủ công trên emulator ở task cuối.

---

### Task 1: Cờ `intro_shown` trong `AuthRepository`

**Files:**
- Modify: `app/src/main/java/com/example/photofilter/data/AuthRepository.java`
- Test: `app/src/test/java/com/example/photofilter/data/AuthRepositoryTest.java`

**Interfaces:**
- Produces: `AuthRepository.hasSeenIntro()` (trả `boolean`), `AuthRepository.markIntroSeen()` (`void`) — dùng bởi `LoginActivity`/`RegisterActivity`/`IntroActivity` ở Task 3.

- [ ] **Step 1: Viết test cho hành vi mới**

Thêm vào cuối class `AuthRepositoryTest` (trước dấu `}` đóng class, sau method `signOut_clearsSession`):

```java
    @Test
    public void hasSeenIntro_initiallyFalse() {
        assertFalse(repository.hasSeenIntro());
    }

    @Test
    public void markIntroSeen_persists() {
        repository.markIntroSeen();

        assertTrue(repository.hasSeenIntro());
    }
```

- [ ] **Step 2: Chạy test để xác nhận fail**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew testDebugUnitTest --tests "com.example.photofilter.data.AuthRepositoryTest" --console=plain`
Expected: FAIL — lỗi biên dịch, `hasSeenIntro()`/`markIntroSeen()` chưa tồn tại.

- [ ] **Step 3: Thêm cờ vào `AuthRepository`**

Trong `app/src/main/java/com/example/photofilter/data/AuthRepository.java`, thêm hằng số ngay dưới `KEY_CURRENT_EMAIL`:

```java
    private static final String KEY_CURRENT_EMAIL = "current_email";
    private static final String KEY_INTRO_SHOWN = "intro_shown";
```

Thêm 2 method mới ngay sau `getCurrentUserEmail()`:

```java
    public String getCurrentUserEmail() {
        return prefs.getString(KEY_CURRENT_EMAIL, null);
    }

    /** Theo thiết bị, không theo tài khoản — dùng chung cho mọi user đăng nhập trên máy này. */
    public boolean hasSeenIntro() {
        return prefs.getBoolean(KEY_INTRO_SHOWN, false);
    }

    public void markIntroSeen() {
        prefs.edit().putBoolean(KEY_INTRO_SHOWN, true).apply();
    }
```

- [ ] **Step 4: Chạy test để xác nhận pass**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew testDebugUnitTest --tests "com.example.photofilter.data.AuthRepositoryTest" --console=plain`
Expected: PASS, 7 test (5 cũ + 2 mới).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/photofilter/data/AuthRepository.java app/src/test/java/com/example/photofilter/data/AuthRepositoryTest.java
git commit -m "feat(data): them co intro_shown vao AuthRepository (theo thiet bi)"
```

---

### Task 2: Assets + `IntroActivity`

**Files:**
- Create: `app/src/main/res/drawable-nodpi/img_intro_bubbles.jpg` (copy từ `E:\DownloadFromEdge\da950fa178a1d51bd1d4dd362f125007.jpg`)
- Create: `app/src/main/res/raw/welcome_hat.mp3` (copy từ `welcome_to_h_a_t_4297a736-0c80-484a-9305-12edca1b15d9.mp3` ở gốc repo)
- Delete: `welcome_to_h_a_t_4297a736-0c80-484a-9305-12edca1b15d9.mp3` (gốc repo, sau khi đã copy)
- Modify: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/layout/activity_intro.xml`
- Create: `app/src/main/java/com/example/photofilter/ui/IntroActivity.java`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Consumes: `AuthRepository.markIntroSeen()` từ Task 1.
- Produces: `IntroActivity` (activity name `.ui.IntroActivity`) — khởi động bằng `Intent` tường minh, không có `intent-filter` — dùng bởi `LoginActivity`/`RegisterActivity` ở Task 3.

- [ ] **Step 1: Copy 2 file asset, xoá file mp3 gốc**

Run (từ thư mục gốc repo `D:\Photo123`):

```bash
mkdir -p app/src/main/res/drawable-nodpi app/src/main/res/raw
cp "/e/DownloadFromEdge/da950fa178a1d51bd1d4dd362f125007.jpg" "app/src/main/res/drawable-nodpi/img_intro_bubbles.jpg"
cp "welcome_to_h_a_t_4297a736-0c80-484a-9305-12edca1b15d9.mp3" "app/src/main/res/raw/welcome_hat.mp3"
rm "welcome_to_h_a_t_4297a736-0c80-484a-9305-12edca1b15d9.mp3"
```

- [ ] **Step 2: Thêm string chào mừng**

Trong `app/src/main/res/values/strings.xml`, thêm ngay sau dòng `content_desc_account`:

```xml
    <string name="content_desc_account">Tài khoản</string>
    <string name="intro_welcome_text">Welcome to H.A.T</string>
```

- [ ] **Step 3: Tạo layout `activity_intro.xml`**

Create `app/src/main/res/layout/activity_intro.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:id="@+id/introBackgroundView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:contentDescription="@null"
        android:scaleType="centerCrop"
        android:src="@drawable/img_intro_bubbles" />

    <TextView
        android:id="@+id/introWelcomeText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:alpha="0"
        android:fontFamily="@font/sora_extrabold"
        android:letterSpacing="0.04"
        android:shadowColor="#B3000000"
        android:shadowDx="0"
        android:shadowDy="2"
        android:shadowRadius="8"
        android:text="@string/intro_welcome_text"
        android:textColor="@color/accent_yellow"
        android:textSize="28sp" />

</FrameLayout>
```

- [ ] **Step 4: Tạo `IntroActivity`**

Create `app/src/main/java/com/example/photofilter/ui/IntroActivity.java`:

```java
package com.example.photofilter.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photofilter.R;
import com.example.photofilter.data.AuthRepository;

/**
 * One-time welcome screen shown after the first login/registration on a
 * device. Plays a short voice clip and auto-advances to {@link HomeActivity}
 * when it finishes; Back or a MediaPlayer failure both skip straight to Home.
 */
public class IntroActivity extends AppCompatActivity {

    private static final int FALLBACK_DELAY_MS = 2500;

    private AuthRepository authRepository;
    private MediaPlayer mediaPlayer;
    private ValueAnimator kenBurnsAnimator;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        authRepository = new AuthRepository(getApplicationContext());

        startKenBurnsZoom(findViewById(R.id.introBackgroundView));
        findViewById(R.id.introWelcomeText).animate()
                .alpha(1f)
                .setStartDelay(300)
                .setDuration(600)
                .start();

        mediaPlayer = MediaPlayer.create(this, R.raw.welcome_hat);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> goToHome());
            mediaPlayer.start();
        } else {
            mainHandler.postDelayed(this::goToHome, FALLBACK_DELAY_MS);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goToHome();
            }
        });
    }

    private void startKenBurnsZoom(View backgroundView) {
        kenBurnsAnimator = ValueAnimator.ofFloat(1f, 1.15f);
        kenBurnsAnimator.setDuration(9000);
        kenBurnsAnimator.setRepeatMode(ValueAnimator.REVERSE);
        kenBurnsAnimator.setRepeatCount(ValueAnimator.INFINITE);
        kenBurnsAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        kenBurnsAnimator.addUpdateListener(a -> {
            float scale = (float) a.getAnimatedValue();
            backgroundView.setScaleX(scale);
            backgroundView.setScaleY(scale);
        });
        kenBurnsAnimator.start();
    }

    /** Idempotent: completion listener, Back press and the fallback timer can each try to call this once. */
    private void goToHome() {
        if (navigated) {
            return;
        }
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
        if (kenBurnsAnimator != null) {
            kenBurnsAnimator.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
```

- [ ] **Step 5: Khai báo trong `AndroidManifest.xml`**

Trong `app/src/main/AndroidManifest.xml`, thêm activity mới ngay sau `.ui.RegisterActivity`, trước `.ui.HomeActivity`:

```xml
        <activity
            android:name=".ui.RegisterActivity"
            android:exported="false"
            android:screenOrientation="portrait" />
        <activity
            android:name=".ui.IntroActivity"
            android:exported="false"
            android:screenOrientation="portrait" />
        <activity
            android:name=".ui.HomeActivity"
            android:exported="false"
            android:screenOrientation="portrait" />
```

- [ ] **Step 6: Build để xác nhận biên dịch được**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew assembleDebug --console=plain`
Expected: BUILD SUCCESSFUL (không lỗi resource-not-found cho `img_intro_bubbles`/`welcome_hat`, không lỗi biên dịch `IntroActivity`).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/res/drawable-nodpi/img_intro_bubbles.jpg app/src/main/res/raw/welcome_hat.mp3 app/src/main/res/values/strings.xml app/src/main/res/layout/activity_intro.xml app/src/main/java/com/example/photofilter/ui/IntroActivity.java app/src/main/AndroidManifest.xml welcome_to_h_a_t_4297a736-0c80-484a-9305-12edca1b15d9.mp3
git commit -m "feat(ui): them IntroActivity - man chao mung phat giong noi Welcome to H.A.T tren nen anh bong bong (Ken Burns zoom)"
```

(Lệnh `git add` liệt kê cả file mp3 gốc dù đã bị xoá — cần thiết để Git ghi nhận việc xoá file untracked này vào cùng commit; nếu file đã xoá khỏi working tree, `git add` trên đường dẫn đó sẽ báo "did not match any files", bỏ qua tên đó khỏi lệnh là được, không ảnh hưởng các file còn lại.)

---

### Task 3: Điều hướng qua `IntroActivity` từ Login/Register

**Files:**
- Modify: `app/src/main/java/com/example/photofilter/ui/LoginActivity.java`
- Modify: `app/src/main/java/com/example/photofilter/ui/RegisterActivity.java`

**Interfaces:**
- Consumes: `AuthRepository.hasSeenIntro()` từ Task 1, `IntroActivity` từ Task 2.

- [ ] **Step 1: Sửa `LoginActivity.goToHome()`**

Thay toàn bộ method `goToHome()` trong `app/src/main/java/com/example/photofilter/ui/LoginActivity.java`:

```java
    private void goToHome() {
        Class<?> target = authRepository.hasSeenIntro() ? HomeActivity.class : IntroActivity.class;
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
```

(Cả 2 chỗ gọi `goToHome()` đang có — nhánh `authRepository.isLoggedIn()` trong `onCreate()` và nhánh đăng nhập thành công trong `attemptLogin()` — tự động dùng logic mới, không cần sửa gì thêm ở đó.)

- [ ] **Step 2: Sửa `RegisterActivity.attemptRegister()`**

Trong `app/src/main/java/com/example/photofilter/ui/RegisterActivity.java`, thay khối `if (error == null) { ... }` bên trong `attemptRegister()`:

```java
                if (error == null) {
                    Class<?> target = authRepository.hasSeenIntro() ? HomeActivity.class : IntroActivity.class;
                    Intent intent = new Intent(RegisterActivity.this, target);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    setLoading(false);
                    showError(error);
                }
```

- [ ] **Step 3: Build + chạy toàn bộ unit test**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew testDebugUnitTest assembleDebug --console=plain`
Expected: BUILD SUCCESSFUL, mọi test (kể cả `AuthRepositoryTest` mới) đều pass.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/photofilter/ui/LoginActivity.java app/src/main/java/com/example/photofilter/ui/RegisterActivity.java
git commit -m "feat(ui): dieu huong qua IntroActivity truoc Home neu chua xem intro, tu Login va Register"
```

---

### Task 4: Kiểm thử thủ công trên emulator

**Files:** none (verification only).

- [ ] **Step 1: Cài bản build mới nhất**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew installDebug --console=plain` (emulator/thiết bị đang chạy).

- [ ] **Step 2: Xoá dữ liệu app để mô phỏng cài mới**

Run: `adb shell pm clear com.example.photofilter` (xoá cả session lẫn cờ `intro_shown`, coi như máy chưa từng mở app).

- [ ] **Step 3: Đăng ký tài khoản mới, xác nhận Intro hiện ra**

Mở app → màn Đăng nhập → bấm "Đăng ký" → điền email/mật khẩu hợp lệ → đăng ký thành công. Kỳ vọng: vào thẳng `IntroActivity` (ảnh bong bóng phóng to chậm, chữ "Welcome to H.A.T" fade in, có tiếng nói phát ra), sau đó tự động chuyển sang màn Home khi tiếng nói dứt.

- [ ] **Step 4: Đăng xuất, đăng nhập lại — xác nhận Intro KHÔNG hiện lại**

Từ Home, đăng xuất → đăng nhập lại đúng tài khoản vừa tạo. Kỳ vọng: vào thẳng Home, không qua Intro nữa (cờ theo thiết bị đã set).

- [ ] **Step 5: Xác nhận Back trong lúc Intro = bỏ qua**

Chạy lại Bước 2 (`pm clear`) rồi Bước 3 tới khi vào `IntroActivity`, bấm Back ngay khi đang phát tiếng. Kỳ vọng: chuyển thẳng sang Home ngay lập tức (không kẹt ở Intro), và mở lại app sau đó cũng không thấy Intro nữa (cờ đã được set khi bấm Back).

- [ ] **Step 6: Báo cáo kết quả**

Không commit cho task này — báo kết quả trong chat; nếu bước nào fail thì ghi nhận thành việc cần sửa tiếp theo.

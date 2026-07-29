# Việc của Tú Anh là gì? — giải thích bằng lời dễ hiểu

> File này viết riêng cho phần của **Tú Anh** (nhóm trưởng), giải thích bằng ngôn ngữ đời thường xem bạn ấy làm cái gì, tại sao lại làm vậy, và làm cụ thể ra sao. Đọc xong là hiểu được, không cần biết code trước.

---

## Tóm tắt trong 1 câu

Tú Anh **không đụng vào màn hình, không đụng vào database** — chỉ lo đúng 2 thứ: **"bộ não xử lý màu ảnh"** (tức là phần code tính toán ra 13 kiểu lọc ảnh) và vai trò **trưởng nhóm** (viết tài liệu giới thiệu, kiểm tra chất lượng lần cuối, và là người gộp code của cả nhóm lại mỗi ngày).

Nói cách khác: nếu ví cả app như 1 cái nhà hàng —
- Trần Tú lo "kho hàng + bồi bàn" (lưu trữ dữ liệu, chuyển món qua lại),
- Phan Lê Huy lo "trang trí quán + bày món ra đĩa" (giao diện),
- thì **Tú Anh lo đúng công thức nấu ăn** (mỗi bộ lọc ảnh là 1 công thức) — và là bếp trưởng, người nếm lại món ăn lần cuối trước khi mang ra cho khách.

---

## Việc 1: Dựng "cái khuôn" chung cho mọi bộ lọc

Trước khi viết 13 bộ lọc, phải có 1 cái khuôn chung để tất cả bộ lọc đều "vừa khớp" vào chỗ khác trong app (màn hình, danh sách chọn lọc...). Cái khuôn đó gồm 2 file:

- **`Filter.java`** — quy định "muốn được gọi là 1 bộ lọc thì phải có khả năng gì". Giống như quy định "món tráng miệng nào cũng phải có thể múc ra đĩa" — không quan tâm bên trong nó là gì, chỉ cần làm được việc đó.
- **`BaseFilter.java`** — phần code dùng chung cho tất cả bộ lọc (kiểm tra ảnh đầu vào có hỏng không, tạo ảnh kết quả...), để mỗi bộ lọc cụ thể không phải viết lại đoạn này 13 lần.

**Vì sao phải tách làm 2 file** thay vì gộp chung? Vì đề bài yêu cầu phải có cả 2 kiểu (1 cái quy định "phải làm được gì", 1 cái là code dùng chung) — đây cũng là kiểu tổ chức code chuẩn mà môn học muốn thấy.

---

## Việc 2: Viết ra 13 "công thức pha màu"

Đây là phần chiếm nhiều công sức nhất của Tú Anh — viết ra toàn bộ 13 bộ lọc ảnh, mỗi cái là 1 file riêng. Nói bằng lời thường, từng cái làm gì:

| Bộ lọc | Làm gì (nói nôm na) |
|---|---|
| Màu gốc | Không đổi gì cả, giữ nguyên ảnh |
| Trắng đen | Rút hết màu, chỉ còn sáng-tối |
| Âm bản | Đảo ngược màu kiểu phim âm bản cũ (đen thành trắng, trắng thành đen...) |
| Sepia | Nhuộm ảnh ngả nâu vàng kiểu ảnh cổ |
| Ấm / Lạnh | Đẩy tông màu ngả sang đỏ-cam (ấm) hoặc xanh dương (lạnh) |
| Sáng | Tăng độ sáng + tương phản nhẹ |
| Cổ điển (Vintage) | Làm ảnh bạc màu, ngả vàng nhẹ như ảnh cũ để lâu |
| Viền tối (Vignette) | Làm tối dần 4 góc ảnh, giữ sáng ở giữa — tạo cảm giác chiều sâu |
| Mờ nhẹ (Blur) | Làm mờ nhẹ toàn bộ ảnh |
| Film | Giả lập tông màu phim: đỡ gắt, hơi ngả vàng-xanh |
| Mono | Đen trắng nhưng tương phản mạnh hơn "Trắng đen" thường |
| Retro | Tông màu thập niên 70: rực màu, ngả hồng/đỏ |
| (Tuỳ chỉnh) | Không phải 1 nút có sẵn, mà là 5 thanh trượt (Sáng/Tương phản/Bão hoà/Sắc độ/Phơi sáng) người dùng tự kéo — Tú Anh viết cái máy tính đứng sau 5 thanh trượt này |

Tất cả 13 cái này đều dựa vào "cái khuôn" ở Việc 1 — nên viết cái nào cũng theo đúng 1 khuôn mẫu, không cái nào viết lối khác.

---

## Việc 3: Tự kiểm tra bài trước khi nộp (viết test)

Với phần lớn các bộ lọc trên, Tú Anh viết thêm 1 đoạn code khác để **tự động kiểm tra xem bộ lọc có chạy đúng không** — kiểu như tự ra đề rồi tự chấm điểm chính mình, chạy 1 lệnh là biết ngay bộ lọc nào bị hỏng mà không cần mở app lên bấm thử bằng tay. Việc này làm cho code đáng tin hơn khi có ai đó sửa lại sau này (sửa mà lỡ làm hỏng thứ gì, chạy lại test là biết liền).

---

## Việc 4: Viết README — "tờ giới thiệu" cho cả dự án

README là file đầu tiên ai mở repo cũng thấy. Tú Anh (với vai trò trưởng nhóm) viết file này để giới thiệu: app tên gì, làm được gì, cấu trúc code chia làm mấy tầng, muốn chạy thử thì gõ lệnh gì, muốn chạy test thì gõ lệnh gì. Coi như "brochure" giới thiệu nhanh cho người lạ (hoặc thầy cô) đọc trong 2 phút là hiểu sơ bộ cả dự án.

---

## Việc 5: "Nếm lại món ăn" lần cuối + là người gộp code mỗi ngày

Đây là 2 việc gắn liền với vai trò trưởng nhóm, làm **mỗi ngày**, không phải làm 1 lần:

1. **Rà soát cuối + chạy lại toàn bộ test** trước khi merge — đảm bảo code của cả 3 người ghép lại không bị lỗi gì trước khi coi như "xong việc hôm nay".
2. **Là người duy nhất chạy lệnh gộp code** (`git merge`) của cả 3 nhánh (`nhanh-tuanh`, `nhanh-trantu`, `nhanh-huy`) vào nhánh chính (`main`), rồi đẩy lên server (`git push`). Cả nhóm code trên nhánh riêng của mình cả ngày, cuối ngày chỉ Tú Anh mới gộp lại — tránh 3 người cùng đụng vào `main` gây rối.

---

## Việc 6 (mới nhất — "Ngày 7"): viết đề cương trước khi code

Trước khi cả nhóm bắt tay làm 2 tính năng lớn gần đây nhất (Undo/Redo toàn cục, và màn Chào mừng + Sticker), Tú Anh viết trước 1 bản "đề cương" mô tả sẽ làm gì, làm theo cách nào, chia thành mấy bước nhỏ — giống như viết dàn ý trước khi viết bài văn, để cả nhóm biết chắc sẽ làm gì trước khi đụng vào code, đỡ phải sửa đi sửa lại giữa chừng.

---

## Cách làm cụ thể (thực hành từng bước)

Mỗi ngày làm việc của Tú Anh đều theo đúng 1 khuôn:

### Bước 1 — Vào đúng "vai" của Tú Anh

```bash
git checkout nhanh-tuanh
git config user.name "Tu Anh"
git config user.email "<email_tuanh>"
```

Đổi tên/email git tạm thời để mỗi commit ghi đúng "ai làm" — đây là cách giả lập 3 người trong 1 máy.

### Bước 2 — Add đúng file, commit đúng message

Ví dụ 1 ngày làm việc thật (Ngày 2):

```bash
git add app/src/main/java/com/example/photofilter/domain/filter/Filter.java
git commit -m "feat(domain): them interface Filter"

git add app/src/main/java/com/example/photofilter/domain/filter/BaseFilter.java
git commit -m "feat(domain): them abstract class BaseFilter (template method)"

git add app/src/main/java/com/example/photofilter/domain/filter/OriginalFilter.java
git commit -m "feat(domain): trien khai OriginalFilter (Mau goc)"

# ... tương tự cho Grayscale, Negative, rồi tới phần test
```

Toàn bộ danh sách đầy đủ (đúng thứ tự, đúng message) cho **tất cả 7 ngày** đã có sẵn trong file `COMMIT_PLAN.md` ở gốc repo, mục "Tú Anh" của từng ngày — chỉ cần copy-paste theo đúng thứ tự là ra kết quả giống hệt.

### Bước 3 — Cuối ngày: gộp code (chỉ Tú Anh làm bước này)

```bash
git checkout main
git pull origin main          # nếu server đã có gì mới thì lấy về trước
git merge nhanh-tuanh --no-ff
git merge nhanh-trantu --no-ff
git merge nhanh-huy --no-ff
git push origin main
```

`--no-ff` nghĩa là dù merge không bị xung đột (conflict) gì, git vẫn giữ lại dấu vết rõ ràng "đây là 1 lần gộp nhánh" trong lịch sử, thay vì làm cho lịch sử trông như code được viết thẳng trên `main` từ đầu.

### Bước 4 — Trước khi merge lần cuối (cuối mỗi đợt lớn)

```bash
./gradlew testDebugUnitTest assembleDebug
```

Chạy lệnh này để chắc chắn: toàn bộ test đều pass, và app build ra được (không lỗi cú pháp/thiếu file) — pass hết mới merge, không thì phải sửa trước.

---

## Nếu cần làm lại y hệt trên máy khác

1. Mở `COMMIT_PLAN.md`, tìm đúng mục "Tú Anh" của từng ngày (từ Ngày 2 đến Ngày 7).
2. Làm đúng theo Bước 1 → Bước 2 → Bước 3 ở trên, lặp lại cho từng ngày, đúng thứ tự ngày.
3. Nếu code trên máy đó đang cũ hơn (chưa có Ngày 7), chỉ cần làm tiếp từ chỗ máy đó đang dừng — không cần làm lại từ đầu.

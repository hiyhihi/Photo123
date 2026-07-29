const pptxgen = require("pptxgenjs");
const path = require("path");

const SHOTS = path.join(__dirname, "shots");

// ---- Brand palette (matches app's real colors.xml) ----
const BG = "0F1115";       // home_bg
const SURFACE = "1A1D24";  // home_surface
const YELLOW = "FFD60A";   // accent_yellow
const YELLOW_SOFT = "FFE066";
const TEXT = "F5F5F7";     // text_primary
const MUTED = "9AA0AC";    // home_text_muted

const pres = new pptxgen();
pres.layout = "LAYOUT_WIDE"; // 13.33 x 7.5 in

const IMG_Y = 1.3;
const IMG_H = 5.85;
const IMG_W = IMG_H * (1080 / 2400); // 2.6325
const IMG_X = 0.55;

// Convert a fraction (0..1) of the screenshot into slide inches.
function px(fx, fy) {
  return { x: IMG_X + fx * IMG_W, y: IMG_Y + fy * IMG_H };
}

function addBg(slide) {
  slide.background = { color: BG };
}

function addTitle(slide, kicker, title) {
  slide.addText(kicker.toUpperCase(), {
    x: 0.55, y: 0.28, w: 8, h: 0.35,
    fontFace: "Calibri", fontSize: 13, bold: true, color: YELLOW, charSpacing: 2,
  });
  slide.addText(title, {
    x: 0.55, y: 0.55, w: 8.5, h: 0.6,
    fontFace: "Cambria", fontSize: 28, bold: true, color: TEXT,
  });
}

/**
 * A "screenshot with circled callouts" slide.
 * callouts: [{ fx, fy, num, big }]  (fx/fy in 0..1 of the 1080x2400 screenshot)
 * legend:   [{ num, head, body }]
 */
function addScreenshotSlide(kicker, title, imgFile, callouts, legend, notes) {
  const slide = pres.addSlide();
  addBg(slide);
  addTitle(slide, kicker, title);
  if (notes) {
    slide.addNotes(notes);
  }

  // Phone frame behind the screenshot (subtle rounded card, no accent stripe)
  slide.addShape(pres.ShapeType.roundRect, {
    x: IMG_X - 0.12, y: IMG_Y - 0.12, w: IMG_W + 0.24, h: IMG_H + 0.24,
    rectRadius: 0.18, fill: { color: SURFACE }, line: { color: "2A2E38", width: 1 },
    shadow: { type: "outer", color: "000000", opacity: 0.4, blur: 12, offset: 4, angle: 90 },
  });
  slide.addImage({ path: imgFile, x: IMG_X, y: IMG_Y, w: IMG_W, h: IMG_H });

  // Numbered circles on the screenshot
  callouts.forEach((c) => {
    const p = px(c.fx, c.fy);
    const d = c.big ? 0.54 : 0.36;
    slide.addShape(pres.ShapeType.ellipse, {
      x: p.x - d / 2, y: p.y - d / 2, w: d, h: d,
      fill: { color: YELLOW, transparency: 78 },
      line: { color: YELLOW, width: 2.25 },
    });
    slide.addText(String(c.num), {
      x: p.x - d / 2, y: p.y - d / 2, w: d, h: d,
      align: "center", valign: "middle", fontFace: "Calibri", fontSize: 15, bold: true, color: YELLOW,
      margin: 0,
    });
  });

  // Legend column
  const legX = IMG_X + IMG_W + 0.65;
  const legW = 13.33 - legX - 0.55;
  let y = IMG_Y + 0.05;
  const rowH = (IMG_H - 0.1) / legend.length;
  legend.forEach((item) => {
    slide.addShape(pres.ShapeType.ellipse, {
      x: legX, y: y + 0.02, w: 0.5, h: 0.5,
      fill: { color: YELLOW, transparency: 78 }, line: { color: YELLOW, width: 2 },
    });
    slide.addText(String(item.num), {
      x: legX, y: y + 0.02, w: 0.5, h: 0.5, align: "center", valign: "middle",
      fontFace: "Calibri", fontSize: 16, bold: true, color: YELLOW, margin: 0,
    });
    slide.addText(item.head, {
      x: legX + 0.68, y: y - 0.03, w: legW - 0.68, h: 0.4,
      fontFace: "Calibri", fontSize: 16, bold: true, color: TEXT, margin: 0,
    });
    slide.addText(item.body, {
      x: legX + 0.68, y: y + 0.33, w: legW - 0.68, h: rowH - 0.4,
      fontFace: "Calibri", fontSize: 12.5, color: MUTED, margin: 0, valign: "top",
    });
    y += rowH;
  });

  return slide;
}

// =====================================================================
// Slide 1 — Title
// =====================================================================
{
  const slide = pres.addSlide();
  addBg(slide);

  slide.addShape(pres.ShapeType.ellipse, {
    x: 9.6, y: -1.6, w: 5.6, h: 5.6, fill: { color: YELLOW, transparency: 90 }, line: { type: "none" },
  });
  slide.addShape(pres.ShapeType.ellipse, {
    x: -1.8, y: 5.2, w: 4.4, h: 4.4, fill: { color: YELLOW, transparency: 93 }, line: { type: "none" },
  });

  slide.addText("HATFilter", {
    x: 0.9, y: 2.55, w: 10, h: 1.3, fontFace: "Cambria", fontSize: 60, bold: true, color: YELLOW, margin: 0,
  });
  slide.addText("Transform every photo beautifully.", {
    x: 0.95, y: 3.65, w: 9, h: 0.55, fontFace: "Calibri", fontSize: 20, color: TEXT, margin: 0,
  });
  slide.addText("Ứng dụng chỉnh sửa ảnh Android  ·  Đồ án môn Lập trình Android", {
    x: 0.95, y: 4.25, w: 9, h: 0.5, fontFace: "Calibri", fontSize: 14, color: MUTED, margin: 0,
  });

  slide.addText("Tú Anh   ·   Trần Tú   ·   Phan Lê Huy", {
    x: 0.95, y: 6.55, w: 9, h: 0.4, fontFace: "Calibri", fontSize: 13, bold: true, color: YELLOW_SOFT, margin: 0,
  });
  slide.addNotes(
    "Kính chào thầy/cô. Em xin phép demo đồ án HATFilter — ứng dụng chỉnh sửa ảnh trên Android, làm bởi nhóm 3 thành viên: Tú Anh phụ trách domain (bộ máy filter), Trần Tú phụ trách data/presenter, Phan Lê Huy phụ trách giao diện và AI. Sau đây em sẽ đi qua từng tính năng chính của app."
  );
}

// =====================================================================
// Slide 2 — Welcome / Intro
// =====================================================================
addScreenshotSlide(
  "Lần đầu mở app",
  "Chỉ hiện đúng 1 lần trên mỗi máy",
  path.join(SHOTS, "01_welcome.png"),
  [{ fx: 0.5, fy: 0.487, num: 1, big: true }],
  [
    { num: 1, head: "Giọng nói + hiệu ứng Ken Burns", body: "Nền ảnh phóng to chậm liên tục, giọng nói “Welcome to H.A.T” phát tự động khi vào màn." },
    { num: 2, head: "Chỉ hiện đúng 1 lần / thiết bị", body: "Ngay sau lần đăng nhập/đăng ký đầu tiên trên máy. Mở app lần sau sẽ vào thẳng Home." },
    { num: 3, head: "Xem lại bất kỳ lúc nào", body: "Có nút “Xem lại màn chào mừng” trong menu tài khoản ở Home, tiện demo nhiều lần." },
  ]
);

// =====================================================================
// Slide 3 — Login / Register
// =====================================================================
addScreenshotSlide(
  "Tài khoản",
  "Đăng nhập / Đăng ký",
  path.join(SHOTS, "02_login.png"),
  [
    { fx: 0.5, fy: 0.305, num: 1 },
    { fx: 0.83, fy: 0.472, num: 2 },
    { fx: 0.5, fy: 0.535, num: 3 },
  ],
  [
    { num: 1, head: "Xác thực cục bộ bằng SQLite", body: "Không cần server hay mạng — email/mật khẩu lưu trực tiếp trên máy, mật khẩu băm SHA-256." },
    { num: 2, head: "Đăng nhập", body: "Kiểm tra khớp email + mật khẩu đã băm, báo lỗi rõ ràng nếu sai." },
    { num: 3, head: "Chuyển nhanh Đăng nhập ↔ Đăng ký", body: "Chưa có tài khoản bấm sang màn Đăng ký ngay, không cần thoát app." },
  ],
  "Sau màn chào mừng là màn Đăng nhập. Em dùng SQLite cục bộ thay vì Firebase để không phụ thuộc mạng — mật khẩu được băm SHA-256 trước khi lưu, không lưu dạng chữ thường. Nếu chưa có tài khoản, bấm 'Đăng ký' ngay bên dưới là chuyển màn tức thì, không cần thoát app ra vào lại."
);

// =====================================================================
// Slide 4 — Home
// =====================================================================
addScreenshotSlide(
  "Màn chính",
  "Home — 4 lối vào nhanh",
  path.join(SHOTS, "03_home.png"),
  [
    { fx: 0.854, fy: 0.0915, num: 4 },
    { fx: 0.274, fy: 0.6165, num: 1 },
    { fx: 0.728, fy: 0.6165, num: 1 },
    { fx: 0.274, fy: 0.762, num: 1 },
    { fx: 0.728, fy: 0.762, num: 1 },
    { fx: 0.5, fy: 0.875, num: 2 },
  ],
  [
    { num: 1, head: "4 thẻ chức năng", body: "Thư viện, Máy ảnh, Công cụ (Làm nét/Khử nhiễu/Xoá nền...), Lịch sử — mỗi thẻ 1 khối độc lập, bố cục GridLayout 2 cột." },
    { num: 2, head: "Recent Photos", body: "Dải ảnh vừa chỉnh sửa gần đây, lấy dữ liệu thật từ Lịch sử đã lưu (SQLite)." },
    { num: 4, head: "Menu tài khoản", body: "Đăng xuất, hoặc xem lại màn chào mừng — góc trên bên phải khối hero." },
  ],
  "Đây là màn Home. Bốn thẻ chính đưa người dùng vào các luồng hay dùng nhất: Thư viện, Máy ảnh, Công cụ và Lịch sử — mỗi thẻ giờ là 1 khối độc lập trong GridLayout, không còn lồng chung 2 thẻ 1 hàng như bản cũ. Recent Photos bên dưới lấy đúng dữ liệu đã lưu, không phải dữ liệu giả. Góc phải trên cùng là menu tài khoản để đăng xuất hoặc xem lại màn chào mừng."
);

// =====================================================================
// Slide 5 — Editor overview
// =====================================================================
addScreenshotSlide(
  "Màn chỉnh sửa",
  "Thanh công cụ trên cùng & 5 tab chức năng",
  path.join(SHOTS, "04_editor_overview.png"),
  [
    { fx: 0.734, fy: 0.062, num: 1 },
    { fx: 0.5, fy: 0.9865, num: 2, big: true },
  ],
  [
    { num: 1, head: "Hoàn tác / Làm lại / Lưu", body: "Undo-Redo dùng chung cho MỌI thao tác ở cả 5 tab — không riêng lẻ theo từng tab như trước." },
    { num: 2, head: "5 tab công cụ", body: "Bộ lọc · Cắt · Tuỳ chỉnh · Công cụ · Sticker — mỗi tab có luồng nháp (draft) riêng: mở tab để thử, Xác nhận mới lưu, Huỷ thì không đổi gì." },
  ],
  "Vào màn chỉnh sửa, phía trên là 3 nút Hoàn tác, Làm lại và Lưu — đây là điểm mới quan trọng nhất: trước kia mỗi tab tự xử lý riêng, giờ có 1 lịch sử Undo/Redo DÙNG CHUNG cho cả 5 tab bên dưới. Mỗi tab khi mở ra sẽ vào chế độ nháp, chỉnh xong bấm Xác nhận mới thật sự lưu, còn Huỷ thì bỏ qua không đổi gì."
);

// =====================================================================
// Slide 6 — Bo loc
// =====================================================================
addScreenshotSlide(
  "Tab Bộ lọc",
  "13 bộ lọc dựng sẵn",
  path.join(SHOTS, "05_filters.png"),
  [
    { fx: 0.5, fy: 0.7825, num: 1, big: true },
    { fx: 0.265, fy: 0.853, num: 2 },
    { fx: 0.735, fy: 0.853, num: 2 },
  ],
  [
    { num: 1, head: "13 bộ lọc", body: "Màu gốc, Trắng đen, Âm bản, Sepia, Ấm/Lạnh, Vintage, Vền tối, Mờ nhẹ, Phim, Mono, Retro... xem trước ngay khi chạm." },
    { num: 2, head: "Huỷ / Xác nhận", body: "Xác nhận mới commit thành 1 bước Hoàn tác; Huỷ quay lại ảnh trước đó, không mất gì." },
  ],
  "Tab đầu tiên là Bộ lọc, có 13 bộ lọc dựng sẵn từ đơn giản như Trắng đen, Âm bản đến phức tạp hơn như Vintage, Phim, Retro. Chạm vào là xem trước ngay trên ảnh. Xác nhận thì lưu thành 1 bước trong lịch sử Hoàn tác, còn Huỷ thì ảnh trở lại y như trước khi mở tab."
);

// =====================================================================
// Slide 7 — Cat
// =====================================================================
addScreenshotSlide(
  "Tab Cắt",
  "Cắt theo tỉ lệ, Xoay, Lật, Cắt tự do",
  path.join(SHOTS, "06_crop.png"),
  [
    { fx: 0.11, fy: 0.756, num: 3 },
    { fx: 0.44, fy: 0.756, num: 1 },
    { fx: 0.79, fy: 0.756, num: 2 },
  ],
  [
    { num: 1, head: "5 tỉ lệ cố định", body: "1:1, 4:3, 16:9 và Tự do (kéo 4 góc, có lưới rule-of-thirds)." },
    { num: 2, head: "Xoay 90° / Lật ngang", body: "Áp dụng tức thì trên bản nháp, xem trước trước khi Xác nhận." },
    { num: 3, head: "“Gốc” luôn đúng ảnh gốc", body: "Sửa lỗi cũ: trước đây bấm “Gốc” không đưa về đúng ảnh ban đầu nếu đã cắt/xoay nhiều bước — nay luôn về đúng." },
  ],
  "Tab Cắt có đủ 4 tỉ lệ cố định cộng Cắt tự do kéo tay có lưới rule-of-thirds, kèm Xoay 90 độ và Lật ngang. Điểm em muốn nhấn mạnh là nút 'Gốc': trước đây có 1 lỗi là bấm Gốc không đưa ảnh về đúng bản gốc nếu đã cắt/xoay nhiều lần — em đã sửa bằng cách luôn giữ 1 bản gốc riêng (pristine), không bao giờ bị ghi đè."
);

// =====================================================================
// Slide 8 — Tuy chinh
// =====================================================================
addScreenshotSlide(
  "Tab Tuỳ chỉnh",
  "5 thanh điều chỉnh màu sắc",
  path.join(SHOTS, "07_adjust.png"),
  [
    { fx: 0.5, fy: 0.756, num: 1, big: true },
  ],
  [
    { num: 1, head: "Độ sáng · Tương phản · Bão hoà · Sắc độ · Phơi sáng", body: "Kéo thanh trượt, ảnh cập nhật theo thời gian thực ngay trên bản nháp trước khi Xác nhận." },
  ],
  "Tab Tuỳ chỉnh có 5 thanh trượt: Độ sáng, Tương phản, Bão hoà, Sắc độ và Phơi sáng. Kéo tới đâu ảnh cập nhật ngay tới đó theo thời gian thực, giống các app chỉnh ảnh chuyên nghiệp."
);

// =====================================================================
// Slide 9 — Cong cu (AI)
// =====================================================================
addScreenshotSlide(
  "Tab Công cụ",
  "4 công cụ chỉnh sửa nâng cao",
  path.join(SHOTS, "08_ai_tools.png"),
  [
    { fx: 0.24, fy: 0.756, num: 1 },
    { fx: 0.76, fy: 0.756, num: 2 },
  ],
  [
    { num: 1, head: "Xử lý ảnh thuần", body: "Làm nét, Khử nhiễu, Tăng độ phân giải — xử lý trực tiếp mảng pixel, không cần mạng." },
    { num: 2, head: "Xoá nền bằng AI thật", body: "Dùng Google ML Kit Selfie Segmentation — mô hình học máy chạy ngay trên máy." },
  ],
  "Tab Công cụ gồm 4 tính năng: Làm nét, Khử nhiễu và Tăng độ phân giải là xử lý pixel thuần, không cần mạng. Riêng Xoá nền là công cụ AI thật sự, dùng mô hình Selfie Segmentation của Google ML Kit chạy ngay trên thiết bị."
);

// =====================================================================
// Slide 10 — Sticker
// =====================================================================
addScreenshotSlide(
  "Tab Sticker",
  "Dán, kéo, phóng to/nhỏ, xoay sticker",
  path.join(SHOTS, "09_sticker.png"),
  [
    { fx: 0.5, fy: 0.53, num: 2, big: true },
    { fx: 0.5, fy: 0.899, num: 1 },
  ],
  [
    { num: 1, head: "15 sticker (OpenMoji)", body: "Bộ icon/emoji mã nguồn mở, giấy phép CC BY-SA 4.0, ghi công đầy đủ trong README." },
    { num: 2, head: "Kéo · Pinch-zoom · Xoay", body: "1 ngón để di chuyển; 2 ngón vừa phóng to/nhỏ vừa xoay cùng lúc — đổi sticker khác vẫn giữ nguyên vị trí/góc/kích thước." },
  ],
  "Tab cuối cùng là Sticker — tính năng em thêm mới. Có 15 sticker lấy từ bộ OpenMoji mã nguồn mở, ghi công rõ trong README. Chạm 1 sticker để dán lên ảnh, kéo 1 ngón để di chuyển, 2 ngón vừa phóng to/nhỏ vừa xoay cùng lúc giống Instagram. Nếu đổi sang sticker khác, vị trí/góc/kích thước vẫn giữ nguyên, chỉ đổi hình."
);

// =====================================================================
// Slide 11 — Ky thuat + Cam on
// =====================================================================
{
  const slide = pres.addSlide();
  addBg(slide);
  addTitle(slide, "Kiến trúc", "MVP · Undo/Redo toàn cục · Unit test");

  const items = [
    ["Kiến trúc MVP", "domain (Filter engine) · data (Repository/SQLite) · presenter (EditorPresenter + EditHistory) · ui — tách bạch rõ ràng."],
    ["EditHistory", "Ngăn xếp Undo/Redo tối đa 15 bước, ảnh gốc (pristine) luôn được ghim riêng, không bao giờ bị ghi đè."],
    ["Kiểm thử tự động", "Robolectric + JUnit cho toàn bộ Filter, CropUtils, EditorPresenter, EditHistory, AuthRepository..."],
    ["100% cục bộ", "SQLite thuần cho tài khoản/lịch sử/yêu thích — không phụ thuộc server hay Internet (trừ Xoá nền tải model lần đầu)."],
  ];

  const colW = 5.9, gap = 0.5;
  items.forEach((it, i) => {
    const col = i % 2, row = Math.floor(i / 2);
    const x = 0.55 + col * (colW + gap);
    const y = 1.7 + row * 2.35;
    slide.addShape(pres.ShapeType.roundRect, {
      x, y, w: colW, h: 2.05, rectRadius: 0.12,
      fill: { color: SURFACE }, line: { type: "none" },
    });
    slide.addShape(pres.ShapeType.ellipse, {
      x: x + 0.35, y: y + 0.3, w: 0.55, h: 0.55,
      fill: { color: YELLOW, transparency: 78 }, line: { color: YELLOW, width: 2 },
    });
    slide.addText(String(i + 1), {
      x: x + 0.35, y: y + 0.3, w: 0.55, h: 0.55, align: "center", valign: "middle",
      fontFace: "Calibri", fontSize: 18, bold: true, color: YELLOW, margin: 0,
    });
    slide.addText(it[0], {
      x: x + 1.1, y: y + 0.28, w: colW - 1.4, h: 0.4,
      fontFace: "Calibri", fontSize: 17, bold: true, color: TEXT, margin: 0,
    });
    slide.addText(it[1], {
      x: x + 0.35, y: y + 1.0, w: colW - 0.7, h: 0.95,
      fontFace: "Calibri", fontSize: 12.5, color: MUTED, margin: 0, valign: "top",
    });
  });
  slide.addNotes(
    "Về mặt kỹ thuật, app theo kiến trúc MVP tách rõ 4 tầng: domain, data, presenter, ui. Điểm em tự hào nhất là EditHistory — lớp quản lý Undo/Redo tối đa 15 bước, luôn giữ riêng 1 bản ảnh gốc không bao giờ bị ghi đè. Toàn bộ logic quan trọng đều có unit test bằng Robolectric/JUnit, và app chạy 100% cục bộ bằng SQLite, không phụ thuộc server."
  );
}

// =====================================================================
// Slide 12 — Thank you
// =====================================================================
{
  const slide = pres.addSlide();
  addBg(slide);
  slide.addShape(pres.ShapeType.ellipse, {
    x: -2, y: -2, w: 6, h: 6, fill: { color: YELLOW, transparency: 91 }, line: { type: "none" },
  });
  slide.addText("Cảm ơn thầy/cô đã theo dõi!", {
    x: 0.9, y: 3.0, w: 11.5, h: 1.0, fontFace: "Cambria", fontSize: 40, bold: true, color: YELLOW, margin: 0,
  });
  slide.addText("HATFilter  ·  Đồ án Lập trình Android", {
    x: 0.95, y: 4.05, w: 9, h: 0.5, fontFace: "Calibri", fontSize: 16, color: TEXT, margin: 0,
  });
  slide.addNotes(
    "Trên đây là toàn bộ các tính năng chính của HATFilter. Em xin cảm ơn thầy/cô đã theo dõi, và sẵn sàng trả lời câu hỏi nếu có."
  );
}

pres.writeFile({ fileName: path.join(__dirname, "HATFilter_Demo.pptx") }).then(() => {
  console.log("done");
});

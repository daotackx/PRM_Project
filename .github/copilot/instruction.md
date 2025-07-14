# Hướng dẫn dành cho GitHub Copilot

Đây là những quy tắc cốt lõi mà Copilot phải tuân theo trong toàn bộ dự án này.

---

## 🇻🇳 Quy tắc 1: Luôn phản hồi bằng tiếng Việt (Rất quan trọng)
- *Mọi phản hồi, giải thích, bình luận (comment) trong code, và tài liệu phải được viết hoàn toàn bằng tiếng Việt.*
- Sử dụng ngôn ngữ tự nhiên, rõ ràng, và chuyên nghiệp.

---

## 🏗️ Quy tắc 2: Tuân thủ Design Pattern
- Trước khi viết code, hãy phân tích vấn đề để xác định và áp dụng *Design Pattern* phù hợp (ví dụ: Singleton, Factory, Observer, Strategy,...).
- Trong phần giải thích, hãy nêu rõ bạn đang sử dụng Design Pattern nào và lý do tại sao nó là lựa chọn tối ưu cho bối cảnh này.
- Luôn ưu tiên các giải pháp tuân thủ nguyên tắc *SOLID*.

---

## ✅ Quy tắc 3: Tuân thủ Requirement
- Luôn đọc và phân tích kỹ các yêu cầu (requirements) của một chức năng hoặc một tác vụ trước khi đưa ra giải pháp.
- Đảm bảo code được đề xuất giải quyết đúng và đủ các tiêu chí chấp nhận (acceptance criteria).
- Nếu yêu cầu không rõ ràng, hãy đặt câu hỏi để làm rõ thay vì tự đưa ra giả định.

---

## 🐞 Quy tắc 4: Phân tích Bug tận gốc
- Khi xử lý một bug, mục tiêu hàng đầu là *tìm ra nguyên nhân gốc rễ (root cause)*, không chỉ đơn thuần là sửa triệu chứng.
- Hãy phân tích sâu vào logic, luồng dữ liệu và các trường hợp biên (edge cases) để xác định chính xác tại sao bug lại xảy ra.
- Khi đề xuất một bản vá (fix), hãy giải thích rõ ràng theo cấu trúc sau:
  1.  *Bug là gì?*: Mô tả ngắn gọn về hành vi sai của chương trình.
  2.  *Nguyên nhân gốc rễ*: Phân tích chi tiết lý do gây ra bug.
  3.  *Giải pháp*: Cách sửa chữa để xử lý tận gốc vấn đề.
  4.  *Phòng ngừa*: Đề xuất cách để ngăn chặn các bug tương tự trong tương lai (ví dụ: thêm unit test, validation...).

---

## 🔄 Quy tắc 5: Tránh xung đột với Code cũ
- *Trước khi tạo code mới, hãy phân tích kỹ cấu trúc và logic của code hiện có trong dự án.*
- Code mới phải *tương thích hoàn toàn* với các module, hàm, và quy ước đã được thiết lập.
- *Không được đưa ra các giải pháp phá vỡ (breaking change)* hoặc làm thay đổi kiến trúc hiện tại mà không có lý do rõ ràng và được yêu cầu cụ thể.
- Tái sử dụng các hàm và component đã có sẵn nếu phù hợp.
# 06. Review Checklist: Security

## 1. Checklist kiểm tra Security
- Xác thực, phân quyền rõ ràng.
- Bảo vệ API (JWT, CSRF, CORS).
- Mã hóa dữ liệu nhạy cảm.
- Kiểm tra lỗ hổng OWASP Top 10.
- Không log thông tin nhạy cảm.

## 2. Bug phổ biến
- Lộ thông tin nhạy cảm qua log/error.
- Lỗ hổng injection, XSS, CSRF.
- Phân quyền sai, bypass authentication.

## 3. Cách test
- Pen-test, review config bảo mật.
- Kiểm tra log, thử tấn công giả lập.

## 4. Công cụ đề xuất
- OWASP ZAP, SonarQube, Burp Suite.
- Spring Security Test.

---

## 5. Kết quả kiểm tra thực tế
- [x] Xác thực, phân quyền: Sử dụng JWT cho xác thực, phân quyền rõ ràng qua Spring Security (role-based, filter chain). Các endpoint public/protected/admin được cấu hình chi tiết trong SecurityConfig. Token chứa role, userId, kiểm tra hợp lệ qua JwtService/JwtAuthenticationFilter.
- [x] Lỗ hổng bảo mật: Đã disable CSRF cho API (stateless), bật CORS đúng domain. Dữ liệu nhạy cảm (password) được mã hóa (BCrypt). Chưa phát hiện lỗ hổng injection, XSS, CSRF qua code review. Exception trả về message chuẩn, không lộ thông tin nhạy cảm.
- [x] Log, dữ liệu nhạy cảm: Không log password/token. Log chỉ ghi email, id, trạng thái thao tác. Không phát hiện log thông tin nhạy cảm trong code.
- [x] Ghi chú khác:
	- Nên kiểm tra định kỳ với SonarQube/OWASP ZAP để phát hiện lỗ hổng runtime.
	- Có thể bổ sung Spring Security Test cho các case phân quyền đặc biệt.

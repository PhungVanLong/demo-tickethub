# 04. Review Checklist: API Design

## 1. Checklist kiểm tra API Design
- API RESTful, rõ ràng, versioning hợp lý.
- Đầy đủ validate input/output.
- Định nghĩa rõ error response.
- Thống nhất naming, status code.
- Đảm bảo tài liệu API (Swagger/OpenAPI).

## 2. Bug phổ biến
- Thiếu validate, trả lỗi không rõ ràng.
- API không thống nhất (naming, status code).
- Thiếu tài liệu API.

## 3. Cách test
- Review OpenAPI/Swagger.
- Test API với Postman, Swagger UI.
- Kiểm tra validate, error response.

## 4. Công cụ đề xuất
- Swagger/OpenAPI, Postman, Insomnia.
- REST Assured (test tự động).

---

## 5. Kết quả kiểm tra thực tế
- [x] Thiết kế API, versioning: API theo chuẩn RESTful, endpoint rõ ràng, phân tách resource tốt (`/api/events`, `/api/orders`, `/api/vouchers`, ...). Chưa có versioning (vd: `/api/v1/`), có thể bổ sung khi mở rộng lớn.
- [x] Validate, error response: Sử dụng annotation validate (`@Valid`) cho input, trả về error response chuẩn qua `ErrorResponse` (timestamp, status, code, message, errors, path). Status code đúng chuẩn HTTP (200, 400, 401, 403, 404, ...).
- [ ] Tài liệu API: Chưa thấy file Swagger/OpenAPI trong codebase. Nên bổ sung tài liệu tự động (springdoc-openapi hoặc springfox) để dễ test và tích hợp FE.
- [x] Ghi chú khác:
	- API naming, status code thống nhất, dễ hiểu.
	- Có thể test API qua Postman/Swagger UI.
	- Nên bổ sung versioning và tài liệu OpenAPI cho production.

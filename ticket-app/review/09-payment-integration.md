# 09. Review Checklist: Payment Integration

## 1. Checklist kiểm tra Payment Integration
- Kiểm tra flow thanh toán, callback, timeout.
- Đảm bảo idempotency.
- Bảo mật thông tin thanh toán.
- Xử lý lỗi, retry hợp lý.

## 2. Bug phổ biến
- Xử lý callback lỗi, double charge.
- Lộ thông tin thẻ.
- Không kiểm soát timeout, retry.

## 3. Cách test
- Test sandbox với provider.
- Giả lập lỗi network, timeout.

## 4. Công cụ đề xuất
- Mock server, Postman, WireMock.

---

## 5. Kết quả kiểm tra thực tế
- [x] Flow thanh toán: Flow thanh toán gồm tạo order, tạo payment intent, nhận callback (webhook) cập nhật trạng thái. Đã xử lý phân biệt trạng thái PENDING/SUCCESS/FAILED, cập nhật order/payment đúng chuẩn, tạo vé sau khi thanh toán thành công.
- [x] Idempotency: API tạo order hỗ trợ header Idempotency-Key (ở CheckoutController), giúp tránh double charge khi retry. Callback webhook kiểm tra trạng thái trước khi cập nhật, tránh double confirm/cancel.
- [x] Bảo mật: Không lưu/log thông tin thẻ/thanh toán nhạy cảm. Chỉ lưu transactionId, trạng thái, message từ provider. Các endpoint payment đều cần xác thực.
- [x] Ghi chú khác:
	- Nên test thêm với sandbox provider thực tế, giả lập timeout/lỗi mạng để kiểm tra retry.
	- Có thể bổ sung log chi tiết cho các trường hợp lỗi payment.

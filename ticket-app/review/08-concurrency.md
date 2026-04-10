# 08. Review Checklist: Concurrency

## 1. Checklist kiểm tra Concurrency
- Đảm bảo thread-safe cho service dùng chung.
- Kiểm tra transaction, deadlock.
- Đánh giá xử lý race condition.
- Đảm bảo transaction rollback đúng.

## 2. Bug phổ biến
- Deadlock, race condition.
- Transaction không rollback đúng.
- Không thread-safe.

## 3. Cách test
- Viết test đồng thời (concurrent test).
- Review transaction config.

## 4. Công cụ đề xuất
- JUnit (parallel test), Awaitility.
- Java Concurrency Tools.

---

## 5. Kết quả kiểm tra thực tế
- [x] Thread-safe: Các service đều là singleton, không lưu state nội bộ, chỉ thao tác qua repository nên thread-safe. Không phát hiện biến static/mutable dùng chung giữa thread.
- [x] Transaction, deadlock: Các thao tác ghi dữ liệu đều bọc @Transactional. Một số thao tác giữ chỗ ghế (holdSeats) dùng for update để tránh race condition, đảm bảo rollback khi lỗi. Chưa phát hiện deadlock qua code review.
- [x] Race condition: Đã xử lý race condition khi giữ chỗ ghế (findByIdInForUpdate), cập nhật trạng thái seat, hold token đều trong transaction. Các thao tác liên quan đến usageLimit voucher cũng nên kiểm tra lock khi concurrent checkout.
- [x] Ghi chú khác:
	- Nên bổ sung test đồng thời cho các nghiệp vụ quan trọng (đặt vé, giữ ghế, sử dụng voucher).
	- Có thể dùng Awaitility/JUnit parallel test để kiểm tra thực tế.

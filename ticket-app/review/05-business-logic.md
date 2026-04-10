# 05. Review Checklist: Business Logic

## 1. Checklist kiểm tra Business Logic
- Logic tách biệt khỏi controller.
- Đảm bảo unit test cho các rule quan trọng.
- Kiểm tra xử lý edge case.
- Đảm bảo không trùng lặp logic giữa các service.

## 2. Bug phổ biến
- Logic trùng lặp, khó maintain.
- Thiếu test case cho case đặc biệt.
- Logic business nằm trong controller.

## 3. Cách test
- Review code, viết/kiểm tra unit test.
- Test manual các case đặc biệt.

## 4. Công cụ đề xuất
- JUnit, Mockito (test).
- JaCoCo (coverage).

---

## 5. Kết quả kiểm tra thực tế
- [x] Tách biệt logic: Logic nghiệp vụ được tách biệt hoàn toàn khỏi controller, tập trung ở các service (OrderService, CheckoutService, VoucherService, ...). Controller chỉ nhận request, trả response, không xử lý nghiệp vụ.
- [ ] Unit test, coverage: Chưa phát hiện file unit test cho các service nghiệp vụ (chỉ có test contextLoads). Nên bổ sung test cho các rule quan trọng (đặt vé, thanh toán, voucher, ...), kiểm tra coverage bằng JaCoCo.
- [x] Edge case: Logic xử lý các case đặc biệt (phân quyền, kiểm tra trạng thái, validate quyền truy cập, ...), có throw exception rõ ràng khi vi phạm nghiệp vụ (ResponseStatusException, ResourceNotFoundException).
- [x] Ghi chú khác:
	- Không phát hiện logic trùng lặp giữa các service.
	- Nên bổ sung unit test cho các service chính để đảm bảo chất lượng và dễ refactor.

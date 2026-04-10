# 07. Review Checklist: Performance

## 1. Checklist kiểm tra Performance
- Đánh giá bottleneck (DB, API, memory).
- Kiểm tra caching, connection pool.
- Đảm bảo response time hợp lý.
- Theo dõi memory, CPU khi tải cao.

## 2. Bug phổ biến
- N+1 query, memory leak.
- Không cache, timeout.
- Connection pool cạn kiệt.

## 3. Cách test
- Benchmark API, stress test.
- Phân tích log, profile JVM.

## 4. Công cụ đề xuất
- JMeter, VisualVM, YourKit.
- Spring Boot Actuator.

---

## 5. Kết quả kiểm tra thực tế
- [ ] Bottleneck: Chưa phát hiện bottleneck lớn qua code review. Tuy nhiên, chưa có log hoặc cấu hình profile JVM/SQL để xác định N+1 query hoặc memory leak. Nên bổ sung log slow query và kiểm tra thực tế khi tải cao.
- [ ] Caching, connection pool: Chưa thấy cấu hình cache hoặc pool rõ ràng trong application.properties. Spring Boot mặc định dùng HikariCP, nhưng nên cấu hình pool size, timeout cho production. Chưa có cache (Redis/Ehcache) cho các endpoint truy vấn nhiều.
- [ ] Response time: Chưa có số liệu benchmark thực tế. Codebase không có sleep/blocking bất thường, các truy vấn chính đều dùng repository chuẩn. Nên bổ sung Spring Boot Actuator để theo dõi health/performance.
- [x] Ghi chú khác:
	- Nên benchmark API bằng JMeter/VisualVM khi có dữ liệu lớn.
	- Bổ sung cache cho các API đọc nhiều, cấu hình connection pool rõ ràng.
	- Bật actuator/metrics để theo dõi runtime.

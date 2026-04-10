# 10. Review Checklist: Logging & Monitoring

## 1. Checklist kiểm tra Logging & Monitoring
- Log đủ thông tin, không log dữ liệu nhạy cảm.
- Định dạng log chuẩn, dễ truy vết.
- Thiết lập alert, dashboard monitoring.
- Theo dõi log, phát hiện lỗi kịp thời.

## 2. Bug phổ biến
- Log thiếu, log tràn ổ cứng.
- Không phát hiện lỗi kịp thời.
- Log dữ liệu nhạy cảm.

## 3. Cách test
- Review log thực tế, test alert.
- Kiểm tra dashboard.

## 4. Công cụ đề xuất
- ELK Stack, Grafana, Prometheus.
- Spring Boot Actuator.

---

## 5. Kết quả kiểm tra thực tế
- [x] Log, định dạng: Sử dụng log chuẩn (Slf4j), log đủ thông tin thao tác (email, id, trạng thái), không log dữ liệu nhạy cảm (password, token). Log SQL bật ở mức DEBUG cho dev, có thể chuyển INFO/ERROR khi production.
- [ ] Alert, dashboard: Chưa thấy cấu hình Spring Boot Actuator, ELK/Grafana/Prometheus. Nên bổ sung để theo dõi health, alert lỗi, dashboard realtime.
- [x] Dữ liệu nhạy cảm: Không log thông tin nhạy cảm. Các log chỉ ghi trạng thái, id, email, không ghi nội dung nhạy cảm.
- [x] Ghi chú khác:
	- Nên cấu hình log rolling để tránh tràn ổ cứng.
	- Bổ sung actuator/metrics để dễ monitoring và alert khi production.

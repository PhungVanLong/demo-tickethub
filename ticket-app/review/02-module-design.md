# 02. Review Checklist: Module Design

## 1. Checklist kiểm tra Module Design
- Module tách biệt, rõ ràng chức năng.
- Interface giữa các module rõ ràng, không phụ thuộc lẫn nhau.
- Đảm bảo module không bị trùng lặp logic.
- Đặt tên module, package, class theo chuẩn thống nhất.
- Đảm bảo module dễ mở rộng, maintain.

## 2. Bug phổ biến
- Module phụ thuộc lẫn nhau (tight coupling).
- Logic business bị rải rác nhiều nơi.
- Trùng lặp code giữa các module.
- Đặt tên module/class không nhất quán.

## 3. Cách test
- Code review, kiểm tra sơ đồ module.
- Phân tích dependency giữa các module.
- Kiểm tra các interface, contract giữa module.
- So sánh các module để phát hiện code trùng lặp.

## 4. Công cụ đề xuất
- SonarQube (phân tích code, duplicate code).
- JDepend, ArchUnit (phân tích dependency).
- Structurizr, PlantUML (vẽ sơ đồ module).

---

## 5. Kết quả kiểm tra thực tế
- [x] Phân tách module: Các module (event, order, user, voucher, platform sale, seatmap, checkout, payment, admin...) được tách biệt rõ ràng, mỗi module đảm nhận một chức năng nghiệp vụ riêng biệt, không bị chồng chéo.
- [x] Interface/contract giữa module: Giao tiếp giữa các module thông qua service interface, DTO rõ ràng, không phụ thuộc lẫn nhau. API contract nhất quán, chuẩn REST, các endpoint đều có prefix `/api` (FE/BE đã thống nhất).
- [x] Trùng lặp logic/code: Không phát hiện trùng lặp logic lớn giữa các module. Logic nghiệp vụ tập trung ở Service, không bị rải rác nhiều nơi. Một số logic dùng chung (voucher, platform sale) đã được gom lại thành service riêng.
- [x] Đặt tên, chuẩn hóa: Tên module, package, class, method tuân thủ chuẩn Java, đặt tên rõ nghĩa, nhất quán. Các entity, repository, service, controller đều theo convention Spring Boot.
- [x] Ghi chú khác:
	- Có thể bổ sung test tự động phát hiện duplicate code (SonarQube).
	- Nên duy trì tài liệu sơ đồ module (PlantUML/Structurizr) để dễ onboard và mở rộng.
	- Đã fix các lỗi endpoint thiếu `/api` prefix ở FE (xem API_ENDPOINT_CHECK.md).

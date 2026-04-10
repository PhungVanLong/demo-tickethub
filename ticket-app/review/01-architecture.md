# 01. Review Checklist: Architecture

## 1. Checklist kiểm tra Architecture
- Đánh giá tổng thể kiến trúc Monolith: phân lớp, phân chia module, luồng giao tiếp nội bộ.
- Kiểm tra separation of concerns giữa Controller, Service, Repository, Entity.
- Phân tích dependency giữa các module, phát hiện coupling/circular dependency.
- Đánh giá khả năng mở rộng, maintainability, chuẩn hóa codebase.

## 2. Bug phổ biến
- Coupling giữa các module quá cao, khó tách module.
- Circular dependency gây lỗi runtime hoặc khó maintain.
- Logic business bị trộn lẫn vào controller hoặc repository.

## 3. Cách test
- Review sơ đồ kiến trúc (nếu có), hoặc tự dựng lại bằng PlantUML/Structurizr.
- Phân tích dependency graph bằng tool tự động.
- Đọc codebase, kiểm tra các package chính và luồng giao tiếp.

## 4. Công cụ đề xuất
- Structurizr, PlantUML (vẽ sơ đồ).
- SonarQube, ArchUnit (phân tích code).
- JDepend (phân tích dependency).

---

## 5. Kết quả kiểm tra thực tế
- [x] Tổng quan kiến trúc: Hệ thống sử dụng kiến trúc Monolith, phân lớp rõ ràng (Controller, Service, Repository, Entity), các module tách biệt chức năng (event, order, user, voucher, payment, seatmap...). Luồng giao tiếp nội bộ qua service, không có coupling bất thường.
- [x] Separation of concerns: Controller chỉ nhận request/trả response, Service xử lý nghiệp vụ, Repository thao tác DB, Entity ánh xạ dữ liệu. Không trộn logic business vào controller/repository.
- [x] Dependency giữa module: Các module chỉ inject service/repository cần thiết, không phát hiện circular dependency hoặc coupling cao.
- [x] Maintainability: Codebase chuẩn hóa, dễ mở rộng, sử dụng Lombok giảm boilerplate, đặt tên rõ ràng, có comment cho logic phức tạp.
- [x] Ghi chú khác:
	- Có thể bổ sung test tự động kiểm tra dependency (ArchUnit/JDepend).
	- Nên duy trì tài liệu kiến trúc cập nhật khi mở rộng hệ thống.

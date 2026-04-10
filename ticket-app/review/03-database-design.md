# 03. Review Checklist: Database Design

## 1. Checklist kiểm tra Database Design
- Chuẩn hóa bảng, tránh lặp dữ liệu.
- Đảm bảo khóa chính, khóa ngoại, index hợp lý.
- Kiểm tra migration script, seed data.
- Đảm bảo các constraint (unique, not null, check, ...).
- Đánh giá performance query, index.

## 2. Bug phổ biến
- Thiếu index, query chậm.
- Thiếu ràng buộc dữ liệu (constraint).
- Migration lỗi, mất dữ liệu.
- Trùng lặp dữ liệu giữa các bảng.

## 3. Cách test
- Review ERD, migration script.
- Chạy query benchmark, kiểm tra constraint.
- Kiểm tra seed data, rollback migration.

## 4. Công cụ đề xuất
- DBeaver, DataGrip (quản lý DB, ERD).
- Liquibase/Flyway (migration).
- JMeter (DB test).

---

## 5. Kết quả kiểm tra thực tế
- [x] Chuẩn hóa bảng, constraint: Các bảng được chuẩn hóa, không lặp dữ liệu. Đầy đủ khóa chính, khóa ngoại, unique, not null cho trường quan trọng. Có constraint enum, check logic nghiệp vụ.
- [x] Index, performance: Các trường truy vấn nhiều đều có index (PK/FK/UNIQUE). Thiết kế phù hợp cho truy vấn lớn. Có thể bổ sung index cho các trường filter nhiều nếu cần tối ưu thêm.
- [x] Migration, seed data: Sử dụng Hibernate/JPA tự động tạo bảng (`ddl-auto=update`). Có cấu hình Flyway (comment, có thể bật khi cần migration versioned). Seed data cho admin, voucher config, payment fee... đã có trong application.properties.
- [x] Ghi chú khác:
	- Nên duy trì migration script versioned (bật Flyway/Liquibase khi production).
	- Nên kiểm tra rollback migration và seed data định kỳ.
	- Có thể bổ sung test benchmark query thực tế khi dữ liệu lớn.

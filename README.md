# Ứng dụng quản lý mượn trả sách

Ứng dụng Spring Boot MVC/Thymeleaf cho phép quản lý sách, độc giả, phiếu mượn, trả từng phần, phí phạt và thanh toán. Dữ liệu mẫu được tạo tự động lần đầu chạy bằng Flyway.

## Chạy ứng dụng

1. Cài JDK 21 và MySQL 8, sau đó khởi động MySQL.
2. Mở thư mục dự án trong IntelliJ IDEA. Tạo database `library_management` nếu MySQL không cho tài khoản hiện tại quyền tự tạo database.
3. Nếu MySQL không dùng tài khoản `root` không mật khẩu, đặt các biến môi trường `DB_USERNAME` và `DB_PASSWORD`. Có thể đổi địa chỉ database bằng `DB_URL`.
4. Mở terminal tại thư mục dự án và chạy:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

5. Mở trình duyệt tại `http://localhost:8081`. Cổng 8081 được chọn để tránh xung đột với XAMPP thường dùng cổng 8080.

Ví dụ, nếu tài khoản MySQL là `library_user` với mật khẩu riêng, cấu hình biến môi trường trong terminal trước khi chạy:

```powershell
$env:DB_USERNAME = "library_user"
$env:DB_PASSWORD = "mat-khau-cua-ban"
.\mvnw.cmd spring-boot:run
```

Không đưa mật khẩu thật vào mã nguồn. Mặc định kết nối là `jdbc:mysql://localhost:3306/library_management`, tài khoản `root`, mật khẩu trống.

## Chạy kiểm thử

```powershell
.\mvnw.cmd test
```

Kiểm thử dùng cơ sở dữ liệu H2 riêng, không cần khởi động MySQL.

## Dữ liệu và nghiệp vụ

Lần đầu ứng dụng tự tạo một số sách, thể loại, độc giả và chính sách phạt mẫu. Có thể bổ sung sách/độc giả từ các mục tương ứng trên thanh điều hướng. Phí mặc định là 1.000 đồng/cuốn/ngày, tối đa 50.000 đồng cho một dòng mượn.

Các thao tác mượn, trả, hủy, thanh toán và miễn giảm được xử lý trong transaction. Hệ thống khóa bản ghi sách khi cập nhật tồn kho để tránh hai yêu cầu đồng thời mượn cùng bản cuối; chính sách phí đang áp dụng cũng được thay thế theo transaction.

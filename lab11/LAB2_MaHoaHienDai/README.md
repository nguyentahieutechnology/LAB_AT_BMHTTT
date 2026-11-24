# HƯỚNG DẪN CHẠY DỰ ÁN LAB 2 - MÃ HÓA ĐỐI XỨNG HIỆN ĐẠI
- Học phần: An toàn và Bảo mật Hệ thống Thông tin
- Sinh viên: Nguyễn Tạ Hiếu
- MSSV: 1150080015
- Lớp: 11_THMT

---

## 1. Cấu trúc thư mục
- `LAB2_MaHoaHienDai/`: Maven Project Java Swing (NetBeans)
  - `src/main/java/week_02/`: Chứa 20 lớp Java (DES, TripleDES, AES, RC4, Twofish, AES-CTR, SQL DAO & UI).
  - `sql/`: Các script SQL khởi tạo CSDL `LAB2_MaHoaHienDai` và tài khoản `lab2_user`.
  - `pom.xml`: Khai báo thư viện mssql-jdbc và BouncyCastle.
- `LAB2_Python/`:
  - `aes_cbc_gui.py`: Bài nâng cao 02 - Mã hóa AES-256-CBC bằng Python Tkinter.

---

## 2. Cách chạy ứng dụng Java
1. Mở NetBeans -> **Open Project** -> Chọn thư mục `LAB2_MaHoaHienDai`.
2. Bấm phím **F6** (hoặc chuột phải vào project chọn **Run**).
3. Cửa sổ **frm_Menu** sẽ xuất hiện, cho phép mở tất cả các form thực hành:
   - Bài 2.1: DES
   - Bài 2.2: TripleDES
   - Bài 2.3: AES + Login
   - Bài 2.4: RC4
   - Bài 2.5: Twofish
   - Bài 2.6: Nâng cao - AES-CTR
   - Tiện ích: Kết nối SQL Server & Xem lịch sử mã hóa

---

## 3. Cách chạy ứng dụng Python
Mở Command Prompt hoặc PowerShell tại thư mục `LAB2_Python`:
- Chạy giao diện Tkinter:
  ```bash
  py aes_cbc_gui.py
  ```
- Chạy kiểm thử tự động (Self-test):
  ```bash
  py aes_cbc_gui.py --self-test
  ```

---

## 4. Thiết lập Cơ sở dữ liệu SQL Server
1. Mở SSMS, kết nối máy chủ SQL Server bằng Windows Authentication.
2. Chạy lần lượt 2 script trong thư mục `sql/`:
   - `01_Tao_CSDL.sql`: Khởi tạo database và 2 bảng `app_users`, `crypto_history`.
   - `02_Tao_Tai_Khoan_SQL.sql`: Tạo login `lab2_user` mật khẩu `LabUser@2026`.
3. Khởi động lại dịch vụ SQL Server (MSSQLSERVER) để cổng 14333 và Mixed Mode có hiệu lực.
4. Mở form **Kết nối SQL Server** trên ứng dụng, bấm **Test Connection**.

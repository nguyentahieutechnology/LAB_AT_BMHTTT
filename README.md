# BÁO CÁO THỰC HÀNH LAB 1: BẮT GÓI TIN TELNET - SSH
**Môn học:** An toàn Hệ thống thông tin  
**Khoa:** Mạng máy tính & Truyền thông - Trường ĐH Công nghệ Thông tin (UIT - ĐHQG-HCM)

---

## 👨‍🎓 THÔNG TIN SINH VIÊN
* **Họ và tên:** Nguyễn Tạ Hiếu
* **Mã số sinh viên (MSSV): **1150080015**
* **Lớp**:**11_THMT**
* **Link Video YouTube Demo:**https://youtu.be/uQoAgbQjmBY **

---

## 🎯 1. NỘI DUNG ĐÃ THỰC HIỆN
Trong bài Lab 1, sinh viên đã xây dựng mô hình mạng giả lập và thực hiện các nội dung sau:
1. **Thiết lập mô hình mạng & Dịch vụ máy chủ:**
   * Dựng máy chủ trên máy ảo **Kali Linux** (IP: `192.168.19.134`), cài đặt và mở dịch vụ **Telnet Server (TCP/23)** và **SSH Server (TCP/22)**.
   * Cấu hình máy Client & Attacker trên máy thật **Windows 11** (IP: `192.168.19.1`) kết nối qua card mạng ảo `VMware Network Adapter VMnet8`.
2. **Thực nghiệm bắt gói tin Telnet:**
   * Sử dụng **PuTTY** kết nối Telnet đến máy chủ qua cổng 23, đăng nhập tài khoản và thực thi các câu lệnh cơ bản (`whoami`, `ls`, `mkdir`, `pwd`).
   * Sử dụng **Wireshark** bắt gói tin trên card mạng `VMnet8`, lọc `tcp.port == 23` và phân tích luồng TCP (Follow TCP Stream).
3. **Thực nghiệm bắt gói tin SSH:**
   * Sử dụng **PuTTY** kết nối SSH đến máy chủ qua cổng 22, xác nhận *Host-Key Fingerprint*, đăng nhập và thực thi câu lệnh.
   * Sử dụng **Wireshark** lọc `tcp.port == 22`, phân tích luồng TCP để so sánh với Telnet.
4. **Mở rộng & Demo:**
   * Cấu hình và thực nghiệm thành công phương thức xác thực bằng cặp khóa **SSH Public-Key Authentication** từ Windows sang Linux, cho phép đăng nhập an toàn mà không cần nhập mật khẩu.
5. **Hoàn thành trả lời 11 câu hỏi lý thuyết và thực nghiệm** trong đề cương bài Lab.

---

## 📊 2. KẾT QUẢ THỰC HIỆN
* **Đối với Telnet:**
  * Toàn bộ phiên làm việc gồm: Tài khoản đăng nhập (Username), Mật khẩu (Password) và nội dung các lệnh điều khiển đều được truyền dưới dạng văn bản thô (**Plaintext**).
  * Kẻ tấn công (Attacker) trên cùng mạng dễ dàng bắt gói và đọc được 100% dữ liệu mà không cần bẻ khóa.
* **Đối với SSH:**
  * Dữ liệu payload của phiên làm việc đều được mã hóa đối xứng mạnh (**Encrypted Payload**).
  * Wireshark chỉ nhìn thấy các gói tin TCP và metadata (IP, Port 22, phiên bản OpenSSH, kích thước gói), toàn bộ dữ liệu người dùng được bảo vệ tuyệt đối về tính bí mật (Confidentiality) và tính toàn vẹn (Integrity).
* **Đối với SSH Public Key Authentication:**
  * Đăng nhập thành công từ Windows Command Prompt vào máy ảo Kali Linux chỉ bằng cặp khóa RSA/Ed25519 mà không cần truyền mật khẩu qua mạng, ngăn chặn hoàn toàn nguy cơ tấn công Brute-force/Dictionary.

---

## ⚙️ 3. HƯỚNG DẪN KIỂM TRA & CHẠY LẠI BÀI LÀM (Dành cho GV / Reviewer)

### Yêu cầu môi trường:
* VMware Workstation Pro / Player.
* Máy ảo Kali Linux (Network Adapter: **NAT / VMnet8**).
* Phần mềm trên Host Windows: PuTTY 0.85, Wireshark 4.6.x, OpenSSH Client (CMD/PowerShell).

### Các bước tái hiện:
1. **Khởi động dịch vụ trên Kali Linux:**
   ```bash
   sudo systemctl restart inetutils-inetd
   sudo systemctl restart ssh
   ss -ltn | grep -E ':22|:23'
   ```
2. **Kiểm tra kết nối Telnet từ Windows:**
   * Mở PuTTY $\rightarrow$ Host: `<IP_Kali>`, Connection type: **Telnet** (Port 23) $\rightarrow$ Đăng nhập tài khoản kiểm tra.
3. **Kiểm tra kết nối SSH từ Windows:**
   * Mở PuTTY $\rightarrow$ Host: `<IP_Kali>`, Connection type: **SSH** (Port 22) $\rightarrow$ Đăng nhập kiểm tra.
   * Hoặc dùng CMD kiểm tra SSH Key login: `ssh <username>@<IP_Kali>`.
4. **Bắt gói tin với Wireshark:**
   * Chọn card `VMware Network Adapter VMnet8`.
   * Lọc `tcp.port == 23` để xem Telnet (Follow TCP Stream).
   * Lọc `tcp.port == 22` để xem SSH (Follow TCP Stream).

---
*Báo cáo chi tiết và toàn bộ câu trả lời kèm hình ảnh minh chứng được lưu tại file Word đính kèm trong thư mục này.*

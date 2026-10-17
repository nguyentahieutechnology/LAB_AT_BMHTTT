# BÁO CÁO THỰC HÀNH BÀI SỐ 6
## XÁC THỰC ACTIVE DIRECTORY VÀ CHÍNH SÁCH TRUY CẬP TRÊN PFSENSE

* **Học phần:** Thực hành An toàn & Bảo mật Hệ thống Thông tin
* **Họ và tên sinh viên:** Nguyễn Tạ Hiếu
* **Mã số sinh viên (MSSV):** 1150080015
* **Lớp:** 11_THMT
* **Tài liệu chuẩn:** LAB6_pfSense_AD_Access_Policies_2026.pdf
* **Tệp cấu hình nộp kèm:** `config.xml` (sao lưu toàn bộ cấu hình pfSense)

---

## I. THÔNG TIN SINH VIÊN VÀ MÔI TRƯỜNG THỰC HÀNH

### 1. Bảng thông số môi trường chuẩn hóa

| Thành phần mạng | Hệ điều hành / Cấu hình IP | Vai trò và Dịch vụ triển khai |
| :--- | :--- | :--- |
| **Firewall (pfSense-LAB6)** | FreeBSD / pfSense CE 2.7.2 (4 vNIC)<br>• **WAN:** `192.168.250.3/24`<br>• **MGMT:** `192.168.56.254/24`<br>• **LAN:** `10.6.0.1/24`<br>• **DMZ:** `172.16.6.1/24` | Router biên, Stateful Firewall, LDAP Client, Captive Portal Zone, Aliases & Schedules. |
| **Domain Controller (DC01)** | Windows Server 2022 Standard<br>• **IP:** `10.6.0.10/24` (GW: `10.6.0.1`)<br>• **DNS:** `10.6.0.10` | Máy chủ miền Active Directory `lab6.test`, OU `LAB6-Users`, tài khoản `sv_a`, `sv_b`, `svc_pfsense_ldap`. |
| **Máy chủ Web (DMZ-WEB)** | Ubuntu Server 22.04 LTS (Nginx)<br>• `172.16.6.10` (GENERAL_SITE)<br>• `172.16.6.20` (JOB_SITE)<br>• `172.16.6.30` (SOCIAL_SITE)<br>• `172.16.6.40` (RESTRICTED_SITE) | Mô phỏng 4 nhóm website phân loại nội dung trong vùng DMZ, phục vụ kiểm thử Access Policies. |
| **Máy trạm kiểm thử (Client)** | Kali Linux 2026<br>• **Client-A:** `10.6.0.21/24`<br>• **Client-B:** `10.6.0.22/24`<br>• **GW:** `10.6.0.1` | **DNS:** `10.6.0.10` | Đóng vai người dùng máy trạm LAN kiểm thử Captive Portal, duyệt web và kiểm tra các firewall rules. |

### 2. Minh chứng khởi tạo hệ thống

* **Hình 1 (H1):** pfSense console phân gán 4 card mạng (WAN, LAN, OPT1/MGMT, OPT2/DMZ).  
  ![Hình 1 (H1)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-13%20145939.png)

* **Hình 2 (H2):** Cấu hình đủ 4 địa chỉ IP phụ (Secondary IPs) mô phỏng các phân nhóm web trên máy chủ DMZ-WEB.  
  ![Hình 2 (H2)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-18%20015933.png)

---

## II. CẤU HÌNH HỆ THỐNG VÀ DỊCH VỤ XÁC THỰC (MỤC B, C, D, E, F)

### 1. Quy trình triển khai kỹ thuật
* **Thiết lập hệ thống pfSense ban đầu:** Hoàn tất trình hướng dẫn Setup Wizard, thiết lập hostname `pfSense-LAB6.lab6.test`, trỏ DNS Server về Domain Controller `10.6.0.10` và quản trị độc lập qua giao diện MGMT `192.168.56.254`.
* **Dựng máy chủ miền Active Directory (DC01):** Nâng cấp Domain Controller với tên miền `lab6.test`. Tạo đơn vị tổ chức `OU=LAB6-Users` chứa người dùng `sv_a`, `sv_b` và tài khoản dịch vụ `svc_pfsense_ldap`. Cấu hình DNS Server phân giải tên miền.
* **Tích hợp máy chủ xác thực LDAP trên pfSense:** Tại `System > User Manager > Authentication Servers`, tạo kết nối `LAB6-AD` trỏ tới `10.6.0.10:389`, Base DN `DC=lab6,DC=test`, Bind DN `CN=svc_pfsense_ldap,OU=LAB6-Users,DC=lab6,DC=test`. Kiểm thử `Diagnostics > Authentication` đạt 100%.
* **Triển khai Captive Portal Zone trên LAN:** Tại `Services > Captive Portal`, khởi tạo zone `LAN_PORTAL` trên interface LAN, phương thức xác thực Authentication Backend liên kết máy chủ LDAP `LAB6-AD`.
* **Quy hoạch danh mục Aliases và Schedules:** Tạo 8 đối tượng định danh Aliases (`CLIENT_A`, `CLIENT_B`, `POLICY_CLIENTS`, 4 Sites DMZ, `WEB_PORTS`) và 3 khung thời gian Schedules (`LUNCH_WINDOW`, `TEST_NOW`, `WORK_HOURS`) phục vụ thiết lập chính sách truy cập theo thời gian.

### 2. Hình ảnh minh chứng cấu hình xác thực và định danh

* **Hình 3 (H3):** Hoàn tất trình hướng dẫn Setup Wizard cấu hình hệ thống pfSense ban đầu.  
  ![Hình 3 (H3)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-13%20154018.png)

* **Hình 4 (H4):** Giao diện Dashboard tổng quan hệ thống pfSense, trạng thái các interfaces và DNS Server `10.6.0.10`.  
  ![Hình 4 (H4)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-13%20154407.png)

* **Hình 5 (H5):** Kết quả kiểm tra xác thực người dùng AD `sv_a` thành công tại `Diagnostics > Authentication`.  
  ![Hình 5 (H5)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-15%20023648.png)

* **Hình 6 (H6):** Giao diện cổng đăng nhập Captive Portal hiển thị khi máy trạm Client truy cập mạng.  
  ![Hình 6 (H6)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-17%20142748.png)

* **Hình 7 (H7):** Máy trạm Client tải thành công trang web sau khi xác thực thành công qua Captive Portal.  
  ![Hình 7 (H7)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-17%20143631.png)

* **Hình 8 (H8):** Danh sách phiên người dùng đăng nhập Captive Portal đang hoạt động tại `Status > Captive Portal`.  
  ![Hình 8 (H8)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-17%20143746.png)

* **Hình 9 (H9):** Bảng tổng hợp các đối tượng Aliases được cấu hình tại `Firewall > Aliases`.  
  ![Hình 9 (H9)](file:///D:/LAB6_VMS/screenshots/Firewall_Aliases_Configured.png)

* **Hình 10 (H10):** Danh mục 3 Schedules quản lý khung giờ truy cập tại `Firewall > Schedules`.  
  ![Hình 10 (H10)](file:///D:/LAB6_VMS/screenshots/Firewall_Schedules_Configured.png)

---

## III. KẾT QUẢ THỰC HIỆN CÁC KỊCH BẢN CHÍNH SÁCH (G.1 – G.7)

### 1. Kịch bản 1 (KB1) - Client-A bị chặn truy cập JOB_SITE
* **Kết quả:** PASS (Client-A bị chặn vào `172.16.6.20`, vào được `172.16.6.10`; Firewall Log ghi nhận gói tin bị chặn).
* **Cấu hình Rule:** Tab LAN: `Action = Block` | `Source = CLIENT_A` | `Destination = JOB_SITE` | `Port = 80 (HTTP)` | Bật Logging.
* **Thực nghiệm:** Client-A (`10.6.0.21`) truy cập `http://172.16.6.10` tải trang thành công 'DMZ WEB OK'; truy cập `http://172.16.6.20` trình duyệt báo *The connection has timed out*. Nhật ký Firewall Log ghi nhận dòng chặn màu đỏ từ `10.6.0.21` tới `172.16.6.20:80`.

* **Hình 11 (H11):** Trình duyệt Client-A bị chặn kết nối khi truy cập vào website tìm việc JOB_SITE (`172.16.6.20`).  
  ![Hình 11 (H11)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-17%20155953.png)

* **Hình 12 (H12):** Trình duyệt Client-A vẫn truy cập thành công website thông thường GENERAL_SITE (`172.16.6.10`).  
  ![Hình 12 (H12)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-17%20162044.png)

* **Hình 13 (H13):** Nhật ký Firewall Log ghi nhận quy tắc chặn luồng TCP 80 từ Client-A tới JOB_SITE.  
  ![Hình 13 (H13)](file:///D:/LAB6_VMS/screenshots/KB1_Firewall_Log_JobSite_Blocked.png)

---

### 2. Kịch bản 2 (KB2) - Cấm nhóm RESTRICTED_SITE cho cả Client-A và Client-B
* **Kết quả:** PASS (Cả Client-A và Client-B đều bị chặn truy cập `172.16.6.40`, nhưng vẫn vào được `172.16.6.10`).
* **Cấu hình Rule:** Tab LAN: `Action = Block` | `Source = POLICY_CLIENTS` | `Destination = RESTRICTED_SITE` | `Port = 80 (HTTP)` | Bật Logging.
* **Thực nghiệm:** Mở trình duyệt lần lượt trên Client-A và Client-B truy cập `http://172.16.6.40` đều bị xoay vòng và báo *Connection timed out*. Kiểm tra Firewall Log thấy đầy đủ các bản ghi chặn tương ứng với cả hai địa chỉ IP nguồn `10.6.0.21` và `10.6.0.22`.

* **Hình 14 (H14):** Trình duyệt Client bị chặn kết nối khi truy cập nhóm trang cấm RESTRICTED_SITE (`172.16.6.40`).  
  ![Hình 14 (H14)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-17%20160149.png)

* **Hình 15 (H15):** Nhật ký Firewall Log ghi nhận quy tắc KB2 chặn luồng từ LAN tới RESTRICTED_SITE:80.  
  ![Hình 15 (H15)](file:///D:/LAB6_VMS/screenshots/KB2_Firewall_Log_RestrictedSite_Blocked.png)

---

### 3. Kịch bản 3 (KB3) - Client-B chỉ được truy cập SOCIAL_SITE trong giờ nghỉ
* **Kết quả:** PASS (Trong khung giờ cho phép truy cập thành công; ngoài khung giờ bị rule Block phía dưới chặn ngay lập tức).
* **Cấu hình Rule:** Tab LAN cấu hình cặp quy tắc:
  1. `Pass` | `CLIENT_B -> SOCIAL_SITE:80` | `Schedule = LUNCH_WINDOW`
  2. `Block` | `CLIENT_B -> SOCIAL_SITE:80` (Explicit Deny ngoài giờ)
* **Thực nghiệm:** Gắn Schedule `TEST_NOW` bao trùm giờ hiện tại -> Client-B truy cập `http://172.16.6.30` tải thành công trang 'DMZ WEB OK'. Tắt rule Pass (hoặc ngoài giờ) -> Client-B lập tức bị chặn với thông báo *Connection timed out*.

* **Hình 16 (H16):** Client-B truy cập thành công mạng xã hội SOCIAL_SITE khi Schedule đang Active trong giờ nghỉ.  
  ![Hình 16 (H16)](file:///D:/LAB6_VMS/screenshots/KB3_ClientB_Success_SocialSite_Browser.png)

* **Hình 17 (H17):** Client-B bị chặn kết nối tới SOCIAL_SITE khi ngoài khung giờ cho phép.  
  ![Hình 17 (H17)](file:///D:/LAB6_VMS/screenshots/KB3_ClientB_Blocked_SocialSite_Browser.png)

---

### 4. Kịch bản 4 (KB4) - Chỉ cho phép HTTP/HTTPS trong giờ hành chính & Chặn dịch vụ khác
* **Kết quả:** PASS (Trong giờ duyệt web port 80/443 bình thường; mọi giao thức khác như ICMP ping bị chặn bởi rule Explicit Deny cuối cùng).
* **Cấu hình Rule:** Tab LAN:
  1. `Pass` | `POLICY_CLIENTS -> Any:WEB_PORTS` | `Schedule = WORK_HOURS`
  2. `Block` | `POLICY_CLIENTS -> Any:*` | Bật Logging.
* **Thực nghiệm:** Trong giờ, Client chạy `curl -I http://172.16.6.10` nhận được `HTTP/1.1 200 OK`. Lệnh `ping 8.8.8.8` bị 100% packet loss. Firewall Log ghi nhận dòng đỏ chặn giao thức ICMP từ `10.6.0.22` tới `8.8.8.8` bởi rule Explicit Deny.

* **Hình 18 (H18):** Kiểm thử KB4 trên Terminal Client: truy cập Web 80 thành công (HTTP 200 OK) nhưng ping 8.8.8.8 bị chặn 100% packet loss.  
  ![Hình 18 (H18)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-18%20013443.png)

* **Hình 19 (H19):** Nhật ký Firewall Log ghi nhận quy tắc Explicit Deny chặn gói tin ICMP ping 8.8.8.8 từ máy Client-B (`10.6.0.22`).  
  ![Hình 19 (H19)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-18%20013935.png)

---

### 5. Kịch bản 5 (KB5) - Cô lập DMZ khỏi LAN nhưng cho LAN truy cập DMZ
* **Kết quả:** PASS (LAN khởi tạo kết nối tới DMZ thành công; DMZ chủ động kết nối vào mạng nội bộ LAN bị chặn hoàn toàn).
* **Cấu hình Rule:** Tab OPT2 (DMZ): `Action = Block` | `Interface = OPT2` | `Protocol = Any` | `Source = OPT2 subnets` | `Destination = LAN subnets` | Bật Logging.
* **Thực nghiệm:** Từ Kali (LAN) truy cập web DMZ `http://172.16.6.10` thành công (`HTTP 200 OK`) nhờ cơ chế Stateful Inspection. Ngược lại, đứng từ máy chủ DMZ-WEB chạy lệnh `ping 10.6.0.10` (DC01) bị treo và báo 100% packet loss.

* **Hình 20 (H20):** Cấu hình quy tắc Block cô lập DMZ net tới LAN net trên giao diện OPT2 của pfSense.  
  ![Hình 20 (H20)](file:///C:/Users/Admin/Pictures/lab6/Screenshot%202026-10-18%20020012.png)

* **Hình 21 (H21):** Kết quả kiểm thử lệnh ping từ DMZ-WEB tới máy chủ DC01 (`10.6.0.10`) bị chặn 100% packet loss.  
  ![Hình 21 (H21)](file:///D:/LAB6_VMS/screenshots/KB5_DMZ_Ping_DC01_Blocked.png)

---

### 6. Kịch bản 6 (KB6) - So sánh cơ chế phản hồi mạng giữa Block và Reject
* **Kết quả:** PASS (Đo đạc chính xác thời gian và thông báo lỗi: Block gây timeout 5.054s; Reject trả cờ TCP RST báo lỗi tức thì 0.010s).
* **Thực nghiệm:** Cùng kiểm thử kết nối từ Client tới `172.16.6.40:80` qua lệnh `time curl -m 5`:
  * Khi dùng **Block**: Client chờ hết 5 giây timeout mới báo lỗi *Connection timed out* (`real: 5.054s`).
  * Khi dùng **Reject**: pfSense phản hồi cờ `TCP RST` ngay lập tức, Client nhận thông báo *Connection refused* chỉ sau `0.010` giây.

* **Hình 22 (H22):** Màn hình so sánh trực quan thời gian phản hồi và thông báo lỗi giữa Action Block (5.054s timeout) và Reject (0.010s refused).  
  ![Hình 22 (H22)](file:///D:/LAB6_VMS/screenshots/KB6_Compare_Block_Reject.png)

---

### 7. Kịch bản 7 (KB7) - Đọc Firewall Log và Bảng quản lý trạng thái State Table
* **Kết quả:** PASS (Sử dụng bộ lọc Advanced Filter tra cứu log chính xác theo IP nguồn; kiểm soát và làm sạch bảng trạng thái với Reset States).
* **Thực nghiệm:** Vào `Status > System Logs > Firewall` lọc theo IP `10.6.0.22` đối chiếu interface, action, protocol, rule tracker ID. Vào `Diagnostics > States` theo dõi các luồng kết nối đang mở; thực hiện Reset States để áp dụng chính sách mới.

* **Hình 23 (H23):** Bảng theo dõi trạng thái các phiên kết nối đang hoạt động tại `Diagnostics > States`.  
  ![Hình 23 (H23)](file:///D:/LAB6_VMS/screenshots/KB7_Firewall_States_Table.png)

---

## IV. BẢNG QUY TẮC RULESET TỔNG HỢP VÀ KẾT QUẢ KIỂM THỬ BẮT BUỘC (MỤC H)

### 1. Bảng quy tắc Ruleset tổng hợp trên LAN
Quy tắc lọc gói tin trên tab LAN được cấu hình theo thứ tự ưu tiên chặt chẽ từ trên xuống dưới (First-match):

| Ưu tiên | Action | Source | Destination | Port | Schedule | Mục đích bảo mật |
| :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **1** | Block | CLIENT_A | JOB_SITE | 80 (HTTP) | none | Chặn Client-A vào website tìm việc |
| **2** | Block | POLICY_CLIENTS | RESTRICTED_SITE | 80 (HTTP) | none | Cấm cả A và B vào website cấm |
| **3** | Pass | CLIENT_B | SOCIAL_SITE | 80 (HTTP) | LUNCH_WINDOW | Cho phép Client-B vào Social trong giờ nghỉ trưa |
| **4** | Block | CLIENT_B | SOCIAL_SITE | 80 (HTTP) | none | Chặn Client-B vào Social ngoài giờ nghỉ trưa |
| **5** | Pass | POLICY_CLIENTS | GENERAL_SITE | 80 (HTTP) | none | Cho phép cả A và B vào website thông thường |
| **6** | Pass | POLICY_CLIENTS | Any | WEB_PORTS | WORK_HOURS | Cho phép duyệt Web Internet trong giờ hành chính |
| **7** | Block | POLICY_CLIENTS | Any | Any | none | Explicit Deny: Chặn toàn bộ dịch vụ khác (bật Log) |

* **Hình 24 (H24):** Toàn cảnh danh sách 7 Firewall Rules sắp xếp theo đúng thứ tự first-match trên tab LAN của pfSense.  
  ![Hình 24 (H24)](file:///D:/LAB6_VMS/screenshots/LAN_Rules_Configured.png)

### 2. Nội dung kiểm thử bắt buộc theo ma trận

| Nguồn (Client) | Đích (Destination) | Trong Schedule | Ngoài Schedule | Kết quả thực tế kiểm chứng |
| :--- | :--- | :---: | :---: | :--- |
| **Client-A (10.6.0.21)** | GENERAL_SITE (.10) | Allow | Allow | Truy cập thành công (`HTTP 200 OK`) |
| **Client-A (10.6.0.21)** | JOB_SITE (.20) | Block | Block | Bị chặn *Connection timed out* (Rule 1) |
| **Client-A (10.6.0.21)** | RESTRICTED_SITE (.40) | Block | Block | Bị chặn *Connection timed out* (Rule 2) |
| **Client-B (10.6.0.22)** | SOCIAL_SITE (.30) | Allow (LUNCH) | Block | Trong giờ vào được; ngoài giờ bị chặn Timeout |
| **Client-B (10.6.0.22)** | RESTRICTED_SITE (.40) | Block | Block | Bị chặn *Connection timed out* (Rule 2) |
| **DMZ-WEB (172.16.6.10)** | DC01 (10.6.0.10) | Block | Block | Bị chặn 100% packet loss (Rule OPT2 Block) |

---

## V. BẢNG TỔNG HỢP KẾT QUẢ VÀ TRẢ LỜI 12 CÂU HỎI PHÂN TÍCH (MỤC J)

### 1. Bảng tổng hợp kết quả và minh chứng thực nghiệm

| Kịch bản kiểm thử | Dự đoán | Thực tế | Ảnh rule | Ảnh test | Nhận xét kỹ thuật |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **LDAP sv_a / sv_b** | PASS | PASS | Có | Có | Xác thực tài khoản người dùng miền AD thành công 100%. |
| **Captive Portal** | PASS | PASS | Có | Có | Chặn máy chưa xác thực, cấp quyền Internet sau khi login user AD. |
| **1. JOB_SITE** | Chặn Client-A | Bị chặn | Có | Có | First-match khớp ngay Rule 1 Block, ghi log chính xác. |
| **2. RESTRICTED_SITE** | Chặn cả A và B | Bị chặn | Có | Có | Khớp Rule 2 POLICY_CLIENTS, cấm triệt để IP .40. |
| **3. Social Schedule** | Trong giờ Pass, ngoài giờ Block | Đúng dự đoán | Có | Có | Khi hết giờ, rule Pass tự ẩn; rule Block phía dưới chặn. |
| **4. Work hours** | Chỉ Web, cấm ping | Đúng dự đoán | Có | Có | Rule Explicit Deny bắt toàn bộ ICMP và cổng ngoài 80/443. |
| **5. DMZ isolation** | DMZ không vào LAN | Đúng dự đoán | Có | Có | Ping DC01 bị 100% packet loss; LAN vào DMZ vẫn OK do Stateful. |
| **6–7. Reject/Log/State** | Reject nhanh hơn Block | Đúng dự đoán | Có | Có | Reject trả TCP RST mất 0.010s; Block làm client chờ timeout 5.054s. |

### 2. Trả lời chi tiết 12 câu hỏi phân tích chuyên sâu

#### Câu 1: Vì sao LAB 6 phải dùng topology riêng thay vì tiếp tục sử dụng trực tiếp topology LAB 5?
* **Phân tách môi trường sạch (Clean Environment):** LAB 6 yêu cầu một cấu trúc mạng doanh nghiệp hoàn chỉnh gồm 4 vùng mạng độc lập (WAN, MGMT, LAN, DMZ) nhằm kiểm thử mô hình kiểm soát truy cập và bảo vệ máy chủ DMZ.
* **Tránh xung đột cấu hình:** Topology LAB 5 có các cấu hình NAT/Virtual IP và dịch vụ thử nghiệm khác, có thể gây xung đột bảng định tuyến và bảng trạng thái kết nối.
* **Đáp ứng yêu cầu chuẩn hóa:** Tách riêng card mạng MGMT (Host-Only `192.168.56.0/24`) và LAN (Internal `lab6-lan` `10.6.0.0/24`) giúp Captive Portal trên LAN không cản trở việc quản trị tường lửa qua WebGUI.

#### Câu 2: LDAP được dùng cho chức năng nào trên pfSense? Firewall rule chuẩn có thể dùng username AD làm Source trực tiếp không?
* **Chức năng của LDAP trên pfSense:** LDAP được sử dụng làm máy chủ xác thực danh tính tập trung (Authentication Server) cho các dịch vụ truy cập như Captive Portal, VPN, và đăng nhập quản trị WebGUI.
* **Firewall rule chuẩn có dùng username AD trực tiếp không:** **KHÔNG**. Bộ lọc gói tin pf (Packet Filter) của pfSense hoạt động ở Tầng Mạng (L3) và Giao vận (L4), chỉ so khớp các trường tiêu đề IP nguồn/đích, giao thức và cổng. Khi người dùng xác thực thành công qua Captive Portal, pfSense gán IP của máy trạm đó vào danh sách được phép đi qua tường lửa (IP-based Binding).

#### Câu 3: Vì sao Captive Portal được đặt trên LAN nhưng WebGUI quản trị lại tách sang MGMT?
* **Nguyên tắc an ninh Out-of-Band Management:** Tách biệt mặt phẳng điều khiển/quản trị (Management Plane) và mặt phẳng dữ liệu người dùng (Data Plane).
* **Ngăn ngừa tình trạng khóa quản trị (Anti-Lockout):** Nếu WebGUI đặt chung trên LAN bị Captive Portal kiểm soát, khi dịch vụ xác thực AD gặp lỗi hoặc Captive Portal bị treo, người quản trị cũng sẽ bị chặn và mất quyền truy cập cấu hình tường lửa.

#### Câu 4: Search Scope Entire Subtree có ý nghĩa gì khi pfSense truy vấn Active Directory?
* **Ý nghĩa kỹ thuật:** Search Scope xác định phạm vi độ sâu tìm kiếm trong cấu trúc cây thư mục AD DS. Toàn bộ cây con (Entire Subtree) chỉ đạo truy vấn từ Base DN chỉ định (ví dụ `DC=lab6,DC=test`) xuống toàn bộ các OU, Container và nhánh con bên dưới.
* **Lợi ích thực tế:** Cho phép người dùng nằm rải rác ở các OU khác nhau (OU=KinhDoanh, OU=KyThuat) đều được tìm thấy và xác thực thành công mà không phải giới hạn ở một cấp duy nhất.

#### Câu 5: Tại sao nên dùng service account thay vì Domain Administrator làm LDAP bind?
* **Nguyên tắc đặc quyền tối thiểu (Principle of Least Privilege - PoLP):** Quá trình LDAP Bind của pfSense chỉ cần quyền đọc (Read-only) các thuộc tính người dùng và nhóm trong AD để xác thực, không cần quyền thay đổi cấu hình hay can thiệp hệ thống.
* **Giảm thiểu rủi ro an ninh:** Nếu lưu mật khẩu Domain Administrator dưới dạng bản rõ trong cấu hình pfSense (hoặc file backup xml), kẻ tấn công chiếm được firewall sẽ sở hữu quyền kiểm soát toàn bộ hạ tầng miền của doanh nghiệp.

#### Câu 6: Trong Kịch bản 3, vì sao cần một rule Block SOCIAL_SITE nằm dưới rule Pass có Schedule?
* **Cơ chế hoạt động của Scheduled Rule trên pfSense:** Khi khung thời gian của Schedule không hoạt động (Inactive), rule Pass gắn schedule đó sẽ tự động bị ẩn (coi như không tồn tại) chứ **KHÔNG** tự động đảo ngược hành vi thành chặn.
* **Tránh lọt gói tin:** Nếu không có rule Block nằm ngay bên dưới, khi rule Pass biến mất, lưu lượng truy cập mạng xã hội sẽ tiếp tục trôi xuống các rule cho phép rộng hơn phía dưới (như rule cho phép duyệt Web giờ hành chính) và người dùng vẫn vào được mạng xã hội ngoài giờ.

#### Câu 7: Vì sao Client-A/B dùng DNS 10.6.0.10 nhưng rule Internet của client không cần cho DNS tới 10.6.0.10?
* **Bản chất giao tiếp mạng cục bộ cùng Subnet (Layer 2 Switching):** Client-A (`10.6.0.21`), Client-B (`10.6.0.22`) và máy chủ DNS DC01 (`10.6.0.10`) đều nằm chung trong dải mạng `10.6.0.0/24`.
* **Không đi qua Gateway:** Giao tiếp giữa các máy cùng subnet được chuyển tiếp trực tiếp ở Tầng Liên kết Dữ liệu (Layer 2) qua địa chỉ MAC bằng bảng ARP, gói tin hoàn toàn không đi qua Default Gateway (pfSense `10.6.0.1`) nên không bị Firewall Rules trên pfSense kiểm soát.

#### Câu 8: Giải thích first-match bằng một ví dụ cụ thể trong ruleset tổng hợp.
* **Khái niệm First-Match:** Bộ lọc pfSense duyệt danh sách firewall rules từ trên xuống dưới; ngay khi một gói tin khớp với điều kiện của rule đầu tiên, hành động (Pass hoặc Block) của rule đó sẽ được thực thi ngay lập tức và toàn bộ các rule phía dưới bị bỏ qua.
* **Ví dụ cụ thể trong bài LAB:** Rule 1 Block Client-A truy cập JOB_SITE (.20) nằm ở trên. Rule 6 cho phép duyệt Web cổng 80/443 nằm ở dưới. Khi Client-A gửi gói tin HTTP tới `172.16.6.20`, gói tin khớp ngay Rule 1 và bị Block lập tức. Nếu đảo ngược vị trí đặt Rule 6 lên trên Rule 1, Client-A sẽ vào được JOB_SITE vì khớp Rule 6 Pass trước.

#### Câu 9: Vì sao sau khi đổi rule cần tạo kết nối mới hoặc Reset States?
* **Cơ chế Stateful Firewall:** pfSense quản lý lưu lượng dựa trên bảng trạng thái (State Table). Khi gói tin đầu tiên (TCP SYN) được rule Pass cho phép, một trạng thái được ghi vào RAM. Các gói tin tiếp theo của cùng phiên sẽ được tự động cho qua dựa trên State Table mà không so khớp lại ruleset.
* **Tránh lưu phiên cũ:** Khi chuyển một rule từ Pass sang Block, nếu phiên kết nối cũ vẫn còn state hợp lệ trong RAM, lưu lượng vẫn tiếp tục đi qua. Thao tác Reset States sẽ xóa sạch các phiên kết nối cũ, buộc mọi gói tin mới phải so khớp lại từ đầu với ruleset mới.

#### Câu 10: Mô hình JOB_SITE/SOCIAL_SITE/RESTRICTED_SITE trong DMZ khác Web Category Filtering thật như thế nào?
* **Mô hình mô phỏng trong LAB 6:** Hoạt động thuần túy ở Tầng Mạng và Giao vận (L3/L4) bằng cách ánh xạ danh mục sang các địa chỉ IP và Port cố định (`172.16.6.20:80`, `.30:80`). Không giải mã nội dung, không kiểm tra tên miền hay URL.
* **Web Category Filtering thực tế (Next-Gen Firewall / Sophos / UTM):** Hoạt động ở Tầng Ứng dụng (L7). Tường lửa kiểm tra trường SNI trong bắt tay TLS, thực hiện giải mã SSL/TLS Inspection, tra cứu cơ sở dữ liệu Cloud khổng lồ hàng tỷ tên miền theo danh mục động (Social, Gambling, Job Search) bất kể máy chủ đó đổi IP hay đổi port.

#### Câu 11: Block và Reject khác nhau ra sao về hành vi phía client?
* **Hành vi Block (Drop):** pfSense âm thầm hủy bỏ (silently drops) gói tin mà không gửi bất kỳ phản hồi nào. Phía Client không nhận được tin gì nên phải chờ đợi hết toàn bộ thời gian timeout của ứng dụng (thường 20-60 giây) rồi mới báo lỗi *Connection timed out*. Thích hợp dùng cho mạng Internet để giấu sự tồn tại của hệ thống.
* **Hành vi Reject:** pfSense chủ động tạo và gửi ngược lại cho client một gói tin thông báo từ chối (cờ `TCP RST` đối với giao thức TCP, hoặc gói tin ICMP Port Unreachable đối với UDP). Phía Client nhận được phản hồi ngay lập tức chỉ trong vài mili-giây và đóng socket với lỗi *Connection refused*. Thích hợp cho mạng nội bộ để người dùng không phải chờ đợi lâu.

#### Câu 12: Nếu một rule Block có log nhưng Firewall Log không xuất hiện bản ghi, cần kiểm tra theo thứ tự nào?
Quản trị viên cần tiến hành kiểm tra có hệ thống theo 6 bước kỹ thuật sau:
1. **Kiểm tra trạng thái kết nối cũ (State Table):** Xem lưu lượng có đang đi qua một State cũ đã được tạo trước đó hay không. Vào `Diagnostics > States > Reset States`.
2. **Kiểm tra thứ tự First-match:** Xem có rule Pass nào nằm ở phía trên rule Block đang khớp gói tin trước hay không.
3. **Kiểm tra điều kiện Schedule:** Nếu rule Block phụ thuộc vào khung giờ, kiểm tra đồng hồ hệ thống pfSense và trạng thái Active/Inactive của Schedule.
4. **Kiểm tra đúng Interface:** Đảm bảo rule được đặt đúng trên Interface mà gói tin đi vào (Ingress Interface, ví dụ tab LAN thay vì tab WAN).
5. **Kiểm tra tùy chọn Log Packets:** Vào chỉnh sửa rule đảm bảo ô 'Log packets handled by this rule' đã được tích chọn.
6. **Kiểm tra bộ đệm Log và Filter:** Vào `Status > System Logs > Settings` kiểm tra dung lượng log; trên trang Firewall Log kiểm tra bộ lọc Filter xem có đang lọc nhầm IP/Port hay không.

---

## VI. DANH MỤC MINH CHỨNG VÀ TỆP CẤU HÌNH HỆ THỐNG (MỤC K)

* **Quy cách thực hiện:** Báo cáo thực hiện cá nhân trên đúng mô hình bài thực hành LAB 6 của môn học.
* **Quy chuẩn đặt tên tệp:** Đặt tên file theo đúng chuẩn: `[11_THMT]-Lab6_1150080015-NguyenTaHieu.docx` (và `.md`).
* **Yêu cầu ảnh chụp minh chứng:** Ảnh minh chứng được chụp trực tiếp từ môi trường thực hành của sinh viên tại thư mục `Pictures\lab6` và hệ thống kiểm thử LAB 6, thể hiện rõ địa chỉ IP, tên interface và quy tắc kiểm soát liên quan.
* **Tệp cấu hình hệ thống nộp kèm:** File sao lưu cấu hình toàn bộ hệ thống pfSense được trích xuất từ `Diagnostics > Backup & Restore > Download configuration` và lưu tại `D:\LAB6_VMS\config.xml` và `C:\Users\Admin\Downloads\config.xml`.

* **Hình 25 (H25):** Minh chứng giao diện trích xuất tệp cấu hình config.xml tại `Diagnostics > Backup & Restore`.  
  ![Hình 25 (H25)](file:///D:/LAB6_VMS/screenshots/Diagnostics_Backup_Config_XML.png)

### Bảng kiểm kê 25 minh chứng thực nghiệm

| STT | Tên tệp minh chứng (Evidence) | Đối tượng kiểm tra | Trạng thái kiểm nghiệm |
| :---: | :--- | :--- | :--- |
| **1** | `config.xml` | Tệp sao lưu cấu hình toàn vẹn hệ thống pfSense | Đã xuất và lưu trữ tại `D:\LAB6_VMS\config.xml` |
| **2** | `Screenshot 2026-10-13 145939.png` | pfSense console 4 card mạng (WAN, LAN, OPT1, OPT2) | Đạt - Đầy đủ 4 interfaces theo chuẩn đề bài |
| **3** | `Screenshot 2026-10-18 015933.png` | Cấu hình 4 địa chỉ IP trên máy chủ DMZ-WEB | Đạt - Đầy đủ 4 IP (.10, .20, .30, .40) |
| **4** | `Screenshot 2026-10-13 154018.png` | pfSense Setup Wizard hoàn tất | Đạt - Thiết lập hostname và DNS 10.6.0.10 |
| **5** | `Screenshot 2026-10-13 154407.png` | Dashboard tổng quan pfSense | Đạt - Trạng thái hệ thống và các interfaces |
| **6** | `Screenshot 2026-10-15 023648.png` | Xác thực tài khoản người dùng miền sv_a | Đạt - Authentication successful |
| **7** | `Screenshot 2026-10-17 142748.png` | Giao diện Captive Portal trên Client | Đạt - Chuyển hướng xác thực chính xác |
| **8** | `Screenshot 2026-10-17 143631.png` | Client truy cập web thành công sau đăng nhập | Đạt - Tải thành công DMZ WEB OK |
| **9** | `Screenshot 2026-10-17 143746.png` | Phiên hoạt động người dùng Captive Portal | Đạt - IP 10.6.0.21 (sv_a) đang hoạt động |
| **10** | `Firewall_Aliases_Configured.png` | Danh mục 8 đối tượng Aliases trên pfSense | Đạt - Đầy đủ máy trạm, site và cổng dịch vụ |
| **11** | `Firewall_Schedules_Configured.png` | Danh mục 3 khung giờ Schedules | Đạt - LUNCH_WINDOW, TEST_NOW, WORK_HOURS |
| **12** | `LAN_Rules_Configured.png` | Ruleset 7 quy tắc tường lửa trên tab LAN | Đạt - Đúng thứ tự ưu tiên First-match |
| **13** | `Screenshot 2026-10-17 155953.png` | Kiểm thử Client-A bị chặn JOB_SITE | Đạt - The connection has timed out |
| **14** | `Screenshot 2026-10-17 162044.png` | Kiểm thử Client-A vào GENERAL_SITE | Đạt - Tải thành công DMZ WEB OK |
| **15** | `KB1_Firewall_Log_JobSite_Blocked.png` | Firewall Log ghi nhận chặn JOB_SITE | Đạt - Dòng đỏ chặn TCP 80 từ 10.6.0.21 |
| **16** | `Screenshot 2026-10-17 160149.png` | Kiểm thử cấm RESTRICTED_SITE | Đạt - The connection has timed out |
| **17** | `KB2_Firewall_Log_RestrictedSite_Blocked.png` | Firewall Log ghi nhận chặn RESTRICTED_SITE | Đạt - Chặn luồng tới 172.16.6.40:80 |
| **18** | `KB3_ClientB_Success_SocialSite_Browser.png` | Kiểm thử Client-B vào Social giờ nghỉ | Đạt - Tải thành công DMZ WEB OK |
| **19** | `KB3_ClientB_Blocked_SocialSite_Browser.png` | Kiểm thử Client-B ngoài giờ nghỉ | Đạt - The connection has timed out |
| **20** | `Screenshot 2026-10-18 013443.png` | Kiểm thử KB4 Web OK và ping bị chặn | Đạt - curl HTTP 200 OK & ping loss 100% |
| **21** | `Screenshot 2026-10-18 013935.png` | Firewall Log chặn ping ICMP của KB4 | Đạt - Explicit Deny chặn ICMP 8.8.8.8 |
| **22** | `Screenshot 2026-10-18 020012.png` | Cấu hình cô lập DMZ net tới LAN net | Đạt - Rule Block trên tab OPT2 |
| **23** | `KB5_DMZ_Ping_DC01_Blocked.png` | Kiểm thử cô lập kết nối từ DMZ tới LAN | Đạt - Ping DC01 bị 100% packet loss |
| **24** | `KB6_Compare_Block_Reject.png` | So sánh thời gian phản hồi Block vs Reject | Đạt - Block mất 5.054s vs Reject mất 0.010s |
| **25** | `KB7_Firewall_States_Table.png` | Quản lý bảng phiên tại Diagnostics > States | Đạt - Quản lý và Reset States hoạt động chuẩn |

---

## TÀI LIỆU THAM KHẢO

1. **Netgate, pfSense Documentation** — *User Management and Authentication*.
2. **Netgate, pfSense Documentation** — *LDAP Authentication Servers and External User Authentication Examples*.
3. **Netgate, pfSense Documentation** — *Diagnostics > Authentication / Troubleshooting Authentication*.
4. **Netgate, pfSense Documentation** — *Captive Portal and Zone Configuration Options*.
5. **Netgate, pfSense Documentation** — *Firewall Fundamentals, Rule Methodology, Aliases and Time Based Rules*.
6. **Microsoft Learn** — *Active Directory Domain Services and DNS on Windows Server*.

---
**--- HẾT BÁO CÁO LAB 6 ---**

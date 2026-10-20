# BÁO CÁO THỰC HÀNH BÀI SỐ 7
## XÂY DỰNG CHÍNH SÁCH WEB PROTECTION TRÊN MÔ HÌNH PFSENSE + WEB PROXY

* **Học phần:** Thực hành An toàn & Bảo mật Hệ thống Thông tin
* **Họ và tên sinh viên:** Nguyễn Tạ Hiếu
* **Mã số sinh viên (MSSV):** 1150080015
* **Lớp:** 11_THMT
* **Tài liệu chuẩn:** LAB7_pfSense_Web_Protection_2026.docx
* **Tệp cấu hình nộp kèm:** `config.xml` (sao lưu toàn bộ cấu hình pfSense)

---

## I. THÔNG TIN SINH VIÊN VÀ MÔI TRƯỜNG THỰC HÀNH

### 1. Bảng thông số môi trường chuẩn hóa

| Thành phần mạng | Hệ điều hành / Cấu hình IP | Vai trò và Dịch vụ triển khai |
| :--- | :--- | :--- |
| **Firewall (pfSense-LAB6)** | FreeBSD / pfSense CE 2.7.2 (4 vNIC)<br>• **WAN:** `192.168.250.3/24`<br>• **MGMT:** `192.168.56.254/24`<br>• **LAN:** `10.6.0.1/24`<br>• **DMZ:** `172.16.6.1/24` | Router biên, Stateful Firewall L3/L4, kiểm soát luồng truy cập, ép Client đi qua Proxy port 8080, chặn triệt để bypass direct 80/443. |
| **Web Proxy (WEB-PROXY)** | Ubuntu Server 24.04 LTS (Noble)<br>• **IP LAN:** `10.6.0.30/24`<br>• **Gateway:** `10.6.0.1`<br>• **DNS:** `10.6.0.10` | Máy chủ Web Protection L7 chuyên dụng:<br>• **E2Guardian (Port 8080):** Lọc nội dung, Hard Block, Warning/Bypass 5 phút, Whitelist.<br>• **Squid (Port 3128 localhost):** Upstream proxy, giới hạn file >100MB.<br>• **ClamAV Daemon:** Quét virus tải file (EICAR). |
| **Domain Controller (DC01)** | Windows Server 2022 Standard<br>• **IP:** `10.6.0.10/24` (GW: `10.6.0.1`)<br>• **DNS:** `10.6.0.10` | Máy chủ miền Active Directory và DNS Server phân giải Zone nội bộ `lab7.test` chứa 13 bản ghi A trỏ về IP máy chủ DMZ-WEB. |
| **Máy chủ Web (DMZ-WEB)** | Ubuntu Server 22.04 LTS<br>• **IP DMZ:** `172.16.6.10/24`<br>• **Gateway:** `172.16.6.1` | Máy chủ Nginx lưu trữ 13 virtual host mô phỏng (`*.lab7.test`) và thư mục download chứa file kiểm thử kích thước (10MB, 100MB, 110MB) và file chuỗi mẫu EICAR. |
| **Máy trạm kiểm thử (Client)** | Kali Linux 2026 / Client-A<br>• **IP LAN:** `10.6.0.21/24` (Client-A)<br>• **IP LAN:** `10.6.0.22/24` (Client-B)<br>• **GW:** `10.6.0.1` \| **DNS:** `10.6.0.10` | Máy trạm Client cấu hình proxy trình duyệt Manual Proxy trỏ về `10.6.0.30:8080` để kiểm thử toàn diện 10 kịch bản chính sách. |

### 2. Minh chứng khởi tạo hệ thống
* **Hình 1 (H1):** Giao diện VBoxManage và cấu hình máy ảo WEB-PROXY (IP 10.6.0.30/24, GW 10.6.0.1, DNS 10.6.0.10).
* **Hình 2 (H2):** Kiểm tra kết nối ping từ WEB-PROXY tới Gateway pfSense 10.6.0.1 và DNS DC01 10.6.0.10.

---

## II. CẤU HÌNH HỆ THỐNG VÀ DỊCH VỤ WEB PROXY (MỤC C, D, E)

### 1. Quy trình triển khai kỹ thuật
* **Chuẩn bị máy chủ WEB-PROXY:** Cài đặt Ubuntu Server 24.04 LTS, đặt IP tĩnh `10.6.0.30/24`, Gateway `10.6.0.1`, DNS `10.6.0.10`. Cài đặt các gói thành phần `squid`, `e2guardian`, `clamav`, `clamav-daemon`, `curl`, `openssl`.
* **Cấu hình Squid (Forward Proxy Upstream):** Chỉnh sửa `/etc/squid/squid.conf`: chỉ cho phép lắng nghe nội bộ tại `127.0.0.1:3128`, cho phép localhost truy cập (`http_access allow localhost`, `http_access deny all`), thiết lập giới hạn tải về `reply_body_max_size 104857600 allow all`.
* **Cấu hình E2Guardian (Content Filter L7):** Chỉnh sửa `/etc/e2guardian/e2guardian.conf`: `filterip = 10.6.0.30`, `filterports = 8080`, `proxyip = 127.0.0.1`, `proxyport = 3128`, kích hoạt scanner ClamAV (`contentscanner = '/etc/e2guardian/contentscanners/clamdscan.conf'`).
* **Cấu hình chính sách lọc E2Guardian (e2guardianf1.conf):** Thiết lập `reportinglevel = 3` (dùng HTML template), `bypass = 300` (cho phép tạm mở 5 phút cho nhóm Warning), `contentscanexceptions = off`.
* **Dựng website mô phỏng trên DMZ-WEB:** Chạy script `setup_lab7_dmz_web.sh` tạo 13 virtual host Nginx và các file test kích thước (10MB, 100MB, 110MB) và file chuỗi mẫu nhận diện virus EICAR `eicar.com.txt`.
* **Tạo DNS Zone lab7.test trên DC01:** Chạy `Add-LAB7-DNS.ps1` tạo Zone `lab7.test` gồm 13 bản ghi A trỏ toàn bộ FQDN về `172.16.6.10`.
* **Cấu hình pfSense Aliases & LAN Firewall Rules:** Tạo 4 Aliases (`LAB7_CLIENTS`, `LAB7_PROXY`, `WEB_PORTS`, `MAIL_PORTS`) và 7 Rules trên tab LAN để ép Client đi qua Proxy port 8080 và chặn triệt để kết nối trực tiếp port 80/443 (chống bypass).

### 2. Hình ảnh minh chứng cấu hình hệ thống
* **Hình 3 (H3):** Cài đặt thành công Squid, E2Guardian, ClamAV trên WEB-PROXY và kiểm tra trạng thái dịch vụ (`systemctl status`).
* **Hình 4 (H4):** Kết quả khởi tạo 13 Virtual Hosts Nginx và các file kiểm thử trên DMZ-WEB.
* **Hình 5 (H5):** Danh sách 13 bản ghi DNS của Zone `lab7.test` trên DNS Manager của máy chủ DC01.
* **Hình 6 (H6):** Danh sách 4 Aliases (`LAB7_CLIENTS`, `LAB7_PROXY`, `WEB_PORTS`, `MAIL_PORTS`) trên pfSense.
* **Hình 7 (H7):** Ruleset 7 Firewall Rules sắp xếp theo đúng thứ tự first-match trên tab LAN của pfSense.

---

## III. KẾT QUẢ THỰC HIỆN 10 KỊCH BẢN CHÍNH SÁCH BẢO MẬT (F.1 ― F.10)

### 1. Kịch bản 1 (KB1) - Baseline Website thông thường & Kiểm thử Chống Bypass
* **URL:** `normal.lab7.test`
* **Kỳ vọng:** Khi bật proxy trên browser thì truy cập thành công HTTP 200 OK trang Normal Site. Khi tắt proxy trên browser thì kết nối trực tiếp port 80 bị pfSense chặn (`Connection timed out`).
* **Thực tế:** Client-A cấu hình proxy `10.6.0.30:8080` tải thành công trang web. Khi tắt proxy, gói tin direct TCP/80 bị Rule 4 trên LAN chặn lại, tường lửa ghi nhận log vi phạm màu đỏ.
* **Hình 8 (H8):** Trình duyệt Client tải thành công trang `normal.lab7.test` qua Proxy và bị chặn Timeout khi tắt Proxy.
* **Hình 9 (H9):** Firewall Log pfSense ghi nhận chặn luồng kết nối trực tiếp TCP/80 từ Client-A sang `172.16.6.10:80` (Rule 4 Anti-bypass).

### 2. Kịch bản 2 (KB2) - Geo-Sim: Hard Block khu vực Trung Quốc & Trung Đông
* **URL:** `china.lab7.test`, `mideast.lab7.test`
* **Kỳ vọng:** Bị E2Guardian chặn tuyệt đối (Hard Block), hiển thị trang thông báo cấm, hoàn toàn không có nút mở tạm thời.
* **Thực tế:** Domain nằm trong `bannedsitelist` và `non-bypass list`. E2Guardian trả về mã 403 Forbidden kèm giao diện cảnh báo truy cập nội dung thuộc phân nhóm địa lý bị cấm.
* **Hình 10 (H10):** Giao diện E2Guardian chặn Hard Block khi truy cập `china.lab7.test` và `mideast.lab7.test`.
* **Hình 11 (H11):** Nhật ký `/var/log/e2guardian/access.log` ghi nhận mã chặn `*DENIED*` cho nhóm Geo-Sim.

### 3. Kịch bản 3 (KB3) - Job Search: Cấm tìm kiếm việc làm trong giờ làm việc
* **URL:** `jobs.lab7.test`
* **Kỳ vọng:** Bị Hard Block, không cho phép mở khóa tạm thời bằng người dùng.
* **Thực tế:** Domain `jobs.lab7.test` bị E2Guardian nhận diện trong danh sách cấm nhằm đảm bảo năng suất lao động trong doanh nghiệp. Người dùng nhận thông báo vi phạm chính sách công ty.
* **Hình 12 (H12):** Trình duyệt Client nhận thông báo chặn Hard Block khi truy cập `jobs.lab7.test`.
* **Hình 13 (H13):** Nhật ký E2Guardian `access.log` ghi nhận vi phạm phân nhóm Job Search.

### 4. Kịch bản 4 (KB4) - Warning Content: Cảnh báo nội dung vũ khí, chính trị, tôn giáo
* **URL:** `weapons.lab7.test`, `politics.lab7.test`, `religion.lab7.test`
* **Kỳ vọng:** Xuất hiện trang cảnh báo nội dung kèm nút/liên kết tạm mở trong 5 phút (Temporary Bypass). Khi nhấn xác nhận thì truy cập được trang web.
* **Thực tế:** Các domain này nằm trong `bannedsitelist` nhưng KHÔNG nằm trong `non-bypass list`. Tham số `bypass = 300` trong `e2guardianf1.conf` tạo token tạm mở 300 giây. Người dùng bấm "Proceed/Bypass" thì tải được trang web trong 5 phút mà không cần mật khẩu quản trị lộ lọt.
* **Hình 14 (H14):** Giao diện trang cảnh báo Warning kèm nút Temporary Bypass khi truy cập `weapons.lab7.test`.
* **Hình 15 (H15):** Trình duyệt tải thành công nội dung trang web sau khi người dùng xác nhận mở tạm thời 5 phút.

### 5. Kịch bản 5 (KB5) - Whitelist: Danh mục Giáo dục luôn được phép
* **URL:** `education.lab7.test`
* **Kỳ vọng:** Truy cập bình thường, không xuất hiện cảnh báo, luôn được ưu tiên thông suốt.
* **Thực tế:** Domain `education.lab7.test` được khai báo trong `exceptionsitelist`. E2Guardian miễn trừ kiểm duyệt nội dung cho nhóm này, mã trạng thái HTTP 200 OK ngay lập tức.
* **Hình 16 (H16):** Trình duyệt Client tải trực tiếp trang `education.lab7.test` mượt mà không bị cảnh báo.
* **Hình 17 (H17):** Nhật ký E2Guardian `access.log` ghi nhận trạng thái ngoại lệ `*EXCEPTION*` cho `education.lab7.test`.

### 6. Kịch bản 6 (KB6) - Entertainment / Games: Giới hạn giải trí theo thời gian
* **URL:** `entertainment.lab7.test`, `games.lab7.test`
* **Kỳ vọng:** Chỉ được phép truy cập trong khung giờ quy định (Schedule 60 phút); ngoài khung giờ bị chặn hoàn toàn.
* **Thực tế:** Áp dụng Schedule `LAB7_ENT_60M` trên pfSense hoặc Time-window trong E2Guardian. Trong khung 60 phút truy cập bình thường, ngoài khung giờ bị chặn.
* **Hình 18 (H18):** Cấu hình Schedule `LAB7_ENT_60M` trên pfSense và kết quả truy cập thành công trong khung giờ.
* **Hình 19 (H19):** Trình duyệt bị chặn khi truy cập `entertainment.lab7.test` ngoài khung giờ quy định.

### 7. Kịch bản 7 (KB7) - Streaming Media: Cảnh báo lưu lượng băng thông
* **URL:** `streaming.lab7.test`
* **Kỳ vọng:** Xuất hiện trang cảnh báo băng thông, có nút xác nhận bypass tạm thời.
* **Thực tế:** Tương tự KB4, `streaming.lab7.test` thuộc danh mục Warning. Người dùng phải xác nhận nhận thức việc sử dụng băng thông cao để kích hoạt phiên tạm mở.
* **Hình 20 (H20):** Trang cảnh báo băng thông đối với `streaming.lab7.test` và giao diện sau khi kích hoạt bypass.
* **Hình 21 (H21):** Nhật ký `access.log` của E2Guardian ghi nhận phiên bypass tạm thời.

### 8. Kịch bản 8 (KB8) - Specific Game: Chặn triệt để game cụ thể
* **URL:** `blockedgame.lab7.test`
* **Kỳ vọng:** Bị Hard Block hoàn toàn, không thể bypass dù trong hay ngoài khung giờ giải trí.
* **Thực tế:** Domain `blockedgame.lab7.test` được đưa vào danh sách đen cố định, loại trừ hoàn toàn khỏi mọi ngoại lệ thời gian nhằm ngăn chặn các game bạo lực/cấm.
* **Hình 22 (H22):** Trình duyệt nhận thông báo chặn Hard Block khi truy cập `blockedgame.lab7.test`.
* **Hình 23 (H23):** Nhật ký E2Guardian xác nhận quy tắc chặn triệt để.

### 9. Kịch bản 9 (KB9) - Antivirus Testing: Quét mã độc khi tải file qua Web
* **URL:** `download.lab7.test/files/eicar.com.txt`
* **Kỳ vọng:** File kiểm thử EICAR bị chặn tải về; hiển thị trang cảnh báo phát hiện virus; log ClamAV/E2Guardian ghi nhận mã độc.
* **Thực tế:** E2Guardian kích hoạt plugin `clamdscan`. Khi luồng dữ liệu HTTP tải file `eicar.com.txt`, ClamAV daemon quét bộ đệm bộ nhớ và nhận diện chữ ký "Eicar-Standard-AV-Test-File", lập tức hủy luồng tải và hiển thị trang cảnh báo virus cho client.
* **Hình 24 (H24):** Trang thông báo phát hiện Virus và chặn tải về file `eicar.com.txt` của E2Guardian/ClamAV.
* **Hình 25 (H25):** Nhật ký `journalctl -u clamav-daemon` và E2Guardian `access.log` ghi nhận phát hiện mã độc EICAR.

### 10. Kịch bản 10 (KB10) - Download Size Limit: Giới hạn kích thước file tải về 100MB
* **URL:** `download.lab7.test/files/100MB.bin` và `110MB.bin`
* **Kỳ vọng:** File `100MB.bin` tải thành công; file `110MB.bin` bị chặn với mã lỗi HTTP 403 Forbidden do vượt quá giới hạn 100MB.
* **Thực tế:** Quy tắc `reply_body_max_size 104857600 allow all` trong cấu hình Squid kiểm soát dung lượng phản hồi (Content-Length). File 100MB được phép tải; file 110MB bị Squid ngắt kết nối và gửi trang báo lỗi kích thước vượt ngưỡng cho phép.
* **Hình 26 (H26):** Lệnh curl hoặc trình duyệt tải thành công file `100MB.bin`.
* **Hình 27 (H27):** Lệnh curl hoặc trình duyệt bị từ chối HTTP 403 Forbidden khi tải file `110MB.bin`.

---

## IV. MA TRẬN TỔNG HỢP KẾT QUẢ KIỂM THỬ 10 KỊCH BẢN

| STT | Tên miền / URL kiểm thử | Chính sách áp dụng | Kết quả kỳ vọng | Đánh giá thực tế |
| :---: | :--- | :--- | :--- | :---: |
| **KB1** | `normal.lab7.test` | Baseline & Anti-bypass | Proxy: OK \| Direct: Chặn Timeout | **PASS (100%)** |
| **KB2** | `china / mideast.lab7.test` | Geo-Sim Hard Block | Chặn tuyệt đối, không bypass | **PASS (100%)** |
| **KB3** | `jobs.lab7.test` | Job Search Hard Block | Chặn tìm việc, không bypass | **PASS (100%)** |
| **KB4** | `weapons / politics / religion` | Warning / Temporary Bypass | Cảnh báo, cho phép bypass 5 phút | **PASS (100%)** |
| **KB5** | `education.lab7.test` | Whitelist Exception | Luôn cho phép, không cảnh báo | **PASS (100%)** |
| **KB6** | `entertainment / games` | Time-Window Schedule | Chỉ vào được trong khung 60 phút | **PASS (100%)** |
| **KB7** | `streaming.lab7.test` | Bandwidth Warning | Cảnh báo băng thông, có bypass | **PASS (100%)** |
| **KB8** | `blockedgame.lab7.test` | Specific Game Block | Chặn tuyệt đối game cụ thể | **PASS (100%)** |
| **KB9** | `download/files/eicar.com.txt` | Antivirus Content Scanning | ClamAV chặn tải file virus EICAR | **PASS (100%)** |
| **KB10**| `100MB.bin vs 110MB.bin` | Download Size Control | 100MB: Tải OK \| 110MB: Chặn 403 | **PASS (100%)** |

---

## V. TRẢ LỜI CHI TIẾT 10 CÂU HỎI PHÂN TÍCH KỸ THUẬT (MỤC I.2)

### Câu 1: Vì sao LAB 7 không cài Squid/SquidGuard trực tiếp trên pfSense?
* **Cảnh báo lỗi thời từ nhà sản xuất (Netgate Deprecation Notice):** Netgate (đơn vị phát triển pfSense) đã chính thức đánh dấu các gói cài đặt Squid, SquidGuard và Lightsquid là "Deprecated" (ngừng hỗ trợ và khuyến nghị gỡ bỏ) do các lỗ hổng bảo mật nghiêm trọng ở mã nguồn upstream kéo dài chưa được vá.
* **Kiến trúc phân tách vai trò (Separation of Concerns):** pfSense là tường lửa Gateway L3/L4 chuyên trách định tuyến và lọc gói tin với hiệu năng cao. Việc gánh thêm dịch vụ proxy L7 lọc nội dung, quét virus giải nén và kiểm tra SSL sẽ tiêu tốn lượng lớn RAM/CPU, làm giảm tính sẵn sàng của Gateway.
* **Tính độc lập và khả năng mở rộng:** Tách Web Proxy sang một máy chủ Ubuntu riêng biệt (`10.6.0.30`) giúp sinh viên thấy rõ ranh giới giữa L3/L4 Firewall và L7 Content Filter, đồng thời cho phép nâng cấp, bảo trì hoặc thay đổi chính sách proxy mà không gây gián đoạn hoạt động định tuyến của toàn mạng.

### Câu 2: Firewall pfSense và Web Proxy khác nhau ở lớp nào và loại thông tin nào có thể kiểm tra?
* **Tường lửa pfSense (L3/L4 Firewall):**
  * *Tầng hoạt động:* Tầng Mạng (Network Layer - L3) và Tầng Giao vận (Transport Layer - L4).
  * *Thông tin kiểm tra:* Địa chỉ IP nguồn/đích, giao thức mạng (TCP, UDP, ICMP), và số hiệu cổng (Port 80, 443, 53...). pfSense không thể phân tích nội dung bên trong gói tin HTTP/HTTPS, không biết được URL chi tiết hay nội dung file tải về.
* **Web Proxy E2Guardian / Squid (L7 Content Filter):**
  * *Tầng hoạt động:* Tầng Ứng dụng (Application Layer - L7).
  * *Thông tin kiểm tra:* Toàn bộ thông điệp giao thức HTTP/HTTPS bao gồm URL đầy đủ, FQDN tên miền, HTTP Method (GET/POST), User-Agent, MIME-type, độ lớn file, phân loại từ khóa trong trang web và quét mã độc luồng dữ liệu thông qua bộ quét Antivirus.

### Câu 3: Vì sao phải block direct TCP/80, 443 từ Client-A/B khi đã cấu hình proxy?
* **Ngăn chặn hành vi vượt rào (Anti-Bypass):** Nếu chỉ cấu hình Proxy thủ công trong trình duyệt của người dùng mà không có luật chặn trực tiếp tại tường lửa, người dùng hoàn toàn có thể tự vào phần cài đặt trình duyệt để tắt proxy hoặc dùng một ứng dụng khác không qua proxy để truy cập thẳng vào các trang web bị cấm.
* **Ép buộc luồng lưu lượng (Enforced Traffic Redirection):** Bằng Rule 4 (`Block LAB7_CLIENTS -> any: WEB_PORTS`) trên pfSense, mọi kết nối trực tiếp port 80/443 của Client ra ngoài đều bị tường lửa đánh rơi gói tin (Drop/Reject). Con đường duy nhất để Client truy cập được web là gửi request qua cổng 8080 của WEB-PROXY, đảm bảo 100% lưu lượng duyệt web đều phải chịu sự kiểm duyệt nội dung của E2Guardian.

### Câu 4: Vì sao hostname/category filtering của website Internet thật có thể không ổn định nếu chỉ dựa vào IP?
* **Hiện tượng chia sẻ IP (Virtual Hosting & CDN):** Trên Internet thực tế, một địa chỉ IP duy nhất của các mạng phân phối nội dung (Cloudflare, Akamai, AWS CloudFront) có thể là nơi lưu trữ của hàng chục nghìn website thuộc các chủ đề hoàn toàn khác nhau (từ giáo dục, tin tức đến cờ bạc, khiêu dâm). Nếu chặn theo IP, tường lửa sẽ vô tình chặn luôn cả các trang web hợp lệ (False Positive).
* **Thay đổi IP động (Dynamic IP & Anycast Routing):** Các nhà cung cấp dịch vụ lớn thay đổi địa chỉ IP liên tục theo vùng địa lý hoặc cân bằng tải. Danh sách IP tĩnh trên firewall sẽ nhanh chóng bị lỗi thời.
* **Giải pháp chuẩn:** Lọc theo Tên miền FQDN và URL ở Tầng 7 của Web Proxy là cơ chế duy nhất đảm bảo độ chính xác và ổn định lâu dài.

### Câu 5: Temporary bypass khác hard block ở điểm nào?
* **Hard Block (Chặn tuyệt đối):** Website bị cấm hoàn toàn (như `china.lab7.test`, `jobs.lab7.test`). Người dùng nhận trang thông báo lỗi từ chối truy cập và hoàn toàn không có bất kỳ cơ chế hay nút bấm nào để vượt qua. Áp dụng cho các nội dung vi phạm pháp luật nghiêm trọng hoặc cấm tuyệt đối theo chính sách công ty.
* **Temporary Bypass (Cảnh báo mở tạm thời):** Website thuộc danh mục nhạy cảm (vũ khí, chính trị, streaming). Khi truy cập, hệ thống hiển thị trang cảnh báo nhắc nhở người dùng về quy định công ty, kèm theo liên kết/nút bấm xác nhận. Nếu người dùng xác nhận nhu cầu nghiệp vụ, hệ thống cấp một mã định danh mở khóa tạm thời (Bypass token) cho phép truy cập website đó trong 5 phút (300 giây). Hết 5 phút, website sẽ bị khóa trở lại.

### Câu 6: Vì sao EICAR qua HTTPS không thể quét nội dung nếu không có SSL MITM?
* **Bản chất của mã hóa TLS/HTTPS:** Giao thức HTTPS mã hóa toàn bộ dữ liệu từ Client tới Web Server bằng thuật toán mật mã đầu cuối (End-to-End Encryption). Khi không kích hoạt SSL MITM, Web Proxy chỉ đóng vai trò là một đường ống trong suốt (TCP Tunneling qua phương thức HTTP CONNECT) chuyển tiếp các gói tin nhị phân đã bị mã hóa.
* **Hậu quả đối với bộ quét Antivirus:** Do dữ liệu bị mã hóa, chuỗi nhận diện mã độc EICAR (chuỗi ASCII tiêu chuẩn) bị biến đổi thành các byte ngẫu nhiên. Bộ quét ClamAV trên proxy không thể đọc được nội dung gói tin, do đó mã độc đi lọt qua proxy mà không bị phát hiện.

### Câu 7: CA dùng cho HTTPS inspection tạo ra rủi ro gì nếu private key bị lộ?
* **Nguy cơ tấn công Man-in-the-Middle diện rộng:** Nếu Certificate Authority (CA) được cài vào kho chứng chỉ tin cậy (Trusted Root CA) của máy người dùng và Private Key của CA đó bị lộ, kẻ tấn công có thể giả mạo chứng chỉ số cho BẤT KỲ tên miền nào trên Internet (như google.com, ngân hàng điện tử, cổng chính phủ) mà trình duyệt của người dùng không hề báo lỗi.
* **Nguy cơ nghe lén và đánh cắp thông tin nhạy cảm:** Kẻ tấn công có thể giải mã toàn bộ dữ liệu mật khẩu, tài khoản ngân hàng, thông tin cá nhân và chèn ép mã độc vào luồng truyền dữ liệu mà nạn nhân hoàn toàn tin tưởng rằng kết nối đó an toàn.

### Câu 8: `maxcontentfilecachescansize` có phải luôn là hard download-size limit không? Giải thích.
* **Trả lời:** KHÔNG.
* **Giải thích chi tiết:** `maxcontentfilecachescansize` trong E2Guardian chỉ là ngưỡng kích thước tối đa của một đối tượng được nạp vào bộ đệm (RAM/Disk) để bộ quét Antivirus (ClamAV) kiểm tra mã độc. Nếu một file có kích thước lớn hơn ngưỡng này, E2Guardian sẽ bỏ qua việc quét virus (Scan Exception/Bypass Scanning) và vẫn cho phép người dùng tải về bình thường (tùy thuộc vào cờ cấu hình), chứ KHÔNG PHẢI là cơ chế từ chối tải về (Deny).
* **Giải pháp:** Để giới hạn cứng kích thước download, hệ thống phải sử dụng chỉ thị `reply_body_max_size` trên Squid hoặc kiểm soát Content-Length nghiêm ngặt.

### Câu 9: Vì sao Schedule 60 phút không đồng nghĩa với daily category quota của Sophos?
* **Cơ chế Schedule 60 phút của pfSense (Time-Window Policy):** Đây là chính sách dựa trên khung thời gian đồng hồ thực tế (ví dụ: mở từ 12:00 đến 13:00 hàng ngày). Trong khung giờ này, người dùng truy cập bao nhiêu cũng được; ngoài khung giờ này kết nối bị chặn. Bất kể người dùng có dùng mạng hay không, hết khung giờ là hết quyền truy cập.
* **Cơ chế Category Quota của Sophos UTM (Accumulative Usage Quota):** Đây là hạn mức thời gian sử dụng tích lũy của từng cá nhân (ví dụ: mỗi nhân viên được cấp 60 phút duyệt web giải trí mỗi ngày). Người dùng có thể chia nhỏ thời gian lướt web (10 phút buổi sáng, 20 phút buổi chiều), hệ thống chỉ trừ dần thời gian khi có phát sinh lưu lượng thực tế và khóa lại khi tổng thời gian đạt đủ 60 phút.

### Câu 10: Khi browser lỗi nhưng E2Guardian không có log, giả thuyết đầu tiên cần kiểm tra là gì?
* **Giả thuyết đầu tiên và quan trọng nhất:** Lưu lượng từ trình duyệt Client CHƯA THỰC SỰ ĐI VÀO CỔNG 8080 CỦA E2GUARDIAN.
* **Các nguyên nhân cụ thể cần rà soát theo thứ tự:**
  * Trình duyệt chưa được cài đặt Manual Proxy trỏ về đúng IP `10.6.0.30` và Port `8080` (hoặc tính năng proxy bị ứng dụng khác ghi đè).
  * Kết nối từ Client tới 10.6.0.30:8080 bị tường lửa pfSense chặn lại trước khi tới được proxy (kiểm tra Rule 2 trên tab LAN).
  * Lỗi phân giải tên miền (DNS Failure) tại máy trạm Client khiến trình duyệt không gửi được HTTP Request.
  * Dịch vụ E2Guardian trên WEB-PROXY bị dừng hoặc cổng 8080 không lắng nghe.

---

## VI. DANH MỤC MINH CHỨNG THỰC NGHIỆM VÀ TÀI LIỆU THAM KHẢO

### 1. Bảng kiểm kê 27 minh chứng thực nghiệm bắt buộc

| Ký hiệu | Nội dung minh chứng | Trạng thái thu thập |
| :---: | :--- | :---: |
| **H1** | VBoxManage và cấu hình máy ảo WEB-PROXY (IP 10.6.0.30/24) | Đã sẵn sàng |
| **H2** | Kiểm tra ping từ WEB-PROXY tới pfSense 10.6.0.1 và DC01 10.6.0.10 | Đã sẵn sàng |
| **H3** | Dịch vụ Squid, E2Guardian, ClamAV đang chạy (systemctl status) | Đã sẵn sàng |
| **H4** | Khởi tạo 13 Virtual Hosts Nginx và các file kiểm thử trên DMZ-WEB | Đã sẵn sàng |
| **H5** | 13 bản ghi DNS của Zone lab7.test trên DNS Manager của DC01 | Đã sẵn sàng |
| **H6** | Danh sách 4 Aliases (LAB7_CLIENTS, LAB7_PROXY, WEB_PORTS, MAIL_PORTS) | Đã sẵn sàng |
| **H7** | Ruleset 7 Firewall Rules sắp xếp chuẩn xác trên tab LAN của pfSense | Đã sẵn sàng |
| **H8** | KB1: Truy cập thành công normal.lab7.test qua Proxy và bị chặn khi tắt Proxy | Đã sẵn sàng |
| **H9** | KB1: Firewall Log pfSense chặn direct web TCP/80 khi tắt proxy (Rule 4) | Đã sẵn sàng |
| **H10** | KB2: Giao diện Hard Block khi truy cập china.lab7.test và mideast.lab7.test | Đã sẵn sàng |
| **H11** | KB2: Log E2Guardian access.log ghi nhận mã chặn *DENIED* Geo-Sim | Đã sẵn sàng |
| **H12** | KB3: Trình duyệt bị chặn Hard Block khi truy cập jobs.lab7.test | Đã sẵn sàng |
| **H13** | KB3: Log E2Guardian access.log ghi nhận chặn tìm kiếm việc làm | Đã sẵn sàng |
| **H14** | KB4: Trang cảnh báo Warning kèm nút Temporary Bypass trên weapons.lab7.test | Đã sẵn sàng |
| **H15** | KB4: Trình duyệt tải thành công sau khi nhấn xác nhận bypass 5 phút | Đã sẵn sàng |
| **H16** | KB5: Trình duyệt tải mượt mà trang education.lab7.test không bị cảnh báo | Đã sẵn sàng |
| **H17** | KB5: Log E2Guardian access.log ghi nhận ngoại lệ *EXCEPTION* | Đã sẵn sàng |
| **H18** | KB6: Cấu hình Schedule LAB7_ENT_60M và truy cập thành công trong giờ | Đã sẵn sàng |
| **H19** | KB6: Trình duyệt bị chặn khi truy cập entertainment ngoài khung giờ | Đã sẵn sàng |
| **H20** | KB7: Cảnh báo băng thông streaming.lab7.test và sau khi mở tạm thời | Đã sẵn sàng |
| **H21** | KB7: Log E2Guardian access.log ghi nhận phiên bypass streaming | Đã sẵn sàng |
| **H22** | KB8: Trình duyệt nhận thông báo chặn Hard Block đối với blockedgame.lab7.test | Đã sẵn sàng |
| **H23** | KB8: Log E2Guardian access.log xác nhận quy tắc chặn game cụ thể | Đã sẵn sàng |
| **H24** | KB9: E2Guardian/ClamAV chặn tải file virus EICAR eicar.com.txt | Đã sẵn sàng |
| **H25** | KB9: Log journalctl ClamAV daemon và E2Guardian phát hiện virus EICAR | Đã sẵn sàng |
| **H26** | KB10: Tải thành công file 100MB.bin qua Squid Proxy | Đã sẵn sàng |
| **H27** | KB10: Bị chặn HTTP 403 Forbidden khi tải file 110MB.bin | Đã sẵn sàng |

### 2. Tài liệu tham khảo
* **[1]** Netgate pfSense Documentation - Firewall Rules, Aliases, Schedules and Captive Portal.
* **[2]** Netgate Official Deprecation Notice: Squid, SquidGuard, and Lightsquid Packages on pfSense.
* **[3]** Ubuntu Server Official Documentation - Ubuntu 24.04 LTS (Noble Numbat) Package Management.
* **[4]** Squid Cache Official Wiki - Configuration Directives and Access Control Lists (ACLs).
* **[5]** E2Guardian Project Wiki - Filtering Groups, Content Scanners and Temporary Bypass Mechanisms.
* **[6]** EICAR - European Institute for Computer Antivirus Research Standard Anti-Malware Testfile.

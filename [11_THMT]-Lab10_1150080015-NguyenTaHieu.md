# BÁO CÁO THỰC HÀNH BÀI SỐ 10
## REMOTE DNS CACHE POISONING TRONG MÔI TRƯỜNG PFSENSE CÔ LẬP
### (KAMINSKY DNS ATTACK SIMULATION & RESOLVER HARDENING)

* **Học phần:** Thực hành An toàn & Bảo mật Hệ thống Thông tin
* **Họ và tên sinh viên:** Nguyễn Tạ Hiếu
* **Mã số sinh viên (MSSV):** 1150080015
* **Lớp:** 11_THMT
* **Tài liệu chuẩn:** LAB10_HuongDan.docx / SEED Labs Remote DNS Cache Poisoning Attack
* **Tệp tài nguyên kèm theo:** Bộ kịch bản mô phỏng Python và mẫu cấu hình BIND9 (`LAB10_Resources`)

---

## I. THÔNG TIN SINH VIÊN VÀ MÔI TRƯỜNG THỰC HÀNH

### 1. Bảng thông số môi trường chuẩn hóa

| Thành phần mạng | Hệ điều hành / Cấu hình IP | Vai trò và Dịch vụ triển khai |
| :--- | :--- | :--- |
| **Firewall (pfSense-LAB6)** | FreeBSD / pfSense CE 2.7.2 (6 vNIC)<br>• **WAN:** `192.168.250.3/24`<br>• **MGMT:** `192.168.56.254/24`<br>• **LAN:** `10.6.0.1/24`<br>• **DMZ:** `172.16.6.1/24`<br>• **DNSLAB:** `10.9.0.1/24`<br>• **ATTACKNET (Mới):** `10.10.0.1/24` | Router biên & Stateful Firewall L3/L4 cô lập môi trường mạng:<br>• Tách Attacker sang phân vùng riêng biệt `attack-net` (`10.10.0.0/24`).<br>• Cấu hình các bộ luật Firewall Policy Matrix phân tách triệt để, không cho Attacker sniff DNSLAB.<br>• Mở tạm UDP 5300 cho race harness nội bộ phòng lab. |
| **DNS Resolver (DNS-SERVER)** | Ubuntu Server 22.04 LTS<br>• **IP DNSLAB:** `10.9.0.10/24`<br>• **Gateway:** `10.9.0.1`<br>• **DNS:** `127.0.0.1` | Máy chủ DNS Resolver phục vụ mạng nội bộ:<br>• **Baseline:** BIND 9.18 cấu hình forward zone `example.lab10.test` sang `172.16.6.20`, bật DNSSEC với ngoại lệ `validate-except`.<br>• **Pha tấn công:** Tạm dừng BIND9 và chạy `vuln_resolver_lab10.py` (mô phỏng resolver cổ điển cố định UDP source port 33333).<br>• **Hardening:** Phục hồi BIND9 hiện đại với source-port randomization và thu hẹp ACL đệ quy. |
| **Nạn nhân (VICTIM)** | Ubuntu Desktop / Server<br>• **IP DNSLAB:** `10.9.0.100/24`<br>• **Gateway:** `10.9.0.1`<br>• **DNS Server:** `10.9.0.10` | Máy trạm client gửi truy vấn DNS hợp lệ tới `10.9.0.10` và kiểm tra chuyển hướng web sau khi bộ nhớ đệm resolver bị đầu độc. |
| **Kẻ tấn công (KaliLinux)** | Kali Linux 2026 (Chuyển mạng)<br>• **IP ATTACKNET:** `10.10.0.200/24`<br>• **Gateway:** `10.10.0.1`<br>• **Promiscuous Mode:** `Deny` | Máy trạm tấn công từ xa (Remote Attacker):<br>• Không thể sniff gói tin trên DNSLAB.<br>• Chạy Evil DNS (`evil_dns_lab10.py`) trên cổng 53 trả về bản ghi độc hại.<br>• Chạy Web Server giả mạo (`index.html`) trên cổng 80.<br>• Chạy script `kaminsky_remote_lab10.py` kích hoạt truy vấn tên miền ngẫu nhiên và đoán TXID qua relay. |
| **Authoritative DNS (AUTH-DNS / APP-TEST)** | Ubuntu Server (DMZ-WEB)<br>• **IP DMZ:** `172.16.6.20/24` (IP Alias)<br>• **Gateway:** `172.16.6.1` | Máy chủ quản lý tên miền hợp lệ `example.lab10.test`:<br>• Trả về bản ghi `www.example.lab10.test -> 172.16.6.10`.<br>• Với các nhãn ngẫu nhiên, tạo trễ 3.0 giây trước khi trả NXDOMAIN để mô phỏng khoảng thời gian race window.<br>• Lắng nghe cổng UDP 5300 (Race Relay) để tiếp nhận các batch đoán TXID của Attacker. |
| **Web Server hợp lệ (DMZ-WEB)** | Ubuntu Server 22.04 LTS<br>• **IP DMZ:** `172.16.6.10/24` | Trang web gốc cung cấp dịch vụ thông thường của bài thực hành. |

---

## II. CHUẨN BỊ MÔ HÌNH VÀ THIẾT LẬP HẠ TẦNG (MỤC C, D, E)

### 1. Quy trình triển khai kỹ thuật hạ tầng

1. **Thêm Card mạng ATTACKNET trên pfSense:**
   - Trong VirtualBox, thiết lập Adapter 6 cho `pfSense-LAB6`: Attached to = `Internal Network`, Name = `attack-net`, Promiscuous Mode = `Deny`.
   - Trên WebGUI pfSense (`https://192.168.56.254`), truy cập **Interfaces > Assignments**, kích hoạt thêm interface mới đặt tên là `ATTACKNET`.
   - Cấu hình IPv4 tĩnh: `10.10.0.1/24`, tuyệt đối không chọn Upstream Gateway (đảm bảo tính chất interface nội bộ, tránh xung đột NAT/routing).
2. **Chuyển máy Kali khỏi DNSLAB sang ATTACKNET:**
   - Tắt card mạng `dns-lab` kế thừa từ bài lab trước; gán card mạng duy nhất vào `attack-net`.
   - Thiết lập Promiscuous Mode = **Deny** trên VirtualBox.
   - Cấu hình IP tĩnh trên Kali: `10.10.0.200/24`, Gateway: `10.10.0.1`.
3. **Cấu hình Firewall Policy Matrix trên pfSense:**
   - **Tab DNSLAB:**
     + Pass: `10.9.0.100` -> `10.9.0.10` (TCP/UDP 53 - Victim gửi DNS tới resolver).
     + Pass: `10.9.0.10` -> `172.16.6.20` (UDP 53 - Resolver gửi truy vấn tới AUTH-DNS hợp lệ).
     + Pass: `10.9.0.10` -> `10.10.0.200` (UDP 53 - Resolver hỏi Evil DNS sau khi bị đầu độc).
     + Pass: `10.9.0.0/24` -> `10.10.0.200` (TCP 80 - Victim truy cập Fake Web).
     + Block + Log: `10.9.0.0/24` sang LAN (`10.6.0.0/24`), MGMT (`192.168.56.0/24`) và Any (cô lập mặc định).
   - **Tab ATTACKNET:**
     + Pass: `10.10.0.200` -> `10.9.0.10` (TCP/UDP 53 - Attacker gửi trigger query tới resolver).
     + Pass: `10.10.0.200` -> `172.16.6.20` (UDP 5300 - Attacker gửi batch TXID tới Race Relay).
     + Block + Log: `10.10.0.0/24` sang LAN, MGMT và Any (chặn tuyệt đối WAN và các vùng khác).
   - Thực hiện **Apply Changes** và **Reset States** trên pfSense.
4. **Triển khai Dịch vụ Authoritative DNS & Race Relay trên DMZ (`172.16.6.20`):**
   - Chạy script [`auth_dns_relay_lab10.py`](file:///C:/Users/Admin/Downloads/LAB10/LAB10_Resources/04_AUTH_DNS_RELAY/auth_dns_relay_lab10.py) bằng quyền root: `sudo python3 auth_dns_relay_lab10.py`.
   - Dịch vụ lắng nghe trên cổng 53 (DNS) và cổng 5300 (Race Relay nhận batch TXID từ `10.10.0.200`).
5. **Kiểm tra Baseline từ DNS-SERVER và VICTIM:**
   - Nạp cấu hình BIND9 baseline (`named.conf.options.baseline` và `named.conf.local.baseline`).
   - Từ VICTIM (`10.9.0.100`), thực hiện truy vấn:
     ```bash
     dig @10.9.0.10 www.example.lab10.test
     ```
   - **Kết quả Baseline đạt chuẩn:** Resolver phân giải thành công ra IP hợp lệ `172.16.6.10` (có độ trễ nhẹ do AUTH-DNS xử lý).
6. **Xác nhận tính chất "Remote" của bài tấn công:**
   - Trên Kali, chạy lệnh bắt gói tin: `sudo tcpdump -i eth0 -n udp port 53`.
   - Trên VICTIM, thực hiện truy vấn `www.example.lab10.test`.
   - **Minh chứng:** Kali hoàn toàn **không bắt được** bất kỳ gói tin nào trao đổi giữa `10.9.0.10` và `172.16.6.20`. Điều này chứng minh Attacker ở trạng thái Blind/Remote, không thể sniff Transaction ID như bài lab tấn công mạng cục bộ.

---

## III. KẾT QUẢ THỰC HIỆN CÁC TÁC VỤ TẤN CÔNG VÀ PHÒNG THỦ

### 1. Task 1: Thực hiện tấn công Remote DNS Cache Poisoning (Kaminsky Simulation)

* **Bản chất kỹ thuật:**
  - Vì BIND 9.18 hiện đại đã mặc định tính năng ngẫu nhiên hóa cổng nguồn (Source-Port Randomization), để tái hiện bản chất kỹ thuật của lỗ hổng Kaminsky trên các phiên bản DNS Server cũ (chỉ ngẫu nhiên hóa Transaction ID 16-bit nhưng cố định cổng nguồn), bài lab sử dụng [`vuln_resolver_lab10.py`](file:///C:/Users/Admin/Downloads/LAB10/LAB10_Resources/01_DNS_SERVER/vuln_resolver_lab10.py).
  - Resolver mô phỏng này bind tại `10.9.0.10:53` và luôn gửi truy vấn đệ quy upstream từ UDP source port cố định `33333`.
* **Các bước tiến hành:**
  1. **Trên DNS-SERVER (`10.9.0.10`):**
     ```bash
     sudo systemctl stop named
     sudo python3 vuln_resolver_lab10.py
     ```
  2. **Trên Kali (`10.10.0.200`):**
     - *Terminal 1 (Evil DNS):* Khởi chạy `sudo python3 evil_dns_lab10.py` (lắng nghe `10.10.0.200:53`).
     - *Terminal 2 (Fake Web):* Khởi chạy `sudo python3 -m http.server 80` phục vụ `index.html`.
     - *Terminal 3 (Tấn công Kaminsky):* Chạy script kích hoạt `python3 kaminsky_remote_lab10.py`.
* **Cơ chế hoạt động của kịch bản:**
  - Kali tạo một tên miền con ngẫu nhiên (ví dụ `r8x9a2k1q0.example.lab10.test`) và gửi query tới resolver `10.9.0.10`.
  - Vì tên miền con này chưa hề có trong bộ nhớ đệm, resolver bắt buộc phải gửi truy vấn đệ quy lên Authoritative DNS `172.16.6.20:53` từ cổng nguồn cố định `33333` với một Transaction ID (TXID) 16-bit ngẫu nhiên.
  - Kali gửi 16 batch (mỗi batch đại diện một khoảng 4096 giá trị TXID) tới race relay trên cổng UDP 5300 của máy `172.16.6.20`.
  - Khi batch chứa đúng TXID của truy vấn đang chờ (Pending), relay lập tức phát một gói tin DNS Response giả mạo từ `172.16.6.20:53` về `10.9.0.10:33333` trước khi phản hồi thật kịp đến.
  - Gói phản hồi giả mạo chứa:
    + **Authority Section:** `example.lab10.test. IN NS ns.evil.example.lab10.test.`
    + **Additional Section (Glue Record):** `ns.evil.example.lab10.test. IN A 10.10.0.200`
* **Ghi nhận nhật ký thực nghiệm (Logs & Evidence):**
  - **Trên AUTH-DNS (`172.16.6.20`):**
    ```text
    [PENDING] r8x9a2k1q0.example.lab10.test txid=38412 resolver=10.9.0.10:33333 delay=3.0s
    [HIT] batch 36864-40959 contains txid=38412; forged delegation -> ns.evil.example.lab10.test 10.10.0.200
    ```
  - **Trên DNS-SERVER (`10.9.0.10`):**
    ```text
    [CACHE] delegation poisoned: example.lab10.test. NS ns.evil.example.lab10.test. -> 10.10.0.200
    ```
  - **Trên Terminal 3 Kali:**
    ```text
    [VERIFY] www.example.lab10.test A = ['10.10.0.200']
    SUCCESS: resolver now follows malicious delegation to attacker DNS
    ```
* **Kiểm tra kết quả từ phía nạn nhân (VICTIM):**
  - Thực hiện truy vấn trên VICTIM: `dig www.example.lab10.test` -> Nhận kết quả phân giải `10.10.0.200`.
  - Mở trình duyệt truy cập `http://www.example.lab10.test`: Hiển thị giao diện cảnh báo giả mạo **"LAB 10 - Fake Site / DNS Cache Poisoning Demonstration"** đặt tại máy chủ web của Kali.

---

### 2. Task 2: Phục hồi BIND9 và Triển khai các biện pháp Hardening

1. **Khôi phục dịch vụ BIND9:**
   - Dừng script `vuln_resolver_lab10.py` trên máy DNS-SERVER (`Ctrl + C`).
   - Khởi động lại dịch vụ BIND9: `sudo systemctl start named` (hoặc `bind9`).
   - Xóa bộ nhớ đệm cũ: `sudo rndc flush`.
2. **Thu hẹp Access Control List (ACL) Recursion:**
   - File cấu hình `named.conf.options.baseline` chỉ cho phép recursion đối với dải mạng tin cậy của Client:
     ```text
     acl "trusted_clients" { 10.9.0.0/24; 127.0.0.1; };
     allow-recursion { trusted_clients; };
     allow-query-cache { trusted_clients; };
     ```
   - Kẻ tấn công trên `ATTACKNET` (`10.10.0.200`) khi gửi truy vấn đệ quy sẽ bị BIND từ chối ngay lập tức với mã `REFUSED`, ngăn chặn việc biến Resolver thành Open Resolver để kích hoạt tấn công.
3. **Kiểm chứng Source-Port Randomization:**
   - Chạy lệnh bắt gói tin trên máy AUTH-DNS hoặc DNS-SERVER:
     ```bash
     sudo tcpdump -i any -n "udp and (dst port 53 or src port 53)"
     ```
   - Từ VICTIM, thực hiện nhiều truy vấn với các tên miền ngẫu nhiên khác nhau.
   - **Kết quả quan sát:** Cổng UDP nguồn phát đi từ BIND9 không còn bị cố định ở `33333` mà liên tục thay đổi ngẫu nhiên trên toàn bộ dải cổng động (Ephemeral Ports, ví dụ: 48291, 53104, 38112...).
4. **Phân tích cơ chế DNSSEC Validation:**
   - BIND9 cấu hình `dnssec-validation auto;`. Đối với các tên miền trên Internet có ký số DNSSEC, mọi phản hồi giả mạo không có chữ ký số RRSIG hợp lệ từ khóa ủy quyền DNSKEY/DS sẽ bị Resolver phát hiện và từ chối với lỗi `SERVFAIL`.
   - Trong môi trường thử nghiệm, do tên miền `.test` không có chữ ký từ Root Zone công cộng, BIND sử dụng chỉ thị `validate-except { "example.lab10.test"; };` để xử lý ngoại lệ cho bài lab.
5. **Dọn dẹp môi trường sau bài thực hành:**
   - Vô hiệu hóa/Xóa rule cho phép cổng UDP 5300 trên pfSense.
   - Đảm bảo card mạng Kali duy trì `Promiscuous Mode = Deny`.

---

## IV. MA TRẬN ĐỐI CHIẾU KẾT QUẢ CÁC GIAI ĐOẠN

| Tiêu chí đánh giá | Trước tấn công (Baseline) | Trong khi tấn công (Poisoned) | Sau phục hồi & Hardening | Đánh giá |
| :--- | :--- | :--- | :--- | :---: |
| **Phân giải FQDN `www.example.lab10.test`** | `172.16.6.10` (Web gốc) | `10.10.0.200` (Attacker IP) | `172.16.6.10` (Web gốc) | **ĐẠT (PASS)** |
| **Máy chủ ủy quyền (Authoritative NS)** | `ns.legit.example.lab10.test` (`172.16.6.20`) | `ns.evil.example.lab10.test` (`10.10.0.200`) | `ns.legit.example.lab10.test` (`172.16.6.20`) | **ĐẠT (PASS)** |
| **Giao diện trang web khi Victim truy cập** | Website hợp lệ trên DMZ | Fake Site ("LAB 10 Demonstration") | Website hợp lệ trên DMZ | **ĐẠT (PASS)** |
| **Cổng nguồn UDP phát truy vấn từ Resolver** | Ngẫu nhiên (Random Ephemeral) | Cố định `33333` (Lỗ hổng mô phỏng) | Ngẫu nhiên (Random Ephemeral) | **ĐẠT (PASS)** |
| **Khả năng sniff gói tin của Attacker Kali** | Không thể sniff (Khác subnet) | Không thể sniff (Đoán TXID từ xa) | Không thể sniff | **ĐẠT (PASS)** |
| **Quyền truy vấn đệ quy của Kali (Recursion)**| Bị hạn chế | Được chấp nhận trên script lab | Bị BIND từ chối (`REFUSED`) | **ĐẠT (PASS)** |

---

## V. TRẢ LỜI CHI TIẾT 12 CÂU HỎI PHÂN TÍCH KỸ THUẬT (MỤC K)

### Câu 1: Vì sao LAB 10 buộc Kali rời DNSLAB và đặt Promiscuous Mode = Deny?
* **Mục tiêu mô hình hóa:** LAB 10 nghiên cứu kỹ thuật **Remote DNS Cache Poisoning (tấn công từ xa)**, phân biệt hoàn toàn với bài tấn công DNS cục bộ (Local Spoofing) ở bài lab trước.
* **Ngăn chặn nghe lén thông tin:** Trong bài lab trước, Kali nằm cùng broadcast domain `dns-lab` với DNS-SERVER và bật Promiscuous Mode `Allow All`, cho phép card mạng thu nhận toàn bộ gói tin DNS, đọc trực tiếp Transaction ID và UDP port của truy vấn đệ quy để giả mạo phản hồi với độ chính xác 100%.
* **Yêu cầu cô lập:** Việc chuyển Kali sang subnet riêng biệt `ATTACKNET` (`10.10.0.0/24`) qua Router/Firewall pfSense và đặt Promiscuous Mode = `Deny` đảm bảo Attacker hoàn toàn "mù" (Blind), không thể sniff bất kỳ gói tin nào giữa DNS-SERVER và Authoritative DNS, buộc Attacker phải thực hiện kỹ thuật đoán xác suất không gian số.

---

### Câu 2: Trong remote poisoning, attacker thiếu thông tin nào so với LAB trước?
Trong kịch bản tấn công từ xa, Attacker thiếu hai thông số quan trọng nhất:
1. **Transaction ID (TXID):** Giá trị định danh 16-bit ngẫu nhiên nằm trong DNS Header của truy vấn mà Resolver gửi tới Authoritative DNS.
2. **UDP Source Port của Resolver:** Cổng nguồn mà Resolver mở để gửi truy vấn ra ngoài (nếu Resolver áp dụng cơ chế ngẫu nhiên hóa cổng).
3. **Thời điểm chính xác gửi truy vấn:** Attacker không biết chính xác mili-giây nào Resolver bắt đầu phát gói tin đi, mà chỉ có thể ước lượng thông qua gói tin trigger do chính mình kích hoạt.

---

### Câu 3: Nếu chỉ có transaction ID 16-bit, xác suất một batch K giá trị chứa TXID đúng là bao nhiêu?
* **Không gian mẫu:** Transaction ID là một trường 16-bit nguyên không dấu, do đó tổng số giá trị TXID khả dĩ là $N = 2^{16} = 65,536$.
* **Công thức xác suất:** Khi Attacker gửi một đợt (batch) gồm $K$ giá trị TXID phân biệt khác nhau để thử đoán một giá trị TXID duy nhất của truy vấn đang chờ, xác suất để giá trị TXID thực tế nằm trong tập hợp $K$ giá trị này là:
  $$P(\text{Trúng TXID}) = \frac{K}{65,536}$$
* **Áp dụng trong bài lab:** Với cấu hình $K = 4,096$ giá trị trong mỗi batch:
  $$P = \frac{4,096}{65,536} = \frac{1}{16} = 6.25\%$$
  Nghĩa là trung bình chỉ cần gửi 16 đợt batch (hoặc chia 16 khoảng) là chắc chắn phủ kín toàn bộ 100% không gian 65,536 giá trị.

---

### Câu 4: Source-port randomization làm tăng không gian đoán như thế nào?
* **Khi cổng nguồn bị cố định (như cổng 33333):**
  - Attacker chỉ cần đoán đúng Transaction ID (16-bit).
  - Không gian tìm kiếm: $S = 2^{16} = 65,536$ trạng thái.
* **Khi cổng nguồn được ngẫu nhiên hóa (Source-Port Randomization):**
  - Hệ điều hành/Resolver chọn ngẫu nhiên một cổng UDP nguồn trong dải Ephemeral Ports (thường từ cổng 1024 hoặc 49152 đến 65535, khoảng $\approx 2^{15}$ đến $2^{16}$ cổng, tương đương $\approx 60,000$ cổng).
  - Để một gói tin DNS phản hồi giả mạo được Resolver chấp nhận, gói tin đó phải **đồng thời khớp cả 2 yếu tố**: đúng Transaction ID VÀ gửi đến đúng cổng UDP nguồn của Resolver.
  - Không gian tìm kiếm tổng cộng mở rộng theo tích Descartes:
    $$S_{\text{total}} = 2^{16} \times N_{\text{ports}} \approx 65,536 \times 60,000 \approx 4 \times 10^9 \text{ (hơn 4 tỷ khả năng)}$$
  - Việc mở rộng không gian tìm kiếm từ hàng chục nghìn lên tới hàng tỷ khả năng khiến kiểu tấn công đoán mò xác suất trong cửa sổ thời gian vài trăm mili-giây của DNS trở nên bất khả thi trên thực tế.

---

### Câu 5: Vì sao dùng tên ngẫu nhiên ở mỗi vòng giúp vượt qua cache effect?
* **Hiện tượng Cache Effect (Hiệu ứng bộ nhớ đệm):** Nếu Attacker cố gắng đầu độc bản ghi `www.example.lab10.test` bằng cách gửi liên tục truy vấn cho chính tên miền này:
  - Ở lần thử đầu tiên, nếu Attacker đoán sai TXID, phản hồi hợp lệ từ Authoritative DNS sẽ tới trước và được Resolver lưu vào cache.
  - Khi đã có cache, Resolver sẽ phục vụ trực tiếp cho các truy vấn sau đó trong suốt thời gian TTL (Time-To-Live, ví dụ 3600 giây) mà **không hề gửi thêm bất kỳ truy vấn đệ quy nào ra ngoài**. Attacker buộc phải đợi hết TTL mới có thể thử lại lần tiếp theo.
* **Giải pháp đột phá của Dan Kaminsky (Tên ngẫu nhiên):**
  - Attacker chủ động tạo ra các tiền tố hoàn toàn ngẫu nhiên và không tồn tại trước tên miền mục tiêu: ví dụ `fake01.example.lab10.test`, `fake02.example.lab10.test`...
  - Vì các tên này không hề có trong cache, Resolver **bắt buộc phải gửi truy vấn đệ quy ra Authoritative DNS ở mỗi lần truy vấn**.
  - Điều này cho phép Attacker tạo ra vô số cơ hội đoán (race window) liên tục mà không bao giờ bị chặn lại bởi hiệu ứng cache.

---

### Câu 6: Bản ghi NS và glue A có vai trò gì trong việc đổi hướng resolver?
* **Mục tiêu tối thượng của tấn công Kaminsky:** Không chỉ đầu độc một bản ghi A đơn lẻ (chỉ chiếm quyền một subdomain ngắn hạn), mà là **chiếm quyền kiểm soát toàn bộ Zone tên miền**.
* **Vai trò của bản ghi NS (Authority Section):** Trong gói tin giả mạo trả về cho tên miền ngẫu nhiên, Attacker chèn bản ghi NS:
  `example.lab10.test. IN NS ns.evil.example.lab10.test.`
  Bản ghi này thông báo cho Resolver biết rằng máy chủ quản lý toàn quyền cho cả zone `example.lab10.test` từ nay là `ns.evil`.
* **Vai trò của bản ghi Glue A (Additional Section):** Do `ns.evil.example.lab10.test` là một tên miền nằm trong chính zone đang ủy quyền, Resolver cần biết ngay địa chỉ IP của máy chủ này để liên lạc mà không rơi vào vòng lặp truy vấn. Attacker cung cấp bản ghi Glue A:
  `ns.evil.example.lab10.test. IN A 10.10.0.200`
* **Kết quả:** Khi Resolver chấp nhận phản hồi, nó lưu bản ghi NS và Glue vào bộ nhớ đệm. Kể từ thời điểm đó, mọi truy vấn đối với **BẤT KỲ** tên miền con nào thuộc `example.lab10.test` (kể cả `www`, `mail`, `vpn`...) đều được Resolver chuyển thẳng tới Evil DNS của Attacker tại `10.10.0.200`.

---

### Câu 7: Vì sao Additional record ngoài bailiwick có thể bị resolver từ chối?
* **Khái niệm Bailiwick (Phạm vi thẩm quyền):** Một máy chủ DNS chỉ có quyền đưa ra thông tin xác thực cho các tên miền thuộc phạm vi quản lý của chính nó.
* **Nguyên tắc an toàn (Bailiwick Rule):**
  - Nếu Authoritative DNS của zone `example.com` trả về một bản ghi trong phần Additional tuyên bố địa chỉ IP của `google.com` hoặc `bank.com`, một Resolver hiện đại tuân thủ quy tắc Bailiwick sẽ ngay lập tức **loại bỏ (Drop)** bản ghi Additional đó vì nó nằm ngoài phạm vi thẩm quyền của `example.com`.
  - Nếu không kiểm tra Bailiwick, kẻ tấn công chỉ cần sở hữu một zone bất kỳ là có thể chèn bản ghi đầu độc cho các tên miền tài chính, ngân hàng toàn cầu.
* **Ứng dụng trong bài lab:** Trong LAB 10, tên máy chủ độc hại được đặt cố tình nằm bên trong zone mục tiêu (`ns.evil.example.lab10.test`) để bản ghi Glue A nằm hợp lệ bên trong phạm vi thẩm quyền (in-bailiwick), nhờ đó Resolver mô phỏng chấp nhận bản ghi.

---

### Câu 8: Tại sao LAB 10 dùng .test thay cho website thật?
* **Tuân thủ tiêu chuẩn RFC 2606 / RFC 6761:** Tên miền đuôi `.test` là Top-Level Domain (TLD) đặc biệt được IETF dành riêng cho mục đích thử nghiệm và kiểm thử phần mềm nội bộ, cam kết không bao giờ được định tuyến hoặc tồn tại trên hệ thống DNS Root toàn cầu.
* **An toàn đạo đức và pháp lý:** Tránh mọi nguy cơ vô tình gửi gói tin tấn công ra Internet, làm ảnh hưởng đến các tổ chức, doanh nghiệp hoặc hạ tầng mạng công cộng.
* **Không làm ô nhiễm bộ nhớ đệm thực tế:** Tránh việc các thiết bị máy tính cá nhân lưu cache sai lệch các tên miền dịch vụ thật (như ngân hàng, mạng xã hội).

---

### Câu 9: Phân biệt tác dụng của DNSSEC validation, recursion ACL và firewall segmentation.

| Cơ chế bảo vệ | Tầng hoạt động | Tác dụng cốt lõi | Cách ngăn chặn tấn công DNS |
| :--- | :---: | :--- | :--- |
| **DNSSEC Validation** | L7 (DNS Protocol) | Xác thực tính toàn vẹn và nguồn gốc dữ liệu bằng chữ ký số bất đối xứng (Cryptographic Authentication). | Resolver kiểm tra chữ ký RRSIG của gói phản hồi với Root Key. Gói tin giả mạo của Attacker không có Private Key để ký hợp lệ nên bị từ chối triệt để (SERVFAIL). |
| **Recursion ACL** | L7 (Resolver Configuration) | Kiểm soát quyền sử dụng dịch vụ phân giải tên miền đệ quy (Access Control). | Chỉ cho phép các dải mạng nội bộ tin cậy (như `10.9.0.0/24`) gửi truy vấn đệ quy; chặn Attacker từ xa kích hoạt các truy vấn mồi để mở race window. |
| **Firewall Segmentation** | L3/L4 (Network Perimeter) | Phân vùng mạng và kiểm soát luồng gói tin dựa trên IP/Port/Interface (Network Isolation). | Cách ly Attacker sang subnet riêng (`10.10.0.0/24`), chặn nghe lén gói tin (Anti-sniffing), chặn kết nối trực tiếp trái phép vào vùng quản trị hoặc cơ sở dữ liệu. |

---

### Câu 10: Vì sao race relay không làm cho LAB 10 trở thành mô hình tấn công Internet thực tế?
* **Mục đích của Race Relay trong Lab:** Đây là một cơ chế hỗ trợ thực hành (Lab Harness). Trong môi trường thực tế, Attacker phải gửi hàng chục ngàn gói tin UDP giả mạo qua đường truyền mạng để "bắn hú họa" tìm kiếm TXID đúng, gây tốn băng thông và có nguy cơ nghẽn mạng ảo VirtualBox. Script relay được đặt tại AUTH-DNS giúp kiểm tra xác suất theo các khoảng 4096 giá trị mà không cần phát sinh bão gói tin (packet flooding).
* **Vì sao không áp dụng được trên Internet thật:**
  - Trên Internet, Attacker **hoàn toàn không thể cài đặt bất kỳ phần mềm nào lên máy chủ Authoritative DNS hợp lệ** của đối tượng mục tiêu để làm "relay".
  - Attacker cũng không có quyền "trao đổi" bí mật để Authoritative DNS trì hoãn trả lời 3.0 giây tạo điều kiện cho mình tấn công.
  - Do đó, kịch bản lab chỉ mô phỏng chính xác về mặt lý thuyết toán học xác suất không gian 16-bit, không phải là một công cụ khai thác (exploit) thực tế trên mạng Internet.

---

### Câu 11: Khi khôi phục BIND, vì sao Kali không nên còn quyền recursion?
* **Ngăn chặn Open Resolver:** Một máy chủ DNS cho phép bất kỳ ai từ Internet gửi truy vấn đệ quy được gọi là Open Resolver.
* **Nguy cơ tấn công DNS Amplification (Khuếch đại DNS):** Kẻ tấn công có thể giả mạo IP nguồn của nạn nhân và gửi các truy vấn dung lượng lớn (như truy vấn DNSSEC `ANY` hoặc `TXT`) tới Open Resolver để khuếch đại lưu lượng DDoS đánh sập nạn nhân.
* **Ngăn chặn kích hoạt Poisoning:** Attacker từ ngoài không thể ép Resolver thực hiện phân giải đệ quy cho các tên miền ngẫu nhiên do chính Attacker chỉ định, từ đó triệt tiêu hoàn toàn khả năng tổ chức các cuộc tấn công kiểu Kaminsky.

---

### Câu 12: Nêu tối thiểu bốn biện pháp hardening resolver production.
1. **Source-Port Randomization (Ngẫu nhiên hóa cổng nguồn UDP):** Cấu hình resolver sử dụng toàn bộ dải cổng động (Ephemeral Ports) ngẫu nhiên cho mỗi truy vấn upstream thay vì cố định cổng nguồn, nâng không gian tìm kiếm lên trên 4 tỷ khả năng.
2. **Kích hoạt DNSSEC Validation toàn diện:** Bật `dnssec-validation auto;` trên BIND9/Unbound để xác thực tính hợp lệ của mọi bản ghi DNS thông qua chuỗi tin cậy (Chain of Trust) từ ICANN Root Zone, vô hiệu hóa hoàn toàn mọi nỗ lực giả mạo dữ liệu.
3. **Giới hạn Recursion ACL nghiêm ngặt:** Chỉ cho phép các subnet client nội bộ thuộc quyền quản lý được phép gửi truy vấn đệ quy (`allow-recursion { local_nets; };`), kiên quyết không cấu hình Open Resolver.
4. **Áp dụng DNS Response Rate Limiting (RRL) & DNS-over-HTTPS/TLS (DoH/DoT):** Triển khai giới hạn tần suất truy vấn để chống brute-force và mã hóa luồng truyền đệ quy lên upstream resolver để bảo vệ tính bí mật và toàn vẹn dữ liệu.

---

## VI. DANH MỤC MINH CHỨNG THỰC NGHIỆM VÀ TÀI LIỆU THAM KHẢO

### 1. Bảng kiểm kê các minh chứng thực nghiệm bắt buộc

| Ký hiệu | Nội dung minh chứng thực nghiệm | Vị trí / Nguồn kiểm chứng | Trạng thái |
| :---: | :--- | :--- | :---: |
| **H1** | Cấu hình Interface ATTACKNET (`10.10.0.1/24`) trên pfSense | pfSense WebGUI: Interfaces > ATTACKNET | **ĐÃ XÁC MINH** |
| **H2** | Ma trận Firewall Rules cô lập trên tab DNSLAB và ATTACKNET | pfSense WebGUI: Firewall > Rules | **ĐÃ XÁC MINH** |
| **H3** | Thiết lập card mạng Kali chỉ gắn `attack-net`, Promisc = Deny | VirtualBox VM Settings: KaliLinux Network | **ĐÃ XÁC MINH** |
| **H4** | Baseline VICTIM phân giải `www.example.lab10.test -> 172.16.6.10` | Terminal VICTIM: `dig @10.9.0.10` | **ĐÃ XÁC MINH** |
| **H5** | Tcpdump trên Kali chứng minh không sniff được DNSLAB | Terminal Kali: `tcpdump -i eth0 udp port 53` | **ĐÃ XÁC MINH** |
| **H6** | Nhật ký AUTH-DNS hiển thị log `[PENDING]` và `[HIT]` | Terminal AUTH-DNS: `auth_dns_relay_lab10.py` | **ĐÃ XÁC MINH** |
| **H7** | Nhật ký DNS-SERVER ghi nhận `[CACHE] delegation poisoned` | Terminal DNS-SERVER: `vuln_resolver_lab10.py` | **ĐÃ XÁC MINH** |
| **H8** | Script tấn công trên Kali in ra kết quả `SUCCESS` | Terminal Kali: `kaminsky_remote_lab10.py` | **ĐÃ XÁC MINH** |
| **H9** | Gói tin bắt được thể hiện truy vấn đệ quy từ UDP port 33333 | Wireshark/Tcpdump trên DNS-SERVER / AUTH-DNS | **ĐÃ XÁC MINH** |
| **H10**| VICTIM phân giải ra `10.10.0.200` và hiển thị trang Fake Site | Trình duyệt & Terminal máy VICTIM | **ĐÃ XÁC MINH** |
| **H11**| Khôi phục BIND9 thành công và kiểm tra trạng thái hoạt động | Terminal DNS-SERVER: `systemctl status named` | **ĐÃ XÁC MINH** |
| **H12**| Kiểm chứng BIND9 ngẫu nhiên hóa cổng nguồn (Random Source Port)| Wireshark/Tcpdump trên DNS-SERVER | **ĐÃ XÁC MINH** |

### 2. Tài liệu tham khảo
* **[1]** SEED Labs - *Remote DNS Cache Poisoning Attack Lab* (Syracuse University, Kevin Du).
* **[2]** ISC BIND 9.18 Administrator Reference Manual - *Query Source Port Randomization, Access Control Lists and DNSSEC Validation*.
* **[3]** Netgate pfSense Documentation - *Static IPv4 Interface Configuration, Firewall Rules and Multi-Subnet Routing Isolation*.
* **[4]** RFC 2606 - *Reserved Top Level DNS Names (.test usage for testing and staging environments)*.
* **[5]** Dan Kaminsky - *Black Hat USA 2008: It's The End Of The Cache As We Know It (Kaminsky Bug DNS Flaw Analysis)*.
* **[6]** RFC 5452 - *Measures for Making DNS More Resilient against Forgery*.

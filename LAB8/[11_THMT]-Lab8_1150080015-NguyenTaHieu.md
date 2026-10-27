# BÁO CÁO THỰC HÀNH BÀI SỐ 8 / SỐ 9
## LOCAL DNS ATTACK: DNS SPOOFING VÀ CACHE POISONING TRONG MẠNG ẢO CÔ LẬP PFSENSE

* **Học phần:** Thực hành An toàn & Bảo mật Hệ thống Thông tin
* **Họ và tên sinh viên:** Nguyễn Tạ Hiếu
* **Mã số sinh viên (MSSV):** 1150080015
* **Lớp chuyên ngành:** 11_THMT
* **Tài liệu chuẩn hóa:** `LAB8_Local_DNS_Attack_pfSense_2026.docx` / `SEED Labs Local DNS`
* **Môi trường ảo hóa:** Oracle VM VirtualBox 7.x, pfSense CE 2.7.2, Ubuntu Server 24.04 LTS, Kali Linux 2026

---

## I. TỔNG QUAN VÀ MÔ HÌNH HỆ THỐNG LAB

Bài thực hành kế thừa toàn bộ hạ tầng mạng pfSense từ các bài Lab trước và mở rộng một phân đoạn mạng ảo độc lập mang tên **DNSLAB (`10.9.0.0/24`)**. Toàn bộ quá trình kiểm thử được thiết lập nhằm khảo sát ba kỹ thuật tấn công DNS kinh điển (dựa trên bài gốc SEED Labs Local DNS Attack) nhưng được hiện đại hóa trên nền tảng **Ubuntu 24.04 LTS**, **Kali Linux 2026** và tường lửa **pfSense CE 2.7.2**.

### 1. Bảng quy hoạch địa chỉ IP và vai trò các máy trong hệ thống

| Thành phần VM | Hệ điều hành / Dịch vụ | Địa chỉ IP | Vai trò và ghi chú |
| :--- | :--- | :--- | :--- |
| **pfSense** | FreeBSD / pfSense 2.7.2 | `10.9.0.1/24` | Gateway & Firewall cô lập mạng DNSLAB (gắn Adapter 5 OPT2 / DNSLAB). |
| **DNS-SERVER** | Ubuntu Server 24.04 LTS | `10.9.0.10` | Recursive & Caching Resolver (BIND9), authoritative cho `lab9.test`. |
| **VICTIM** | Ubuntu 24.04 Desktop/Server | `10.9.0.100` | Máy trạm client, cấu hình trỏ DNS duy nhất về `10.9.0.10`. |
| **ATTACKER** | Kali Linux 2026 | `10.9.0.200` | Máy kẻ tấn công; Promiscuous Mode = **Allow VMs**; chạy Scapy & Fake Web. |
| **APP-TEST** | Ubuntu Server 24.04 LTS | `172.16.6.20` | Authoritative DNS mô phỏng cho `external.lab9.test` (`auth_dns_delayed.py`, delay 1.5s). |
| **DMZ-WEB** | Ubuntu Server (Nginx) | `172.16.6.10` | Web server thật, đích đến hợp lệ của tên miền `www.external.lab9.test`. |
| **HOST MGMT** | Windows 11 (Host OS) | `192.168.56.1` | Máy thật truy cập quản trị WebGUI pfSense qua port `192.168.56.254`. |

---

### 2. Ma trận quy tắc lọc Firewall trên pfSense (Interface DNSLAB)

Giao diện **DNSLAB (`10.9.0.1/24`)** được thiết lập tường lửa nghiêm ngặt: Không cho phép truy cập ra WAN/Internet, không cho phép truy cập vào vùng Quản trị MGMT và LAN nội bộ, chỉ mở đúng luồng phân giải DNS và HTTP tới DMZ:

| Thứ tự | Action | Interface | Source | Destination | Port | Mục đích bảo mật |
| :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **1** | **Pass** | DNSLAB | `10.9.0.10` | `172.16.6.20` | UDP/TCP 53 | DNS-SERVER chuyển tiếp truy vấn đệ quy sang Auth-DNS. |
| **2** | **Pass** | DNSLAB | `10.9.0.0/24` | `172.16.6.10` | TCP 80 | Victim truy cập Web server hợp lệ trong vùng DMZ. |
| **3** | **Block + Log** | DNSLAB | `10.9.0.0/24` | `10.6.0.0/24` | Any | Ngăn chặn tấn công xâm lấn sang mạng LAN-DOMAIN. |
| **4** | **Block + Log** | DNSLAB | `10.9.0.0/24` | `192.168.56.0/24` | Any | Bảo vệ tuyệt đối dải mạng Quản trị Host MGMT. |
| **5** | **Block + Log** | DNSLAB | `10.9.0.0/24` | Any | Any | Cách ly hoàn toàn dải DNSLAB (Chặn toàn bộ WAN trong pha tấn công). |

> ⚠️ **Lưu ý đặc biệt:** Interface `DNSLAB` trên pfSense tuyệt đối **không được gán Upstream Gateway** (nếu gán sẽ bị pfSense coi là WAN và làm hỏng routing/NAT).

#### [HÌNH 1: DÁN ẢNH CHỤP MINH CHỨNG TẠI ĐÂY]
* **Hình:** Cấu hình giao diện DNSLAB và Ruleset Firewall trên pfSense
* **Mô tả:** Chụp WebGUI pfSense tại mục `Interfaces > Assignments` (`OPT2` gán IP `10.9.0.1/24`, không gateway) và tab `Firewall > Rules > DNSLAB` thể hiện 5 rules theo bảng ma trận.

---

## II. QUY TRÌNH THIẾT LẬP VÀ CẤU HÌNH CÁC MÁY TRẢI NGHIỆM

### 1. Cấu hình máy chủ phân giải DNS-SERVER (BIND9 - `10.9.0.10`)

Trên máy ảo **DNS-SERVER** (Ubuntu Server 24.04), gán IP tĩnh `10.9.0.10/24`, gateway `10.9.0.1`. Cài đặt `bind9` và `bind9utils`. Chép các tệp cấu hình chuẩn hóa từ bộ tài nguyên:

```bash
# 1. Sao lưu cấu hình BIND9 mặc định
sudo cp /etc/bind/named.conf.options /etc/bind/named.conf.options.bak
sudo cp /etc/bind/named.conf.local /etc/bind/named.conf.local.bak

# 2. Kiểm tra cú pháp cấu hình và zone sau khi nạp cấu hình chuẩn
sudo named-checkconf
sudo named-checkzone lab9.test /etc/bind/db.lab9.test
sudo named-checkzone 9.10.in-addr.arpa /etc/bind/db.10.9.0

# 3. Khởi động lại dịch vụ và kiểm tra trạng thái hoạt động
sudo systemctl restart bind9
sudo systemctl status bind9 --no-pager
```

#### [HÌNH 2: DÁN ẢNH CHỤP MINH CHỨNG TẠI ĐÂY]
* **Hình:** Kiểm tra tính hợp lệ của cấu hình BIND9 và trạng thái dịch vụ
* **Mô tả:** Lệnh `named-checkconf`, `named-checkzone` trả về `OK` và `systemctl status bind9` hiển thị `Active: active (running)`.

---

### 2. Cấu hình máy nạn nhân VICTIM (`10.9.0.100`)

Cấu hình Netplan cho card mạng `dns-lab` nhận IP `10.9.0.100/24`, gateway `10.9.0.1` và DNS Server duy nhất là `10.9.0.10`:

```yaml
# /etc/netplan/01-victim-dnslab.yaml
network:
  version: 2
  renderer: networkd
  ethernets:
    enp0s3: # hoặc tên interface thực tế (kiểm tra bằng ip link)
      addresses: [10.9.0.100/24]
      routes:
        - to: default
          via: 10.9.0.1
      nameservers:
        addresses: [10.9.0.10]
```

Áp dụng cấu hình:
```bash
sudo netplan apply
resolvectl status
```

---

### 3. Cấu hình máy tấn công Kali Linux (`10.9.0.200`)

Trên Kali Linux, cấu hình IP tĩnh `10.9.0.200/24` trên card `dns-lab`. Đặt card mạng này ở chế độ **`Promiscuous Mode = Allow VMs`** trong VirtualBox:

```bash
# 1. Kiểm tra interface mạng trên Kali
ip -br addr

# 2. Khởi tạo fake web server mô phỏng (port 80)
sudo python3 -m http.server 80 --directory /home/kali/LAB9_Resources/03_ATTACKER/

# 3. Kiểm tra Scapy sẵn sàng
python3 -c "import scapy; print('Scapy version:', scapy.__version__)"
```

---

### 4. Khởi động Authoritative DNS mô phỏng trên APP-TEST (`172.16.6.20`)

Trên máy **APP-TEST** (`172.16.6.20`), chạy script `auth_dns_delayed.py` với độ trễ 1.5 giây để mô phỏng độ trễ mạng thực tế và tạo race window:

```bash
sudo python3 auth_dns_delayed.py --bind 172.16.6.20 --delay 1.5
# Kết quả hiển thị: LAB9 AUTH-DNS listening 172.16.6.20:53, delay=1.5s, target=www.external.lab9.test. -> 172.16.6.10
```

#### [HÌNH 3: DÁN ẢNH CHỤP MINH CHỨNG TẠI ĐÂY]
* **Hình:** Kiểm tra địa chỉ IP trên các máy và khởi động Authoritative DNS
* **Mô tả:** Lệnh `ip a` / `ip route` từ cả 3 máy: DNS-SERVER (`10.9.0.10`), VICTIM (`10.9.0.100`), ATTACKER (`10.9.0.200`) và terminal đang chạy `auth_dns_delayed.py` trên `172.16.6.20`.

---

## III. KIỂM TRA BASELINE TRƯỚC TẤN CÔNG

Chứng minh hệ thống phân giải tên miền hoạt động hoàn toàn chính xác theo thiết kế ban đầu:

```bash
# 1. Từ máy Victim (10.9.0.100), kiểm tra phân giải tên miền
dig www.external.lab9.test
curl -I http://www.external.lab9.test

# 2. Trên Kali (10.9.0.200), mở Wireshark với filter:
dns.qry.name == "www.external.lab9.test"
```

* **Kết quả quan sát được:**
  - Bản ghi A của `www.external.lab9.test` trỏ về đúng Web server hợp lệ `172.16.6.10`.
  - Lệnh `curl` nhận phản hồi `HTTP/1.1 200 OK` từ trang web DMZ thật.
  - Wireshark trên Kali bắt được đúng luồng truy vấn hợp lệ.

#### [HÌNH 4: DÁN ẢNH CHỤP MINH CHỨNG TẠI ĐÂY]
* **Hình:** Kết quả phân giải Baseline và gói tin bắt trên Wireshark
* **Mô tả:** Terminal Victim chạy `dig www.external.lab9.test` trả về `172.16.6.10` và Wireshark trên Kali ghi nhận truy vấn DNS bình thường.

---

## IV. THỰC HIỆN TASK 1 - CHUYỂN HƯỚNG BẰNG LOCAL HOSTS FILE

### 1. Kịch bản & Thao tác
Mô phỏng trường hợp máy Victim đã bị can thiệp file `/etc/hosts` cục bộ nhằm điều hướng tên miền sang IP độc hại:

```bash
# Bước 1: Sao lưu file /etc/hosts trên Victim
sudo cp /etc/hosts /etc/hosts.bak

# Bước 2: Thêm ánh xạ giả mạo trỏ www.lab9.test về Kali 10.9.0.200
echo "10.9.0.200 www.lab9.test" | sudo tee -a /etc/hosts

# Bước 3: Kiểm tra hành vi của các ứng dụng mạng thông thường
getent hosts www.lab9.test
curl http://www.lab9.test

# Bước 4: Kiểm tra bằng công cụ chuyên dụng dig
dig @10.9.0.10 www.lab9.test

# Bước 5: Khôi phục lại file /etc/hosts sau thử nghiệm
sudo cp /etc/hosts.bak /etc/hosts
```

### 2. Đánh giá và phân tích cơ chế
* **Hiện tượng:** Lệnh `curl` và trình duyệt web hiển thị trang web giả mạo của Kali (`10.9.0.200`). Nhưng `dig @10.9.0.10 www.lab9.test` vẫn trả về địa chỉ IP chuẩn `172.16.6.10`.
* **Giải thích kỹ thuật:** Hệ điều hành Linux sử dụng cơ chế **Name Service Switch** (quy định trong `/etc/nsswitch.conf` với dòng `hosts: files dns`). Các ứng dụng thông thường sử dụng hàm thư viện `getaddrinfo()` nên kiểm tra tệp cục bộ `/etc/hosts` trước. Trong khi đó, `dig` gửi truy vấn trực tiếp qua socket mạng tới port 53 của nameserver chỉ định, bỏ qua hoàn toàn file `/etc/hosts`.

#### [HÌNH 5: DÁN ẢNH CHỤP MINH CHỨNG TẠI ĐÂY]
* **Hình:** Minh chứng Task 1 - curl chuyển hướng sang Kali nhưng dig vẫn trả IP gốc
* **Mô tả:** Terminal hiển thị song song `curl http://www.lab9.test` nhận web fake của Kali `10.9.0.200`, trong khi `dig @10.9.0.10 www.lab9.test` trả về `172.16.6.10`.

---

## V. THỰC HIỆN TASK 2 - DIRECT DNS SPOOFING (GIẢ MẠO PHẢN HỒI TỚI CLIENT)

### 1. Kịch bản & Thao tác
Attacker nằm cùng phân đoạn mạng LAN (DNSLAB), lắng nghe (sniff) truy vấn từ Victim gửi tới DNS-SERVER `10.9.0.10` và nhanh chóng tạo gói DNS Response giả gửi trực tiếp cho Victim:

```bash
# Bước 1: Trên DNS-SERVER, xóa sạch cache
sudo rndc flush

# Bước 2: Trên Kali (10.9.0.200), chạy script giả mạo ở chế độ 'victim'
# (thay eth1 bằng tên card mạng DNS-LAB thực tế trên máy Kali)
sudo python3 dns_spoof_lab9.py --mode victim --iface eth1

# Bước 3: Trên Victim (10.9.0.100), gửi truy vấn mới
dig www.external.lab9.test
curl http://www.external.lab9.test
```

### 2. Phân tích gói tin và điều kiện chấp nhận trên Wireshark
Để Victim chấp nhận gói tin giả mạo, phản hồi của Attacker phải khớp chính xác toàn bộ bộ thông số:
* **IP Header:** Source IP giả mạo `10.9.0.10`, Destination IP là `10.9.0.100`.
* **UDP Header:** Source Port `53`, Destination Port trùng với Ephemeral Port mà Victim dùng khi gửi truy vấn.
* **DNS Header:** **Transaction ID (16-bit)** trùng khớp hoàn toàn với gói Query của Victim; cờ `QR=1` (Response), `AA=0`, `RA=1`.
* **DNS Answer:** Bản ghi A trỏ `www.external.lab9.test` về `10.9.0.200`.
* **Cuộc đua thời gian (Race Condition):** Gói giả từ Kali gửi qua LAN L2 chỉ mất vài ms, đến trước phản hồi hợp lệ (vốn bị hoãn 1.5s trên AUTH-DNS). Client tiếp nhận gói giả đầu tiên và drop gói hợp lệ đến sau.

#### [HÌNH 6: DÁN ẢNH CHỤP MINH CHỨNG TẠI ĐÂY]
* **Hình:** Phân tích Wireshark Task 2 - Cặp gói Query và Forged Response
* **Mô tả:** Wireshark trên Kali lọc `dns`: Thấy rõ gói Query (ID=0xXXXX) từ Victim, gói Forged Response mang cùng ID=0xXXXX trả về IP `10.9.0.200` đến trước, và gói Legitimate Response đến sau.

---

## VI. THỰC HIỆN TASK 3 - DNS CACHE POISONING (ĐẦU ĐỘC CACHE BIND9)

### 1. Kịch bản & Thao tác
Attacker nhắm thẳng vào máy chủ phân giải trung tâm **DNS-SERVER (`10.9.0.10`)**. Bằng cách giả mạo phản hồi từ Authoritative DNS (`172.16.6.20`) trả về cho BIND9, kẻ tấn công cài cắm bản ghi độc hại vào cache của resolver:

```bash
# Bước 1: Trên DNS-SERVER, flush sạch cache
sudo rndc flush

# Bước 2: Trên Kali, chạy script giả mạo ở chế độ 'resolver'
sudo python3 dns_spoof_lab9.py --mode resolver --iface eth1

# Bước 3: Trên máy Victim, tạo truy vấn kích hoạt cơ chế đệ quy
dig www.external.lab9.test

# Bước 4: Sau khi Kali thông báo đã gửi forged resolver response, DỪNG script trên Kali (Ctrl+C)!

# Bước 5: Trên Victim, kiểm tra lại khi script Attacker ĐÃ DỪNG HOÀN TOÀN
dig www.external.lab9.test
curl http://www.external.lab9.test

# Bước 6: Trên DNS-SERVER, kết xuất (dump) bộ nhớ cache ra file
sudo rndc dumpdb -cache
sudo grep -A 5 "www.external.lab9.test" /var/cache/bind/named_dump.db
```

### 2. Đánh giá kết quả và ý nghĩa của trường TTL
* **Kết quả:** Khi script trên Kali đã dừng, Victim query lại vẫn nhận IP `10.9.0.200` được trả về trực tiếp từ cache của BIND9!
* **Ý nghĩa TTL (120s):** Bản ghi độc hại được BIND9 lưu giữ và phục vụ cho toàn bộ client trong mạng trong suốt 120 giây mà không cần truy vấn ra ngoài.

#### [HÌNH 7: DÁN ẢNH CHỤP MINH CHỨNG TẠI ĐÂY]
* **Hình:** Minh chứng Task 3 - Dump cache BIND9 và Victim nhận IP giả khi script đã tắt
* **Mô tả:** Terminal DNS-SERVER hiển thị lệnh `grep` bản ghi `www.external.lab9.test` trong `/var/cache/bind/named_dump.db` chứa IP `10.9.0.200`, kèm màn hình Victim chạy `dig` nhận kết quả `10.9.0.200` khi Kali đã ngắt script.

---

## VII. KHÔI PHỤC HỆ THỐNG VÀ THỰC NGHIỆM PHÒNG THỦ (HARDENING)

### 1. Quy trình khôi phục
```bash
# 1. Dừng mọi tiến trình tấn công và fake web trên Kali
sudo pkill -f dns_spoof_lab9.py
sudo pkill -f http.server

# 2. Xóa sạch bộ đệm độc hại trên DNS-SERVER
sudo rndc flush
sudo rndc reload

# 3. Khôi phục máy trạm Victim
sudo resolvectl flush-caches

# 4. Trả card mạng VirtualBox của Kali về: Promiscuous Mode = Deny
```

### 2. Thử nghiệm phòng vệ DNS COOKIE (RFC 7873)
* **Thao tác:** Xóa bỏ khối lệnh `server 172.16.6.20 { send-cookie no; };` trong `/etc/bind/named.conf.options`, sau đó `sudo systemctl restart bind9`. Chạy lại kịch bản Task 3 trên Kali.
* **Kết quả:** BIND9 yêu cầu và kiểm tra Cookie trong gói tin phản hồi. Gói tin giả mạo của script không có cookie hợp lệ nên bị BIND9 loại bỏ; cuộc tấn công Cache Poisoning bị vô hiệu hóa hoàn toàn!

#### [HÌNH 8: DÁN ẢNH CHỤP MINH CHỨNG TẠI ĐÂY]
* **Hình:** Khôi phục hệ thống thành công và cấu hình phòng thủ DNS COOKIE
* **Mô tả:** Sau khi flush cache, `dig www.external.lab9.test` trả về đúng `172.16.6.10`; cấu hình VirtualBox của Kali đưa Promiscuous Mode về `Deny`.

---

## VIII. TRẢ LỜI ĐẦY ĐỦ 10 CÂU HỎI PHÂN TÍCH (MỤC L)

### Câu 1: Tại sao `/etc/hosts` có thể ảnh hưởng trình duyệt nhưng `dig @DNS-SERVER` vẫn trả kết quả DNS gốc?
**Trả lời:**
Nguyên nhân nằm ở sự khác biệt trong cơ chế phân giải tên miền của hai lớp ứng dụng:
* Trình duyệt web và các ứng dụng tiêu chuẩn trên hệ điều hành Linux (như `curl`, `ping`, `wget`) sử dụng thư viện hệ thống `glibc` thông qua các hàm tiêu chuẩn như `getaddrinfo()`. Luồng phân giải này bị chi phối bởi tệp cấu hình Name Service Switch (`/etc/nsswitch.conf`), trong đó chỉ thị `hosts: files dns` quy định ưu tiên đọc tệp cục bộ `/etc/hosts` trước, nếu không tìm thấy mới gửi truy vấn tới máy chủ DNS.
* Ngược lại, công cụ `dig` (Domain Information Groper) là tiện ích chẩn đoán mạng chuyên dụng được phát triển bởi ISC. Lệnh `dig @DNS-SERVER` chủ động bỏ qua hoàn toàn hệ thống NSSwitch của OS và tệp `/etc/hosts`; nó trực tiếp mở một socket UDP tới cổng 53 của máy chủ DNS được chỉ định để gửi truy vấn giao thức DNS thuần túy và hiển thị đúng dữ liệu mà máy chủ DNS trả về.

---

### Câu 2: Những trường nào của DNS/UDP phải khớp để một phản hồi giả có cơ hội được chấp nhận?
**Trả lời:**
Để một gói phản hồi DNS giả mạo (spoofed response) được phía nhận (Client hoặc Recursive Resolver) chấp nhận, gói tin phải trùng khớp tuyệt đối ở cả tầng Giao vận (Transport Layer) và tầng Ứng dụng (Application Layer):
1. **Tầng IP:** IP nguồn (Source IP) phải trùng với IP của máy chủ DNS/Authoritative server mà client vừa gửi hỏi; IP đích (Destination IP) phải là IP của máy gửi truy vấn.
2. **Tầng UDP:** Cổng nguồn (UDP Source Port) phải là 53; Cổng đích (UDP Destination Port) phải khớp chính xác với cổng ngẫu nhiên (Ephemeral Source Port) mà bên gửi truy vấn đã khởi tạo.
3. **Tầng DNS - Transaction ID (16-bit):** Trường ID trong DNS Header phải trùng khớp tuyệt đối với Transaction ID của gói truy vấn vừa gửi đi (xác suất đoán ngẫu nhiên nếu không sniff được là $1/65536$).
4. **Tầng DNS - Query Section:** Tên miền truy vấn (`qname`), loại bản ghi (`qtype`, ví dụ A), và lớp bản ghi (`qclass`, IN) phải trùng khớp với câu hỏi ban đầu.
5. **DNS Flags:** Cờ QR phải bằng 1 (Response), mã trả lời (RCODE) phải hợp lệ (thường là `NoError = 0`).

---

### Câu 3: Tại sao Task 2 chỉ ảnh hưởng Victim hiện tại, trong khi Task 3 có thể ảnh hưởng nhiều client dùng cùng resolver?
**Trả lời:**
Sự khác biệt cốt lõi nằm ở vị trí thực hiện tấn công và đối tượng tiếp nhận gói tin giả:
* **Trong Task 2 (Direct DNS Spoofing):** Kẻ tấn công giả mạo gói tin trả lời trực tiếp cho máy trạm Victim (`10.9.0.100`). Gói tin này chỉ đi vào socket đang chờ của ứng dụng trên máy Victim và không được lưu trữ tập trung cho mạng. Do đó, chỉ duy nhất máy Victim đó bị dẫn hướng sai lệch, các máy tính khác trong mạng không bị ảnh hưởng.
* **Trong Task 3 (DNS Cache Poisoning):** Kẻ tấn công đầu độc vào bộ nhớ đệm (RAM Cache) của máy chủ phân giải tập trung DNS-SERVER (`10.9.0.10`). Bởi vì tất cả các máy trạm trong phân đoạn mạng đều trỏ DNS về máy chủ này, nên khi bộ đệm của resolver đã bị nhiễm độc, bất kỳ máy tính nào gửi yêu cầu phân giải tên miền đó đều sẽ được DNS-SERVER phục vụ trực tiếp bản ghi độc hại từ cache cho đến khi hết hạn TTL. Cuộc tấn công vì vậy có tính chất diện rộng (One-to-Many).

---

### Câu 4: Vai trò của TTL (Time-To-Live) trong cache poisoning là gì?
**Trả lời:**
TTL đóng vai trò là "thời gian sống" và thời hạn duy trì hiệu lực của cuộc tấn công:
* **Xác định thời gian tồn tại trong cache:** TTL quy định khoảng thời gian (tính bằng giây) mà máy chủ resolver được phép lưu trữ và tái sử dụng bản ghi DNS mà không cần phải thực hiện truy vấn đệ quy ra bên ngoài.
* **Tối đa hóa hiệu quả tấn công:** Trong cuộc tấn công Cache Poisoning, nếu kẻ tấn công cài đặt giá trị TTL lớn, bản ghi độc hại sẽ tồn tại lâu dài trong bộ nhớ của resolver. Kẻ tấn công chỉ cần thắng cuộc đua một lần duy nhất là có thể duy trì việc điều hướng giả mạo liên tục trong nhiều phút hoặc nhiều giờ mà không cần tiếp tục phát gói tin giả, giảm thiểu nguy cơ bị hệ thống IDS/IPS phát hiện.

---

### Câu 5: Tại sao VirtualBox Promiscuous Mode = Allow VMs cần thiết trong bài này?
**Trả lời:**
* Mặc định trong cơ chế ảo hóa mạng nội bộ (Internal Network) của VirtualBox, switch ảo hoạt động tương tự như một switch vật lý L2 có lọc địa chỉ MAC: Mỗi card mạng ảo chỉ nhận được các khung tin có MAC đích là chính nó hoặc các khung tin broadcast/multicast.
* Trong bài lab này, Attacker (`10.9.0.200`) thực hiện kỹ thuật tấn công on-path sniffing đối với các gói tin unicast trao đổi trực tiếp giữa Victim (`10.9.0.100`) và DNS-SERVER (`10.9.0.10`). Nếu để chế độ mặc định (`Deny`), Kali sẽ không thể nhìn thấy lưu lượng unicast này, dẫn đến không thể đọc trộm được Transaction ID và UDP Port để tạo phản hồi giả.
* Do đó, cần bật **`Promiscuous Mode = Allow VMs`** để card mạng của Kali chuyển sang chế độ hỗn tạp, cho phép switch ảo chuyển tiếp toàn bộ các gói tin unicast giữa các VM tới giao diện mạng của Kali.

---

### Câu 6: Vì sao LAB 9 dùng mạng DNSLAB riêng thay vì tấn công trực tiếp DC01 DNS của LAN-DOMAIN?
**Trả lời:**
* **Đảm bảo tính sẵn sàng và toàn vẹn của hạ tầng (Availability & Integrity):** Máy chủ DC01 (`10.6.0.10`) đóng vai trò Domain Controller cốt lõi quản lý Active Directory, chính sách GPO, Kerberos và phân giải tên miền cho toàn bộ hệ thống máy trạm Windows của LAB 6, LAB 7 và LAB 8. Tấn công đầu độc cache trực tiếp vào DC01 có thể làm gián đoạn toàn bộ quá trình xác thực domain, hỏng kết nối của các dịch vụ khác và khiến sinh viên phải cài lại môi trường từ đầu.
* **Nguyên tắc cô lập an toàn (Containment):** Việc tạo phân đoạn DNSLAB độc lập (`10.9.0.0/24`) phía sau pfSense giúp cô lập triệt để mọi hành vi phát sinh mã giả mạo, đảm bảo môi trường thử nghiệm hoàn toàn kiểm soát được và an toàn tuyệt đối.

---

### Câu 7: Khác nhau giữa Authoritative DNS, Recursive Resolver và Caching Resolver?
**Trả lời:**
1. **Authoritative DNS (Máy chủ có thẩm quyền):**
   - Nắm giữ cơ sở dữ liệu gốc (zone files) chứa các bản ghi DNS chính thức do quản trị viên cấu hình cho một tên miền cụ thể.
   - Chỉ trả lời dứt khoát về các tên miền mà nó quản lý; không đi hỏi giùm các máy chủ khác.
2. **Recursive Resolver (Máy chủ giải đệ quy):**
   - Đóng vai trò trung gian phục vụ yêu cầu của client: Khi nhận được yêu cầu cho một tên miền lạ, nó thay mặt client chủ động đi hỏi lần lượt từ Root DNS (`.`), TLD DNS (`.test`, `.com`) cho đến Authoritative DNS để tìm câu trả lời cuối cùng.
3. **Caching Resolver (Máy chủ đệm):**
   - Là chức năng thường được tích hợp cùng Recursive Resolver: Sau khi tìm được câu trả lời từ Authoritative server, nó lưu tạm kết quả đó vào bộ nhớ RAM trong khoảng thời gian TTL để tái sử dụng ngay cho các truy vấn tiếp theo mà không cần hỏi lại mạng ngoài.

---

### Câu 8: DNSSEC giải quyết vấn đề gì và vì sao zone .test unsigned trong LAB 9 không tự được bảo vệ?
**Trả lời:**
* **DNSSEC (Domain Name System Security Extensions)** giải quyết triệt để vấn đề xác thực nguồn gốc (Origin Authentication) và tính toàn vẹn dữ liệu (Data Integrity) của các bản ghi DNS. Nó sử dụng chữ ký số mật mã khóa công khai: Mỗi tập bản ghi (RRset) đều được ký bằng khóa riêng tạo thành bản ghi chữ ký `RRSIG`; client/resolver sử dụng khóa công khai `DNSKEY` để xác thực chuỗi ủy thác (Chain of Trust) từ Root xuống.
* Trong LAB 9, domain thử nghiệm `external.lab9.test` là vùng chưa được ký số (unsigned zone) và phân cấp giả lập không có chuỗi ủy thác hợp lệ từ Root zone. Khi một zone là unsigned, cơ chế xác thực DNSSEC (`dnssec-validation auto`) trên BIND9 xem zone đó ở trạng thái "Insecure" và chấp nhận bản ghi DNS thông thường mà không thể kiểm tra chữ ký, do đó phản hồi giả mạo của Attacker vẫn được chấp nhận bình thường.

---

### Câu 9: Tại sao chỉ chặn UDP/53 là chưa đủ nếu muốn kiểm soát DNS trong mạng hiện đại?
**Trả lời:**
Trong môi trường mạng hiện đại, việc chỉ kiểm soát cổng UDP 53 là hoàn toàn không đủ vì:
1. **DNS qua TCP (TCP/53):** Giao thức DNS tiêu chuẩn tự động chuyển sang sử dụng TCP cổng 53 khi kích thước gói tin phản hồi vượt quá giới hạn UDP (thường là trên 512 bytes hoặc với các gói tin DNSSEC có mang chữ ký số lớn) và trong các tiến trình truyền dữ liệu vùng (Zone Transfer - AXFR/IXFR).
2. **DoT (DNS over TLS - RFC 7858):** Toàn bộ lưu lượng truy vấn DNS được mã hóa bằng TLS và hoạt động trên cổng chuyên dụng **TCP 853**, ngăn chặn hoàn toàn việc giám sát nội dung nếu không có biện pháp can thiệp chuyên sâu.
3. **DoH (DNS over HTTPS - RFC 8484):** Các truy vấn DNS được đóng gói dưới dạng thông điệp HTTP/2 hoặc HTTP/3 trên cổng tiêu chuẩn **TCP 443** (hoàn toàn trùng lẫn với lưu lượng duyệt web HTTPS thông thường). Người dùng hoặc mã độc có thể dễ dàng cấu hình DoH để vượt qua tường lửa kiểm soát cổng 53 truyền thống.

---

### Câu 10: Nêu tối thiểu bốn biện pháp hardening sau khi hoàn thành bài?
**Trả lời:**
Bốn biện pháp củng cố và bảo mật hệ thống (Hardening) then chốt bao gồm:
1. **Triển khai DNS COOKIE (RFC 7873):** Bật cơ chế DNS Cookie giữa Resolver và Authoritative Server nhằm tạo mã xác thực nguồn gốc nhẹ 64-bit/128-bit, ngăn chặn triệt để kỹ thuật giả mạo địa chỉ IP (IP Spoofing) và tấn công khuếch đại DNS.
2. **Triển khai ký số DNSSEC toàn diện:** Thực hiện ký số cho các zone nội bộ và bật kiểm tra xác thực DNSSEC bắt buộc trên các Recursive Resolver, giúp loại bỏ các bản ghi giả mạo do không có chữ ký RRSIG hợp lệ.
3. **Thiết lập kiểm soát chuyển tiếp DNS ra ngoài (DNS Egress Filtering):** Trên tường lửa pfSense, chặn toàn bộ lưu lượng UDP/TCP cổng 53, 853 và dải IP của các nhà cung cấp DoH công cộng từ các máy trạm; bắt buộc mọi client nội bộ phải gửi truy vấn qua máy chủ DNS Resolver được quản trị ủy quyền của doanh nghiệp.
4. **Phân đoạn mạng (Network Segmentation) và chống nghe lén (Anti-Sniffing):** Tách riêng dải mạng của máy trạm và dải mạng của máy chủ dịch vụ; trên switch mạng cấu hình Port Security, DHCP Snooping và Dynamic ARP Inspection (DAI), vô hiệu hóa cổng promiscuous mode trên hạ tầng ảo hóa nhằm triệt tiêu hoàn toàn khả năng nghe lén gói tin.

---

## IX. TỔNG KẾT VÀ KẾT LUẬN

Qua bài thực hành LAB 8 / LAB 9 (Local DNS Attack), sinh viên đã trực tiếp triển khai và làm chủ các kỹ thuật cốt lõi:
1. Nắm vững cơ chế vận hành của hệ thống phân giải tên miền BIND9 trong mô hình mạng doanh nghiệp cô lập bảo vệ bởi tường lửa pfSense.
2. Thực nghiệm thành công ba kỹ thuật tấn công DNS: Tác động cục bộ bằng file hosts, tấn công giả mạo phản hồi trực tiếp (Direct Spoofing) dựa trên điểm yếu thời gian và bắt gói tin, và tấn công đầu độc bộ đệm (DNS Cache Poisoning) nhắm vào máy chủ resolver trung tâm.
3. Hiểu rõ bản chất kỹ thuật của các cơ chế phòng thủ hiện đại: Tác dụng ngăn chặn của DNS COOKIE, tầm quan trọng của việc ngẫu nhiên hóa cổng/Transaction ID, vai trò của chữ ký số DNSSEC, và sự cần thiết của chính sách cô lập mạng ở cả Layer 2 (ngăn sniffing) lẫn Layer 3/4 (Firewall DNS Egress Filtering).

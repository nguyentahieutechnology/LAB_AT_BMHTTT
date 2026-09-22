# BÁO CÁO BÀI THỰC HÀNH SỐ 3
## NHẬN DIỆN VÀ ỨNG PHÓ CÁC MỐI ĐE DỌA ĐẾN AN TOÀN THÔNG TIN
*(Identifying and Responding to Information Security Threats)*

---

## I. THÔNG TIN SINH VIÊN VÀ MÔI TRƯỜNG THỰC HÀNH

* **Họ và tên:** Nguyễn Tạ Hiếu
* **Mã số sinh viên (MSSV):** 1150080015
* **Lớp:** 11_THMT
* **Học phần:** Thực hành An toàn & Bảo mật Hệ thống Thông tin (AN HTTT)
* **Năm học:** 2026 – 2027
* **Kho lưu trữ (GitHub Repository):** `LAB_AT_BMHTTT/LAB3`

### 1. Thông số môi trường thực hành
| Thành phần | Phiên bản / Cấu hình | Vai trò trong bài Lab |
| :--- | :--- | :--- |
| **Nền tảng ảo hóa** | VMware Workstation Pro 17.6.4 | Chạy môi trường máy ảo, chế độ mạng `Host-only`, hỗ trợ Snapshot khôi phục sạch. |
| **Hệ điều hành máy ảo (VM)** | Windows Server 2022 Standard x64 | Đóng vai trò máy trạm / máy chủ mục tiêu (4 vCPU, 4GB RAM). |
| **Endpoint Protection** | Microsoft Defender Antivirus | Real-time Protection & Tamper Protection luôn bật. |
| **Shell điều khiển** | Windows PowerShell 5.1 (Administrator) | Thực thi các tập lệnh giám sát, tạo mốc thời gian và xử lý sự kiện. |
| **Sysinternals Suite** | Sysmon v15.22, Autoruns v14.3, Process Explorer v17.14 | Giám sát Process, Registry, phát hiện cơ chế khởi động ngầm (Persistence). |
| **Bắt gói tin mạng** | Wireshark 4.6.8 + Npcap | Bắt và phân tích gói tin HTTP (Plaintext) và HTTPS (TLS/443). |
| **Môi trường chạy Script** | Python 3.11 / 3.14 | Chạy máy chủ HTTP cục bộ và script kiểm thử tải nội bộ (`127.0.0.1:8080`). |
| **Gói dữ liệu Lab** | `LAB3_Threats_Assets.zip` | Cung cấp dataset ddos, mailbomb, mẫu email phishing và ca social engineering. |

---

## II. CÁCH DỰNG MÔI TRƯỜNG VÀ QUẢN LÝ BẰNG CHỨNG

1. **Khởi tạo thư mục làm việc:**
   ```powershell
   $Lab = 'C:\LAB3'
   New-Item -ItemType Directory -Force "$Lab\Evidence", "$Lab\Tools", "$Lab\Downloads", "$Lab\Assets" | Out-Null
   Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz' | Out-File "$Lab\Evidence\start_time.txt"
   ```
2. **Cài đặt & nạp dữ liệu:**
   * Nạp gói tài nguyên vào `C:\LAB3\lab3_assets`.
   * Cài đặt Sysmon với cấu hình `sysmon-lab.xml` (Schema 4.90).
3. **Quy tắc bảo đảm an toàn:**
   * Không tắt Real-time Protection của Defender; không tạo thư mục loại trừ (exclusion).
   * Mọi dịch vụ kiểm thử chỉ lắng nghe duy nhất trên địa chỉ loopback `127.0.0.1:8080`.
   * Toàn bộ tệp bằng chứng sau bài lab đều được băm kiểm tra toàn vẹn bằng thuật toán SHA-256.

---

## III. KẾT QUẢ THỰC HIỆN CÁC TÌNH HUỐNG THỰC HÀNH (TH1 – TH7)

### 1. Tình huống 1 (TH1): Thiết lập Baseline và Bảng Quản trị Rủi ro (Risk Register)
* **Mục tiêu:** Lấy mốc cấu hình hệ thống ban đầu và phân loại 5 nguồn đe dọa chính.
* **Kết quả:** **PASS**
* **Minh chứng:** `baseline_os.txt`, `baseline_defender.txt`, `baseline_firewall.txt`, `baseline_network.txt`, `baseline_processes.txt`.
* **Ảnh minh chứng:** 
  * `H1_VM_WindowsVersion.png`: Chụp cửa sổ VMware Workstation và bảng `winver`.
  * `H3_Baseline_Defender_Firewall.png`: Chụp kết quả `Get-MpComputerStatus` và `Get-NetFirewallProfile`.

#### Bảng Quản trị Rủi ro (Risk Register):
| Tài sản (Asset) | Điểm yếu (Vulnerability) | Mối đe dọa (Threat) | Rủi ro (Risk) | Biện pháp kiểm soát (Control) |
| :--- | :--- | :--- | :--- | :--- |
| **Thông tin xác thực tài khoản** | Mật khẩu ngắn, dễ đoán, nhập vào ứng dụng không tin cậy. | Dò mật khẩu (Brute-force), Keylogging, Phishing. | Chiếm quyền điều khiển tài khoản quản trị. | Đặt mật khẩu mạnh, bật MFA, bảo vệ endpoint, bật Audit log. |
| **Dữ liệu bài Lab (`C:\LAB3`)** | Thiếu cơ chế sao lưu tự động và kiểm soát tính toàn vẹn. | Xóa nhầm, Ransomware mã hóa, lỗi hỏng ổ cứng. | Mất mát hoặc bị sửa đổi dữ liệu quan trọng. | Phân quyền tối thiểu (Least Privilege), sao lưu định kỳ, băm SHA-256. |
| **Dịch vụ mạng cục bộ (Web Server)** | Cổng dịch vụ mở ngoài ý muốn, không mã hóa TLS. | Backdoor, DoS làm nghẽn dịch vụ, Sniffing. | Gián đoạn truy cập, lộ dữ liệu truyền tải. | Tường lửa ngăn cổng ngoài, chỉ bind 127.0.0.1, mã hóa HTTPS. |
| **Người dùng / Hộp thư điện tử** | Thiếu kỹ năng nhận diện email giả mạo, không xác minh domain. | Social Engineering, Spear Phishing lừa đảo. | Rò rỉ thông tin nội bộ, bị cài cắm mã độc. | Đào tạo nhận thức an toàn thông tin, lọc email tự động, xác minh đa kênh. |
| **Cấu hình hệ thống & Registry** | Quyền cấp cho người dùng quá rộng, thiếu giám sát tiến trình khởi động. | Tạo persistence ngầm qua Registry Run / Scheduled Task. | Mã độc tồn tại vĩnh viễn sau khi khởi động lại máy. | Giám sát Sysmon (Event ID 1, 12, 13), kiểm tra Autoruns định kỳ. |

#### Phân loại 5 tình huống nguồn đe dọa:
1. *Nhân viên xóa nhầm tệp cấu hình đang dùng:* **Hành động vô ý (Accidental Action)**.
2. *Người có ác ý cài phần mềm thu thập dữ liệu:* **Hành động cố ý (Deliberate / Malicious Action)**.
3. *Mất điện kéo dài làm dịch vụ dừng và file chưa ghi bị mất:* **Thảm họa tự nhiên / Môi trường (Environmental / Natural Disaster)**.
4. *Ổ đĩa hỏng hoặc dịch vụ treo do lỗi phần mềm:* **Lỗi kỹ thuật phần cứng/phần mềm (Technical Failure)**.
5. *Tổ chức không áp dụng chính sách sao lưu/vá lỗi:* **Lỗi quản lý / Quy trình (Management Failure)**.

---

### 2. Tình huống 2 (TH2): Kiểm chứng chu trình phát hiện mã độc bằng chuỗi EICAR
* **Mục tiêu:** Kiểm tra khả năng phát hiện, cảnh báo và cách ly tự động của Microsoft Defender theo thời gian thực mà không cần dùng mã độc thật.
* **Thao tác:** Ghi chuỗi kiểm thử `EICAR-STANDARD-ANTIVIRUS-TEST-FILE` vào file `C:\LAB3\Evidence\eicar.com.txt`.
* **Kết quả:** **PASS** (Defender chặn ngay lập tức, tự động cách ly và ghi nhận vào lịch sử).
* **Minh chứng:** `defender_eicar.txt` chứa chi tiết đe dọa `ThreatID`, `Resources`, `ActionSuccess`.
* **Ảnh minh chứng:** `H4_ProtectionHistory_EICAR.png` chụp trong giao diện *Windows Security $\rightarrow$ Protection history*.

---

### 3. Tình huống 3 (TH3): Tấn công mật khẩu và Quản lý sự kiện xác thực (Event Logs)
* **Mục tiêu:** Sinh các sự kiện xác thực hợp lệ/bất hợp lệ, phân tích Log sự kiện và thực hiện đổi thông tin xác thực (Credential Rotation).
* **Thao tác:** 
  1. Bật Audit Logon Policy (`auditpol /set /subcategory:{0CCE9215-69AE-11D9-BED3-505054503030} /success:enable /failure:enable`).
  2. Tạo tài khoản cục bộ `lab3user`.
  3. Thực hiện 1 lần đăng nhập thành công (`runas /user:.\lab3user cmd.exe` với mật khẩu đúng).
  4. Thực hiện 2 lần đăng nhập thất bại (cố ý nhập sai mật khẩu).
  5. Đổi mật khẩu mới cho `lab3user` bằng `Set-LocalUser` để vô hiệu hóa mật khẩu cũ.
* **Kết quả:** **PASS**
* **Minh chứng:** `auth_events_before_rotation.txt` ghi nhận đầy đủ Event ID `4624` (Đăng nhập thành công), `4625` (Đăng nhập thất bại) và `4648` (Đăng nhập bằng thông tin xác thực tường minh).
* **Ảnh minh chứng:** `H5_Event4625.png` chụp trong **Event Viewer** (`Security Log`) thể hiện chi tiết Event ID `4625` liên quan tới user `lab3user`.

---

### 4. Tình huống 4 (TH4): Nhận diện cơ chế Backdoor & Persistence ngầm
* **Mục tiêu:** Tạo và truy vết cơ chế duy trì quyền truy cập (Persistence) bằng Registry Run Key, Scheduled Task và dịch vụ lắng nghe cổng.
* **Thao tác:**
  1. Cài đặt Sysmon 15.22 với cấu hình `sysmon-lab.xml` để ghi nhận Event ID 1 (Process Create), Event ID 12/13 (Registry Event).
  2. Tạo mục khởi động ngầm `LAB3_Run_Demo` trong `HKCU:\Software\Microsoft\Windows\CurrentVersion\Run`.
  3. Tạo Scheduled Task `LAB3_Persistence_Demo` tự động kích hoạt khi đăng nhập.
  4. Khởi chạy HTTP Server cục bộ `python -m http.server 8080 --bind 127.0.0.1`.
* **Kết quả:** **PASS**
* **Minh chứng:** `autoruns_before.csv`, `sysmon_persistence.txt`.
* **Ảnh minh chứng:**
  * `H6_Sysmon_Event1.png`: Chụp Event Viewer tại log *Sysmon/Operational* thấy Event ID 1.
  * `H7_Autoruns_LAB3_Run_Demo.png`: Chụp công cụ **Autoruns** (tab Logon) thấy mục `LAB3_Run_Demo`.
  * `H8_ProcessExplorer_Python.png`: Chụp công cụ **Process Explorer** ánh xạ đúng tiến trình `python.exe` đang lắng nghe port 8080.

---

### 5. Tình huống 5 (TH5): Bắt gói tin và So sánh Sniffing giữa HTTP và HTTPS
* **Mục tiêu:** Dùng Wireshark quan sát sự khác biệt giữa giao thức truyền thông văn bản rõ (Plaintext) và giao thức mã hóa (TLS/HTTPS).
* **Thao tác:**
  1. Bắt gói tin trên `Adapter for loopback traffic capture` với bộ lọc `http.request || tcp.port == 8080`.
  2. Gửi request: `curl.exe "http://127.0.0.1:8080/?lab_user=lab3_student&lab_code=TRAINING_ONLY"`.
  3. Bắt gói tin trên card mạng ra Internet với bộ lọc `tls || tcp.port == 443` và gửi request HTTPS tới `example.com`.
* **Kết quả:** **PASS**
  * Với HTTP: Toàn bộ thông tin đường dẫn URI, tham số `lab_user` và `lab_code=TRAINING_ONLY` bị đọc rõ ràng trên Wireshark.
  * Với HTTPS: Nội dung dữ liệu (Payload) và Request URI được mã hóa hoàn toàn bởi TLS; Wireshark chỉ nhìn thấy metadata (địa chỉ IP, Port, kích thước gói, thông tin bắt tay TLS).
* **Ảnh minh chứng:**
  * `H9_HTTP_Plaintext.png`: Bắt được gói tin HTTP GET thấy rõ chuỗi `TRAINING_ONLY`.
  * `H10_TLS_443.png`: Bắt được gói tin TLS mã hóa bảo mật.

---

### 6. Tình huống 6 (TH6): Phân tích Tấn công Từ chối Dịch vụ (DoS/DDoS) & Mail Bombing
* **Mục tiêu:** Phân biệt cơ chế DoS đơn nguồn và DDoS đa nguồn phân tán; nhận diện hành vi gửi thư rác ồ ạt qua log.
* **Thao tác:**
  1. Chạy thử nghiệm tải nội bộ 50 requests với 5 worker tới `127.0.0.1:8080` qua `local_load_test.py`.
  2. Phân tích dataset `ddos_sample.csv` (dải TEST-NET) bằng cách nhóm theo `SourceIP`.
  3. Thống kê số lượng email và dung lượng theo người gửi trong `mailbomb_sample.csv`.
* **Kết quả:** **PASS**
* **Minh chứng:** `local_load_test.txt`, `ddos_sources.txt`, `mail_sender_counts.txt`, `mail_volume.txt`.
* **Ảnh minh chứng:** `H10_Load_and_Log_Analysis.png` chụp cửa sổ PowerShell hiển thị thống kê DDoS và Mail bombing.

---

### 7. Tình huống 7 (TH7): Phân tích Tấn công Phi kỹ thuật (Social Engineering & Phishing)
* **Mục tiêu:** Nhận diện các chỉ dấu lừa đảo trong email và phân loại các biến thể Social Engineering.
* **Thao tác:**
  1. Mở và phân tích tệp `phishing_email.txt`.
  2. Phân loại 6 ca tấn công trong `social_engineering_cases.csv`.
* **Kết quả:** **PASS**
  * **5 chỉ dấu Phishing phát hiện được:**
    1. *Tạo cảm giác khẩn cấp giả tạo:* Đe dọa tài khoản sẽ bị vô hiệu hóa trong vòng 24 giờ nếu không cập nhật.
    2. *Mạo danh thương hiệu / Uy tín giả:* Sử dụng tên hiển thị "IT Service Desk / Security Center".
    3. *Domain gửi thư đáng ngờ:* Tên miền gửi thư không khớp với tổ chức chính thống.
    4. *Sai lệch Header:* Trường `Reply-To` trỏ đến một địa chỉ hoàn toàn khác với trường `From`.
    5. *Yêu cầu truy cập liên kết đăng nhập giả mạo:* Thúc giục nhấp vào đường link lạ để nhập tên đăng nhập và mật khẩu.
  * **Phân loại 6 tình huống:** Phishing đại trà, Spear Phishing (nhắm mục tiêu cụ thể), Baiting (dụ dỗ bằng mồi nhử), Pretexting (dựng kịch bản giả danh), Quid Pro Quo (trao đổi dịch vụ giả), Watering Hole (cài bẫy trên trang web thường truy cập).
* **Ảnh minh chứng:** `H10_Phishing_Offline.png` chụp Notepad mở `phishing_email.txt` cùng bảng PowerShell hiển thị các case.

---

## IV. CÔNG TÁC DỌN DẸP VÀ KIỂM TRA TOÀN VẸN (CLEANUP & SHA-256)

1. **Thực thi lệnh dọn dẹp:**
   * Xóa mục Run trong Registry `LAB3_Run_Demo`.
   * Gỡ bỏ Scheduled Task `LAB3_Persistence_Demo`.
   * Dừng tiến trình HTTP Server port 8080.
   * Xóa tài khoản thử nghiệm `lab3user`.
2. **Kiểm tra trạng thái sau dọn dẹp:**
   * Xuất lại file `autoruns_after.csv` và chạy `Compare-Object` với `autoruns_before.csv` (lưu vào `autoruns_diff.txt`) để đảm bảo hệ thống đã trở về trạng thái sạch ban đầu.
3. **Băm mã kiểm tra toàn vẹn (SHA-256):**
   * Tính mã SHA-256 cho toàn bộ các file trong thư mục `C:\LAB3\Evidence\` và xuất ra file `evidence_sha256.csv`.
* **Ảnh minh chứng:** `H11_Recovery_Verification.png` chụp màn hình xác nhận dọn dẹp hoàn tất, hệ thống an toàn.

---

## V. TRẢ LỜI CHI TIẾT 20 CÂU HỎI BÁO CÁO (MỤC C)

### Câu 1: Phân biệt Asset, Vulnerability, Threat, Risk và Attack qua ví dụ thực tế trên VM
* **Tài sản (Asset):** Cơ sở dữ liệu tài khoản quản trị và dữ liệu báo cáo trong thư mục `C:\LAB3`.
* **Lỗ hổng (Vulnerability):** Cấu hình cho phép người dùng đặt mật khẩu ngắn, không bắt buộc xác thực hai yếu tố (MFA).
* **Mối đe dọa (Threat):** Kẻ tấn công hoặc phần mềm độc hại cố gắng dò mật khẩu hoặc đánh cắp thông tin đăng nhập qua mạng.
* **Rủi ro (Risk):** Khả năng tài khoản quản trị bị kẻ xấu chiếm đoạt, dẫn đến lộ lọt dữ liệu và mất quyền kiểm soát máy tính.
* **Tấn công (Attack):** Hành động kẻ xấu gửi hàng ngàn yêu cầu thử mật khẩu liên tục (Brute Force) vào cổng dịch vụ để đăng nhập trái phép.

### Câu 2: Phân loại 5 tình huống ở TH1 và tiêu chí phân loại
* **ID 1 (Nhân viên xóa nhầm cấu hình):** *Hành động vô ý* – Do sai sót của con người, không có chủ đích phá hoại.
* **ID 2 (Cài phần mềm gián điệp):** *Hành động cố ý* – Có động cơ ác ý nhằm đánh cắp dữ liệu.
* **ID 3 (Mất điện đột ngột làm mất file):** *Thảm họa tự nhiên / Môi trường* – Sự cố hạ tầng vật lý ngoài ý muốn.
* **ID 4 (Hỏng ổ cứng / crash phần mềm):** *Lỗi kỹ thuật* – Sự cố phát sinh do phần cứng hao mòn hoặc lỗi lập trình (Bug).
* **ID 5 (Không thực thi chính sách sao lưu):** *Lỗi quản lý* – Thiếu sót trong việc ban hành, giám sát và thực thi quy trình bảo mật của tổ chức.

### Câu 3: Ý nghĩa của chuỗi kiểm thử EICAR đối với Defender
* **Chứng minh được:** Chứng minh cơ chế bảo vệ theo thời gian thực (Real-time Protection), chức năng quét tệp khi ghi (On-write scan), cơ chế cô lập (Quarantine) và khả năng ghi nhận sự kiện bảo mật của Defender đang hoạt động chính xác.
* **Không chứng minh được:** Không chứng minh rằng hệ thống có khả năng chống lại 100% các loại mã độc mới (Zero-day, Fileless malware, mã độc đa hình) vì EICAR chỉ là một mẫu chữ ký tĩnh đã được chuẩn hóa toàn cầu.

### Câu 4: Vì sao không được tắt Defender/EDR để "cho mẫu chạy"? Quy trình xử lý phần mềm hợp lệ bị chặn
* **Lý do không tắt Defender:** Khi tắt Defender/EDR, toàn bộ hệ thống sẽ mất đi lớp lá chắn bảo vệ, tạo cơ hội cho mã độc thật xâm nhập, lây lan sang các máy khác trong mạng nội bộ và phá hủy bằng chứng pháp chứng.
* **Quy trình xử lý đúng khi phần mềm hợp lệ bị chặn (False Positive):**
  1. *Cách ly & Xác minh:* Thu thập mã băm SHA-256 của file, kiểm tra trên các nền tảng phân tích (VirusTotal, Sandbox).
  2. *Kiểm tra nguồn gốc:* Xác thực chữ ký số (Digital Signature) và nhà phát triển của phần mềm.
  3. *Tạo ngoại lệ có kiểm soát (Scoped Exclusion):* Thay vì tắt Defender, chỉ tạo Rule loại trừ chính xác theo đường dẫn tệp cụ thể hoặc mã hash sau khi đã được phê duyệt.
  4. *Báo cáo nhà cung cấp:* Gửi mẫu tệp lên Microsoft Security Intelligence để cập nhật lại cơ sở dữ liệu nhận dạng.

### Câu 5: So sánh Brute Force, Dictionary Attack và Keylogger
| Tiêu chí | Brute Force Attack | Dictionary Attack | Keylogger Attack |
| :--- | :--- | :--- | :--- |
| **Dữ liệu đầu vào** | Thử vét cạn tất cả các tổ hợp ký tự có thể (a-z, 0-9, ký tự đặc biệt). | Sử dụng danh sách các từ phổ biến hoặc mật khẩu đã bị rò rỉ (Wordlist). | Thu thập trực tiếp chuỗi phím bấm bàn phím do nạn nhân gõ. |
| **Cách phát hiện** | Nhận diện qua lượng lớn Event ID 4625 xuất hiện dồn dập trong thời gian ngắn từ một nguồn. | Lượng Event ID 4625 xuất hiện với các chuỗi thử có quy luật từ ngữ. | Phát hiện tiến trình lạ móc vào hàm API bàn phím (SetWindowsHookEx), kiểm tra bằng Process Explorer / Sysmon. |
| **Biện pháp giảm thiểu**| Khóa tài khoản sau N lần nhập sai (Account Lockout), CAPTCHA, giới hạn tần suất. | Bắt buộc mật khẩu phức tạp/ngẫu nhiên, cấm mật khẩu nằm trong danh sách rò rỉ. | Sử dụng bàn phím ảo, công nghệ chống ghi phím, triển khai MFA / Passkey / FIDO2. |

### Câu 6: Giải thích trạng thái xác thực qua Event ID 4624, 4625, 4648
* **Event ID 4624 (Successful Logon):** Ghi nhận khi người dùng cung cấp chính xác tên đăng nhập và mật khẩu hợp lệ của `lab3user`.
* **Event ID 4625 (Failed Logon):** Ghi nhận khi có yêu cầu đăng nhập nhưng cung cấp sai mật khẩu (hoặc tài khoản bị khóa), giúp phát hiện dấu hiệu dò mật khẩu.
* **Event ID 4648 (Logon with Explicit Credentials):** Ghi nhận khi một tiến trình chạy dưới quyền tài khoản hiện tại cố gắng khởi chạy ứng dụng bằng bộ thông tin xác thực khác (như qua lệnh `runas`).
* **Sau khi đổi mật khẩu:** Mật khẩu cũ khi nhập vào sẽ lập tức sinh ra Event 4625, chỉ mật khẩu mới mới sinh ra Event 4624, chứng minh việc đổi thông tin xác thực (Credential Rotation) đã vô hiệu hóa thành công nguy cơ từ mật khẩu cũ.

### Câu 7: Vì sao mật khẩu dài/phức tạp không ngăn được Keylogger? Vai trò của MFA / FIDO2
* **Vì sao mật khẩu phức tạp vô hiệu:** Dù mật khẩu dài và phức tạp đến đâu, khi người dùng gõ từng ký tự từ bàn phím, Keylogger nằm ở tầng hệ thống vẫn thu thập lại đầy đủ chuỗi ký tự theo đúng thứ tự gõ.
* **Vai trò & Giới hạn của các cơ chế xác thực:**
  * *MFA dạng SMS/OTP:* Yêu cầu thêm mã dùng một lần (hết hạn sau 30-60s), giúp bảo vệ ngay cả khi mật khẩu bị lộ. Tuy nhiên, OTP vẫn có thể bị vượt qua nếu nạn nhân bị lừa đảo qua trang Phishing trực tiếp (Adversary-in-the-Middle).
  * *Push MFA:* Tiện lợi hơn qua ứng dụng điện thoại, nhưng có thể bị tấn công bằng kỹ thuật làm phiền thông báo (*MFA Fatigue / Spamming*).
  * *FIDO2 / Passkey:* Cơ chế xác thực dựa trên cặp khóa mật mã phần cứng bất đối xứng và gắn liền với domain (Origin-bound). Đây là biện pháp chống Phishing và Keylogger triệt để nhất hiện nay vì không có mật khẩu nào được gõ từ bàn phím.

### Câu 8: Một cổng đang Listen có đủ kết luận có Backdoor không? 4 bằng chứng tương quan
* **Không thể kết luận chỉ dựa vào việc có cổng mở:** Vì nhiều dịch vụ hợp lệ của hệ điều hành và phần mềm quản trị cũng lắng nghe trên các cổng mạng.
* **4 bằng chứng cần tương quan trước khi kết luận:**
  1. *Tiến trình sở hữu (Owning Process):* Tên tiến trình, đường dẫn thực thi và PID gắn với cổng đó (kiểm tra qua `netstat` / `Get-NetTCPConnection`).
  2. *Chữ ký số & Nhà phát hành (Digital Signature / Publisher):* Tiến trình có được ký bởi nhà phát hành uy tín (Microsoft, Google...) hay là tệp vô danh không chữ ký.
  3. *Dòng lệnh khởi chạy (Command Line Arguments):* Kiểm tra các tham số truyền vào tiến trình trong Process Explorer.
  4. *Cơ chế tự khởi động (Persistence Entry):* Tiến trình có tự đăng ký vào Registry Run, Scheduled Task hoặc Service lạ nào không.

### Câu 9: Cơ chế tạo Persistence của Run Key & Scheduled Task. Vì sao phải kiểm tra trước khi xóa
* **Cơ chế hoạt động:**
  * *Registry Run Key (`HKCU\...\Run`):* Hệ điều hành tự động quét và chạy tất cả các lệnh/file có trong khóa này mỗi khi người dùng đăng nhập.
  * *Scheduled Task:* Trình lập lịch của Windows kích hoạt chạy chương trình dựa trên các sự kiện kích hoạt (như khi đăng nhập, theo giờ cố định, hoặc khi hệ thống khởi động).
* **Vì sao cần kiểm tra kỹ trước khi xóa:** Cần kiểm tra tên, đường dẫn tệp, chữ ký số và ngữ cảnh hoạt động để tránh xóa nhầm các tiến trình hệ thống thiết yếu của Windows (như driver đồ họa, trình quản lý âm thanh, dịch vụ cập nhật), nếu xóa nhầm có thể làm hỏng hệ điều hành.

### Câu 10: So sánh DoS và DDoS. Vì sao DDoS khó chặn chỉ bằng một IP rule
* **So sánh:**
  * *DoS (Denial of Service):* Cuộc tấn công xuất phát từ một nguồn duy nhất (1 địa chỉ IP).
  * *DDoS (Distributed Denial of Service):* Cuộc tấn công xuất phát đồng loạt từ hàng ngàn/hàng triệu nguồn phân tán trên toàn cầu (mạng Botnet).
* **Vì sao khó chặn:** Trong DDoS, lưu lượng tấn công đến từ vô số địa chỉ IP khác nhau và mỗi IP chỉ gửi một lượng nhỏ yêu cầu (nhìn như người dùng bình thường). Do đó, nếu chỉ tạo rule chặn 1 IP đơn lẻ thì không làm giảm đáng kể tổng lưu lượng tấn công, mà cần các giải pháp chuyên dụng (Rate Limiting, CDN Scrubbing, Anycast DNS, Web Application Firewall).

### Câu 11: Thuộc tính bị ảnh hưởng bởi Mail Bombing và 2 chỉ số phát hiện
* **Thuộc tính an toàn thông tin bị ảnh hưởng:** **Tính sẵn sàng (Availability)** – Làm tràn ngập dung lượng hộp thư, tắc nghẽn hàng đợi (Mail Queue) và làm tê liệt máy chủ nhận thư.
* **2 chỉ số quan trọng để phát hiện bất thường:**
  1. *Tần suất gửi từ một địa chỉ / tên miền (Message Count per Sender/Domain):* Số lượng email gửi đến đột biến từ một người gửi trong khoảng thời gian ngắn.
  2. *Tổng dung lượng và kích thước tệp đính kèm (Total Size / Spike in Byte Volume):* Tổng dung lượng hòm thư tăng vọt bất thường.

### Câu 12: Sử dụng Sniffing hợp pháp vs bất hợp pháp. So sánh HTTP và HTTPS
* **Hợp pháp:** Quản trị viên mạng dùng Sniffing (Wireshark) để chẩn đoán sự cố mạng, phân tích hiệu năng và phát hiện tấn công bảo mật.
* **Bất hợp pháp:** Kẻ xấu nghe lén gói tin trên mạng không mã hóa (như Wi-Fi công cộng) để đánh cắp mật khẩu, session cookie và dữ liệu cá nhân.
* **Khác biệt qua 2 capture TH5:**
  * *HTTP:* Bắt được gói tin thấy toàn bộ nội dung rõ ràng (Plaintext), bao gồm phương thức GET, URL đầy đủ và tham số `TRAINING_ONLY`.
  * *HTTPS:* Toàn bộ phần Application Data được mã hóa bằng TLS. Người nghe lén chỉ thấy được các gói tin dạng nhị phân mã hóa và metadata (IP, Port 443), không đọc được dữ liệu trao đổi bên trong.

### Câu 13: Các kỹ thuật Man-in-the-Middle (MITM). Kỹ thuật không thực hiện trong lab và lý do
* **Các kỹ thuật:**
  * *Email Hijacking:* Chặn và sửa đổi nội dung thư điện tử giữa hai bên.
  * *Wi-Fi Eavesdropping:* Lập điểm phát sóng giả (Evil Twin) để dụ người dùng kết nối và nghe lén.
  * *Session Hijacking:* Đánh cắp chuỗi định danh phiên (Session Cookie) để chiếm đoạt phiên làm việc của người dùng.
  * *IP/DNS/HTTPS Spoofing:* Giả mạo địa chỉ IP, phản hồi DNS sai lệch hoặc chèn chứng chỉ số giả để chuyển hướng người dùng về máy chủ độc hại.
* **Kỹ thuật không thực hiện chủ động trong Lab:** Các kỹ thuật như *ARP Poisoning, DNS Spoofing, Evil Twin Wi-Fi* không được thực hiện chủ động vì chúng có thể gây gián đoạn mạng, làm ảnh hưởng đến các máy trạm khác ngoài phạm vi bài lab và vi phạm đạo đức an toàn thông tin.

### Câu 14: Cơ chế giảm rủi ro MITM của HTTPS, HSTS, VPN và các giới hạn
* **Cơ chế bảo vệ:**
  * *HTTPS:* Mã hóa kênh truyền và xác thực danh tính máy chủ qua chứng chỉ số (X.509 Certificate).
  * *HSTS (HTTP Strict Transport Security):* Ép trình duyệt luôn luôn kết nối qua HTTPS, ngăn chặn tấn công hạ cấp giao thức (SSL Stripping).
  * *VPN:* Tạo đường hầm mã hóa toàn bộ dữ liệu từ máy trạm đến cổng mạng riêng, chống nghe lén trên đường truyền công cộng.
* **Giới hạn không thể giải quyết:**
  1. Không chống được **Phishing:** Nếu người dùng tự truy cập vào một trang web giả mạo có cài HTTPS hợp lệ và tự tay nhập mật khẩu.
  2. Không chống được **Endpoint Compromise:** Nếu máy người dùng đã bị cài mã độc hoặc Keylogger từ trước.

### Câu 15: Phân biệt Spoofing ở lớp IP, DNS và Website/Domain
* **IP Spoofing:** Giả mạo địa chỉ IP nguồn trong gói tin IP.
  * *Kiểm soát/Bằng chứng:* Bật tính năng Ingress/Egress Filtering trên Router (RFC 2827 / BCP 38), phân tích TTL bất thường trên Wireshark.
* **DNS Spoofing:** Làm sai lệch bản ghi DNS để hướng người dùng tới địa chỉ IP giả mạo.
  * *Kiểm soát/Bằng chứng:* Triển khai **DNSSEC** (chữ ký số cho bản ghi DNS), cấu hình DNS over HTTPS (DoH) / DNS over TLS (DoT).
* **Website / Domain Spoofing:** Đăng ký tên miền gần giống (Typosquatting) và dựng giao diện giả mạo trang web thật.
  * *Kiểm soát/Bằng chứng:* Kiểm tra chứng chỉ SSL/TLS, xác thực SPF / DKIM / DMARC cho email, sử dụng bộ lọc URL an toàn (SmartScreen/Safe Browsing).

### Câu 16: Phân biệt 6 dạng tấn công Social Engineering
1. **Phishing (Lừa đảo đại trà):** Gửi email/tin nhắn giả mạo hàng loạt với nội dung chung chung để đánh cắp thông tin của nhiều nạn nhân.
2. **Spear Phishing (Lừa đảo có chủ đích):** Tấn công lừa đảo được thiết kế riêng cho một cá nhân/tổ chức cụ thể, có thu thập thông tin cá nhân từ trước để tăng độ tin cậy.
3. **Watering Hole (Tấn công hố nước):** Thâm nhập và cài mã độc vào một trang web hợp lệ mà nhóm đối tượng mục tiêu thường xuyên truy cập.
4. **Pretexting (Dựng kịch bản):** Kẻ tấn công tạo ra một danh tính giả và câu chuyện hoàn cảnh hợp lý (ví dụ đóng vai nhân viên IT/kiểm toán) để yêu cầu cung cấp thông tin bí mật.
5. **Baiting (Mồi nhử):** Đặt bẫy bằng mồi nhử hấp dẫn (ví dụ để lại USB có dán nhãn "Bảng lương" hoặc tặng phần mềm bản quyền miễn phí) để nạn nhân tự cài mã độc.
6. **Quid Pro Quo (Đổi chác):** Hứa hẹn cung cấp một dịch vụ hoặc lợi ích nào đó để đổi lấy thông tin bảo mật từ nạn nhân (ví dụ giả danh nhân viên hỗ trợ kỹ thuật gọi điện sửa lỗi máy tính).

### Câu 17: Phân tích 5 chỉ dấu trong `phishing_email.txt`
1. *Uy tín giả:* Tên người gửi hiển thị là `IT Support Center` để tạo lòng tin.
2. *Cảm giác khẩn cấp:* Đưa ra cảnh báo tài khoản sẽ bị khóa vĩnh viễn trong vòng 24 giờ nếu không xác minh.
3. *Domain đáng ngờ:* Tên miền người gửi sử dụng đuôi `.example` hoặc không khớp với cổng thông tin nội bộ của đơn vị.
4. *Header sai lệch:* Dòng `Reply-To` trỏ về một hòm thư cá nhân không liên quan thay vì địa chỉ chính thức của bộ phận IT.
5. *Yêu cầu thông tin xác thực:* Chứa liên kết yêu cầu người dùng truy cập và nhập tên đăng nhập cùng mật khẩu.

### Câu 18: Quy trình ứng phó sự cố (Incident Response) cho Endpoint nghi bị nhiễm Keylogger/Backdoor
$$\text{Triage} \rightarrow \text{Preserve Evidence} \rightarrow \text{Contain} \rightarrow \text{Credential Action} \rightarrow \text{Eradicate} \rightarrow \text{Recover} \rightarrow \text{Monitor}$$
1. **Triage (Đánh giá sơ bộ):** Xác định phạm vi ảnh hưởng, mức độ nghiêm trọng và dấu hiệu bất thường trên máy trạm.
2. **Preserve Evidence (Bảo lưu bằng chứng):** Trích xuất bộ nhớ RAM, lưu file log sự kiện, xuất danh sách tiến trình và tính mã băm SHA-256 của các tệp nghi ngờ.
3. **Contain (Cô lập sự cố):** Ngắt kết nối mạng của máy trạm (chuyển sang Host-only hoặc rút cáp mạng) để ngăn mã độc nhận lệnh C2 hoặc lây lan ngang.
4. **Credential Action (Xử lý thông tin xác thực):** Thực hiện đổi toàn bộ mật khẩu của các tài khoản đã đăng nhập trên máy trạm từ một thiết bị sạch khác; thu hồi token phiên làm việc.
5. **Eradicate (Loại bỏ triệt để):** Xóa bỏ các mục khởi động ngầm trong Registry, gỡ Scheduled Task, xóa file mã độc và chấm dứt các tiến trình độc hại.
6. **Recover (Khôi phục hệ thống):** Cập nhật bản vá bảo mật, khôi phục dữ liệu sạch từ bản sao lưu hoặc Revert về Snapshot an toàn.
7. **Monitor (Giám sát liên tục):** Tiếp tục theo dõi log Sysmon, Event Log và lưu lượng mạng để đảm bảo mã độc không tái xuất hiện.

### Câu 19: Ý nghĩa của mã băm SHA-256 đối với file Evidence
* **Chứng minh được:** Chứng minh **Tính toàn vẹn (Integrity)** của tệp dữ liệu. Đảm bảo rằng file bằng chứng không bị chỉnh sửa, giả mạo hay thay đổi bất kỳ ký tự nào kể từ thời điểm tạo ra mã băm.
* **Không chứng minh được:** Không chứng minh được **Nguồn gốc (Origin/Authenticity)** hay **Tính đúng đắn của nội dung** bên trong tệp (nếu file ban đầu chứa thông tin sai lệch thì mã hash vẫn băm đúng nội dung sai lệch đó).

### Câu 20: Đề xuất mô hình Phòng thủ Chiều sâu (Defense-in-Depth) cho máy tính doanh nghiệp
1. **Endpoint Protection:** Cài đặt phần mềm diệt virus/EDR có bật Real-time Protection, Behavior Monitoring và Tamper Protection.
2. **Least Privilege (Quyền hạn tối thiểu):** Tài khoản làm việc hàng ngày chỉ cấp quyền Standard User; chỉ dùng quyền Administrator khi cần thiết và qua cơ chế UAC.
3. **Application Control:** Triển khai AppLocker hoặc Windows Defender Application Control (WDAC) để chỉ cho phép các phần mềm được phê duyệt hoạt động.
4. **Logging & Monitoring:** Bật đầy đủ Audit Policy (Logon/Logoff, Process Creation với Event ID 4688 / Sysmon) và đẩy log về hệ thống giám sát tập trung (SIEM).
5. **Network Control:** Cấu hình tường lửa Host-based Firewall chặn toàn bộ cổng vào không cần thiết; áp dụng phân vùng mạng (VLAN), chỉ cho phép truy cập qua VPN an toàn.
6. **Backup:** Thực hiện chiến lược sao lưu định kỳ 3-2-1, lưu bản sao lưu ở nơi cách ly (Offline/Immutable backup) để phòng ngừa Ransomware.
7. **Credential Protection:** Triển khai xác thực đa yếu tố không cần mật khẩu (FIDO2 / Passkey), kích hoạt tính năng Credential Guard của Windows để chống trích xuất mã băm mật khẩu từ LSASS.

---

## VI. DANH MỤC FILE BẰNG CHỨNG (EVIDENCE) VÀ BẢNG SHA-256

Toàn bộ các file trong thư mục `C:\LAB3\Evidence\` sau khi hoàn thành bài lab:
* `start_time.txt`: Mốc thời gian bắt đầu thực hành.
* `baseline_os.txt`, `baseline_defender.txt`, `baseline_firewall.txt`: Trạng thái hệ thống ban đầu.
* `defender_eicar.txt`: Log Defender phát hiện chuỗi EICAR.
* `auth_events_before_rotation.txt`: Log Event ID 4624, 4625, 4648 của tài khoản `lab3user`.
* `autoruns_before.csv`, `autoruns_after.csv`, `autoruns_diff.txt`: Log đối chiếu mục khởi động ngầm.
* `sysmon_persistence.txt`: Log Sysmon ghi nhận tiến trình và registry.
* `local_load_test.txt`: Kết quả thử tải DoS cục bộ.
* `ddos_sources.txt`: Bảng thống kê phân bố IP nguồn tấn công DDoS.
* `mail_sender_counts.txt`, `mail_volume.txt`: Thống kê tần suất và dung lượng Mail bombing.
* `evidence_sha256.csv`: Bảng chữ ký băm SHA-256 của toàn bộ tệp bằng chứng.

---
*Báo cáo được hoàn thành và đối chiếu đầy đủ với yêu cầu môn học Thực hành An toàn và Bảo mật Hệ thống Thông tin.*

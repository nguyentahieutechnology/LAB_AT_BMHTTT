# BÁO CÁO THỰC HÀNH BÀI SỐ 02
## MÃ HÓA ĐỐI XỨNG HIỆN ĐẠI
### (JAVA SWING GUI BUILDER – APACHE MAVEN – BOUNCY CASTLE – MICROSOFT SQL SERVER – PYTHON CRYPTOGRAPHY)

* **Học phần:** An toàn và Bảo mật Thông tin
* **Họ và tên sinh viên:** Nguyễn Tạ Hiếu
* **Mã số sinh viên (MSSV):** 1150080015
* **Lớp:** 11_THMT
* **Tài liệu chuẩn:** LAB 2 - Hướng dẫn thực hành từng bước Mã hóa đối xứng hiện đại (`LAB2_HuongDan.docx`)
* **Project triển khai:** `LAB2_MaHoaHienDai` (Package: `week_02`) tại `C:\BaoMat\LAB2_MaHoaHienDai`

---

## I. THÔNG TIN SINH VIÊN VÀ MÔI TRƯỜNG THỰC HÀNH

### 1. Bảng thông số môi trường chuẩn hóa

| Thành phần hệ thống | Phiên bản / Cấu hình chi tiết | Vai trò và Chức năng triển khai | Trạng thái kiểm tra |
| :--- | :--- | :--- | :---: |
| **Hệ điều hành Host** | Windows 10/11 Professional 64-bit | Môi trường máy trạm phát triển ứng dụng phòng Lab | **HOẠT ĐỘNG** |
| **Java Development Kit** | Eclipse Temurin JDK 17.0.21+9 (LTS) / OpenJDK 17<br>• Path: `C:\Program Files\Eclipse Adoptium\jdk-17...`<br>• Biến môi trường: `JAVA_HOME` | Biên dịch (`javac`) và thực thi JVM (`java`), cung cấp thư viện mật mã chuẩn `javax.crypto.*`, Java Security Provider. | **ĐẠT CHUẨN** |
| **Apache Maven** | Maven 3.9.9 (Portable runtime)<br>• Path: `C:\Users\Admin\AppData\Local\Programs\apache-maven-3.9.9\bin` | Quản trị vòng đời ứng dụng, tự động tải và liên kết các thư viện phụ thuộc (`pom.xml`) từ Maven Central. | **ĐẠT CHUẨN** |
| **Apache NetBeans** | NetBeans IDE 20+ (Matisse GUI Builder) | Môi trường lập trình trực quan (IDE), hỗ trợ thiết kế kéo thả giao diện đồ họa Java Swing (JFrame Forms). | **ĐÃ CÀI ĐẶT** |
| **Hệ quản trị CSDL** | Microsoft SQL Server 2022 Express<br>• Cổng kết nối TCP: `14333` (hoặc `1433`)<br>• Database: `LAB2_MaHoaHienDai`<br>• Tài khoản: `lab2_user` / `LabUser@2026` | Lưu trữ định danh người dùng băm mật khẩu kèm muối (Salt) và nhật ký kiểm toán mật mã (`dbo.crypto_history`). | **SẴN SÀNG** |
| **JDBC Driver** | `com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11` | Cung cấp Type-4 JDBC Driver kết nối an toàn với SQL Server thông qua kết nối có mã hóa TLS. | **BUILD SUCCESS** |
| **Bouncy Castle Provider**| `org.bouncycastle:bcprov-jdk18on:1.78.1` | Thư viện mở rộng Java Cryptography Architecture (JCA/JCE), cung cấp thuật toán mật mã khối Twofish. | **BUILD SUCCESS** |
| **Python Runtime & Lib** | Python 3.10+ & thư viện `cryptography`<br>• Lệnh cài đặt: `py -m pip install cryptography` | Môi trường triển khai bài nâng cao AES-CBC đa nền tảng và giao diện đồ họa Tkinter GUI. | **SẴN SÀNG** |

---

## II. THIẾT KẾ KIẾN TRÚC PROJECT VÀ CÁC MODULE DÙNG CHUNG

### 1. Cấu trúc tổ chức mã nguồn chuẩn hóa

Mã nguồn được đóng gói hoàn toàn trong package `week_02`, tuân thủ mô hình phân lớp rõ ràng giữa xử lý logic thuật toán mật mã (Cipher Engines), quản trị dữ liệu & xác thực (DAO / Database Access), tiện ích I/O dùng chung và lớp giao diện hiển thị (Swing GUI):

```text
C:\BaoMat\LAB2_MaHoaHienDai\
│   pom.xml                                  <-- Cấu hình quản trị Maven (MSSQL JDBC & Bouncy Castle)
│   01_Tao_CSDL.sql                          <-- Kịch bản T-SQL khởi tạo Database và cấu trúc bảng
│   02_Tao_Tai_Khoan_SQL.sql                 <-- Kịch bản cấp quyền cho tài khoản lab2_user
│   aes_cbc_gui.py                           <-- Ứng dụng Tkinter GUI bài nâng cao AES-CBC (Python)
└───src\main\java\week_02\
    ├── CipherUtil.java                      <-- Tiện ích Base64, Hex, SecureRandom, Hash SHA-256 + Salt
    ├── FileTextIO.java                      <-- Tiện ích nạp và xuất dữ liệu tệp tin văn bản UTF-8
    │
    ├── DbConfig.java                        <-- Quản lý chuỗi kết nối và trạng thái phiên JDBC
    ├── UserDao.java                         <-- Thao tác CRUD đăng ký, đăng nhập xác thực tài khoản
    ├── HistoryDao.java                      <-- Thao tác ghi log và truy vấn bảng lịch sử mã hóa
    ├── frm_KetNoiSQL.java                   <-- Form cấu hình tham số mạng và kiểm tra kết nối CSDL
    ├── frm_LichSu.java                      <-- Form hiển thị lưới JTable truy vấn lịch sử mã hóa
    │
    ├── DESCipher.java                       <-- Engine xử lý giải thuật DES (DES/CBC/PKCS5Padding)
    ├── frm_DES.java                         <-- Cửa sổ giao diện đồ họa Bài 2.1
    │
    ├── TripleDESCipher.java                 <-- Engine xử lý giải thuật TripleDES (DESede/CBC/PKCS5Padding)
    ├── frm_TripleDES.java                   <-- Cửa sổ giao diện đồ họa Bài 2.2
    │
    ├── AESCipher.java                       <-- Engine xử lý giải thuật AES (AES/CBC/PKCS5Padding)
    ├── frm_AES.java                         <-- Cửa sổ giao diện đồ họa Bài 2.3 tích hợp Login & SQL
    │
    ├── RC4Cipher.java                       <-- Engine giải thuật mật mã dòng RC4 (ARCFOUR)
    ├── frm_RC4.java                         <-- Cửa sổ giao diện đồ họa Bài 2.4
    │
    ├── TwofishCipher.java                   <-- Engine giải thuật Twofish (Bouncy Castle Provider)
    ├── frm_Twofish.java                     <-- Cửa sổ giao diện đồ họa Bài 2.5 (Tự động lưu/mở IV)
    │
    ├── AESCTRCipher.java                    <-- Engine giải thuật AES chế độ đếm (AES/CTR/NoPadding)
    ├── frm_AES_CTR.java                     <-- Cửa sổ giao diện đồ họa Bài 2.6 (Java Nâng cao)
    │
    └── frm_Menu.java                        <-- Cửa sổ Menu chính điều hướng toàn bộ chương trình (F6)
```

---

### 2. Thiết kế các Module tiện ích dùng chung

#### A. Tiện ích mật mã học `CipherUtil.java`
* **Xử lý mã hóa dữ liệu nhị phân:** Cung cấp các phương thức chuẩn hóa `toBase64(byte[])`, `fromBase64(String)`, `toHex(byte[])`, `fromHex(String)`. Việc chuyển đổi bản mã nhị phân sang Base64/Hex đảm bảo dữ liệu không bị biến dạng khi lưu trữ trên CSDL hoặc hiển thị lên giao diện Swing.
* **Tạo số giả ngẫu nhiên an toàn (CSPRNG):** Phương thức `randomBytes(int len)` sử dụng `java.security.SecureRandom`, đảm bảo tính ngẫu nhiên về mặt mật mã học cho các vector khởi tạo (IV), Nonce và muối (Salt), ngăn ngừa tuyệt đối nguy cơ dự đoán chuỗi bit.
* **Băm bảo mật kết hợp muối:** Phương thức `hashPassword(String password, byte[] salt)` thực hiện kết hợp mật khẩu với 16 byte Salt ngẫu nhiên và tính toán giá trị băm `SHA-256`, loại bỏ hoàn toàn nguy cơ tấn công dò tìm bằng bảng băm tính trước (Rainbow Table).

#### B. Module đọc/ghi tệp `FileTextIO.java`
* Lớp được khai báo `public final` với constructor `private` nhằm ngăn chặn việc khởi tạo đối tượng không cần thiết.
* Hỗ trợ hai chế độ thao tác:
  - `openText / saveText`: Thao tác trực tiếp với các điều khiển `JTextArea` (áp dụng cho DES, 3DES, AES, RC4).
  - `openString / saveString`: Đọc/ghi cấu trúc văn bản tùy biến chứa tiêu đề tách biệt (dùng cho Twofish và AES-CTR với format lưu kèm `IV=...` hoặc `NONCE=...`).
* Sử dụng `java.nio.file.Files` với bảng mã chuẩn `StandardCharsets.UTF_8` và `JFileChooser` tích hợp bộ lọc `.txt`, tự động thêm phần mở rộng khi lưu tệp.

---

### 3. Cấu trúc CSDL SQL Server và Module DAO

#### A. Kịch bản khởi tạo CSDL (`01_Tao_CSDL.sql` & `02_Tao_Tai_Khoan_SQL.sql`)
Hệ thống sử dụng cơ sở dữ liệu `LAB2_MaHoaHienDai` gồm 2 bảng nghiệp vụ chính:
* **Bảng `dbo.app_users`:**
  - `user_id`: Khóa chính tăng tự động (`IDENTITY(1,1)`).
  - `username`: Tên tài khoản duy nhất (`NVARCHAR(40) UNIQUE`).
  - `salt`: Muối ngẫu nhiên 16 byte (`VARBINARY(16)`).
  - `password_hash`: Chuỗi băm mật khẩu 32 byte (`VARBINARY(32)`).
  - `created_at`: Dấu thời gian hệ thống UTC (`DATETIME2(0)`).
* **Bảng `dbo.crypto_history`:**
  - `history_id`: Khóa chính tăng tự động (`BIGINT IDENTITY(1,1)`).
  - `algorithm`: Tên thuật toán (`NVARCHAR(40)`).
  - `operation`: Thao tác xử lý (`ENCRYPT` / `DECRYPT`).
  - `app_username`: Tài khoản thực hiện thao tác.
  - `input_length` & `output_length`: Kích thước dữ liệu đầu vào và đầu ra (byte).
  - `created_at`: Dấu thời gian ghi vết hệ thống UTC.

#### B. Cơ chế bảo mật và chống lỗi (Fail-safe DAO)
* **Lớp `DbConfig.java`:** Thiết lập chuỗi kết nối:
  `jdbc:sqlserver://127.0.0.1:14333;databaseName=LAB2_MaHoaHienDai;encrypt=true;trustServerCertificate=true`
* **Lớp `UserDao.java` & `HistoryDao.java`:** Toàn bộ truy vấn sử dụng `PreparedStatement` với tham số hóa `?`, triệt tiêu lỗ hổng SQL Injection.
* **Khả năng tự phục hồi (Fail-safe):** Khi SQL Server ngắt kết nối hoặc chưa bật dịch vụ, phương thức `tryLog()` bắt ngoại lệ nội bộ, hiển thị cảnh báo trên thanh trạng thái giao diện mà không làm treo hay sập chương trình chính.

---

## III. KẾT QUẢ TRIỂN KHAI CÁC THUẬT TOÁN MÃ HÓA ĐỐI XỨNG HIỆN ĐẠI CƠ BẢN

### 1. Bài 2.1: Mật mã DES (Data Encryption Standard)

* **Nguyên lý giải thuật:** Mật mã khối đối xứng cấu trúc mạng Feistel 16 vòng. Kích thước khối dữ liệu $64 \text{ bit}$ ($8 \text{ byte}$). Khóa danh định $64 \text{ bit}$ chứa $8 \text{ bit}$ kiểm tra chẵn lẻ (Parity), đem lại độ dài khóa hiệu dụng thực tế là $56 \text{ bit}$. Chế độ vận hành chuẩn: `DES/CBC/PKCS5Padding`.
* **Đặc tính kỹ thuật triển khai:**
  - Lớp `DESCipher` khởi tạo khóa từ `DESKeySpec` và `SecretKeyFactory`.
  - Sử dụng vector khởi tạo ngẫu nhiên $\text{IV}$ kích thước $8 \text{ byte}$ sinh bởi `CipherUtil.randomBytes(8)` thông qua `IvParameterSpec`.
  - Định dạng đóng gói bản mã: Ghép $8 \text{ byte}$ $\text{IV}$ vào đầu dữ liệu mã hóa:
    $$\text{Payload} = \text{IV (8 bytes)} \parallel \text{Ciphertext}$$
    Sau đó chuyển đổi toàn bộ khối sang định dạng Base64. Khi giải mã, chương trình bóc tách $8 \text{ byte}$ đầu tiên để tái lập $\text{IV}$, phần còn lại chuyển giao cho bộ giải mã.
  - **Tính chất bảo mật:** Do mỗi phiên mã hóa sinh một $\text{IV}$ ngẫu nhiên độc lập, nên hai lần mã hóa cùng một bản rõ với cùng một khóa sẽ luôn tạo ra hai chuỗi Base64 hoàn toàn khác nhau, loại bỏ nguy cơ lộ mẫu tần suất.
* **Kiểm thử chuẩn:**
  - Bản rõ (Plaintext): `Xin chào Lab 2 - Mã hóa hiện đại!`
  - Khóa (Key): `12345678` ($8 \text{ ký tự}$)
  - Kết quả: $\text{Decrypt}(\text{Encrypt}(x)) = x$. Giải mã với khóa sai sẽ lập tức ném ra biệt lệ `BadPaddingException` (hoặc thông báo lỗi khối đệm không hợp lệ).

---

### 2. Bài 2.2: Mật mã TripleDES (3DES / TDEA)

* **Nguyên lý giải thuật:** Kéo dài vòng đời của DES bằng cơ chế áp dụng 3 lần thuật toán DES liên tiếp với 3 khóa độc lập theo cấu trúc Encrypt-Decrypt-Encrypt (EDE):
  $$C = E_{K3}(D_{K2}(E_{K1}(P)))$$
  $$P = D_{K1}(E_{K2}(D_{K3}(C)))$$
  Chế độ triển khai: `DESede/CBC/PKCS5Padding`, kích thước khối $64 \text{ bit}$ ($8 \text{ byte}$), vector khởi tạo $\text{IV}$ ngẫu nhiên $8 \text{ byte}$.
* **Đặc tính kỹ thuật triển khai:**
  - Khóa bắt buộc phải có độ dài chính xác $24 \text{ byte}$ ($192 \text{ bit}$, trong đó có $168 \text{ bit}$ hiệu dụng và $24 \text{ bit}$ parity).
  - Lớp `TripleDESCipher` sử dụng `DESedeKeySpec` và kiểm tra độ dài khóa chặt chẽ. Nếu khóa không đủ 24 byte (ví dụ 23 ký tự), hệ thống chủ động chặn lại và thông báo lỗi `InvalidKeyException: Khóa DESede phải đúng 24 byte`.
  - Bản mã được đóng gói kèm $8 \text{ byte}$ $\text{IV}$ ở phần đầu và chuyển đổi sang chuỗi Base64.
* **Kiểm thử chuẩn:**
  - Bản rõ: `Xin chào Lab 2 - Mã hóa hiện đại!`
  - Khóa đúng ($24 \text{ ký tự}$): `123456789012345678901234` $\to$ Giải mã thành công, tái lập $100\%$ bản rõ ban đầu.
  - Khóa thử nghiệm sai ($23 \text{ ký tự}$): `12345678901234567890123` $\to$ Báo lỗi khóa không đủ 24 ký tự theo đúng yêu cầu kiểm thử Bảng 2.1.

---

### 3. Bài 2.3: Mật mã AES (Advanced Encryption Standard) tích hợp SQL Server

* **Nguyên lý giải thuật:** Tiêu chuẩn mã hóa tiên tiến dựa trên mạng thay thế - hoán vị (SPN - Substitution-Permutation Network). Kích thước khối chuẩn $128 \text{ bit}$ ($16 \text{ byte}$), hỗ trợ các độ dài khóa $128, 192, 256 \text{ bit}$. Chế độ triển khai: `AES/CBC/PKCS5Padding`, sử dụng $\text{IV}$ ngẫu nhiên $16 \text{ byte}$.
* **Quy trình xác thực & Tích hợp CSDL:**
  1. **Đăng ký tài khoản (Register):** Người dùng nhập Username và Password $\to$ Ứng dụng sinh ngẫu nhiên $16 \text{ byte}$ Salt $\to$ Băm `SHA-256(Password + Salt)` $\to$ Lưu `salt` và `password_hash` vào bảng `dbo.app_users`.
  2. **Đăng nhập hệ thống (Login):** Truy vấn CSDL lấy `salt` của username $\to$ Tính toán băm mật khẩu vừa nhập với `salt` $\to$ So sánh kết quả băm trong CSDL. Nếu khớp, cấp phiên đăng nhập thành công và sinh một **Ticket** ngẫu nhiên gồm $32 \text{ ký tự Hex}$ ($16 \text{ byte}$ `SecureRandom`).
  3. **Mã hóa / Giải mã & Ghi vết kiểm toán (Audit Trail):** Chỉ khi đã đăng nhập thành công (có Ticket hợp lệ), các chức năng Encrypt và Decrypt mới được kích hoạt. Sau mỗi thao tác, một bản ghi tự động được thêm vào bảng `dbo.crypto_history` ghi nhận: thuật toán `AES`, thao tác (`ENCRYPT`/`DECRYPT`), tên người dùng, độ dài đầu vào và độ dài đầu ra.
* **Kiểm thử chuẩn:**
  - Đăng ký user: `student01` / `MatKhau@123` $\to$ Thông báo tạo tài khoản thành công.
  - Bấm Login $\to$ Cửa sổ báo *“Login thành công”*, ô Ticket hiển thị chuỗi 32 ký tự HEX.
  - Thực hiện Encrypt chuỗi văn bản $\to$ Giải mã thành công; mở `frm_LichSu` thấy xuất hiện ngay bản ghi kiểm toán tương ứng.

---

### 4. Bài 2.4: Mật mã dòng RC4 (Rivest Cipher 4 / ARCFOUR)

* **Nguyên lý giải thuật:** Mật mã dòng (Stream Cipher) tốc độ cao dựa trên việc xáo trộn hoán vị của một mảng $256 \text{ byte}$ trạng thái nội tại ($S$-box). Giải thuật gồm 2 giai đoạn:
  1. **KSA (Key-Scheduling Algorithm):** Khởi tạo hoán vị mảng $S$ từ $0 \dots 255$ dựa trên khóa bí mật.
  2. **PRGA (Pseudo-Random Generation Algorithm):** Liên tục sinh dòng khóa giả ngẫu nhiên (Keystream byte $K$). Bản mã được tạo ra bằng phép XOR trực tiếp giữa dòng khóa và từng byte bản rõ:
     $$C_i = P_i \oplus K_i \quad \Longleftrightarrow \quad P_i = C_i \oplus K_i$$
* **Đặc tính kỹ thuật triển khai:**
  - Sử dụng định danh thuật toán Java JCE: `"ARCFOUR"`.
  - Mật mã dòng xử lý trực tiếp từng bit/byte dữ liệu, do đó **không cần vector khởi tạo (IV)** và **không cần cơ chế đệm (Padding)**.
  - Kích thước bản mã nhị phân luôn luôn bằng đúng kích thước bản rõ.
  - Dữ liệu mã hóa được hiển thị dưới dạng chuỗi **HEX** (hệ cơ số 16) viết hoa để người dùng dễ dàng quan sát và đối chiếu.
* **Kiểm thử chuẩn:**
  - Bản rõ (Plaintext): `Plaintext`
  - Khóa (Key): `Key`
  - Kết quả bản mã HEX thu được: `BBF316E8D940AF0AD3` $\to$ **Khớp chính xác 100% với giá trị kiểm thử chuẩn của Bảng 2.1**.

---

### 5. Bài 2.5: Mật mã Twofish (Bouncy Castle Provider)

* **Nguyên lý giải thuật:** Mật mã khối đối xứng kích thước khối $128 \text{ bit}$ ($16 \text{ byte}$), khóa linh hoạt $128/192/256 \text{ bit}$, được thiết kế bởi Bruce Schneier và các cộng sự (từng lọt vào top 5 vòng chung kết chuẩn AES). Cấu trúc Feistel $16 \text{ vòng}$ kết hợp các hộp thay thế $S$-box phụ thuộc khóa và ma trận nhân khoảng cách cực đại MDS (Maximum Distance Separable).
* **Đặc tính kỹ thuật triển khai:**
  - Do JDK mặc định không tích hợp Twofish, chương trình cấu hình nạp động thư viện nhà cung cấp mật mã Bouncy Castle:
    ```java
    if (Security.getProvider("BC") == null) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    ```
  - Chế độ vận hành: `Twofish/CBC/PKCS5Padding` thông qua Provider `"BC"`.
  - Giao diện tích hợp nút **Generate IV** cho phép sinh ngẫu nhiên $16 \text{ byte}$ IV độc lập.
  - **Cơ chế lưu trữ và nạp tệp tích hợp:**
    - Nút `Save File` ghi dữ liệu theo định dạng văn bản chuẩn:
      ```text
      IV=<Chuỗi Base64 của 16 byte IV>
      <Chuỗi Base64 của bản mã>
      ```
    - Nút `Open File` tự động đọc tệp, phân tích chuỗi (Parse string) để bóc tách dòng chứa tiền tố `IV=` đưa vào ô `txt_iv` và phần thân bản mã vào ô `txt_ciphertext`, giúp việc giải mã diễn ra hoàn toàn tự động mà người dùng không cần sao chép IV thủ công.
* **Kiểm thử chuẩn:**
  - Nhập Key cố định $\to$ Bấm *Generate IV* $\to$ Bấm *Encrypt* $\to$ Bấm *Save File* lưu ra ổ đĩa.
  - Xóa trắng form $\to$ Bấm *Open File* mở lại tệp vừa lưu $\to$ Form tự động nạp lại đầy đủ Key, IV và Ciphertext $\to$ Bấm *Decrypt* thu lại bản rõ nguyên vẹn.

---

## IV. KẾT QUẢ TRIỂN KHAI CÁC BÀI TẬP NÂNG CAO

### 1. Bài 2.6.1: Mật mã AES chế độ đếm (AES-CTR) trên Java

* **Nguyên lý giải thuật:** Chế độ đếm (Counter Mode - CTR) biến đổi mật mã khối AES thành một mật mã dòng đồng bộ. Thay vì mã hóa trực tiếp dữ liệu, thuật toán mã hóa một khối đếm duy nhất bao gồm một giá trị ngẫu nhiên dùng một lần (**Nonce**) kết hợp với chỉ số bộ đếm tăng dần $i$:
  $$O_i = E_K(\text{Nonce} \parallel i)$$
  $$C_i = P_i \oplus O_i \quad \Longleftrightarrow \quad P_i = C_i \oplus O_i$$
* **Đặc tính kỹ thuật triển khai:**
  - Chuỗi định danh thuật toán: `AES/CTR/NoPadding`.
  - Do vận hành theo cơ chế dòng qua phép XOR, thuật toán **hoàn toàn không sử dụng cơ chế đệm (NoPadding)**.
  - **Độ dài bản mã luôn bằng chính xác độ dài bản rõ:** Nếu bản rõ dài $27 \text{ byte}$ thì bản mã sau mã hóa nhị phân cũng dài đúng $27 \text{ byte}$.
  - Nonce ngẫu nhiên $16 \text{ byte}$ được sinh qua `SecureRandom` và lưu kèm bản mã theo cấu trúc tệp:
    ```text
    NONCE=<Chuỗi Base64 của Nonce>
    <Chuỗi Base64 của Ciphertext>
    ```
  - **Khả năng xử lý vượt trội:** Giải mã và mã hóa có thể thực hiện song song (Parallel execution) độc lập giữa các khối dữ liệu, hỗ trợ truy xuất ngẫu nhiên (Random access) tới khối bất kỳ mà không cần giải mã tuần tự từ đầu.
* **Kiểm thử chuẩn:**
  - Nhập bản rõ bất kỳ $\to$ Bấm sinh Nonce ngẫu nhiên $\to$ Encrypt.
  - Kiểm tra độ dài: Kích thước byte bản mã khớp chính xác kích thước byte bản rõ. Quá trình giải mã tái lập $100\%$ bản rõ.

---

### 2. Bài 2.6.2: Triển khai AES-CBC trên Python với thư viện `cryptography` & GUI Tkinter

* **Kiến trúc ứng dụng Python (`aes_cbc_gui.py`):**
  - Xây dựng ứng dụng hoàn chỉnh bằng Python 3 sử dụng module chuẩn cấp thấp `cryptography.hazmat.primitives.ciphers` (`algorithms.AES`, `modes.CBC`) và cơ chế đệm chuẩn `padding.PKCS7(128)`.
  - Giao diện đồ họa được lập trình trực quan bằng thư viện chuẩn `tkinter` và `ttk`, tích hợp đầy đủ các chức năng:
    + Sinh ngẫu nhiên khóa $256 \text{ bit}$ ($32 \text{ byte}$) hiển thị dạng Base64.
    + Sinh ngẫu nhiên $\text{IV}$ $16 \text{ byte}$ ($128 \text{ bit}$) hiển thị dạng Base64.
    + Mã hóa và giải mã văn bản độ dài tùy ý.
    + Chức năng kiểm thử tự động nội tại (**Self-test**).
* **Kết quả Self-test tự động:**
  - Khi ứng dụng khởi chạy hoặc khi bấm nút kiểm thử, hàm `run_selftest()` tự động thực thi kịch bản kiểm thử:
    1. Mã hóa chuỗi bản rõ mẫu $3 \text{ byte}$ (`abc`).
    2. Xác nhận độ dài bản mã sau đệm PKCS7 tròn đúng $16 \text{ byte}$ ($1 \text{ block}$).
    3. Thử nghiệm giải mã với khóa sai và kiểm tra việc kích hoạt ngoại lệ `ValueError`.
    4. Giải mã lại với khóa đúng và so khớp toàn vẹn dữ liệu.
  - Khi toàn bộ các bước kiểm tra thành công, ứng dụng in dòng chữ thông báo kiểm thử thành công:
    $$\textbf{SELF-TEST OK}$$

---

## V. HỆ THỐNG GIAO DIỆN TỔNG HỢP VÀ ĐIỀU HƯỚNG

### 1. Form điều hướng chính (`frm_Menu.java`)
* Được cấu hình làm điểm khởi động chính của ứng dụng trong Maven (`<exec.mainClass>week_02.frm_Menu</exec.mainClass>`).
* Bố cục giao diện thiết kế chuyên nghiệp, chia nhóm các chức năng khoa học:
  - Nhóm các thuật toán cơ bản: Nút bấm mở trực tiếp các form `frm_DES`, `frm_TripleDES`, `frm_AES`, `frm_RC4`, `frm_Twofish`.
  - Nhóm bài tập nâng cao: Nút bấm mở form `frm_AES_CTR` và nhãn hướng dẫn thực thi tệp `aes_cbc_gui.py`.
  - Nhóm quản trị hệ thống: Nút mở `frm_KetNoiSQL` và `frm_LichSu`.

### 2. Cửa sổ giám sát kết nối và lịch sử CSDL (`frm_KetNoiSQL`, `frm_LichSu`)
* `frm_KetNoiSQL`: Cho phép kiểm tra trạng thái máy chủ CSDL SQL Server, thay đổi linh hoạt địa chỉ Host, Cổng TCP, Tên đăng nhập và Mật khẩu kết nối.
* `frm_LichSu`: Tích hợp bảng dữ liệu `JTable` phân trang, tự động truy vấn bảng `dbo.crypto_history` hiển thị trực quan các trường: Mã lịch sử, Tên thuật toán, Hành động (ENCRYPT/DECRYPT), Người dùng thực thi, Kích thước Input/Output và Dấu thời gian.

---

## VI. MA TRẬN ĐỐI CHIẾU KẾT QUẢ KIỂM THỬ THỰC NGHIỆM

Bảng đối chiếu toàn diện giữa dữ liệu kiểm thử chuẩn của tài liệu hướng dẫn (Bảng 2.1) và kết quả thực thi thực tế trên mã nguồn của sinh viên:

| Bài | Thuật toán | Dữ liệu kiểm thử đầu vào (Plaintext / Key / IV) | Kết quả yêu cầu chuẩn theo Lab | Kết quả chương trình chạy thực tế | Đánh giá |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **2.1** | **DES** | Plaintext: `Xin chào Lab 2 - Mã hóa hiện đại!`<br>Key: `12345678` | $\text{Decrypt}(\text{Encrypt}(x)) = x$; mỗi lần Encrypt sinh IV ngẫu nhiên ra bản mã khác nhau | Khớp $100\%$; mỗi lần bấm Encrypt ra chuỗi Base64 khác nhau; giải mã chính xác bản rõ | **ĐẠT (PASS)** |
| **2.2** | **TripleDES** | Plaintext: `Xin chào Lab 2 - Mã hóa hiện đại!`<br>Key: `123456789012345678901234` ($24 \text{ ký tự}$) | $\text{Decrypt}(\text{Encrypt}(x)) = x$; nếu Key $23 \text{ ký tự}$ thì hệ thống báo lỗi | Khóa 24 ký tự giải mã thành công; khóa 23 ký tự ứng dụng cảnh báo lỗi `InvalidKey` | **ĐẠT (PASS)** |
| **2.3** | **AES** | Đăng ký user mới $\to$ Đăng nhập hệ thống $\to$ Mã hóa dữ liệu | Đăng nhập thành công cấp Ticket (32 hex); thêm bản ghi vào `app_users` và `crypto_history` | Cấp Ticket 32 hex; bảng SQL Server xuất hiện đầy đủ thông tin tài khoản và nhật ký | **ĐẠT (PASS)** |
| **2.4** | **RC4** | Plaintext: `Plaintext`<br>Key: `Key` | Bản mã HEX đúng chuẩn:<br>`BBF316E8D940AF0AD3` | Bản mã HEX xuất ra:<br>`BBF316E8D940AF0AD3` | **ĐẠT (PASS)** |
| **2.5** | **Twofish** | Key cố định; IV sinh qua nút *Generate IV* | Giải mã chuẩn; thao tác Save File rồi Open File tự động khôi phục chính xác IV | Lưu file format `IV=...`, mở file tự động điền lại IV và Ciphertext, giải mã hoàn hảo | **ĐẠT (PASS)** |
| **2.6** | **AES-CTR (Java)**| Plaintext tùy ý; Key & Nonce ngẫu nhiên | Giải mã đúng; độ dài byte bản mã bằng đúng độ dài byte bản rõ (không đệm) | Độ dài dữ liệu mã hóa bằng $100\%$ độ dài bản rõ; giải mã nguyên vẹn không cần đệm | **ĐẠT (PASS)** |
| **2.6** | **AES-CBC (Python)**| Văn bản độ dài tùy ý; khóa 256-bit | Giải mã toàn vẹn; chức năng Self-test in ra `SELF-TEST OK` | Chạy ứng dụng Tkinter giải mã chuẩn; Self-test vượt qua tất cả kiểm thử (`SELF-TEST OK`) | **ĐẠT (PASS)** |

---

## VII. TRẢ LỜI CÂU HỎI PHÂN TÍCH KỸ THUẬT VÀ ĐÁNH GIÁ AN TOÀN

### 1. Bảng so sánh toàn diện các thuật toán mật mã đối xứng hiện đại

| Tiêu chí phân tích | DES (Data Encryption Standard) | TripleDES (3DES / TDEA) | AES (Rijndael) | RC4 (ARCFOUR) | Twofish |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Năm công bố** | 1977 (NIST / IBM) | 1998 (NIST) | 2001 (NIST / Daemen & Rijmen) | 1987 (Ron Rivest) | 1998 (Bruce Schneier et al.) |
| **Loại hình mật mã** | Mật mã khối (Block Cipher) | Mật mã khối (Block Cipher) | Mật mã khối (Block Cipher) | Mật mã dòng (Stream Cipher) | Mật mã khối (Block Cipher) |
| **Cấu trúc lõi** | Mạng Feistel (16 vòng) | Mạng Feistel (48 vòng EDE) | Mạng SPN (10/12/14 vòng) | Mảng hoán vị trạng thái (KSA/PRGA) | Mạng Feistel (16 vòng) + MDS |
| **Kích thước khối (Block size)**| $64 \text{ bit}$ ($8 \text{ byte}$) | $64 \text{ bit}$ ($8 \text{ byte}$) | $128 \text{ bit}$ ($16 \text{ byte}$) | Không có khối (xử lý từng byte) | $128 \text{ bit}$ ($16 \text{ byte}$) |
| **Độ dài khóa (Key size)** | $56 \text{ bit}$ hiệu dụng ($64 \text{ bit}$) | $112 \text{ hoặc } 168 \text{ bit}$ | $128, 192, 256 \text{ bit}$ | $40 \dots 2048 \text{ bit}$ | $128, 192, 256 \text{ bit}$ |
| **Tốc độ xử lý phần mềm** | Trung bình | Chậm (gấp 3 lần DES) | Rất nhanh (tối ưu hóa CPU AES-NI) | Cực nhanh | Nhanh, linh hoạt |
| **Tình trạng an toàn hiện nay** | **Không an toàn** (Bị bẻ khóa dễ dàng bằng Brute-force trong vài giờ) | **Lỗi thời / Ngừng sử dụng** (NIST chính thức khai tử từ cuối năm 2023) | **Cực kỳ an toàn** (Tiêu chuẩn mã hóa vàng toàn cầu) | **Bị cấm trong TLS** (Lỗ hổng thiên vị thống kê byte đầu) | **Rất an toàn** (Chưa có tấn công thực tế khả thi) |

---

### 2. So sánh chuyên sâu các chế độ hoạt động của mã khối (Cipher Modes)

#### A. Chế độ ECB (Electronic Codebook) – Nguy cơ rò rỉ mẫu dữ liệu
* **Cơ chế:** Chia bản rõ thành các khối độc lập $P_1, P_2, \dots$ và mã hóa từng khối bằng cùng một khóa: $C_i = E_K(P_i)$.
* **Điểm yếu cốt tử:** Các khối bản rõ có nội dung giống nhau sẽ luôn sinh ra các khối bản mã giống hệt nhau. Kẻ tấn công có thể nhận diện được hình dạng cấu trúc dữ liệu mà không cần giải mã (hiện tượng rò rỉ hình ảnh *ECB Penguin*). **Tuyệt đối không sử dụng ECB trong các hệ thống thực tế.**

#### B. Chế độ CBC (Cipher Block Chaining)
* **Cơ chế:** Mỗi khối bản rõ trước khi mã hóa được XOR với khối bản mã liền trước nó; khối đầu tiên được XOR với vector khởi tạo $\text{IV}$:
  $$C_1 = E_K(P_1 \oplus \text{IV}), \quad C_i = E_K(P_i \oplus C_{i-1})$$
* **Ưu điểm:** Loại bỏ hoàn toàn sự lặp lại mẫu dữ liệu nhờ tính chất dây chuyền và $\text{IV}$ ngẫu nhiên.
* **Hạn chế:** Việc mã hóa phải thực hiện tuần tự, không thể phân tán xử lý song song. Khi truyền tin trên kênh không tin cậy nếu không có mã xác thực tin nhắn (MAC), chế độ CBC có nguy cơ bị tấn công vét cạn đệm (*Padding Oracle Attack*).

#### C. Chế độ CTR (Counter Mode)
* **Cơ chế:** Biến đổi mã khối thành mã dòng bằng cách mã hóa giá trị bộ đếm tăng dần ghép với Nonce: $C_i = P_i \oplus E_K(\text{Nonce} \parallel i)$.
* **Ưu điểm vượt trội:**
  1. Cho phép xử lý song song hoàn toàn ở cả hai chiều mã hóa và giải mã.
  2. Hỗ trợ truy xuất ngẫu nhiên tới bất kỳ vị trí byte dữ liệu nào.
  3. Hoàn toàn không cần thêm đệm (Padding), dữ liệu bản mã có kích thước đúng bằng bản rõ.

---

### 3. Tầm quan trọng của Vector khởi tạo (IV) và Nonce trong an toàn mật mã

1. **Tại sao IV phải luôn ngẫu nhiên và không dự đoán trước được?**
   Trong chế độ CBC, nếu $\text{IV}$ cố định hoặc có thể dự đoán trước (ví dụ dùng số thứ tự tuần tự), kẻ tấn công có thể thực hiện tấn công bản rõ lựa chọn (Chosen-Plaintext Attack - CPA) bằng cách dự đoán phép XOR giữa bản rõ và $\text{IV}$, dẫn đến làm mất hoàn toàn tính bảo mật ngữ nghĩa (Semantic Security).
2. **Hậu quả thảm khốc khi tái sử dụng Nonce trong chế độ CTR:**
   Trong chế độ đếm (CTR), nếu cùng một cặp $(\text{Key}, \text{Nonce})$ bị tái sử dụng cho hai bản rõ khác nhau $P_A$ và $P_B$, dòng khóa $K_{\text{stream}}$ được sinh ra sẽ giống hệt nhau. Khi đó, kẻ nghe lén trên đường truyền chỉ cần thực hiện phép XOR giữa hai bản mã thu được:
   $$C_A \oplus C_B = (P_A \oplus K_{\text{stream}}) \oplus (P_B \oplus K_{\text{stream}}) = P_A \oplus P_B$$
   Lúc này, toàn bộ lớp bảo vệ mật mã bị triệt tiêu, kẻ tấn công có thể sử dụng các kỹ thuật phân tích thống kê ngôn ngữ (Two-Time Pad attack) để khôi phục lại cả hai bản rõ gốc một cách dễ dàng.

---

## VIII. DANH MỤC MINH CHỨNG THỰC NGHIỆM VÀ TÀI LIỆU THAM KHẢO

### 1. Danh mục các minh chứng thực nghiệm trong bài Lab

| Ký hiệu | Nội dung minh chứng thực nghiệm | Vị trí / Đối tượng kiểm chứng | Kết quả đánh giá |
| :---: | :--- | :--- | :---: |
| **H1** | Cấu hình `pom.xml` nạp `mssql-jdbc` và Bouncy Castle `bcprov-jdk18on` | Thư mục `LAB2_MaHoaHienDai`: `mvn clean compile` | **BUILD SUCCESS** |
| **H2** | Khởi tạo CSDL `LAB2_MaHoaHienDai`, bảng `app_users` và `crypto_history` | SSMS / `sqlcmd` truy vấn `sys.tables` | **SẴN SÀNG** |
| **H3** | Giao diện và kết quả mã hóa/giải mã thuật toán DES | Form `frm_DES` (Kiểm thử IV ngẫu nhiên) | **ĐẠT (PASS)** |
| **H4** | Giao diện và kiểm soát lỗi độ dài khóa thuật toán TripleDES | Form `frm_TripleDES` (Test khóa 24 ký tự & khóa 23 ký tự) | **ĐẠT (PASS)** |
| **H5** | Đăng ký tài khoản, băm mật khẩu kèm Salt vào SQL Server | Form `frm_AES` (Đăng ký tài khoản `app_users`) | **ĐẠT (PASS)** |
| **H6** | Đăng nhập hệ thống, cấp Ticket 32 HEX và mã hóa AES | Form `frm_AES` (Đăng nhập & Mã hóa AES-128/CBC) | **ĐẠT (PASS)** |
| **H7** | Giao diện và kết quả mã hóa mật mã dòng RC4 | Form `frm_RC4` (`Plaintext` / `Key` $\to$ `BBF316E8D940AF0AD3`) | **ĐẠT (PASS)** |
| **H8** | Giao diện mã hóa Twofish (Bouncy Castle) tự động lưu/mở IV qua file | Form `frm_Twofish` (Tích hợp Generate IV & File I/O) | **ĐẠT (PASS)** |
| **H9** | Giao diện mã hóa chế độ đếm AES-CTR trên Java | Form `frm_AES_CTR` (Mã hóa không đệm, Nonce ngẫu nhiên) | **ĐẠT (PASS)** |
| **H10**| Ứng dụng Tkinter GUI Python AES-CBC và kết quả Self-test | Tệp `aes_cbc_gui.py` (In thông báo `SELF-TEST OK`) | **ĐẠT (PASS)** |
| **H11**| Lưới hiển thị nhật ký kiểm toán mật mã từ SQL Server | Form `frm_LichSu` (JTable kết nối `dbo.crypto_history`) | **ĐẠT (PASS)** |
| **H12**| Cửa sổ Menu chính điều hướng toàn bộ chương trình | Form `frm_Menu` (Run Project F6) | **ĐẠT (PASS)** |

---

### 2. Tài liệu tham khảo
* **[1]** Bộ môn An toàn và Bảo mật Thông tin – *Tài liệu hướng dẫn thực hành LAB 2: Mã hóa đối xứng hiện đại*, 2026.
* **[2]** National Institute of Standards and Technology (NIST) – *FIPS PUB 197: Advanced Encryption Standard (AES)*, U.S. Department of Commerce.
* **[3]** National Institute of Standards and Technology (NIST) – *NIST SP 800-38A: Recommendation for Block Cipher Modes of Operation*, Methods and Techniques.
* **[4]** Bruce Schneier, John Kelsey, Doug Whiting, David Wagner, Chris Hall, Niels Ferguson – *The Twofish Encryption Algorithm: A 128-Bit Block Cipher*, John Wiley & Sons.
* **[5]** The Legion of the Bouncy Castle – *Bouncy Castle Cryptography APIs Documentation for Java (JDK 1.8 to 21)*.
* **[6]** Python Cryptographic Authority – *Cryptography Package Documentation (hazmat cipher primitives)*.

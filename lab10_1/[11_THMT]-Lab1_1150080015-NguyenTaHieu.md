# BÁO CÁO THỰC HÀNH BÀI SỐ 01
## MÃ HÓA ĐỐI XỨNG CỔ ĐIỂN
### (JAVA SWING GUI BUILDER – APACHE MAVEN – MICROSOFT SQL SERVER)

* **Học phần:** An toàn và Bảo mật Thông tin
* **Họ và tên sinh viên:** Nguyễn Tạ Hiếu
* **Mã số sinh viên (MSSV):** 1150080015
* **Lớp:** 11_THMT
* **Tài liệu chuẩn:** LAB 1 - Hướng dẫn thực hành từng bước Mã hóa đối xứng cổ điển (`LAB10_HuongDan.docx`)
* **Project triển khai:** `LAB1_MaHoaCoDien` (Package: `week_01`) tại `C:\BaoMat\LAB1_MaHoaCoDien`

---

## I. THÔNG TIN SINH VIÊN VÀ MÔI TRƯỜNG THỰC HÀNH

### 1. Bảng thông số môi trường chuẩn hóa

| Thành phần hệ thống | Phiên bản / Cấu hình chi tiết | Vai trò và Chức năng triển khai | Trạng thái kiểm tra |
| :--- | :--- | :--- | :---: |
| **Hệ điều hành Host** | Windows 10/11 Professional 64-bit | Môi trường máy trạm phát triển ứng dụng phòng Lab | **HOẠT ĐỘNG** |
| **Java Development Kit** | Eclipse Temurin JDK 17.0.21+9 (LTS)<br>• Path: `C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot`<br>• Biến môi trường: `JAVA_HOME` | Biên dịch (`javac`) và thực thi máy ảo JVM (`java`), hỗ trợ các API chuẩn Swing, File I/O, JDBC. | **ĐẠT CHUẨN** |
| **Apache Maven** | Maven 3.9.9 (Portable runtime)<br>• Path: `C:\Users\Admin\AppData\Local\Programs\apache-maven-3.9.9\bin` | Quản trị vòng đời ứng dụng, tự động giải quyết các dependencies (`pom.xml`) và build ứng dụng. | **ĐẠT CHUẨN** |
| **Apache NetBeans** | NetBeans IDE 31 (GUI Builder / Matisse) | Môi trường lập trình trực quan (IDE), thiết kế kéo thả giao diện đồ họa Java Swing (JFrame Forms). | **ĐÃ CÀI ĐẶT** |
| **Hệ quản trị CSDL** | Microsoft SQL Server 2022 Express<br>• Cổng kết nối TCP: `14333`<br>• Database: `BaoMatDB`<br>• Tài khoản: `lab1_user` / `LabUser@2026` | Lưu trữ tập trung lịch sử các thao tác mã hóa/giải mã phục vụ bài nâng cao kiểm toán bảo mật. | **SẴN SÀNG** |
| **Thư viện JDBC** | `com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11` | Cung cấp Type-4 JDBC Driver kết nối an toàn với SQL Server thông qua kết nối có mã hóa TLS. | **BUILD SUCCESS** |

---

## II. THIẾT KẾ KIẾN TRÚC PROJECT VÀ CÁC MODULE DÙNG CHUNG

### 1. Cấu trúc tổ chức mã nguồn chuẩn hóa

Mã nguồn được đóng gói hoàn toàn trong package `week_01`, tuân thủ mô hình phân lớp rõ ràng giữa xử lý logic thuật toán (Cipher Engines), quản trị dữ liệu (DAO / Database Access) và lớp giao diện hiển thị (Swing GUI):

```text
C:\BaoMat\LAB1_MaHoaCoDien\
│   pom.xml                              <-- Cấu hình quản trị Maven và khai báo JDBC Driver
│   README.md                            <-- Hướng dẫn vận hành và các phím tắt thực thi
└───src\main\java\week_01\
    ├── FileTextIO.java                  <-- Tiện ích nạp và xuất dữ liệu tệp tin văn bản UTF-8
    │
    ├── CaesarCipher.java                <-- Engine xử lý giải thuật Caesar (Dịch chuyển vòng)
    ├── frm_Caesar.java                  <-- Cửa sổ giao diện đồ họa Bài 1.1
    │
    ├── VigenereCipher.java              <-- Engine xử lý giải thuật Vigenère (Đa bảng chữ cái)
    ├── frm_Vigenere.java                <-- Cửa sổ giao diện đồ họa Bài 1.2
    │
    ├── RailFenceCipher.java             <-- Engine giải thuật hoán vị hàng rào Zigzag
    ├── frm_RailFence.java               <-- Cửa sổ giao diện đồ họa Bài 1.3
    │
    ├── PlayfairCipher.java              <-- Engine giải thuật Playfair ma trận khóa 5x5
    ├── frm_Playfair.java                <-- Cửa sổ giao diện đồ họa Bài 1.4
    │
    ├── TranspositionCipher.java         <-- Engine hoán vị cột theo khóa thứ tự
    ├── frm_Transposition.java           <-- Cửa sổ giao diện đồ họa Bài 1.5
    │
    ├── AutokeyCipher.java               <-- Engine giải thuật khóa tự sinh nối tiếp bản rõ
    ├── frm_Autokey.java                 <-- Cửa sổ giao diện đồ họa Bài 1.6
    │
    ├── CipherTongHop.java               <-- Bộ điều phối đa thuật toán tập trung
    ├── frm_TongHop.java                 <-- Form tổng hợp tích hợp Dropdown lựa chọn thuật toán
    │
    ├── DbConfig.java                    <-- Quản lý chuỗi kết nối và trạng thái phiên JDBC
    ├── HistoryDao.java                  <-- Thao tác CRUD bảng lịch sử trên SQL Server
    ├── frm_KetNoiSQL.java               <-- Form cấu hình tham số mạng và kiểm tra kết nối CSDL
    ├── frm_LichSu.java                  <-- Form hiển thị lưới JTable truy vấn lịch sử mã hóa
    │
    ├── AttackDemos.java                 <-- Engine tấn công phân tích mật mã (Cryptanalysis)
    ├── frm_TanCong.java                 <-- Giao diện demo Brute-force, Tần suất, Known-plaintext
    │
    └── frm_Menu.java                    <-- Cửa sổ Menu chính điều hướng toàn bộ chương trình (F6)
```

### 2. Thiết kế Module đọc/ghi tệp `FileTextIO.java`
* Lớp được khai báo `public final` với constructor private để ngăn ngừa việc khởi tạo đối tượng.
* Sử dụng `JFileChooser` kết hợp `FileNameExtensionFilter` chỉ lọc các file có đuôi `.txt`.
* Sử dụng `java.nio.file.Files.readString` và `Files.writeString` với bảng mã `StandardCharsets.UTF_8` đảm bảo không bị lỗi font tiếng Việt hoặc các ký tự đặc biệt.
* Tự động bổ sung phần mở rộng `.txt` nếu người dùng lưu file mà quên nhập đuôi mở rộng.

---

## III. KẾT QUẢ TRIỂN KHAI 6 THUẬT TOÁN MÃ HÓA CỔ ĐIỂN CƠ BẢN

### 1. Bài 1.1: Mật mã Caesar (Caesar Cipher)
* **Nguyên lý giải thuật:** Dịch chuyển từng ký tự trong bảng chữ cái tiếng Anh đi $k$ vị trí theo vòng tròn modulo 26:
  $$\text{Mã hóa: } C_i = (P_i + k) \pmod{26}$$
  $$\text{Giải mã: } P_i = (C_i - k) \pmod{26}$$
* **Đặc tính kỹ thuật:** Xử lý độc lập chữ hoa (`A-Z`) và chữ thường (`a-z`), bảo lưu nguyên vẹn khoảng trắng và các ký tự đặc biệt/dấu câu; sử dụng hàm `Math.floorMod()` trong Java để xử lý an toàn giá trị dịch chuyển âm khi giải mã.
* **Kiểm thử chuẩn:**
  * Bản rõ: `HELLO`
  * Khóa $k$: `3`
  * Bản mã: `KHOOR` $\to$ **Khớp 100% với Bảng 1.1**.

### 2. Bài 1.2: Mật mã Vigenère (Vigenère Cipher)
* **Nguyên lý giải thuật:** Mật mã thay thế đa bảng (Polyalphabetic). Mỗi ký tự của từ khóa $K$ sẽ quy định một bước dịch chuyển Caesar tương ứng cho ký tự bản rõ tại vị trí đó:
  $$C_i = (P_i + K_{i \pmod{m}}) \pmod{26}$$
* **Đặc tính kỹ thuật:** Khóa được chuẩn hóa viết hoa; các ký tự khoảng trắng và dấu câu trong bản rõ không làm tiêu tốn ký tự khóa (bước tiến con trỏ khóa chỉ tăng khi gặp ký tự chữ cái).
* **Kiểm thử chuẩn:**
  * Bản rõ: `HELLO`
  * Khóa: `KEY`
  * Bản mã: `RIJVS` $\to$ **Khớp 100% với Bảng 1.1**.

### 3. Bài 1.3: Mật mã Rail Fence (Mật mã hàng rào Zigzag)
* **Nguyên lý giải thuật:** Mật mã hoán vị vị trí (Transposition). Bản rõ được ghi theo đường zic-zac chạy lên xuống qua $N$ hàng (rails), sau đó bản mã được tạo ra bằng cách đọc lần lượt từng hàng từ trên xuống dưới.
* **Đặc tính kỹ thuật:** Xây dựng ma trận ký tự ảo hoặc mảng các `StringBuilder`; biến đổi hướng (`direction = +1` khi chạm hàng trên cùng, `direction = -1` khi chạm hàng dưới cùng).
* **Kiểm thử chuẩn:**
  * Bản rõ: `HELLO WORLD`
  * Khóa (Số hàng): `3`
  * Bản mã: `HOREL OLLWD` $\to$ **Khớp 100% với Bảng 1.1**.

### 4. Bài 1.4: Mật mã Playfair (Playfair Cipher)
* **Nguyên lý giải thuật:** Mã hóa thay thế theo từng cặp ký tự (Digram Substitution) sử dụng ma trận vuông kích thước $5 \times 5$ được sinh từ từ khóa:
  - Bảng chữ cái 25 ký tự, gộp chung `I` và `J` vào một ô.
  - Tách bản rõ thành các cặp 2 chữ cái: nếu 2 chữ cái trùng nhau, chèn ký tự đệm `X` (hoặc `Q` nếu là cặp `XX`); nếu độ dài lẻ thì bổ sung `X` vào cuối.
  - Quy tắc dịch chuyển: Cùng hàng $\to$ lấy ký tự bên phải (vòng tròn); Cùng cột $\to$ lấy ký tự bên dưới (vòng tròn); Khác hàng khác cột $\to$ lấy ký tự tạo thành hình chữ nhật ở cùng hàng.
* **Kiểm thử chuẩn:**
  * Bản rõ: `HELLOWORLD`
  * Khóa: `KEYWORD`
  * Bản mã: `GYIZSCOKCFBU` $\to$ **Khớp 100% với Bảng 1.1**.

### 5. Bài 1.5: Mật mã hoán vị cột (Columnar Transposition Cipher)
* **Nguyên lý giải thuật:** Xếp bản rõ theo từng hàng vào một bảng có số cột bằng độ dài khóa hoán vị. Sau đó đọc các cột theo thứ tự số thứ tự được chỉ định bởi mảng khóa.
* **Đặc tính kỹ thuật:** Hàm `parseKey("2,3,1")` kiểm tra tính hợp lệ: các số phải tạo thành một phép thế (permutation) hợp lệ từ 1 đến $n$, không trùng lặp và không bỏ sót số. Nếu độ dài bản rõ không chia hết cho số cột, tự động đệm ký tự `X` vào cuối hàng cuối.
* **Kiểm thử chuẩn:**
  * Bản rõ: `BAOMAT`
  * Khóa: `2,3,1`
  * Bản mã: `AAOTBM` $\to$ **Khớp 100% với Bảng 1.1**.

### 6. Bài 1.6: Mật mã Autokey (Autokey Cipher)
* **Nguyên lý giải thuật:** Khắc phục điểm yếu chu kỳ lặp lại của mật mã Vigenère bằng cách sử dụng chính các chữ cái của bản rõ để làm phần tiếp theo của dòng khóa:
  $$\text{Dòng khóa: } K_{\text{full}} = K_{\text{init}} + P_0 + P_1 + P_2 + \dots$$
* **Đặc tính kỹ thuật:** Khi giải mã, mỗi ký tự bản rõ vừa giải mã được sẽ lập tức được nối vào cuối dòng khóa để dùng làm khóa giải mã cho các ký tự tiếp theo.
* **Kiểm thử chuẩn:**
  * Bản rõ: `HELLO`
  * Khóa khởi tạo: `KEY`
  * Bản mã: `RIJSS` $\to$ **Khớp 100% với Bảng 1.1**.

---

## IV. BÀI NÂNG CAO 01 – CHƯƠNG TRÌNH TỔNG HỢP VÀ LƯU LỊCH SỬ SQL SERVER

### 1. Kiến trúc kết nối CSDL và Cơ chế chống lỗi
* **Lớp `DbConfig.java`:** Thiết lập chuỗi kết nối chuẩn:
  `jdbc:sqlserver://127.0.0.1:14333;databaseName=BaoMatDB;encrypt=true;trustServerCertificate=true`
  Cờ `trustServerCertificate=true` cho phép ứng dụng bỏ qua kiểm tra chứng chỉ SSL tự ký của SQL Server trong môi trường phòng lab mà không làm gián đoạn mã hóa đường truyền.
* **Lớp `HistoryDao.java`:** Toàn bộ câu lệnh thêm lịch sử (`INSERT INTO dbo.history`) và truy vấn (`SELECT ... ORDER BY timestamp DESC`) đều sử dụng `PreparedStatement` với tham số hóa `?`, ngăn chặn triệt để lỗ hổng SQL Injection.
* **Cơ chế Fail-safe (Không crash khi mất kết nối):** Hàm `tryInsert()` kiểm tra trạng thái cờ `DbConfig.isConnected()`; nếu SQL Server chưa sẵn sàng, chương trình ghi log trạng thái ra nhãn thông báo và tiếp tục mã hóa bình thường mà không phát sinh biệt lệ sập ứng dụng.

### 2. Mô hình giao diện tích hợp (`frm_TongHop`, `frm_KetNoiSQL`, `frm_LichSu`)
* `frm_TongHop` cung cấp `JComboBox` liệt kê 6 thuật toán; khi người dùng thay đổi lựa chọn, sự kiện `ItemStateChanged` tự động kích hoạt hàm `sampleKey()` để nạp khóa mẫu tương ứng vào ô `txt_key` và hiển thị gợi ý định dạng khóa.
* `frm_LichSu` sử dụng thành phần `JTable` với `DefaultTableModel` hiển thị trực quan các trường: Thời gian thực thi, Tên thuật toán, Chế độ (Encrypt/Decrypt), Bản rõ, Khóa và Bản mã kết quả.

---

## V. BÀI NÂNG CAO 02 – PHÂN TÍCH VÀ TẤN CÔNG MẬT MÃ CỔ ĐIỂN (CRYPTANALYSIS)

### 1. Kỹ thuật 1: Tấn công vét cạn Caesar (Brute-Force Attack)
* **Bản chất kỹ thuật:** Do không gian khóa của thuật toán Caesar cực kỳ nhỏ ($K \in [0, 25]$), kẻ tấn công chỉ cần duyệt lần lượt tất cả 26 giá trị dịch chuyển có thể có để giải mã.
* **Kết quả thực nghiệm:**
  - Bản mã đầu vào: `KHOOR`
  - Vòng lặp duyệt $k = 0 \dots 25$:
    + $k=0 \to \text{KHOOR}$
    + $k=1 \to \text{JGNNQ}$
    + $k=2 \to \text{IFMMP}$
    + **$k=3 \to \text{HELLO}$** *(Xuất hiện từ vựng tiếng Anh có nghĩa hoàn chỉnh)*
  - **Kết luận:** Khóa $k=3$ bị bẻ gãy tức thì trong thời gian dưới $1 \text{ ms}$.

### 2. Kỹ thuật 2: Phân tích tần suất chữ cái (Frequency Analysis)
* **Bản chất kỹ thuật:** Mật mã thay thế đơn bảng giữ nguyên đặc trưng thống kê ngôn ngữ. Trong tiếng Anh, chữ `E` có tần suất xuất hiện cao nhất ($\approx 12.7\%$), kế tiếp là `T` ($\approx 9.1\%$), `A` ($\approx 8.2\%$), `O` ($\approx 7.5\%$).
* **Kết quả thực nghiệm:**
  - Thống kê trên bản mã đủ dài: Ký tự nào xuất hiện nhiều nhất trong bản mã có xác suất rất cao chính là chữ `E` đã bị thay thế. Từ độ lệch giữa ký tự đó với vị trí chữ `E`, ta suy ra ngay khóa dịch chuyển mà không cần biết nội dung trước.

### 3. Kỹ thuật 3: Tấn công bản rõ đã biết trên Vigenère (Known Plaintext Attack)
* **Bản chất kỹ thuật:** Dựa trên quan hệ đại số của phép cộng modulo 26 giữa bản rõ $P$ và bản mã $C$, khóa $K$ tại từng vị trí ký tự được tính ngược lại một cách chính xác tuyệt đối:
  $$K_i = (C_i - P_i + 26) \pmod{26}$$
* **Kết quả thực nghiệm:**
  - Bản rõ đã biết: `HELLO`
  - Bản mã tương ứng: `RIJVS`
  - Tính toán từng vị trí:
    + $i=0: \text{'R'}(17) - \text{'H'}(7) = 10 \to \text{'K'}$
    + $i=1: \text{'I'}(8) - \text{'E'}(4) = 4 \to \text{'E'}$
    + $i=2: \text{'J'}(9) - \text{'L'}(11) = -2 \equiv 24 \to \text{'Y'}$
    + $i=3: \text{'V'}(21) - \text{'L'}(11) = 10 \to \text{'K'}$
    + $i=4: \text{'S'}(18) - \text{'O'}(14) = 4 \to \text{'E'}$
  - Chuỗi khóa thu được: `KEYKE...` $\to$ **Suy ra chính xác từ khóa gốc là `KEY`**.

---

## VI. MA TRẬN ĐỐI CHIẾU KẾT QUẢ KIỂM THỬ THỰC NGHIỆM

Bảng đối chiếu kết quả giữa chuẩn yêu cầu của bài Lab (Bảng 1.1) và kết quả thực thi thực tế trên mã nguồn của sinh viên:

| Thuật toán | Bản rõ kiểm thử (Plaintext) | Khóa thiết lập (Key) | Ciphertext chuẩn theo Lab | Ciphertext chương trình chạy thực tế | Đánh giá |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **1.1 Caesar** | `HELLO` | `3` | `KHOOR` | `KHOOR` | **ĐẠT (PASS)** |
| **1.2 Vigenère** | `HELLO` | `KEY` | `RIJVS` | `RIJVS` | **ĐẠT (PASS)** |
| **1.3 Rail Fence** | `HELLO WORLD` | `3` | `HOREL OLLWD` | `HOREL OLLWD` | **ĐẠT (PASS)** |
| **1.4 Playfair** | `HELLOWORLD` | `KEYWORD` | `GYIZSCOKCFBU` | `GYIZSCOKCFBU` | **ĐẠT (PASS)** |
| **1.5 Transposition** | `BAOMAT` | `2,3,1` | `AAOTBM` | `AAOTBM` | **ĐẠT (PASS)** |
| **1.6 Autokey** | `HELLO` | `KEY` | `RIJSS` | `RIJSS` | **ĐẠT (PASS)** |

---

## VII. TRẢ LỜI CÂU HỎI PHÂN TÍCH KỸ THUẬT VÀ NHẬN XÉT ĐÁNH GIÁ AN TOÀN

### 1. Bảng phân tích điểm yếu cốt tử và phương pháp bẻ khóa của các mật mã cổ điển

| Thuật toán | Không gian khóa ($|\mathcal{K}|$) | Điểm yếu cốt tử | Phương pháp tấn công hiệu quả nhất |
| :--- | :--- | :--- | :--- |
| **Caesar** | Cực nhỏ ($26$) | Số lượng khóa quá ít, không thể che giấu cấu trúc thống kê. | Vét cạn (Brute-force) toàn bộ 26 trường hợp; phân tích tần suất ký tự đơn lẻ. |
| **Vigenère** | $26^m$ ($m$ là độ dài khóa) | Khóa bị lặp lại theo chu kỳ cố định $m$. | Phương pháp Kasiski / Friedman tìm độ dài chu kỳ $m$, sau đó chia bản mã thành $m$ cột để giải như $m$ mật mã Caesar độc lập. |
| **Rail Fence** | Rất nhỏ ($2 \dots L-1$) | Không làm thay đổi ký tự mà chỉ xáo trộn vị trí; số lượng hàng khả dĩ rất ít. | Thử duyệt số hàng từ $2$ đến $n$; giữ nguyên biểu đồ tần suất xuất hiện chữ cái của ngôn ngữ gốc. |
| **Playfair** | $\approx 25! \approx 1.55 \times 10^{25}$ | Thay thế theo cặp chữ cái cố định; ma trận chỉ có 25 ô (thiếu ký tự $J$). | Thống kê tần suất xuất hiện của các cặp ký tự phổ biến (Digrams như `TH`, `HE`, `IN`); tấn công Known-Plaintext để tái lập ma trận. |
| **Transposition** | $n!$ ($n$ là số cột) | Tần suất xuất hiện của mọi chữ cái được giữ nguyên $100\%$. | Thử độ dài khóa nhỏ, thực hiện ghép cặp các ký tự có xác suất đi liền nhau cao (Anagramming) để khôi phục thứ tự cột. |
| **Autokey** | Phụ thuộc khóa đầu | Dòng khóa chứa chính các ký tự bản rõ sau phần khóa khởi tạo. | Đoán các từ thông dụng phổ biến (như `THE`, `AND`, `THAT`) tại các vị trí nghi ngờ để dò ngược dần toàn bộ chuỗi khóa. |

### 2. Nhận xét và đánh giá tổng quan về an toàn thông tin
1. **Nguyên lý Kerckhoffs:** Độ an toàn của một hệ mật mã phải hoàn toàn phụ thuộc vào tính bí mật của khóa (Key), chứ không được phụ thuộc vào việc giữ bí mật thuật toán (Security through obscurity). Toàn bộ 6 thuật toán cổ điển trên đều bị phá vỡ dễ dàng một khi thuật toán bị công khai do không gian khóa quá bé hoặc không bảo toàn được tính ngẫu nhiên thống kê.
2. **Khái niệm Hoang mang (Confusion) và Khuếch tán (Diffusion):**
   - Các mật mã thay thế cổ điển (Caesar, Vigenère) chỉ có tính chất thay thế (Substitution) yếu ớt, không đạt được tính **Hoang mang** (mối quan hệ giữa bản rõ, khóa và bản mã quá tuyến tính).
   - Các mật mã hoán vị (Rail Fence, Hoán vị cột) chỉ dịch chuyển vị trí, không thay đổi giá trị chữ cái, hoàn toàn không đạt được tính **Khuếch tán** (sự thay đổi 1 bit của bản rõ không làm xáo trộn toàn bộ bản mã).
3. **Ý nghĩa thực tiễn đối với mật mã hiện đại:** Các giải thuật hiện đại (như AES, RSA, ECC) đã kết hợp nhiều vòng lặp đan xen giữa thay thế (S-Box) và hoán vị (P-Box), sử dụng không gian khóa khổng lồ ($2^{128}, 2^{256}$ bit) để chống lại triệt để cả hai phương pháp tấn công vét cạn và tấn công vi sai/tuyến tính.

---

## VIII. DANH MỤC MINH CHỨNG THỰC NGHIỆM VÀ TÀI LIỆU THAM KHẢO

### 1. Danh mục các minh chứng thực nghiệm trong bài Lab

| Ký hiệu | Nội dung minh chứng thực nghiệm | Vị trí / Đối tượng kiểm chứng | Kết quả đánh giá |
| :---: | :--- | :--- | :---: |
| **H1** | Cấu hình `pom.xml` nạp `mssql-jdbc` và biên dịch Maven thành công | Thư mục `LAB1_MaHoaCoDien`: `mvn compile` | **BUILD SUCCESS** |
| **H2** | Giao diện và kết quả mã hóa/giải mã thuật toán Caesar | Form `frm_Caesar` (`HELLO` $\to$ `KHOOR`) | **ĐẠT (PASS)** |
| **H3** | Giao diện và kết quả mã hóa/giải mã thuật toán Vigenère | Form `frm_Vigenere` (`HELLO` $\to$ `RIJVS`) | **ĐẠT (PASS)** |
| **H4** | Giao diện và kết quả mã hóa/giải mã thuật toán Rail Fence | Form `frm_RailFence` (`HELLO WORLD` $\to$ `HOREL OLLWD`) | **ĐẠT (PASS)** |
| **H5** | Giao diện và kết quả mã hóa/giải mã thuật toán Playfair | Form `frm_Playfair` (`HELLOWORLD` $\to$ `GYIZSCOKCFBU`) | **ĐẠT (PASS)** |
| **H6** | Giao diện và kết quả mã hóa/giải mã hoán vị cột (Transposition) | Form `frm_Transposition` (`BAOMAT` $\to$ `AAOTBM`) | **ĐẠT (PASS)** |
| **H7** | Giao diện và kết quả mã hóa/giải mã thuật toán Autokey | Form `frm_Autokey` (`HELLO` $\to$ `RIJSS`) | **ĐẠT (PASS)** |
| **H8** | Giao diện tổng hợp `frm_TongHop` tích hợp ComboBox và phím chuyển đổi | Cửa sổ `frm_TongHop` | **ĐẠT (PASS)** |
| **H9** | Kết nối SQL Server thành công và bảng hiển thị lịch sử `frm_LichSu` | Cửa sổ `frm_KetNoiSQL` & `frm_LichSu` | **ĐẠT (PASS)** |
| **H10**| Màn hình phân tích mật mã `frm_TanCong` (Brute-force & Known Plaintext)| Cửa sổ `frm_TanCong` | **ĐẠT (PASS)** |
| **H11**| Menu chính điều hướng chương trình `frm_Menu` mở các form con | Cửa sổ `frm_Menu` (Run Project F6) | **ĐẠT (PASS)** |

### 2. Tài liệu tham khảo
* **[1]** Bộ môn An toàn và Bảo mật Thông tin – *Tài liệu hướng dẫn thực hành LAB 1: Mã hóa đối xứng cổ điển*, 2026.
* **[2]** William Stallings – *Cryptography and Network Security: Principles and Practice* (7th Edition), Pearson.
* **[3]** Alfred J. Menezes, Paul C. van Oorschot, Scott A. Vanstone – *Handbook of Applied Cryptography*, CRC Press.
* **[4]** Oracle Corporation – *Java Swing Architecture and NetBeans GUI Builder Reference Guide*.
* **[5]** Microsoft Corporation – *Microsoft JDBC Driver for SQL Server Documentation*.

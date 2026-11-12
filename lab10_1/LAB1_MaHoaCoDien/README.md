# LAB 1: MÃ HÓA ĐỐI XỨNG CỔ ĐIỂN
## Môn học: Thực hành An toàn và Bảo mật Hệ thống Thông tin
* **Sinh viên thực hiện:** Nguyễn Tạ Hiếu
* **MSSV:** 1150080015
* **Lớp:** 11_THMT

### Cấu trúc Project (Package: `week_01`):
1. **Tiện ích chung:** `FileTextIO.java` (Đọc/Ghi file text UTF-8).
2. **6 Thuật toán cơ bản:**
   - Caesar: `CaesarCipher.java`, `frm_Caesar.java`
   - Vigenère: `VigenereCipher.java`, `frm_Vigenere.java`
   - Rail Fence: `RailFenceCipher.java`, `frm_RailFence.java`
   - Playfair: `PlayfairCipher.java`, `frm_Playfair.java`
   - Hoán vị cột (Transposition): `TranspositionCipher.java`, `frm_Transposition.java`
   - Autokey: `AutokeyCipher.java`, `frm_Autokey.java`
3. **Bài nâng cao 01 (Tổng hợp & SQL Server):**
   - `CipherTongHop.java`, `frm_TongHop.java`
   - `DbConfig.java`, `HistoryDao.java`, `frm_KetNoiSQL.java`, `frm_LichSu.java`
4. **Bài nâng cao 02 (Cryptanalysis):**
   - `AttackDemos.java`, `frm_TanCong.java` (Brute-force Caesar, Tần suất, Known Plaintext Vigenère)
5. **Menu điều hướng trung tâm:** `frm_Menu.java`

### Hướng dẫn biên dịch & chạy:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="week_01.frm_Menu"
```

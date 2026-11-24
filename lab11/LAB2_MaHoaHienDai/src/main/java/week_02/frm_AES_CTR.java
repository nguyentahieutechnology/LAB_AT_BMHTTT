package week_02;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class frm_AES_CTR extends JFrame {

    private JTextArea txt_plaintext;
    private JTextField txt_key;
    private JButton btn_genkey;
    private JTextField txt_nonce;
    private JButton btn_gennonce;
    private JTextArea txt_ciphertext;
    private JButton btn_encrypt;
    private JButton btn_decrypt;
    private JButton btn_save;
    private JButton btn_open;
    private JLabel lbl_status;

    public frm_AES_CTR() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("AES-CTR Cipher");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setSize(630, 520);
        setResizable(false);

        JLabel lbl_title = new JLabel("BÀI NÂNG CAO 01 - AES-256 CHẾ ĐỘ CTR", SwingConstants.CENTER);
        lbl_title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl_title.setBounds(20, 15, 570, 30);
        add(lbl_title);

        JLabel lbl_p = new JLabel("Plaintext:");
        lbl_p.setBounds(30, 55, 100, 22);
        add(lbl_p);

        txt_plaintext = new JTextArea();
        txt_plaintext.setLineWrap(true);
        txt_plaintext.setRows(3);
        JScrollPane sp_p = new JScrollPane(txt_plaintext);
        sp_p.setBounds(30, 80, 555, 60);
        add(sp_p);

        // Hàng Key
        JLabel lbl_k = new JLabel("Key (Base64):");
        lbl_k.setBounds(30, 150, 90, 25);
        add(lbl_k);

        txt_key = new JTextField();
        txt_key.setBounds(120, 150, 310, 25);
        add(txt_key);

        btn_genkey = new JButton("Generate Key");
        btn_genkey.setBounds(440, 150, 145, 25);
        btn_genkey.addActionListener(e -> btn_genkeyActionPerformed());
        add(btn_genkey);

        // Hàng Nonce
        JLabel lbl_n = new JLabel("Nonce (Base64):");
        lbl_n.setBounds(30, 185, 100, 25);
        add(lbl_n);

        txt_nonce = new JTextField();
        txt_nonce.setBounds(120, 185, 310, 25);
        add(txt_nonce);

        btn_gennonce = new JButton("Generate Nonce");
        btn_gennonce.setBounds(440, 185, 145, 25);
        btn_gennonce.addActionListener(e -> btn_gennonceActionPerformed());
        add(btn_gennonce);

        JLabel lbl_c = new JLabel("Ciphertext:");
        lbl_c.setBounds(30, 220, 100, 22);
        add(lbl_c);

        txt_ciphertext = new JTextArea();
        txt_ciphertext.setLineWrap(true);
        txt_ciphertext.setRows(3);
        JScrollPane sp_c = new JScrollPane(txt_ciphertext);
        sp_c.setBounds(30, 245, 555, 60);
        add(sp_c);

        btn_encrypt = new JButton("Encrypt");
        btn_encrypt.setBounds(50, 325, 110, 32);
        btn_encrypt.addActionListener(e -> btn_encryptActionPerformed());
        add(btn_encrypt);

        btn_decrypt = new JButton("Decrypt");
        btn_decrypt.setBounds(180, 325, 110, 32);
        btn_decrypt.addActionListener(e -> btn_decryptActionPerformed());
        add(btn_decrypt);

        btn_save = new JButton("Save File");
        btn_save.setBounds(320, 325, 120, 32);
        btn_save.addActionListener(e -> btn_saveActionPerformed());
        add(btn_save);

        btn_open = new JButton("Open File");
        btn_open.setBounds(460, 325, 120, 32);
        btn_open.addActionListener(e -> btn_openActionPerformed());
        add(btn_open);

        lbl_status = new JLabel("<html>Sẵn sàng. Bấm Generate Key và Generate Nonce trước.</html>", SwingConstants.CENTER);
        lbl_status.setBounds(30, 375, 555, 50);
        add(lbl_status);
    }

    private void btn_genkeyActionPerformed() {
        txt_key.setText(AESCTRCipher.newKey());
        lbl_status.setText("Đã tạo khóa ngẫu nhiên 256 bit.");
    }

    private void btn_gennonceActionPerformed() {
        txt_nonce.setText(AESCTRCipher.newNonce());
        lbl_status.setText("Đã tạo nonce ngẫu nhiên 96 bit. Mỗi lần mã hóa nên tạo nonce mới.");
    }

    private void btn_encryptActionPerformed() {
        String plaintext = txt_plaintext.getText();
        if (plaintext.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Plaintext trước khi mã hóa.");
            return;
        }
        try {
            String ciphertext = AESCTRCipher.encrypt(plaintext, txt_key.getText(), txt_nonce.getText());
            txt_ciphertext.setText(ciphertext);
            lbl_status.setText("<html>Đã mã hóa AES-CTR. " + HistoryDao.tryInsert("AES-CTR", "ENCRYPT", null, plaintext.length(), ciphertext.length()) + "</html>");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Mã hóa thất bại:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_decryptActionPerformed() {
        String ciphertext = txt_ciphertext.getText();
        if (ciphertext.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Ciphertext trước khi giải mã.");
            return;
        }
        try {
            String plaintext = AESCTRCipher.decrypt(ciphertext, txt_key.getText(), txt_nonce.getText());
            txt_plaintext.setText(plaintext);
            lbl_status.setText("<html>Đã giải mã AES-CTR. " + HistoryDao.tryInsert("AES-CTR", "DECRYPT", null, ciphertext.length(), plaintext.length()) + "</html>");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giải mã thất bại: sai Key hoặc bản mã bị sửa.\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_saveActionPerformed() {
        if (txt_ciphertext.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Chưa có bản mã để lưu.");
            return;
        }
        String content = AESCTRCipher.toFile(txt_nonce.getText(), txt_ciphertext.getText());
        String path = FileTextIO.saveString(this, content, "aes_ctr_ciphertext.txt");
        if (path != null) {
            lbl_status.setText("Đã lưu nonce + bản mã (không lưu Key) vào: " + path);
        }
    }

    private void btn_openActionPerformed() {
        String content = FileTextIO.openString(this);
        if (content == null) {
            return;
        }
        try {
            String[] parts = AESCTRCipher.fromFile(content);
            txt_nonce.setText(parts[0]);
            txt_ciphertext.setText(parts[1]);
            lbl_status.setText("Đã mở tệp. Nhập đúng Key rồi bấm Decrypt.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Tệp không hợp lệ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frm_AES_CTR().setVisible(true));
    }
}
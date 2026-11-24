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

public class frm_Twofish extends JFrame {

    private JTextArea txt_plaintext;
    private JTextField txt_key;
    private JTextField txt_iv;
    private JButton btn_iv;
    private JTextArea txt_ciphertext;
    private JButton btn_encrypt;
    private JButton btn_decrypt;
    private JButton btn_save;
    private JButton btn_open;
    private JLabel lbl_status;

    public frm_Twofish() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Twofish Cipher");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setSize(630, 520);
        setResizable(false);

        JLabel lbl_title = new JLabel("MẬT MÃ TWOFISH (CBC)", SwingConstants.CENTER);
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

        JLabel lbl_k = new JLabel("Key:");
        lbl_k.setBounds(30, 150, 50, 25);
        add(lbl_k);

        txt_key = new JTextField("Twofish@Lab2");
        txt_key.setBounds(80, 150, 200, 25);
        add(txt_key);

        JLabel lbl_k_hint = new JLabel("(Mật khẩu bất kỳ, khóa = SHA-256)");
        lbl_k_hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl_k_hint.setBounds(290, 150, 290, 25);
        add(lbl_k_hint);

        // Hàng IV
        JLabel lbl_iv = new JLabel("IV (HEX):");
        lbl_iv.setBounds(30, 185, 60, 25);
        add(lbl_iv);

        txt_iv = new JTextField();
        txt_iv.setBounds(80, 185, 330, 25);
        add(txt_iv);

        btn_iv = new JButton("Generate IV");
        btn_iv.setBounds(425, 185, 120, 25);
        btn_iv.addActionListener(e -> btn_ivActionPerformed());
        add(btn_iv);

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

        lbl_status = new JLabel("<html>Sẵn sàng. Bấm Generate IV trước khi Encrypt.</html>", SwingConstants.CENTER);
        lbl_status.setBounds(30, 375, 555, 50);
        add(lbl_status);
    }

    private void btn_ivActionPerformed() {
        txt_iv.setText(TwofishCipher.newIv());
        lbl_status.setText("Đã tạo IV ngẫu nhiên 16 byte. Giữ nguyên IV này để giải mã.");
    }

    private void btn_encryptActionPerformed() {
        String plaintext = txt_plaintext.getText();
        if (plaintext.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Plaintext trước khi mã hóa.");
            return;
        }
        try {
            String ciphertext = TwofishCipher.encrypt(plaintext, txt_key.getText(), txt_iv.getText());
            txt_ciphertext.setText(ciphertext);
            lbl_status.setText("<html>Đã mã hóa Twofish. " + HistoryDao.tryInsert("Twofish", "ENCRYPT", null, plaintext.length(), ciphertext.length()) + "</html>");
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
            String plaintext = TwofishCipher.decrypt(ciphertext, txt_key.getText(), txt_iv.getText());
            txt_plaintext.setText(plaintext);
            lbl_status.setText("<html>Đã giải mã Twofish. " + HistoryDao.tryInsert("Twofish", "DECRYPT", null, ciphertext.length(), plaintext.length()) + "</html>");
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
        String content = TwofishCipher.toFile(txt_iv.getText(), txt_ciphertext.getText());
        String path = FileTextIO.saveString(this, content, "twofish_ciphertext.txt");
        if (path != null) {
            lbl_status.setText("Đã lưu IV + bản mã vào: " + path);
        }
    }

    private void btn_openActionPerformed() {
        String content = FileTextIO.openString(this);
        if (content == null) {
            return;
        }
        try {
            String[] parts = TwofishCipher.fromFile(content);
            txt_iv.setText(parts[0]);
            txt_ciphertext.setText(parts[1]);
            lbl_status.setText("Đã mở tệp, khôi phục IV và bản mã. Nhập đúng Key rồi bấm Decrypt.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Tệp không hợp lệ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frm_Twofish().setVisible(true));
    }
}

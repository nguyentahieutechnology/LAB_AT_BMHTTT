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

public class frm_DES extends JFrame {

    private JTextArea txt_plaintext;
    private JTextField txt_key;
    private JTextArea txt_ciphertext;
    private JButton btn_encrypt;
    private JButton btn_decrypt;
    private JButton btn_save;
    private JButton btn_open;
    private JLabel lbl_status;

    public frm_DES() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("DES Cipher");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setSize(630, 480);
        setResizable(false);

        JLabel lbl_title = new JLabel("MẬT MÃ DES", SwingConstants.CENTER);
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
        sp_p.setBounds(30, 80, 555, 65);
        add(sp_p);

        JLabel lbl_k = new JLabel("Key:");
        lbl_k.setBounds(30, 155, 60, 25);
        add(lbl_k);

        txt_key = new JTextField("12345678");
        txt_key.setBounds(90, 155, 200, 25);
        add(txt_key);

        JLabel lbl_k_hint = new JLabel("(Đúng 8 ký tự không dấu = 8 byte)");
        lbl_k_hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbl_k_hint.setBounds(300, 155, 250, 25);
        add(lbl_k_hint);

        JLabel lbl_c = new JLabel("Ciphertext:");
        lbl_c.setBounds(30, 190, 100, 22);
        add(lbl_c);

        txt_ciphertext = new JTextArea();
        txt_ciphertext.setLineWrap(true);
        txt_ciphertext.setRows(3);
        JScrollPane sp_c = new JScrollPane(txt_ciphertext);
        sp_c.setBounds(30, 215, 555, 65);
        add(sp_c);

        btn_encrypt = new JButton("Encrypt");
        btn_encrypt.setBounds(50, 295, 110, 32);
        btn_encrypt.addActionListener(e -> btn_encryptActionPerformed());
        add(btn_encrypt);

        btn_decrypt = new JButton("Decrypt");
        btn_decrypt.setBounds(180, 295, 110, 32);
        btn_decrypt.addActionListener(e -> btn_decryptActionPerformed());
        add(btn_decrypt);

        btn_save = new JButton("Save File");
        btn_save.setBounds(320, 295, 120, 32);
        btn_save.addActionListener(e -> btn_saveActionPerformed());
        add(btn_save);

        btn_open = new JButton("Open File");
        btn_open.setBounds(460, 295, 120, 32);
        btn_open.addActionListener(e -> btn_openActionPerformed());
        add(btn_open);

        lbl_status = new JLabel("<html>Sẵn sàng.</html>", SwingConstants.CENTER);
        lbl_status.setBounds(30, 345, 555, 50);
        add(lbl_status);
    }

    private void btn_encryptActionPerformed() {
        String plaintext = txt_plaintext.getText();
        if (plaintext.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Plaintext trước khi mã hóa.");
            return;
        }
        try {
            String ciphertext = DESCipher.encrypt(plaintext, txt_key.getText());
            txt_ciphertext.setText(ciphertext);
            lbl_status.setText("<html>Đã mã hóa DES. " + HistoryDao.tryInsert("DES", "ENCRYPT", null, plaintext.length(), ciphertext.length()) + "</html>");
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
            String plaintext = DESCipher.decrypt(ciphertext, txt_key.getText());
            txt_plaintext.setText(plaintext);
            lbl_status.setText("<html>Đã giải mã DES. " + HistoryDao.tryInsert("DES", "DECRYPT", null, ciphertext.length(), plaintext.length()) + "</html>");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giải mã thất bại: sai Key hoặc bản mã bị sửa.\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_saveActionPerformed() {
        String path = FileTextIO.saveText(this, txt_ciphertext, "des_ciphertext.txt");
        if (path != null) {
            lbl_status.setText("Đã lưu tệp: " + path);
        }
    }

    private void btn_openActionPerformed() {
        String path = FileTextIO.openText(this, txt_ciphertext);
        if (path != null) {
            lbl_status.setText("Đã mở tệp: " + path);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frm_DES().setVisible(true));
    }
}
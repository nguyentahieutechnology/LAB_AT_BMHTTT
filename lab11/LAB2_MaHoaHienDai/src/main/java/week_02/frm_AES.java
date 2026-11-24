package week_02;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class frm_AES extends JFrame {

    /** Tên người dùng đã Login thành công; null = chưa đăng nhập. */
    private String currentUser = null;

    private JTextField txt_username;
    private JPasswordField txt_password;
    private JButton btn_sql;
    private JButton btn_register;
    private JButton btn_login;
    private JTextField txt_ticket;
    private JTextArea txt_plaintext;
    private JTextField txt_key;
    private JTextArea txt_ciphertext;
    private JButton btn_encrypt;
    private JButton btn_decrypt;
    private JButton btn_save;
    private JButton btn_open;
    private JLabel lbl_status;

    public frm_AES() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("AES Cipher - Register / Login");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setSize(650, 540);
        setResizable(false);

        JLabel lbl_title = new JLabel("AES-GCM - ĐĂNG KÝ / ĐĂNG NHẬP (SQL SERVER)", SwingConstants.CENTER);
        lbl_title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lbl_title.setBounds(20, 15, 600, 30);
        add(lbl_title);

        // Hàng đăng ký / đăng nhập
        JLabel lbl_u = new JLabel("Username:");
        lbl_u.setBounds(30, 55, 75, 25);
        add(lbl_u);
        txt_username = new JTextField("sinhvien01");
        txt_username.setBounds(105, 55, 140, 25);
        add(txt_username);

        JLabel lbl_p = new JLabel("Password:");
        lbl_p.setBounds(255, 55, 70, 25);
        add(lbl_p);
        txt_password = new JPasswordField();
        txt_password.setBounds(325, 55, 140, 25);
        add(txt_password);

        JLabel lbl_p_hint = new JLabel("(≥ 8 ký tự)");
        lbl_p_hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl_p_hint.setBounds(470, 55, 100, 25);
        add(lbl_p_hint);

        // Hàng nút tài khoản
        btn_sql = new JButton("Kết nối SQL Server");
        btn_sql.setBounds(30, 88, 160, 28);
        btn_sql.addActionListener(e -> new frm_KetNoiSQL().setVisible(true));
        add(btn_sql);

        btn_register = new JButton("Register");
        btn_register.setBounds(205, 88, 105, 28);
        btn_register.addActionListener(e -> btn_registerActionPerformed());
        add(btn_register);

        btn_login = new JButton("Login");
        btn_login.setBounds(325, 88, 105, 28);
        btn_login.addActionListener(e -> btn_loginActionPerformed());
        add(btn_login);

        // Hàng Ticket
        JLabel lbl_t = new JLabel("Ticket:");
        lbl_t.setBounds(30, 124, 60, 25);
        add(lbl_t);
        txt_ticket = new JTextField();
        txt_ticket.setEditable(false);
        txt_ticket.setBounds(90, 124, 510, 25);
        add(txt_ticket);

        // Hàng Plaintext
        JLabel lbl_pt = new JLabel("Plaintext:");
        lbl_pt.setBounds(30, 155, 100, 20);
        add(lbl_pt);
        txt_plaintext = new JTextArea();
        txt_plaintext.setLineWrap(true);
        txt_plaintext.setRows(3);
        JScrollPane sp_p = new JScrollPane(txt_plaintext);
        sp_p.setBounds(30, 178, 570, 60);
        add(sp_p);

        // Hàng Key AES
        JLabel lbl_k = new JLabel("Key AES:");
        lbl_k.setBounds(30, 245, 65, 25);
        add(lbl_k);
        txt_key = new JTextField("MatKhauAES@2026");
        txt_key.setBounds(95, 245, 200, 25);
        add(txt_key);

        JLabel lbl_k_hint = new JLabel("(Mật khẩu tạo khóa AES-256 qua PBKDF2)");
        lbl_k_hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl_k_hint.setBounds(305, 245, 295, 25);
        add(lbl_k_hint);

        // Hàng Ciphertext
        JLabel lbl_ct = new JLabel("Ciphertext:");
        lbl_ct.setBounds(30, 276, 100, 20);
        add(lbl_ct);
        txt_ciphertext = new JTextArea();
        txt_ciphertext.setLineWrap(true);
        txt_ciphertext.setRows(3);
        JScrollPane sp_c = new JScrollPane(txt_ciphertext);
        sp_c.setBounds(30, 298, 570, 60);
        add(sp_c);

        // Các nút thao tác
        btn_encrypt = new JButton("Encrypt");
        btn_encrypt.setBounds(40, 370, 110, 32);
        btn_encrypt.addActionListener(e -> btn_encryptActionPerformed());
        add(btn_encrypt);

        btn_decrypt = new JButton("Decrypt");
        btn_decrypt.setBounds(180, 370, 110, 32);
        btn_decrypt.addActionListener(e -> btn_decryptActionPerformed());
        add(btn_decrypt);

        btn_save = new JButton("Save File");
        btn_save.setBounds(320, 370, 120, 32);
        btn_save.addActionListener(e -> btn_saveActionPerformed());
        add(btn_save);

        btn_open = new JButton("Open File");
        btn_open.setBounds(460, 370, 120, 32);
        btn_open.addActionListener(e -> btn_openActionPerformed());
        add(btn_open);

        lbl_status = new JLabel("<html>Sẵn sàng. Hãy đăng nhập trước khi mã hóa AES.</html>", SwingConstants.CENTER);
        lbl_status.setBounds(30, 415, 570, 45);
        add(lbl_status);
    }

    private void btn_registerActionPerformed() {
        try {
            String user = txt_username.getText().trim();
            UserDao.register(user, txt_password.getPassword());
            HistoryDao.tryInsert("AES", "REGISTER", user, 0, 0);
            lbl_status.setText("Register thành công: " + user + ". Bấm Login để đăng nhập.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi SQL Server: " + ex.getMessage(), "Lỗi SQL Server", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_loginActionPerformed() {
        try {
            String user = txt_username.getText().trim();
            if (UserDao.login(user, txt_password.getPassword())) {
                currentUser = user;
                txt_ticket.setText(UserDao.newTicket());
                HistoryDao.tryInsert("AES", "LOGIN", user, 0, 0);
                lbl_status.setText("Login thành công: " + user + ". Có thể dùng Encrypt/Decrypt.");
            } else {
                currentUser = null;
                txt_ticket.setText("");
                JOptionPane.showMessageDialog(this, "Sai username hoặc mật khẩu.", "Login thất bại", JOptionPane.ERROR_MESSAGE);
            }
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi SQL Server: " + ex.getMessage(), "Lỗi SQL Server", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_encryptActionPerformed() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Hãy Login trước khi dùng AES.");
            return;
        }
        String plaintext = txt_plaintext.getText();
        if (plaintext.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Plaintext trước khi mã hóa.");
            return;
        }
        try {
            String ciphertext = AESCipher.encrypt(plaintext, txt_key.getText());
            txt_ciphertext.setText(ciphertext);
            lbl_status.setText("<html>Đã mã hóa AES. " + HistoryDao.tryInsert("AES", "ENCRYPT", currentUser, plaintext.length(), ciphertext.length()) + "</html>");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Mã hóa thất bại:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_decryptActionPerformed() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Hãy Login trước khi dùng AES.");
            return;
        }
        String ciphertext = txt_ciphertext.getText();
        if (ciphertext.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Ciphertext trước khi giải mã.");
            return;
        }
        try {
            String plaintext = AESCipher.decrypt(ciphertext, txt_key.getText());
            txt_plaintext.setText(plaintext);
            lbl_status.setText("<html>Đã giải mã AES. " + HistoryDao.tryInsert("AES", "DECRYPT", currentUser, ciphertext.length(), plaintext.length()) + "</html>");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giải mã thất bại: sai Key hoặc bản mã bị sửa.\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_saveActionPerformed() {
        String path = FileTextIO.saveText(this, txt_ciphertext, "aes_ciphertext.txt");
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
        java.awt.EventQueue.invokeLater(() -> new frm_AES().setVisible(true));
    }
}
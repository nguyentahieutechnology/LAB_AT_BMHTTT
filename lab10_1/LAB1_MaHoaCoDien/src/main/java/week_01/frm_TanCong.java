package week_01;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class frm_TanCong extends JFrame {

    private JTextArea txt_cipher;
    private JButton btn_bruteforce;
    private JButton btn_frequency;
    private JTextField txt_known_plain;
    private JTextField txt_known_cipher;
    private JButton btn_known;
    private JTextArea txt_result;
    private JLabel lbl_status;

    public frm_TanCong() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Phân tích mật mã cổ điển");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setSize(780, 680);
        setResizable(false);

        JLabel lbl_title = new JLabel("PHÂN TÍCH (TẤN CÔNG) MẬT MÃ CỔ ĐIỂN", SwingConstants.CENTER);
        lbl_title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl_title.setBounds(20, 15, 720, 32);
        add(lbl_title);

        // Section 1: Caesar Brute force & Frequency
        JLabel lbl_c = new JLabel("Ciphertext:");
        lbl_c.setBounds(30, 60, 100, 22);
        add(lbl_c);

        txt_cipher = new JTextArea("KHOOR");
        txt_cipher.setLineWrap(true);
        txt_cipher.setRows(2);
        JScrollPane sp_cipher = new JScrollPane(txt_cipher);
        sp_cipher.setBounds(120, 60, 615, 60);
        add(sp_cipher);

        btn_bruteforce = new JButton("Brute Force Caesar");
        btn_bruteforce.setBounds(120, 130, 180, 32);
        btn_bruteforce.addActionListener(e -> btn_bruteforceActionPerformed());
        add(btn_bruteforce);

        btn_frequency = new JButton("Frequency Analysis");
        btn_frequency.setBounds(315, 130, 180, 32);
        btn_frequency.addActionListener(e -> btn_frequencyActionPerformed());
        add(btn_frequency);

        // Section 2: Known Plaintext Vigenere
        JLabel lbl_kp = new JLabel("Bản rõ đã biết:");
        lbl_kp.setBounds(30, 180, 100, 25);
        add(lbl_kp);

        txt_known_plain = new JTextField("HELLO");
        txt_known_plain.setBounds(135, 180, 130, 28);
        add(txt_known_plain);

        JLabel lbl_kc = new JLabel("Bản mã:");
        lbl_kc.setBounds(280, 180, 60, 25);
        add(lbl_kc);

        txt_known_cipher = new JTextField("RIJVS");
        txt_known_cipher.setBounds(345, 180, 130, 28);
        add(txt_known_cipher);

        btn_known = new JButton("Known Plaintext (Vigenère)");
        btn_known.setBounds(490, 178, 245, 32);
        btn_known.addActionListener(e -> btn_knownActionPerformed());
        add(btn_known);

        // Section 3: Result
        JLabel lbl_r = new JLabel("Kết quả:");
        lbl_r.setBounds(30, 225, 100, 22);
        add(lbl_r);

        txt_result = new JTextArea();
        txt_result.setEditable(false);
        txt_result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane sp_result = new JScrollPane(txt_result);
        sp_result.setBounds(30, 250, 705, 330);
        add(sp_result);

        lbl_status = new JLabel("Sẵn sàng.", SwingConstants.LEFT);
        lbl_status.setBounds(30, 590, 705, 30);
        add(lbl_status);
    }

    private void btn_bruteforceActionPerformed() {
        String cipher = txt_cipher.getText().trim();
        if (cipher.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Ciphertext.");
            return;
        }
        txt_result.setText(AttackDemos.caesarBruteForce(cipher));
        txt_result.setCaretPosition(0);
        lbl_status.setText("Đã thử đủ 26 khóa Caesar.");
    }

    private void btn_frequencyActionPerformed() {
        String cipher = txt_cipher.getText().trim();
        if (cipher.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Ciphertext.");
            return;
        }
        txt_result.setText(AttackDemos.frequency(cipher));
        txt_result.setCaretPosition(0);
        lbl_status.setText("Đã thống kê tần suất chữ cái.");
    }

    private void btn_knownActionPerformed() {
        try {
            String key = AttackDemos.knownPlaintext(txt_known_plain.getText(), txt_known_cipher.getText());
            txt_result.setText("Khóa Vigenère suy ra từng vị trí: " + key
                    + "\nNếu khóa ngắn hơn bản rõ, khóa sẽ lặp lại (ví dụ KEY KEY ...).");
            lbl_status.setText("Khóa suy ra: " + key);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frm_TanCong().setVisible(true));
    }
}

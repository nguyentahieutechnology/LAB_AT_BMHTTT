package week_01;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class frm_TongHop extends JFrame {

    private JComboBox<String> cbo_algorithm;
    private JLabel lbl_hint;
    private JTextArea txt_plaintext;
    private JTextField txt_key;
    private JTextArea txt_ciphertext;
    private JButton btn_encrypt;
    private JButton btn_decrypt;
    private JButton btn_open;
    private JButton btn_save;
    private JButton btn_sql;
    private JButton btn_history;
    private JLabel lbl_status;

    public frm_TongHop() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Chương trình tổng hợp - 6 thuật toán");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setSize(780, 680);
        setResizable(false);

        JLabel lbl_title = new JLabel("CHƯƠNG TRÌNH TỔNG HỢP 6 THUẬT TOÁN CỔ ĐIỂN", SwingConstants.CENTER);
        lbl_title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl_title.setBounds(20, 15, 720, 32);
        add(lbl_title);

        JLabel lbl_algo = new JLabel("Thuật toán:");
        lbl_algo.setBounds(30, 60, 90, 25);
        add(lbl_algo);

        cbo_algorithm = new JComboBox<>(CipherTongHop.ALGORITHMS);
        cbo_algorithm.setBounds(120, 60, 180, 28);
        cbo_algorithm.addActionListener(e -> {
            String algo = (String) cbo_algorithm.getSelectedItem();
            txt_key.setText(CipherTongHop.sampleKey(algo));
            lbl_hint.setText("Khóa mẫu: " + CipherTongHop.sampleKey(algo) + " (" + CipherTongHop.keyHint(algo) + ")");
        });
        add(cbo_algorithm);

        lbl_hint = new JLabel("Khóa mẫu: 3 (" + CipherTongHop.keyHint("Caesar") + ")");
        lbl_hint.setBounds(315, 60, 420, 25);
        add(lbl_hint);

        JLabel lbl_p = new JLabel("Plaintext:");
        lbl_p.setBounds(30, 98, 100, 22);
        add(lbl_p);

        txt_plaintext = new JTextArea();
        txt_plaintext.setLineWrap(true);
        txt_plaintext.setRows(3);
        JScrollPane sp_plain = new JScrollPane(txt_plaintext);
        sp_plain.setBounds(30, 122, 705, 95);
        add(sp_plain);

        JLabel lbl_k = new JLabel("Key:");
        lbl_k.setBounds(30, 228, 80, 25);
        add(lbl_k);

        txt_key = new JTextField("3");
        txt_key.setBounds(120, 228, 200, 28);
        add(txt_key);

        JLabel lbl_c = new JLabel("Ciphertext:");
        lbl_c.setBounds(30, 265, 100, 22);
        add(lbl_c);

        txt_ciphertext = new JTextArea();
        txt_ciphertext.setLineWrap(true);
        txt_ciphertext.setRows(3);
        JScrollPane sp_cipher = new JScrollPane(txt_ciphertext);
        sp_cipher.setBounds(30, 290, 705, 95);
        add(sp_cipher);

        // Action Buttons Row 1
        btn_encrypt = new JButton("Encrypt");
        btn_encrypt.setBounds(30, 400, 110, 32);
        btn_encrypt.addActionListener(e -> btn_encryptActionPerformed());
        add(btn_encrypt);

        btn_decrypt = new JButton("Decrypt");
        btn_decrypt.setBounds(150, 400, 110, 32);
        btn_decrypt.addActionListener(e -> btn_decryptActionPerformed());
        add(btn_decrypt);

        btn_open = new JButton("Open File");
        btn_open.setBounds(270, 400, 110, 32);
        btn_open.addActionListener(e -> {
            String path = FileTextIO.openText(this, txt_ciphertext);
            if (path != null) lbl_status.setText("Đã mở tệp: " + path);
        });
        add(btn_open);

        btn_save = new JButton("Save File");
        btn_save.setBounds(390, 400, 110, 32);
        btn_save.addActionListener(e -> {
            String path = FileTextIO.saveText(this, txt_ciphertext, "tonghop_output.txt");
            if (path != null) lbl_status.setText("Đã lưu tệp: " + path);
        });
        add(btn_save);

        // Action Buttons Row 2 (Database connection)
        btn_sql = new JButton("Kết nối SQL Server");
        btn_sql.setBounds(30, 445, 170, 32);
        btn_sql.addActionListener(e -> new frm_KetNoiSQL().setVisible(true));
        add(btn_sql);

        btn_history = new JButton("Xem lịch sử");
        btn_history.setBounds(210, 445, 130, 32);
        btn_history.addActionListener(e -> new frm_LichSu().setVisible(true));
        add(btn_history);

        lbl_status = new JLabel("Sẵn sàng.", SwingConstants.LEFT);
        lbl_status.setBounds(30, 495, 705, 30);
        add(lbl_status);
    }

    private void btn_encryptActionPerformed() {
        String algorithm = (String) cbo_algorithm.getSelectedItem();
        String input = txt_plaintext.getText();
        try {
            String result = CipherTongHop.encrypt(algorithm, input, txt_key.getText());
            txt_ciphertext.setText(result);
            String saved = HistoryDao.tryInsert(algorithm, "ENCRYPT", input, result);
            lbl_status.setText("Đã mã hóa " + algorithm + ". " + saved);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_decryptActionPerformed() {
        String algorithm = (String) cbo_algorithm.getSelectedItem();
        String input = txt_ciphertext.getText();
        try {
            String result = CipherTongHop.decrypt(algorithm, input, txt_key.getText());
            txt_plaintext.setText(result);
            String saved = HistoryDao.tryInsert(algorithm, "DECRYPT", input, result);
            lbl_status.setText("Đã giải mã " + algorithm + ". " + saved);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frm_TongHop().setVisible(true));
    }
}

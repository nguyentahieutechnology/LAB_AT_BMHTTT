package week_02;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class frm_Menu extends JFrame {

    private JButton btn_des;
    private JButton btn_tripledes;
    private JButton btn_aes;
    private JButton btn_rc4;
    private JButton btn_twofish;
    private JButton btn_aesctr;
    private JButton btn_sql;
    private JButton btn_history;

    public frm_Menu() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("LAB 2 - Mã hóa đối xứng hiện đại");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setSize(580, 400);
        setResizable(false);

        JLabel lbl_title = new JLabel("LAB 2 - MÃ HÓA ĐỐI XỨNG HIỆN ĐẠI", SwingConstants.CENTER);
        lbl_title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl_title.setBounds(20, 18, 525, 30);
        add(lbl_title);

        JLabel lbl_sub = new JLabel("Chọn một bài để mở form tương ứng:", SwingConstants.CENTER);
        lbl_sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl_sub.setBounds(20, 50, 525, 25);
        add(lbl_sub);

        // Cột trái: Thuật toán cơ bản 1-4
        btn_des = new JButton("2.1 DES");
        btn_des.setBounds(40, 95, 230, 36);
        btn_des.addActionListener(e -> new frm_DES().setVisible(true));
        add(btn_des);

        btn_tripledes = new JButton("2.2 TripleDES");
        btn_tripledes.setBounds(40, 145, 230, 36);
        btn_tripledes.addActionListener(e -> new frm_TripleDES().setVisible(true));
        add(btn_tripledes);

        btn_aes = new JButton("2.3 AES + Login");
        btn_aes.setBounds(40, 195, 230, 36);
        btn_aes.addActionListener(e -> new frm_AES().setVisible(true));
        add(btn_aes);

        btn_rc4 = new JButton("2.4 RC4");
        btn_rc4.setBounds(40, 245, 230, 36);
        btn_rc4.addActionListener(e -> new frm_RC4().setVisible(true));
        add(btn_rc4);

        // Cột phải: Thuật toán nâng cao & CSDL
        btn_twofish = new JButton("2.5 Twofish");
        btn_twofish.setBounds(295, 95, 230, 36);
        btn_twofish.addActionListener(e -> new frm_Twofish().setVisible(true));
        add(btn_twofish);

        btn_aesctr = new JButton("2.6 Nâng cao - AES-CTR");
        btn_aesctr.setBounds(295, 145, 230, 36);
        btn_aesctr.addActionListener(e -> new frm_AES_CTR().setVisible(true));
        add(btn_aesctr);

        btn_sql = new JButton("Kết nối SQL Server");
        btn_sql.setBounds(295, 195, 230, 36);
        btn_sql.addActionListener(e -> new frm_KetNoiSQL().setVisible(true));
        add(btn_sql);

        btn_history = new JButton("Xem lịch sử");
        btn_history.setBounds(295, 245, 230, 36);
        btn_history.addActionListener(e -> new frm_LichSu().setVisible(true));
        add(btn_history);

        // Chú thích Python
        JLabel lbl_py = new JLabel("Bài nâng cao 02 (AES-CBC) viết bằng Python: chạy tệp aes_cbc_gui.py", SwingConstants.CENTER);
        lbl_py.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl_py.setBounds(20, 310, 525, 25);
        add(lbl_py);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frm_Menu().setVisible(true));
    }
}
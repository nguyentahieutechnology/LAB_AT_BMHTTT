package week_01;

import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class frm_Menu extends JFrame {

    private JButton btn_caesar;
    private JButton btn_vigenere;
    private JButton btn_railfence;
    private JButton btn_playfair;
    private JButton btn_transposition;
    private JButton btn_autokey;
    private JButton btn_tonghop;
    private JButton btn_tancong;
    private JButton btn_sql;

    public frm_Menu() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("LAB 1 - Mã hóa đối xứng cổ điển");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setSize(700, 480);
        setResizable(false);

        JLabel lbl_title = new JLabel("LAB 1 - MÃ HÓA ĐỐI XỨNG CỔ ĐIỂN", SwingConstants.CENTER);
        lbl_title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl_title.setBounds(20, 20, 640, 35);
        add(lbl_title);

        JLabel lbl_sub = new JLabel("Chọn một bài để mở form tương ứng:", SwingConstants.CENTER);
        lbl_sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl_sub.setBounds(20, 60, 640, 25);
        add(lbl_sub);

        JPanel panelButtons = new JPanel(new GridLayout(3, 3, 15, 15));
        panelButtons.setBounds(50, 100, 580, 230);

        btn_caesar = new JButton("1.1 Caesar");
        btn_caesar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn_caesar.addActionListener(e -> new frm_Caesar().setVisible(true));
        panelButtons.add(btn_caesar);

        btn_vigenere = new JButton("1.2 Vigenère");
        btn_vigenere.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn_vigenere.addActionListener(e -> new frm_Vigenere().setVisible(true));
        panelButtons.add(btn_vigenere);

        btn_railfence = new JButton("1.3 Rail Fence");
        btn_railfence.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn_railfence.addActionListener(e -> new frm_RailFence().setVisible(true));
        panelButtons.add(btn_railfence);

        btn_playfair = new JButton("1.4 Playfair");
        btn_playfair.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn_playfair.addActionListener(e -> new frm_Playfair().setVisible(true));
        panelButtons.add(btn_playfair);

        btn_transposition = new JButton("1.5 Transposition");
        btn_transposition.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn_transposition.addActionListener(e -> new frm_Transposition().setVisible(true));
        panelButtons.add(btn_transposition);

        btn_autokey = new JButton("1.6 Autokey");
        btn_autokey.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn_autokey.addActionListener(e -> new frm_Autokey().setVisible(true));
        panelButtons.add(btn_autokey);

        btn_tonghop = new JButton("Nâng cao 01 - Tổng hợp");
        btn_tonghop.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn_tonghop.addActionListener(e -> new frm_TongHop().setVisible(true));
        panelButtons.add(btn_tonghop);

        btn_tancong = new JButton("Nâng cao 02 - Tấn công");
        btn_tancong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn_tancong.addActionListener(e -> new frm_TanCong().setVisible(true));
        panelButtons.add(btn_tancong);

        btn_sql = new JButton("Kết nối SQL Server");
        btn_sql.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn_sql.addActionListener(e -> new frm_KetNoiSQL().setVisible(true));
        panelButtons.add(btn_sql);

        add(panelButtons);

        JLabel lbl_note = new JLabel("Mỗi form mở ra là một cửa sổ riêng; đóng form con không làm tắt chương trình.", SwingConstants.CENTER);
        lbl_note.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbl_note.setBounds(20, 360, 640, 30);
        add(lbl_note);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frm_Menu().setVisible(true));
    }
}

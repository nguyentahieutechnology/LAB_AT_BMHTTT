package week_01;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class frm_KetNoiSQL extends JFrame {

    private JTextField txt_server;
    private JTextField txt_port;
    private JTextField txt_database;
    private JTextField txt_user;
    private JPasswordField txt_password;
    private JButton btn_test;
    private JButton btn_history;
    private JLabel lbl_status;

    public frm_KetNoiSQL() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Kết nối Microsoft SQL Server");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setSize(500, 420);
        setResizable(false);

        JLabel lbl_title = new JLabel("KẾT NỐI MICROSOFT SQL SERVER", SwingConstants.CENTER);
        lbl_title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl_title.setBounds(20, 15, 450, 30);
        add(lbl_title);

        JLabel lbl_s = new JLabel("Server:");
        lbl_s.setBounds(40, 60, 100, 25);
        add(lbl_s);
        txt_server = new JTextField("127.0.0.1");
        txt_server.setBounds(140, 60, 300, 25);
        add(txt_server);

        JLabel lbl_p = new JLabel("Port:");
        lbl_p.setBounds(40, 95, 100, 25);
        add(lbl_p);
        txt_port = new JTextField("14333");
        txt_port.setBounds(140, 95, 300, 25);
        add(txt_port);

        JLabel lbl_d = new JLabel("Database:");
        lbl_d.setBounds(40, 130, 100, 25);
        add(lbl_d);
        txt_database = new JTextField("LAB1_MaHoaCoDien");
        txt_database.setBounds(140, 130, 300, 25);
        add(txt_database);

        JLabel lbl_u = new JLabel("User:");
        lbl_u.setBounds(40, 165, 100, 25);
        add(lbl_u);
        txt_user = new JTextField("lab1_user");
        txt_user.setBounds(140, 165, 300, 25);
        add(txt_user);

        JLabel lbl_pw = new JLabel("Password:");
        lbl_pw.setBounds(40, 200, 100, 25);
        add(lbl_pw);
        txt_password = new JPasswordField("LabUser@2026");
        txt_password.setBounds(140, 200, 300, 25);
        add(txt_password);

        btn_test = new JButton("Test Connection");
        btn_test.setBounds(100, 245, 140, 32);
        btn_test.addActionListener(e -> btn_testActionPerformed());
        add(btn_test);

        btn_history = new JButton("Xem lịch sử");
        btn_history.setBounds(260, 245, 130, 32);
        btn_history.addActionListener(e -> new frm_LichSu().setVisible(true));
        add(btn_history);

        lbl_status = new JLabel("<html>Chưa kết nối.</html>", SwingConstants.CENTER);
        lbl_status.setBounds(20, 290, 450, 70);
        add(lbl_status);
    }

    private void btn_testActionPerformed() {
        try {
            int port = Integer.parseInt(txt_port.getText().trim());
            DbConfig.set(txt_server.getText().trim(), port, txt_database.getText().trim(),
                    txt_user.getText().trim(), txt_password.getPassword());
            lbl_status.setText("<html><font color='green'>" + DbConfig.test() + "</font></html>");
        } catch (Exception ex) {
            DbConfig.clear();
            lbl_status.setText("<html><font color='red'>Kết nối thất bại: " + ex.getMessage() + "</font></html>");
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Không kết nối được SQL Server", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frm_KetNoiSQL().setVisible(true));
    }
}

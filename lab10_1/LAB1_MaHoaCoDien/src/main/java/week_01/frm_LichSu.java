package week_01;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class frm_LichSu extends JFrame {

    private JTable tbl_history;
    private JButton btn_refresh;
    private JLabel lbl_status;

    public frm_LichSu() {
        initComponents();
        setLocationRelativeTo(null);
        loadHistory();
    }

    private void initComponents() {
        setTitle("Lịch sử thao tác (SQL Server)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 480);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lbl_title = new JLabel("LỊCH SỬ THAO TÁC - BẢNG DBO.HISTORY", SwingConstants.CENTER);
        lbl_title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl_title.setPreferredSize(new Dimension(800, 40));
        topPanel.add(lbl_title, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        tbl_history = new JTable(HistoryDao.emptyModel());
        tbl_history.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(tbl_history);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        btn_refresh = new JButton("Tải lại");
        btn_refresh.setPreferredSize(new Dimension(110, 32));
        btn_refresh.addActionListener(e -> loadHistory());
        bottomPanel.add(btn_refresh, BorderLayout.WEST);

        lbl_status = new JLabel("Đang tải...", SwingConstants.LEFT);
        bottomPanel.add(lbl_status, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadHistory() {
        try {
            tbl_history.setModel(HistoryDao.loadLatest(50));
            lbl_status.setText("Đã tải " + tbl_history.getRowCount() + " dòng mới nhất từ dbo.history.");
        } catch (Exception ex) {
            lbl_status.setText("Không đọc được lịch sử: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frm_LichSu().setVisible(true));
    }
}

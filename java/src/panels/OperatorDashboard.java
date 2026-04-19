package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class OperatorDashboard extends JPanel {
    private JTable priorityTable, isolatedTable;
    private String userRole;

    public OperatorDashboard(String role) {
        this.userRole = role;
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(20, 20));

        // Header: High Alert
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel title = new JLabel("🚨 EMERGENCY OPERATIONS DESK");
        title.setFont(Theme.FONT_H1);
        title.setForeground(new Color(255, 69, 58)); 
        
        JLabel subtitle = new JLabel("Active SOP: " + userRole.toUpperCase() + " | Rapid Triage Mode");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Center: Two Main Action Tables
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 20));
        centerGrid.setOpaque(false);

        centerGrid.add(createSection("🔥 LIVE PRIORITY QUEUE (CRITICAL)", priorityTable = new JTable()));
        centerGrid.add(createSection("🏚️ ISOLATED VICTIMS (NO INFRASTRUCTURE)", isolatedTable = new JTable()));

        add(centerGrid, BorderLayout.CENTER);

        // Right side: Quick Action Panel
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.ACCENT_COLOR), "QUICK ACTIONS", 0, 0, Theme.FONT_BOLD, Theme.ACCENT_COLOR));

        JButton btnNewRequest = new JButton("CREATE RELIEF REQUEST");
        Theme.styleButton(btnNewRequest);
        btnNewRequest.setBackground(new Color(40, 167, 69)); // Success Green
        btnNewRequest.addActionListener(e -> launchRequestDialog());

        JButton btnReportIsolated = new JButton("REPORT ISOLATION");
        Theme.styleButton(btnReportIsolated);
        btnReportIsolated.setBackground(new Color(255, 159, 10)); // Warning Orange
        btnReportIsolated.addActionListener(e -> launchIsolationReport());

        JPanel btnBox = new JPanel(new GridLayout(2, 1, 10, 10));
        btnBox.setOpaque(false);
        btnBox.add(btnNewRequest);
        btnBox.add(btnReportIsolated);
        
        rightPanel.add(btnBox, BorderLayout.NORTH);
        add(rightPanel, BorderLayout.EAST);

        refreshAll();
    }

    private JPanel createSection(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(" " + title);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.TEXT_MAIN);
        p.add(lbl, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        Theme.styleTable(table, sp);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void launchRequestDialog() {
        JTextField vicId = new JTextField();
        JTextField resId = new JTextField();
        JTextField qty = new JTextField();
        Object[] message = {"Victim ID:", vicId, "Resource ID:", resId, "Quantity:", qty};
        if (JOptionPane.showConfirmDialog(this, message, "Quick Relief Request", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Requests (victim_id, resource_id, quantity_requested, status) VALUES (?, ?, ?, 'Pending')")) {
                        pstmt.setInt(1, Integer.parseInt(vicId.getText()));
                        pstmt.setInt(2, Integer.parseInt(resId.getText()));
                        pstmt.setInt(3, Integer.parseInt(qty.getText()));
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() { try { get(); refreshAll(); } catch (Exception ex) { JOptionPane.showMessageDialog(OperatorDashboard.this, "Error: " + ex.getMessage()); } }
            }.execute();
        }
    }

    private void launchIsolationReport() {
        JOptionPane.showMessageDialog(this, "Feature coming soon: Direct satellite metadata injection for isolated victim tagging.");
    }

    public void refreshAll() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                updateTable(priorityTable, "SELECT * FROM view_priority_requests");
                updateTable(isolatedTable, "SELECT * FROM view_isolated_victims");
                return null;
            }
        }.execute();
    }

    private void updateTable(JTable table, String query) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData();
            Vector<String> colNames = new Vector<>();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) colNames.add(meta.getColumnLabel(i));
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= colCount; i++) row.add(rs.getObject(i));
                data.add(row);
            }
            SwingUtilities.invokeLater(() -> table.setModel(new DefaultTableModel(data, colNames)));
        }
    }
}

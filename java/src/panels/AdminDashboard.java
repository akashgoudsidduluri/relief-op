package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AdminDashboard extends JPanel {
    private JTable trendsTable, pressureTable, logsTable;
    private String userRole;

    public AdminDashboard(String role) {
        this.userRole = role;
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(20, 20));

        // Header: Command Center
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel title = new JLabel("🛡️ DISTRICT STRATEGIC COMMAND CENTER");
        title.setFont(Theme.FONT_H1);
        title.setForeground(Theme.ACCENT_COLOR);
        
        JLabel subtitle = new JLabel("Active SOP: " + userRole.toUpperCase() + " | Strategic Oversight Active");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Center: Analytics Grid
        JPanel centerGrid = new JPanel(new GridLayout(1, 3, 15, 15));
        centerGrid.setOpaque(false);

        centerGrid.add(createSection("📈 DEMAND TRENDS", trendsTable = new JTable()));
        centerGrid.add(createSection("🔥 RESOURCE PRESSURE (%)", pressureTable = new JTable()));
        centerGrid.add(createSection("📜 FORENSIC AUDIT FEED", logsTable = new JTable()));

        add(centerGrid, BorderLayout.CENTER);

        // Bottom: System Controls
        JPanel systemControls = new JPanel(new BorderLayout(10, 10));
        systemControls.setOpaque(false);
        systemControls.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR), "SYSTEM OPERATIONS", 0, 0, Theme.FONT_BOLD, Theme.TEXT_SECONDARY));

        JButton btnBatch = new JButton("⚡ EXECUTE ATOMIC BATCH FULFILLMENT");
        Theme.styleButton(btnBatch);
        btnBatch.setBackground(new Color(13, 110, 253));
        btnBatch.addActionListener(e -> handleBatchProcess());

        JButton btnSimulate = new JButton("🌊 SIMULATE CYCLONE IMPACT (ODISHA)");
        Theme.styleButton(btnSimulate);
        btnSimulate.setBackground(new Color(220, 53, 69)); // Danger Red
        btnSimulate.addActionListener(e -> handleSimulateCyclone());

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setOpaque(false);
        btnBar.add(btnSimulate);
        btnBar.add(btnBatch);

        systemControls.add(btnBar, BorderLayout.EAST);
        add(systemControls, BorderLayout.SOUTH);

        refreshAll();
    }

    private JPanel createSection(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(" " + title);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.ACCENT_COLOR);
        p.add(lbl, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        Theme.styleTable(table, sp);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void handleSimulateCyclone() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Inject 50 Simulated High-Priority Victims along Odisha Coast?\nThis will provide strategic density for the Map Dashboard.", 
            "Crisis Simulation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO Victims (name, location, disaster_type, severity_level, is_isolated, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        
                        // Cluster around Puri/Bhubaneswar (19.8 - 20.3 N, 85.7 - 86.0 E)
                        for (int i = 1; i <= 50; i++) {
                            pstmt.setString(1, "Simulated Case #" + i);
                            pstmt.setString(2, "Odisha Coastal Zone");
                            pstmt.setString(3, (i % 2 == 0) ? "Flood" : "Storm");
                            int sev = (int)(Math.random() * 5) + 1;
                            pstmt.setInt(4, sev);
                            pstmt.setBoolean(5, (sev >= 4)); // Pulse only for critical
                            pstmt.setDouble(6, 19.8 + (Math.random() * 0.5));
                            pstmt.setDouble(7, 85.7 + (Math.random() * 0.3));
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); JOptionPane.showMessageDialog(AdminDashboard.this, "Simulation Engine Active: 50 Tactical Units injected."); refreshAll(); } 
                    catch (Exception ex) { JOptionPane.showMessageDialog(AdminDashboard.this, "Simulation Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void handleBatchProcess() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Execute Atomic Batch Process for top 20 High-Priority requests?\nThis uses database-level best-effort logic.", 
            "Batch Fulfillment", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         CallableStatement cstmt = conn.prepareCall("{call process_all_high_priority()}")) {
                        cstmt.execute();
                    }
                    return null;
                }
                @Override protected void done() { try { get(); JOptionPane.showMessageDialog(AdminDashboard.this, "Batch processing complete."); refreshAll(); } catch (Exception ex) { JOptionPane.showMessageDialog(AdminDashboard.this, "Batch Error: " + ex.getMessage()); } }
            }.execute();
        }
    }

    public void refreshAll() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                updateTable(trendsTable, "SELECT * FROM view_demand_trends");
                updateTable(pressureTable, "SELECT * FROM view_current_stock_pressure");
                updateTable(logsTable, "SELECT * FROM Logs ORDER BY timestamp DESC LIMIT 20");
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

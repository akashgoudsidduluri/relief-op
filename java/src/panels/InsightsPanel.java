package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class InsightsPanel extends JPanel {
    private JTable priorityTable, lowStockTable, utilizationTable, pressureTable, trendsTable, distributionTable;
    private JButton btnBatch;

    public InsightsPanel(String role) {
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(15, 15));

        // Create the header with a title
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Relief-OP Full Strategic System Report");
        title.setFont(Theme.FONT_H1);
        title.setForeground(Theme.TEXT_MAIN);
        header.add(title, BorderLayout.WEST);

        if (role.equals("Admin")) {
            btnBatch = new JButton("⚡ Batch Process High Priority");
            Theme.styleButton(btnBatch);
            btnBatch.setBackground(new Color(13, 110, 253));
            btnBatch.addActionListener(e -> handleBatchProcess());
            header.add(btnBatch, BorderLayout.EAST);
        }
        add(header, BorderLayout.NORTH);

        // Main content: Grid of 6 sections (Full System Overview)
        JPanel center = new JPanel(new GridLayout(2, 3, 15, 15));
        center.setOpaque(false);

        center.add(createSection("🚨 PRIORITY RELIEF QUEUE", priorityTable = new JTable()));
        center.add(createSection("⚠️ LOW INVENTORY ALERTS", lowStockTable = new JTable()));
        center.add(createSection("🏥 SHELTER UTILIZATION", utilizationTable = new JTable()));
        center.add(createSection("🔥 RESOURCE PRESSURE (%)", pressureTable = new JTable()));
        center.add(createSection("📈 DEMAND TRENDS (VOLUME)", trendsTable = new JTable()));
        center.add(createSection("📦 REGIONAL STOCK DISTRIBUTION", distributionTable = new JTable()));
        
        add(new JScrollPane(center), BorderLayout.CENTER);

        refreshAll();
    }

    private JPanel createSection(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Theme.BORDER_COLOR));

        JLabel lbl = new JLabel(" " + title);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.ACCENT_COLOR);
        lbl.setPreferredSize(new Dimension(0, 30));
        p.add(lbl, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        Theme.styleTable(table, sp);
        p.add(sp, BorderLayout.CENTER);
        
        return p;
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
                @Override protected void done() {
                    try { 
                        get();
                        JOptionPane.showMessageDialog(InsightsPanel.this, "Batch processing complete.");
                        refreshAll(); 
                    } catch (Exception ex) { 
                        JOptionPane.showMessageDialog(InsightsPanel.this, "Batch Error: " + ex.getMessage()); 
                    }
                }
            }.execute();
        }
    }

    public void refreshAll() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                updateTable(priorityTable, "SELECT * FROM view_priority_requests");
                updateTable(lowStockTable, "SELECT * FROM view_low_stock_alerts");
                updateTable(utilizationTable, "SELECT * FROM view_shelter_utilization");
                updateTable(pressureTable, "SELECT * FROM view_current_stock_pressure");
                updateTable(trendsTable, "SELECT * FROM view_demand_trends");
                updateTable(distributionTable, "SELECT * FROM view_shelter_inventory_distribution");
                return null;
            }
        }.execute();
    }

    private void updateTable(JTable table, String query) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            ResultSetMetaData meta = rs.getMetaData();
            Vector<String> columnNames = new Vector<>();
            int columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++) columnNames.add(meta.getColumnLabel(i));

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> vector = new Vector<>();
                for (int i = 1; i <= columnCount; i++) vector.add(rs.getObject(i));
                data.add(vector);
            }
            
            SwingUtilities.invokeLater(() -> table.setModel(new DefaultTableModel(data, columnNames)));
        }
    }
}

package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CoordinatorDashboard extends JPanel {
    private JTable distributionTable, imbalanceTable, lowStockTable;
    private String userRole;

    public CoordinatorDashboard(String role) {
        this.userRole = role;
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(20, 20));

        // Header: Logistics Control
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel title = new JLabel("📦 LOGISTICS & SUPPLY CONTROL ROOM");
        title.setFont(Theme.FONT_H1);
        title.setForeground(new Color(0, 122, 255)); 
        
        JLabel subtitle = new JLabel("Active SOP: " + userRole.toUpperCase() + " | Logistics Coordination Active");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Center: Primary Supply Dash
        JPanel centerGrid = new JPanel(new GridLayout(1, 3, 15, 15));
        centerGrid.setOpaque(false);

        centerGrid.add(createSection("📦 REGIONAL DISTRIBUTION", distributionTable = new JTable()));
        centerGrid.add(createSection("⚠️ SHELTER IMBALANCES", imbalanceTable = new JTable()));
        centerGrid.add(createSection("📉 LOW STOCK ALERTS", lowStockTable = new JTable()));

        add(centerGrid, BorderLayout.CENTER);

        // Action Panel
        JPanel bottomAction = new JPanel(new BorderLayout(10, 10));
        bottomAction.setOpaque(false);
        bottomAction.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton btnAddSupply = new JButton("RECORD DIRECT SHELTER SUPPLY 🚛");
        Theme.styleButton(btnAddSupply);
        btnAddSupply.setBackground(new Color(13, 110, 253));
        btnAddSupply.addActionListener(e -> launchSupplyDialog());

        bottomAction.add(btnAddSupply, BorderLayout.EAST);
        add(bottomAction, BorderLayout.SOUTH);

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

    private void launchSupplyDialog() {
        JTextField nameF = new JTextField();
        JTextField resIdF = new JTextField();
        JTextField qtyF = new JTextField();
        JTextField shelterIdF = new JTextField();
        
        Object[] message = {
            "Supplier Name:", nameF,
            "Resource ID:", resIdF,
            "Quantity Supplied:", qtyF,
            "Target Shelter ID:", shelterIdF
        };

        if (JOptionPane.showConfirmDialog(this, message, "Add Supply to Shelter", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO Suppliers (name, resource_id, quantity_supplied, received_at_shelter_id) VALUES (?, ?, ?, ?)")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setInt(2, Integer.parseInt(resIdF.getText()));
                        pstmt.setInt(3, Integer.parseInt(qtyF.getText()));
                        pstmt.setInt(4, Integer.parseInt(shelterIdF.getText()));
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() { try { get(); refreshAll(); } catch (Exception ex) { JOptionPane.showMessageDialog(CoordinatorDashboard.this, "Error: " + ex.getMessage()); } }
            }.execute();
        }
    }

    public void refreshAll() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                updateTable(distributionTable, "SELECT * FROM view_shelter_inventory_distribution");
                updateTable(imbalanceTable, "SELECT * FROM view_shelter_inventory_imbalance");
                updateTable(lowStockTable, "SELECT * FROM view_low_stock_alerts");
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

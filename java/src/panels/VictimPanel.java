package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.net.URI;

public class VictimPanel extends JPanel {
    private DefaultTableModel model;

    public VictimPanel(String role) {
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(15, 15));
        
        String[] columns = {"ID", "Name", "Location", "Disaster", "Severity (1-5)", "Lat", "Long", "Contact", "Isolated"};
        model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        Theme.styleTable(table, scrollPane);
        
        add(scrollPane, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        Theme.styleControlsPanel(controls);
        
        JButton btnAdd = new JButton("Register Victim");
        JButton btnUpdate = new JButton("Update Details");
        JButton btnDelete = new JButton("Delete");
        JButton btnMap = new JButton("View Map 📍");
        
        Theme.styleButton(btnAdd);
        Theme.styleButton(btnUpdate);
        Theme.styleButton(btnDelete);
        Theme.styleButton(btnMap);
        btnDelete.setBackground(new Color(220, 53, 69));
        btnMap.setBackground(new Color(40, 167, 69));

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate(table));
        btnDelete.addActionListener(e -> handleDelete(table));
        btnMap.addActionListener(e -> handleViewMap(table));

        // Action-Level Security: Only Admin can delete victims
        controls.add(btnAdd);
        controls.add(btnUpdate);
        if (role.equals("Admin")) {
            controls.add(btnDelete);
        }
        controls.add(btnMap);
        add(controls, BorderLayout.SOUTH);

        loadData();
    }

    private void handleAdd() {
        JTextField nameF = new JTextField();
        JTextField locF = new JTextField();
        JTextField disF = new JTextField();
        JTextField sevF = new JTextField("3");
        JTextField latF = new JTextField();
        JTextField lonF = new JTextField();
        JTextField conF = new JTextField();
        JCheckBox isoC = new JCheckBox("Isolated by Disaster");

        Object[] message = {
            "Victim Name:", nameF,
            "Location:", locF,
            "Disaster Type:", disF,
            "Severity Level (1-5):", sevF,
            "Latitude:", latF,
            "Longitude:", lonF,
            "Contact Number:", conF,
            "", isoC
        };

        if (JOptionPane.showConfirmDialog(this, message, "Register Victim", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO Victims (name, location, disaster_type, severity_level, latitude, longitude, contact_number, is_isolated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setString(2, locF.getText());
                        pstmt.setString(3, disF.getText());
                        pstmt.setInt(4, Integer.parseInt(sevF.getText()));
                        pstmt.setObject(5, latF.getText().isEmpty() ? null : Double.parseDouble(latF.getText()));
                        pstmt.setObject(6, lonF.getText().isEmpty() ? null : Double.parseDouble(lonF.getText()));
                        pstmt.setString(7, conF.getText());
                        pstmt.setBoolean(8, isoC.isSelected());
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { 
                        JOptionPane.showMessageDialog(VictimPanel.this, "Error: " + ex.getMessage()); 
                    }
                }
            }.execute();
        }
    }

    private void handleUpdate(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) model.getValueAt(row, 0);
        JTextField nameF = new JTextField(model.getValueAt(row, 1).toString());
        JTextField locF = new JTextField(model.getValueAt(row, 2).toString());
        JTextField disF = new JTextField(model.getValueAt(row, 3).toString());
        JTextField sevF = new JTextField(model.getValueAt(row, 4).toString());
        JTextField latF = new JTextField(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");
        JTextField lonF = new JTextField(model.getValueAt(row, 6) != null ? model.getValueAt(row, 6).toString() : "");
        JTextField conF = new JTextField(model.getValueAt(row, 7) != null ? model.getValueAt(row, 7).toString() : "");
        JCheckBox isoC = new JCheckBox("Isolated by Disaster", (Boolean) model.getValueAt(row, 8));

        Object[] message = {
            "Victim Name:", nameF,
            "Location:", locF,
            "Disaster Type:", disF,
            "Severity Level (1-5):", sevF,
            "Latitude:", latF,
            "Longitude:", lonF,
            "Contact Number:", conF,
            "", isoC
        };

        if (JOptionPane.showConfirmDialog(this, message, "Update Victim Details", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "UPDATE Victims SET name=?, location=?, disaster_type=?, severity_level=?, latitude=?, longitude=?, contact_number=?, is_isolated=? WHERE victim_id=?")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setString(2, locF.getText());
                        pstmt.setString(3, disF.getText());
                        pstmt.setInt(4, Integer.parseInt(sevF.getText()));
                        pstmt.setObject(5, latF.getText().isEmpty() ? null : Double.parseDouble(latF.getText()));
                        pstmt.setObject(6, lonF.getText().isEmpty() ? null : Double.parseDouble(lonF.getText()));
                        pstmt.setString(7, conF.getText());
                        pstmt.setBoolean(8, isoC.isSelected());
                        pstmt.setInt(9, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { 
                        JOptionPane.showMessageDialog(VictimPanel.this, "Error: " + ex.getMessage()); 
                    }
                }
            }.execute();
        }
    }

    private void handleDelete(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Victims WHERE victim_id = ?")) {
                        pstmt.setInt(1, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { 
                        JOptionPane.showMessageDialog(VictimPanel.this, "Error: " + ex.getMessage()); 
                    }
                }
            }.execute();
        }
    }

    private void handleViewMap(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        Object lat = model.getValueAt(row, 5);
        Object lon = model.getValueAt(row, 6);

        if (lat == null || lon == null || lat.toString().isEmpty() || lon.toString().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No coordinates set for this victim");
            return;
        }

        try {
            String url = String.format("https://www.openstreetmap.org/?mlat=%s&mlon=%s#map=15/%s/%s", lat, lon, lat, lon);
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open map: " + ex.getMessage());
        }
    }

    private void loadData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                try (Connection conn = DBConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM Victims")) {
                    while (rs.next()) {
                        data.add(new Object[]{
                            rs.getInt("victim_id"),
                            rs.getString("name"),
                            rs.getString("location"),
                            rs.getString("disaster_type"),
                            rs.getInt("severity_level"),
                            rs.getObject("latitude"),
                            rs.getObject("longitude"),
                            rs.getString("contact_number"),
                            rs.getBoolean("is_isolated")
                        });
                    }
                }
                return data;
            }
            @Override protected void done() {
                try {
                    model.setRowCount(0);
                    for (Object[] row : get()) model.addRow(row);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(VictimPanel.this, "Database Error: " + e.getMessage());
                }
            }
        }.execute();
    }
}
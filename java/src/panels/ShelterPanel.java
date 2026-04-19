package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.net.URI;

public class ShelterPanel extends JPanel {
    private DefaultTableModel model;

    public ShelterPanel(String role) {
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(15, 15));
        
        String[] columns = {"ID", "Name", "Location", "Capacity", "Occupancy", "Lat", "Long"};
        model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        Theme.styleTable(table, scrollPane);
        
        add(scrollPane, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        Theme.styleControlsPanel(controls);
        
        JButton btnAdd = new JButton("Add Shelter");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnMap = new JButton("View Map 📍");
        JButton btnRefresh = new JButton("Refresh");
        
        Theme.styleButton(btnAdd);
        Theme.styleButton(btnEdit);
        Theme.styleButton(btnDelete);
        Theme.styleButton(btnMap);
        Theme.styleButton(btnRefresh);
        btnDelete.setBackground(new Color(220, 53, 69));
        btnMap.setBackground(new Color(40, 167, 69));

        btnAdd.addActionListener(e -> handleAdd());
        btnEdit.addActionListener(e -> handleUpdate(table));
        btnDelete.addActionListener(e -> handleDelete(table));
        btnMap.addActionListener(e -> handleViewMap(table));
        btnRefresh.addActionListener(e -> loadData());

        // Action-Level Security: Only Admin can delete shelters
        controls.add(btnAdd);
        controls.add(btnEdit);
        if (role.equals("Admin")) {
            controls.add(btnDelete);
        }
        controls.add(btnMap);
        controls.add(btnRefresh);
        add(controls, BorderLayout.SOUTH);

        loadData();
    }

    private void handleAdd() {
        JTextField nameF = new JTextField();
        JTextField locF = new JTextField();
        JTextField capF = new JTextField();
        JTextField occF = new JTextField("0");
        JTextField latF = new JTextField();
        JTextField lonF = new JTextField();

        Object[] message = {
            "Shelter Name:", nameF,
            "Location:", locF,
            "Capacity:", capF,
            "Occupancy:", occF,
            "Latitude:", latF,
            "Longitude:", lonF
        };

        if (JOptionPane.showConfirmDialog(this, message, "Add Shelter", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO Shelters (name, location, capacity, occupancy, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setString(2, locF.getText());
                        pstmt.setInt(3, Integer.parseInt(capF.getText()));
                        pstmt.setInt(4, Integer.parseInt(occF.getText()));
                        pstmt.setObject(5, latF.getText().isEmpty() ? null : Double.parseDouble(latF.getText()));
                        pstmt.setObject(6, lonF.getText().isEmpty() ? null : Double.parseDouble(lonF.getText()));
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(ShelterPanel.this, "Error: " + ex.getMessage()); }
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
        JTextField capF = new JTextField(model.getValueAt(row, 3).toString());
        JTextField occF = new JTextField(model.getValueAt(row, 4).toString());
        JTextField latF = new JTextField(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");
        JTextField lonF = new JTextField(model.getValueAt(row, 6) != null ? model.getValueAt(row, 6).toString() : "");

        Object[] message = {
            "Shelter Name:", nameF,
            "Location:", locF,
            "Capacity:", capF,
            "Occupancy:", occF,
            "Latitude:", latF,
            "Longitude:", lonF
        };

        if (JOptionPane.showConfirmDialog(this, message, "Update Shelter", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "UPDATE Shelters SET name=?, location=?, capacity=?, occupancy=?, latitude=?, longitude=? WHERE shelter_id=?")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setString(2, locF.getText());
                        pstmt.setInt(3, Integer.parseInt(capF.getText()));
                        pstmt.setInt(4, Integer.parseInt(occF.getText()));
                        pstmt.setObject(5, latF.getText().isEmpty() ? null : Double.parseDouble(latF.getText()));
                        pstmt.setObject(6, lonF.getText().isEmpty() ? null : Double.parseDouble(lonF.getText()));
                        pstmt.setInt(7, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(ShelterPanel.this, "Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void handleDelete(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete shelter?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Shelters WHERE shelter_id=?")) {
                        pstmt.setInt(1, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(ShelterPanel.this, "Error: " + ex.getMessage()); }
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
            JOptionPane.showMessageDialog(this, "No coordinates set for this shelter");
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
                     ResultSet rs = stmt.executeQuery("SELECT * FROM Shelters")) {
                    while (rs.next()) {
                        data.add(new Object[]{
                            rs.getInt("shelter_id"),
                            rs.getString("name"),
                            rs.getString("location"),
                            rs.getInt("capacity"),
                            rs.getInt("occupancy"),
                            rs.getObject("latitude"),
                            rs.getObject("longitude")
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
                    JOptionPane.showMessageDialog(ShelterPanel.this, "Database Error: " + e.getMessage());
                }
            }
        }.execute();
    }
}
package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class ResourcePanel extends JPanel {
    private DefaultTableModel model;

    public ResourcePanel(String role) {
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(15, 15));

        String[] columns = {"ID", "Resource Name", "Quantity", "Unit"};
        model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        Theme.styleTable(table, scrollPane);

        add(scrollPane, BorderLayout.CENTER);

        // Action-Level Security: Only Admin and Coordinator can manage resources
        if (role.equals("Admin") || role.equals("Coordinator")) {
            JPanel controls = new JPanel();
            Theme.styleControlsPanel(controls);

            JButton btnAdd = new JButton("Add Resource");
            JButton btnUpdate = new JButton("Update Details");
            JButton btnDelete = new JButton("Remove");

            Theme.styleButton(btnAdd);
            Theme.styleButton(btnUpdate);
            Theme.styleButton(btnDelete);
            btnDelete.setBackground(new Color(220, 53, 69));

            btnAdd.addActionListener(e -> handleAdd());
            btnUpdate.addActionListener(e -> handleUpdate(table));
            btnDelete.addActionListener(e -> handleDelete(table));

            controls.add(btnAdd);
            controls.add(btnUpdate);
            controls.add(btnDelete);
            add(controls, BorderLayout.SOUTH);
        }

        loadData();
    }

    private void handleAdd() {
        JTextField nameF = new JTextField();
        JTextField qtyF = new JTextField("0");
        JTextField unitF = new JTextField();

        Object[] message = {
            "Resource Name:", nameF,
            "Quantity:", qtyF,
            "Unit (e.g. Boxes, Litres):", unitF
        };

        if (JOptionPane.showConfirmDialog(this, message, "Add Resource", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO Resources (resource_name, quantity, unit) VALUES (?, ?, ?)")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setInt(2, Integer.parseInt(qtyF.getText()));
                        pstmt.setString(3, unitF.getText());
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(ResourcePanel.this, "Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void handleUpdate(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);

        JTextField nameF = new JTextField(model.getValueAt(row, 1).toString());
        JTextField qtyF = new JTextField(model.getValueAt(row, 2).toString());
        JTextField unitF = new JTextField(model.getValueAt(row, 3).toString());

        Object[] message = {
            "Resource Name:", nameF,
            "Quantity:", qtyF,
            "Unit:", unitF
        };

        if (JOptionPane.showConfirmDialog(this, message, "Update Resource", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "UPDATE Resources SET resource_name=?, quantity=?, unit=? WHERE resource_id=?")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setInt(2, Integer.parseInt(qtyF.getText()));
                        pstmt.setString(3, unitF.getText());
                        pstmt.setInt(4, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(ResourcePanel.this, "Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void handleDelete(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete resource?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Resources WHERE resource_id=?")) {
                        pstmt.setInt(1, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(ResourcePanel.this, "Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void loadData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                try (Connection conn = DBConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM Resources")) {
                    while (rs.next()) {
                        data.add(new Object[]{
                            rs.getInt("resource_id"),
                            rs.getString("resource_name"),
                            rs.getInt("quantity"),
                            rs.getString("unit")
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
                    JOptionPane.showMessageDialog(ResourcePanel.this, "Database Error: " + e.getMessage());
                }
            }
        }.execute();
    }
}

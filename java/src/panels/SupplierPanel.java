package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class SupplierPanel extends JPanel {
    private DefaultTableModel model;

    public SupplierPanel(String role) {
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(15, 15));

        String[] columns = {"ID", "Supplier Name", "Resource ID", "Qty Supplied", "Shelter ID", "Supply Date"};
        model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        Theme.styleTable(table, scrollPane);

        add(scrollPane, BorderLayout.CENTER);

        // Action-Level Security: Only Admin can manage suppliers (Tab is already Admin-restricted)
        if (role.equals("Admin")) {
            JPanel controls = new JPanel();
            Theme.styleControlsPanel(controls);

            JButton btnAdd = new JButton("Add Supplier");
            JButton btnEdit = new JButton("Edit");
            JButton btnDelete = new JButton("Remove");

            Theme.styleButton(btnAdd);
            Theme.styleButton(btnEdit);
            Theme.styleButton(btnDelete);
            btnDelete.setBackground(new Color(220, 53, 69));

            btnAdd.addActionListener(e -> handleAdd());
            btnEdit.addActionListener(e -> handleUpdate(table));
            btnDelete.addActionListener(e -> handleDelete(table));

            controls.add(btnAdd);
            controls.add(btnEdit);
            controls.add(btnDelete);
            add(controls, BorderLayout.SOUTH);
        }

        loadData();
    }

    private void handleAdd() {
        JTextField nameF = new JTextField();
        JTextField resIdF = new JTextField();
        JTextField qtyF = new JTextField();
        JTextField shelterF = new JTextField();

        Object[] message = {
            "Supplier Name:", nameF,
            "Resource ID:", resIdF,
            "Quantity Supplied:", qtyF,
            "Received at Shelter ID:", shelterF
        };

        if (JOptionPane.showConfirmDialog(this, message, "Add Supplier", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO Suppliers (name, resource_id, quantity_supplied, received_at_shelter_id) VALUES (?, ?, ?, ?)")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setObject(2, resIdF.getText().isEmpty() ? null : Integer.parseInt(resIdF.getText()));
                        pstmt.setInt(3, Integer.parseInt(qtyF.getText()));
                        pstmt.setObject(4, shelterF.getText().isEmpty() ? null : Integer.parseInt(shelterF.getText()));
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(SupplierPanel.this, "Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void handleUpdate(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);

        JTextField nameF = new JTextField(model.getValueAt(row, 1).toString());
        JTextField resIdF = new JTextField(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
        JTextField qtyF = new JTextField(model.getValueAt(row, 3).toString());
        JTextField shelterF = new JTextField(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");

        Object[] message = {
            "Supplier Name:", nameF,
            "Resource ID:", resIdF,
            "Quantity Supplied:", qtyF,
            "Received at Shelter ID:", shelterF
        };

        if (JOptionPane.showConfirmDialog(this, message, "Update Supplier", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "UPDATE Suppliers SET name=?, resource_id=?, quantity_supplied=?, received_at_shelter_id=? WHERE supplier_id=?")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setObject(2, resIdF.getText().isEmpty() ? null : Integer.parseInt(resIdF.getText()));
                        pstmt.setInt(3, Integer.parseInt(qtyF.getText()));
                        pstmt.setObject(4, shelterF.getText().isEmpty() ? null : Integer.parseInt(shelterF.getText()));
                        pstmt.setInt(5, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(SupplierPanel.this, "Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void handleDelete(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Remove supplier?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Suppliers WHERE supplier_id=?")) {
                        pstmt.setInt(1, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(SupplierPanel.this, "Error: " + ex.getMessage()); }
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
                     ResultSet rs = stmt.executeQuery("SELECT * FROM Suppliers")) {
                    while (rs.next()) {
                        data.add(new Object[]{
                            rs.getInt("supplier_id"),
                            rs.getString("name"),
                            rs.getObject("resource_id"),
                            rs.getInt("quantity_supplied"),
                            rs.getObject("received_at_shelter_id"),
                            rs.getTimestamp("supply_date")
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
                    JOptionPane.showMessageDialog(SupplierPanel.this, "Database Error: " + e.getMessage());
                }
            }
        }.execute();
    }
}

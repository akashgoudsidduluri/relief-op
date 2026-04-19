package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class HelperPanel extends JPanel {
    private DefaultTableModel model;

    public HelperPanel(String role) {
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(15, 15));

        String[] columns = {"ID", "Name", "Assigned Shelter ID"};
        model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        Theme.styleTable(table, scrollPane);

        add(scrollPane, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        Theme.styleControlsPanel(controls);

        JButton btnAdd = new JButton("Add Helper");
        JButton btnUpdate = new JButton("Update Status");
        JButton btnDelete = new JButton("Remove");

        Theme.styleButton(btnAdd);
        Theme.styleButton(btnUpdate);
        Theme.styleButton(btnDelete);
        btnDelete.setBackground(new Color(220, 53, 69));

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate(table));
        btnDelete.addActionListener(e -> handleDelete(table));

        // Action-Level Security
        controls.add(btnAdd);
        controls.add(btnUpdate);
        if (role.equals("Admin")) {
            controls.add(btnDelete);
        }
        add(controls, BorderLayout.SOUTH);

        loadData();
    }

    private void handleAdd() {
        JTextField nameF = new JTextField();
        JTextField shelterF = new JTextField();

        Object[] message = {
            "Helper Name:", nameF,
            "Shelter ID:", shelterF
        };

        if (JOptionPane.showConfirmDialog(this, message, "Add Helper", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO Personnel (name, role_type, assigned_shelter_id) VALUES (?, 'Helper', ?)")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setObject(2, shelterF.getText().isEmpty() ? null : Integer.parseInt(shelterF.getText()));
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(HelperPanel.this, "Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void handleUpdate(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);

        JTextField nameF = new JTextField(model.getValueAt(row, 1).toString());
        JTextField shelterF = new JTextField(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");

        Object[] message = {
            "Helper Name:", nameF,
            "Shelter ID:", shelterF
        };

        if (JOptionPane.showConfirmDialog(this, message, "Update Helper", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "UPDATE Personnel SET name=?, assigned_shelter_id=? WHERE person_id=? AND role_type='Helper'")) {
                        pstmt.setString(1, nameF.getText());
                        pstmt.setObject(2, shelterF.getText().isEmpty() ? null : Integer.parseInt(shelterF.getText()));
                        pstmt.setInt(3, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(HelperPanel.this, "Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void handleDelete(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Remove helper?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Personnel WHERE person_id=? AND role_type='Helper'")) {
                        pstmt.setInt(1, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(HelperPanel.this, "Error: " + ex.getMessage()); }
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
                     ResultSet rs = stmt.executeQuery("SELECT * FROM Personnel WHERE role_type='Helper'")) {
                    while (rs.next()) {
                        data.add(new Object[]{
                            rs.getInt("person_id"),
                            rs.getString("name"),
                            rs.getObject("assigned_shelter_id")
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
                    JOptionPane.showMessageDialog(HelperPanel.this, "Database Error: " + e.getMessage());
                }
            }
        }.execute();
    }
}

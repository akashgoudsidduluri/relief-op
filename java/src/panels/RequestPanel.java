package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class RequestPanel extends JPanel {
    private DefaultTableModel model;

    public RequestPanel(String role) {
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout(15, 15));

        String[] columns = {"ID", "Date", "Status", "Qty Req", "Rem Qty", "Notes", "Victim ID", "Res ID"};
        model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        Theme.styleTable(table, scrollPane);

        add(scrollPane, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        Theme.styleControlsPanel(controls);

        JButton btnNew = new JButton("New Request");
        JButton btnHandle = new JButton("Process Request");
        JButton btnDelete = new JButton("Delete");

        Theme.styleButton(btnNew);
        Theme.styleButton(btnHandle);
        Theme.styleButton(btnDelete);
        btnDelete.setBackground(new Color(220, 53, 69));

        btnNew.addActionListener(e -> handleAdd());
        btnHandle.addActionListener(e -> handleProcess(table));
        btnDelete.addActionListener(e -> handleDelete(table));

        // Action-Level Security
        controls.add(btnNew);
        if (role.equals("Admin") || role.equals("Coordinator")) {
            controls.add(btnHandle);
        }
        if (role.equals("Admin")) {
            controls.add(btnDelete);
        }
        add(controls, BorderLayout.SOUTH);

        loadData();
    }

    private void handleAdd() {
        JTextField qtyF = new JTextField();
        JTextField notesF = new JTextField();
        JTextField vicF = new JTextField();
        JTextField resF = new JTextField();

        Object[] message = {
            "Quantity Requested:", qtyF,
            "Notes:", notesF,
            "Victim ID:", vicF,
            "Resource ID:", resF
        };

        if (JOptionPane.showConfirmDialog(this, message, "New Request", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO Requests (quantity_requested, notes, victim_id, resource_id) VALUES (?, ?, ?, ?)")) {
                        pstmt.setInt(1, Integer.parseInt(qtyF.getText()));
                        pstmt.setString(2, notesF.getText());
                        pstmt.setObject(3, vicF.getText().isEmpty() ? null : Integer.parseInt(vicF.getText()));
                        pstmt.setObject(4, resF.getText().isEmpty() ? null : Integer.parseInt(resF.getText()));
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(RequestPanel.this, "Error: " + ex.getMessage()); }
                }
            }.execute();
        }
    }

    private void handleProcess(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int requestId = (int) model.getValueAt(row, 0);
        String currentStatus = model.getValueAt(row, 2).toString();

        if (currentStatus.equalsIgnoreCase("Fulfilled") || currentStatus.equalsIgnoreCase("Rejected")) {
            JOptionPane.showMessageDialog(this, "This request is already finalized.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Execute Database Process for Request #" + requestId + "?\nThis will automatically check stock and fulfill/partial/reject.", 
            "Database Decision", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         CallableStatement cstmt = conn.prepareCall("{call process_request(?)}")) {
                        
                        cstmt.setInt(1, requestId);
                        cstmt.execute();
                        return "Database decision executed successfully.";
                    }
                }
                @Override protected void done() {
                    try { 
                        String msg = get(); 
                        JOptionPane.showMessageDialog(RequestPanel.this, msg);
                        loadData(); 
                    } catch (Exception ex) { 
                        // Enhanced Error Reporting for SIGNAL messages
                        String errMsg = ex.getMessage();
                        if (ex.getCause() != null) errMsg = ex.getCause().getMessage();
                        JOptionPane.showMessageDialog(RequestPanel.this, "Database Policy Violation:\n" + errMsg); 
                    }
                }
            }.execute();
        }
    }

    private void handleDelete(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete request?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Requests WHERE request_id=?")) {
                        pstmt.setInt(1, id);
                        pstmt.executeUpdate();
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); loadData(); } catch (Exception ex) { JOptionPane.showMessageDialog(RequestPanel.this, "Error: " + ex.getMessage()); }
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
                     ResultSet rs = stmt.executeQuery("SELECT * FROM Requests")) {
                    while (rs.next()) {
                        data.add(new Object[]{
                            rs.getInt("request_id"),
                            rs.getTimestamp("request_time"),
                            rs.getString("status"),
                            rs.getInt("quantity_requested"),
                            rs.getInt("remaining_quantity"),
                            rs.getString("notes"),
                            rs.getObject("victim_id"),
                            rs.getObject("resource_id")
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
                    JOptionPane.showMessageDialog(RequestPanel.this, "Database Error: " + e.getMessage());
                }
            }
        }.execute();
    }
}

package panels;

import javax.swing.*;
import java.awt.*;

public class MapsPanel extends JPanel {
    
    public MapsPanel(String role) {
        Theme.applyBasePanelStyle(this);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new GridLayout(2, 1, 10, 10));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel title = new JLabel("Relief-OP Map Dashboard", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subTitle = new JLabel("Visualize all victims and shelters in real-time.", JLabel.CENTER);
        subTitle.setForeground(new Color(180, 180, 180));

        header.add(title);
        header.add(subTitle);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        
        JButton btnOpen = new JButton("Launch Interactive Control Center 📍");
        btnOpen.setFont(new Font("Segoe UI", Font.BOLD, 18));
        Theme.styleButton(btnOpen);
        btnOpen.setBackground(new Color(13, 110, 253));
        btnOpen.setPreferredSize(new Dimension(400, 60));

        btnOpen.addActionListener(e -> {
            btnOpen.setEnabled(false);
            btnOpen.setText("⏳ Generating map...");
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    MapController.openFullMap();
                    return null;
                }
                @Override
                protected void done() {
                    btnOpen.setEnabled(true);
                    btnOpen.setText("Launch Interactive Control Center 📍");
                    try { get(); } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MapsPanel.this,
                            "Map error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        center.add(btnOpen);
        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.add(new JLabel("Note: Requires internet connection to load map tiles."));
        add(footer, BorderLayout.SOUTH);
    }
}

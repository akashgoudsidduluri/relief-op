package panels;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Theme {
    // Premium Dark Mode palette
    public static final Color PRIMARY_BG = new Color(30, 30, 32);
    public static final Color SECONDARY_BG = new Color(40, 40, 43);
    public static final Color ACCENT_COLOR = new Color(41, 121, 255);
    public static final Color DANGER_COLOR = new Color(220, 53, 69); // Added explicitly
    public static final Color TEXT_MAIN = new Color(245, 245, 245);
    public static final Color TEXT_SECONDARY = new Color(180, 180, 182); // High-contrast subtitle
    public static final Color TEXT_MUTED = new Color(150, 150, 150);
    public static final Color BORDER_COLOR = new Color(60, 60, 65);
    
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_H1 = new Font("Segoe UI", Font.BOLD, 22);

    public static void styleTabbedPane(JTabbedPane tabs) {
        tabs.setBackground(SECONDARY_BG);
        tabs.setForeground(TEXT_MAIN);
        tabs.setOpaque(true);
        tabs.setFont(FONT_BOLD);
        tabs.setBorder(BorderFactory.createEmptyBorder());
    }

    public static void applyBasePanelStyle(JPanel panel) {
        panel.setBackground(PRIMARY_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    public static void styleTable(JTable table, JScrollPane scrollPane) {
        table.setBackground(SECONDARY_BG);
        table.setForeground(TEXT_MAIN);
        table.setGridColor(BORDER_COLOR);
        table.setFont(FONT_REGULAR);
        table.setRowHeight(35);
        table.setSelectionBackground(ACCENT_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setBorder(null);

        // Center Align Cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(22, 22, 24));
        header.setForeground(new Color(200, 200, 200));
        header.setFont(FONT_BOLD);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Center Align Headers
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        scrollPane.getViewport().setBackground(PRIMARY_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setBackground(PRIMARY_BG);
    }

    public static void styleButton(JButton btn) {
        applyButtonColors(btn, ACCENT_COLOR);
    }
    
    public static void styleDangerButton(JButton btn) {
        applyButtonColors(btn, DANGER_COLOR);
    }

    private static void applyButtonColors(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add sleek hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
    }
    
    public static void styleControlsPanel(JPanel controls) {
        controls.setBackground(PRIMARY_BG);
        controls.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 15));
    }
    
    public static void styleTextField(JTextField tf) {
        tf.setBackground(SECONDARY_BG);
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(TEXT_MAIN);
        tf.setFont(FONT_REGULAR);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    public static void styleLabel(JLabel label) {
        label.setForeground(TEXT_MAIN);
        label.setFont(FONT_BOLD);
    }
}

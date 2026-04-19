import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import panels.*;

public class MainFrame extends JFrame {
    private String userRole;

    public MainFrame(String role) {
        this.userRole = role;
        setTitle("Relief-OP [" + role + "] - Decision System");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.PRIMARY_BG);

        // Styling the global UI manager for JTabbedPane to match dark theme
        UIManager.put("TabbedPane.background", new ColorUIResource(Theme.SECONDARY_BG));
        UIManager.put("TabbedPane.foreground", new ColorUIResource(Theme.TEXT_MAIN));
        UIManager.put("TabbedPane.selected", new ColorUIResource(Theme.ACCENT_COLOR));
        UIManager.put("TabbedPane.contentAreaColor", new ColorUIResource(Theme.PRIMARY_BG));
        UIManager.put("TabbedPane.font", Theme.FONT_BOLD);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        Theme.styleTabbedPane(tabbedPane);

        // --- ROLE-BASED MISSION ROUTER [V10.2] ---
        if (userRole.equals("Operator")) {
            // Operator's Mission: Field Triage & Emergency Response
            tabbedPane.addTab("🏠 Home (Emergency Desk)", new OperatorDashboard(userRole));
            tabbedPane.addTab("👥 Victims", new VictimPanel(userRole));
            tabbedPane.addTab("📝 Requests", new RequestPanel(userRole));
            tabbedPane.addTab("📍 Map Dashboard", new MapsPanel(userRole));

        } else if (userRole.equals("Coordinator")) {
            // Coordinator's Mission: Regional Logistics & Shelter Health
            tabbedPane.addTab("🏠 Home (Supply Room)", new CoordinatorDashboard(userRole));
            tabbedPane.addTab("🏠 Shelters", new ShelterPanel(userRole));
            tabbedPane.addTab("📦 Resources", new ResourcePanel(userRole));
            tabbedPane.addTab("🤝 Volunteers", new VolunteerPanel(userRole));
            tabbedPane.addTab("🤝 Helpers", new HelperPanel(userRole));
            tabbedPane.addTab("📍 Map Dashboard", new MapsPanel(userRole));

        } else if (userRole.equals("Admin")) {
            // Admin's Mission: Strategic Oversight & System Forensics
            tabbedPane.addTab("🏠 Home (Strategic Command)", new AdminDashboard(userRole));
            tabbedPane.addTab("📈 System-Wide Reports", new InsightsPanel(userRole));
            tabbedPane.addTab("📍 Map Dashboard", new MapsPanel(userRole));
            tabbedPane.addTab("🚛 Suppliers / Donors", new SupplierPanel(userRole));
        }

        add(tabbedPane, BorderLayout.CENTER);

        // Sidebar / Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.SECONDARY_BG);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        JLabel lblStatus = new JLabel("  Relief-OP Active SOP Mode: " + userRole.toUpperCase() + " COORDINATION");
        lblStatus.setForeground(Theme.ACCENT_COLOR);
        lblStatus.setFont(Theme.FONT_BOLD);
        statusBar.add(lblStatus, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame("Admin"));
    }
}
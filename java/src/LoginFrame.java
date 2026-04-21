import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.plaf.ColorUIResource;
import panels.Theme;

public class LoginFrame extends JFrame {
    private JTextField loginUser, signupUser, email;
    private JPasswordField loginPass, signupPass;
    private JTabbedPane tabbedPane;
    private String selectedRole = null;

    public LoginFrame() {
        setTitle("Relief-OP Secure Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        showRoleSelection();
    }

    private void showRoleSelection() {
        // Theme configuration
        UIManager.put("TabbedPane.background", new ColorUIResource(Theme.SECONDARY_BG));
        UIManager.put("TabbedPane.foreground", new ColorUIResource(Theme.TEXT_MAIN));
        UIManager.put("TabbedPane.selected", new ColorUIResource(Theme.ACCENT_COLOR));
        UIManager.put("TabbedPane.contentAreaColor", new ColorUIResource(Theme.PRIMARY_BG));
        UIManager.put("TabbedPane.font", Theme.FONT_BOLD);

        getContentPane().removeAll();
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.PRIMARY_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        headerPanel.setBackground(Theme.PRIMARY_BG);
        
        JLabel logoLabel = new JLabel("🚨 Relief-OP");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logoLabel.setForeground(Theme.ACCENT_COLOR);
        logoLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel subtitleLabel = new JLabel("Select Your Role to Continue");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        headerPanel.add(logoLabel);
        headerPanel.add(subtitleLabel);

        // Role selection buttons
        JPanel rolePanel = new JPanel(new GridLayout(1, 3, 20, 0));
        rolePanel.setBackground(Theme.PRIMARY_BG);
        rolePanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JButton adminBtn = createRoleButton("👤 ADMIN", "admin", new Color(220, 53, 69));
        JButton operatorBtn = createRoleButton("🚁 OPERATOR", "operator", new Color(13, 110, 253));
        JButton coordBtn = createRoleButton("🏢 COORDINATOR", "coordinator", new Color(40, 167, 69));

        adminBtn.addActionListener(e -> proceedToLogin("Admin"));
        operatorBtn.addActionListener(e -> proceedToLogin("Operator"));
        coordBtn.addActionListener(e -> proceedToLogin("Coordinator"));

        rolePanel.add(adminBtn);
        rolePanel.add(operatorBtn);
        rolePanel.add(coordBtn);

        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(rolePanel);

        add(mainPanel);
        setSize(900, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createRoleButton(String label, String role, Color color) {
        JButton btn = new JButton(label);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(color.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(color);
            }
        });
        return btn;
    }

    private void proceedToLogin(String role) {
        this.selectedRole = role;
        showLoginScreen();
    }

    private void showLoginScreen() {
        getContentPane().removeAll();
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.PRIMARY_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.SECONDARY_BG);
        tabbedPane.setForeground(Theme.TEXT_MAIN);

        // LOGIN PANEL
        JPanel loginPanel = new JPanel(new BorderLayout(15, 15));
        loginPanel.setBackground(Theme.PRIMARY_BG);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JPanel loginForm = new JPanel(new GridLayout(3, 2, 15, 15));
        loginForm.setBackground(Theme.PRIMARY_BG);

        loginUser = new JTextField(15);
        loginPass = new JPasswordField(15);
        Theme.styleTextField(loginUser);
        Theme.styleTextField(loginPass);

        JButton loginBtn = new JButton("🔐 Login");
        Theme.styleButton(loginBtn);

        JLabel lblUser = new JLabel("Username:");
        JLabel lblPass = new JLabel("Password:");
        Theme.styleLabel(lblUser);
        Theme.styleLabel(lblPass);

        loginForm.add(lblUser);
        loginForm.add(loginUser);
        loginForm.add(lblPass);
        loginForm.add(loginPass);
        loginForm.add(new JLabel("")); 
        loginForm.add(loginBtn);

        // Demo credentials panel
        JPanel demoPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        demoPanel.setBackground(new Color(50, 50, 55));
        demoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        JLabel demoTitle = new JLabel("📋 Demo Credentials:");
        demoTitle.setForeground(Theme.ACCENT_COLOR);
        demoTitle.setFont(Theme.FONT_BOLD);
        
        JLabel adminCred = new JLabel("Admin:      admin / admin123");
        adminCred.setForeground(Theme.TEXT_SECONDARY);
        adminCred.setFont(new Font("Courier New", Font.PLAIN, 12));
        
        JLabel opCred = new JLabel("Operator:  operator / op123");
        opCred.setForeground(Theme.TEXT_SECONDARY);
        opCred.setFont(new Font("Courier New", Font.PLAIN, 12));
        
        JLabel coordCred = new JLabel("Coordinator:  coord / coord123");
        coordCred.setForeground(Theme.TEXT_SECONDARY);
        coordCred.setFont(new Font("Courier New", Font.PLAIN, 12));
        
        demoPanel.add(demoTitle);
        demoPanel.add(adminCred);
        demoPanel.add(opCred);
        demoPanel.add(coordCred);

        loginPanel.add(loginForm, BorderLayout.CENTER);
        loginPanel.add(demoPanel, BorderLayout.SOUTH);

        // Only show signup tab if NOT admin
        if (!selectedRole.equals("Admin")) {
            // SIGNUP PANEL
            JPanel signupPanel = new JPanel(new BorderLayout(15, 15));
            signupPanel.setBackground(Theme.PRIMARY_BG);
            signupPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            JPanel signupForm = new JPanel(new GridLayout(4, 2, 15, 15));
            signupForm.setBackground(Theme.PRIMARY_BG);

            signupUser = new JTextField(15);
            signupPass = new JPasswordField(15);
            email = new JTextField(15);
            Theme.styleTextField(signupUser);
            Theme.styleTextField(signupPass);
            Theme.styleTextField(email);

            JButton signupBtn = new JButton("✅ Create Account");
            Theme.styleButton(signupBtn);

            JLabel slUser = new JLabel("Username:");
            JLabel slPass = new JLabel("Password:");
            JLabel slEmail = new JLabel("Email:");
            Theme.styleLabel(slUser);
            Theme.styleLabel(slPass);
            Theme.styleLabel(slEmail);

            signupForm.add(slUser);
            signupForm.add(signupUser);
            signupForm.add(slPass);
            signupForm.add(signupPass);
            signupForm.add(slEmail);
            signupForm.add(email);
            signupForm.add(new JLabel("")); 
            signupForm.add(signupBtn);

            JPanel infoPanel = new JPanel();
            infoPanel.setBackground(new Color(50, 50, 55));
            infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));

            JLabel infoLabel = new JLabel("ℹ️ New accounts are created as 'Operator' role by default");
            infoLabel.setForeground(Theme.TEXT_SECONDARY);
            infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            infoPanel.add(infoLabel);

            signupPanel.add(signupForm, BorderLayout.CENTER);
            signupPanel.add(infoPanel, BorderLayout.SOUTH);
            
            tabbedPane.addTab("🔐 LOGIN", loginPanel);
            tabbedPane.addTab("✍️ REGISTER", signupPanel);
            
            signupBtn.addActionListener(e -> handleSignup());
        } else {
            tabbedPane.addTab("🔐 LOGIN (" + selectedRole + ")", loginPanel);
        }

        // Header
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.setBackground(Theme.PRIMARY_BG);
        
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.setBackground(Theme.PRIMARY_BG);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel logoLabel = new JLabel("🚨 Relief-OP");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logoLabel.setForeground(Theme.ACCENT_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Disaster Relief Operations & Decision System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(logoLabel);
        headerPanel.add(subtitleLabel);
        
        boxPanel.add(headerPanel);
        boxPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        boxPanel.add(tabbedPane);

        mainPanel.add(boxPanel);
        add(mainPanel);

        // Action Listeners
        loginBtn.addActionListener(e -> handleLogin());

        setSize(600, 580);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void handleLogin() {
        String user = loginUser.getText();
        String pass = new String(loginPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter credentials", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = panels.DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Users WHERE username = ? AND password = ?")) {
            
            pstmt.setString(1, user);
            pstmt.setString(2, pass); 
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String name = rs.getString("name");
                String role = rs.getString("role");

                // Initialize Database Session for Auditing
                try (PreparedStatement sessStmt = conn.prepareStatement("SET @current_user_id = ?")) {
                    sessStmt.setInt(1, userId);
                    sessStmt.execute();
                }
                
                JOptionPane.showMessageDialog(this, "Login successful! Welcome " + name + " (" + role + ")");
                this.dispose();
                new MainFrame(role); // Passing the role to MainFrame
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSignup() {
        String user = signupUser.getText();
        String pass = new String(signupPass.getPassword());
        String mail = email.getText();

        if (user.isEmpty() || pass.isEmpty() || mail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = panels.DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO Users (name, role, username, password) VALUES (?, 'Operator', ?, ?)")) {
            
            pstmt.setString(1, user); 
            pstmt.setString(2, user);
            pstmt.setString(3, pass);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Account created successfully as 'Operator'! Please login.");
                // Switch to login tab
                tabbedPane.setSelectedIndex(0);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Signup failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
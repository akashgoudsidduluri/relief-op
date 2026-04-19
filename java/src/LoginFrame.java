import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.plaf.ColorUIResource;
import panels.Theme; // Import our new Theme!

public class LoginFrame extends JFrame {
    private JTextField loginUser, signupUser, email;
    private JPasswordField loginPass, signupPass;

    public LoginFrame() {
        setTitle("Relief-OP Secure Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make the JTabbedPane look premium locally
        UIManager.put("TabbedPane.background", new ColorUIResource(Theme.SECONDARY_BG));
        UIManager.put("TabbedPane.foreground", new ColorUIResource(Theme.TEXT_MAIN));
        UIManager.put("TabbedPane.selected", new ColorUIResource(Theme.ACCENT_COLOR));
        UIManager.put("TabbedPane.contentAreaColor", new ColorUIResource(Theme.PRIMARY_BG));
        UIManager.put("TabbedPane.font", Theme.FONT_BOLD);

        // Main container (centers everything)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.PRIMARY_BG); // Dark mode background
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.SECONDARY_BG);
        tabbedPane.setForeground(Theme.TEXT_MAIN);

        // ---------------- LOGIN PANEL ----------------
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        loginPanel.setBackground(Theme.PRIMARY_BG);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        loginUser = new JTextField(15);
        loginPass = new JPasswordField(15);
        Theme.styleTextField(loginUser);
        Theme.styleTextField(loginPass);

        JButton loginBtn = new JButton("Login");
        Theme.styleButton(loginBtn);

        JLabel lblUser = new JLabel("Username:");
        JLabel lblPass = new JLabel("Password:");
        Theme.styleLabel(lblUser);
        Theme.styleLabel(lblPass);

        loginPanel.add(lblUser);
        loginPanel.add(loginUser);
        loginPanel.add(lblPass);
        loginPanel.add(loginPass);
        loginPanel.add(new JLabel("")); 
        loginPanel.add(loginBtn);

        // ---------------- SIGNUP PANEL ----------------
        JPanel signupPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        signupPanel.setBackground(Theme.PRIMARY_BG);
        signupPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        signupUser = new JTextField(15);
        signupPass = new JPasswordField(15);
        email = new JTextField(15);
        Theme.styleTextField(signupUser);
        Theme.styleTextField(signupPass);
        Theme.styleTextField(email);

        JButton signupBtn = new JButton("Sign Up Account");
        Theme.styleButton(signupBtn);

        JLabel slUser = new JLabel("Username:");
        JLabel slPass = new JLabel("Password:");
        JLabel slEmail = new JLabel("Email:");
        Theme.styleLabel(slUser);
        Theme.styleLabel(slPass);
        Theme.styleLabel(slEmail);

        signupPanel.add(slUser);
        signupPanel.add(signupUser);
        signupPanel.add(slPass);
        signupPanel.add(signupPass);
        signupPanel.add(slEmail);
        signupPanel.add(email);
        signupPanel.add(new JLabel("")); // spacer
        signupPanel.add(signupBtn);

        // Add tabs
        tabbedPane.addTab("Login", loginPanel);
        tabbedPane.addTab("Register", signupPanel);

        // Add a title header above the tabs
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.setBackground(Theme.PRIMARY_BG);
        
        JLabel titleLabel = new JLabel("Relief-OP Portal");
        titleLabel.setFont(Theme.FONT_H1);
        titleLabel.setForeground(Theme.TEXT_MAIN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        boxPanel.add(titleLabel);
        boxPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacing
        boxPanel.add(tabbedPane);

        mainPanel.add(boxPanel);
        add(mainPanel);

        // Action Listeners
        loginBtn.addActionListener(e -> handleLogin());
        signupBtn.addActionListener(e -> handleSignup());

        setSize(500, 450);
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
                JTabbedPane tabs = (JTabbedPane) ((JPanel)getContentPane().getComponent(0)).getComponent(2); // Index adjusted for BoxPanel structure
                tabs.setSelectedIndex(0);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Signup failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
package panels;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Moved to constants for easier maintenance
    private static final String URL = "jdbc:mysql://localhost:3306/reliefops";
    private static final String USER = "root";
    private static final String PASS = "1602-akash-066";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Returns a database connection. 
     * NOTE: For a real application, consider using a connection pool (like HikariCP).
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Ensure mysql-connector-j is in the lib folder.", e);
        }
    }

    /**
     * Utility method to test the connection.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database Connection Test Failed: " + e.getMessage());
            return false;
        }
    }
}
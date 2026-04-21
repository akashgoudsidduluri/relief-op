package panels;

/**
 * Database Configuration Class
 * Centralized location for database credentials and settings.
 * 
 * For production: Use environment variables or .properties files
 * Example: URL = System.getenv("DB_URL");
 */
public class DBConfig {
    
    // TODO: Move these to environment variables or external config file for production
    private static final String DB_URL = "jdbc:mysql://localhost:3306/reliefops";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1602-akash-066"; // SECURITY: Change this immediately in production
    
    /**
     * Get database URL
     */
    public static String getUrl() {
        return System.getenv("DB_URL") != null ? System.getenv("DB_URL") : DB_URL;
    }
    
    /**
     * Get database username
     */
    public static String getUser() {
        return System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DB_USER;
    }
    
    /**
     * Get database password
     */
    public static String getPassword() {
        return System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DB_PASSWORD;
    }
}

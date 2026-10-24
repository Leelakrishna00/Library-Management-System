package library;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {}

        try {
            Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            System.err.println("Notice: MySQL Driver not present. Using in-memory fallback.");
        }
    }

    private DBConnection() {}

    public static String getUrl() {
        return props.getProperty("db.url", "jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public static String getUser() { return props.getProperty("db.user", "root"); }
    public static String getPassword() { return props.getProperty("db.password", "root"); }
    public static double getFinePerDay() {
        try { return Double.parseDouble(props.getProperty("library.fine_per_day", "2.00")); } catch (Exception e) { return 2.0; }
    }
    public static int getBorrowDurationDays() {
        try { return Integer.parseInt(props.getProperty("library.borrow_duration_days", "14")); } catch (Exception e) { return 14; }
    }

    public static Connection getConnection() throws Exceptions.DatabaseException {
        try {
            return DriverManager.getConnection(getUrl(), getUser(), getPassword());
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Database error: " + e.getMessage(), e);
        }
    }

    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}

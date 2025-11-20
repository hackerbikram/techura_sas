package bikram.db;

import java.io.File;
import java.sql.*;

public class DbConnector {
    private static Connection connection;
    private static final String DB_DIR = System.getProperty("user.home") + "/Techura"; // writable folder
    private static final String DB_PATH = DB_DIR + "/app.db";

    private DbConnector() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Ensure directory exists
                File dir = new File(DB_DIR);
                if (!dir.exists()) dir.mkdirs();

                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
                connection.setAutoCommit(true);
                System.out.println("db connect successfully: " + DB_PATH);
            }
        } catch (Exception e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("SQL db connection closed");
            }
        } catch (Exception e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

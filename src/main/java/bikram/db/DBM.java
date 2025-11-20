package bikram.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBM {
    public static int getCount(String tableName){
        String sql = "SELECT COUNT(*) AS total FROM "+tableName;
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()){
            return rs.getInt("total");
        } catch (Exception e){
            System.err.println("Coutn failed: "+e.getMessage());
        }return 0;
    }

    public static void dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("✅ Dropped table: " + tableName);

        } catch (Exception e) {
            System.err.println("❌ Failed to drop table " + tableName + ": " + e.getMessage());
        }
    }

    /**
     * Drop products and sales tables at once
     */
    public static void dropAllTables() {
        dropTable("products");
        dropTable("sales");
        System.out.println("✅ All tables dropped successfully!");
    }
}

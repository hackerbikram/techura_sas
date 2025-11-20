package bikram.db;

import bikram.model.Sales;
import bikram.views.page.TechuraDashboard;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ⚡ Advanced Sales Database Manager
 * Supports CRUD + sales analytics and insights.
 */
public class SalesDB implements Salesrepository{
    public SalesDB() {
        createTable();
    }

    // ---------- CREATE TABLE ----------
    @Override
    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    quantity INTEGER,
                    purchase_price REAL,
                    sale_price REAL,
                    discount REAL,
                    final_amount REAL,
                    profit REAL,
                    loss REAL,
                    created_at TEXT,
                    updated_at TEXT
                );
                """;

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Sales table ready.");
        } catch (Exception e) {
            System.err.println("❌ Table creation failed: " + e.getMessage());
        }
    }

    // ---------- INSERT ----------
    @Override
    public boolean saveSales(Sales s) {
        String sql = """
                INSERT INTO sales 
                (product_id, name, quantity, purchase_price, sale_price, discount, final_amount, profit, loss, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getProductId());
            pstmt.setString(2, s.getName());
            pstmt.setInt(3, s.getQuantity());
            pstmt.setDouble(4, s.getPurchasePrice());
            pstmt.setDouble(5, s.getSalePrice());
            pstmt.setDouble(6, s.getDiscount());
            pstmt.setDouble(7, s.getFinalAmount());
            pstmt.setDouble(8, s.getProfit());
            pstmt.setDouble(9, s.getLoss());
            pstmt.setString(10, s.getCreatedAt().toString());
            pstmt.setString(11, s.getUpdatedAt().toString());

            pstmt.executeUpdate();
            System.out.println("💾 Sale saved: " + s.getName());
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Save failed: " + e.getMessage());
            return false;
        }
    }

    // ---------- BASIC ANALYTICS ----------

    /** 💰 Total Revenue */
    @Override
    public double getTotalRevenue() {
        String sql = "SELECT SUM(final_amount) FROM sales";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error calculating total revenue: " + e.getMessage());
            return 0;
        }
    }

    /** 💸 Total Profit */
    @Override
    public double getTotalProfit() {
        String sql = "SELECT SUM(profit) FROM sales";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error calculating profit: " + e.getMessage());
            return 0;
        }
    }

    /** 📉 Total Loss */
    @Override
    public double getTotalLoss() {
        String sql = "SELECT SUM(loss) FROM sales";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error calculating loss: " + e.getMessage());
            return 0;
        }
    }

    /** 📦 Total Quantity Sold */
    @Override
    public int getTotalQuantitySold() {
        String sql = "SELECT SUM(quantity) FROM sales";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error calculating total quantity: " + e.getMessage());
            return 0;
        }
    }

    // ---------- ADVANCED ANALYTICS ----------

    /** 🏆 Most Sold Product */
    @Override
    public String getMostSoldProduct() {
        String sql = """
                SELECT name, SUM(quantity) as total_sold
                FROM sales
                GROUP BY name
                ORDER BY total_sold DESC
                LIMIT 1
                """;
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getString("name") + " (" + rs.getInt("total_sold") + ")" : "No data";
        } catch (SQLException e) {
            System.err.println("Error getting most sold product: " + e.getMessage());
            return "Error";
        }
    }

    /** 🗓️ Average Sales Per Day */
    @Override
    public double getAverageSalesPerDay() {
        String sql = """
                SELECT AVG(daily_total) FROM (
                    SELECT DATE(created_at) as date, SUM(final_amount) as daily_total
                    FROM sales
                    GROUP BY DATE(created_at)
                )
                """;
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error calculating average sales: " + e.getMessage());
            return 0;
        }
    }

    /** 📅 Get Sales Summary for Date Range */
    @Override
    public double getSalesByDateRange(LocalDate start, LocalDate end) {
        String sql = "SELECT SUM(final_amount) FROM sales WHERE DATE(created_at) BETWEEN ? AND ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error getting range sales: " + e.getMessage());
            return 0;
        }
    }

    /** 🔝 Top 5 Products by Revenue */
    @Override
    public List<String> getTop5Products() {
        String sql = """
                SELECT name, SUM(final_amount) as total
                FROM sales
                GROUP BY name
                ORDER BY total DESC
                LIMIT 5
                """;
        List<String> top = new ArrayList<>();
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                top.add(rs.getString("name") + " - ¥" + rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching top products: " + e.getMessage());
        }
        return top;
    }

    /** 📊 Print Sales Summary */
    @Override
    public void printSalesSummary() {
        System.out.println("\n===== 🧾 SALES SUMMARY =====");
        System.out.printf("💰 Total Revenue: ¥%.2f%n", getTotalRevenue());
        System.out.printf("💸 Total Profit: ¥%.2f%n", getTotalProfit());
        System.out.printf("📉 Total Loss: ¥%.2f%n", getTotalLoss());
        System.out.printf("📦 Total Quantity Sold: %d%n", getTotalQuantitySold());
        System.out.printf("🏆 Most Sold Product: %s%n", getMostSoldProduct());
        System.out.printf("📈 Avg. Daily Sales: ¥%.2f%n", getAverageSalesPerDay());
        System.out.println("🔝 Top 5 Products:");
        getTop5Products().forEach(p -> System.out.println("   • " + p));
        System.out.println("===========================\n");
    }
    @Override
    public Map<String, Integer> getMonthlyNewUsers() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = """
            SELECT strftime('%m', created_at) AS month, COUNT(*) AS total
            FROM users
            WHERE created_at >= date('now', '-6 months')
            GROUP BY strftime('%Y-%m', created_at)
            ORDER BY month ASC
            """;

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // 📅 Pre-fill last 6 months to keep chart consistent
            java.time.format.DateTimeFormatter monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM");
            java.time.LocalDate now = java.time.LocalDate.now();

            for (int i = 5; i >= 0; i--) {
                java.time.LocalDate month = now.minusMonths(i);
                data.put(month.format(monthFormatter), 0);
            }

            // 📊 Fill database results
            while (rs.next()) {
                String monthNum = rs.getString("month");
                int total = rs.getInt("total");

                int monthInt = Integer.parseInt(monthNum);
                String monthName = java.time.LocalDate.of(now.getYear(), monthInt, 1)
                        .format(monthFormatter);

                data.put(monthName, total);
            }

        } catch (SQLException e) {
            System.err.println("Error loading monthly new users: " + e.getMessage());
        }

        return data;
    }


    /** 🔢 Count Total Sales Transactions */
    @Override
    public int countSales() {
        String sql = "SELECT COUNT(*) FROM sales";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting sales: " + e.getMessage());
            return 0;
        }
    }

    // ---------- ADVANCED DASHBOARD ANALYTICS ----------

    /**
     * 📅 Monthly Revenue (last 6 months)
     * Returns Map<String, Double> where key = "Jan", value = total
     */
    @Override
    public Map<String, Double> getMonthlyRevenue() {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = """
                SELECT strftime('%m', created_at) AS month, SUM(final_amount) AS total
                FROM sales
                WHERE created_at >= date('now', '-6 months')
                GROUP BY strftime('%Y-%m', created_at)
                ORDER BY month ASC
                """;

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
            LocalDate now = LocalDate.now();

            // Preload last 6 months to ensure chart continuity
            for (int i = 5; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                data.put(month.format(monthFormatter), 0.0);
            }

            while (rs.next()) {
                String monthNum = rs.getString("month");
                double total = rs.getDouble("total");

                int monthInt = Integer.parseInt(monthNum);
                String monthName = LocalDate.of(LocalDate.now().getYear(), monthInt, 1)
                        .format(monthFormatter);

                data.put(monthName, total);
            }

        } catch (SQLException e) {
            System.err.println("Error loading monthly revenue: " + e.getMessage());
        }

        return data;
    }

    /**
     * 🧾 Recent Activities (latest 10 sales)
     * Returns List<Activity> for dashboard table.
     */
    @Override
    public List<TechuraDashboard.Activity> getRecentActivities() {
        List<TechuraDashboard.Activity> list = new ArrayList<>();
        String sql = """
                SELECT name, quantity, final_amount, created_at
                FROM sales
                ORDER BY datetime(created_at) DESC
                LIMIT 10
                """;

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm");

            while (rs.next()) {
                String user = rs.getString("name");
                String action = "Sold x" + rs.getInt("quantity") + " for ¥" + rs.getDouble("final_amount");
                String time = rs.getString("created_at");

                try {
                    LocalDate date = LocalDate.parse(time.split("T")[0]);
                    time = date.format(formatter);
                } catch (Exception ignored) {}

                list.add(new TechuraDashboard.Activity(user, action, time));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching recent activities: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Map<String, Double> getMonthlyProfit() {
        Map<String, Double> profitMap = new LinkedHashMap<>();

        String sql = """
        SELECT 
            strftime('%Y-%m', created_at) AS month,
            SUM(profit) AS total_profit
        FROM sales
        GROUP BY month
        ORDER BY month;
    """;

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String month = rs.getString("month");
                double profit = rs.getDouble("total_profit");
                profitMap.put(month, profit);
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error fetching monthly profit: " + e.getMessage());
        }

        return profitMap;
    }
    @Override
    public int countSalesByProduct(String name) {
        String sql = "SELECT SUM(quantity) AS total_sold FROM sales WHERE name = ?;";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("total_sold");
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Error counting sales by product: " + e.getMessage());
        }
        return 0;
    }
    @Override
    public Map<String, Integer> getMonthlySalesTrend() {
        Map<String, Integer> trendMap = new LinkedHashMap<>();

        String sql = """
        SELECT 
            strftime('%Y-%m', created_at) AS month,
            SUM(quantity) AS total_sold
        FROM sales
        GROUP BY month
        ORDER BY month;
    """;

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String month = rs.getString("month");
                int total = rs.getInt("total_sold");
                trendMap.put(month, total);
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error fetching monthly sales trend: " + e.getMessage());
        }

        return trendMap;
    }

    @Override
    public double getProfitByProduct(String productName) {
        String sql = """
        SELECT SUM((sale_price - purchase_price) * quantity) AS profit
        FROM sales
        WHERE name = ?
    """;

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("profit");
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Error fetching profit for product '" + productName + "': " + e.getMessage());
        }

        return 0.0;
    }
    @Override
    public double getTotalCost() {
        String sql = "SELECT SUM(purchase_priceå * quantity) AS total_cost FROM sales";

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total_cost");
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Error fetching total cost: " + e.getMessage());
        }

        return 0.0;
    }

    @Override
    public Map<String, Double> getDailySales() {
        Map<String, Double> dailySales = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29); // last 30 days

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

        for (int i = 0; i < 30; i++) {
            LocalDate date = startDate.plusDays(i);
            double total = 0;

            try {
                String sql = "SELECT SUM(final_amount) FROM sales WHERE DATE(created_at) = ?";

                PreparedStatement ps = DbConnector.getConnection().prepareStatement(sql);
                ps.setString(1, date.toString()); // assuming date stored as 'YYYY-MM-DD'
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
                rs.close();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            dailySales.put(date.format(formatter), total);
        }

        return dailySales;
    }


}

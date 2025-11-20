package bikram.db;

import bikram.model.Product;
import bikram.util.IdGenerator;

import java.sql.*;
import java.util.*;

public class ProductDB implements ProductRepository {
    // ---------------- CREATE TABLE ----------------
    @Override
    public void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS products (
                id TEXT UNIQUE NOT NULL,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                cost REAL DEFAULT 0,
                discount REAL DEFAULT 0,
                quantity INTEGER NOT NULL,
                description TEXT,
                category TEXT,
                supplier TEXT,
                created_at DATE,
                expire_date DATE
            );
        """;
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log("✅ 商品テーブル作成成功！");
        } catch (SQLException e) {
            logError("商品テーブル作成失敗: " + e.getMessage());
        }
    }

    // ---------------- ADD PRODUCT ----------------
    @Override
    public void addProduct(Product product) {
        String sql = """
            INSERT INTO products (id, name, price, cost, discount, quantity, description, category, supplier, created_at, expire_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String productId = (product.getId() == null || product.getId().isEmpty()) ? IdGenerator.idGenerate("PRD", 4) : product.getId();

            pstmt.setString(1, productId);
            pstmt.setString(2, product.getName());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setDouble(4, product.getCost());
            pstmt.setDouble(5, product.getDiscount());
            pstmt.setInt(6, product.getQuantity());
            pstmt.setString(7, product.getDescription());
            pstmt.setString(8, product.getCategory());
            pstmt.setString(9, product.getSupplier());
            pstmt.setDate(10, product.getCreated_at());
            pstmt.setDate(11, product.getExpire_date());

            pstmt.executeUpdate();
            log("✅ 商品追加成功: " + product.getName());
        } catch (SQLException e) {
            logError("商品追加失敗: " + e.getMessage());
        }
    }

    // ---------------- ANALYTICS METHODS ----------------

    @Override
    public double getTotalCost() {
        String sql = "SELECT SUM(cost * quantity) AS totalCost FROM products";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("totalCost");
        } catch (SQLException e) {
            logError("総コスト計算失敗: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public double getProfitByProduct(String productName) {
        String sql = """
            SELECT (price - cost) * quantity AS profit
            FROM products
            WHERE LOWER(name) = LOWER(?)
        """;
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("profit");
            }
        } catch (SQLException e) {
            logError("利益計算失敗: " + productName + " → " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public Map<String, Double> getMonthlyProfit() {
        Map<String, Double> monthlyProfit = new LinkedHashMap<>();
        String sql = """
            SELECT strftime('%m', created_at) AS month,
                   SUM((price - cost) * quantity) AS profit
            FROM products
            GROUP BY strftime('%Y-%m', created_at)
            ORDER BY month ASC
        """;
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                monthlyProfit.put(rs.getString("month"), rs.getDouble("profit"));
            }
        } catch (SQLException e) {
            logError("月別利益取得失敗: " + e.getMessage());
        }
        return monthlyProfit;
    }

    @Override
    public Map<String, Integer> getMonthlySalesTrend() {
        Map<String, Integer> monthlyTrend = new LinkedHashMap<>();
        String sql = """
            SELECT strftime('%m', created_at) AS month,
                   SUM(quantity) AS total_sold
            FROM products
            GROUP BY strftime('%Y-%m', created_at)
            ORDER BY month ASC
        """;
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                monthlyTrend.put(rs.getString("month"), rs.getInt("total_sold"));
            }
        } catch (SQLException e) {
            logError("月別販売数取得失敗: " + e.getMessage());
        }
        return monthlyTrend;
    }

    // ---------------- EXISTING METHODS ----------------
    @Override
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetToProduct(rs));
            log("✅ 全商品取得成功 (" + list.size() + ")");
        } catch (SQLException e) {
            logError("商品取得失敗: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Product getProductById(String id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            logError("ID検索失敗: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Product> getProductByName(String name) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE LOWER(name) LIKE LOWER(?)";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logError("商品名検索失敗: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void updateProduct(Product product) {
        String sql = """
            UPDATE products SET
                name = ?, price = ?, cost = ?, discount = ?, quantity = ?, description = ?,
                category = ?, supplier = ?, created_at = ?, expire_date = ?
            WHERE id = ?
        """;
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setDouble(3, product.getCost());
            pstmt.setDouble(4, product.getDiscount());
            pstmt.setInt(5, product.getQuantity());
            pstmt.setString(6, product.getDescription());
            pstmt.setString(7, product.getCategory());
            pstmt.setString(8, product.getSupplier());
            pstmt.setDate(9, product.getCreated_at());
            pstmt.setDate(10, product.getExpire_date());
            pstmt.setString(11, product.getId());

            int rows = pstmt.executeUpdate();
            if (rows > 0) log("✅ 商品更新成功: " + product.getName());
        } catch (SQLException e) {
            logError("商品更新失敗: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteProductById(String id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logError("ID削除失敗: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int deleteProductByName(String name) {
        String sql = "DELETE FROM products WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("名前削除失敗: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public double getTotalProductValue() {
        String sql = "SELECT SUM(price * quantity) AS totalValue FROM products";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("totalValue");
        } catch (SQLException e) {
            logError("総価値取得失敗: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public double getAveragePrice() {
        String sql = "SELECT AVG(price) AS avgPrice FROM products";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("avgPrice");
        } catch (SQLException e) {
            logError("平均価格取得失敗: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public Map<String, Integer> getCategorySales() {
        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        String sql = "SELECT category, SUM(quantity) AS total_sold FROM products GROUP BY category ORDER BY total_sold DESC";

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categoryMap.put(rs.getString("category"), rs.getInt("total_sold"));
            }
        } catch (SQLException e) {
            logError("カテゴリー売上取得失敗: " + e.getMessage());
        }
        return categoryMap;
    }

    // --- Helper ---
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getString("id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getDouble("price"));
        p.setCost(rs.getDouble("cost"));
        p.setDiscount(rs.getDouble("discount"));
        p.setQuantity(rs.getInt("quantity"));
        p.setDescription(rs.getString("description"));
        p.setCategory(rs.getString("category"));
        p.setSupplier(rs.getString("supplier"));
        p.setCreated_at(rs.getDate("created_at"));
        p.setExpire_date(rs.getDate("expire_date"));
        return p;
    }

    private void log(String msg) {
        System.out.println("📌 " + msg);
    }

    private void logError(String msg) {
        System.err.println("❌ " + msg);
    }
}

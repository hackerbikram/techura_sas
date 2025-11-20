package bikram.db;

import bikram.model.Role;
import bikram.model.User;
import bikram.security.SecurityAuth;
import bikram.security.SecurityUtil;
import bikram.util.IdGenerator;

import java.sql.*;
import java.util.*;

/**
 * 🔹 UserDB — Handles all user-related database operations
 * Fully refactored for Techura app
 */
public class UserDB implements UserRepository {


    // ---------------- CREATE TABLE ----------------
    @Override
    public void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT UNIQUE NOT NULL,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                address TEXT,
                phone_number TEXT NOT NULL,
                email TEXT UNIQUE,
                password TEXT,
                role TEXT NOT NULL,
                joined_date DATE,
                salaryPerMonth NUMBER
            );
        """;

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log("✅ ユーザーテーブル作成成功！");
        } catch (SQLException e) {
            logError("ユーザーテーブル作成失敗: " + e.getMessage());
        }
    }

    // ---------------- ADD USER ----------------
    @Override
    public void addUser(User user) {
        String sql = """
            INSERT INTO users(id, first_name, last_name, address, phone_number, email, password, role, joined_date, salaryPerMonth)
            VALUES(?,?,?,?,?,?,?,?,?,?)
        """;

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String userId = (user.getId() == null || user.getId().isEmpty()) ? IdGenerator.idGenerate("USR", 4) : user.getId();
            String hashedPassword = SecurityUtil.hashPassword(user.getPassword());

            pstmt.setString(1, userId);
            pstmt.setString(2, user.getFirstName());
            pstmt.setString(3, user.getLastName());
            pstmt.setString(4, user.getAddress());
            pstmt.setString(5, user.getPhoneNumber());
            pstmt.setString(6, user.getEmail());
            pstmt.setString(7, hashedPassword);
            pstmt.setString(8, user.getRole().name());
            pstmt.setDate(9, user.getJoined_date());
            pstmt.setDouble(10, user.getSalaryPerMonth());

            pstmt.executeUpdate();
            log("✅ ユーザー追加成功: " + user.getFullName());

        } catch (SQLException e) {
            logError("ユーザー追加失敗: " + e.getMessage());
        }
    }

    // ---------------- READ ALL USERS ----------------
    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
            log("✅ 全ユーザー取得成功 (" + list.size() + ")");

        } catch (SQLException e) {
            logError("ユーザー取得失敗: " + e.getMessage());
        }
        return list;
    }

    // ---------------- READ BY EMAIL ----------------
    @Override
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            logError("メール検索失敗: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Optional<User> getUserByEmailOptional(String email) {
        return Optional.ofNullable(getUserByEmail(email));
    }

    // ---------------- UPDATE USER ----------------
    @Override
    public void updateUser(User user) {
        String sql = """
            UPDATE users SET
                first_name = ?,
                last_name = ?,
                address = ?,
                phone_number = ?,
                email = ?,
                password = ?,
                role = ?,
                joined_date = ?,
                salaryPerMonth = ?
            WHERE id = ?
        """;

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getAddress());
            pstmt.setString(4, user.getPhoneNumber());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPassword());
            pstmt.setString(7, user.getRole().name());
            pstmt.setDate(8, user.getJoined_date());
            pstmt.setDouble(9, user.getSalaryPerMonth());
            pstmt.setString(10, user.getId());

            int rows = pstmt.executeUpdate();
            if (rows > 0) log("✅ ユーザー更新成功: " + user.getFullName());
            else log("⚠️ 該当ユーザーなし: " + user.getId());

        } catch (SQLException e) {
            logError("ユーザー更新失敗: " + e.getMessage());
        }
    }

    // ---------------- DELETE USER ----------------
    @Override
    public void deleteUser(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) log("🗑️ ユーザー削除成功: " + id);
            else log("⚠️ 該当ユーザーなし: " + id);

        } catch (SQLException e) {
            logError("ユーザー削除失敗: " + e.getMessage());
        }
    }

    // ---------------- DROP TABLE ----------------
    @Override
    public void dropTable(String tablename) {
        String sql = "DROP TABLE IF EXISTS " + tablename;
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log("✅ テーブル削除成功: " + tablename);
        } catch (SQLException e) {
            logError("テーブル削除失敗: " + e.getMessage());
        }
    }

    // ---------------- LOGIN ----------------
    @Override
    public Optional<User> verifyLogin(String email, String rawPassword) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (SecurityUtil.verifyPassword(rawPassword, storedHash)) {
                        User user = mapResultSetToUser(rs);
                        SecurityAuth.setCurrentUser(user);
                        log("✅ ログイン成功: " + email);
                        return Optional.of(user);
                    } else log("⚠️ パスワード不正: " + email);
                } else log("⚠️ 該当ユーザーなし: " + email);
            }

        } catch (SQLException e) {
            logError("ログインチェック失敗: " + e.getMessage());
        }
        return Optional.empty();
    }

    // ---------------- MONTHLY USER ANALYTICS ----------------
    @Override
    public Map<String, Integer> getMonthlyNewUsers() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = """
            SELECT strftime('%m', joined_date) AS month, COUNT(*) AS total
            FROM users
            WHERE joined_date >= date('now', '-6 months')
            GROUP BY strftime('%Y-%m', joined_date)
            ORDER BY month ASC
        """;

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            java.time.format.DateTimeFormatter monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM");
            java.time.LocalDate now = java.time.LocalDate.now();

            for (int i = 5; i >= 0; i--) {
                java.time.LocalDate month = now.minusMonths(i);
                data.put(month.format(monthFormatter), 0);
            }

            while (rs.next()) {
                int monthInt = Integer.parseInt(rs.getString("month"));
                String monthName = java.time.LocalDate.of(now.getYear(), monthInt, 1).format(monthFormatter);
                data.put(monthName, rs.getInt("total"));
            }

        } catch (SQLException e) {
            logError("月別ユーザー取得失敗: " + e.getMessage());
        }
        return data;
    }

    // ---------------- GET BY ID ----------------
    @Override
    public User getUserById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            logError("ID検索失敗: " + e.getMessage());
        }
        return null;
    }

    // ---------------- HELPER: MAP RESULTSET ----------------
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role").toUpperCase());
        return new User(
                rs.getString("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("address"),
                rs.getString("phone_number"),
                rs.getString("email"),
                rs.getString("password"),
                role,
                rs.getDate("joined_date"),
                rs.getDouble("salaryPerMonth")
        );
    }

    // ---------------- LOGGING ----------------
    private void log(String msg) {
        System.out.println("📌 " + msg);
    }

    private void logError(String msg) {
        System.err.println("❌ " + msg);
    }

}

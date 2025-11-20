package bikram.db;

import bikram.model.EmployeTime;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;



public class EmployeeTimeDB implements EmployeeTimeRepository {
    private UserRepository urepo;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    // ============================================================
// TABLE CREATION / DROP
// ============================================================
    @Override
    public void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS employeeTime (
                userid TEXT,
                entrytime TEXT,
                exittime TEXT,
                hours REAL
            );
            """;

        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            log("🟢 employeeTime table created successfully");

        } catch (SQLException e) {
            logError("❌ Failed to create employeeTime table:"+e.getMessage());
        }
    }

    private void log(String s) {
        System.err.println(s);
    }

    private void logError(String message) {
        System.err.println("❌ ERROR: " + message);
    }


    @Override
    public void dropTable() {
        String sql = "DROP TABLE IF EXISTS employeeTime";
        try (Connection conn = DbConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            log("🟢 employeeTime table dropped successfully");

        } catch (SQLException e) {
            logError("❌ Failed to drop employeeTime table: " + e.getMessage());
        }
    }

    // ============================================================
// SAVE / UPDATE
// ============================================================
    @Override
    public void saveEmployeTime(EmployeTime et) {
        String sql = "INSERT INTO employeeTime(userid, entrytime, exittime, hours) VALUES(?,?,?,?)";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, et.getUserId());
            pstmt.setString(2, et.getEntryTime());
            pstmt.setString(3, et.getExitTime());
            pstmt.setDouble(4, et.getWorkHours());

            pstmt.executeUpdate();
            log("🟢 EmployeeTime saved successfully");

        } catch (SQLException e) {
            logError("❌ Failed to save EmployeeTime: " + e.getMessage());
        }
    }

    @Override
    public void updateTime(String userid, EmployeTime et) {
        String sql = """
            UPDATE employeeTime
            SET exittime = ?, hours = ?
            WHERE userid = ? AND entrytime = ?
            """;

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, et.getExitTime());
            pstmt.setDouble(2, et.getWorkHours());
            pstmt.setString(3, userid);
            pstmt.setString(4, et.getEntryTime());

            pstmt.executeUpdate();
            log("🔵 EmployeeTime updated successfully");

        } catch (SQLException e) {
            logError("❌ Failed to update EmployeeTime: " + e.getMessage());
        }
    }

    // ============================================================
// GETTERS
// ============================================================
    private LocalDateTime parse(String ts) {
        if (ts == null) return null;
        try {
            return LocalDateTime.parse(ts, FORMATTER);
        } catch (Exception e) {
            logError("❌ Failed to parse timestamp: " + ts + " → " + e.getMessage());
            return null;
        }
    }

    private EmployeTime fromResultSet(ResultSet rs) throws SQLException {
        return new EmployeTime(
                rs.getString("userid"),
                rs.getString("entrytime"),
                rs.getString("exittime")
        );
    }

    @Override
    public EmployeTime getLastEntry(String userid) {
        String sql = """
            SELECT * FROM employeeTime
            WHERE userid = ? AND exittime IS NULL
            ORDER BY entrytime DESC LIMIT 1
            """;

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userid);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) return fromResultSet(rs);

        } catch (SQLException e) {
            logError("❌ Failed to get last entry: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<EmployeTime> getAllRecords(String userid) {
        List<EmployeTime> list = new ArrayList<>();
        String sql = "SELECT * FROM employeeTime WHERE userid = ? ORDER BY entrytime ASC";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userid);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(fromResultSet(rs));
            }

        } catch (SQLException e) {
            logError("❌ Failed to get all records: " + e.getMessage());
        }

        return list;
    }

    @Override
    public List<EmployeTime> getMonthlyRecords(String userid, int year, int month) {
        List<EmployeTime> list = new ArrayList<>();
        String sql = """
            SELECT * FROM employeeTime
            WHERE userid = ? AND strftime('%Y', entrytime)=? AND strftime('%m', entrytime)=?
            ORDER BY entrytime ASC
            """;

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userid);
            pstmt.setString(2, String.valueOf(year));
            pstmt.setString(3, String.format("%02d", month));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(fromResultSet(rs));

        } catch (SQLException e) {
            logError("❌ Failed to get monthly records: " + e.getMessage());
        }
        return list;
    }
    // ============================================================
// TIME CALCULATIONS
// ============================================================
    @Override
    public double getDailyHours(String userid, int year, int month, int day) {
        String sql = """
            SELECT SUM(hours) FROM employeeTime
            WHERE userid = ? AND strftime('%Y', entrytime)=? AND strftime('%m', entrytime)=? AND strftime('%d', entrytime)=?
            """;

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userid);
            pstmt.setString(2, String.valueOf(year));
            pstmt.setString(3, String.format("%02d", month));
            pstmt.setString(4, String.format("%02d", day));

            ResultSet rs = pstmt.executeQuery();
            return rs.getDouble(1);

        } catch (SQLException e) {
            logError("❌ Failed to get daily hours: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public double getWeeklyHours(String userid, int year, int weekNumber) {
        LocalDate start = LocalDate.ofYearDay(year, 1)
                .with(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNumber)
                .with(java.time.DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(6);

        String sql = "SELECT SUM(hours) FROM employeeTime WHERE userid=? AND entrytime BETWEEN ? AND ?";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userid);
            pstmt.setString(2, start.atStartOfDay().format(FORMATTER));
            pstmt.setString(3, end.atTime(23, 59, 59).format(FORMATTER));

            ResultSet rs = pstmt.executeQuery();
            return rs.getDouble(1);

        } catch (SQLException e) {
            logError("❌ Failed to get weekly hours: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public double getMonthlyTime(String userid, int year, int month) {
        String sql = "SELECT SUM(hours) FROM employeeTime WHERE userid=? AND strftime('%Y', entrytime)=? AND strftime('%m', entrytime)=?";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userid);
            pstmt.setString(2, String.valueOf(year));
            pstmt.setString(3, String.format("%02d", month));

            ResultSet rs = pstmt.executeQuery();
            return rs.getDouble(1);

        } catch (SQLException e) {
            logError("❌ Failed to get monthly hours: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public double getYearlyHours(String userid, int year) {
        String sql = "SELECT SUM(hours) FROM employeeTime WHERE userid=? AND strftime('%Y', entrytime)=?";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userid);
            pstmt.setString(2, String.valueOf(year));

            ResultSet rs = pstmt.executeQuery();
            return rs.getDouble(1);

        } catch (SQLException e) {
            logError("❌ Failed to get yearly hours: " + e.getMessage());
            return 0;
        }
    }

    // ============================================================
// OVERTIME
// ============================================================
    @Override
    public double getDailyOvertime(String userid, int year, int month, int day) {
        return Math.max(0, getDailyHours(userid, year, month, day) - 8.0);
    }

    @Override
    public double getMonthlyOvertime(String userid, int year, int month) {
        double total = 0;
        int days = YearMonth.of(year, month).lengthOfMonth();
        for (int d = 1; d <= days; d++) total += getDailyOvertime(userid, year, month, d);
        return total;
    }

    // ============================================================
// LATE / EARLY LEAVE
// ============================================================
    @Override
    public int getLateMinutes(String userid, int year, int month, int day) {
        String sql = "SELECT entrytime FROM employeeTime WHERE userid=? AND strftime('%Y', entrytime)=? AND strftime('%m', entrytime)=? AND strftime('%d', entrytime)=? ORDER BY entrytime ASC LIMIT 1";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userid);
            pstmt.setString(2, String.valueOf(year));
            pstmt.setString(3, String.format("%02d", month));
            pstmt.setString(4, String.format("%02d", day));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                LocalDateTime actual = parse(rs.getString("entrytime"));
                if (actual != null) {
                    LocalDateTime expected = LocalDateTime.of(year, month, day, 9, 0);
                    if (actual.isAfter(expected)) return (int) Duration.between(expected, actual).toMinutes();
                }
            }

        } catch (SQLException e) {
            logError("❌ Failed to calculate late minutes: " + e.getMessage());
        }

        return 0;
    }

    @Override
    public int getEarlyLeaveMinutes(String userid, int year, int month, int day) {
        String sql = "SELECT exittime FROM employeeTime WHERE userid=? AND strftime('%Y', entrytime)=? AND strftime('%m', entrytime)=? AND strftime('%d', entrytime)=? ORDER BY exittime DESC LIMIT 1";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userid);
            pstmt.setString(2, String.valueOf(year));
            pstmt.setString(3, String.format("%02d", month));
            pstmt.setString(4, String.format("%02d", day));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                LocalDateTime actual = parse(rs.getString("exittime"));
                if (actual != null) {
                    LocalDateTime expected = LocalDateTime.of(year, month, day, 18, 0);
                    if (actual.isBefore(expected)) return (int) Duration.between(actual, expected).toMinutes();
                }
            }

        } catch (SQLException e) {
            logError("❌ Failed to calculate early leave minutes: " + e.getMessage());
        }

        return 0;
    }

    // ============================================================
// SALARY CALCULATIONS
// ============================================================
    @Override
    public double calculateDailyWage(String userid, int year, int month, int day, double hourlyRate) {
        return getDailyHours(userid, year, month, day) * hourlyRate;
    }

    @Override
    public double calculateMonthlySalary(String userid, int year, int month, double hourlyRate) {
        return getMonthlyTime(userid, year, month) * hourlyRate;
    }

    @Override
    public double calculateMonthlyOvertimePay(String userid, int year, int month, double overtimeRate) {
        return getMonthlyOvertime(userid, year, month) * overtimeRate;
    }

    @Override
    public double calculateMonthlyDeductions(String userid, int year, int month, double penaltyPerMinute) {
        double total = 0;
        int days = YearMonth.of(year, month).lengthOfMonth();
        for (int d = 1; d <= days; d++)
            total += (getLateMinutes(userid, year, month, d) + getEarlyLeaveMinutes(userid, year, month, d)) * penaltyPerMinute;
        return total;
    }

    @Override
    public double calculateTotalMonthlyPay(String userid, int year, int month, double hourlyRate, double overtimeRate) {
        return calculateMonthlySalary(userid, year, month, hourlyRate)
                + calculateMonthlyOvertimePay(userid, year, month, overtimeRate);
    }

    @Override
    public double calculateFinalPayroll(String userid, int year, int month, double hourlyRate, double overtimeRate, double penaltyPerMinute) {
        return calculateTotalMonthlyPay(userid, year, month, hourlyRate, overtimeRate)
                - calculateMonthlyDeductions(userid, year, month, penaltyPerMinute);
    }

}

package bikram.db;


import bikram.model.EmployeTime;

import java.util.List;

public interface EmployeeTimeRepository {

    // -------------------------
    // DATABASE STRUCTURE
    // -------------------------
    void createTable();
    void dropTable(); // optional

    // -------------------------
    // CRUD OPERATIONS
    // -------------------------
    void saveEmployeTime(EmployeTime employeTime);
    void updateTime(String userid, EmployeTime employeTime);
    List<EmployeTime> getAllRecords(String userid);
    EmployeTime getLastEntry(String userid);

    // -------------------------
    // TIME CALCULATIONS
    // -------------------------


    // Total work hours for given day
    double getDailyHours(String userid, int year, int month, int day);

    // Get weekly hours (Mon–Sun)
    double getWeeklyHours(String userid, int year, int weekNumber);

    // Total hours in month
    double getMonthlyTime(String userid, int year, int month);

    // Total hours in year
    double getYearlyHours(String userid, int year);

    // Overtime (>8 hours/day)
    double getDailyOvertime(String userid, int year, int month, int day);

    // Overtime for entire month
    double getMonthlyOvertime(String userid, int year, int month);

    // Late entry calculation
    int getLateMinutes(String userid, int year, int month, int day);

    // Early leave calculation
    int getEarlyLeaveMinutes(String userid, int year, int month, int day);

    // -------------------------
    // SALARY / PAYROLL
    // -------------------------

    // Wage calculation for 1 day
    double calculateDailyWage(String userid, int year, int month, int day, double hourlyRate);

    // Monthly salary (normal hours)
    double calculateMonthlySalary(String userid, int year, int month, double hourlyRate);

    // Overtime salary
    double calculateMonthlyOvertimePay(String userid, int year, int month, double overtimeRate);

    // Total salary = regular + overtime
    double calculateTotalMonthlyPay(String userid, int year, int month, double hourlyRate, double overtimeRate);

    // -------------------------
    // EXTRA BUSINESS FEATURES
    // -------------------------

    // Deduction for late & early leave
    double calculateMonthlyDeductions(String userid, int year, int month, double penaltyPerMinute);

    // Final payroll after deductions
    double calculateFinalPayroll(
            String userid,
            int year,
            int month,
            double hourlyRate,
            double overtimeRate,
            double penaltyPerMinute
    );

    // Export all data for a month (for CSV/PDF generation)
    List<EmployeTime> getMonthlyRecords(String userid, int year, int month);
}

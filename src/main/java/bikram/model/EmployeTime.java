package bikram.model;

import bikram.db.EmployeeTimeDB;
import bikram.db.EmployeeTimeRepository;
import bikram.db.UserDB;
import bikram.db.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EmployeTime {

    // Updated formatter for "yyyy-MM-dd HH:mm:ss"
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String userId;
    private String entryTime;   // stored as String
    private String exitTime;    // stored as String
    private double workHours;

    private UserRepository urepo = new UserDB();
    private EmployeeTimeRepository etRepo = new EmployeeTimeDB();

    // --------------------------
    // ENTRY ONLY
    // --------------------------
    public EmployeTime(String userId, String entryTime) {
        this.userId = userId;
        this.entryTime = entryTime;
        this.exitTime = null;
        this.workHours = 0;
    }

    // --------------------------
    // ENTRY + EXIT
    // --------------------------
    public EmployeTime(String userId, String entryTime, String exitTime) {
        this.userId = userId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;

        if (exitTime != null) {
            this.workHours = calculateHours(entryTime, exitTime);
        }
    }

    // --------------------------
    // TIME CALCULATION
    // --------------------------
    private double calculateHours(String entry, String exit) {
        try {
            LocalDateTime e1 = LocalDateTime.parse(entry, F);
            LocalDateTime e2 = LocalDateTime.parse(exit, F);
            Duration d = Duration.between(e1, e2);
            return d.toMinutes() / 60.0;
        } catch (DateTimeParseException ex) {
            System.err.println("Failed to parse entry/exit times: " + entry + " / " + exit);
            return 0;
        }
    }

    // --------------------------
    // SET EXIT
    // --------------------------
    public void setExitNow() {
        this.exitTime = LocalDateTime.now().format(F);
        this.workHours = calculateHours(entryTime, exitTime);
        etRepo.updateTime(userId, this);
    }

    // --------------------------
    // GETTERS
    // --------------------------
    public String getUserId() {
        return userId;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public String getExitTime() {
        return exitTime;
    }

    public double getWorkHours() {
        return workHours;
    }

    public long getWorkSeconds() {
        return (long) (workHours * 3600);
    }

    public User getUser() {
        return urepo.getUserById(userId);
    }
    public static EmployeTime getLastEntry(String userId, EmployeeTimeRepository etRepo, UserRepository urepo) {
        EmployeTime et = etRepo.getLastEntry(userId);
        if (et != null) {
            // Inject repositories for further operations
            et.urepo = urepo;
            et.etRepo = etRepo;
        }
        return et;
    }

}

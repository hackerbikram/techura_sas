package bikram.util;
import java.time.LocalDate;
public class DateManager {
    private static LocalDate created_at = LocalDate.now();
    private static LocalDate expiryDate = created_at.plusMonths(6);

    public static LocalDate getExpiryDate() {
        return expiryDate;
    }

    public static LocalDate getCreated_at() {
        return created_at;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public static void setCreated_at(LocalDate created_at) {
        DateManager.created_at = created_at;
    }
}

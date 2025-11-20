package bikram.db;

import bikram.model.User;
import java.util.*;

public interface UserRepository {
    void createTable();
    void addUser(User user);
    List<User> getAllUsers();
    User getUserByEmail(String email);
    Optional<User> getUserByEmailOptional(String email);
    void updateUser(User user);
    void deleteUser(String id);
    void dropTable(String tableName);
    Optional<User> verifyLogin(String email, String rawPassword);
    User getUserById(String id);
    Map<String, Integer> getMonthlyNewUsers();
}

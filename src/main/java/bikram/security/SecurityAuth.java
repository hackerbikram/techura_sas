package bikram.security;

import bikram.model.Role;
import bikram.model.User;
import bikram.views.ui.NotificationsManager;

/**
 * 🔒 SecurityAuth manages the current logged-in user session.
 * It ensures that the app can easily check authentication and authorization
 * at any time — without leaking sensitive data.
 */
public class SecurityAuth {

    private static User currentUser;

    /** ✅ Set authenticated user after successful login */
    public static void setCurrentUser(User user) {
        currentUser = user;
        System.out.println("👤 Logged in as: " + user.getFullName() + " (" + user.getRole() + ")");
    }

    /** ✅ Get currently logged-in user */
    public static User getCurrentUser() {
        return currentUser;
    }

    /** ✅ Check if a user is logged in */
    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    /** ✅ Check if current user has a specific role */
    public static boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    /** 🚪 Logout current user and clear session */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("🚪 User logged out: " + currentUser.getFullName());
            currentUser = null;
        }
    }
    public static boolean permission(){
        if (currentUser ==null){
            NotificationsManager.showNotification("ログイン","ログインが必要です。ログインして下さい", NotificationsManager.NotificationType.WARNING);
        }
        if (currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.OWNER){
            return true;
        }else {
            NotificationsManager.showNotification("許可が否定されました","所有者 ID または管理者 ID でログインしてください。", NotificationsManager.NotificationType.WARNING);
        }
        return false;
    }
}

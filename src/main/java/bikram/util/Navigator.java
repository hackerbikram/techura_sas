package bikram.util;

import bikram.security.SecurityAuth;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class Navigator {

    private static StackPane mainContent;

    // Known package paths
    private static final String[] VIEW_PACKAGES = {
            "bikram.views.page.",
            "bikram.views.form.",
            "bikram.views.ui.",
            "bikram.views.dashboard.",
            "bikram.views."
    };

    private static final Map<String, Node> REGISTERED_PAGES = new HashMap<>();
    private static final Map<String, Class<? extends Node>> REGISTERED_CLASSES = new HashMap<>();

    // ✅ Set the main StackPane
    public static void setMainContent(StackPane contentPane) {
        mainContent = contentPane;
    }

    // ✅ Register page (instance)
    public static void registerPage(String name, Node page) {
        REGISTERED_PAGES.put(name, page);
        System.out.println("📦 Registered page instance: " + name);
    }

    // ✅ Register class
    public static void registerPageClass(String name, Class<? extends Node> clazz) {
        REGISTERED_CLASSES.put(name, clazz);
        System.out.println("📘 Registered page class: " + name);
    }

    // ✅ Clear cache
    public static void clearRegistry() {
        REGISTERED_PAGES.clear();
        REGISTERED_CLASSES.clear();
        System.out.println("🧹 Navigator cache cleared");
    }

    // 🚀 Non-blocking navigate with fade animation
    public static void navigate(String pageName) {
        if (mainContent == null) {
            System.err.println("⚠️ mainContent not set — call Navigator.setMainContent()");
            return;
        }

        Task<Node> loadTask = new Task<>() {
            @Override
            protected Node call() {
                return getPageInstance(pageName);
            }
        };

        loadTask.setOnSucceeded(event -> {
            Node page = loadTask.getValue();
            if (page == null) {
                System.err.println("❌ Page not found: " + pageName);
                mainContent.getChildren().setAll(new Label("❌ Page not found: " + pageName));
                return;
            }

            // Replace content instantly
            mainContent.getChildren().setAll(page);

            // ✨ Smooth fade
            FadeTransition fade = new FadeTransition(Duration.millis(350), page);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.setCycleCount(1);
            fade.play();

            System.out.println("✅ Navigated to " + pageName);
        });

        loadTask.setOnFailed(event -> {
            Throwable ex = loadTask.getException();
            System.err.println("❌ Failed to navigate: " + ex.getMessage());
            ex.printStackTrace();
        });

        new Thread(loadTask, "Page-Loader-" + pageName).start();
    }

    // 🔍 Load or reuse a page instance
    private static Node getPageInstance(String name) {
        try {
            // Preloaded instance
            if (REGISTERED_PAGES.containsKey(name))
                return REGISTERED_PAGES.get(name);

            // Registered class
            if (REGISTERED_CLASSES.containsKey(name)) {
                Node node = REGISTERED_CLASSES.get(name).getDeclaredConstructor().newInstance();
                REGISTERED_PAGES.put(name, node);
                return node;
            }

            // Auto-load from known packages
            for (String pkg : VIEW_PACKAGES) {
                String full = pkg + name;
                try {
                    Class<?> clazz = Class.forName(full);
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    if (instance instanceof Node node) {
                        REGISTERED_PAGES.put(name, node);
                        return node;
                    }
                } catch (ClassNotFoundException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error instantiating page: " + name + " → " + e.getMessage());
            e.printStackTrace();
        }
        return new Label("❌ Page not found: " + name);
    }

    // 🔄 Reset to login
    public static void resetToLogin() {
        try {
            bikram.security.SecurityAuth.logout();
            if (mainContent != null)
                mainContent.getChildren().clear();

            navigate("LoginPage");
            System.out.println("🔁 Reset to LoginPage complete.");
        } catch (Exception e) {
            System.err.println("⚠️ Failed to reset: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void nevigateToSecurePage(String Pagename){
        if (SecurityAuth.permission()){
            navigate(Pagename);
        }else {
            System.err.println("セキュリティ リージョンによって許可が与えられました");
        }
    }
    public static void nevigateToLoginRequiredPage(String pagename){
        if (SecurityAuth.isAuthenticated()){
            navigate(pagename);
        }else {
            System.err.println("ログインが拒否された場合、権限が拒否されました");
        }
    }
}

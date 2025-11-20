package bikram.views.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ThemeManager {

    private static final String DARK_STYLESHEET = "dark-theme.css"; // adjust your path
    private static final AtomicBoolean darkMode = new AtomicBoolean(false);

    private ThemeManager() {}

    public static void toggleTheme() {
        boolean toDark = !darkMode.get();
        darkMode.set(toDark);
        applyTheme(toDark);
    }

    private static void applyTheme(boolean dark) {
        Platform.runLater(() -> {
            for (Window window : Window.getWindows()) {
                Scene scene = window.getScene();
                if (scene == null) continue;

                if (dark) {
                    if (!scene.getStylesheets().contains(DARK_STYLESHEET)) {
                        scene.getStylesheets().add(DARK_STYLESHEET);
                    }
                } else {
                    scene.getStylesheets().removeIf(s -> s.equals(DARK_STYLESHEET));
                }
            }
        });
    }

    public static boolean isDarkMode() {
        return darkMode.get();
    }
}

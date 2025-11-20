package bikram.util;

import bikram.util.ConfirmDialog;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Small utility for app refresh and confirmed exit.
 * Keep heavy tasks off the FX thread.
 */
public final class AppRefresher {

    private AppRefresher() {}

    public static void refreshApp() {
        // lightweight refresh hook: you can wire actual refresh logic here.
        // If you need to perform heavy IO, run it on a background thread.
        Platform.runLater(() -> {
            // Example: call some app-wide refresh listeners
            // AppEventBus.publish(new AppRefreshEvent());
        });
    }

    public static void confirmExit() {
        Platform.runLater(() -> {
            boolean yes = ConfirmDialog.show("アプリケーションを終了する", "本当に終了してもよろしいですか?");
            if (yes) {
                Stage primaryStage = AppContext.getPrimaryStage(); // make sure you stored the main stage
                if (primaryStage != null) {
                    BorderPane root = (BorderPane) primaryStage.getScene().getRoot();

                    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), root);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(ev -> {
                        Platform.exit();  // close JavaFX threads
                        System.exit(0);   // exit JVM
                    });
                    fadeOut.play();
                } else {
                    // fallback if stage is not available
                    Platform.exit();
                    System.exit(0);
                }
            }
        });
    }

}

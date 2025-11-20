package bikram.views.ui;

import bikram.util.AppContext;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

// In AppRefresher class
public class AppRefresher {

    public static void confirmExit() {
        Stage primaryStage = AppContext.getPrimaryStage();
 /* get your main stage reference */;
        if (primaryStage == null) return;

        BorderPane root = (BorderPane) primaryStage.getScene().getRoot();

        FadeTransition fadeRoot = new FadeTransition(Duration.seconds(0.8), root);
        fadeRoot.setFromValue(1.0);
        fadeRoot.setToValue(0.0);
        fadeRoot.setOnFinished(ev -> Platform.exit());
        fadeRoot.play();
    }
}

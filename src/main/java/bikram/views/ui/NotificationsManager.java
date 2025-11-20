package bikram.views.ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.Queue;

public class NotificationsManager {

    private static final Popup popup = new Popup();
    private static final VBox notificationBox = new VBox(10);
    private static final Queue<HBox> activeNotifications = new LinkedList<>();

    static {
        notificationBox.setAlignment(Pos.TOP_RIGHT);
        notificationBox.setPadding(new Insets(20));
        notificationBox.setStyle("-fx-background-color: transparent;");
        popup.getContent().add(notificationBox);
        popup.setAutoHide(false);
    }

    // Show a notification globally
    public static void showNotification(String title, String message, NotificationType type) {
        Stage stage = getPrimaryStage();
        if (stage == null) return;

        HBox notification = createNotification(title, message, type);
        notificationBox.getChildren().add(0, notification);
        activeNotifications.add(notification);

        if (!popup.isShowing()) popup.show(stage);

        // Slide in + fade + slight scale
        notification.setTranslateX(400);
        notification.setOpacity(0);
        notification.setScaleX(0.9);
        notification.setScaleY(0.9);

        Timeline slideIn = new Timeline(
                new KeyFrame(Duration.seconds(0.45),
                        new KeyValue(notification.translateXProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(notification.opacityProperty(), 1, Interpolator.EASE_IN),
                        new KeyValue(notification.scaleXProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(notification.scaleYProperty(), 1, Interpolator.EASE_OUT))
        );
        slideIn.play();

        // Auto-hide after 4s with pause on hover
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        notification.setOnMouseEntered(e -> pause.pause());
        notification.setOnMouseExited(e -> pause.playFromStart());
        pause.setOnFinished(e -> hideNotification(notification));
        pause.play();
    }

    // Hide notification smoothly
    private static void hideNotification(HBox notification) {
        Timeline slideOut = new Timeline(
                new KeyFrame(Duration.seconds(0.35),
                        new KeyValue(notification.translateXProperty(), 400, Interpolator.EASE_IN),
                        new KeyValue(notification.opacityProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(notification.scaleXProperty(), 0.9, Interpolator.EASE_IN),
                        new KeyValue(notification.scaleYProperty(), 0.9, Interpolator.EASE_IN))
        );
        slideOut.setOnFinished(e -> {
            notificationBox.getChildren().remove(notification);
            activeNotifications.remove(notification);
            if (notificationBox.getChildren().isEmpty()) popup.hide();
        });
        slideOut.play();
    }

    // Create notification UI
    private static HBox createNotification(String title, String message, NotificationType type) {
        HBox box = new HBox(12);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(400);
        box.setStyle("-fx-background-color: " + getBgColor(type) + ";" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.35);" +
                "-fx-border-width: 1.5;" +
                "-fx-backdrop-filter: blur(12px);");

        DropShadow glow = new DropShadow(18, getGlowColor(type));
        glow.setSpread(0.25);
        box.setEffect(glow);

        Label icon = new Label(getIcon(type));
        icon.setFont(Font.font(22));

        VBox textBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.WHITE);

        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Arial", 13));
        msgLabel.setTextFill(Color.web("#f1f1f1"));
        msgLabel.setWrapText(true);

        textBox.getChildren().addAll(titleLabel, msgLabel);
        box.getChildren().addAll(icon, textBox);

        return box;
    }

    private static String getBgColor(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "rgba(0, 200, 83, 0.22)";
            case INFO -> "rgba(33, 150, 243, 0.22)";
            case WARNING -> "rgba(255, 193, 7, 0.25)";
            case ERROR -> "rgba(244, 67, 54, 0.25)";
        };
    }

    private static Color getGlowColor(NotificationType type) {
        return switch (type) {
            case SUCCESS -> Color.LIMEGREEN;
            case INFO -> Color.DODGERBLUE;
            case WARNING -> Color.GOLD;
            case ERROR -> Color.RED;
        };
    }

    private static String getIcon(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "✅";
            case INFO -> "💡";
            case WARNING -> "⚠️";
            case ERROR -> "❌";
        };
    }

    private static Stage getPrimaryStage() {
        for (var s : Stage.getWindows().stream().filter(w -> w instanceof Stage).toList()) {
            if (s.isShowing()) return (Stage) s;
        }
        return null;
    }

    public enum NotificationType { SUCCESS, INFO, WARNING, ERROR }
}

package bikram.views.page;

import bikram.model.User;
import bikram.security.SecurityAuth;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfilePage extends StackPane {

    public ProfilePage() {
        setPadding(new Insets(60));
        setAlignment(Pos.CENTER);

        // 🌌 Gradient background
        setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #141E30, #243B55);
        """);

        User user = SecurityAuth.getCurrentUser();
        if (user == null) {
            Label error = new Label("⚠️ ログイン中のユーザーがいません！");
            error.setTextFill(Color.WHITE);
            error.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            getChildren().add(error);
            return;
        }

        // 🌈 Profile Card
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setPrefWidth(480);
        card.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.12);
            -fx-background-radius: 25;
            -fx-border-radius: 25;
            -fx-border-color: rgba(255,255,255,0.25);
            -fx-border-width: 1.2;
        """);

        DropShadow cardGlow = new DropShadow(25, Color.web("#00FFFF80"));
        cardGlow.setSpread(0.25);
        card.setEffect(cardGlow);

        // 🧑‍💼 Avatar with initials
        String initials = getInitials(user.getFullName());
        Circle avatarCircle = new Circle(60);
        avatarCircle.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5,
                true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#00c6ff")),
                new Stop(1, Color.web("#0072ff"))));
        avatarCircle.setEffect(new DropShadow(15, Color.CYAN));

        Label initialsLabel = new Label(initials);
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setFont(Font.font("Poppins", FontWeight.BOLD, 34));

        StackPane avatar = new StackPane(avatarCircle, initialsLabel);
        avatar.setAlignment(Pos.CENTER);

        // ✨ Avatar hover pulse animation
        avatar.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(250), avatar);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });
        avatar.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(250), avatar);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // 🧾 User info
        Label name = new Label(user.getFullName());
        name.setFont(Font.font("Poppins", FontWeight.BOLD, 26));
        name.setTextFill(Color.WHITE);

        Label role = new Label("🛡️ 役割: " + user.getRole());
        role.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 16));
        role.setTextFill(Color.LIGHTGRAY);

        Label email = new Label("✉️ メール: " + user.getEmail());
        email.setFont(Font.font("Segoe UI", 15));
        email.setTextFill(Color.SILVER);

        String formattedTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日  HH:mm"));
        Label login = new Label("🕒 ログイン時刻: " + formattedTime);
        login.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        login.setTextFill(Color.rgb(200, 200, 200));

        card.getChildren().addAll(avatar, name, role, email, login);
        getChildren().add(card);

        // 🌟 Fade-in animation
        FadeTransition fade = new FadeTransition(Duration.seconds(1.3), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /** 🔠 Extract initials from user full name */
    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}

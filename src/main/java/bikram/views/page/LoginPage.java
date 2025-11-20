package bikram.views.page;

import bikram.db.UserDB;
import bikram.model.User;
import bikram.security.SecurityAuth;
import bikram.util.Navigator;
import bikram.views.ui.NotificationsManager;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.util.Optional;

public class LoginPage extends StackPane {

    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private static boolean isLogin;

    public LoginPage() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(60));

        // 🌌 Animated gradient background
        BackgroundFill gradientFill = new BackgroundFill(
                Color.web("#0f2027"), CornerRadii.EMPTY, Insets.EMPTY);
        setBackground(new Background(gradientFill));

        // 🔄 Animate gradient background smoothly
        Timeline bgAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(backgroundProperty(),
                                new Background(new BackgroundFill(
                                        Color.web("#0f2027"), CornerRadii.EMPTY, Insets.EMPTY)))),
                new KeyFrame(Duration.seconds(8),
                        new KeyValue(backgroundProperty(),
                                new Background(new BackgroundFill(
                                        Color.web("#2c5364"), CornerRadii.EMPTY, Insets.EMPTY)))),
                new KeyFrame(Duration.seconds(16),
                        new KeyValue(backgroundProperty(),
                                new Background(new BackgroundFill(
                                        Color.web("#203a43"), CornerRadii.EMPTY, Insets.EMPTY))))
        );
        bgAnimation.setCycleCount(Animation.INDEFINITE);
        bgAnimation.setAutoReverse(true);
        bgAnimation.play();

        // 🧱 Main form container
        VBox form = new VBox(18);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(50, 60, 60, 60));
        form.setPrefWidth(420);
        form.setStyle("""
            -fx-background-color: rgba(255,255,255,0.08);
            -fx-background-radius: 25;
            -fx-border-color: rgba(255,255,255,0.2);
            -fx-border-radius: 25;
            -fx-border-width: 1.2;
            -fx-effect: dropshadow(gaussian, rgba(0,255,255,0.3), 20, 0.3, 0, 0);
        """);

        // 🌟 Subtle glass glow
        DropShadow neon = new DropShadow(25, Color.CYAN);
        neon.setSpread(0.3);
        form.setEffect(neon);

        // 🏷️ Title + Subtitle
        Label title = new Label("TECHURAログイン");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("デジタルコントロールパネルにアクセスする");
        subtitle.setTextFill(Color.LIGHTGRAY);
        subtitle.setFont(Font.font("Segoe UI", 14));

        // 📧 Email field
        emailField.setPromptText("電子メールアドレス");
        emailField.setStyle("""
            -fx-background-color: rgba(255,255,255,0.12);
            -fx-text-fill: white;
            -fx-prompt-text-fill: gray;
            -fx-background-radius: 20;
            -fx-padding: 12 18;
            -fx-border-color: transparent;
        """);
        addFocusGlow(emailField);

        // 🔒 Password field
        passwordField.setPromptText("パスワード");
        passwordField.setStyle("""
            -fx-background-color: rgba(255,255,255,0.12);
            -fx-text-fill: white;
            -fx-prompt-text-fill: gray;
            -fx-background-radius: 20;
            -fx-padding: 12 18;
            -fx-border-color: transparent;
        """);
        addFocusGlow(passwordField);

        // 🚀 Login Button
        Button loginBtn = new Button("Login");
        loginBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        loginBtn.setTextFill(Color.WHITE);
        loginBtn.setStyle("""
            -fx-background-color: linear-gradient(to right, #6a11cb, #2575fc);
            -fx-background-radius: 30;
            -fx-cursor: hand;
            -fx-padding: 12 45;
            -fx-effect: dropshadow(gaussian, rgba(0, 200, 255, 0.4), 15, 0.3, 0, 0);
        """);

        // ✨ Hover animation
        loginBtn.setOnMouseEntered(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(150), loginBtn);
            s.setToX(1.07);
            s.setToY(1.07);
            s.play();

            loginBtn.setStyle("""
                -fx-background-color: linear-gradient(to right, #8e2de2, #4a00e0);
                -fx-background-radius: 30;
                -fx-cursor: hand;
                -fx-padding: 12 45;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.6), 15, 0.4, 0, 0);
            """);
        });

        loginBtn.setOnMouseExited(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(150), loginBtn);
            s.setToX(1.0);
            s.setToY(1.0);
            s.play();

            loginBtn.setStyle("""
                -fx-background-color: linear-gradient(to right, #6a11cb, #2575fc);
                -fx-background-radius: 30;
                -fx-cursor: hand;
                -fx-padding: 12 45;
                -fx-effect: dropshadow(gaussian, rgba(0, 200, 255, 0.4), 15, 0.3, 0, 0);
            """);
        });

        // ⚡ Fast click animation
        loginBtn.setOnMousePressed(e -> loginBtn.setScaleX(0.95));
        loginBtn.setOnMouseReleased(e -> {
            loginBtn.setScaleX(1);
            handleLogin();
        });

        // ✨ Enter key triggers login
        passwordField.setOnAction(e -> handleLogin());

        // 🌀 Fade-in animation for form
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), form);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // 🪄 Add all nodes
        form.getChildren().addAll(title, subtitle, emailField, passwordField, loginBtn);
        getChildren().add(form);
    }

    // ✅ Handle login logic
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            NotificationsManager.showNotification("欠落しているフィールド", "すべての項目にご記入ください.", NotificationsManager.NotificationType.WARNING);
            return;
        }

        UserDB userDB = new UserDB();
        Optional<User> result = userDB.verifyLogin(email, password);

        if (result.isPresent()) {
            SecurityAuth.setCurrentUser(result.get());
            setLogin(true);
            NotificationsManager.showNotification("おかえり 🎉", "こんにちは " + result.get().getFullName()+ "様", NotificationsManager.NotificationType.SUCCESS);

            clearFields();
            Navigator.navigate("TechuraDashboard");
        } else {
            NotificationsManager.showNotification("ログインに失敗しました", "メールアドレスまたはパスワードが無効です", NotificationsManager.NotificationType.ERROR);
            clearFields();
        }
    }

    // 🧹 Clear all input fields
    private void clearFields() {
        emailField.clear();
        passwordField.clear();
    }

    // 💡 Add glow on focus for input fields
    private void addFocusGlow(TextField field) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.15);
                    -fx-text-fill: white;
                    -fx-prompt-text-fill: gray;
                    -fx-background-radius: 20;
                    -fx-border-color: #00e5ff;
                    -fx-border-width: 1.5;
                    -fx-border-radius: 20;
                    -fx-padding: 12 18;
                """);
            } else {
                field.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.12);
                    -fx-text-fill: white;
                    -fx-prompt-text-fill: gray;
                    -fx-background-radius: 20;
                    -fx-border-color: transparent;
                    -fx-padding: 12 18;
                """);
            }
        });
    }
    public static boolean isLogin(){
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }
}

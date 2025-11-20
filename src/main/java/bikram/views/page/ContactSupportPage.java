package bikram.views.page;

import bikram.util.AppContext;
import bikram.views.ui.NotificationsManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ContactSupportPage extends StackPane {

    public ContactSupportPage() {
        setPadding(new Insets(60));
        setAlignment(Pos.CENTER);
        setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #0f0c29, #302b63, #24243e);
        """);

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setPrefWidth(480);
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.08);
            -fx-background-radius: 25;
            -fx-border-color: rgba(255,255,255,0.2);
            -fx-border-radius: 25;
            -fx-border-width: 1.5;
        """);

        DropShadow glow = new DropShadow(25, Color.CYAN);
        glow.setSpread(0.2);
        card.setEffect(glow);

        Label title = new Label("💬 テクラサポートに問い合わせる");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);

        Label desc = new Label("いつでもお手伝いいたします。以下の連絡方法をお選びください。:");
        desc.setFont(Font.font("Segoe UI", 14));
        desc.setTextFill(Color.LIGHTGRAY);
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);

        Label email = new Label("📩 dhurbakhadka85@gmail.com");
        email.setFont(Font.font("Segoe UI", 16));
        email.setTextFill(Color.WHITE);
        email.setOnMouseClicked(e -> openEmail());
        email.setCursor(Cursor.HAND);

        Button facebookBtn = new Button("Facebookページにアクセス 🌐");
        facebookBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        facebookBtn.setTextFill(Color.WHITE);
        facebookBtn.setStyle("""
            -fx-background-color: linear-gradient(to right, #1877F2, #42A5F5);
            -fx-background-radius: 20;
            -fx-cursor: hand;
            -fx-padding: 10 25;
        """);
        facebookBtn.setOnAction(e -> openFacebook("https://www.facebook.com/share/1BV14Ya4VB/?mibextid=wwXIfr"));

        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        card.getChildren().addAll(title, desc, email, facebookBtn);
        getChildren().add(card);
    }

    private void openEmail() {
        try {
            AppContext.getHostServices().showDocument("mailto:dhurbakhadka85@gmail.com");
            NotificationsManager.showNotification("メールを開く", "メールアプリを起動する...", NotificationsManager.NotificationType.INFO);
        } catch (Exception e) {
            NotificationsManager.showNotification("エラー", "メールクライアントを開けません.", NotificationsManager.NotificationType.ERROR);
        }
    }

    private void openFacebook(String url) {
        if (isOnline()) {
            NotificationsManager.showNotification("Facebookを開く", "Techuraページにリダイレクトしています...", NotificationsManager.NotificationType.SUCCESS);
            AppContext.getHostServices().showDocument(url);
        } else {
            NotificationsManager.showNotification("オフライン", "インターネット接続が検出されませんでした ⚠️", NotificationsManager.NotificationType.ERROR);
        }
    }

    private boolean isOnline() {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int response = conn.getResponseCode();
            return (200 <= response && response <= 399);
        } catch (IOException e) {
            return false;
        }
    }
}

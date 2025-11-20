package bikram.views.page;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.net.URL;

public class AboutPage extends StackPane {

    public AboutPage() {
        setPrefSize(1200, 800);
        setStyle("""
            -fx-background-color: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
        """);

        VBox main = new VBox(40);
        main.setAlignment(Pos.CENTER);
        main.setPadding(new Insets(60));

        // 🌟 タイトル
        Label title = new Label("Techuraについて");
        title.setTextFill(Color.CYAN);
        title.setFont(Font.font("Poppins", FontWeight.EXTRA_BOLD, 42));
        title.setEffect(new DropShadow(20, Color.CYAN));

        // 🖼️ ロゴやバナー
        ImageView logo;
        URL logoUrl = getClass().getResource("/images/logo.png");
        if (logoUrl != null) {
            logo = new ImageView(new Image(logoUrl.toExternalForm()));
        } else {
            System.err.println("⚠️ ロゴが見つかりません: /images/logo.png");
            logo = new ImageView();
        }
        logo.setFitWidth(40);
        logo.setFitHeight(40);


        // 💬 説明
        Label desc = new Label("""
            Techuraは、最先端技術を通じてビジネスを変革することに尽力する次世代デジタルイノベーション企業です。
            私たちの使命は、創造性、自動化、そしてデザインを融合させ、未来を見据えたシームレスなデジタルソリューションを提供することです。
            2025年に設立されたTechuraは、フルスタック開発、デジタルトランスフォーメーション（DX）、そしてインテリジェントなビジネスオートメーションを専門としており、人々がよりスマートに働き、より大きな夢を実現できるよう支援しています。
        """);
        desc.setTextFill(Color.LIGHTGRAY);
        desc.setFont(Font.font("Poppins", FontPosture.REGULAR, 16));
        desc.setWrapText(true);
        desc.setMaxWidth(800);
        desc.setAlignment(Pos.CENTER);

        // ⚙️ セクション区切り
        Region divider = new Region();
        divider.setPrefHeight(2);
        divider.setMaxWidth(400);
        divider.setStyle("-fx-background-color: rgba(0,255,255,0.5);");

        // 💎 ビジョン
        VBox teamSection = new VBox(10);
        teamSection.setAlignment(Pos.CENTER);
        Label teamTitle = new Label("私たちのビジョン");
        teamTitle.setTextFill(Color.WHITE);
        teamTitle.setFont(Font.font("Poppins", FontWeight.BOLD, 22));

        Label teamDesc = new Label("""
            国境のないデジタルエコシステムを構築し、
            クリエイターを刺激し、イノベーターを支援し、
            技術を通じて人々をつなげること。
        """);
        teamDesc.setTextFill(Color.GRAY);
        teamDesc.setFont(Font.font("Poppins", 15));
        teamDesc.setWrapText(true);
        teamDesc.setMaxWidth(700);
        teamDesc.setAlignment(Pos.CENTER);

        teamSection.getChildren().addAll(teamTitle, teamDesc);

        // 🌈 フッター
        Label footer = new Label("© 2025 Techura株式会社 | Dhurba Bikram Khadka による❤\uFE0F のデザイン");
        footer.setTextFill(Color.web("#888"));
        footer.setFont(Font.font("Poppins", FontWeight.NORMAL, 12));

        main.getChildren().addAll(logo, title, desc, divider, teamSection, footer);
        getChildren().add(main);
        StackPane.setAlignment(main, Pos.CENTER);

        // ✨ スムーズフェードイン
        FadeTransition fade = new FadeTransition(Duration.seconds(1.5), main);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}

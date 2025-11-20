package bikram.views.page;

import bikram.model.User;
import bikram.security.SecurityAuth;
import com.google.gson.Gson;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Settings extends BorderPane {

    private final String SETTINGS_FILE = "settings.json";
    private final Gson gson = new Gson();
    private final User user = SecurityAuth.getCurrentUser();
    private final ProfilePage profilePage = new ProfilePage();

    // UI Components
    private final VBox mainBox = new VBox(20);
    private final Label title = new Label("⚙️ アプリ設定");
    private final Label nameField = new Label();
    private final Label emailField = new Label();
    private final ComboBox<String> languageSelect = new ComboBox<>();
    private final ComboBox<String> fontSizeSelect = new ComboBox<>();
    private final ToggleButton themeToggle = new ToggleButton("🌞 ライトモード");

    private final Label infoLabel = new Label();

    public Settings() {
        setupLayout();
        loadSettings();
    }

    private void setupLayout() {
        setPadding(new Insets(30));
        setBackground(new Background(new BackgroundFill(Color.web("#f9f9f9"), CornerRadii.EMPTY, Insets.EMPTY)));

        title.setFont(Font.font("Poppins", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#333"));

        VBox profileBox = createProfileSection();
        VBox appearanceBox = createAppearanceSection();
        VBox appBox = createAppSection();

        mainBox.getChildren().addAll(profileBox, appearanceBox, appBox);
        mainBox.setPadding(new Insets(20));
        mainBox.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(mainBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        setCenter(scroll);
    }

    private VBox createProfileSection() {
        Label sectionTitle = sectionTitle("👤 プロフィール");

        Button saveProfile = createStyledButton("💾 プロフィールを保存", "#4CAF50");
        saveProfile.setOnAction(e -> saveSettings());

        VBox box = new VBox(10, sectionTitle, profilePage, nameField, emailField, saveProfile);
        styleSection(box);
        return box;
    }

    private VBox createAppearanceSection() {
        Label sectionTitle = sectionTitle("🎨 外観設定");

        themeToggle.setOnAction(e -> {
            if (themeToggle.isSelected()) {
                themeToggle.setText("🌙 ダークモード");
                getScene().getRoot().setStyle("-fx-base: #2b2b2b; -fx-text-fill: white;");
            } else {
                themeToggle.setText("🌞 ライトモード");
                getScene().getRoot().setStyle("");
            }
            saveSettings();
        });

        languageSelect.getItems().addAll("English", "日本語", "हिन्दी");
        fontSizeSelect.getItems().addAll("小", "中", "大");

        VBox box = new VBox(10, sectionTitle, new Label("テーマ:"), themeToggle,
                new Label("言語:"), languageSelect,
                new Label("フォントサイズ:"), fontSizeSelect);
        styleSection(box);
        return box;
    }

    private VBox createAppSection() {
        Label sectionTitle = sectionTitle("💾 アプリデータ");

        Button clearBtn = createStyledButton("🧹 ローカルデータを削除", "#ff7043");
        clearBtn.setOnAction(e -> {
            File file = new File("tasks.json");
            if (file.exists()) file.delete();
            new Alert(Alert.AlertType.INFORMATION, "ローカルデータがすべて削除されました。").showAndWait();
        });

        Button resetBtn = createStyledButton("♻️ 設定をリセット", "#f44336");
        resetBtn.setOnAction(e -> {
            File file = new File(SETTINGS_FILE);
            if (file.exists()) file.delete();
            infoLabel.setText("設定が正常にリセットされました。");
        });

        Label about = new Label("🧠 バージョン: 1.0.0\n開発: Techura (Bikram)\n© 2025 すべての権利を保有");
        about.setTextFill(Color.GRAY);
        about.setFont(Font.font("Poppins", 12));

        VBox box = new VBox(10, sectionTitle, clearBtn, resetBtn, about, infoLabel);
        styleSection(box);
        return box;
    }

    private Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Poppins", FontWeight.BOLD, 18));
        lbl.setTextFill(Color.web("#444"));
        return lbl;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
        return btn;
    }

    private void styleSection(VBox box) {
        box.setPadding(new Insets(15));
        box.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        box.setEffect(new DropShadow(5, Color.gray(0.3)));
        box.setPrefWidth(600);
    }

    // ✅ 設定をJSONに保存
    private void saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("name", user.getFullName());
        settings.put("email", user.getEmail());
        settings.put("language", languageSelect.getValue());
        settings.put("fontSize", fontSizeSelect.getValue());
        settings.put("theme", themeToggle.isSelected());

        try (Writer writer = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(settings, writer);
            infoLabel.setText("設定を保存しました ✅");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ JSONから設定を読み込む
    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> settings = gson.fromJson(reader, type);

            nameField.setText(String.valueOf(settings.getOrDefault("name", "")));
            emailField.setText(String.valueOf(settings.getOrDefault("email", "")));
            languageSelect.setValue(String.valueOf(settings.getOrDefault("language", "English")));
            fontSizeSelect.setValue(String.valueOf(settings.getOrDefault("fontSize", "中")));

            Object themeVal = settings.get("theme");
            boolean dark = themeVal instanceof Boolean && (Boolean) themeVal;
            themeToggle.setSelected(dark);
            themeToggle.setText(dark ? "🌙 ダークモード" : "🌞 ライトモード");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

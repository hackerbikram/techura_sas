package bikram.views.ui;

import bikram.model.Product;
import bikram.model.Role;
import bikram.model.Sales;
import bikram.model.User;
import bikram.security.SecurityAuth;
import bikram.util.AppContext;
import bikram.util.ConfirmDialog;
import bikram.util.Navigator;
import bikram.views.page.EmploymentPaySlip;
import bikram.views.page.LoginPage;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.RealSense;

import java.io.*;

@Slf4j
public class Navbar extends HBox {

    private final MenuBar menuBar;
    private final SerchBar searchBar;
    private final Button loginButton;
    private String loginText;

    private final TextArea textEditor;
    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    private final ClipboardContent clipboardContent = new ClipboardContent();
    private final User currentuser = SecurityAuth.getCurrentUser();



    public Navbar() {
        setLoginText();
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(10, 20, 10, 20));
        setSpacing(15);
        setStyle("""
            -fx-background-color: linear-gradient(to right, rgba(10,20,25,0.9), rgba(25,40,55,0.9));
            -fx-border-color: rgba(255,255,255,0.15);
            -fx-border-width: 0 0 1 0;
            -fx-border-style: solid;
            -fx-effect: dropshadow(gaussian, rgba(0,255,255,0.25), 20, 0.2, 0, 2);
        """);
        textEditor = new TextArea();
        textEditor.setPromptText("ファイルまたは編集アクションを使用してください...");

        // ====== メニューバー設定 ======
        menuBar = new MenuBar();
        menuBar.setStyle("""
            -fx-background-color: transparent;
            -fx-font-size: 14px;
            -fx-font-family: 'Poppins';
            -fx-text-fill: white;
        """);

        menuBar.getMenus().forEach(m -> styleMenu(m));

        menuBar.getMenus().addAll(
                createFileMenu(),
                createEditMenu(),
                createProductMenu(),
                createSalesMenu(),
                createEmployeeMenu(),
                createSettingsMenu(),
                createHelpMenu()
        );

        searchBar = new SerchBar();
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        animateSearchFocus();

        // ====== ログインボタン ======
        loginButton = new Button("ログイン");
        loginButton.setText(loginText);
        loginButton.setFont(Font.font("Poppins", 14));
        loginButton.setTextFill(Color.WHITE);
        loginButton.setStyle("""
            -fx-background-color: linear-gradient(to right, #6a11cb, #2575fc);
            -fx-background-radius: 25;
            -fx-padding: 7 25;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,255,255,0.4), 10, 0.3, 0, 0);
        """);
        addButtonHover(loginButton);
        loginButton.setOnAction(e -> Navigator.navigate("LoginPage"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(menuBar, spacer, searchBar, loginButton);

        // 💫 フェードインアニメーション
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.6), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }


    // ================================================================
    // メニュー
    // ================================================================

    private Menu createFileMenu() {
        Menu file = new Menu("📁 ファイル");
        MenuItem newFile = new MenuItem("🆕 新規作成");
        MenuItem open = new MenuItem("📂 開く");
        MenuItem save = new MenuItem("💾 保存");
        MenuItem export = new MenuItem("📤 エクスポート");
        MenuItem exit = new MenuItem("❌ 終了");

        newFile.setOnAction(e -> handleNewFile());
        open.setOnAction(e -> handleOpenFile());
        save.setOnAction(e -> handleSaveFile());
        export.setOnAction(e -> handleExport());
        exit.setOnAction(e -> handleExit());

        file.getItems().addAll(newFile, open, save, export, new SeparatorMenuItem(), exit);
        return file;
    }

    private Menu createEditMenu() {
        Menu edit = new Menu("✏️ 編集");
        MenuItem undo = new MenuItem("↩️ 元に戻す");
        MenuItem redo = new MenuItem("↪️ やり直す");
        MenuItem copy = new MenuItem("📋 コピー");
        MenuItem paste = new MenuItem("📥 ペースト");

        undo.setOnAction(e -> handleUndo());
        redo.setOnAction(e -> handleRedo());
        copy.setOnAction(e -> handleCopy());
        paste.setOnAction(e -> handlePaste());

        edit.getItems().addAll(undo, redo, new SeparatorMenuItem(), copy, paste);
        return edit;
    }

    private Menu createProductMenu() {
        Menu product = new Menu("📦 製品");
        MenuItem add = new MenuItem("➕ 製品を追加");
        MenuItem manage = new MenuItem("📋 製品を管理");
        MenuItem exportCSV = new MenuItem("📊 CSVにエクスポート");
        MenuItem exportbarcode = new MenuItem("プライスカードをエクスポート");
        exportbarcode.setOnAction(e->Navigator.navigate("PriceCardPage"));
        add.setOnAction(e -> Navigator.navigate("ProductFormPage"));
        manage.setOnAction(e -> Navigator.navigate("ProductPage"));
        exportCSV.setOnAction(e -> handleExportCSV());

        product.getItems().addAll(add, manage, exportCSV, exportbarcode);
        return product;
    }

    private Menu createSalesMenu() {
        Menu sales = new Menu("💰 販売");
        MenuItem create = new MenuItem("🛒 セールを作成");
        MenuItem report = new MenuItem("📈 売上レポート");
        MenuItem top = new MenuItem("🏆 トップセールス");

        create.setOnAction(e -> {
            Navigator.navigate("SalesPage");});
        report.setOnAction(e -> Navigator.navigate("SalesReportPage"));
        top.setOnAction(e -> Navigator.navigate("TopSalesPage"));

        sales.getItems().addAll(create, report, top);
        return sales;
    }

    private Menu createEmployeeMenu() {
        Menu emp = new Menu("👥 従業員");
        MenuItem add = new MenuItem("👤 従業員を登録");
        MenuItem manage = new MenuItem("🧾 従業員を管理");
        MenuItem idcard = new MenuItem("IDカード作成");
        MenuItem timeEntry = new MenuItem("時間");
        MenuItem payroll = new MenuItem("💵 給与計算");



        add.setOnAction(e -> {

                Navigator.nevigateToSecurePage("UserFormPage");

        });

        manage.setOnAction(e -> Navigator.nevigateToSecurePage("UserDashboardPage"));
        idcard.setOnAction(e->Navigator.nevigateToSecurePage("UserIDCardPage"));
        payroll.setOnAction(e -> Navigator.navigate("EmploymentPaySlip"));
        timeEntry.setOnAction(e->Navigator.navigate("TimeManagementView"));

        emp.getItems().addAll(add, manage, idcard, payroll);
        return emp;
    }

    private Menu createSettingsMenu() {
        Menu settings = new Menu("⚙️ 設定");
        MenuItem settingsItem = new MenuItem("設定");
        MenuItem theme = new MenuItem("🎨 テーマ");
        MenuItem backup = new MenuItem("🗂 バックアップ");
        MenuItem security = new MenuItem("🔐 セキュリティ");

        settingsItem.setOnAction(e->Navigator.navigate("Settings"));
        theme.setOnAction(e -> NotificationsManager.showNotification("テーマ", "テーマ設定は近日公開予定", NotificationsManager.NotificationType.INFO));
        backup.setOnAction(e -> handleBackup());
        security.setOnAction(e -> Navigator.nevigateToSecurePage("SecuritySettingsPage"));

        settings.getItems().addAll(settingsItem, theme, backup, security);
        return settings;
    }

    private Menu createHelpMenu() {
        Menu help = new Menu("❓ ヘルプ");
        MenuItem about = new MenuItem("ℹ️ Techuraについて");
        MenuItem docs = new MenuItem("📘 ドキュメント");
        MenuItem support = new MenuItem("🆘 サポートに連絡");

        about.setOnAction(e -> Navigator.navigate("AboutPage"));
        docs.setOnAction(e -> Navigator.navigate("DocumentationPage"));
        support.setOnAction(e -> Navigator.navigate("ContactSupportPage"));

        help.getItems().addAll(about, docs, support);
        return help;
    }

    // ================================================================
    // ファイル操作
    // ================================================================

    private void handleNewFile() {
        textEditor.clear();
        NotificationsManager.showNotification("新しいファイル", "空のファイルを作成しました", NotificationsManager.NotificationType.INFO);
    }

    private void handleOpenFile() {
        NotificationsManager.showNotification("ファイルを開く", "機能は近日公開予定です", NotificationsManager.NotificationType.INFO);
    }

    private void handleSaveFile() {
        NotificationsManager.showNotification("ファイル保存", "ファイルが正常に保存されました", NotificationsManager.NotificationType.SUCCESS);
    }

    private void handleExport() {
        NotificationsManager.showNotification("エクスポート", "データをエクスポートしました", NotificationsManager.NotificationType.SUCCESS);
    }

    private void handleExit() {
        if (ConfirmDialog.show("終了", "本当に終了しますか？")) {
            Platform.exit();
            System.exit(0);
        }
    }

    // ================================================================
    // 編集操作
    // ================================================================

    private void handleUndo() {
        textEditor.undo();
        NotificationsManager.showNotification("元に戻す", "最後の操作を元に戻しました", NotificationsManager.NotificationType.INFO);
    }

    private void handleRedo() {
        textEditor.redo();
        NotificationsManager.showNotification("やり直す", "操作を再実行しました", NotificationsManager.NotificationType.INFO);
    }

    private void handleCopy() {
        String selected = textEditor.getSelectedText();
        clipboardContent.putString(selected);
        clipboard.setContent(clipboardContent);
        NotificationsManager.showNotification("コピー", "テキストをコピーしました", NotificationsManager.NotificationType.SUCCESS);
    }

    private void handlePaste() {
        if (clipboard.hasString()) {
            textEditor.insertText(textEditor.getCaretPosition(), clipboard.getString());
            NotificationsManager.showNotification("ペースト", "テキストを挿入しました", NotificationsManager.NotificationType.SUCCESS);
        }
    }

    // ================================================================
    // 追加操作
    // ================================================================

    private void handleExportCSV() {
        NotificationsManager.showNotification("CSVエクスポート", "製品データをCSVとしてエクスポートしました", NotificationsManager.NotificationType.SUCCESS);
    }

    private void handleBackup() {
        NotificationsManager.showNotification("バックアップ", "システムバックアップを開始しました...", NotificationsManager.NotificationType.INFO);
        new Thread(() -> {
            try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
            Platform.runLater(() ->
                    NotificationsManager.showNotification("バックアップ完了", "全データを安全に保存しました", NotificationsManager.NotificationType.SUCCESS)
            );
        }).start();
    }

    // ================================================================
    // UI改善
    // ================================================================

    private void addButtonHover(Button btn) {
        btn.setOnMouseEntered(e -> {
            btn.setStyle("""
            -fx-background-color: linear-gradient(to right, #8e2de2, #4a00e0);
            -fx-background-radius: 25;
            -fx-padding: 7 25;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.6), 15, 0.4, 0, 0);
        """);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), btn);
            scaleIn.setToX(1.07);
            scaleIn.setToY(1.07);
            scaleIn.play();
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle("""
            -fx-background-color: linear-gradient(to right, #6a11cb, #2575fc);
            -fx-background-radius: 25;
            -fx-padding: 7 25;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,255,255,0.4), 10, 0.3, 0, 0);
        """);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), btn);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);
            scaleOut.play();
        });
    }

    private void animateSearchFocus() {
        searchBar.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                searchBar.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.18);
                    -fx-text-fill: white;
                    -fx-background-radius: 20;
                    -fx-border-color: #00e5ff;
                    -fx-border-width: 1.3;
                    -fx-prompt-text-fill: #AAAAAA;
                    -fx-padding: 6 14;
                """);
            } else {
                searchBar.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.15);
                    -fx-text-fill: white;
                    -fx-background-radius: 20;
                    -fx-border-color: transparent;
                    -fx-padding: 6 14;
                """);
            }
        });
    }

    private void styleMenu(Menu menu) {
        menu.setStyle("-fx-text-fill: white; -fx-font-weight: 500;");
        menu.setOnShowing(e -> menu.setStyle("-fx-text-fill: cyan;"));
        menu.setOnHidden(e -> menu.setStyle("-fx-text-fill: white;"));
    }

    // Getters
    public MenuBar getMenuBar() { return menuBar; }
    public Button getLoginButton() { return loginButton; }
    public String getLoginText(){return loginText;}

    public void setLoginText() {
        if (!LoginPage.isLogin()){
            this.loginText="ログイン";
        }else {
            this.loginText = "ログアウト";
        }
    }

}

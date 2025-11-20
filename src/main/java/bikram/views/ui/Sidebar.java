package bikram.views.ui;

import bikram.security.SecurityAuth;
import bikram.util.AppContext;
import bikram.util.AppRefresher;
import bikram.util.Navigator;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;


/**
 * 超スリムサイドバー（機能は全て保持）
 * - 幅を25％スリム化
 * - パディング・間隔をコンパクトに
 * - フォントを小さくしてタイトな見た目
 * - 完全にレスポンシブで機能的
 */
public class Sidebar extends VBox {

    public final MenuButton dashboardMenu, analyticsMenu, toolsMenu, systemMenu, helpMenu, profileMenu;
    public final MenuItem viewDashboardItem, liveMonitorItem, performanceItem;
    public final MenuItem aiInsightsItem, salesTrendItem, profitTrackerItem;
    public final MenuItem calculatorItem, notesItem, taskManagerItem, qrGeneratorItem;
    public final MenuItem themeSwitcherItem, refreshAppItem, exitItem;
    public final MenuItem aboutItem, docsItem, contactSupportItem;
    public final MenuItem profileItem, settingsItem, logoutItem;
    public final MenuItem workEntryItem;


    public Sidebar() {
        // --- レイアウト調整 ---
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(4, 2, 4, 2)); // タイトなパディング
        setSpacing(3); // 間隔を詰める
        setPrefWidth(110);
        setMinWidth(100);
        setMaxWidth(120);
        getStyleClass().add("techura-ultraslim-sidebar");

        // ---------------- ダッシュボード ----------------
        viewDashboardItem = new MenuItem("🏠 ダッシュボード");
        viewDashboardItem.setOnAction(e -> Navigator.navigate("TechuraDashboard"));

        liveMonitorItem = new MenuItem("📡 ライブモニター");
        liveMonitorItem.setOnAction(e -> Platform.runLater(() -> MonitorWindow.getInstance().show()));

        performanceItem = new MenuItem("⚙️ パフォーマンス");
        performanceItem.setOnAction(e -> Platform.runLater(() -> MonitorWindow.getInstance().showPerformanceSnapshot()));

        dashboardMenu = createMenu("📊", viewDashboardItem, liveMonitorItem, performanceItem);

        // ---------------- 分析 ----------------
        aiInsightsItem = new MenuItem("🤖 AI インサイト");
        aiInsightsItem.setOnAction(e -> Navigator.navigate("AITrendPage"));

        salesTrendItem = new MenuItem("📈 売上トレンド");
        salesTrendItem.setOnAction(e -> Navigator.navigate("SalesReportPage"));

        profitTrackerItem = new MenuItem("💹 利益トラッカー");
        profitTrackerItem.setOnAction(e -> Navigator.navigate("ProfitTrackerPage"));

        analyticsMenu = createMenu("📈", aiInsightsItem, salesTrendItem, profitTrackerItem);

        // ---------------- ツール ----------------
        calculatorItem = new MenuItem("🧮 電卓");
        calculatorItem.setOnAction(e -> Navigator.navigate("Calculator"));

        notesItem = new MenuItem("📝 ノート");
        notesItem.setOnAction(e -> Navigator.navigate("Notebook"));

        taskManagerItem = new MenuItem("🧠 タスク");
        taskManagerItem.setOnAction(e -> Navigator.navigate("TaskManager"));

        qrGeneratorItem = new MenuItem("🔳 QR ジェネレーター");
        qrGeneratorItem.setOnAction(e -> Navigator.navigate("QRGeneratorPage"));

        toolsMenu = createMenu("🧰", calculatorItem, notesItem, taskManagerItem, qrGeneratorItem);

        // ---------------- システム ----------------
        themeSwitcherItem = new MenuItem("🌗 テーマ切替");
        themeSwitcherItem.setOnAction(e -> {
            Platform.runLater(ThemeManager::toggleTheme);
            NotificationsManager.showNotification("テーマ", "切り替えました", NotificationsManager.NotificationType.INFO);
        });

        refreshAppItem = new MenuItem("🔄 更新");
        refreshAppItem.setOnAction(e -> {
            Platform.runLater(() -> {
                AppRefresher.refreshApp();
                NotificationsManager.showNotification("更新", "アプリが更新されました", NotificationsManager.NotificationType.SUCCESS);
            });
        });

        exitItem = new MenuItem("🚪 終了");
        exitItem.setOnAction(e -> Platform.runLater(AppRefresher::confirmExit));





        systemMenu = createMenu("⚙️", themeSwitcherItem, refreshAppItem, exitItem);
        workEntryItem = new MenuItem();
        workEntryItem.setText("出働");
        MenuButton workEntry = createMenu("出働", workEntryItem);
        workEntryItem.setOnAction(e->Navigator.navigate("TimeManagementView"));

        // ---------------- ヘルプ ----------------
        aboutItem = new MenuItem("ℹ️ 情報");
        aboutItem.setOnAction(e -> Navigator.navigate("AboutPage"));

        docsItem = new MenuItem("📘 ドキュメント");
        docsItem.setOnAction(e -> Navigator.navigate("DocumentationPage"));

        contactSupportItem = new MenuItem("🆘 サポート");
        contactSupportItem.setOnAction(e -> Navigator.navigate("ContactSupportPage"));

        helpMenu = createMenu("❓", aboutItem, docsItem, contactSupportItem);

        // ---------------- プロフィール ----------------
        profileItem = new MenuItem("👤 プロフィール");
        profileItem.setOnAction(e -> Navigator.navigate("ProfilePage"));

        settingsItem = new MenuItem("⚙️ 設定");
        settingsItem.setOnAction(e -> Navigator.navigate("Settings"));

        logoutItem = new MenuItem("🚫 ログアウト");
        logoutItem.setOnAction(e -> {
            if (!SecurityAuth.isAuthenticated() || SecurityAuth.getCurrentUser() == null) {
                NotificationsManager.showNotification("エラー", "まずログインしてください", NotificationsManager.NotificationType.WARNING);
                return;
            }
            Navigator.resetToLogin();
        });

        profileMenu = createMenu("🧑‍💻", profileItem, settingsItem, logoutItem);

        // --- レイアウト ---
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(
                dashboardMenu,
                analyticsMenu,
                toolsMenu,
                systemMenu,
                workEntry,
                spacer,
                helpMenu,
                profileMenu
        );
    }

    private MenuButton createMenu(String title, MenuItem... items) {
        MenuButton menu = new MenuButton(title);
        menu.getItems().addAll(items);
        menu.getStyleClass().add("side-nav-btn");
        menu.setMaxWidth(Double.MAX_VALUE);
        menu.setStyle("""
                -fx-font-size: 11px;
                -fx-padding: 2 4;
                -fx-background-radius: 3;
                -fx-background-color: transparent;
                -fx-text-fill: #ccc;
                -fx-alignment: center;
                """);
        return menu;
    }

    // 将来のノートモジュール用
    private String createNote() {
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        return textArea.getText();
    }
}

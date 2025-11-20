package bikram.views.page;

import bikram.db.*;
import bikram.security.SecurityAuth;
import bikram.views.ui.NotificationsManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;
import java.util.Locale;


import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class TechuraDashboard extends BorderPane {

    private final NotificationsManager notificationsManager = new NotificationsManager();
    private final ProductRepository productDB = new ProductDB();
    private final UserRepository userDB = new UserDB();
    private final Salesrepository salesDB = new SalesDB();

    private Label revenueValue, usersValue, salesValue, stockValue;
    private LineChart<String, Number> revenueChart;
    private BarChart<String, Number> userChart;
    private TableView<Activity> recentTable;

    private final String currentUser = SecurityAuth.getCurrentUser() != null
            ? SecurityAuth.getCurrentUser().getFullName()
            : "ゲスト";

    public TechuraDashboard() {
        setPrefSize(1400, 900);
        setBackground(createGlassBackground());
        createScrollPane();
        loadDashboardAsync();
    }

    // 🌌 背景 + ガラスオーバーレイ
    private Background createGlassBackground() {
        URL imgUrl = getClass().getResource("/images/dashboard.jpg");
        if (imgUrl == null) {
            System.err.println("⚠️ 背景画像が見つかりません！パスを確認してください: /images/dashboard.jpg");
            return new Background(new BackgroundFill(Color.rgb(20, 20, 30), CornerRadii.EMPTY, Insets.EMPTY));
        }

        Image bg = new Image(imgUrl.toExternalForm(), 1920, 1080, true, true);
        ImageView imgView = new ImageView(bg);
        GaussianBlur blur = new GaussianBlur(5);
        imgView.setEffect(blur);

        final WritableImage[] blurredImage = new WritableImage[1];

        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                blurredImage[0] = imgView.snapshot(new SnapshotParameters(), null);
                latch.countDown();
            });
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BackgroundImage bgImg = new BackgroundImage(
                blurredImage[0],
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(1.0, 1.0, true, true, false, false)
        );

        BackgroundFill overlay = new BackgroundFill(
                Color.rgb(10, 10, 20, 0.4),
                CornerRadii.EMPTY,
                Insets.EMPTY
        );

        return new Background(Arrays.asList(overlay), Arrays.asList(bgImg));
    }

    // 🌐 ScrollPane レイアウト
    private void createScrollPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox container = new VBox(50);
        container.setPadding(new Insets(35, 50, 80, 50));

        container.getChildren().addAll(
                createHeader(),
                createGreeting(),
                createStatsGrid(),
                createCharts(),
                createRecentActivity(),
                createFooter()
        );
        scroll.setContent(container);
        setCenter(scroll);
    }

    // 🌈 ヘッダー
    private HBox createHeader() {
        HBox top = new HBox(20);
        top.setAlignment(Pos.CENTER_LEFT);

        ImageView logo = new ImageView(new Image(
                getClass().getResource("/images/logo.png").toExternalForm()
        ));
        logo.setFitWidth(60);
        logo.setFitHeight(60);
        logo.setEffect(new DropShadow(25, Color.web("#00ffff")));

        Label title = new Label("テクラ ダッシュボード");
        title.setFont(Font.font("Orbitron", FontWeight.EXTRA_BOLD, 32));
        title.setTextFill(Color.web("#00ffff"));
        title.setEffect(new DropShadow(30, Color.web("#00ffff")));

        Timeline glow = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(title.textFillProperty(), Color.web("#00ffff"))),
                new KeyFrame(Duration.seconds(2), new KeyValue(title.textFillProperty(), Color.web("#ff00ff")))
        );
        glow.setAutoReverse(true);
        glow.setCycleCount(Animation.INDEFINITE);
        glow.play();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label date = new Label(LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE", Locale.JAPAN)
        ));

        date.setTextFill(Color.LIGHTGRAY);
        Label time = new Label();
        time.setTextFill(Color.CYAN);
        time.setFont(Font.font("Consolas", FontWeight.BOLD, 15));

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> time.setText(java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        top.getChildren().addAll(logo, title, spacer, date, time);
        fadeIn(top);
        return top;
    }

    private HBox createGreeting() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        Label greet = new Label("👋 お帰りなさい、" + currentUser + " さん");
        greet.setFont(Font.font("Poppins", FontWeight.SEMI_BOLD, 22));
        greet.setTextFill(Color.web("#7CFC00"));
        greet.setEffect(new DropShadow(10, Color.web("#00ffcc")));
        fadeIn(greet);
        box.getChildren().add(greet);
        return box;
    }

    // ⚙️ 統計カード
    private GridPane createStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);

        revenueValue = createStatCard("💰 総収益", "$0", "#00ffff");
        usersValue = createStatCard("👤 ユーザー総数", "0", "#ff00ff");
        salesValue = createStatCard("🛒 総販売数", "0", "#00ff66");
        stockValue = createStatCard("📦 総製品数", "0", "#ffaa00");

        grid.add(revenueValue, 0, 0);
        grid.add(usersValue, 1, 0);
        grid.add(salesValue, 0, 1);
        grid.add(stockValue, 1, 1);

        return grid;
    }

    private Label createStatCard(String title, String value, String glowColor) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(300);
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.1);
            -fx-background-radius: 18;
            -fx-border-color: rgba(255,255,255,0.3);
            -fx-border-radius: 18;
        """);
        card.setEffect(new GaussianBlur(10));

        Label titleLbl = new Label(title);
        titleLbl.setTextFill(Color.LIGHTGRAY);
        titleLbl.setFont(Font.font("Poppins", FontWeight.MEDIUM, 15));

        Label valueLbl = new Label(value);
        valueLbl.setTextFill(Color.web(glowColor));
        valueLbl.setFont(Font.font("Orbitron", FontWeight.BOLD, 28));

        card.setEffect(new DropShadow(25, Color.web(glowColor, 0.6)));
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        card.setOnMouseEntered(e -> { st.setToX(1.07); st.setToY(1.07); st.playFromStart(); });
        card.setOnMouseExited(e -> { st.setToX(1); st.setToY(1); st.playFromStart(); });

        card.getChildren().addAll(titleLbl, valueLbl);
        fadeIn(card);
        return valueLbl;
    }

    // 📊 チャート
    private HBox createCharts() {
        HBox charts = new HBox(30);
        charts.setAlignment(Pos.CENTER);
        charts.setPadding(new Insets(20, 0, 30, 0));

        revenueChart = createLineChart("月次収益");
        userChart = createBarChart("月次新規ユーザー");

        charts.getChildren().addAll(revenueChart, userChart);
        fadeIn(charts);
        return charts;
    }

    private LineChart<String, Number> createLineChart(String title) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setPrefWidth(600);
        chart.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 15;");
        chart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        chart.setEffect(new DropShadow(25, Color.web("#00ffff", 0.4)));
        return chart;
    }

    private BarChart<String, Number> createBarChart(String title) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setPrefWidth(600);
        chart.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 15;");
        chart.setEffect(new DropShadow(25, Color.web("#ff00ff", 0.4)));
        return chart;
    }

    // 📅 最近の活動
    private VBox createRecentActivity() {
        VBox section = new VBox(12);

        Label header = new Label("最近の活動");
        header.setFont(Font.font("Poppins", FontWeight.BOLD, 18));
        header.setTextFill(Color.WHITE);

        recentTable = new TableView<>();
        TableColumn<Activity, String> userCol = new TableColumn<>("ユーザー");
        TableColumn<Activity, String> actionCol = new TableColumn<>("操作");
        TableColumn<Activity, String> timeCol = new TableColumn<>("時刻");

        userCol.setCellValueFactory(c -> c.getValue().userProperty());
        actionCol.setCellValueFactory(c -> c.getValue().actionProperty());
        timeCol.setCellValueFactory(c -> c.getValue().timeProperty());

        recentTable.getColumns().addAll(userCol, actionCol, timeCol);

        recentTable.setStyle("""
        -fx-background-color: rgba(255,255,255,0.1);
        -fx-control-inner-background: rgba(255,255,255,0.1);
        -fx-text-background-color: white;
        -fx-table-cell-border-color: transparent;
        -fx-selection-bar: #00ffff;
        -fx-selection-bar-text: white;
        -fx-table-header-border-color: transparent;
        -fx-font-size: 13px;
    """);

        recentTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            recentTable.lookupAll(".column-header .label").forEach(node -> ((Label) node).setTextFill(Color.BLACK));
        });

        section.getChildren().addAll(header, recentTable);
        fadeIn(section);
        return section;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        Label version = new Label("© 2025 Techura株式会社 | システム安定 🟢");
        version.setTextFill(Color.GRAY);
        footer.getChildren().add(version);
        fadeIn(footer);
        return footer;
    }

    // 🧠 非同期ロード
    private void loadDashboardAsync() {
        Task<Void> task = new Task<>() {
            private double totalRevenue;
            private int totalUsers, totalSales, totalProducts;
            private Map<String, Double> monthlyRevenue;
            private Map<String, Integer> monthlyUsers;
            private List<Activity> activities;

            @Override
            protected Void call() {
                try {
                    totalUsers = DBM.getCount("users");
                    totalProducts = DBM.getCount("products");
                    totalSales = salesDB.countSales();
                    totalRevenue = salesDB.getTotalRevenue();
                    monthlyRevenue = salesDB.getMonthlyRevenue();
                    monthlyUsers = userDB.getMonthlyNewUsers();
                    activities = salesDB.getRecentActivities();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    usersValue.setText(String.valueOf(totalUsers));
                    stockValue.setText(String.valueOf(totalProducts));
                    salesValue.setText(String.valueOf(totalSales));
                    revenueValue.setText("$" + String.format("%,.2f", totalRevenue));
                    updateCharts(monthlyRevenue, monthlyUsers);
                    updateRecent(activities);
                    notificationsManager.showNotification(
                            "ダッシュボード更新",
                            "統計情報が正常に更新されました ✅",
                            NotificationsManager.NotificationType.SUCCESS
                    );
                });
            }
        };
        new Thread(task).start();
    }

    private void updateCharts(Map<String, Double> revenue, Map<String, Integer> users) {
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> usrSeries = new XYChart.Series<>();
        if (revenue != null) revenue.forEach((m, v) -> revSeries.getData().add(new XYChart.Data<>(m, v)));
        if (users != null) users.forEach((m, v) -> usrSeries.getData().add(new XYChart.Data<>(m, v)));
        revenueChart.getData().setAll(revSeries);
        userChart.getData().setAll(usrSeries);
    }

    private void updateRecent(List<Activity> data) {
        if (data != null) recentTable.setItems(FXCollections.observableArrayList(data));
    }

    private void fadeIn(Node n) {
        FadeTransition ft = new FadeTransition(Duration.millis(800), n);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // 🧾 Activity クラス
    public static class Activity {
        private final javafx.beans.property.StringProperty user;
        private final javafx.beans.property.StringProperty action;
        private final javafx.beans.property.StringProperty time;

        public Activity(String u, String a, String t) {
            user = new javafx.beans.property.SimpleStringProperty(u);
            action = new javafx.beans.property.SimpleStringProperty(a);
            time = new javafx.beans.property.SimpleStringProperty(t);
        }
        public javafx.beans.property.StringProperty userProperty() { return user; }
        public javafx.beans.property.StringProperty actionProperty() { return action; }
        public javafx.beans.property.StringProperty timeProperty() { return time; }






        public String getTime() {
            return time.get(); // simply return the stored String
        }

        public String getAction() {
            return action.get();
        }
    }
}

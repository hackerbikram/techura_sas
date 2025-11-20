package bikram.views.page;

import bikram.db.SalesDB;
import bikram.db.Salesrepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SalesReportPage extends VBox {

    private final Salesrepository repo = new SalesDB();

    private Label totalRevenueLabel, totalProfitLabel, totalLossLabel, totalQtyLabel, avgSalesLabel;
    private BarChart<String, Number> monthlyRevenueChart;
    private BarChart<String, Number> monthlyProfitChart;
    private LineChart<String, Number> dailySalesChart;
    private TableView<Activity> recentActivityTable;
    private TableView<TopProduct> topProductTable;

    public SalesReportPage() {
        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: linear-gradient(to bottom right,#f7f9fb,#e3e3e3);");

        getChildren().addAll(
                createHeader(),
                createMetricsGrid(),
                createCharts(),
                createTopProductsSection(),
                createRecentActivitySection(),
                createDateFilterSection()
        );

        loadData();
    }

    // ================= Header ===================
    private Label createHeader() {
        Label title = new Label("📊 売上レポートダッシュボード");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2e2e2e"));
        return title;
    }

    // ================= Metrics ===================
    private GridPane createMetricsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);

        totalRevenueLabel = createStatCard(grid, "💰 総収益", repo.getTotalRevenue(), 0, 0, "#00b894");
        totalProfitLabel = createStatCard(grid, "📈 総利益", repo.getTotalProfit(), 1, 0, "#0984e3");
        totalLossLabel = createStatCard(grid, "📉 総損失", repo.getTotalLoss(), 0, 1, "#d63031");
        totalQtyLabel = createStatCard(grid, "🛒 総販売数量", repo.getTotalQuantitySold(), 1, 1, "#fdcb6e");
        avgSalesLabel = createStatCard(grid, "📊 平均売上/日", repo.getAverageSalesPerDay(), 0, 2, "#6c5ce7");

        return grid;
    }

    private Label createStatCard(GridPane grid, String title, double value, int col, int row, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);");

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Poppins", FontWeight.MEDIUM, 14));

        Label lblValue = new Label(String.format("%,.2f", value));
        lblValue.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
        lblValue.setTextFill(Color.web(color));

        card.getChildren().addAll(lblTitle, lblValue);
        grid.add(card, col, row);
        return lblValue;
    }

    private Label createStatCard(GridPane grid, String title, int value, int col, int row, String color) {
        return createStatCard(grid, title, (double) value, col, row, color);
    }

    // ================= Charts ===================
    private HBox createCharts() {
        HBox chartsBox = new HBox(30);
        chartsBox.setAlignment(Pos.CENTER);

        monthlyRevenueChart = createBarChart("月別売上", "#00b894");
        monthlyProfitChart = createBarChart("月別利益", "#0984e3");
        dailySalesChart = createLineChart("日別売上");

        chartsBox.getChildren().addAll(monthlyRevenueChart, monthlyProfitChart, dailySalesChart);
        return chartsBox;
    }

    private BarChart<String, Number> createBarChart(String title, String barColor) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("月");
        yAxis.setLabel(title);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setPrefWidth(400);
        chart.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 15;");
        chart.setEffect(new DropShadow(15, Color.web(barColor, 0.4)));
        return chart;
    }

    private LineChart<String, Number> createLineChart(String title) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("日");
        yAxis.setLabel(title);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setPrefWidth(600);
        chart.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 15;");
        chart.setEffect(new DropShadow(15, Color.web("#6c5ce7", 0.4)));
        return chart;
    }

    // ================= Top Products ===================
    private VBox createTopProductsSection() {
        VBox section = new VBox(10);
        Label header = new Label("🏆 トップ5売上商品");
        header.setFont(Font.font("Poppins", FontWeight.BOLD, 18));

        topProductTable = new TableView<>();
        TableColumn<TopProduct, String> productCol = new TableColumn<>("商品名");
        TableColumn<TopProduct, Integer> qtyCol = new TableColumn<>("数量");
        TableColumn<TopProduct, Double> revenueCol = new TableColumn<>("売上");

        productCol.setCellValueFactory(c -> c.getValue().productProperty());
        qtyCol.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        revenueCol.setCellValueFactory(c -> c.getValue().revenueProperty().asObject());

        topProductTable.getColumns().addAll(productCol, qtyCol, revenueCol);
        topProductTable.setPrefHeight(200);

        section.getChildren().addAll(header, topProductTable);
        return section;
    }

    // ================= Recent Activity ===================
    private VBox createRecentActivitySection() {
        VBox section = new VBox(10);
        Label header = new Label("📝 最近の販売活動");
        header.setFont(Font.font("Poppins", FontWeight.BOLD, 18));

        recentActivityTable = new TableView<>();
        TableColumn<Activity, String> productCol = new TableColumn<>("商品名");
        TableColumn<Activity, Integer> qtyCol = new TableColumn<>("数量");
        TableColumn<Activity, Double> revenueCol = new TableColumn<>("売上");
        TableColumn<Activity, String> dateCol = new TableColumn<>("日付");

        productCol.setCellValueFactory(c -> c.getValue().productProperty());
        qtyCol.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        revenueCol.setCellValueFactory(c -> c.getValue().revenueProperty().asObject());
        dateCol.setCellValueFactory(c -> c.getValue().dateProperty());

        recentActivityTable.getColumns().addAll(productCol, qtyCol, revenueCol, dateCol);
        recentActivityTable.setPrefHeight(200);

        section.getChildren().addAll(header, recentActivityTable);
        return section;
    }

    // ================= Date Filter ===================
    private HBox createDateFilterSection() {
        HBox box = new HBox(10);
        DatePicker startDate = new DatePicker(LocalDate.now().minusDays(7));
        DatePicker endDate = new DatePicker(LocalDate.now());
        Button filterBtn = new Button("フィルター");
        Label resultLabel = new Label();

        filterBtn.setOnAction(e -> {
            double total = repo.getSalesByDateRange(startDate.getValue(), endDate.getValue());
            resultLabel.setText("選択期間の売上合計: $" + String.format("%,.2f", total));
        });

        box.getChildren().addAll(new Label("開始:"), startDate, new Label("終了:"), endDate, filterBtn, resultLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // ================= Load Data ===================
    private void loadData() {
        totalRevenueLabel.setText("$" + String.format("%,.2f", repo.getTotalRevenue()));
        totalProfitLabel.setText("$" + String.format("%,.2f", repo.getTotalProfit()));
        totalLossLabel.setText("$" + String.format("%,.2f", repo.getTotalLoss()));
        totalQtyLabel.setText(String.valueOf(repo.getTotalQuantitySold()));
        avgSalesLabel.setText("$" + String.format("%,.2f", repo.getAverageSalesPerDay()));

        // Monthly Revenue
        Map<String, Double> revenueMap = repo.getMonthlyRevenue();
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revenueMap.forEach((m, v) -> revSeries.getData().add(new XYChart.Data<>(m, v)));
        monthlyRevenueChart.getData().setAll(revSeries);

        // Monthly Profit
        Map<String, Double> profitMap = repo.getMonthlyProfit();
        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        profitMap.forEach((m, v) -> profitSeries.getData().add(new XYChart.Data<>(m, v)));
        monthlyProfitChart.getData().setAll(profitSeries);

        // Daily Sales (last 30 days)
        Map<String, Double> dailySales = repo.getDailySales(); // Implement in repo
        XYChart.Series<String, Number> dailySeries = new XYChart.Series<>();
        dailySales.forEach((d, v) -> dailySeries.getData().add(new XYChart.Data<>(d, v)));
        dailySalesChart.getData().setAll(dailySeries);

        // Top Products
        List<String> topProducts = repo.getTop5Products();
        ObservableList<TopProduct> topList = FXCollections.observableArrayList();
        for (String p : topProducts) {
            int qty = repo.countSalesByProduct(p);
            double rev = repo.getProfitByProduct(p);
            topList.add(new TopProduct(p, qty, rev));
        }
        topProductTable.setItems(topList);

        // Recent Activities
        List<TechuraDashboard.Activity> activities = repo.getRecentActivities();
        ObservableList<Activity> recentList = FXCollections.observableArrayList();
        for (TechuraDashboard.Activity a : activities) {
            recentList.add(new Activity(
                    a.getAction(),
                    repo.countSalesByProduct(a.getAction()),
                    repo.getProfitByProduct(a.getAction()),
                    a.getTime() // use getTime() instead of getDate
            ));
        }
        recentActivityTable.setItems(recentList);
    }

    // ================= Activity class ===================
    public static class Activity {
        private final javafx.beans.property.StringProperty product;
        private final javafx.beans.property.IntegerProperty quantity;
        private final javafx.beans.property.DoubleProperty revenue;
        private final javafx.beans.property.StringProperty date;

        public Activity(String product, int quantity, double revenue, String date) {
            this.product = new javafx.beans.property.SimpleStringProperty(product);
            this.quantity = new javafx.beans.property.SimpleIntegerProperty(quantity);
            this.revenue = new javafx.beans.property.SimpleDoubleProperty(revenue);
            this.date = new javafx.beans.property.SimpleStringProperty(date);
        }

        public javafx.beans.property.StringProperty productProperty() { return product; }
        public javafx.beans.property.IntegerProperty quantityProperty() { return quantity; }
        public javafx.beans.property.DoubleProperty revenueProperty() { return revenue; }
        public javafx.beans.property.StringProperty dateProperty() { return date; }
    }

    // ================= Top Product class ===================
    public static class TopProduct {
        private final javafx.beans.property.StringProperty product;
        private final javafx.beans.property.IntegerProperty quantity;
        private final javafx.beans.property.DoubleProperty revenue;

        public TopProduct(String product, int quantity, double revenue) {
            this.product = new javafx.beans.property.SimpleStringProperty(product);
            this.quantity = new javafx.beans.property.SimpleIntegerProperty(quantity);
            this.revenue = new javafx.beans.property.SimpleDoubleProperty(revenue);
        }

        public javafx.beans.property.StringProperty productProperty() { return product; }
        public javafx.beans.property.IntegerProperty quantityProperty() { return quantity; }
        public javafx.beans.property.DoubleProperty revenueProperty() { return revenue; }
    }
}

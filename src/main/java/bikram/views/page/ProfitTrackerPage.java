package bikram.views.page;

import bikram.db.ProductDB;
import bikram.db.ProductRepository;
import bikram.db.SalesDB;
import bikram.db.Salesrepository;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.List;
import java.util.Map;

/**
 * 📊 Profit Tracker Page
 * 売上・コスト・利益を追跡するダッシュボード
 */
public class ProfitTrackerPage extends StackPane {

    private final Salesrepository salesRepo = new SalesDB();
    private final ProductRepository productRepo = new ProductDB();

    public ProfitTrackerPage() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(40);
        content.setPadding(new Insets(40, 50, 100, 50));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: linear-gradient(to bottom right, #0a0f1a, #111b2b);");

        content.getChildren().addAll(
                createHeader(),
                createSummaryCards(),
                createMonthlyProfitChart(),
                createTopProductsChart()
        );

        scrollPane.setContent(content);
        getChildren().add(scrollPane);

        playIntroAnimation();
    }

    // ---------------- HEADER ----------------
    private Node createHeader() {
        Label title = new Label("💹 利益トラッカー");
        title.setFont(Font.font("Arial Rounded MT Bold", 42));
        title.setTextFill(Color.web("#00D9FF"));
        title.setEffect(glow());

        Label subtitle = new Label("売上・コスト・利益の分析と月次推移");
        subtitle.setFont(Font.font("Segoe UI", 20));
        subtitle.setTextFill(Color.LIGHTGRAY);

        VBox box = new VBox(10, title, subtitle);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    // ---------------- SUMMARY CARDS ----------------
    private Node createSummaryCards() {
        double revenue = salesRepo.getTotalRevenue();
        double cost = salesRepo.getTotalCost();
        double profit = salesRepo.getTotalProfit();

        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
                statCard("💰 総売上", "¥" + format(revenue), "#00FFD1"),
                statCard("💸 総コスト", "¥" + format(cost), "#FF5577"),
                statCard("📈 総利益", "¥" + format(profit), "#00FF88")
        );

        return row;
    }

    private VBox statCard(String title, String value, String color) {
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.LIGHTGRAY);
        titleLabel.setFont(Font.font(16));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(color));
        valueLabel.setFont(Font.font("Arial Black", 26));

        VBox box = new VBox(10, titleLabel, valueLabel);
        box.setPadding(new Insets(15));
        box.setPrefSize(200, 110);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 18;");
        box.setEffect(glow());
        return box;
    }

    // ---------------- MONTHLY PROFIT CHART ----------------
    private Node createMonthlyProfitChart() {
        Map<String, Double> monthlyProfit = salesRepo.getMonthlyProfit();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("月");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("利益 (¥)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("📊 月次利益推移");
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> entry : monthlyProfit.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().add(series);
        return styledBox("月次利益チャート", chart);
    }

    // ---------------- TOP PRODUCTS BY PROFIT ----------------
    private Node createTopProductsChart() {
        List<String> topProducts = salesRepo.getTop5Products(); // 利益トップ5
        PieChart chart = new PieChart();

        for (String product : topProducts) {
            int quantity = salesRepo.countSalesByProduct(product);
            double profit = salesRepo.getProfitByProduct(product); // 必要に応じてSalesDBに実装
            chart.getData().add(new PieChart.Data(product + " (¥" + format(profit) + ")", profit));
        }

        chart.setTitle("🏆 利益トップ5商品");
        return styledBox("利益トップ商品", chart);
    }

    // ---------------- UTILS ----------------
    private VBox styledBox(String title, Node content) {
        Label header = new Label(title);
        header.setTextFill(Color.LIGHTGRAY);
        header.setFont(Font.font("Arial Rounded MT Bold", 20));

        VBox box = new VBox(10, header, content);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setMaxWidth(900);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 20;");
        box.setEffect(glow());
        return box;
    }

    private DropShadow glow() {
        DropShadow glow = new DropShadow();
        glow.setRadius(20);
        glow.setColor(Color.web("#00D9FF", 0.6));
        return glow;
    }

    private String format(double value) {
        return String.format("%,.0f", value);
    }

    private void playIntroAnimation() {
        FadeTransition ft = new FadeTransition(Duration.seconds(2), this);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}

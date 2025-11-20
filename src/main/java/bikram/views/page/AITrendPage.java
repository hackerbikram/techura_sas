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
 * 🤖 Techura AI トレンド分析ページ (実データ + 日本語対応)
 * 売上データ・利益・商品分析・AI予測を含むプロフェッショナル版。
 */
public class AITrendPage extends StackPane {

    private final Salesrepository salesRepo = new SalesDB();
    private final ProductRepository productRepo = new ProductDB();

    public AITrendPage() {

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.getStyleClass().add("scroll-pane-transparent");

        VBox content = new VBox(40);
        content.setPadding(new Insets(40, 50, 100, 50));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: linear-gradient(to bottom right, #0a0f1a, #111b2b);");

        content.getChildren().addAll(
                createHeader(),
                createSummaryCards(),
                createRevenueProfitChart(),
                createTopProductsChart(),
                createMonthlyTrendChart(),
                createCategoryChart(),
                createAIInsight()
        );

        scrollPane.setContent(content);
        getChildren().add(scrollPane);
        playIntroAnimation();
    }

    // ------------------ HEADER ------------------
    private Node createHeader() {
        Label title = new Label("🤖 AIビジネストレンド分析");
        title.setFont(Font.font("Arial Rounded MT Bold", 42));
        title.setTextFill(Color.web("#00D9FF"));
        title.setEffect(glow());

        Label subtitle = new Label("データ駆動型インサイトと将来予測 - Techura AI");
        subtitle.setFont(Font.font("Segoe UI", 20));
        subtitle.setTextFill(Color.LIGHTGRAY);

        VBox box = new VBox(10, title, subtitle);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    // ------------------ SUMMARY CARDS ------------------
    private Node createSummaryCards() {
        double totalRevenue = salesRepo.getTotalRevenue();
        double totalProfit = salesRepo.getTotalProfit();
        double totalLoss = salesRepo.getTotalLoss();
        int totalSales = salesRepo.countSales();
        double avgPrice = productRepo.getAveragePrice();

        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
                statCard("💰 総売上", "¥" + format(totalRevenue), "#00FFD1"),
                statCard("📈 利益", "¥" + format(totalProfit), "#00FF88"),
                statCard("📉 損失", "¥" + format(totalLoss), "#FF5577"),
                statCard("🛒 販売件数", totalSales + " 件", "#00D9FF"),
                statCard("⚖️ 平均価格", "¥" + format(avgPrice), "#FFD700")
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
        box.setPrefSize(180, 110);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 18;");
        box.setEffect(glow());
        return box;
    }

    // ------------------ REVENUE & PROFIT ------------------
    private Node createRevenueProfitChart() {
        Map<String, Double> monthlyRevenue = salesRepo.getMonthlyRevenue();
        Map<String, Double> monthlyProfit = salesRepo.getMonthlyProfit();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("月");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("金額 (¥)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("📅 月別 売上・利益の推移");
        chart.setLegendVisible(true);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("売上");

        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        profitSeries.setName("利益");

        for (String month : monthlyRevenue.keySet()) {
            revenueSeries.getData().add(new XYChart.Data<>(month, monthlyRevenue.get(month)));
            profitSeries.getData().add(new XYChart.Data<>(month, monthlyProfit.getOrDefault(month, 0.0)));
        }

        chart.getData().addAll(revenueSeries, profitSeries);
        return styledBox("💹 売上と利益の月別チャート", chart);
    }

    // ------------------ TOP PRODUCTS ------------------
    private Node createTopProductsChart() {
        List<String> top5 = salesRepo.getTop5Products();
        PieChart chart = new PieChart();

        for (String name : top5) {
            int count = salesRepo.countSalesByProduct(name);
            chart.getData().add(new PieChart.Data(name, count));
        }

        chart.setTitle("🏆 売上上位5商品");
        return styledBox("人気商品ランキング", chart);
    }

    // ------------------ MONTHLY TREND ------------------
    private Node createMonthlyTrendChart() {
        Map<String, Integer> trendMap = salesRepo.getMonthlySalesTrend();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("月");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("販売数");

        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("📈 月別販売推移");

        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName("販売数");

        for (String month : trendMap.keySet()) {
            trendSeries.getData().add(new XYChart.Data<>(month, trendMap.get(month)));
        }

        chart.getData().add(trendSeries);
        return styledBox("📊 売上トレンド分析", chart);
    }

    // ------------------ CATEGORY SALES ------------------
    private Node createCategoryChart() {
        Map<String, Integer> categorySales = productRepo.getCategorySales();

        PieChart chart = new PieChart();
        categorySales.forEach((category, value) ->
                chart.getData().add(new PieChart.Data(category, value))
        );
        chart.setTitle("🧩 カテゴリ別販売割合");
        return styledBox("カテゴリ分析", chart);
    }

    // ------------------ AI INSIGHT ------------------
    private Node createAIInsight() {
        double profit = salesRepo.getTotalProfit();
        double loss = salesRepo.getTotalLoss();
        double revenue = salesRepo.getTotalRevenue();
        String topProduct = salesRepo.getMostSoldProduct();

        String trend = profit > loss ? "📈 利益傾向が続いています！" : "⚠️ 損失が発生しています。";
        String advice = profit > loss
                ? "「" + topProduct + "」の在庫を確保し、販促を強化しましょう。"
                : "損失要因を分析し、価格や在庫バランスを調整してください。";

        Label label = new Label(
                "🤖 AIインサイト:\n" +
                        trend + "\n" +
                        "総売上: ¥" + format(revenue) + "\n" +
                        "主力商品: " + topProduct + "\n" +
                        advice
        );

        label.setFont(Font.font("Segoe UI Semibold", 18));
        label.setTextFill(Color.web("#00D9FF"));
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(700);
        label.setEffect(glow());
        return styledBox("🧠 AIによる経営分析とアドバイス", label);
    }

    // ------------------ UTIL ------------------
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

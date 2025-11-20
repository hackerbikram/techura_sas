package bikram.views.page;

import bikram.db.EmployeeTimeDB;
import bikram.db.EmployeeTimeRepository;
import bikram.db.UserDB;
import bikram.db.UserRepository;
import bikram.model.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.chart.*;
import javafx.scene.control.*;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.time.Year;


public class EmploymentPaySlip extends BorderPane {
    private UserRepository userRepo = new UserDB();
    private EmployeeTimeRepository timeRepo = new EmployeeTimeDB();
    private ListView<User> userListView;
    private ObservableList<User> userList;

    private String selectedUserId = "";
    private double totalMonthlyHours;

    private VBox userCardBox;
    private BarChart<String, Number> barChart;
    private Label nameLabel;
    private Label emailLabel;
    private Label userIdLabel;
    private Label totalHoursLabel;
    private Label totalSalaryLabel;

    private final int YEAR = Year.now().getValue();
    private final double SALARY_RATE = 1200; // hourly salary rate


    public EmploymentPaySlip() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: linear-gradient(to bottom,#141414,#202020);");

        // -----------------------
        // TITLE
        // -----------------------
        Label title = new Label("給与確認ページ");
        title.setFont(Font.font(28));
        title.setTextFill(Color.WHITE);

        VBox topBox = new VBox(title);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));
        setTop(topBox);

        // ------------------------------------------------------
        // LEFT SIDE → USER LIST
        // ------------------------------------------------------
        userList = FXCollections.observableArrayList(userRepo.getAllUsers());
        userListView = new ListView<>(userList);

        userListView.setPrefWidth(260);
        userListView.setStyle("""
                -fx-background-color:#1e1e1e;
                -fx-text-fill:white;
                -fx-control-inner-background:#1e1e1e;
                -fx-font-size:15px;
        """);

        userListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                    return;
                }

                setText(user.getId() + " | " + user.getFullName());
                setTextFill(Color.WHITE);
            }
        });

        userListView.setOnMouseClicked(e -> {
            User selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                loadUserSalary(selected.getId());
            }
        });

        VBox leftPane = new VBox(new Label("社員一覧"), userListView);
        leftPane.setSpacing(10);
        leftPane.setPadding(new Insets(15));
        leftPane.setStyle("""
                -fx-background-color:#00000055;
                -fx-background-radius:10;
                -fx-border-color:#ffffff33;
                -fx-border-radius:10;
                -fx-border-width:1;
        """);
        setLeft(leftPane);

        // -----------------------------------------------------
        // CENTER → USER ID INPUT
        // -----------------------------------------------------
        Label inputLabel = new Label("社員ID検索");
        inputLabel.setTextFill(Color.WHITE);

        TextField userIdField = new TextField();
        userIdField.setPromptText("社員IDを入力…");
        userIdField.setPrefWidth(200);

        Button searchBtn = new Button("検索");
        searchBtn.setStyle("-fx-background-color:#2196F3; -fx-text-fill:white;");
        searchBtn.setOnAction(e -> loadUserSalary(userIdField.getText()));

        VBox centerBox = new VBox(10, inputLabel, userIdField, searchBtn);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(30));
        setCenter(centerBox);

        // ----------------------------------------------------
        // RIGHT → USER DETAILS + SALARY CARD
        // ----------------------------------------------------
        userCardBox = new VBox(15);
        userCardBox.setAlignment(Pos.TOP_CENTER);
        userCardBox.setPadding(new Insets(25));
        userCardBox.setPrefWidth(320);

        userCardBox.setStyle("""
                -fx-background-color:#ffffff15;
                -fx-background-radius:15;
                -fx-border-radius:15;
                -fx-border-color:#ffffff44;
                -fx-border-width:1;
        """);

        userIdLabel = new Label("ID: -");
        nameLabel = new Label("名前: -");
        emailLabel = new Label("Email: -");

        nameLabel.setTextFill(Color.WHITE);
        emailLabel.setTextFill(Color.WHITE);
        userIdLabel.setTextFill(Color.WHITE);

        nameLabel.setFont(Font.font(16));
        emailLabel.setFont(Font.font(16));
        userIdLabel.setFont(Font.font(16));

        totalHoursLabel = new Label("総働いた時間: 0 h");
        totalHoursLabel.setFont(Font.font(18));
        totalHoursLabel.setTextFill(Color.WHITE);

        totalSalaryLabel = new Label("給与: ¥0");
        totalSalaryLabel.setFont(Font.font(20));
        totalSalaryLabel.setTextFill(Color.LIGHTGREEN);

        Button payButton = new Button("給与支払い");
        payButton.setStyle("-fx-background-color:#43a047; -fx-text-fill:white;");
        payButton.setOnAction(e -> handlePayment());

        userCardBox.getChildren().addAll(
                userIdLabel, nameLabel, emailLabel,
                new Separator(),
                totalHoursLabel, totalSalaryLabel,
                payButton
        );

        setRight(userCardBox);

        // -----------------------------------------------------
        // BOTTOM → BAR CHART
        // -----------------------------------------------------
        barChart = createChart();
        setBottom(barChart);
    }


    // ==========================================================================
    // LOAD USER, SALARY, CHART
    // ==========================================================================
    private void loadUserSalary(String userId) {
        if (userId == null || userId.isEmpty()) return;

        selectedUserId = userId;

        User user = userRepo.getUserById(userId);
        if (user == null) {
            userIdLabel.setText("ID: 不明");
            nameLabel.setText("名前: 不明");
            emailLabel.setText("Email: 不明");
            return;
        }

        // Update card
        userIdLabel.setText("ID: " + user.getId());
        nameLabel.setText("名前: " + user.getFullName());
        emailLabel.setText("Email: " + user.getEmail());

        // Monthly hours for chart
        ObservableList<XYChart.Data<String, Number>> monthData = FXCollections.observableArrayList();
        totalMonthlyHours = 0;

        for (int m = 1; m <= 12; m++) {
            double hours = timeRepo.getMonthlyTime(userId, YEAR, m);
            monthData.add(new XYChart.Data<>(String.valueOf(m), hours));
            totalMonthlyHours += hours;
        }

        // Update chart
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Hours");
        series.setData(monthData);
        barChart.getData().add(series);

        // Update salary
        totalHoursLabel.setText("総働いた時間: " + totalMonthlyHours + " h");
        totalSalaryLabel.setText("給与: ¥" + (int) (totalMonthlyHours * SALARY_RATE));
    }


    // ==========================================================================
    // CHART CREATION
    // ==========================================================================
    private BarChart<String, Number> createChart() {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("月");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("時間");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("月別働いた時間");
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color:transparent;");

        chart.setPrefHeight(350);
        chart.setPadding(new Insets(20));

        return chart;
    }


    // ==========================================================================
    // PAY BUTTON
    // ==========================================================================
    private void handlePayment() {
        if (selectedUserId == null || selectedUserId.isEmpty()) return;

        totalSalaryLabel.setText("支払い完了 ✔");
        totalSalaryLabel.setTextFill(Color.YELLOW);
    }
}

package bikram.views.ui;

import bikram.db.EmployeeTimeDB;
import bikram.db.EmployeeTimeRepository;
import bikram.db.UserDB;
import bikram.db.UserRepository;
import bikram.model.EmployeTime;
import bikram.model.User;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimeManagementView extends VBox {

    private EmployeeTimeRepository timeDB;
    private UserRepository urepo = new UserDB();

    private TableView<User> userTable = new TableView<>();
    private Label clockLabel = new Label();
    private Label statusLabel = new Label("ユーザーの選択はありません");

    private TextField searchField = new TextField();

    private Button clockInBtn = new Button("出働");
    private Button breakBtn = new Button("休憩");
    private Button resumeBtn = new Button("続き");
    private Button clockOutBtn = new Button("退勤");

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ObservableList<User> allUsers;

    public TimeManagementView() {
        // Load users and DB
        allUsers = FXCollections.observableArrayList(urepo.getAllUsers());
        timeDB.createTable();

        BorderPane root = new BorderPane();
        root.setTop(createTopBar());
        root.setCenter(createUserTable());
        root.setBottom(createActionButtons());

        BorderPane.setMargin(root.getCenter(), new Insets(15));
        BorderPane.setMargin(root.getBottom(), new Insets(20));

        startClock();

        this.getChildren().add(root);
        this.setStyle("-fx-background-color: #f0f0f0;");
    }

    private void startClock() {
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> clockLabel.setText(LocalDateTime.now().format(F))));
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
    }

    private HBox createTopBar() {
        clockLabel.setFont(Font.font("Consolas", 28));
        clockLabel.setTextFill(Color.DARKGREEN);
        statusLabel.setFont(Font.font("Arial", 20));
        statusLabel.setTextFill(Color.DARKBLUE);

        HBox topBar = new HBox(20, statusLabel, new Pane(), clockLabel);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #c0c0c0; -fx-border-radius: 8; -fx-background-radius: 8;");
        return topBar;
    }

    private VBox createUserTable() {
        searchField.setPromptText("名前かメルidを入力して探す...");
        searchField.setFont(Font.font("Arial", 14));
        searchField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 5;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers(newVal));

        TableColumn<User, String> idCol = new TableColumn<>("ユーザーID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("Id"));

        TableColumn<User, String> nameCol = new TableColumn<>("フルネーム");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        userTable.getColumns().addAll(idCol, nameCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        userTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                row.setStyle(isNowHovered && !row.isEmpty() ? "-fx-background-color: #d0e7ff;" : "");
            });
            return row;
        });

        userTable.setItems(FXCollections.observableArrayList(allUsers));

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selectedUser) -> {
            if (selectedUser != null) {
                statusLabel.setText("Selected: " + selectedUser.getFullName());
            }
        });

        VBox tableBox = new VBox(10, searchField, userTable);
        tableBox.setPadding(new Insets(10));
        return tableBox;
    }

    private void filterUsers(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            userTable.setItems(FXCollections.observableArrayList(allUsers));
            return;
        }

        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User u : allUsers) {
            if (u.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                    u.getId().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(u);
            }
        }

        userTable.setItems(filtered);
    }

    private HBox createActionButtons() {
        styleButton(clockInBtn, Color.LIMEGREEN);
        styleButton(breakBtn, Color.ORANGE);
        styleButton(resumeBtn, Color.CYAN);
        styleButton(clockOutBtn, Color.RED);

        clockInBtn.setOnAction(e -> handleClockIn());
        breakBtn.setOnAction(e -> handleBreak());
        resumeBtn.setOnAction(e -> handleResume());
        clockOutBtn.setOnAction(e -> handleClockOut());

        HBox hb = new HBox(20, clockInBtn, breakBtn, resumeBtn, clockOutBtn);
        hb.setAlignment(Pos.CENTER);
        return hb;
    }

    private void styleButton(Button btn, Color glow) {
        btn.setPrefWidth(150);
        btn.setPrefHeight(45);
        btn.setFont(Font.font("Arial", 16));
        btn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 10;");

        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() +
                "-fx-effect: dropshadow(gaussian, " + toRgb(glow) + ", 20, 0.5, 0, 0);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 10;"));
    }

    private String toRgb(Color c) {
        return "rgba(" + (int) (c.getRed() * 255) + "," +
                (int) (c.getGreen() * 255) + "," +
                (int) (c.getBlue() * 255) + ",1)";
    }

    // --------------------------
    // HANDLERS
    // --------------------------
    private User selectedUser() {
        return userTable.getSelectionModel().getSelectedItem();
    }

    private void handleClockIn() {
        User u = selectedUser();
        if (u == null) return;

        String now = LocalDateTime.now().format(F);
        EmployeTime et = new EmployeTime(u.getId(), now);

        timeDB.saveEmployeTime(et);  // Save to DB
        statusLabel.setText("🟢 出働: " + u.getFullName());
    }

    private void handleBreak() {
        statusLabel.setText("🟡 休憩時間始めました");
    }

    private void handleResume() {
        statusLabel.setText("🔵 休憩時間 → 出働中");
    }

    private void handleClockOut() {
        User u = selectedUser();
        if (u == null) return;

        EmployeTime last = EmployeTime.getLastEntry(u.getId(), timeDB, urepo);
        if (last == null) {
            statusLabel.setText("⚠ アクティブな作業セッションはありません");
            return;
        }

        last.setExitNow(); // Updates DB automatically
        statusLabel.setText("🔴 退勤: " + u.getFullName());
    }
}

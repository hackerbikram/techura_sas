package bikram.views.page;

import bikram.db.UserDB;
import bikram.db.UserRepository;
import bikram.model.Role;
import bikram.model.User;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UserDashboardPage extends BorderPane {

    private final UserRepository userDB = new UserDB();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private TableView<User> table;
    private TextField searchField;

    public UserDashboardPage() {
        setPadding(new Insets(20));
        buildUI();
        loadUsers();
    }

    // ----------------------------------------------------
    // 🌈 Build Dashboard UI
    // ----------------------------------------------------
    private void buildUI() {
        // Title
        Label title = new Label("👥 ユーザーダッシュボード");
        title.setFont(Font.font("Poppins Bold", FontWeight.BOLD, 34));
        title.setTextFill(Color.web("#00eaff"));
        title.setEffect(new DropShadow(25, Color.web("#00ffff")));

        // Search
        searchField = new TextField();
        searchField.setPromptText("名前またはメールで検索...");
        searchField.setFont(Font.font("Poppins", 14));
        searchField.setPrefWidth(280);
        searchField.setStyle("""
            -fx-background-color: rgba(40,40,60,0.8);
            -fx-border-color: #00bfff;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-text-fill: white;
            -fx-prompt-text-fill: rgba(255,255,255,0.5);
        """);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers(newVal));

        // Refresh Button
        Button refreshBtn = new Button("🔄 更新");
        refreshBtn.setFont(Font.font("Poppins SemiBold", 14));
        refreshBtn.setTextFill(Color.WHITE);
        refreshBtn.setStyle("""
            -fx-background-color: linear-gradient(to right, #0072ff, #00c6ff);
            -fx-background-radius: 12;
            -fx-padding: 8 22;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,255,255,0.4), 10, 0.4, 0, 0);
        """);
        refreshBtn.setOnAction(e -> loadUsers());

        HBox topBar = new HBox(20, title, searchField, refreshBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        // Table
        table = new TableView<>();
        table.setEditable(false);
        table.setItems(users);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(600);
        table.setStyle("""
            -fx-background-color: rgba(20,25,35,0.85);
            -fx-border-color: rgba(0,255,255,0.3);
            -fx-border-radius: 16;
            -fx-background-radius: 16;
        """);

        // Columns
        TableColumn<User, String> idCol = makeCol("ID", "id", 80);
        TableColumn<User, String> fNameCol = makeCol("名", "firstName", 120);
        TableColumn<User, String> lNameCol = makeCol("姓", "lastName", 120);
        TableColumn<User, String> emailCol = makeCol("メール", "email", 180);
        TableColumn<User, String> phoneCol = makeCol("電話番号", "phoneNumber", 120);
        TableColumn<User, Role> roleCol = makeCol("役割", "role", 100);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        TableColumn<User, Date> joinedCol = new TableColumn<>("参加日");
        joinedCol.setCellValueFactory(new PropertyValueFactory<>("joined_date"));
        joinedCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) setText(null);
                else setText(df.format(date));
                setTextFill(Color.LIGHTGRAY);
                setFont(Font.font("Poppins", 13));
            }
        });

        TableColumn<User, Number> salaryCol = new TableColumn<>("給与 (¥)");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salaryPerMonth"));
        salaryCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) setText(null);
                else setText(String.format("¥%,.0f", val.doubleValue()));
                setTextFill(Color.LIGHTGREEN);
                setFont(Font.font("Poppins", 13));
            }
        });

        // Actions
        TableColumn<User, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = createActionButton("✏ 編集", "#00ccff");
            private final Button delBtn = createActionButton("🗑 削除", "#ff4d4d");

            {
                editBtn.setOnAction(e -> editUser(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> deleteUser(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(12, editBtn, delBtn) {{
                    setAlignment(Pos.CENTER);
                }});
            }
        });

        table.getColumns().addAll(idCol, fNameCol, lNameCol, emailCol, phoneCol, roleCol, joinedCol, salaryCol, actionCol);

        // Scrollable Container 🌈
        VBox contentBox = new VBox(30, topBar, table);
        contentBox.setPadding(new Insets(30));
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setStyle("""
            -fx-background-color: rgba(15,20,30,0.8);
            -fx-background-radius: 20;
            -fx-border-color: rgba(0,255,255,0.25);
            -fx-border-radius: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,200,255,0.5), 25, 0.7, 0, 0);
        """);

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scroll.setStyle("""
            -fx-background: transparent;
            -fx-background-color: transparent;
            -fx-border-color: transparent;
        """);

        // Custom Scrollbar Style
        scroll.lookupAll(".scroll-bar").forEach(bar ->
                bar.setStyle("""
                -fx-background-color: transparent;
                -fx-border-color: transparent;
            """)
        );

        setCenter(scroll);
        animate(scroll);
    }

    // 🔹 Column Factory
    private <T> TableColumn<User, T> makeCol(String title, String prop, double width) {
        TableColumn<User, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setCellFactory(whiteTextFactory());
        col.setPrefWidth(width);
        return col;
    }

    // 🔹 White Cell Factory
    private <T> Callback<TableColumn<User, T>, TableCell<User, T>> whiteTextFactory() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.toString());
                setTextFill(Color.WHITE);
                setFont(Font.font("Poppins", 13));
            }
        };
    }

    // 🔹 Buttons
    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Poppins SemiBold", 13));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-padding: 6 14;
            -fx-cursor: hand;
        """, color));
        btn.setOnMouseEntered(e -> btn.setStyle(String.format("""
            -fx-background-color: derive(%s, 20%);
            -fx-background-radius: 8;
            -fx-scale-x: 1.05;
            -fx-scale-y: 1.05;
        """, color)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
        """, color)));
        return btn;
    }

    // 🌟 Animations
    private void animate(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(800), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // ----------------------------------------------------
    // Logic
    // ----------------------------------------------------
    private void loadUsers() {
        try {
            List<User> list = userDB.getAllUsers();
            users.setAll(list);
        } catch (Exception e) {
            System.err.println("❌ 読み込みエラー: " + e.getMessage());
        }
    }

    private void filterUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            loadUsers();
            return;
        }
        List<User> filtered = userDB.getAllUsers().stream()
                .filter(u -> u.getFirstName().toLowerCase().contains(keyword.toLowerCase())
                        || u.getLastName().toLowerCase().contains(keyword.toLowerCase())
                        || u.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
        users.setAll(filtered);
    }

    private void editUser(User u) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("✏ ユーザー編集 - " + u.getFullName());
        dialog.getDialogPane().setStyle("""
            -fx-background-color: rgba(25,30,45,0.95);
            -fx-border-color: #00ffff55;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
        """);

        TextField fName = new TextField(u.getFirstName());
        TextField lName = new TextField(u.getLastName());
        TextField phone = new TextField(u.getPhoneNumber());
        ComboBox<Role> roleBox = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        roleBox.setValue(u.getRole());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("名:"), fName);
        grid.addRow(1, new Label("姓:"), lName);
        grid.addRow(2, new Label("電話番号:"), phone);
        grid.addRow(3, new Label("役割:"), roleBox);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                u.setFirstName(fName.getText());
                u.setLastName(lName.getText());
                u.setPhoneNumber(phone.getText());
                u.setRole(roleBox.getValue());
                userDB.updateUser(u);
                loadUsers();
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void deleteUser(User u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "本当に '" + u.getFullName() + "' を削除しますか？",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                userDB.deleteUser(u.getId());
                loadUsers();
                showAlert(Alert.AlertType.INFORMATION, "🗑 削除完了", "ユーザーが正常に削除されました！");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

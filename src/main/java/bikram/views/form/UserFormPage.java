package bikram.views.form;

import bikram.db.UserDB;
import bikram.db.UserRepository;
import bikram.model.Role;
import bikram.model.User;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;

public class UserFormPage extends BorderPane {

    private final UserRepository userDB = new UserDB();

    public UserFormPage() {

        setPadding(new Insets(40));

        Label title = new Label("👤 新しいユーザーを追加");
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 28));
        title.setStyle("""
            -fx-text-fill: linear-gradient(to right, #00c6ff, #0072ff);
            -fx-effect: dropshadow(gaussian, rgba(0,255,255,0.6), 20, 0.4, 0, 0);
        """);

        TextField fname = createField("名");
        TextField lname = createField("姓");
        TextField email = createField("メール");
        TextField phone = createField("電話番号");
        TextField address = createField("住所");
        PasswordField password = new PasswordField();
        password.setPromptText("パスワード");
        password.setStyle(fname.getStyle());
        TextField salaryField = createField("月給 (¥)");

        ComboBox<Role> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(Role.values());
        roleBox.setPromptText("役職を選択");
        roleBox.setStyle("""
            -fx-background-color: rgba(30,30,40,0.6);
            -fx-text-fill: white;
            -fx-border-color: #00bfff;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
        """);

        Button saveBtn = createSaveButton();

        saveBtn.setOnAction(e -> {
            try {
                if (fname.getText().isEmpty()
                        || lname.getText().isEmpty()
                        || email.getText().isEmpty()
                        || password.getText().isEmpty()
                        || salaryField.getText().isEmpty()
                        || roleBox.getValue() == null) {

                    showAlert(Alert.AlertType.WARNING, "未入力項目", "すべての項目を入力してください。");
                    return;
                }

                User u = new User();
                u.setFirstName(fname.getText());
                u.setLastName(lname.getText());
                u.setEmail(email.getText());
                u.setPhoneNumber(phone.getText());
                u.setAddress(address.getText());
                u.setPassword(password.getText());
                u.setRole(roleBox.getValue());
                double salary_data = Double.parseDouble(salaryField.getText());
                u.setSalaryPerMonth(salary_data);

                userDB.addUser(u);

                showAlert(Alert.AlertType.INFORMATION, "成功",
                        "✅ ユーザー '" + u.getFullName() + "' が追加されました！");

                fname.clear(); lname.clear(); email.clear(); phone.clear();
                address.clear(); password.clear(); roleBox.setValue(null); salaryField.clear();

            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "エラー", ex.getMessage());
            }
        });

        VBox form = new VBox(15, fname, lname, email, phone, address, password, salaryField, roleBox, saveBtn);
        form.setAlignment(Pos.CENTER);

        form.setStyle("""
            -fx-background-color: rgba(25,25,35,0.7);
            -fx-background-radius: 25;
            -fx-border-color: rgba(0,200,255,0.3);
            -fx-border-radius: 25;
            -fx-effect: dropshadow(gaussian, rgba(0,180,255,0.4), 25, 0.6, 0, 0);
        """);

        form.setPadding(new Insets(40, 120, 40, 120));

        VBox center = new VBox(20, title, form);
        center.setAlignment(Pos.CENTER);

        setCenter(center);
    }

    private TextField createField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefWidth(400);
        f.setStyle("""
            -fx-background-color: rgba(30,30,40,0.7);
            -fx-text-fill: white;
            -fx-border-color: #00bfff;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-padding: 10;
            -fx-font-family: 'SF Pro Display';
        """);
        return f;
    }

    private Button createSaveButton() {
        Button btn = new Button("💾 ユーザーを保存");
        btn.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("""
            -fx-background-color: linear-gradient(to right, #00c6ff, #0072ff);
            -fx-background-radius: 12;
            -fx-padding: 13 35;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,200,255,0.5), 15, 0.4, 0, 0);
        """);

        btn.setOnMouseEntered(e -> animateGlow(btn, Color.CYAN));
        btn.setOnMouseExited(e -> animateGlow(btn, Color.web("#00c6ff")));

        return btn;
    }

    private void animateGlow(Button btn, Color color) {
        DropShadow shadow = new DropShadow(25, color);
        shadow.setSpread(0.6);

        Timeline t = new Timeline(
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(btn.effectProperty(), shadow))
        );
        t.play();
    }

    private void showAlert(Alert.AlertType type, String t, String msg) {
        Alert a = new Alert(type);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

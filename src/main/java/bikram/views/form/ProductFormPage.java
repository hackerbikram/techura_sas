package bikram.views.form;

import bikram.db.ProductDB;
import bikram.db.ProductRepository;
import bikram.model.Product;
import bikram.model.ProductType;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class ProductFormPage extends BorderPane {

    private final ProductDB productDB = new ProductDB();

    public ProductFormPage() {
        setPadding(new Insets(40));
        getStyleClass().add("techura-root");

        Label title = new Label("🧾 新製品を追加");
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 30));
        title.setStyle("""
            -fx-text-fill: linear-gradient(to right, #00c6ff, #0072ff);
            -fx-effect: dropshadow(gaussian, rgba(0,255,255,0.7), 20, 0.5, 0, 0);
        """);

        // --- Fields ---
        TextField nameField = createField("製品名");
        TextField priceField = createField("価格 (¥)");
        TextField costField = createField("原価 (¥)");
        TextField discountField = createField("割引 (%)");
        TextField quantityField = createField("量");
        TextField supplierField = createField("サプライヤー");

        ComboBox<ProductType> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().setAll(ProductType.values());
        typeComboBox.setPromptText("カテゴリを選択");
        typeComboBox.setStyle("""
            -fx-background-color: rgba(35,35,45,0.8);
            -fx-text-fill: white;
            -fx-border-color: #00bfff;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-padding: 10 14;
            -fx-font-family: 'SF Pro Display';
        """);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("説明...");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle("""
            -fx-background-color: rgba(35,35,45,0.8);
            -fx-text-fill: white;
            -fx-border-color: #00bfff;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-focus-color: #00bfff;
            -fx-faint-focus-color: transparent;
            -fx-font-family: 'SF Pro Display';
        """);

        Button saveBtn = new Button("💾 製品の保存");
        saveBtn.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
        saveBtn.setTextFill(Color.WHITE);
        saveBtn.setStyle("""
            -fx-background-color: linear-gradient(to right, #0072ff, #00c6ff);
            -fx-background-radius: 14;
            -fx-cursor: hand;
            -fx-padding: 14 40;
            -fx-effect: dropshadow(gaussian, rgba(0,200,255,0.6), 15, 0.6, 0, 0);
        """);

        saveBtn.setOnMouseEntered(e -> animateGlow(saveBtn, Color.CYAN, 0.7));
        saveBtn.setOnMouseExited(e -> animateGlow(saveBtn, Color.web("#00c6ff"), 0.4));

        saveBtn.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                ProductType type = typeComboBox.getValue();
                String supplier = supplierField.getText().trim();
                double price = Double.parseDouble(priceField.getText());
                double cost = Double.parseDouble(costField.getText());
                double discount = Double.parseDouble(discountField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                String desc = descriptionArea.getText().trim();

                if (name.isEmpty() || type == null || supplier.isEmpty() || desc.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "欠落フィールド", "すべての詳細を入力してください。");
                    return;
                }

                Product p = new Product(name, price, quantity, desc);
                p.setCost(cost);
                p.setDiscount(discount);
                p.setSupplier(supplier);
                p.setCategory(type.name());

                productDB.createTable();
                productDB.addProduct(p);

                showAlert(Alert.AlertType.INFORMATION, "成功", "✅ 製品 '" + name + "' が正常に追加されました!");
                clearFields(nameField, priceField, costField, discountField, quantityField, supplierField, descriptionArea);
                typeComboBox.getSelectionModel().clearSelection();

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "無効な入力", "価格、原価、割引、数量は数値で入力してください。");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "データベースエラー", "❌ 製品を保存できませんでした:\n" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        VBox formBox = new VBox(14,
                nameField, priceField, costField, discountField, quantityField,
                typeComboBox, supplierField, descriptionArea, saveBtn);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(35, 120, 35, 120));
        formBox.setStyle("""
            -fx-background-color: rgba(25,25,35,0.7);
            -fx-background-radius: 30;
            -fx-border-color: rgba(0,200,255,0.3);
            -fx-border-radius: 30;
            -fx-effect: dropshadow(gaussian, rgba(0,180,255,0.45), 25, 0.6, 0, 0);
        """);

        VBox centerBox = new VBox(25, title, formBox);
        centerBox.setAlignment(Pos.CENTER);
        setCenter(centerBox);
        setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(10,15,30,0.9), rgba(20,25,40,0.95));");
        formBox.setEffect(new DropShadow(50, Color.rgb(0,255,255,0.25)));
    }

    private void animateGlow(Button btn, Color color, double opacity) {
        DropShadow shadow = new DropShadow(25, color);
        shadow.setSpread(opacity);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.3), new KeyValue(btn.effectProperty(), shadow)));
        timeline.play();
    }

    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("""
            -fx-background-color: rgba(35,35,45,0.8);
            -fx-text-fill: white;
            -fx-border-color: #00bfff;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-focus-color: #00bfff;
            -fx-faint-focus-color: transparent;
            -fx-padding: 10 14;
            -fx-font-family: 'SF Pro Display';
        """);
        field.setPrefWidth(420);
        return field;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void clearFields(TextField... fields) {
        for (TextField f : fields) f.clear();
    }
    private void clearFields(Control... controls) {
        for (Control c : controls) {
            if (c instanceof TextField tf) tf.clear();
            else if (c instanceof TextArea ta) ta.clear();
        }
    }


}

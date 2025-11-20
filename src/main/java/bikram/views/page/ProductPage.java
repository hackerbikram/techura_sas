package bikram.views.page;

import bikram.db.ProductDB;
import bikram.db.ProductRepository;
import bikram.model.Product;
import bikram.util.Navigator;
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

import java.util.List;

public class ProductPage extends BorderPane {

    private final ProductRepository productDB = new ProductDB();
    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private TableView<Product> table;

    public ProductPage() {
        setPadding(new Insets(30));
        buildUI();
        loadProducts();
    }

    private void buildUI() {
        Label title = new Label("📦 製品ダッシュボード");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#00eaff"));
        title.setEffect(new DropShadow(30, Color.web("#00ffff")));

        Button addBtn = createButton("➕ 製品を追加", "#0072ff");
        addBtn.setOnAction(e -> Navigator.navigate("ProductFormPage"));

        Button refreshBtn = createButton("🔄 更新", "#00c6ff");
        refreshBtn.setOnAction(e -> loadProducts());

        HBox topBar = new HBox(20, title, addBtn, refreshBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        table = new TableView<>();
        table.setEditable(false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(600);

        // Columns
        TableColumn<Product, String> idCol = new TableColumn<>("UUID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setCellFactory(whiteTextFactory());

        TableColumn<Product, String> nameCol = new TableColumn<>("名前");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(whiteTextFactory());

        TableColumn<Product, Double> priceCol = new TableColumn<>("価格 (¥)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(whiteTextFactory());

        TableColumn<Product, Double> costCol = new TableColumn<>("原価 (¥)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        costCol.setCellFactory(whiteTextFactory());

        TableColumn<Product, Double> profitCol = new TableColumn<>("利益 (¥)");
        profitCol.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createDoubleBinding(
                        () -> cellData.getValue().getPrice() - cellData.getValue().getCost()
                ).asObject()
        );
        profitCol.setCellFactory(whiteTextFactory());

        TableColumn<Product, Integer> qtyCol = new TableColumn<>("在庫数");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setCellFactory(whiteTextFactory());

        TableColumn<Product, String> categoryCol = new TableColumn<>("カテゴリ");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setCellFactory(whiteTextFactory());

        TableColumn<Product, String> supplierCol = new TableColumn<>("仕入先");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        supplierCol.setCellFactory(whiteTextFactory());

        TableColumn<Product, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = createActionButton("✏️ 編集", "#00ccff");
            private final Button deleteBtn = createActionButton("🗑 削除", "#ff4d4d");
            {
                editBtn.setOnAction(e -> editProduct(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteProduct(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(10, editBtn, deleteBtn));
            }
        });

        table.getColumns().addAll(idCol, nameCol, priceCol, costCol, profitCol, qtyCol, categoryCol, supplierCol, actionCol);
        table.setItems(products);

        // Scrollable
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        VBox content = new VBox(20, topBar, scrollPane);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: rgba(15,20,30,0.85); -fx-background-radius: 20; -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,200,255,0.3), 20, 0.7, 0, 0);");

        setCenter(content);
    }

    private Button createButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Poppins SemiBold", 14));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            -fx-padding: 8 20;
        """.formatted(colorHex));
        return btn;
    }

    private Button createActionButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Poppins SemiBold", 12));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: %s; -fx-background-radius: 8; -fx-padding: 5 12;".formatted(colorHex));
        return btn;
    }

    private void loadProducts() {
        productDB.createTable();
        List<Product> list = productDB.getAllProducts();
        products.setAll(list);
        animate(table);
    }

    private void animate(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(600), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void editProduct(Product p) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("✏️ 製品編集 - " + p.getName());
        dialog.getDialogPane().setStyle("-fx-background-color: rgba(25,30,45,0.95); -fx-border-radius:10; -fx-background-radius:10;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(10));

        TextField name = new TextField(p.getName());
        TextField price = new TextField(String.valueOf(p.getPrice()));
        TextField cost = new TextField(String.valueOf(p.getCost()));
        TextField discount = new TextField(String.valueOf(p.getDiscount()));
        TextField qty = new TextField(String.valueOf(p.getQuantity()));
        TextField cat = new TextField(p.getCategory());
        TextField sup = new TextField(p.getSupplier());
        TextArea desc = new TextArea(p.getDescription());

        grid.addRow(0, new Label("名前:"), name);
        grid.addRow(1, new Label("価格 (¥):"), price);
        grid.addRow(2, new Label("原価 (¥):"), cost);
        grid.addRow(3, new Label("割引 (%):"), discount);
        grid.addRow(4, new Label("数量:"), qty);
        grid.addRow(5, new Label("カテゴリ:"), cat);
        grid.addRow(6, new Label("仕入先:"), sup);
        grid.addRow(7, new Label("説明:"), desc);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    p.setName(name.getText());
                    p.setPrice(Double.parseDouble(price.getText()));
                    p.setCost(Double.parseDouble(cost.getText()));
                    p.setDiscount(Double.parseDouble(discount.getText()));
                    p.setQuantity(Integer.parseInt(qty.getText()));
                    p.setCategory(cat.getText());
                    p.setSupplier(sup.getText());
                    p.setDescription(desc.getText());
                    productDB.updateProduct(p);
                    loadProducts();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "❌ エラー", ex.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void deleteProduct(Product p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "'" + p.getName() + "' を削除しますか？", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                productDB.deleteProductById(p.getId());
                loadProducts();
                showAlert(Alert.AlertType.INFORMATION, "🗑 削除完了", "製品が削除されました！");
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

    private <T> Callback<TableColumn<Product, T>, TableCell<Product, T>> whiteTextFactory() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item.toString());
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Poppins", 13));
                }
            }
        };
    }
}

/*package bikram.views.page;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class ProductPage extends BorderPane {

    private final TableView<Product> table;

    public ProductPage() {
        setPadding(new Insets(20));
        getStyleClass().add("techura-root");

        Label title = new Label("🧾 Product Management");
        title.getStyleClass().add("page-title");

        // --- Table Setup ---
        table = new TableView<>();
        table.getStyleClass().add("techura-table");

        TableColumn<Product, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Product, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Double> discountCol = new TableColumn<>("Discount (%)");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quintity"));

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(idCol, nameCol, categoryCol, supplierCol, priceCol, discountCol, quantityCol, descCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //refreshTable();

        // --- Add Sample Product Button ---
        Button addBtn = new Button("+ Add Product");
        addBtn.getStyleClass().add("login-btn");
        addBtn.setOnAction(e -> {
            Product sample = new Product("MacBook Pro M4", 3800, 5, "Latest Apple Silicon Laptop");
            sample.setCategory("Laptop");
            sample.setSupplier("Apple Inc.");
            sample.setDiscount(8);
          ///  productDAO.addProduct(sample);
           // refreshTable();
        });

        HBox topBar = new HBox(10, title, addBtn);
        topBar.setPadding(new Insets(10, 0, 20, 0));

        setTop(topBar);
        setCenter(table);
    }

   /* private void refreshTable() {
        ObservableList<Product> products = FXCollections.observableArrayList(productDAO.getAllProducts());
        table.setItems(products);
        System.out.println("✅ Table refreshed: " + products.size() + " products loaded.");
    }*/


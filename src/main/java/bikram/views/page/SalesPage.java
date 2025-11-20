package bikram.views.page;

import bikram.db.*;
import bikram.model.Product;
import bikram.model.Sales;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.util.*;

public class SalesPage extends BorderPane {
    private final VBox cartBox = new VBox(10);
    private final Label totalLabel = new Label("合計: ¥0.00");
    private final Label discountLabel = new Label("割引: ¥0.00");
    private final Label finalLabel = new Label("最終合計: ¥0.00");
    private final Label changeLabel = new Label("お釣り: ¥0.00");

    private final List<CartItem> cart = new ArrayList<>();
    private final GridPane productGrid = new GridPane();
    private double total = 0, discount = 0, finalTotal = 0;

    private final ProductRepository pdb  = new ProductDB();
    private final Salesrepository sdb = new SalesDB();

    // 支払い関連
    private final ComboBox<String> paymentMethod = new ComboBox<>();
    private final TextField paymentAmount = new TextField();

    public SalesPage() {
        setPadding(new Insets(20));
        setBackground(new Background(new BackgroundFill(Color.web("#f9f9f9"), CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = new Label("🛒 販売ページ");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#333"));

        VBox leftPane = new VBox(10, title, createProductView());
        leftPane.setPadding(new Insets(10));
        ScrollPane leftScroll = new ScrollPane(leftPane);
        leftScroll.setFitToWidth(true);

        VBox rightPane = new VBox(10, createCartView(), createCheckoutBox());
        rightPane.setPadding(new Insets(10));
        rightPane.setPrefWidth(360);

        setLeft(leftScroll);
        setRight(rightPane);

        loadProductsFromDB();
    }

    // ✅ 商品をロード
    private void loadProductsFromDB() {
        productGrid.getChildren().clear();
        productGrid.setHgap(15);
        productGrid.setVgap(15);
        productGrid.setPadding(new Insets(10));

        int col = 0, row = 0;
        List<Product> productList = pdb.getAllProducts();

        for (Product p : productList) {
            VBox card = createProductCard(p);
            productGrid.add(card, col++, row);
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ 商品カード作成
    private VBox createProductCard(Product product) {
        Label name = new Label(product.getName());
        name.setFont(Font.font("Poppins", FontWeight.BOLD, 14));

        Label price = new Label(String.format("¥%.2f", product.getPrice()));
        price.setTextFill(Color.web("#4CAF50"));

        Button addBtn = new Button("カートに追加");
        addBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addToCart(product));

        VBox card = new VBox(5, name, price, addBtn);
        card.setPadding(new Insets(10));
        card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        card.setEffect(new DropShadow(3, Color.gray(0.4)));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);

        return card;
    }

    // ✅ カートに追加
    private void addToCart(Product p) {
        if (p.getQuantity() <= 0) {
            showError("在庫切れ", p.getName() + " は在庫がありません！");
            return;
        }
        Optional<CartItem> existing = cart.stream().filter(c -> c.product.getId() == p.getId()).findFirst();
        if (existing.isPresent()) {
            if (existing.get().quantity < p.getQuantity()) {
                existing.get().quantity++;
            } else {
                showError("在庫制限", "これ以上追加できません — 在庫は " + p.getQuantity() + " です！");
            }
        } else {
            cart.add(new CartItem(p, 1));
        }
        updateCartUI();
    }

    // ✅ カートUI更新
    private VBox createCartView() {
        Label cartTitle = new Label("🧾 カート");
        cartTitle.setFont(Font.font("Poppins", FontWeight.BOLD, 18));
        cartBox.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(cartBox);
        scroll.setFitToWidth(true);

        VBox box = new VBox(10, cartTitle, scroll);
        box.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        box.setEffect(new DropShadow(4, Color.gray(0.4)));
        return box;
    }

    private void updateCartUI() {
        cartBox.getChildren().clear();
        total = 0;
        for (CartItem c : cart) {
            double subtotal = c.product.getPrice() * c.quantity;
            total += subtotal;

            HBox item = new HBox(10);
            Label name = new Label(c.product.getName() + " x" + c.quantity);
            Label price = new Label(String.format("¥%.2f", subtotal));
            Button remove = new Button("❌");
            remove.setOnAction(e -> {
                cart.remove(c);
                updateCartUI();
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            item.getChildren().addAll(name, spacer, price, remove);
            cartBox.getChildren().add(item);
        }
        calculateTotals();
    }

    // ✅ 合計計算
    private void calculateTotals() {
        discount = total > 5000 ? total * 0.10 : 0; // 5000円以上で10%割引
        finalTotal = total - discount;
        totalLabel.setText(String.format("合計: ¥%.2f", total));
        discountLabel.setText(String.format("割引: ¥%.2f", discount));
        finalLabel.setText(String.format("最終合計: ¥%.2f", finalTotal));
    }

    // ✅ チェックアウト
    private VBox createCheckoutBox() {
        paymentMethod.getItems().addAll("💴 現金", "💳 カード", "📱 QR/オンライン");
        paymentMethod.setValue("💴 現金");

        paymentAmount.setPromptText("お支払金額入力してください");
        paymentAmount.textProperty().addListener((obs, old, val) -> calculateChange());

        Button checkoutBtn = new Button("💰 会計完了");
        checkoutBtn.setStyle("-fx-background-color: #43A047; -fx-text-fill: white; -fx-font-weight: bold;");
        checkoutBtn.setOnAction(e -> processPayment());

        VBox box = new VBox(10,
                totalLabel,
                discountLabel,
                finalLabel,
                new Label("支払い方法:"), paymentMethod,
                new Label("お支払金額:"),paymentAmount,
                changeLabel,
                checkoutBtn
        );

        box.setPadding(new Insets(15));
        box.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        box.setEffect(new DropShadow(5, Color.gray(0.3)));
        return box;
    }

    // ✅ お釣り計算
    private void calculateChange() {
        try {
            double paid = Double.parseDouble(paymentAmount.getText());
            double change = paid - finalTotal;
            changeLabel.setText(String.format("お釣り: ¥%.2f", change >= 0 ? change : 0));
        } catch (NumberFormatException e) {
            changeLabel.setText("お釣り: ¥0.00");
        }
    }

    // ✅ 支払い処理
    private void processPayment() {
        if (cart.isEmpty()) {
            showError("カートが空です", "まず商品を追加してください！");
            return;
        }

        double paidAmount;
        try {
            paidAmount = Double.parseDouble(paymentAmount.getText());
        } catch (NumberFormatException e) {
            showError("入力エラー", "有効な支払金額を入力してください！");
            return;
        }

        if (paidAmount < finalTotal) {
            showError("支払い不足", "お客様は最終合計以上を支払う必要があります！");
            return;
        }

        String method = paymentMethod.getValue();
        saveSalesToDB(method, paidAmount);
    }

    // ✅ 売上記録保存
    private void saveSalesToDB(String paymentMethod, double paidAmount) {
        try {
            for (CartItem c : cart) {
                Sales sale = new Sales.Builder()
                        .productId(String.valueOf(c.product.getId()))
                        .name(c.product.getName())
                        .quantity(c.quantity)
                        .salePrice(c.product.getPrice())
                        .discount(discount / total * 100)
                        .paymentMethod(paymentMethod)
                        .paidAmount(paidAmount)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                sdb.saveSales(sale);
                int newQuantity = c.product.getQuantity() - c.quantity;
                c.product.setQuantity(Math.max(newQuantity, 0));
                pdb.updateProduct(c.product);
            }

            new Alert(Alert.AlertType.INFORMATION, "✅ 販売が完了しました！").showAndWait();
            cart.clear();
            updateCartUI();

        } catch (Exception e) {
            e.printStackTrace();
            showError("保存失敗", "販売を完了できませんでした。もう一度お試しください。");
        }
    }

    // ✅ カートアイテム
    static class CartItem {
        Product product;
        int quantity;
        CartItem(Product p, int q) {
            product = p;
            quantity = q;
        }
    }

    // ✅ 商品セクション
    private VBox createProductView() {
        Label section = new Label("📦 商品一覧");
        section.setFont(Font.font("Poppins", FontWeight.BOLD, 18));
        return new VBox(10, section, productGrid);
    }
}

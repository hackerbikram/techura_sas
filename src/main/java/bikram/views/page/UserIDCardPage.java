package bikram.views.page;

import bikram.db.UserRepository;
import bikram.model.User;
import bikram.db.UserDB;
import bikram.util.barcode.BarcodeGenerator;
import com.google.zxing.BarcodeFormat;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class UserIDCardPage extends VBox {

    private final UserRepository userDB = new UserDB();
    private final GridPane grid = new GridPane();
    private static final int CARDS_PER_ROW = 2;
    private static final int CARD_WIDTH = 420;
    private static final int CARD_HEIGHT = 210;

    public UserIDCardPage() {
        setPadding(new Insets(20));
        setSpacing(20);
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #f5f5f5;");

        // --- タイトル ---
        Text title = new Text("従業員 / ユーザーIDカード");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setFill(Color.DARKSLATEGRAY);

        // --- ボタンエリア ---
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveAllBtn = new Button("すべて保存");
        Button saveSelectedBtn = new Button("選択した保存");
        Button printAllBtn = new Button("すべて印刷");
        Button printSelectedBtn = new Button("選択した印刷");
        Button printAllDirectBtn = new Button("🖨 直接印刷（全て）");

        saveAllBtn.setStyle(buttonStyle("#4CAF50"));
        saveSelectedBtn.setStyle(buttonStyle("#2196F3"));
        printAllBtn.setStyle(buttonStyle("#009688"));
        printSelectedBtn.setStyle(buttonStyle("#FF9800"));
        printAllDirectBtn.setStyle(buttonStyle("#E91E63"));

        buttonBox.getChildren().addAll(printAllBtn, printSelectedBtn, saveAllBtn, saveSelectedBtn, printAllDirectBtn);

        // --- グリッドレイアウト ---
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setAlignment(Pos.CENTER);

        getChildren().addAll(title, grid, buttonBox);

        // --- ユーザー情報を読み込み ---
        List<User> users = userDB.getAllUsers();
        int col = 0, row = 0;

        for (User user : users) {
            ensureBarcodeExists(user);
            VBox card = createCard(user);
            grid.add(card, col, row);

            // デフォルト枠線
            card.setStyle(card.getStyle() + "-fx-border-color: #0288d1;");

            // --- カード選択イベント ---
            card.setOnMouseClicked(e -> {
                grid.getChildren().forEach(node -> {
                    if (node instanceof VBox vbox)
                        vbox.setStyle(vbox.getStyle().replace("#FF9800", "#0288d1"));
                });
                card.setStyle(card.getStyle().replace("#0288d1", "#FF9800"));

                // 選択したカード保存・印刷
                saveSelectedBtn.setOnAction(ev -> saveSingleCard(card, user));
                printSelectedBtn.setOnAction(ev -> {
                    saveSingleCard(card, user);
                    printSingleCard(card, user);
                });
            });

            col++;
            if (col >= CARDS_PER_ROW) {
                col = 0;
                row++;
            }
        }

        // --- ボタン動作設定 ---
        saveAllBtn.setOnAction(e -> saveAllCards());
        printAllBtn.setOnAction(e -> printAllCards());
        printAllDirectBtn.setOnAction(e -> printAllCardsDirect());
    }

    // --- すべてのカードを印刷（プリンタ選択あり） ---
    private void printAllCards() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            System.err.println("⚠ プリンタジョブを作成できません。");
            return;
        }

        // プリンタ選択ダイアログを表示
        boolean proceed = job.showPrintDialog(null);
        if (!proceed) {
            System.out.println("❌ 印刷がキャンセルされました。");
            return;
        }

        // 選択されたプリンタ情報
        Printer printer = job.getPrinter();
        if (printer == null) {
            System.err.println("⚠ プリンタが選択されていません。");
            return;
        }

        System.out.println("🖨 「" + printer.getName() + "」で全てのIDカードを印刷します...");

        // グリッド全体を印刷
        boolean success = job.printPage(grid);
        if (success) {
            job.endJob();
            System.out.println("✅ すべてのIDカードを正常に印刷しました。");
        } else {
            System.err.println("❌ 印刷に失敗しました。");
        }
    }


    // --- 個別IDカード作成 ---
    private VBox createCard(User user) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(12));
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setStyle("""
        -fx-background-color: linear-gradient(to bottom right, #ffffff, #e3f2fd);
        -fx-border-radius: 12;
        -fx-background-radius: 12;
        -fx-border-color: #0288d1;
        -fx-border-width: 2;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 2, 2);
        """);

        // 上部行（ロゴ・写真）
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        ImageView logo = new ImageView(safeLoadImage("/images/logo.png", "https://via.placeholder.com/50.png?text=L"));
        logo.setFitWidth(45);
        logo.setFitHeight(45);

        Text companyName = new Text("Techura 株式会社");
        companyName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        companyName.setFill(Color.web("#01579b"));

        VBox companyBox = new VBox(companyName);
        companyBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        File photoFile = new File("src/photos/" + user.getId() + ".png");
        Image photo = photoFile.exists() ?
                new Image(photoFile.toURI().toString()) :
                safeLoadImage("/images/avatar.png", "https://via.placeholder.com/70.png?text=User");

        ImageView photoView = new ImageView(photo);
        photoView.setFitWidth(70);
        photoView.setFitHeight(70);
        photoView.setPreserveRatio(true);
        photoView.setClip(new Circle(35, 35, 35));

        VBox photoBox = new VBox(photoView);
        photoBox.setAlignment(Pos.TOP_RIGHT);

        topRow.getChildren().addAll(logo, companyBox, spacer, photoBox);

        // ユーザー情報
        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Text name = new Text(user.getFullName());
        name.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        name.setFill(Color.web("#0d47a1"));

        Text role = new Text(user.getRoleStr());
        role.setFont(Font.font("Arial", 13));
        role.setFill(Color.DARKRED);

        Text idText = new Text("ID: " + user.getId());
        Text phone = new Text("電話番号: " + user.getPhoneNumber());
        Text address = new Text("住所: " + user.getAddress());
        Text joined = new Text("入社日: " + user.getJoined_date());
        for (Text t : List.of(idText, phone, address, joined)) t.setFont(Font.font("Arial", 12));

        infoBox.getChildren().addAll(name, role, idText, phone, address, joined);

        // バーコード
        Image barcodeImage = loadBarcode(user);
        ImageView barcodeView = new ImageView(barcodeImage);
        barcodeView.setFitWidth(280);
        barcodeView.setFitHeight(45);
        barcodeView.setPreserveRatio(true);

        VBox barcodeBox = new VBox(barcodeView);
        barcodeBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(topRow, infoBox, barcodeBox);
        return card;
    }

    private Image safeLoadImage(String path, String fallbackUrl) {
        URL res = getClass().getResource(path);
        if (res != null) return new Image(res.toExternalForm());
        System.err.println("⚠ 画像が見つかりません: " + path);
        return new Image(fallbackUrl);
    }

    private Image loadBarcode(User user) {
        try {
            String basePath = "src/barcodes/";
            File file = new File(basePath + user.getId() + ".png");
            return file.exists() ? new Image(file.toURI().toString()) :
                    new Image("https://via.placeholder.com/200x50.png?text=No+Code");
        } catch (Exception e) {
            return new Image("https://via.placeholder.com/200x50.png?text=Error");
        }
    }

    private void ensureBarcodeExists(User user) {
        String basePath = "src/barcodes/";
        File file = new File(basePath + user.getId() + ".png");
        if (!file.exists()) {
            BarcodeGenerator.generateBarcode(user.getId(), user.getFullName(), BarcodeFormat.CODE_128, 300, 100);
        }
    }

    // --- 個別カード保存 ---
    private void saveSingleCard(VBox card, User user) {
        WritableImage snapshot = card.snapshot(new SnapshotParameters(), null);
        File file = new File("exports/id_" + user.getId() + ".png");
        file.getParentFile().mkdirs();
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            System.out.println("✅ 個別IDカードを保存しました: " + file.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // --- 全カード保存 ---
    private void saveAllCards() {
        WritableImage snapshot = grid.snapshot(new SnapshotParameters(), null);
        File file = new File("exports/all_idcards.png");
        file.getParentFile().mkdirs();
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            System.out.println("✅ すべてのIDカードを保存しました: " + file.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // --- 単一カード印刷 ---
    private void printSingleCard(VBox card, User user) {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job == null) {
            System.err.println("⚠ プリンタジョブが作成できません。");
            return;
        }

        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) {
            System.err.println("⚠ プリンタが接続されていません。");
            return;
        }

        job.setPrinter(printer);
        System.out.println("🖨 " + printer.getName() + " で「" + user.getFullName() + "」のIDカードを印刷中...");

        boolean success = job.printPage(card);
        if (success) {
            job.endJob();
            System.out.println("✅ 「" + user.getFullName() + "」のIDカードを印刷しました。");
        } else {
            System.err.println("❌ 印刷に失敗しました: " + user.getFullName());
        }
    }

    // --- 全カードをまとめて直接印刷 ---
    private void printAllCardsDirect() {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job == null) {
            System.err.println("⚠ プリンタジョブが作成できません。");
            return;
        }

        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) {
            System.err.println("⚠ プリンタが検出されません。");
            return;
        }

        job.setPrinter(printer);
        System.out.println("🖨 すべてのIDカードを " + printer.getName() + " で印刷中...");

        boolean success = job.printPage(grid);
        if (success) {
            job.endJob();
            System.out.println("✅ すべてのIDカードを印刷しました。");
        } else {
            System.err.println("❌ 印刷に失敗しました。");
        }
    }

    private String buttonStyle(String color) {
        return String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 8 16;
            -fx-background-radius: 10;
            """, color);
    }
}

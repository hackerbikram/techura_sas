package bikram.views.page;

import bikram.db.ProductDB;
import bikram.db.ProductRepository;
import bikram.model.Product;
import bikram.util.barcode.BarcodeGenerator;
import com.google.zxing.BarcodeFormat;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PriceCardPage extends VBox {

    private final ProductRepository pdb = new ProductDB();
    private static final int CARDS_PER_ROW = 5;
    private static final int CARD_WIDTH = 200;
    private static final int CARD_HEIGHT = 150;
    private static final double PAGE_WIDTH = 595; // A4 width in px ~72dpi
    private static final double PAGE_HEIGHT = 842; // A4 height in px ~72dpi
    private final List<VBox> pages = new ArrayList<>();

    public PriceCardPage() {
        setPadding(new Insets(20));
        setSpacing(20);
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: white;");

        Text title = new Text("Product Price Cards");
        title.setFont(Font.font("Arial", 24));

        Button printButton = new Button("🖨 印刷する / PDFを保存");
        printButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 10;");
        printButton.setOnAction(e -> printPages());

        getChildren().addAll(title, printButton);

        List<Product> products = pdb.getAllProducts();
        createPages(products);

        // Add all pages to VBox
        getChildren().addAll(pages);
    }

    // --- Create pages with GridPane for each A4 page ---
    private void createPages(List<Product> products) {
        GridPane grid = createEmptyGrid();
        int col = 0, row = 0;
        VBox page = new VBox(grid);
        page.setSpacing(15);
        pages.add(page);

        for (Product p : products) {
            ensureBarcodeExists(p);
            VBox card = createCard(p);

            grid.add(card, col, row);
            col++;
            if (col >= CARDS_PER_ROW) {
                col = 0;
                row++;
            }

            // Check if grid height exceeds page height
            if ((row + 1) * (CARD_HEIGHT + 15) > PAGE_HEIGHT - 50) {
                // Start new page
                grid = createEmptyGrid();
                page = new VBox(grid);
                page.setSpacing(15);
                pages.add(page);
                col = 0;
                row = 0;
            }
        }
    }

    // --- Create an empty GridPane for cards ---
    private GridPane createEmptyGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.TOP_CENTER);
        return grid;
    }

    // --- Individual card ---
    private VBox createCard(Product product) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setStyle("""
                -fx-border-color: #ccc;
                -fx-border-width: 1;
                -fx-border-radius: 5;
                -fx-background-radius: 5;
                -fx-background-color: #ffffff;
                """);

        Text name = new Text(product.getName());
        name.setFont(Font.font("Arial", 14));

        Text idText = new Text(product.getId());
        idText.setFont(Font.font("Arial", 12));

        Image barcodeImage = loadBarcode(product);
        ImageView barcodeView = new ImageView(barcodeImage);
        barcodeView.setFitWidth(150);
        barcodeView.setFitHeight(40);
        barcodeView.setPreserveRatio(true);

        Text price = new Text("¥" + product.getPrice());
        price.setFont(Font.font("Arial", 14));
        price.setFill(Color.DARKBLUE);

        card.getChildren().addAll(name, barcodeView, idText, price);
        return card;
    }

    // --- Load barcode ---
    private Image loadBarcode(Product product) {
        try {
            String basePath = "src/barcodes/";
            File file = new File(basePath + product.getName().replaceAll("[^a-zA-Z0-9-_]", "_") + ".png");
            if (!file.exists()) file = new File(basePath + product.getId() + ".png");

            if (file.exists()) return new Image(file.toURI().toString());
            else return new Image("https://via.placeholder.com/150x40.png?text=No+Barcode");
        } catch (Exception e) {
            e.printStackTrace();
            return new Image("https://via.placeholder.com/150x40.png?text=Error");
        }
    }

    private void ensureBarcodeExists(Product product) {
        String basePath = "src/barcodes/";
        File file = new File(basePath + product.getName().replaceAll("[^a-zA-Z0-9-_]", "_") + ".png");
        if (!file.exists()) {
            BarcodeGenerator.generateBarcode(product.getId(), product.getName(), BarcodeFormat.CODE_128, 300, 100);
        }
    }

    // --- Print all pages ---
    private void printPages() {
        Stage stage = (Stage) getScene().getWindow();
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(stage)) {
            for (VBox page : pages) {
                PageLayout layout = job.getJobSettings().getPageLayout();
                double scaleX = layout.getPrintableWidth() / page.getWidth();
                double scaleY = layout.getPrintableHeight() / page.getHeight();
                page.setScaleX(scaleX);
                page.setScaleY(scaleY);

                job.printPage(page);

                page.setScaleX(1);
                page.setScaleY(1);
            }
            job.endJob();
        }

        // Optional: save as multi-page PNG (first page only for simplicity)
        WritableImage snapshot = pages.get(0).snapshot(new SnapshotParameters(), null);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("価格カードをPNGとして保存");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG File", "*.png"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

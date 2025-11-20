package bikram.views.page;

import bikram.util.barcode.QRCodeGenerator;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;

public class QRGeneratorPage extends VBox {

    private ImageView qrPreview = new ImageView();
    private TextField inputDataField = new TextField();
    private TextField filenameField = new TextField();
    private Slider sizeSlider = new Slider(100, 800, 300);
    private BufferedImage lastGeneratedImage = null;

    public QRGeneratorPage() {

        Label title = new Label("✨ QRコードジェネレーター");
        title.setStyle("""
                -fx-font-size: 32px;
                -fx-font-weight: bold;
                -fx-text-fill: linear-gradient(to right, #00f2ff, #8b5cf6);
                """);

        inputDataField.setPromptText("テキストを入力 / URL / ID ...");
        inputDataField.setStyle(glowInput());

        filenameField.setPromptText("ファイル名 (.png無し)");
        filenameField.setStyle(glowInput());

        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);

        Button generateBtn = styledBtn("生成する");
        Button saveBtn = styledBtn("PNGを保存します");
        Button downloadBtn = styledBtn("ダウンロード");
        Button clearBtn = styledBtn("クリアー");

        qrPreview.setFitWidth(250);
        qrPreview.setFitHeight(250);
        qrPreview.setPreserveRatio(true);
        qrPreview.setStyle("-fx-effect: dropshadow(gaussian, #00f2ff, 20, 0.6, 0, 0);");

        HBox controls = new HBox(15, generateBtn, saveBtn, downloadBtn, clearBtn);
        controls.setAlignment(Pos.CENTER);

        VBox inner = new VBox(20,
                title,
                inputDataField,
                filenameField,
                new Label("Size:"),
                sizeSlider,
                qrPreview,
                controls
        );
        inner.setAlignment(Pos.CENTER);

        this.getChildren().add(inner);
        this.setPadding(new Insets(25));
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle(backgroundStyle());

        // ACTIONS
        generateBtn.setOnAction(e -> generateQR());
        saveBtn.setOnAction(e -> saveQR());
        downloadBtn.setOnAction(e -> downloadQR());
        clearBtn.setOnAction(e -> clearAll());
    }

    /** --------------------- CORE QR GENERATOR ----------------------- **/
    private void generateQR() {
        try {
            String data = inputDataField.getText().trim();
            String file = filenameField.getText().trim();
            int size = (int) sizeSlider.getValue();

            if (data.isEmpty()) {
                alert("いくつかのデータを入力して、QR を生成します");
                return;
            }
            if (file.isEmpty()) {
                alert("ファイル名を入力します");
                return;
            }

            // Save using utility class
            QRCodeGenerator.generateQRCode(data, file, size);

            // Display preview
            BitMatrix matrix = new MultiFormatWriter().encode(
                    data, BarcodeFormat.QR_CODE, size, size);

            lastGeneratedImage = MatrixToImageWriter.toBufferedImage(matrix);
            Image fxImg = SwingFXUtils.toFXImage(lastGeneratedImage, null);
            qrPreview.setImage(fxImg);

        } catch (Exception ex) {
            alert("Error: " + ex.getMessage());
        }
    }

    /** --------------------- SAVE TO src/qrcodes ----------------------- **/
    private void saveQR() {
        String data = inputDataField.getText().trim();
        String file = filenameField.getText().trim();
        int size = (int) sizeSlider.getValue();

        if (data.isEmpty() || file.isEmpty()) {
            alert("Please generate a QR first!");
            return;
        }

        QRCodeGenerator.generateQRCode(data, file, size);
        alert("に保存します: src/qrcodes/" + file + ".png");
    }

    /** --------------------- DOWNLOAD USING FILECHOOSER ----------------------- **/
    private void downloadQR() {

        if (lastGeneratedImage == null) {
            alert("Generate a QR first!");
            return;
        }

        Stage stage = (Stage) this.getScene().getWindow();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Download QR Code");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        File file = chooser.showSaveDialog(stage);

        if (file != null) {
            try {
                MatrixToImageWriter.writeToPath(
                        new MultiFormatWriter().encode(
                                inputDataField.getText(),
                                BarcodeFormat.QR_CODE,
                                lastGeneratedImage.getWidth(),
                                lastGeneratedImage.getHeight()
                        ),
                        "png",
                        file.toPath()
                );
            } catch (Exception e) {
                alert("ダウンロードに失敗しました: " + e.getMessage());
            }
        }
    }

    /** --------------------- CLEAR ----------------------- **/
    private void clearAll() {
        inputDataField.clear();
        filenameField.clear();
        qrPreview.setImage(null);
        lastGeneratedImage = null;
    }

    /** --------------------- STYLES ----------------------- **/
    private String backgroundStyle() {
        return """
                -fx-background-color: linear-gradient(to bottom right, #0f0c29, #302b63, #24243e);
                """;
    }

    private String glowInput() {
        return """
                -fx-background-radius: 12;
                -fx-padding: 10;
                -fx-font-size: 18px;
                -fx-background-color: rgba(255,255,255,0.1);
                -fx-border-color: #00f2ff;
                -fx-border-radius: 12;
                -fx-border-width: 1.5;
                -fx-text-fill: white;
                """;
    }

    private Button styledBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("""
                -fx-font-size: 18px;
                -fx-background-color: linear-gradient(to right, #4e54c8, #8f94fb);
                -fx-text-fill: white;
                -fx-padding: 12 20;
                -fx-cursor: hand;
                -fx-background-radius: 14;
                """);

        btn.setOnMouseEntered(e ->
                btn.setEffect(new DropShadow(25, Color.CYAN))
        );
        btn.setOnMouseExited(e -> btn.setEffect(null));
        return btn;
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.show();
    }
}

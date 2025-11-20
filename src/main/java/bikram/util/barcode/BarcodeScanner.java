package bikram.util.barcode;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamResolution;
//import com.github.sarxos.webcam.ds.buildin.OpenCvDriver;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public class BarcodeScanner {

    private final TextField inputField;
    private final BarcodeCallback callback;

    public interface BarcodeCallback {
        void onBarcodeScanned(String code);
    }

    public BarcodeScanner(TextField field, BarcodeCallback cb) {
        this.inputField = field;
        this.callback = cb;
        setupKeyboardListener();
    }

    /** USB/Bluetooth scanner input (HID keyboard) */
    private void setupKeyboardListener() {
        inputField.setOnAction(e -> {
            String code = inputField.getText();
            if (!code.isEmpty()) {
                callback.onBarcodeScanned(code);
                inputField.clear();
            }
        });
    }

    /** Main method to start scanning with camera if no USB scanner is available */
    public void scanWithCamera() {
        // Use OpenCV driver for MacBook/Apple Silicon
        if (isMacBook()) {
            scanWithMacCamera();
        } else {
            scanWithDefaultCamera();
        }
    }

    /** Detect if the system is macOS */
    private boolean isMacBook() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac");
    }

    /** Apple Silicon / MacBook webcam */
    private void scanWithMacCamera() {
        Webcam webcam = Webcam.getDefault(); // get default webcam once
        if (webcam != null) {
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();
            startCameraLoop(webcam); // start updating preview and scanning
        } else {
            System.out.println("No camera available");
        }
    }


    /** Default camera for Windows/Linux */
    private void scanWithDefaultCamera() {
        Webcam webcam = Webcam.getDefault();
        startCameraLoop(webcam);
    }

    /** Common loop to handle live preview and barcode decoding */
    private void startCameraLoop(Webcam webcam) {
        if (webcam == null) {
            System.err.println("No camera available!");
            return;
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.open();
        AtomicBoolean scanned = new AtomicBoolean(false);

        Platform.runLater(() -> showCameraPreview(webcam, scanned));

        new Thread(() -> {
            while (!scanned.get()) {
                try {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        String code = decodeBarcode(image);
                        if (code != null) {
                            scanned.set(true);
                            Platform.runLater(() -> callback.onBarcodeScanned(code));
                        }
                    }
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
            webcam.close();
        }).start();
    }

    /** Decode barcode from image using ZXing */
    private String decodeBarcode(BufferedImage image) {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null; // no barcode found
        }
    }

    /** Display live camera preview */
    private void showCameraPreview(Webcam webcam, AtomicBoolean scanned) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Camera Barcode Scanner");

        ImageView preview = new ImageView();
        preview.setFitWidth(640);
        preview.setFitHeight(480);

        StackPane root = new StackPane(preview);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // Update preview in FX thread
        Thread updater = new Thread(() -> {
            while (!scanned.get()) {
                BufferedImage img = webcam.getImage();
                if (img != null) {
                    Image fxImage = SwingFXUtils.toFXImage(img, null);
                    Platform.runLater(() -> preview.setImage(fxImage));
                }
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
            Platform.runLater(stage::close);
        });
        updater.setDaemon(true);
        updater.start();
    }
}

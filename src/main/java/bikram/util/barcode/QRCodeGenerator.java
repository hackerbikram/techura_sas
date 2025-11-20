package bikram.util.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * 🌀 QR Code Generator Utility (UTF-8)
 * Saves all generated codes inside "src/qrcodes/" folder.
 */
public class QRCodeGenerator {

    private static final String BASE_DIR = "src/qrcodes/";

    /**
     * Generates a QR code PNG and saves it in src/qrcodes/
     *
     * @param data      The content encoded inside the QR (text, URL, ID, etc.)
     * @param filename  Name of the file (without extension)
     * @param size      Width/Height in pixels
     */
    public static void generateQRCode(String data, String filename, int size) {
        try {
            // Ensure the folder exists
            Path dirPath = FileSystems.getDefault().getPath(BASE_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Sanitize filename (no spaces or special chars)
            String safeName = filename.replaceAll("[^a-zA-Z0-9-_]", "_");
            Path filePath = FileSystems.getDefault().getPath(BASE_DIR + safeName + ".png");

            // QR encoding hints
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Create QR matrix
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(data, BarcodeFormat.QR_CODE, size, size, hints);

            // Save image
            MatrixToImageWriter.writeToPath(matrix, "png", filePath);

            System.out.println("✅ QR Code saved at: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Failed to generate QR Code: " + e.getMessage());
        }
    }

    /**
     * Quick helper: default 250x250 size
     */
    public static void generateDefault(String data, String filename) {
        generateQRCode(data, filename, 250);
    }
}

package bikram.util.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 🔳 Barcode Generator Utility
 * Generates barcodes (EAN_13, CODE_128, CODE_39, etc.)
 */
public class BarcodeGenerator {
    // Default folder to save barcodes
    private static final String BASE_DIR = "src/barcodes/";

    /**
     * Generates a barcode image and saves it as a PNG file.
     *
     * @param data    The data (text or number) encoded into the barcode
     * @param filename The filename to save (without extension)
     * @param format  The barcode format (e.g., BarcodeFormat.CODE_128)
     * @param width   Image width in pixels
     * @param height  Image height in pixels
     */
    public static void generateBarcode(String data, String filename, BarcodeFormat format, int width, int height) {
        try {
            // Ensure directory exists
            Path dirPath = FileSystems.getDefault().getPath(BASE_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Create file path (e.g., src/barcodes/Laptop.png)
            String sanitized = filename.replaceAll("[^a-zA-Z0-9-_]", "_");
            Path filePath = FileSystems.getDefault().getPath(BASE_DIR + sanitized + ".png");

            // Create the barcode
            BitMatrix matrix = new MultiFormatWriter().encode(data, format, width, height);

            // Write barcode image
            MatrixToImageWriter.writeToPath(matrix, "png", filePath);

            System.out.println("✅ Barcode saved at: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Failed to generate barcode: " + e.getMessage());
        }
    }

    /**
     * Quick helper for CODE_128 format (recommended)
     */
    public static void generateCode128(String data, String filename) {
        generateBarcode(data, filename, BarcodeFormat.CODE_128, 300, 100);
    }
}

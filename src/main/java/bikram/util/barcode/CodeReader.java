package bikram.util.barcode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 🧾 Code Reader Utility
 * Reads both QR codes and barcodes from images.
 */
public class CodeReader {

    public static String readCode(String filePath) {
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            System.out.println("✅ Decoded: " + result.getText());
            return result.getText();
        } catch (NotFoundException e) {
            System.err.println("❌ No code found in image.");
        } catch (Exception e) {
            System.err.println("❌ Failed to read code: " + e.getMessage());
        }
        return null;
    }
}

package bikram.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ConfirmDialog {
    public static boolean show(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult() == ButtonType.YES;
    }
}

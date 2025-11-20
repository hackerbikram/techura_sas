package bikram.util;

import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class FileActions {

    public static File showOpenDialog(Window owner, String... extensions) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open File");
        if (extensions != null && extensions.length > 0) {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Files", extensions));
        }
        return fc.showOpenDialog(owner);
    }

    public static File showSaveDialog(Window owner, String suggestedName, String... extensions) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save File");
        if (suggestedName != null) fc.setInitialFileName(suggestedName);
        if (extensions != null && extensions.length > 0) {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Files", extensions));
        }
        return fc.showSaveDialog(owner);
    }

    public static File showDirectoryChooser(Window owner) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Folder");
        return dc.showDialog(owner);
    }

    public static File defaultDesktopFile(String name) {
        String userHome = System.getProperty("user.home");
        File desktop = new File(userHome, "Desktop");
        if (!desktop.exists()) desktop = new File(userHome);
        return new File(desktop, name);
    }

    // Copy a source file to destination (used for import/export/save)
    public static boolean copyFile(File src, File dest) {
        try {
            Files.createDirectories(dest.getParentFile().toPath());
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Save raw bytes
    public static boolean saveBytes(File dest, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
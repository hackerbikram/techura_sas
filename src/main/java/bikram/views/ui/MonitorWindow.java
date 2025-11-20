package bikram.views.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Lightweight single-instance monitor window that updates every second.
 * Lazy-initialized and efficient.
 */
public final class MonitorWindow {

    private static MonitorWindow instance;
    private final Stage stage;
    private final Label cpuLabel = new Label();
    private final Label memLabel = new Label();
    private final Timeline updater;

    private MonitorWindow() {
        stage = new Stage(StageStyle.UTILITY);
        stage.initModality(Modality.NONE);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Live Monitor");

        VBox root = new VBox(8, cpuLabel, memLabel);
        root.setPadding(new Insets(8));
        root.getStyleClass().add("monitor-window");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(260);
        stage.setHeight(120);

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        updater = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
            // CPU load (if available) and memory quick snapshot
            String cpu = "CPU Load: " + formattedLoad(os);
            long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
            long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            String mem = String.format("Memory: %d MB free / %d MB total", free, total);
            cpuLabel.setText(cpu);
            memLabel.setText(mem);
        }));
        updater.setCycleCount(Timeline.INDEFINITE);

        // stop updates when window hidden
        stage.setOnHidden(e -> stop());
        stage.setOnShown(e -> start());
    }

    public static synchronized MonitorWindow getInstance() {
        if (instance == null) instance = new MonitorWindow();
        return instance;
    }

    public void show() {
        if (stage.isShowing()) {
            stage.toFront();
            return;
        }
        Platform.runLater(() -> {
            stage.show();
            start();
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            stage.hide();
            stop();
        });
    }

    public void start() {
        if (updater != null && updater.getStatus() != javafx.animation.Animation.Status.RUNNING) {
            updater.play();
        }
    }

    public void stop() {
        if (updater != null && updater.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            updater.stop();
        }
    }

    public void showPerformanceSnapshot() {
        // quick one-time snapshot shown via notification
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        String cpu = "CPU Load (avg): " + formattedLoad(os);
        long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        String mem = String.format("Memory: %d MB free / %d MB total", free, total);
        NotificationsManager.showNotification("Performance Snapshot", cpu + " | " + mem, NotificationsManager.NotificationType.INFO);
    }

    private static String formattedLoad(OperatingSystemMXBean os) {
        double load = os.getSystemLoadAverage();
        if (load < 0) return "n/a";
        return String.format("%.2f", load);
    }
}

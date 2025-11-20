package bikram.views.page;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager extends BorderPane {

    private final VBox taskList = new VBox(12);
    private final List<Task> tasks = new ArrayList<>();

    private static final String FILE_PATH = "tasks.json";
    private final Gson gson = new Gson();

    private ComboBox<String> filterCombo;
    private TextField searchField;

    public TaskManager() {

        setPadding(new Insets(20));
        setBackground(new Background(new BackgroundFill(Color.web("#f4f6fa"), CornerRadii.EMPTY, Insets.EMPTY)));

        //--------------------------------------------------------
        // TITLE
        //--------------------------------------------------------
        Label title = new Label("📌 タスクマネージャー");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#333"));

        //--------------------------------------------------------
        // TOP BAR (Add + Search + Filter)
        //--------------------------------------------------------
        VBox header = new VBox(15, title, createTopTools(), createAddTaskBox());
        header.setPadding(new Insets(10, 0, 20, 0));

        //--------------------------------------------------------
        // TASK LIST AREA
        //--------------------------------------------------------
        ScrollPane scrollPane = new ScrollPane(taskList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        setTop(header);
        setCenter(scrollPane);

        //--------------------------------------------------------
        // LOAD + DISPLAY
        //--------------------------------------------------------
        loadTasks();
        refreshUI();
    }

    // -----------------------------------------------------------
    // 🔍 SEARCH + FILTER TOOLS
    // -----------------------------------------------------------
    private HBox createTopTools() {

        // 🔍 Search Bar
        searchField = new TextField();
        searchField.setPromptText("🔍 Search tasks...");
        searchField.setPrefWidth(250);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshUI());

        // 🔽 Filter Combo
        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All", "Active", "Completed", "Overdue");
        filterCombo.setValue("All");

        filterCombo.valueProperty().addListener((obs, oldV, newV) -> refreshUI());

        HBox row = new HBox(15, searchField, filterCombo);
        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    // -----------------------------------------------------------
    // ➕ ADD TASK BOX
    // -----------------------------------------------------------
    private VBox createAddTaskBox() {

        TextField taskTitle = new TextField();
        taskTitle.setPromptText("タスク名");

        TextArea taskDesc = new TextArea();
        taskDesc.setPromptText("説明...");
        taskDesc.setPrefRowCount(2);

        DatePicker due = new DatePicker(LocalDate.now());

        ComboBox<String> priority = new ComboBox<>();
        priority.getItems().addAll("Low", "Medium", "High");
        priority.setValue("Medium");

        Button addBtn = new Button("＋追加");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> {
            if (!taskTitle.getText().isEmpty()) {
                tasks.add(new Task(
                        taskTitle.getText(),
                        taskDesc.getText(),
                        due.getValue(),
                        priority.getValue(),
                        false
                ));
                saveTasks();
                refreshUI();

                taskTitle.clear();
                taskDesc.clear();
                due.setValue(LocalDate.now());
                priority.setValue("Medium");
            }
        });

        HBox row = new HBox(10, taskTitle, taskDesc, due, priority, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(8, new Label("➕ New Task"), row);
        return box;
    }

    // -----------------------------------------------------------
    // REFRESH UI
    // -----------------------------------------------------------
    private void refreshUI() {

        taskList.getChildren().clear();

        List<Task> filtered = tasks.stream()
                .filter(this::filterTask)
                .filter(t -> t.getTitle().toLowerCase().contains(searchField.getText().toLowerCase()))
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());

        for (Task t : filtered) {
            taskList.getChildren().add(createTaskCard(t));
        }
    }

    // -----------------------------------------------------------
    // FILTER LOGIC
    // -----------------------------------------------------------
    private boolean filterTask(Task t) {

        switch (filterCombo.getValue()) {
            case "Active":
                return !t.isDone();
            case "Completed":
                return t.isDone();
            case "Overdue":
                return t.getDueDate().isBefore(LocalDate.now()) && !t.isDone();
            default:
                return true;
        }
    }

    // -----------------------------------------------------------
    // TASK CARD UI
    // -----------------------------------------------------------
    private VBox createTaskCard(Task task) {

        // TITLE + DELETE
        Label name = new Label(task.getTitle());
        name.setFont(Font.font("Poppins", FontWeight.BOLD, 18));

        Button delete = new Button("🗑");
        delete.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white;");
        delete.setOnAction(e -> {
            tasks.remove(task);
            saveTasks();
            refreshUI();
        });

        Button edit = new Button("✏");
        edit.setStyle("-fx-background-color: #5856D6; -fx-text-fill: white;");
        edit.setOnAction(e -> editTask(task));

        HBox top = new HBox(10, name, edit, delete);
        top.setAlignment(Pos.CENTER_LEFT);

        // DESCRIPTION
        Label desc = new Label(task.getDescription());
        desc.setWrapText(true);

        // DUE DATE
        Label due = new Label("📅 " + task.getDueDate());
        due.setFont(Font.font(13));

        // PRIORITY BADGE
        Label priority = new Label(" " + task.getPriority() + " ");
        priority.setFont(Font.font("Poppins", FontWeight.BOLD, 12));
        priority.setTextFill(Color.WHITE);
        priority.setStyle("-fx-background-radius: 6;");

        switch (task.getPriority()) {
            case "Low": priority.setStyle("-fx-background-color: #4CAF50;"); break;
            case "Medium": priority.setStyle("-fx-background-color: #FFC107;"); break;
            case "High": priority.setStyle("-fx-background-color: #FF3B30;"); break;
        }

        // COMPLETED CHECKBOX
        CheckBox done = new CheckBox("Completed");
        done.setSelected(task.isDone());
        done.selectedProperty().addListener((obs, o, n) -> {
            task.setDone(n);
            saveTasks();
        });

        VBox card = new VBox(8, top, desc, priority, due, done);
        card.setPadding(new Insets(12));
        card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        card.setEffect(new DropShadow(3, Color.gray(0.3)));
        card.setPrefWidth(650);

        // Smooth animation
        FadeTransition ft = new FadeTransition(Duration.millis(250), card);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        return card;
    }

    // -----------------------------------------------------------
    // TASK EDIT WINDOW
    // -----------------------------------------------------------
    private void editTask(Task task) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");

        TextField name = new TextField(task.getTitle());
        TextArea desc = new TextArea(task.getDescription());
        DatePicker date = new DatePicker(task.getDueDate());

        ComboBox<String> priority = new ComboBox<>();
        priority.getItems().addAll("Low", "Medium", "High");
        priority.setValue(task.getPriority());

        VBox box = new VBox(10,
                new Label("Title:"), name,
                new Label("Description:"), desc,
                new Label("Due date:"), date,
                new Label("Priority:"), priority
        );
        box.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                task.setTitle(name.getText());
                task.setDescription(desc.getText());
                task.setDueDate(date.getValue());
                task.setPriority(priority.getValue());
                saveTasks();
                refreshUI();
            }
        });
    }

    // -----------------------------------------------------------
    // SAVE & LOAD
    // -----------------------------------------------------------
    private void saveTasks() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadTasks() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<List<Task>>() {}.getType();
            List<Task> loaded = gson.fromJson(reader, type);
            if (loaded != null) tasks.addAll(loaded);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // -----------------------------------------------------------
    // TASK MODEL
    // -----------------------------------------------------------
    public static class Task {
        private String title;
        private String description;
        private LocalDate dueDate;
        private String priority;
        private boolean done;

        public Task(String t, String d, LocalDate date, String p, boolean done) {
            this.title = t;
            this.description = d;
            this.dueDate = date;
            this.priority = p;
            this.done = done;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDate getDueDate() { return dueDate; }
        public String getPriority() { return priority; }
        public boolean isDone() { return done; }

        public void setTitle(String t) { title = t; }
        public void setDescription(String d) { description = d; }
        public void setDueDate(LocalDate d) { dueDate = d; }
        public void setPriority(String p) { priority = p; }
        public void setDone(boolean d) { done = d; }
    }
}

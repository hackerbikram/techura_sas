package bikram.views.page;

import bikram.views.ui.NotificationsManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Collectors;

public class Notebook extends BorderPane {

    private VBox notesContainer;
    private TextArea activeNote;

    private ListView<String> savedNotesList;
    private Label statusLabel;

    private Button saveBtn;      // ⭐ Now accessible everywhere
    private Button editBtn;      // ⭐ New
    private Button deleteBtn;    // ⭐ New

    private String noteURL = "src/data/notes";

    public Notebook() {

        setPrefSize(1000, 650);
        setStyle("-fx-background-color: linear-gradient(to bottom right,#ffffff,#eaeaea);");

        //------------------------------------------------------------
        // HEADER
        //------------------------------------------------------------
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white; -fx-border-color:#ccc; -fx-border-width:0 0 1 0;");

        Label title = new Label("📘 テクラ ノート");
        title.setFont(Font.font("SF Pro Display", 26));

        Button newNoteBtn = createButton("＋ 新しいメモ", "#007AFF");
        TextField filename = new TextField();
        filename.setPromptText("📁ファイル名");

        saveBtn = createButton("💾 保存", "#34C759");
        editBtn = createButton("✏ 編集", "#5856D6");
        deleteBtn = createButton("🗑 削除", "#FF3B30");

        Button exportTxtBtn = createButton("📄 TXT 出力", "#34C759");
        Button exportPdfBtn = createButton("📕 PDF 出力", "#FF9500");

        header.getChildren().addAll(
                title, newNoteBtn, filename, saveBtn, editBtn, deleteBtn, exportTxtBtn, exportPdfBtn
        );

        //------------------------------------------------------------
        // LEFT SIDEBAR (Saved notes list)
        //------------------------------------------------------------
        savedNotesList = new ListView<>();
        savedNotesList.setPrefWidth(250);
        savedNotesList.setStyle("""
                -fx-background-color:#fefefe;
                -fx-border-color:#ddd;
                -fx-font-size:15px;
        """);

        Label savedLabel = new Label("📁 保存されたノート");
        savedLabel.setFont(Font.font(16));

        VBox savedBox = new VBox(10, savedLabel, savedNotesList);
        savedBox.setPadding(new Insets(15));
        savedBox.setStyle("-fx-background-color:white; -fx-border-color:#ccc; -fx-border-width:0 1 0 0;");

        setLeft(savedBox);

        //------------------------------------------------------------
        // CENTER AREA (Note Editor)
        //------------------------------------------------------------
        notesContainer = new VBox(15);
        notesContainer.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(notesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color:transparent;");

        //------------------------------------------------------------
        // STATUS BAR
        //------------------------------------------------------------
        statusLabel = new Label("準備完了");
        statusLabel.setPadding(new Insets(10));
        statusLabel.setStyle("-fx-background-color:white; -fx-border-color:#ccc; -fx-border-width:1 0 0 0;");

        //------------------------------------------------------------
        // Layout
        //------------------------------------------------------------
        setTop(header);
        setCenter(scrollPane);
        setBottom(statusLabel);

        //------------------------------------------------------------
        // BUTTON ACTIONS
        //------------------------------------------------------------
        newNoteBtn.setOnAction(e -> createNewNote());

        saveBtn.setOnAction(e -> {
            String name = filename.getText().trim();
            if (name.isEmpty()) {
                NotificationsManager.showNotification("名前エラー", "ファイル名を入力してください！",
                        NotificationsManager.NotificationType.WARNING);
                return;
            }
            saveNote(name);
            filename.clear();
            loadSavedNotes();
        });

        exportTxtBtn.setOnAction(e -> exportAsText());
        exportPdfBtn.setOnAction(e -> exportAsPDF());

        editBtn.setOnAction(e -> editSelectedNote());
        deleteBtn.setOnAction(e -> deleteSelectedNote());

        savedNotesList.setOnMouseClicked(e -> openSelectedNote());

        //------------------------------------------------------------
        // STARTUP
        //------------------------------------------------------------
        createNewNote();
        loadSavedNotes();
    }

    // ----------------------------------------------------------------
    // UI button creator
    // ----------------------------------------------------------------
    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font(15));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color:" + color + "; -fx-background-radius:12;");
        btn.setPadding(new Insets(12));
        btn.setEffect(new DropShadow(4, Color.rgb(0,0,0,0.18)));
        return btn;
    }

    // ----------------------------------------------------------------
    // Create new note
    // ----------------------------------------------------------------
    private void createNewNote() {
        TextArea note = new TextArea();
        note.setPromptText("ここにメモを書いてください...");
        note.setFont(Font.font(16));
        note.setWrapText(true);
        note.setPrefHeight(320);

        note.setStyle("""
                -fx-background-color:white;
                -fx-border-color:#ccc;
                -fx-border-radius:8;
                -fx-background-radius:8;
        """);

        note.setOnMouseClicked(e -> activeNote = note);
        notesContainer.getChildren().add(0, note);
        activeNote = note;

        showStatus("📝 新しいメモを作成しました");
    }

    // ----------------------------------------------------------------
    // Load saved list
    // ----------------------------------------------------------------
    private void loadSavedNotes() {
        try {
            Path folder = Paths.get(noteURL);
            if (!Files.exists(folder)) Files.createDirectories(folder);

            var files = Files.list(folder)
                    .filter(f -> f.getFileName().toString().endsWith(".txt"))
                    .map(f -> f.getFileName().toString())
                    .collect(Collectors.toList());

            savedNotesList.getItems().setAll(files);

            showStatus("📁 保存済みノート: " + files.size());

        } catch (Exception e) {
            showStatus("❌ エラー: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Open note
    // ----------------------------------------------------------------
    private void openSelectedNote() {
        String filename = savedNotesList.getSelectionModel().getSelectedItem();
        if (filename == null) return;

        try {
            Path path = Paths.get(noteURL, filename);
            String text = Files.readString(path);

            createNewNote();
            activeNote.setText(text);

            showStatus("📖 開く: " + filename);

        } catch (Exception e) {
            showStatus("❌ 読み込み失敗: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Save note
    // ----------------------------------------------------------------
    public void saveNote(String filename) {
        try {
            if (activeNote == null) return;

            Path dir = Paths.get(noteURL);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            filename = filename.replaceAll("[^a-zA-Z0-9-_]", "_");
            Path file = dir.resolve(filename + ".txt");

            Files.writeString(file, activeNote.getText(), StandardCharsets.UTF_8);

            showStatus("💾 保存成功: " + filename);

        } catch (Exception e) {
            showStatus("❌ 保存失敗: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Edit existing note
    // ----------------------------------------------------------------
    private void editSelectedNote() {
        String selected = savedNotesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("編集するノートを選択してください.");
            return;
        }

        try {
            Path path = Paths.get(noteURL, selected);
            String content = Files.readString(path);

            createNewNote();
            activeNote.setText(content);

            saveBtn.setOnAction(e -> saveEditedNote(selected));

            showStatus("✏ 編集中: " + selected);

        } catch (Exception e) {
            showAlert("読み込みエラー: " + e.getMessage());
        }
    }

    private void saveEditedNote(String filename) {
        try {
            Path file = Paths.get(noteURL, filename);
            Files.writeString(file, activeNote.getText());
            showStatus("更新されました: " + filename);

            loadSavedNotes();

        } catch (Exception e) {
            showStatus("保存失敗: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Delete note
    // ----------------------------------------------------------------
    private void deleteSelectedNote() {
        String selected = savedNotesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("削除するノートを選択してください!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("削除確認");
        confirm.setHeaderText("削除しますか？");
        confirm.setContentText(selected + " を削除します。");

        confirm.showAndWait().ifPresent(response -> {
            try {
                Path file = Paths.get(noteURL, selected);
                Files.deleteIfExists(file);

                loadSavedNotes();
                showStatus("🗑 削除しました: " + selected);

            } catch (Exception e) {
                showAlert("削除失敗: " + e.getMessage());
            }
        });
    }

    // ----------------------------------------------------------------
    // Export TXT
    // ----------------------------------------------------------------
    private void exportAsText() {
        if (activeNote == null) return;

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showSaveDialog(getScene().getWindow());

        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(activeNote.getText());
            showStatus("TXT 出力成功");
        } catch (Exception e) {
            showStatus("TXT出力失敗");
        }
    }

    // ----------------------------------------------------------------
    // Export PDF
    // ----------------------------------------------------------------
    private void exportAsPDF() {
        if (activeNote == null) return;

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = chooser.showSaveDialog(getScene().getWindow());

        if (file == null) return;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            PDPageContentStream stream = new PDPageContentStream(doc, page);
            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA, 12);
            stream.newLineAtOffset(50, 700);

            for (String line : activeNote.getText().split("\n")) {
                stream.showText(line);
                stream.newLineAtOffset(0, -15);
            }

            stream.endText();
            stream.close();

            doc.save(file);

            showStatus("PDF 出力成功!");

        } catch (Exception e) {
            showStatus("PDF 出力失敗");
        }
    }

    private void showStatus(String msg) {
        statusLabel.setText(msg);
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}

package bikram.views.ui;

import bikram.db.*;
import bikram.model.*;
import bikram.util.SearchService;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.List;

public class SerchBar extends VBox {
    private TextField searchField;
    private ListView<String> suggestionList;
    private final Popup suggestionPopup;

    private final ProductRepository pdb = new ProductDB();
    private final UserRepository udb = new UserDB();
    private final Salesrepository sdb = new SalesDB();

    private final SearchService searchService;

    public SerchBar() {
        List<Product> products = pdb.getAllProducts();
        List<User> users = udb.getAllUsers();

        searchService = new SearchService(products, users);

        searchField = new TextField();
        suggestionList = new ListView<>();
        suggestionPopup = new Popup();

        setupSearchField();
        setupSuggestionPopup();

        this.getChildren().add(searchField);
        this.setAlignment(Pos.CENTER_LEFT);
    }

    private void setupSearchField() {
        searchField.setPromptText("🔍 アプリ全体を検索...");
        searchField.setFont(Font.font("Poppins", 13));
        searchField.setPrefWidth(250);
        searchField.setMaxWidth(300);
        searchField.setStyle("""
            -fx-background-color: rgba(255,255,255,0.15);
            -fx-text-fill: white;
            -fx-prompt-text-fill: #AAAAAA;
            -fx-background-radius: 20;
            -fx-border-color: transparent;
            -fx-padding: 6 14;
        """);

        // Live search
        searchField.textProperty().addListener((obs, oldText, newText) -> handleSearch(newText.trim()));

        // Hide popup when pressing Enter
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                suggestionPopup.hide();
            }
        });

        animateSearchFocus();
    }

    private void setupSuggestionPopup() {
        suggestionList.setPrefHeight(150);
        suggestionList.setStyle("""
            -fx-background-color: rgba(30,30,30,0.95);
            -fx-text-fill: white;
            -fx-control-inner-background: transparent;
        """);

        suggestionList.setOnMouseClicked(e -> {
            String selected = suggestionList.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.startsWith("⚠️")) {
                searchField.setText(selected);
                suggestionPopup.hide();
            }
        });

        suggestionPopup.getContent().add(suggestionList);
    }

    private void animateSearchFocus() {
        searchField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                searchField.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.18);
                    -fx-text-fill: white;
                    -fx-background-radius: 20;
                    -fx-border-color: #00e5ff;
                    -fx-border-width: 1.3;
                    -fx-prompt-text-fill: #AAAAAA;
                    -fx-padding: 6 14;
                """);
            } else {
                searchField.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.15);
                    -fx-text-fill: white;
                    -fx-background-radius: 20;
                    -fx-border-color: transparent;
                    -fx-padding: 6 14;
                """);
                suggestionPopup.hide();
            }
        });
    }

    private void handleSearch(String query) {
        if (query.isEmpty()) {
            suggestionPopup.hide();
            return;
        }

        List<String> results = searchService.search(query);
        if (results.isEmpty()) {
            suggestionPopup.hide();
            return;
        }

        suggestionList.setItems(FXCollections.observableArrayList(results));

        // Show popup below the search bar
        if (!suggestionPopup.isShowing()) {
            suggestionPopup.show(searchField,
                    searchField.localToScreen(searchField.getBoundsInLocal()).getMinX(),
                    searchField.localToScreen(searchField.getBoundsInLocal()).getMaxY());
        }
    }

    public void setOnAction() {}
}

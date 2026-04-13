package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import photos.Photos;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Controller for the search UI preview screen.
 */
public class SearchController {
    private Photos app;
    private String currentUsername;

    @FXML
    private Label headingLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private RadioButton dateSearchRadio;

    @FXML
    private RadioButton tagSearchRadio;

    @FXML
    private ToggleGroup searchModeToggleGroup;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private VBox dateSearchPane;

    @FXML
    private VBox tagSearchPane;

    @FXML
    private ComboBox<String> firstTagTypeComboBox;

    @FXML
    private TextField firstTagValueField;

    @FXML
    private ComboBox<String> secondTagTypeComboBox;

    @FXML
    private TextField secondTagValueField;

    @FXML
    private RadioButton andRadio;

    @FXML
    private RadioButton orRadio;

    @FXML
    private ToggleGroup tagOperatorToggleGroup;

    @FXML
    private ListView<String> resultsListView;

    @FXML
    private Label resultsStatusLabel;

    public void setApp(Photos app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        firstTagTypeComboBox.getItems().setAll("location", "person", "event", "year");
        secondTagTypeComboBox.getItems().setAll("location", "person", "event", "year");
        firstTagTypeComboBox.getSelectionModel().selectFirst();
        secondTagTypeComboBox.getSelectionModel().select(1);

        startDatePicker.setValue(LocalDate.of(2026, 1, 1));
        endDatePicker.setValue(LocalDate.of(2026, 1, 31));

        resultsListView.setCellFactory(listView -> new SearchResultCell());
        resultsListView.getItems().setAll(
                "Beach-Sunrise.jpg | Family Trip | Jan 3, 2026",
                "Museum-Day.png | Family Trip | Jan 5, 2026",
                "Dinner-Night.jpg | Favorites | Jan 9, 2026"
        );

        searchModeToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> updateSearchModeView());
        updateSearchModeView();
    }

    public void setUsername(String username) {
        currentUsername = username;
        headingLabel.setText("Search Photos");
        subtitleLabel.setText("Search UI only for " + username + ". Controls and results are sample placeholders.");
    }

    @FXML
    private void handleBackToAlbums() {
        try {
            app.showAlbumsView(currentUsername);
        } catch (IOException exception) {
            resultsStatusLabel.setText("Unable to return to the albums screen.");
        }
    }

    @FXML
    private void handleRunSearch() {
        String searchMode = dateSearchRadio.isSelected() ? "date search" : "tag search";
        showPlaceholderMessage("Run Search", "Search UI is complete, but actual " + searchMode
                + " behavior belongs to the next logic milestone.");
    }

    @FXML
    private void handleCreateAlbumFromResults() {
        TextInputDialog dialog = new TextInputDialog("Search Results");
        dialog.setTitle("Create Album From Results");
        dialog.setHeaderText("Create album from results UI");
        dialog.setContentText("Album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            showPlaceholderMessage("Create Album From Results", "UI preview only. A real album named '"
                    + result.get().trim() + "' would be created from the current sample search results later.");
        }
    }

    private void updateSearchModeView() {
        boolean dateMode = dateSearchRadio.isSelected();
        dateSearchPane.setVisible(dateMode);
        dateSearchPane.setManaged(dateMode);
        tagSearchPane.setVisible(!dateMode);
        tagSearchPane.setManaged(!dateMode);
        resultsStatusLabel.setText(dateMode
                ? "Showing sample results for date-range search."
                : "Showing sample results for tag-based search.");
    }

    private void showPlaceholderMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("UI placeholder");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class SearchResultCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            String[] parts = item.split("\\|", 3);

            Label fileNameLabel = new Label(parts.length > 0 ? parts[0].trim() : item);
            fileNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label albumLabel = new Label(parts.length > 1 ? "Album: " + parts[1].trim() : "");
            albumLabel.setStyle("-fx-text-fill: #4b5563;");

            Label dateLabel = new Label(parts.length > 2 ? "Date: " + parts[2].trim() : "");
            dateLabel.setStyle("-fx-text-fill: #4b5563;");

            setText(null);
            setGraphic(new VBox(4.0, fileNameLabel, albumLabel, dateLabel));
        }
    }
}

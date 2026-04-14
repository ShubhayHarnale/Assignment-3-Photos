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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import photos.Photos;
import photos.model.Photo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SearchController {
    private Photos app;
    private String currentUsername;
    private List<Photo> currentResults = List.of();

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
    private ListView<Photo> resultsListView;

    @FXML
    private Label resultsStatusLabel;

    public void setApp(Photos app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        firstTagTypeComboBox.setEditable(true);
        secondTagTypeComboBox.setEditable(true);
        resultsListView.setCellFactory(listView -> new SearchResultCell());
        searchModeToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> updateSearchModeView());
        updateSearchModeView();
    }

    public void setUsername(String username) {
        currentUsername = username;
        headingLabel.setText("Search Photos");
        subtitleLabel.setText("Search photos by date range or by up to two tag-value pairs.");
        refreshTagTypeChoices();
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
        if (dateSearchRadio.isSelected()) {
            runDateSearch();
        } else {
            runTagSearch();
        }
    }

    @FXML
    private void handleCreateAlbumFromResults() {
        if (currentResults.isEmpty()) {
            showError("Create Album From Results", "Run a search with at least one result first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Search Results");
        dialog.setTitle("Create Album From Results");
        dialog.setHeaderText("Create album from current search results");
        dialog.setContentText("Album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String albumName = result.get().trim();
        if (albumName.isEmpty()) {
            showError("Create Album From Results", "Enter an album name.");
            return;
        }

        boolean created = app.createAlbumFromPhotos(currentUsername, albumName, currentResults);
        if (!created) {
            showError("Create Album From Results", "Album names must be unique for this user.");
            return;
        }

        resultsStatusLabel.setText("Created album '" + albumName + "' from " + currentResults.size() + " result(s).");
    }

    private void updateSearchModeView() {
        boolean dateMode = dateSearchRadio.isSelected();
        dateSearchPane.setVisible(dateMode);
        dateSearchPane.setManaged(dateMode);
        tagSearchPane.setVisible(!dateMode);
        tagSearchPane.setManaged(!dateMode);
        resultsStatusLabel.setText(dateMode
                ? "Run a date-range search to see matching photos."
                : "Run a tag search to see matching photos.");
    }

    private void runDateSearch() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if (startDate == null || endDate == null) {
            showError("Run Search", "Select both a start date and an end date.");
            return;
        }
        if (endDate.isBefore(startDate)) {
            showError("Run Search", "The end date must be on or after the start date.");
            return;
        }

        currentResults = app.searchPhotosByDate(currentUsername, startDate, endDate);
        resultsListView.getItems().setAll(currentResults);
        resultsStatusLabel.setText("Found " + currentResults.size() + " photo(s) between "
                + startDate + " and " + endDate + ".");
    }

    private void runTagSearch() {
        String firstTagType = getComboBoxValue(firstTagTypeComboBox);
        String firstTagValue = firstTagValueField.getText() == null ? "" : firstTagValueField.getText().trim();
        String secondTagType = getComboBoxValue(secondTagTypeComboBox);
        String secondTagValue = secondTagValueField.getText() == null ? "" : secondTagValueField.getText().trim();

        if (firstTagType.isBlank() || firstTagValue.isBlank()) {
            showError("Run Search", "Enter the first tag type and value.");
            return;
        }

        boolean hasSecondTagInput = !secondTagType.isBlank() || !secondTagValue.isBlank();
        if (hasSecondTagInput && (secondTagType.isBlank() || secondTagValue.isBlank())) {
            showError("Run Search", "Enter both the second tag type and value, or leave both blank.");
            return;
        }

        currentResults = app.searchPhotosByTags(
                currentUsername,
                firstTagType,
                firstTagValue,
                secondTagType,
                secondTagValue,
                andRadio.isSelected()
        );
        resultsListView.getItems().setAll(currentResults);
        resultsStatusLabel.setText("Found " + currentResults.size() + " photo(s) matching the selected tag search.");
    }

    private String getComboBoxValue(ComboBox<String> comboBox) {
        String editorValue = comboBox.getEditor().getText();
        if (editorValue != null && !editorValue.isBlank()) {
            return editorValue.trim();
        }

        String selectedValue = comboBox.getValue();
        return selectedValue == null ? "" : selectedValue.trim();
    }

    private void refreshTagTypeChoices() {
        List<String> tagTypes = app.getTagTypes(currentUsername);
        if (tagTypes.isEmpty()) {
            return;
        }

        firstTagTypeComboBox.getItems().setAll(tagTypes);
        secondTagTypeComboBox.getItems().setAll(tagTypes);
        firstTagTypeComboBox.getSelectionModel().selectFirst();
        if (tagTypes.size() > 1) {
            secondTagTypeComboBox.getSelectionModel().select(1);
        } else {
            secondTagTypeComboBox.getSelectionModel().selectFirst();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Unable to complete action");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class SearchResultCell extends ListCell<Photo> {
        @Override
        protected void updateItem(Photo item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Path path = Path.of(item.getFilePath());
            Path fileName = path.getFileName();

            ImageView thumbnailView = new ImageView();
            thumbnailView.setFitWidth(64);
            thumbnailView.setFitHeight(64);
            thumbnailView.setPreserveRatio(true);
            thumbnailView.setSmooth(true);

            try {
                Image thumbnail = new Image(new File(item.getFilePath()).toURI().toString(), 64, 64, true, true, true);
                if (!thumbnail.isError()) {
                    thumbnailView.setImage(thumbnail);
                }
            } catch (RuntimeException exception) {
                thumbnailView.setImage(null);
            }

            Label fileNameLabel = new Label(fileName == null ? item.getFilePath() : fileName.toString());
            fileNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label captionLabel = new Label(item.getCaption().isBlank() ? "Caption: -" : "Caption: " + item.getCaption());
            captionLabel.setStyle("-fx-text-fill: #4b5563;");
            captionLabel.setWrapText(true);

            Label dateLabel = new Label("Date: " + app.formatPhotoDate(item.getDateTaken()));
            dateLabel.setStyle("-fx-text-fill: #4b5563;");

            setText(null);
            setGraphic(new HBox(10.0, thumbnailView, new VBox(4.0, fileNameLabel, captionLabel, dateLabel)));
        }
    }
}

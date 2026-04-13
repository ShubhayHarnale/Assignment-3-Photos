package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import photos.Photos;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the album contents UI preview screen.
 */
public class AlbumContentsController {
    private Photos app;
    private String currentUsername;

    @FXML
    private Label albumTitleLabel;

    @FXML
    private Label albumSubtitleLabel;

    @FXML
    private ListView<SamplePhoto> photosListView;

    @FXML
    private Label previewTitleLabel;

    @FXML
    private Label captionValueLabel;

    @FXML
    private Label dateValueLabel;

    @FXML
    private Label tagsValueLabel;

    @FXML
    private Label previewStatusLabel;

    @FXML
    private StackPane previewPane;

    @FXML
    private TextArea captionEditor;

    @FXML
    private ListView<String> tagsListView;

    @FXML
    private ComboBox<String> tagTypeComboBox;

    @FXML
    private TextField tagValueField;

    public void setApp(Photos app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        photosListView.setCellFactory(listView -> new SamplePhotoCell());
        photosListView.getItems().setAll(
                new SamplePhoto("Beach-Sunrise.jpg", "Sunrise at the boardwalk", "Jan 3, 2026",
                        Arrays.asList("location = Miami", "person = Ava")),
                new SamplePhoto("Museum-Day.png", "Midday museum visit", "Jan 5, 2026",
                        Arrays.asList("location = Boston", "person = Chris")),
                new SamplePhoto("Dinner-Night.jpg", "Rooftop dinner with friends", "Jan 9, 2026",
                        Arrays.asList("location = New York", "person = Sam"))
        );

        photosListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updatePreview(newValue));
        photosListView.getSelectionModel().selectFirst();
        tagsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                previewStatusLabel.setText("Selected sample tag '" + newValue + "'.");
            }
        });

        Rectangle previewFrame = new Rectangle(320, 220);
        previewFrame.setArcWidth(18);
        previewFrame.setArcHeight(18);
        previewFrame.setStyle("-fx-fill: linear-gradient(to bottom right, #dbeafe, #e5e7eb); -fx-stroke: #94a3b8;");
        previewPane.getChildren().add(previewFrame);

        tagTypeComboBox.getItems().setAll("location", "person", "event", "year");
        tagTypeComboBox.getSelectionModel().selectFirst();
    }

    public void setContext(String username, String albumName) {
        currentUsername = username;
        albumTitleLabel.setText(albumName);
        albumSubtitleLabel.setText("Album contents UI only for " + username + ". Photo data and actions are sample placeholders.");
        previewStatusLabel.setText("Selected sample photo preview for '" + albumName + "'.");
    }

    @FXML
    private void handleBackToAlbums() {
        try {
            app.showAlbumsView(currentUsername);
        } catch (IOException exception) {
            previewStatusLabel.setText("Unable to return to the albums screen.");
        }
    }

    @FXML
    private void handlePreviousPhoto() {
        int currentIndex = photosListView.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            photosListView.getSelectionModel().select(currentIndex - 1);
            photosListView.scrollTo(currentIndex - 1);
            return;
        }

        showPlaceholderMessage("Previous Photo", "You are already on the first sample photo.");
    }

    @FXML
    private void handleNextPhoto() {
        int currentIndex = photosListView.getSelectionModel().getSelectedIndex();
        if (currentIndex < photosListView.getItems().size() - 1) {
            photosListView.getSelectionModel().select(currentIndex + 1);
            photosListView.scrollTo(currentIndex + 1);
            return;
        }

        showPlaceholderMessage("Next Photo", "You are already on the last sample photo.");
    }

    @FXML
    private void handleAddPhoto() {
        showPlaceholderMessage("Add Photo", "Add photo is a UI placeholder in this milestone.");
    }

    @FXML
    private void handleRemovePhoto() {
        showPlaceholderMessage("Remove Photo", "Remove photo is a UI placeholder in this milestone.");
    }

    @FXML
    private void handleCopyPhoto() {
        showPlaceholderMessage("Copy Photo", "Copy photo is a UI placeholder in this milestone.");
    }

    @FXML
    private void handleMovePhoto() {
        showPlaceholderMessage("Move Photo", "Move photo is a UI placeholder in this milestone.");
    }

    @FXML
    private void handleSaveCaption() {
        showPlaceholderMessage("Save Caption", "Caption editing UI is present, but saving caption data belongs to the next logic milestone.\n\nCurrent input: "
                + captionEditor.getText().trim());
    }

    @FXML
    private void handleAddTag() {
        String tagType = tagTypeComboBox.getValue() == null ? "" : tagTypeComboBox.getValue();
        String tagValue = tagValueField.getText() == null ? "" : tagValueField.getText().trim();
        showPlaceholderMessage("Add Tag", "Tag editing UI is present, but adding tags belongs to the next logic milestone.\n\nType: "
                + tagType + "\nValue: " + tagValue);
    }

    @FXML
    private void handleDeleteTag() {
        String selectedTag = tagsListView.getSelectionModel().getSelectedItem();
        if (selectedTag == null) {
            showPlaceholderMessage("Delete Tag", "Select a sample tag to preview the delete-tag flow.");
            return;
        }

        showPlaceholderMessage("Delete Tag", "Tag delete UI is present, but removing tags belongs to the next logic milestone.\n\nSelected tag: "
                + selectedTag);
    }

    private void updatePreview(SamplePhoto selectedPhoto) {
        if (selectedPhoto == null) {
            previewTitleLabel.setText("No photo selected");
            captionValueLabel.setText("-");
            dateValueLabel.setText("-");
            tagsValueLabel.setText("-");
            captionEditor.clear();
            tagsListView.getItems().clear();
            previewStatusLabel.setText("Select a sample photo to preview details.");
            return;
        }

        previewTitleLabel.setText(selectedPhoto.fileName());
        captionValueLabel.setText(selectedPhoto.caption());
        dateValueLabel.setText(selectedPhoto.dateTaken());
        tagsValueLabel.setText(String.join(", ", selectedPhoto.tags()));
        captionEditor.setText(selectedPhoto.caption());
        tagsListView.getItems().setAll(selectedPhoto.tags());
        tagsListView.getSelectionModel().clearSelection();
        tagValueField.clear();
        tagTypeComboBox.getSelectionModel().selectFirst();
        previewStatusLabel.setText("Previewing sample photo '" + selectedPhoto.fileName() + "'.");
    }

    private void showPlaceholderMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("UI placeholder");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record SamplePhoto(String fileName, String caption, String dateTaken, List<String> tags) {
    }

    private static class SamplePhotoCell extends ListCell<SamplePhoto> {
        @Override
        protected void updateItem(SamplePhoto item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label fileNameLabel = new Label(item.fileName());
            fileNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label captionLabel = new Label(item.caption());
            captionLabel.setStyle("-fx-text-fill: #4b5563;");

            setText(null);
            setGraphic(new javafx.scene.layout.VBox(4.0, fileNameLabel, captionLabel));
        }
    }
}

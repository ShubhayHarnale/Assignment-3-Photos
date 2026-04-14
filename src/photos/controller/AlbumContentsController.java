package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import photos.Photos;
import photos.model.Photo;
import photos.model.Tag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for the album contents screen.
 */
public class AlbumContentsController {
    private Photos app;
    private String currentUsername;
    private String currentAlbumName;

    @FXML
    private Label albumTitleLabel;

    @FXML
    private Label albumSubtitleLabel;

    @FXML
    private ListView<Photo> photosListView;

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
        photosListView.setCellFactory(listView -> new PhotoCell());
        photosListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updatePreview(newValue));
        tagsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                previewStatusLabel.setText("Selected tag '" + newValue + "'.");
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
        currentAlbumName = albumName;
        albumTitleLabel.setText(albumName);
        albumSubtitleLabel.setText("Add and remove photos for " + username + ".");
        refreshPhotos();
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

        showError("Previous Photo", "You are already on the first photo.");
    }

    @FXML
    private void handleNextPhoto() {
        int currentIndex = photosListView.getSelectionModel().getSelectedIndex();
        if (currentIndex >= 0 && currentIndex < photosListView.getItems().size() - 1) {
            photosListView.getSelectionModel().select(currentIndex + 1);
            photosListView.scrollTo(currentIndex + 1);
            return;
        }

        showError("Next Photo", "You are already on the last photo.");
    }

    @FXML
    private void handleAddPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(previewPane.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        String errorMessage = app.addPhotoToAlbum(currentUsername, currentAlbumName, selectedFile);
        if (errorMessage != null) {
            showError("Add Photo", errorMessage);
            return;
        }

        refreshPhotos();
        selectPhoto(selectedFile.toPath().toAbsolutePath().normalize().toString());
        previewStatusLabel.setText("Photo added to '" + currentAlbumName + "'.");
    }

    @FXML
    private void handleRemovePhoto() {
        Photo selectedPhoto = getSelectedPhotoOrShowError("Remove Photo", "Select a photo to remove.");
        if (selectedPhoto == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove Photo");
        confirmation.setHeaderText("Remove selected photo?");
        confirmation.setContentText("Remove '" + getFileName(selectedPhoto) + "' from this album?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        boolean removed = app.removePhotoFromAlbum(currentUsername, currentAlbumName, selectedPhoto.getFilePath());
        if (!removed) {
            showError("Remove Photo", "Unable to remove the selected photo.");
            return;
        }

        refreshPhotos();
        previewStatusLabel.setText("Photo removed from '" + currentAlbumName + "'.");
    }

    @FXML
    private void handleCopyPhoto() {
        showPlaceholderMessage("Copy Photo", "Copy photo belongs to a later milestone.");
    }

    @FXML
    private void handleMovePhoto() {
        showPlaceholderMessage("Move Photo", "Move photo belongs to a later milestone.");
    }

    @FXML
    private void handleSaveCaption() {
        showPlaceholderMessage("Save Caption", "Caption editing belongs to a later milestone.");
    }

    @FXML
    private void handleAddTag() {
        showPlaceholderMessage("Add Tag", "Tag editing belongs to a later milestone.");
    }

    @FXML
    private void handleDeleteTag() {
        showPlaceholderMessage("Delete Tag", "Tag editing belongs to a later milestone.");
    }

    private void refreshPhotos() {
        List<Photo> photos = app.getAlbumPhotos(currentUsername, currentAlbumName);
        photosListView.getItems().setAll(photos);

        if (photos.isEmpty()) {
            updatePreview(null);
            previewStatusLabel.setText("No photos in this album yet.");
            return;
        }

        photosListView.getSelectionModel().selectFirst();
    }

    private Photo getSelectedPhotoOrShowError(String title, String message) {
        Photo selectedPhoto = photosListView.getSelectionModel().getSelectedItem();
        if (selectedPhoto == null) {
            showError(title, message);
        }
        return selectedPhoto;
    }

    private void selectPhoto(String normalizedPath) {
        for (Photo photo : photosListView.getItems()) {
            if (photo.getFilePath().equalsIgnoreCase(normalizedPath)) {
                photosListView.getSelectionModel().select(photo);
                photosListView.scrollTo(photo);
                break;
            }
        }
    }

    private void updatePreview(Photo selectedPhoto) {
        if (selectedPhoto == null) {
            previewTitleLabel.setText("No photo selected");
            captionValueLabel.setText("-");
            dateValueLabel.setText("-");
            tagsValueLabel.setText("-");
            captionEditor.clear();
            tagsListView.getItems().clear();
            previewStatusLabel.setText("Select a photo to preview details.");
            return;
        }

        List<String> tagStrings = selectedPhoto.getTags().stream()
                .map(Tag::toString)
                .collect(Collectors.toList());

        previewTitleLabel.setText(getFileName(selectedPhoto));
        captionValueLabel.setText(selectedPhoto.getCaption().isBlank() ? "-" : selectedPhoto.getCaption());
        dateValueLabel.setText(app.formatPhotoDate(selectedPhoto.getDateTaken()));
        tagsValueLabel.setText(tagStrings.isEmpty() ? "-" : String.join(", ", tagStrings));
        captionEditor.setText(selectedPhoto.getCaption());
        tagsListView.getItems().setAll(tagStrings);
        tagsListView.getSelectionModel().clearSelection();
        tagValueField.clear();
        tagTypeComboBox.getSelectionModel().selectFirst();
        previewStatusLabel.setText("Previewing '" + getFileName(selectedPhoto) + "'.");
    }

    private String getFileName(Photo photo) {
        Path path = Path.of(photo.getFilePath());
        Path fileName = path.getFileName();
        return fileName == null ? photo.getFilePath() : fileName.toString();
    }

    private void showPlaceholderMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Not available yet");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Unable to complete action");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class PhotoCell extends ListCell<Photo> {
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

            Label fileNameLabel = new Label(fileName == null ? item.getFilePath() : fileName.toString());
            fileNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label pathLabel = new Label(item.getFilePath());
            pathLabel.setStyle("-fx-text-fill: #4b5563;");
            pathLabel.setWrapText(true);

            setText(null);
            setGraphic(new javafx.scene.layout.VBox(4.0, fileNameLabel, pathLabel));
        }
    }
}

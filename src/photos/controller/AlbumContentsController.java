package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

public class AlbumContentsController {
    private Photos app;
    private String currentUsername;
    private String currentAlbumName;
    private ImageView previewImageView;
    private Label previewFallbackLabel;

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

        previewImageView = new ImageView();
        previewImageView.setFitWidth(520);
        previewImageView.setFitHeight(260);
        previewImageView.setPreserveRatio(true);
        previewImageView.setSmooth(true);

        previewFallbackLabel = new Label("No image loaded");
        previewFallbackLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 16px;");

        previewPane.getChildren().setAll(previewFrame, previewImageView, previewFallbackLabel);
        tagTypeComboBox.setEditable(true);
    }

    public void setContext(String username, String albumName) {
        currentUsername = username;
        currentAlbumName = albumName;
        albumTitleLabel.setText(albumName);
        albumSubtitleLabel.setText("Add, organize, and edit the photos in this album.");
        refreshTagTypeChoices();
        refreshPhotos(null);
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

        String normalizedPath = selectedFile.toPath().toAbsolutePath().normalize().toString();
        refreshPhotos(normalizedPath);
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

        refreshPhotos(null);
        previewStatusLabel.setText("Photo removed from '" + currentAlbumName + "'.");
    }

    @FXML
    private void handleCopyPhoto() {
        Photo selectedPhoto = getSelectedPhotoOrShowError("Copy Photo", "Select a photo to copy.");
        if (selectedPhoto == null) {
            return;
        }

        String targetAlbum = chooseTargetAlbum("Copy Photo");
        if (targetAlbum == null) {
            return;
        }

        String errorMessage = app.copyPhotoToAlbum(currentUsername, currentAlbumName, targetAlbum, selectedPhoto.getFilePath());
        if (errorMessage != null) {
            showError("Copy Photo", errorMessage);
            return;
        }

        previewStatusLabel.setText("Copied photo to '" + targetAlbum + "'.");
    }

    @FXML
    private void handleMovePhoto() {
        Photo selectedPhoto = getSelectedPhotoOrShowError("Move Photo", "Select a photo to move.");
        if (selectedPhoto == null) {
            return;
        }

        String targetAlbum = chooseTargetAlbum("Move Photo");
        if (targetAlbum == null) {
            return;
        }

        String selectedPath = selectedPhoto.getFilePath();
        String errorMessage = app.movePhotoToAlbum(currentUsername, currentAlbumName, targetAlbum, selectedPath);
        if (errorMessage != null) {
            showError("Move Photo", errorMessage);
            return;
        }

        refreshPhotos(null);
        previewStatusLabel.setText("Moved photo to '" + targetAlbum + "'.");
    }

    @FXML
    private void handleSaveCaption() {
        Photo selectedPhoto = getSelectedPhotoOrShowError("Save Caption", "Select a photo to edit its caption.");
        if (selectedPhoto == null) {
            return;
        }

        String errorMessage = app.updatePhotoCaption(
                currentUsername,
                currentAlbumName,
                selectedPhoto.getFilePath(),
                captionEditor.getText()
        );
        if (errorMessage != null) {
            showError("Save Caption", errorMessage);
            return;
        }

        refreshPhotos(selectedPhoto.getFilePath());
        previewStatusLabel.setText("Caption updated for '" + getFileName(selectedPhoto) + "'.");
    }

    @FXML
    private void handleAddTag() {
        Photo selectedPhoto = getSelectedPhotoOrShowError("Add Tag", "Select a photo to tag.");
        if (selectedPhoto == null) {
            return;
        }

        String tagType = getTagTypeInput();
        String tagValue = tagValueField.getText() == null ? "" : tagValueField.getText().trim();
        if (tagType.isBlank() || tagValue.isBlank()) {
            showError("Add Tag", "Enter both a tag type and a tag value.");
            return;
        }

        boolean singleValueIfNew = false;
        if (!app.getTagTypes(currentUsername).stream().anyMatch(existing -> existing.equalsIgnoreCase(tagType))) {
            Optional<Boolean> createMode = chooseNewTagTypeMode(tagType);
            if (createMode.isEmpty()) {
                return;
            }
            singleValueIfNew = createMode.get();
        }

        String errorMessage = app.addTagToPhoto(
                currentUsername,
                currentAlbumName,
                selectedPhoto.getFilePath(),
                tagType,
                tagValue,
                singleValueIfNew
        );
        if (errorMessage != null) {
            showError("Add Tag", errorMessage);
            return;
        }

        refreshTagTypeChoices();
        refreshPhotos(selectedPhoto.getFilePath());
        previewStatusLabel.setText("Added tag '" + tagType.trim() + " = " + tagValue + "'.");
    }

    @FXML
    private void handleDeleteTag() {
        Photo selectedPhoto = getSelectedPhotoOrShowError("Delete Tag", "Select a photo first.");
        if (selectedPhoto == null) {
            return;
        }

        String selectedTag = tagsListView.getSelectionModel().getSelectedItem();
        if (selectedTag == null) {
            showError("Delete Tag", "Select a tag to delete.");
            return;
        }

        String[] parts = selectedTag.split("=", 2);
        if (parts.length != 2) {
            showError("Delete Tag", "The selected tag could not be parsed.");
            return;
        }

        boolean removed = app.removeTagFromPhoto(
                currentUsername,
                currentAlbumName,
                selectedPhoto.getFilePath(),
                parts[0].trim(),
                parts[1].trim()
        );
        if (!removed) {
            showError("Delete Tag", "Unable to remove the selected tag.");
            return;
        }

        refreshPhotos(selectedPhoto.getFilePath());
        previewStatusLabel.setText("Removed tag '" + selectedTag + "'.");
    }

    private void refreshPhotos(String preferredPhotoPath) {
        List<Photo> photos = app.getAlbumPhotos(currentUsername, currentAlbumName);
        photosListView.getItems().setAll(photos);

        if (photos.isEmpty()) {
            updatePreview(null);
            previewStatusLabel.setText("No photos in this album yet.");
            return;
        }

        if (preferredPhotoPath != null) {
            selectPhoto(preferredPhotoPath);
        }

        if (photosListView.getSelectionModel().getSelectedItem() == null) {
            photosListView.getSelectionModel().selectFirst();
        }
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

    private void refreshTagTypeChoices() {
        List<String> tagTypes = app.getTagTypes(currentUsername);
        tagTypeComboBox.getItems().setAll(tagTypes);
        if (!tagTypes.isEmpty()) {
            tagTypeComboBox.getSelectionModel().selectFirst();
        } else {
            tagTypeComboBox.getSelectionModel().clearSelection();
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
            previewImageView.setImage(null);
            previewFallbackLabel.setVisible(true);
            previewStatusLabel.setText("Select a photo to preview details.");
            return;
        }

        List<String> tagStrings = selectedPhoto.getTags().stream()
                .sorted((left, right) -> {
                    int nameComparison = left.getType().compareToIgnoreCase(right.getType());
                    if (nameComparison != 0) {
                        return nameComparison;
                    }
                    return left.getValue().compareToIgnoreCase(right.getValue());
                })
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
        refreshTagTypeChoices();
        loadPreviewImage(selectedPhoto.getFilePath());
        previewStatusLabel.setText("Previewing '" + getFileName(selectedPhoto) + "'.");
    }

    private void loadPreviewImage(String filePath) {
        try {
            Image image = new Image(new File(filePath).toURI().toString(), 520, 260, true, true, true);
            if (image.isError()) {
                previewImageView.setImage(null);
                previewFallbackLabel.setText("Unable to load image preview");
                previewFallbackLabel.setVisible(true);
                return;
            }

            previewImageView.setImage(image);
            previewFallbackLabel.setVisible(false);
        } catch (RuntimeException exception) {
            previewImageView.setImage(null);
            previewFallbackLabel.setText("Unable to load image preview");
            previewFallbackLabel.setVisible(true);
        }
    }

    private String chooseTargetAlbum(String title) {
        List<String> albumNames = app.getAlbumNames(currentUsername).stream()
                .filter(albumName -> !albumName.equalsIgnoreCase(currentAlbumName))
                .toList();

        if (albumNames.isEmpty()) {
            showError(title, "Create another album first.");
            return null;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(albumNames.getFirst(), albumNames);
        dialog.setTitle(title);
        dialog.setHeaderText("Choose a target album");
        dialog.setContentText("Album:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private Optional<Boolean> chooseNewTagTypeMode(String tagType) {
        ButtonType singleValueButton = new ButtonType("Single Value");
        ButtonType multiValueButton = new ButtonType("Multiple Values");
        ButtonType cancelButton = ButtonType.CANCEL;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Create Tag Type");
        alert.setHeaderText("Create new tag type '" + tagType.trim() + "'?");
        alert.setContentText("Choose how many values this tag type can hold on one photo.");
        alert.getButtonTypes().setAll(singleValueButton, multiValueButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == cancelButton) {
            return Optional.empty();
        }

        return Optional.of(result.get() == singleValueButton);
    }

    private String getTagTypeInput() {
        String editorText = tagTypeComboBox.getEditor().getText();
        if (editorText != null && !editorText.isBlank()) {
            return editorText.trim();
        }

        String selectedValue = tagTypeComboBox.getValue();
        return selectedValue == null ? "" : selectedValue.trim();
    }

    private String getFileName(Photo photo) {
        Path path = Path.of(photo.getFilePath());
        Path fileName = path.getFileName();
        return fileName == null ? photo.getFilePath() : fileName.toString();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Unable to complete action");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class PhotoCell extends ListCell<Photo> {
        @Override
        protected void updateItem(Photo item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

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

            Label fileNameLabel = new Label(getFileName(item));
            fileNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            String captionText = item.getCaption().isBlank() ? "Caption: -" : "Caption: " + item.getCaption();
            Label captionLabel = new Label(captionText);
            captionLabel.setStyle("-fx-text-fill: #4b5563;");
            captionLabel.setWrapText(true);

            Label dateLabel = new Label("Date: " + app.formatPhotoDate(item.getDateTaken()));
            dateLabel.setStyle("-fx-text-fill: #4b5563;");

            VBox textContent = new VBox(4.0, fileNameLabel, captionLabel, dateLabel);
            HBox container = new HBox(10.0, thumbnailView, textContent);
            container.setFillHeight(true);

            setText(null);
            setGraphic(container);
        }
    }
}

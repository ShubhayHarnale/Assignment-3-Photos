package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import photos.Photos;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the user albums screen.
 */
public class AlbumsController {
    private Photos app;
    private String currentUsername;

    @FXML
    private Label headingLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label selectionLabel;

    @FXML
    private ListView<String> albumsListView;

    public void setApp(Photos app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        albumsListView.setCellFactory(listView -> new AlbumSummaryCell());
        albumsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectionLabel.setText("Select an album to open or manage.");
            } else {
                selectionLabel.setText("Selected album: " + extractAlbumName(newValue));
            }
        });
        albumsListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2
                    && albumsListView.getSelectionModel().getSelectedItem() != null) {
                handleOpenAlbum();
            }
        });
    }

    public void setUsername(String username) {
        currentUsername = username;
        refreshAlbums();

        if (!app.hasUser(username)) {
            headingLabel.setText("Welcome, " + username);
            subtitleLabel.setText("This user does not exist yet. Create it from the admin screen first.");
            selectionLabel.setText("No albums available.");
            return;
        }

        if ("stock".equalsIgnoreCase(username)) {
            headingLabel.setText("Welcome, stock");
            subtitleLabel.setText("Viewing the stock user's albums.");
            return;
        }

        headingLabel.setText("Welcome, " + username);
        subtitleLabel.setText("Create, rename, delete, and open albums.");
    }

    @FXML
    private void handleCreateAlbum() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Album");
        dialog.setHeaderText("Create a new album");
        dialog.setContentText("Album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String albumName = result.get().trim();
        if (albumName.isEmpty()) {
            showError("Create Album", "Enter an album name.");
            return;
        }

        boolean created = app.createAlbum(currentUsername, albumName);
        if (!created) {
            showError("Create Album", "Album names must be unique for this user.");
            return;
        }

        refreshAlbums();
        selectAlbum(albumName);
        subtitleLabel.setText("Album '" + albumName + "' created.");
    }

    @FXML
    private void handleRenameAlbum() {
        String selectedAlbum = getSelectedAlbumOrShowMessage("Rename Album", "Select an album to rename.");
        if (selectedAlbum == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(extractAlbumName(selectedAlbum));
        dialog.setTitle("Rename Album");
        dialog.setHeaderText("Rename the selected album");
        dialog.setContentText("New album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String currentAlbumName = extractAlbumName(selectedAlbum);
        String newAlbumName = result.get().trim();
        if (newAlbumName.isEmpty()) {
            showError("Rename Album", "Enter a new album name.");
            return;
        }

        boolean renamed = app.renameAlbum(currentUsername, currentAlbumName, newAlbumName);
        if (!renamed) {
            showError("Rename Album", "Album names must be unique for this user.");
            return;
        }

        refreshAlbums();
        selectAlbum(newAlbumName);
        subtitleLabel.setText("Album '" + currentAlbumName + "' renamed to '" + newAlbumName + "'.");
    }

    @FXML
    private void handleDeleteAlbum() {
        String selectedAlbum = getSelectedAlbumOrShowMessage("Delete Album", "Select an album to delete.");
        if (selectedAlbum == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Album");
        alert.setHeaderText("Delete selected album?");
        alert.setContentText("Delete album '" + extractAlbumName(selectedAlbum) + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String albumName = extractAlbumName(selectedAlbum);
        boolean deleted = app.deleteAlbum(currentUsername, albumName);
        if (!deleted) {
            showError("Delete Album", "Unable to delete the selected album.");
            return;
        }

        refreshAlbums();
        subtitleLabel.setText("Album '" + albumName + "' deleted.");
    }

    @FXML
    private void handleOpenAlbum() {
        String selectedAlbum = getSelectedAlbumOrShowMessage("Open Album", "Select an album to open.");
        if (selectedAlbum == null) {
            return;
        }

        try {
            app.showAlbumContentsView(currentUsername, extractAlbumName(selectedAlbum));
        } catch (IOException exception) {
            showError("Open Album", "Unable to load the album contents screen.");
        }
    }

    @FXML
    private void handleSearch() {
        try {
            app.showSearchView(currentUsername);
        } catch (IOException exception) {
            showError("Search", "Unable to load the search screen.");
        }
    }

    @FXML
    private void handleLogout() {
        boolean loggedOut = app.logoutToLogin();
        if (!loggedOut) {
            subtitleLabel.setText("Unable to save changes and return to the login screen.");
        }
    }

    private void refreshAlbums() {
        albumsListView.getItems().setAll(app.getAlbumSummaries(currentUsername));
        albumsListView.getSelectionModel().clearSelection();

        if (albumsListView.getItems().isEmpty()) {
            selectionLabel.setText("No albums yet.");
        } else {
            selectionLabel.setText("Select an album to open or manage.");
        }
    }

    private String getSelectedAlbumOrShowMessage(String title, String message) {
        String selectedAlbum = albumsListView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            showError(title, message);
        }
        return selectedAlbum;
    }

    private void selectAlbum(String albumName) {
        for (String albumSummary : albumsListView.getItems()) {
            if (extractAlbumName(albumSummary).equalsIgnoreCase(albumName)) {
                albumsListView.getSelectionModel().select(albumSummary);
                albumsListView.scrollTo(albumSummary);
                break;
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Unable to complete action");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String extractAlbumName(String albumSummary) {
        int separatorIndex = albumSummary.indexOf('|');
        if (separatorIndex < 0) {
            return albumSummary;
        }
        return albumSummary.substring(0, separatorIndex).trim();
    }

    private static class AlbumSummaryCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            String[] parts = item.split("\\|", 3);
            Label nameLabel = new Label(parts.length > 0 ? parts[0].trim() : item);
            nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

            Label countLabel = new Label(parts.length > 1 ? parts[1].trim() : "");
            countLabel.setStyle("-fx-text-fill: #4b5563;");

            Label dateRangeLabel = new Label(parts.length > 2 ? parts[2].trim() : "");
            dateRangeLabel.setStyle("-fx-text-fill: #4b5563;");

            VBox content = new VBox(4.0, nameLabel, countLabel, dateRangeLabel);
            content.setFillWidth(true);
            setText(null);
            setGraphic(content);
        }
    }
}

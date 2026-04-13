package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
 * Controller for the user albums UI placeholder screen.
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
                selectionLabel.setText("Select a sample album to preview create/rename/delete/open flows.");
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
        if ("stock".equalsIgnoreCase(username)) {
            headingLabel.setText("Welcome, stock");
            subtitleLabel.setText("Stock user UI preview only. Album management dialogs are placeholders in this milestone.");
            return;
        }

        headingLabel.setText("Welcome, " + username);
        subtitleLabel.setText("Album management UI only. Dialogs preview the flow, but no real album data is changed yet.");
    }

    @FXML
    private void handleCreateAlbum() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Album");
        dialog.setHeaderText("Create album UI");
        dialog.setContentText("Album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            showPlaceholderMessage("Create Album", "UI preview only. A real album named '" + result.get().trim()
                    + "' would be created in a later logic milestone.");
        }
    }

    @FXML
    private void handleRenameAlbum() {
        String selectedAlbum = getSelectedAlbumOrShowMessage("Rename Album", "Select an album to rename.");
        if (selectedAlbum == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(extractAlbumName(selectedAlbum));
        dialog.setTitle("Rename Album");
        dialog.setHeaderText("Rename album UI");
        dialog.setContentText("New album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            showPlaceholderMessage("Rename Album", "UI preview only. '" + extractAlbumName(selectedAlbum)
                    + "' would be renamed to '" + result.get().trim() + "' in a later logic milestone.");
        }
    }

    @FXML
    private void handleDeleteAlbum() {
        String selectedAlbum = getSelectedAlbumOrShowMessage("Delete Album", "Select an album to delete.");
        if (selectedAlbum == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Album");
        alert.setHeaderText("Delete album UI");
        alert.setContentText("This is a preview confirmation for deleting '" + extractAlbumName(selectedAlbum) + "'.");
        alert.showAndWait();
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
            showPlaceholderMessage("Open Album", "Unable to load the album contents screen.");
        }
    }

    @FXML
    private void handleSearch() {
        try {
            app.showSearchView(currentUsername);
        } catch (IOException exception) {
            showPlaceholderMessage("Search", "Unable to load the search screen.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            app.showLoginView();
        } catch (IOException exception) {
            subtitleLabel.setText("Unable to return to the login screen.");
        }
    }

    private void showPlaceholderMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("UI placeholder");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getSelectedAlbumOrShowMessage(String title, String message) {
        String selectedAlbum = albumsListView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            showPlaceholderMessage(title, message);
        }
        return selectedAlbum;
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

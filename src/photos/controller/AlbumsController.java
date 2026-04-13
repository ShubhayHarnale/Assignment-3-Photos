package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import photos.Photos;

import java.io.IOException;

/**
 * Controller for the user albums UI placeholder screen.
 */
public class AlbumsController {
    private Photos app;

    @FXML
    private Label headingLabel;

    @FXML
    private Label subtitleLabel;

    public void setApp(Photos app) {
        this.app = app;
    }

    public void setUsername(String username) {
        if ("stock".equalsIgnoreCase(username)) {
            headingLabel.setText("Welcome, stock");
            subtitleLabel.setText("Stock user UI preview only. Stock loading and album data are not implemented yet.");
            return;
        }

        headingLabel.setText("Welcome, " + username);
        subtitleLabel.setText("Albums screen UI only. Album data and login validation are not implemented yet.");
    }

    @FXML
    private void handleCreateAlbum() {
        showPlaceholderMessage("Create Album", "Album creation is part of a later milestone with model and validation support.");
    }

    @FXML
    private void handleRenameAlbum() {
        showPlaceholderMessage("Rename Album", "Album rename logic is not implemented yet in this UI-only step.");
    }

    @FXML
    private void handleDeleteAlbum() {
        showPlaceholderMessage("Delete Album", "Album delete logic is not implemented yet in this UI-only step.");
    }

    @FXML
    private void handleOpenAlbum() {
        showPlaceholderMessage("Open Album", "The album contents screen belongs to a later milestone.");
    }

    @FXML
    private void handleSearch() {
        showPlaceholderMessage("Search", "Search UI and logic will be added in a later milestone.");
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
}

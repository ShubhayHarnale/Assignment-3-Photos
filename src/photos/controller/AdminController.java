package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import photos.Photos;

import java.io.IOException;

/**
 * Controller for the admin UI placeholder screen.
 */
public class AdminController {
    private Photos app;

    @FXML
    private Label subtitleLabel;

    public void setApp(Photos app) {
        this.app = app;
        subtitleLabel.setText("Admin screen UI only. User management logic is not implemented yet.");
    }

    @FXML
    private void handleCreateUser() {
        showPlaceholderMessage("Create User", "User creation UI logic is intentionally deferred until the model and admin business rules are added.");
    }

    @FXML
    private void handleDeleteUser() {
        showPlaceholderMessage("Delete User", "User deletion UI logic is intentionally deferred until the model and admin business rules are added.");
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

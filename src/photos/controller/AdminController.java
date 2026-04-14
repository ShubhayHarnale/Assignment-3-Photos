package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import photos.Photos;
import photos.model.PhotoLibrary;
import photos.model.User;

import java.util.Optional;
import java.util.stream.Collectors;

public class AdminController {
    private Photos app;

    @FXML
    private Label subtitleLabel;

    @FXML
    private ListView<String> usersListView;

    public void setApp(Photos app) {
        this.app = app;
        refreshUsers();
        subtitleLabel.setText("Create and delete users. The stock user is protected.");
    }

    @FXML
    private void handleCreateUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create User");
        dialog.setHeaderText("Create a new user");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String username = result.get().trim();
        if (username.isEmpty()) {
            showError("Create User", "Enter a username.");
            return;
        }

        boolean created = app.createUser(username);
        if (!created) {
            showError("Create User", "Usernames must be unique. 'admin' and 'stock' cannot be created.");
            return;
        }

        refreshUsers();
        usersListView.getSelectionModel().select(username.toLowerCase());
        subtitleLabel.setText("User '" + username.toLowerCase() + "' created.");
    }

    @FXML
    private void handleDeleteUser() {
        String selectedUsername = usersListView.getSelectionModel().getSelectedItem();
        if (selectedUsername == null) {
            showError("Delete User", "Select a user to delete.");
            return;
        }

        if (selectedUsername.equalsIgnoreCase(PhotoLibrary.STOCK_USERNAME)) {
            showError("Delete User", "The stock user cannot be deleted.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setHeaderText("Delete selected user?");
        confirmation.setContentText("Delete user '" + selectedUsername + "'?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        boolean deleted = app.deleteUser(selectedUsername);
        if (!deleted) {
            showError("Delete User", "Unable to delete the selected user.");
            return;
        }

        refreshUsers();
        subtitleLabel.setText("User '" + selectedUsername + "' deleted.");
    }

    @FXML
    private void handleLogout() {
        boolean loggedOut = app.logoutToLogin();
        if (!loggedOut) {
            subtitleLabel.setText("Unable to save changes and return to the login screen.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Unable to complete action");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void refreshUsers() {
        usersListView.getItems().setAll(
                app.getUsers().stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList())
        );
    }

}

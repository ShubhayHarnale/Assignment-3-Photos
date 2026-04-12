package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

/**
 * Controller for the initial login screen.
 * Real login routing is added in a later milestone.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login");
        alert.setHeaderText("Project skeleton ready");
        if (username.isEmpty()) {
            alert.setContentText("Enter a username. Login behavior will be implemented in the next milestone.");
        } else {
            alert.setContentText("Hello, " + username + ". Login behavior will be implemented in the next milestone.");
        }
        alert.showAndWait();
    }
}

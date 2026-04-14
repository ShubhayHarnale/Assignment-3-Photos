package photos.controller;

import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import photos.Photos;

import java.io.IOException;

/**
 * Controller for the login screen UI.
 */
public class LoginController {
    private Photos app;

    @FXML
    private TextField usernameField;

    @FXML
    private Label feedbackLabel;

    public void setApp(Photos app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> feedbackLabel.setText(" "));
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();

        if (username.isEmpty()) {
            feedbackLabel.setText("Enter a username.");
            return;
        }

        if (!app.isAdminUsername(username) && !app.hasUser(username)) {
            feedbackLabel.setText("Unknown username. Create the user from the admin screen first.");
            return;
        }

        try {
            if (app.isAdminUsername(username)) {
                app.showAdminView();
            } else {
                app.showAlbumsView(username);
            }
        } catch (IOException exception) {
            feedbackLabel.setText("Unable to load the next screen.");
        }
    }
}

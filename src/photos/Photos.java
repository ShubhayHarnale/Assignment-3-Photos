package photos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import photos.controller.AdminController;
import photos.controller.AlbumsController;
import photos.controller.LoginController;

import java.io.IOException;

/**
 * JavaFX entry point for the Photos application.
 */
public class Photos extends Application {
    private static final double WINDOW_WIDTH = 860;
    private static final double WINDOW_HEIGHT = 560;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Photos");
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(500);
        showLoginView();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void showLoginView() throws IOException {
        FXMLLoader loader = createLoader("view/LoginView.fxml");
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setApp(this);

        primaryStage.setTitle("Photos - Login");
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    public void showAdminView() throws IOException {
        FXMLLoader loader = createLoader("view/AdminView.fxml");
        Parent root = loader.load();

        AdminController controller = loader.getController();
        controller.setApp(this);

        primaryStage.setTitle("Photos - Admin");
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    public void showAlbumsView(String username) throws IOException {
        FXMLLoader loader = createLoader("view/AlbumsView.fxml");
        Parent root = loader.load();

        AlbumsController controller = loader.getController();
        controller.setApp(this);
        controller.setUsername(username);

        primaryStage.setTitle("Photos - User Albums");
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    private FXMLLoader createLoader(String resourcePath) {
        return new FXMLLoader(Photos.class.getResource(resourcePath));
    }
}

package photos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX entry point for the Photos application.
 */
public class Photos extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Photos.class.getResource("view/LoginView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 500, 320);
        primaryStage.setTitle("Photos");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(420);
        primaryStage.setMinHeight(260);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

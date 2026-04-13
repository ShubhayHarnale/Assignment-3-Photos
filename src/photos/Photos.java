package photos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import photos.controller.AdminController;
import photos.controller.AlbumContentsController;
import photos.controller.AlbumsController;
import photos.controller.LoginController;
import photos.controller.SearchController;
import photos.model.PhotoLibrary;
import photos.model.User;

import java.io.IOException;
import java.util.List;

public class Photos extends Application {
    private static final double DEFAULT_WINDOW_WIDTH = 860;
    private static final double DEFAULT_WINDOW_HEIGHT = 560;
    private static final double ALBUM_WINDOW_WIDTH = 980;
    private static final double ALBUM_WINDOW_HEIGHT = 720;

    private Stage primaryStage;
    private final PhotoLibrary photoLibrary = new PhotoLibrary();

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

        showScene(root, "Photos - Login", DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT, 760, 500);
    }

    public void showAdminView() throws IOException {
        FXMLLoader loader = createLoader("view/AdminView.fxml");
        Parent root = loader.load();

        AdminController controller = loader.getController();
        controller.setApp(this);

        showScene(root, "Photos - Admin", DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT, 760, 500);
    }

    public void showAlbumsView(String username) throws IOException {
        FXMLLoader loader = createLoader("view/AlbumsView.fxml");
        Parent root = loader.load();

        AlbumsController controller = loader.getController();
        controller.setApp(this);
        controller.setUsername(username);

        showScene(root, "Photos - User Albums", DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT, 760, 500);
    }

    public void showAlbumContentsView(String username, String albumName) throws IOException {
        FXMLLoader loader = createLoader("view/AlbumContentsView.fxml");
        Parent root = loader.load();

        AlbumContentsController controller = loader.getController();
        controller.setApp(this);
        controller.setContext(username, albumName);

        showScene(root, "Photos - Album Contents", ALBUM_WINDOW_WIDTH, ALBUM_WINDOW_HEIGHT, 900, 640);
    }

    public void showSearchView(String username) throws IOException {
        FXMLLoader loader = createLoader("view/SearchView.fxml");
        Parent root = loader.load();

        SearchController controller = loader.getController();
        controller.setApp(this);
        controller.setUsername(username);

        showScene(root, "Photos - Search", DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT, 860, 560);
    }

    public List<User> getUsers() {
        return photoLibrary.getUsers();
    }

    public boolean createUser(String username) {
        if (username == null) {
            return false;
        }
        return photoLibrary.addUser(new User(username.trim()));
    }

    public boolean deleteUser(String username) {
        if (username == null) {
            return false;
        }
        return photoLibrary.removeUser(username.trim());
    }

    private FXMLLoader createLoader(String resourcePath) {
        return new FXMLLoader(Photos.class.getResource(resourcePath));
    }

    private void showScene(Parent root, String title, double width, double height, double minWidth, double minHeight) {
        primaryStage.setTitle(title);
        primaryStage.setMinWidth(minWidth);
        primaryStage.setMinHeight(minHeight);
        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.sizeToScene();
    }
}

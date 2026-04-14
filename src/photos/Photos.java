package photos;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import photos.controller.AdminController;
import photos.controller.AlbumContentsController;
import photos.controller.AlbumsController;
import photos.controller.LoginController;
import photos.controller.SearchController;
import photos.model.Album;
import photos.model.PhotoLibrary;
import photos.model.PhotoLibraryStorage;
import photos.model.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Photos extends Application {
    private static final double DEFAULT_WINDOW_WIDTH = 860;
    private static final double DEFAULT_WINDOW_HEIGHT = 560;
    private static final double ALBUM_WINDOW_WIDTH = 980;
    private static final double ALBUM_WINDOW_HEIGHT = 720;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private Stage primaryStage;
    private PhotoLibrary photoLibrary;
    private String startupMessage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        photoLibrary = loadPhotoLibrary();
        primaryStage.setTitle("Photos");
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(500);
        primaryStage.setOnCloseRequest(event -> {
            if (!savePhotoLibrary()) {
                event.consume();
            }
        });
        showLoginView();
        primaryStage.show();

        if (startupMessage != null) {
            Platform.runLater(() -> showError("Saved Data", startupMessage));
        }
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

    public List<String> getAlbumSummaries(String username) {
        User user = getUser(username);
        if (user == null) {
            return List.of();
        }

        return user.getAlbums().stream()
                .map(this::formatAlbumSummary)
                .toList();
    }

    public boolean createAlbum(String username, String albumName) {
        User user = getUser(username);
        if (user == null) {
            return false;
        }
        return user.createAlbum(albumName);
    }

    public boolean renameAlbum(String username, String currentName, String newName) {
        User user = getUser(username);
        if (user == null) {
            return false;
        }
        return user.renameAlbum(currentName, newName);
    }

    public boolean deleteAlbum(String username, String albumName) {
        User user = getUser(username);
        if (user == null) {
            return false;
        }
        return user.deleteAlbum(albumName);
    }

    public boolean hasUser(String username) {
        return getUser(username) != null;
    }

    public boolean logoutToLogin() {
        if (!savePhotoLibrary()) {
            return false;
        }

        try {
            showLoginView();
            return true;
        } catch (IOException exception) {
            showError("Login", "Unable to return to the login screen.");
            return false;
        }
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

    private User getUser(String username) {
        if (username == null) {
            return null;
        }
        return photoLibrary.getUser(username.trim());
    }

    private String formatAlbumSummary(Album album) {
        int photoCount = album.getPhotoCount();
        String photoCountText = photoCount + (photoCount == 1 ? " photo" : " photos");
        String dateRangeText = formatDateRange(album.getStartDate(), album.getEndDate());
        return album.getName() + " | " + photoCountText + " | " + dateRangeText;
    }

    private String formatDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return "No photos";
        }

        return DATE_FORMATTER.format(startDate) + " - " + DATE_FORMATTER.format(endDate);
    }

    private PhotoLibrary loadPhotoLibrary() {
        try {
            return PhotoLibraryStorage.load();
        } catch (IOException | ClassNotFoundException exception) {
            startupMessage = "Saved data could not be loaded. The app started with a new library.";
            return new PhotoLibrary();
        }
    }

    private boolean savePhotoLibrary() {
        try {
            PhotoLibraryStorage.save(photoLibrary);
            return true;
        } catch (IOException exception) {
            showError("Save Data", "Your changes could not be saved.");
            return false;
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Unable to complete action");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

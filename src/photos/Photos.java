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
import photos.model.Photo;
import photos.model.PhotoLibrary;
import photos.model.PhotoLibraryStorage;
import photos.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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

    public boolean isAdminUsername(String username) {
        return username != null && PhotoLibrary.ADMIN_USERNAME.equalsIgnoreCase(username.trim());
    }

    public List<Photo> getAlbumPhotos(String username, String albumName) {
        Album album = getAlbum(username, albumName);
        if (album == null) {
            return List.of();
        }

        return album.getPhotos().stream()
                .sorted(Comparator.comparing(Photo::getFilePath, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public String addPhotoToAlbum(String username, String albumName, File photoFile) {
        if (photoFile == null) {
            return "Select a photo file.";
        }

        if (!Files.exists(photoFile.toPath()) || !Files.isRegularFile(photoFile.toPath())) {
            return "The selected photo file could not be found.";
        }

        User user = getUser(username);
        if (user == null || user.getAlbum(albumName) == null) {
            return "The selected album could not be found.";
        }

        try {
            boolean added = user.addPhotoToAlbum(albumName, photoFile.toPath());
            if (!added) {
                return "This photo is already in the album.";
            }
            return null;
        } catch (IOException exception) {
            return "Unable to read the selected photo file.";
        }
    }

    public boolean removePhotoFromAlbum(String username, String albumName, String filePath) {
        User user = getUser(username);
        if (user == null) {
            return false;
        }

        return user.removePhotoFromAlbum(albumName, filePath);
    }

    public String updatePhotoCaption(String username, String albumName, String filePath, String caption) {
        User user = getUser(username);
        if (user == null) {
            return "The selected user could not be found.";
        }

        return user.updatePhotoCaption(albumName, filePath, caption)
                ? null
                : "Unable to update the caption for the selected photo.";
    }

    public List<String> getTagTypes(String username) {
        User user = getUser(username);
        if (user == null) {
            return List.of();
        }
        return user.getTagTypeNames();
    }

    public String addTagToPhoto(
            String username,
            String albumName,
            String filePath,
            String tagName,
            String tagValue,
            boolean singleValueIfNew
    ) {
        User user = getUser(username);
        if (user == null) {
            return "The selected user could not be found.";
        }

        User.TagAddResult result = user.addTagToPhoto(albumName, filePath, tagName, tagValue, singleValueIfNew);
        return switch (result) {
            case ADDED -> null;
            case DUPLICATE -> "This photo already has that tag.";
            case SINGLE_VALUE_CONFLICT -> "This tag type allows only one value on a photo.";
            case INVALID_TAG -> "Enter both a tag type and a tag value.";
            case PHOTO_NOT_FOUND -> "The selected photo could not be found.";
        };
    }

    public boolean removeTagFromPhoto(String username, String albumName, String filePath, String tagName, String tagValue) {
        User user = getUser(username);
        if (user == null) {
            return false;
        }

        return user.removeTagFromPhoto(albumName, filePath, tagName, tagValue);
    }

    public List<String> getAlbumNames(String username) {
        User user = getUser(username);
        if (user == null) {
            return List.of();
        }

        return user.getAlbums().stream()
                .map(Album::getName)
                .toList();
    }

    public String copyPhotoToAlbum(String username, String sourceAlbumName, String targetAlbumName, String filePath) {
        User user = getUser(username);
        if (user == null) {
            return "The selected user could not be found.";
        }

        if (sourceAlbumName != null && sourceAlbumName.equalsIgnoreCase(targetAlbumName)) {
            return "Choose a different album.";
        }

        return user.copyPhoto(sourceAlbumName, targetAlbumName, filePath)
                ? null
                : "Unable to copy the photo. The target album may already contain it.";
    }

    public String movePhotoToAlbum(String username, String sourceAlbumName, String targetAlbumName, String filePath) {
        User user = getUser(username);
        if (user == null) {
            return "The selected user could not be found.";
        }

        if (sourceAlbumName != null && sourceAlbumName.equalsIgnoreCase(targetAlbumName)) {
            return "Choose a different album.";
        }

        return user.movePhoto(sourceAlbumName, targetAlbumName, filePath)
                ? null
                : "Unable to move the photo. The target album may already contain it.";
    }

    public List<Photo> searchPhotosByDate(String username, LocalDate startDate, LocalDate endDate) {
        User user = getUser(username);
        if (user == null) {
            return List.of();
        }

        return user.searchPhotosByDate(startDate, endDate).stream()
                .sorted(Comparator.comparing(Photo::getDateTaken, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Photo::getFilePath, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Photo> searchPhotosByTags(
            String username,
            String firstTagName,
            String firstTagValue,
            String secondTagName,
            String secondTagValue,
            boolean requireAll
    ) {
        User user = getUser(username);
        if (user == null) {
            return List.of();
        }

        return user.searchPhotosByTags(firstTagName, firstTagValue, secondTagName, secondTagValue, requireAll).stream()
                .sorted(Comparator.comparing(Photo::getFilePath, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public boolean createAlbumFromPhotos(String username, String albumName, List<Photo> photos) {
        User user = getUser(username);
        if (user == null) {
            return false;
        }

        return user.createAlbumFromPhotos(albumName, photos);
    }

    public String formatPhotoDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return DATE_FORMATTER.format(dateTime);
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

    private Album getAlbum(String username, String albumName) {
        User user = getUser(username);
        if (user == null || albumName == null) {
            return null;
        }
        return user.getAlbum(albumName.trim());
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

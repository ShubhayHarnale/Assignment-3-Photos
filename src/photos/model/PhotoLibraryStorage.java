package photos.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

public final class PhotoLibraryStorage {
    private static final Path DATA_FILE = Paths.get("data", "photos.dat");
    private static final Path STOCK_DIRECTORY = Paths.get("data", "stock");
    private static final String STOCK_ALBUM_NAME = "stock";

    private PhotoLibraryStorage() {
    }

    public static PhotoLibrary load() throws IOException, ClassNotFoundException {
        if (Files.notExists(DATA_FILE)) {
            return bootstrapStockData(new PhotoLibrary());
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(DATA_FILE))) {
            Object savedObject = inputStream.readObject();
            if (savedObject instanceof PhotoLibrary photoLibrary) {
                return bootstrapStockData(photoLibrary);
            }
        }

        throw new IOException("Saved data file is invalid.");
    }

    public static void save(PhotoLibrary photoLibrary) throws IOException {
        Files.createDirectories(DATA_FILE.getParent());

        try (ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(DATA_FILE))) {
            outputStream.writeObject(photoLibrary);
        }
    }

    private static PhotoLibrary bootstrapStockData(PhotoLibrary photoLibrary) throws IOException {
        User stockUser = photoLibrary.ensureStockUser();
        Album stockAlbum = stockUser.getAlbum(STOCK_ALBUM_NAME);
        if (stockAlbum == null) {
            stockAlbum = new Album(STOCK_ALBUM_NAME);
            stockUser.addAlbum(stockAlbum);
        }
        Album finalStockAlbum = stockAlbum;

        Files.createDirectories(STOCK_DIRECTORY);

        try (Stream<Path> paths = Files.list(STOCK_DIRECTORY)) {
            paths.filter(Files::isRegularFile)
                    .filter(PhotoLibraryStorage::isSupportedImageFile)
                    .forEach(path -> addStockPhoto(finalStockAlbum, path));
        }

        return photoLibrary;
    }

    private static void addStockPhoto(Album stockAlbum, Path path) {
        String normalizedPath = path.toAbsolutePath().normalize().toString();
        Photo photo = new Photo(normalizedPath);

        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(path);
            LocalDateTime photoDate = LocalDateTime.ofInstant(
                    lastModifiedTime.toInstant(),
                    ZoneId.systemDefault()
            );
            photo.setDateTaken(photoDate);
        } catch (IOException exception) {
            photo.setDateTaken(null);
        }

        stockAlbum.addPhoto(photo);
    }

    private static boolean isSupportedImageFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png")
                || fileName.endsWith(".gif")
                || fileName.endsWith(".bmp");
    }
}

package photos.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PhotoLibraryStorage {
    private static final Path DATA_FILE = Paths.get("data", "photos.dat");

    private PhotoLibraryStorage() {
    }

    public static PhotoLibrary load() throws IOException, ClassNotFoundException {
        if (Files.notExists(DATA_FILE)) {
            return new PhotoLibrary();
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(DATA_FILE))) {
            Object savedObject = inputStream.readObject();
            if (savedObject instanceof PhotoLibrary photoLibrary) {
                return photoLibrary;
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
}

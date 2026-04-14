package photos.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @TempDir
    Path tempDir;

    @Test
    void addPhotoToAlbumSharesPhotoAcrossAlbumsAndPreventsDuplicates() throws IOException {
        User user = new User("alice");
        user.createAlbum("Vacation");
        user.createAlbum("Favorites");

        Path photoPath = createPhotoFile("sunrise.jpg", Instant.parse("2026-01-03T10:15:30Z"));

        assertTrue(user.addPhotoToAlbum("Vacation", photoPath));
        assertFalse(user.addPhotoToAlbum("Vacation", photoPath));
        assertTrue(user.addPhotoToAlbum("Favorites", photoPath));

        Photo vacationPhoto = user.getAlbum("Vacation").getPhotos().getFirst();
        Photo favoritesPhoto = user.getAlbum("Favorites").getPhotos().getFirst();

        assertSame(vacationPhoto, favoritesPhoto);
    }

    @Test
    void captionAndTagsStaySharedAcrossAlbums() throws IOException {
        User user = new User("alice");
        user.createAlbum("Vacation");
        user.createAlbum("Favorites");

        Path photoPath = createPhotoFile("museum.png", Instant.parse("2026-01-05T14:00:00Z"));
        user.addPhotoToAlbum("Vacation", photoPath);
        user.addPhotoToAlbum("Favorites", photoPath);

        assertTrue(user.updatePhotoCaption("Vacation", photoPath.toString(), "Museum visit"));
        assertEquals("Museum visit", user.getAlbum("Favorites").getPhotos().getFirst().getCaption());

        assertEquals(User.TagAddResult.ADDED, user.addTagToPhoto("Vacation", photoPath.toString(), "location", "Boston", false));
        assertEquals(User.TagAddResult.SINGLE_VALUE_CONFLICT, user.addTagToPhoto("Vacation", photoPath.toString(), "location", "New York", false));
        assertEquals(User.TagAddResult.ADDED, user.addTagToPhoto("Vacation", photoPath.toString(), "person", "Chris", false));
        assertEquals(User.TagAddResult.ADDED, user.addTagToPhoto("Vacation", photoPath.toString(), "person", "Sam", false));
        assertEquals(User.TagAddResult.DUPLICATE, user.addTagToPhoto("Vacation", photoPath.toString(), "person", "Chris", false));

        Photo sharedPhoto = user.getAlbum("Favorites").getPhotos().getFirst();
        assertEquals(3, sharedPhoto.getTags().size());
        assertTrue(user.removeTagFromPhoto("Favorites", photoPath.toString(), "person", "Sam"));
        assertEquals(2, user.getAlbum("Vacation").getPhotos().getFirst().getTags().size());
    }

    @Test
    void searchReturnsUniquePhotosForDateAndTags() throws IOException {
        User user = new User("alice");
        user.createAlbum("Vacation");
        user.createAlbum("Favorites");

        Path museumPath = createPhotoFile("museum.png", Instant.parse("2026-01-05T14:00:00Z"));
        Path dinnerPath = createPhotoFile("dinner.jpg", Instant.parse("2026-01-09T18:30:00Z"));

        user.addPhotoToAlbum("Vacation", museumPath);
        user.addPhotoToAlbum("Favorites", museumPath);
        user.addPhotoToAlbum("Vacation", dinnerPath);

        user.addTagToPhoto("Vacation", museumPath.toString(), "location", "Boston", false);
        user.addTagToPhoto("Vacation", museumPath.toString(), "person", "Chris", false);
        user.addTagToPhoto("Vacation", dinnerPath.toString(), "location", "New York", false);

        List<Photo> dateResults = user.searchPhotosByDate(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 6));
        assertEquals(1, dateResults.size());
        assertEquals(museumPath.toAbsolutePath().normalize().toString(), dateResults.getFirst().getFilePath());

        List<Photo> andResults = user.searchPhotosByTags("location", "Boston", "person", "Chris", true);
        assertEquals(1, andResults.size());

        List<Photo> orResults = user.searchPhotosByTags("location", "Boston", "location", "New York", false);
        assertEquals(2, orResults.size());
    }

    @Test
    void createAlbumFromPhotosReusesExistingPhotoObjects() throws IOException {
        User user = new User("alice");
        user.createAlbum("Vacation");

        Path photoPath = createPhotoFile("boardwalk.jpg", Instant.parse("2026-01-03T10:15:30Z"));
        user.addPhotoToAlbum("Vacation", photoPath);

        Photo sharedPhoto = user.getAlbum("Vacation").getPhotos().getFirst();
        assertTrue(user.createAlbumFromPhotos("Search Results", List.of(sharedPhoto)));

        Photo searchAlbumPhoto = user.getAlbum("Search Results").getPhotos().getFirst();
        assertSame(sharedPhoto, searchAlbumPhoto);
    }

    @Test
    void photoLibraryAlwaysIncludesStockUser() {
        PhotoLibrary library = new PhotoLibrary();

        assertTrue(library.hasUser(PhotoLibrary.STOCK_USERNAME));
    }

    private Path createPhotoFile(String fileName, Instant lastModifiedTime) throws IOException {
        Path photoPath = tempDir.resolve(fileName);
        Files.writeString(photoPath, "not-an-image-but-a-regular-file");
        Files.setLastModifiedTime(photoPath, FileTime.from(lastModifiedTime));
        return photoPath;
    }
}

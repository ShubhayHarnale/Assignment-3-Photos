package photos.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class User implements Comparable<User>, Serializable {
    private static final long serialVersionUID = 1L;

    public enum TagAddResult {
        ADDED,
        DUPLICATE,
        SINGLE_VALUE_CONFLICT,
        INVALID_TAG,
        PHOTO_NOT_FOUND
    }

    private final String username;
    private final List<Album> albums;
    private final List<TagDefinition> tagDefinitions;
    private Map<String, Photo> photoRegistry;

    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
        this.tagDefinitions = new ArrayList<>();
        this.photoRegistry = new HashMap<>();

        addTagDefinition(new TagDefinition("location", true));
        addTagDefinition(new TagDefinition("person", false));
    }

    public String getUsername() {
        return username;
    }

    public List<Album> getAlbums() {
        List<Album> sortedAlbums = new ArrayList<>(albums);
        Collections.sort(sortedAlbums);
        return Collections.unmodifiableList(sortedAlbums);
    }

    public List<TagDefinition> getTagDefinitions() {
        return Collections.unmodifiableList(tagDefinitions);
    }

    public boolean hasAlbum(String albumName) {
        for (Album album : albums) {
            if (album.getName().equalsIgnoreCase(albumName)) {
                return true;
            }
        }
        return false;
    }

    public Album getAlbum(String albumName) {
        for (Album album : albums) {
            if (album.getName().equalsIgnoreCase(albumName)) {
                return album;
            }
        }
        return null;
    }

    public boolean createAlbum(String albumName) {
        if (albumName == null) {
            return false;
        }

        String trimmedName = albumName.trim();
        if (trimmedName.isEmpty() || hasAlbum(trimmedName)) {
            return false;
        }

        albums.add(new Album(trimmedName));
        return true;
    }

    public boolean addAlbum(Album album) {
        if (album == null || hasAlbum(album.getName())) {
            return false;
        }
        albums.add(album);
        return true;
    }

    public boolean removeAlbum(Album album) {
        return albums.remove(album);
    }

    public boolean deleteAlbum(String albumName) {
        Album album = getAlbum(albumName);
        if (album == null) {
            return false;
        }
        return albums.remove(album);
    }

    public boolean renameAlbum(String currentName, String newName) {
        if (currentName == null || newName == null) {
            return false;
        }

        Album album = getAlbum(currentName.trim());
        String trimmedNewName = newName.trim();

        if (album == null || trimmedNewName.isEmpty()) {
            return false;
        }

        Album existingAlbum = getAlbum(trimmedNewName);
        if (existingAlbum != null && existingAlbum != album) {
            return false;
        }

        album.setName(trimmedNewName);
        return true;
    }

    public boolean addPhotoToAlbum(String albumName, Path photoPath) throws IOException {
        Album album = getAlbum(albumName);
        if (album == null || photoPath == null) {
            return false;
        }

        ensurePhotoRegistry();

        String normalizedPath = normalizeFilePath(photoPath.toString());
        Photo photo = photoRegistry.get(normalizedPath);
        if (photo == null) {
            photo = new Photo(normalizedPath);
            FileTime lastModifiedTime = Files.getLastModifiedTime(photoPath);
            LocalDateTime photoDate = LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
            photo.setDateTaken(photoDate);
            photoRegistry.put(normalizedPath, photo);
        }

        return album.addPhoto(photo);
    }

    public boolean removePhotoFromAlbum(String albumName, String filePath) {
        Album album = getAlbum(albumName);
        if (album == null || filePath == null || filePath.isBlank()) {
            return false;
        }

        ensurePhotoRegistry();

        String normalizedPath = normalizeFilePath(filePath);
        Photo photo = photoRegistry.get(normalizedPath);
        if (photo == null) {
            return false;
        }

        return album.removePhoto(photo);
    }

    public boolean addTagDefinition(TagDefinition tagDefinition) {
        if (tagDefinition == null || tagDefinitions.contains(tagDefinition)) {
            return false;
        }
        tagDefinitions.add(tagDefinition);
        return true;
    }

    public boolean addTagDefinition(String tagName, boolean singleValue) {
        if (tagName == null || tagName.isBlank()) {
            return false;
        }
        return addTagDefinition(new TagDefinition(tagName, singleValue));
    }

    public boolean hasTagDefinition(String tagName) {
        for (TagDefinition tagDefinition : tagDefinitions) {
            if (tagDefinition.getName().equalsIgnoreCase(tagName)) {
                return true;
            }
        }
        return false;
    }

    public TagDefinition getTagDefinition(String tagName) {
        for (TagDefinition tagDefinition : tagDefinitions) {
            if (tagDefinition.getName().equalsIgnoreCase(tagName)) {
                return tagDefinition;
            }
        }
        return null;
    }

    public List<String> getTagTypeNames() {
        List<String> names = tagDefinitions.stream()
                .map(TagDefinition::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        return Collections.unmodifiableList(names);
    }

    public boolean updatePhotoCaption(String albumName, String filePath, String caption) {
        Photo photo = getPhoto(albumName, filePath);
        if (photo == null) {
            return false;
        }

        photo.setCaption(caption);
        return true;
    }

    public TagAddResult addTagToPhoto(
            String albumName,
            String filePath,
            String tagName,
            String tagValue,
            boolean singleValueIfNew
    ) {
        if (tagName == null || tagName.isBlank() || tagValue == null || tagValue.isBlank()) {
            return TagAddResult.INVALID_TAG;
        }

        Photo photo = getPhoto(albumName, filePath);
        if (photo == null) {
            return TagAddResult.PHOTO_NOT_FOUND;
        }

        String trimmedTagName = tagName.trim();
        String trimmedTagValue = tagValue.trim();
        TagDefinition tagDefinition = getTagDefinition(trimmedTagName);
        if (tagDefinition == null) {
            addTagDefinition(trimmedTagName, singleValueIfNew);
            tagDefinition = getTagDefinition(trimmedTagName);
        }

        boolean duplicate = photo.getTags().stream().anyMatch(tag ->
                tag.getType().equalsIgnoreCase(trimmedTagName)
                        && tag.getValue().equalsIgnoreCase(trimmedTagValue)
        );
        if (duplicate) {
            return TagAddResult.DUPLICATE;
        }

        if (tagDefinition != null && tagDefinition.isSingleValue()) {
            boolean hasConflictingValue = photo.getTags().stream().anyMatch(tag ->
                    tag.getType().equalsIgnoreCase(trimmedTagName)
            );
            if (hasConflictingValue) {
                return TagAddResult.SINGLE_VALUE_CONFLICT;
            }
        }

        return photo.addTag(new Tag(trimmedTagName, trimmedTagValue))
                ? TagAddResult.ADDED
                : TagAddResult.DUPLICATE;
    }

    public boolean removeTagFromPhoto(String albumName, String filePath, String tagName, String tagValue) {
        Photo photo = getPhoto(albumName, filePath);
        if (photo == null) {
            return false;
        }

        for (Tag tag : photo.getTags()) {
            if (tag.getType().equalsIgnoreCase(tagName) && tag.getValue().equalsIgnoreCase(tagValue)) {
                return photo.removeTag(tag);
            }
        }

        return false;
    }

    public boolean copyPhoto(String sourceAlbumName, String targetAlbumName, String filePath) {
        if (targetAlbumName == null || sourceAlbumName == null || sourceAlbumName.equalsIgnoreCase(targetAlbumName)) {
            return false;
        }

        Photo photo = getPhoto(sourceAlbumName, filePath);
        Album targetAlbum = getAlbum(targetAlbumName);
        if (photo == null || targetAlbum == null) {
            return false;
        }

        return targetAlbum.addPhoto(photo);
    }

    public boolean movePhoto(String sourceAlbumName, String targetAlbumName, String filePath) {
        if (targetAlbumName == null || sourceAlbumName == null || sourceAlbumName.equalsIgnoreCase(targetAlbumName)) {
            return false;
        }

        Album sourceAlbum = getAlbum(sourceAlbumName);
        Album targetAlbum = getAlbum(targetAlbumName);
        Photo photo = getPhoto(sourceAlbumName, filePath);
        if (sourceAlbum == null || targetAlbum == null || photo == null || targetAlbum.hasPhoto(photo)) {
            return false;
        }

        if (!targetAlbum.addPhoto(photo)) {
            return false;
        }

        return sourceAlbum.removePhoto(photo);
    }

    public List<Photo> searchPhotosByDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return List.of();
        }

        Map<String, Photo> matches = new LinkedHashMap<>();
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                if (photo.getDateTaken() == null) {
                    continue;
                }

                LocalDate photoDate = photo.getDateTaken().toLocalDate();
                if (!photoDate.isBefore(startDate) && !photoDate.isAfter(endDate)) {
                    matches.putIfAbsent(normalizeFilePath(photo.getFilePath()), photo);
                }
            }
        }

        return List.copyOf(matches.values());
    }

    public List<Photo> searchPhotosByTags(
            String firstTagName,
            String firstTagValue,
            String secondTagName,
            String secondTagValue,
            boolean requireAll
    ) {
        if (firstTagName == null || firstTagName.isBlank() || firstTagValue == null || firstTagValue.isBlank()) {
            return List.of();
        }

        boolean useSecondTag = secondTagName != null && !secondTagName.isBlank()
                && secondTagValue != null && !secondTagValue.isBlank();

        Map<String, Photo> matches = new LinkedHashMap<>();
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                boolean matchesFirst = hasMatchingTag(photo, firstTagName, firstTagValue);
                boolean matchesSecond = useSecondTag && hasMatchingTag(photo, secondTagName, secondTagValue);
                boolean matched = useSecondTag
                        ? (requireAll ? matchesFirst && matchesSecond : matchesFirst || matchesSecond)
                        : matchesFirst;

                if (matched) {
                    matches.putIfAbsent(normalizeFilePath(photo.getFilePath()), photo);
                }
            }
        }

        return List.copyOf(matches.values());
    }

    public boolean createAlbumFromPhotos(String albumName, List<Photo> photosToAdd) {
        if (!createAlbum(albumName)) {
            return false;
        }

        Album album = getAlbum(albumName);
        if (album == null) {
            return false;
        }

        for (Photo photo : photosToAdd) {
            if (photo != null) {
                album.addPhoto(photo);
            }
        }

        return true;
    }

    @Override
    public int compareTo(User other) {
        return this.username.compareToIgnoreCase(other.username);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return username.equalsIgnoreCase(other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username.toLowerCase());
    }

    @Override
    public String toString() {
        return username;
    }

    public Photo getPhoto(String albumName, String filePath) {
        Album album = getAlbum(albumName);
        if (album == null || filePath == null || filePath.isBlank()) {
            return null;
        }

        String normalizedPath = normalizeFilePath(filePath);
        for (Photo photo : album.getPhotos()) {
            if (normalizeFilePath(photo.getFilePath()).equalsIgnoreCase(normalizedPath)) {
                return photo;
            }
        }

        return null;
    }

    private void ensurePhotoRegistry() {
        if (photoRegistry == null) {
            photoRegistry = new HashMap<>();
        }

        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                photoRegistry.putIfAbsent(normalizeFilePath(photo.getFilePath()), photo);
            }
        }
    }

    private String normalizeFilePath(String filePath) {
        return Path.of(filePath).toAbsolutePath().normalize().toString();
    }

    private boolean hasMatchingTag(Photo photo, String tagName, String tagValue) {
        return photo.getTags().stream().anyMatch(tag ->
                tag.getType().equalsIgnoreCase(tagName.trim())
                        && tag.getValue().equalsIgnoreCase(tagValue.trim())
        );
    }
}

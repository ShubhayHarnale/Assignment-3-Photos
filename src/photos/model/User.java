package photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class User implements Comparable<User>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final List<Album> albums;
    private final List<TagDefinition> tagDefinitions;

    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
        this.tagDefinitions = new ArrayList<>();

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

    public boolean addTagDefinition(TagDefinition tagDefinition) {
        if (tagDefinition == null || tagDefinitions.contains(tagDefinition)) {
            return false;
        }
        tagDefinitions.add(tagDefinition);
        return true;
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
}

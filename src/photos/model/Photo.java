package photos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Photo {
    private final String filePath;
    private String caption;//Not making it final because the user can change the caption later.
    private LocalDateTime dateTaken;
    private final List<Tag> tags;

    public Photo(String filePath) {
        this.filePath = filePath;
        this.caption = ""; //The caption starts empty, thats where the user can add their own caption.
        this.dateTaken = null;
        this.tags = new ArrayList<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        if (caption == null) {
      this.caption = "";
  } else {
      this.caption = caption;
  }
    }

    public LocalDateTime getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(LocalDateTime dateTaken) {
        this.dateTaken = dateTaken;
    }

    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public boolean hasTag(String name, String value) { //Checking for any duplicates.
        for (Tag tag : tags) {
            if (tag.getName().equals(name) && tag.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    public boolean addTag(Tag tag) { //Preventing any null or duplicates.
        if (tag == null || hasTag(tag)) {
            return false;
        }
        tags.add(tag);
        return true;
    }

    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Photo)) return false;
        Photo other = (Photo) obj;
        return Objects.equals(filePath, other.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }

    @Override
    public String toString() {
        return filePath;
    }
}

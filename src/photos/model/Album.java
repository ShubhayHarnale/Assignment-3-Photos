package photos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Album implements Comparable<Album> {

    private String name;
    private final List<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public List<Photo> getPhotos() {
        return Collections.unmodifiableList(photos); //returns a read only view of the same list so that no one can modify the list which remove the whole point of this feature.
    }

    public void setName(String name) { //This is used to change the name of the album(mentioned in the assignment)
        this.name = name;
    }

    public int getPhotoCount() {
        return photos.size();
    }

    public boolean hasPhoto(Photo photo) { //Just checking if the photo exists in the album.
        return photos.contains(photo);
    }

    public boolean addPhoto(Photo photo) { //Tells the user if the photo is added or no thats why the boolean.
        if (hasPhoto(photo)) {
            return false;
        }
        photos.add(photo);
        return true;
    }

    public boolean removePhoto(Photo photo) {
        return photos.remove(photo);
    }

    //These methods scan all photos in the album and return the earliest or latest date taken.
    public LocalDateTime getStartDate() {
        LocalDateTime earliest = null;
        for (Photo p : photos) {
            LocalDateTime d = p.getDateTaken();
            if (d != null && (earliest == null || d.isBefore(earliest))) { //Adding d!=null as a safety net, but this should never happen.
                earliest = d;
            }
        }
        return earliest;
    }

   
    public LocalDateTime getEndDate() { //Similar to the method above, but this time we are looking for the latest date taken.
        LocalDateTime latest = null;
        for (Photo p : photos) {
            LocalDateTime d = p.getDateTaken();
            if (d != null && (latest == null || d.isAfter(latest))) {
                latest = d;
            }
        }
        return latest;
    }


    @Override
    public int compareTo(Album other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Album)) return false;
        Album other = (Album) obj;
        return this.name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + photos.size() + " photos)";
    }
}

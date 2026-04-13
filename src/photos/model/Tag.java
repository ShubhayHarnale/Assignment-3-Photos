package photos.model;

import java.util.Objects;

public class Tag {

//Quite straightforward, just storing the type and value.
    private final String type;
    private final String value;

    public Tag(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; //This is crucial because two pictures can not have the same tag.
        if (!(obj instanceof Tag)) return false;
        Tag other = (Tag) obj;
        return Objects.equals(type, other.type) && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return type + " = " + value;
    }
}

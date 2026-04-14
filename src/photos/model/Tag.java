package photos.model;

import java.io.Serializable;
import java.util.Objects;

public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String type;
    private final String value;

    public Tag(String type, String value) {
        this.type = type == null ? "" : type.trim();
        this.value = value == null ? "" : value.trim();
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
        if (this == obj) return true;
        if (!(obj instanceof Tag)) return false;
        Tag other = (Tag) obj;
        return type.equalsIgnoreCase(other.type) && value.equalsIgnoreCase(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type.toLowerCase(), value.toLowerCase());
    }

    @Override
    public String toString() {
        return type + " = " + value;
    }
}

package photos.model;

import java.io.Serializable;
import java.util.Objects;

public class TagDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final boolean singleValue;

    public TagDefinition(String name, boolean singleValue) {
        this.name = name == null ? "" : name.trim();
        this.singleValue = singleValue;
    }

    public String getName() {
        return name;
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TagDefinition)) return false;
        TagDefinition other = (TagDefinition) obj;
        return name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return name + (singleValue ? " (single value)" : " (multi value)");
    }
}

package photos.model;

import java.util.Objects;

public class TagDefinition {
    private final String name;
    private final boolean singleValue;

    public TagDefinition(String name, boolean singleValue) {
        this.name = name;
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
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name + (singleValue ? " (single value)" : " (multi value)");
    }
}

package advisor.models;

import java.util.Objects;

public class Category {
    private final String id;
    private final String name;

    public Category(String categoryId, String name) {
        this.id = categoryId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id && Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}

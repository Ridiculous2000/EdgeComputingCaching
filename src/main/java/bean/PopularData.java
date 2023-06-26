package bean;

import java.util.Objects;

public class PopularData {
    int id = -1000;
    int size = -1000;

    public PopularData(){

    }
    public PopularData(int id, int size) {
        this.id = id;
        this.size = size;
    }

    public PopularData(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PopularData)) return false;
        PopularData that = (PopularData) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

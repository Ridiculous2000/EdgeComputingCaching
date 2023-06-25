package bean;

import java.util.ArrayList;
import java.util.Objects;


public class EdgeServer {
    double coveringRadius = -1000;
    int MaxStorageSpace = -1000;
    int id = -1000;
    double latitude = -1000;
    double longitude = -1000;
    int remainingStorageSpace = -1000;
    ArrayList<PopularData> cachedDataList = new ArrayList<PopularData>();

    public void cachePopularData(PopularData popularData){
        if(remainingStorageSpace>=popularData.getSize()){
            cachedDataList.add(popularData);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getMaximumStorageSpace() {
        return MaxStorageSpace;
    }

    public void setMaximumStorageSpace(int maximumStorageSpace) {
        MaxStorageSpace = maximumStorageSpace;
    }

    public int getRemainingStorageSpace() {
        return remainingStorageSpace;
    }

    public void setRemainingStorageSpace(int remainingStorageSpace) {
        this.remainingStorageSpace = remainingStorageSpace;
    }

    public ArrayList<PopularData> getCachedDataList() {
        return cachedDataList;
    }

    public void setCachedDataList(ArrayList<PopularData> cachedDataList) {
        this.cachedDataList = cachedDataList;
    }

    public double getCoveringRadius() {
        return coveringRadius;
    }

    public void setCoveringRadius(double coveringRadius) {
        this.coveringRadius = coveringRadius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EdgeServer)) return false;
        EdgeServer that = (EdgeServer) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

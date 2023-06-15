package bean;

import java.util.ArrayList;


public class EdgeServer {
    static double coveringRadius = 300;
    static int MaximumStorageSpace = 6;
    int id = 20000;
    double latitude = -1;
    double longitude = -1;
    int remainingStorageSpace = -1;
    ArrayList<PopularData> cachedDataList;

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
        return MaximumStorageSpace;
    }

    public void setMaximumStorageSpace(int maximumStorageSpace) {
        MaximumStorageSpace = maximumStorageSpace;
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

}
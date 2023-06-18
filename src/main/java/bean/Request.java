package bean;

public class Request {
    int id = -1000;
    int userId = -1000;
    int popularDataId = -1000;
    int timestamp = -1000;

    public Request(int id, int userId, int popularDataId, int timestamp) {
        this.id = id;
        this.userId = userId;
        this.popularDataId = popularDataId;
        this.timestamp = timestamp;
    }

    public Request(){}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPopularDataId() {
        return popularDataId;
    }

    public void setPopularDataId(int popularDataId) {
        this.popularDataId = popularDataId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

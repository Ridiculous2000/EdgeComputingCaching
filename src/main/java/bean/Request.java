package bean;

public class Request {
    int userId;
    int popularDataId;
    int timestamp;

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
}

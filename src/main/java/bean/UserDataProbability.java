package bean;

public class UserDataProbability {
    int userId = -1000;
    int dataId = -1000;
    double probability = -1000;

    public UserDataProbability(int userId, int dataId, double probability) {
        this.userId = userId;
        this.dataId = dataId;
        this.probability = probability;
    }
}

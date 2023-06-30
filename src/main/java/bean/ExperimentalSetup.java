package bean;

public class ExperimentalSetup {
    //相似度阈值，大于这个值的再考虑
    public static double similarityThreshold = 0.3;
    //最多要考虑的多少个相似数据
    public static int maxSimilarityNum = 80;
    //置信度阈值
    public static int confidenceThreshold = 5;
    //时间设置
    int BeginTimestamp = 51;
    int EndTimestamp = 80;
    //实验基础数据
    int ExperimentalServer = 40;
    int ExperimentalUser = 400;
    int ExperimentalData = 200;
    //实验设置
    int maxHop = 2;
    int maxStorageSpace = 3;
    int maxDataSize = 3;
    public int minsDataSize=1;
    //权重
    double latencyWeight = -1;
    double SimWeight = -6;
    public double FIndexWeight=1;
    public double SumQoEWeight=1;
    public double Z = 3;
    //遗传算法
    public int x=100;
    public int itrations=10;

    public void setMinsDataSize(int minsDataSize) {
        this.minsDataSize = minsDataSize;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setItrations(int itrations) {
        this.itrations = itrations;
    }


    public int getMinsDataSize() {
        return minsDataSize;
    }

    public int getX() {
        return x;
    }

    public int getItrations() {
        return itrations;
    }

    public static double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public static void setSimilarityThreshold(double similarityThreshold) {
        ExperimentalSetup.similarityThreshold = similarityThreshold;
    }

    public static int getMaxSimilarityNum() {
        return maxSimilarityNum;
    }

    public static void setMaxSimilarityNum(int maxSimilarityNum) {
        ExperimentalSetup.maxSimilarityNum = maxSimilarityNum;
    }

    public static int getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public static void setConfidenceThreshold(int confidenceThreshold) {
        ExperimentalSetup.confidenceThreshold = confidenceThreshold;
    }

    public int getBeginTimestamp() {
        return BeginTimestamp;
    }

    public void setBeginTimestamp(int beginTimestamp) {
        BeginTimestamp = beginTimestamp;
    }

    public int getEndTimestamp() {
        return EndTimestamp;
    }

    public void setEndTimestamp(int endTimestamp) {
        EndTimestamp = endTimestamp;
    }

    public int getExperimentalServer() {
        return ExperimentalServer;
    }

    public void setExperimentalServer(int experimentalServer) {
        ExperimentalServer = experimentalServer;
    }

    public int getExperimentalUser() {
        return ExperimentalUser;
    }

    public void setExperimentalUser(int experimentalUser) {
        ExperimentalUser = experimentalUser;
    }

    public int getExperimentalData() {
        return ExperimentalData;
    }

    public void setExperimentalData(int experimentalData) {
        ExperimentalData = experimentalData;
    }


    public int getMaxHop() {
        return maxHop;
    }

    public void setMaxHop(int maxHop) {
        this.maxHop = maxHop;
    }

    public double getLatencyWeight() {
        return latencyWeight;
    }

    public void setLatencyWeight(double latencyWeight) {
        this.latencyWeight = latencyWeight;
    }

    public double getSimWeight() {
        return SimWeight;
    }

    public void setSimWeight(double simWeight) {
        SimWeight = simWeight;
    }

    public double getFIndexWeight() {
        return FIndexWeight;
    }

    public void setFIndexWeight(double FIndexWeight) {
        this.FIndexWeight = FIndexWeight;
    }

    public double getSumQoEWeight() {
        return SumQoEWeight;
    }

    public void setSumQoEWeight(double sumQoEWeight) {
        SumQoEWeight = sumQoEWeight;
    }

    public double getZ() {
        return Z;
    }

    public void setZ(double z) {
        Z = z;
    }

    public int getMaxStorageSpace() {
        return maxStorageSpace;
    }

    public void setMaxStorageSpace(int maxStorageSpace) {
        this.maxStorageSpace = maxStorageSpace;
    }

    public int getMaxDataSize() {
        return maxDataSize;
    }

    public void setMaxDataSize(int maxDataSize) {
        this.maxDataSize = maxDataSize;
    }
}

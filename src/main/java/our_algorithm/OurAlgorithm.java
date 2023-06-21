package our_algorithm;

import bean.*;
import util.AlgorithmUtils;
import util.DBUtils;
import util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OurAlgorithm {
    public static int minTimestamp = 0;
    public static int maxTimestamp = 50;
    //参数设置和最大跳数严重相关
    public static double latencyWeight = -1;
    public static double simWeight = -6;
//    public static double delayThreshold = 1;
//    public static double simThreshold = 1;
    public static double Z = 3;
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    List<PopularData> experimentalPopularData;
    List<Request> predictiveRequest;
    EdgeServerGraph edgeServerGraph;
    Map<Integer,double[]> dataVectorMap;
    Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    Map<Integer,Integer> userNearestServer;
    public void InitializeData() throws IOException {
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        this.predictiveRequest = DBUtils.getAllRequestByTime("predictive_request",51,80);
        ArrayList<Integer> dataIdList = new ArrayList<Integer>();
        for(PopularData pd:this.experimentalPopularData){
            dataIdList.add(pd.getId());
        }
        this.dataVectorMap = FileUtils.getDataVectorMap("src/AlgorithmicData/data_matrix.txt",dataIdList);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        dataSimilarityMap = AlgorithmUtils.getDataSimilarityMap(dataVectorMap);
        userNearestServer = AlgorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);

        int dataId = dataIdList.get(1);
        int cacheDataId = dataIdList.get(2);
        int latency = 1;
        double QoE = CalculateQoE(dataId,cacheDataId,latency);
        int dataId2 = dataIdList.get(1);
        int cacheDataId2 = dataIdList.get(16);
        int latency2 = 2;
        double qoe = CalculateQoE(dataId2,cacheDataId2,latency2);
    }

    //带有软命中的QoE ： sigmoid(H(x))=1/(1+e^-H(x))
    // H(x) = lw*Latency+s*(1-sim)+Z
    //参数设置后的结果是 H(x) = -Latency[0-3] + (-6*(1-sim[0-1]))+3
    /*
        参数设置理由：
        (1) Latency=0,sim=0时候[最近获取不相关] H(x)=0+-6+3=-3,QoE = 0.047
        (2) Latency=3,sim=0时候[云端获取] H(x)=3+ -6 +3 = 0 QoE=0.5
        (3) Latency=0,sim=0.5时候[就近获取个一般的] H(x)=0+-3 + 3 = 0 QoE=0.5
     */
    private double CalculateQoE(int dataId, int cacheDataId, int latency) {
        double sim = 0;
        if(dataSimilarityMap.get(dataId).get(cacheDataId)!=null){
            sim = dataSimilarityMap.get(dataId).get(cacheDataId);
        }
        double weightValue = latencyWeight*(latency)+simWeight*(1 - sim) + Z;
        return 1 / (1 + Math.exp(-weightValue));
    }


}

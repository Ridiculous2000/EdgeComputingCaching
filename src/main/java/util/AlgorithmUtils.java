package util;

import bean.*;
import our_algorithm.OurAlgorithm;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static our_algorithm.OurAlgorithm.*;

public class AlgorithmUtils {
    //根据传入对象的经纬度，返回距离，单位是 m
    public static HashMap<Integer,HashSet<Integer>> userRequestData = new HashMap<>();
    public static Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    public static List<PopularData> experimentalPopularData;
    public static Map<Integer,double[]> dataVectorMap;
    public static Map<Integer,Integer> userNearestServer;
    public static List<User> experimentalUserList;
    public static List<EdgeServer> experimentalEdgeServer;
    public static EdgeServerGraph edgeServerGraph;
    static {
        ArrayList<Request> allRequest = (ArrayList<Request>) DBUtils.getAllRequestByTime("request",OurAlgorithm.minTimestamp,OurAlgorithm.maxTimestamp);
        for(Request r:allRequest){
            if(r.getTimestamp()<OurAlgorithm.minTimestamp||r.getTimestamp()>OurAlgorithm.maxTimestamp){
                continue;
            }
            if(!userRequestData.containsKey(r.getPopularDataId())){
                userRequestData.put(r.getPopularDataId(),new HashSet<>());
            }
            userRequestData.get(r.getPopularDataId()).add(r.getUserId());
        }

        experimentalPopularData = DBUtils.getAllPopularData();
        ArrayList<Integer> dataIdList = new ArrayList<Integer>();
        for(PopularData pd:experimentalPopularData){
            dataIdList.add(pd.getId());
        }
        try {
            dataVectorMap = FileUtils.getDataVectorMap("src/AlgorithmicData/data_matrix.txt",dataIdList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataSimilarityMap = AlgorithmUtils.getDataSimilarityMap(dataVectorMap);

        experimentalUserList = DBUtils.getAllUser();
        experimentalEdgeServer = DBUtils.getAllEdgeServer();
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) experimentalEdgeServer);
        userNearestServer = AlgorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
    }

    public static double calculateDistance(Object obj1,Object obj2){
        double lat1 = 50.0;
        double lon1 = 50.0;
        double lat2 = -50.0;
        double lon2 = -50.0;
        try {
            Class<?> class1 = obj1.getClass();
            Method getLat1 = class1.getMethod("getLatitude");
            Method getLon1 = class1.getMethod("getLongitude");
            lat1 = (double) getLat1.invoke(obj1);
            lon1 = (double) getLon1.invoke(obj1);

            Class<?> class2 = obj2.getClass();
            Method getLat2 = class2.getMethod("getLatitude");
            Method getLon2 = class2.getMethod("getLongitude");
            lat2 = (double) getLat2.invoke(obj2);
            lon2 = (double) getLon2.invoke(obj2);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return  calculateDistance(lat1, lon1, lat2, lon2);
    }

    // 根据经纬度，返回距离（m为单位）,距离很近直接近似为平面，基于37.8和144点位的经纬度数据计算
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dx = (lon2 - lon1)*82600;
        double dy = (lat2 - lat1)*111000;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

    //获取各个数据之间的相似度，dataId1 - dataId2 - sim.
    // minTime和maxTime写死成调用OurAlgorithmUtils的，因为矩阵分解的结果跟时间序列是绑定的
    public static Map<Integer, Map<Integer, Double>> getDataSimilarityMap(Map<Integer,double[]> dataVectorMap) {
        Map<Integer, Map<Integer, Double>> similarityMap = new HashMap<>();
        for (Map.Entry<Integer, double[]> firstEntry : dataVectorMap.entrySet()) {
            int firstDataId = firstEntry.getKey();
            double[] firstDataVector = firstEntry.getValue();
            Map<Integer, Double> similarDataMap = new HashMap<>();
            for (Map.Entry<Integer, double[]> lastEntry : dataVectorMap.entrySet()) {
                int lastDataId = lastEntry.getKey();
                double[] lastDataVector = lastEntry.getValue();
                // 计算相似度
                double similarity = calculateSimilarity(firstDataId,lastDataId,firstDataVector, lastDataVector, OurAlgorithm.minTimestamp,OurAlgorithm.maxTimestamp);
                if (similarity > similarityThreshold && firstDataId != lastDataId && similarDataMap.size()<maxSimilarityNum) {
                    similarDataMap.put(lastDataId,similarity);
                }
            }
            similarityMap.put(firstDataId, similarDataMap);
        }
        return similarityMap;
    }

    //计算相似度
    private static double calculateSimilarity(int dataId1,int dataId2,double[] dataVector1, double[] dataVector2,int minTimestamp,int maxTimestamp) {
        if(dataId1==dataId2){
            return 1;
        }
        //得到请求的用户的娇集
        HashSet<Integer> userForData1 = userRequestData.get(dataId1);
        HashSet<Integer> userForData2 = userRequestData.get(dataId2);
//        HashSet<Integer> userForData3 = DBUtils.getUserSetByDataId("request",dataId1,minTimestamp,maxTimestamp);
//        HashSet<Integer> userForData4 = DBUtils.getUserSetByDataId("request",dataId2,minTimestamp,maxTimestamp);
        HashSet<Integer> intersection = new HashSet<>(userForData1);
        intersection.retainAll(userForData2);
        // size of jiao!
        int intersectionSize = intersection.size();
        //余弦相似度
        double cosSim = calculateCosSim(dataVector1,dataVector2);
        double similarity = cosSim;
        //置信因子
        if(intersectionSize<confidenceThreshold){
            similarity *= intersectionSize/confidenceThreshold;
        }
        return similarity;
    }

    //向量余弦相似度
    private static double calculateCosSim(double[] dataVector1, double[] dataVector2) {
        if (dataVector1.length != dataVector2.length) {
            throw new IllegalArgumentException("Vector lengths are not equal.");
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < dataVector1.length; i++) {
            dotProduct += dataVector1[i] * dataVector2[i];
            norm1 += Math.pow(dataVector1[i], 2);
            norm2 += Math.pow(dataVector2[i], 2);
        }
        // 计算向量范数
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        // 计算余弦相似度
        double cosineSimilarity = dotProduct / (norm1 * norm2);
        return cosineSimilarity;
    }


    //为每个用户匹配最近的服务器，用户发送的请求默认发送到最近的服务器处理
    public static Map<Integer, Integer> getUserNearestServer(List<User> experimentalUserList, List<EdgeServer> experimentalEdgeServer) {
        HashMap<Integer,Integer> userNearestServer = new HashMap<Integer,Integer>();
        for (User user : experimentalUserList) {
            double minDistance = Double.MAX_VALUE;
            int nearestEdgeServerId = -1;
            for (EdgeServer edgeServer : experimentalEdgeServer) {
                double distance = calculateDistance(user, edgeServer);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestEdgeServerId = edgeServer.getId();
                }
            }
            if (nearestEdgeServerId != -1) {
                userNearestServer.put(user.getId(), nearestEdgeServerId);
            }
        }
        return  userNearestServer;
    }

    public static double calculateQoE(int latency) {
        double sim = 1;
        double weightValue = latencyWeight*(latency) + Z;
        return 1 / (1 + Math.exp(-weightValue));
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
    public static double calculateQoE(int requestDataId, int cacheDataId, int latency) {
        double sim = 0;
        if(dataSimilarityMap.get(requestDataId).get(cacheDataId)!=null){
            sim = dataSimilarityMap.get(requestDataId).get(cacheDataId);
        }
        double weightValue = latencyWeight*(latency)+SimWeight*(1 - sim) + Z;
        return 1 / (1 + Math.exp(-weightValue));
    }

    //根据缓存情况和当前请求，返回总QoE
    public static double cacheDecisionSumQoE(CachingDecision cachingDecision,ArrayList<Request> allRequest){
        Map<EdgeServer, HashSet<PopularData>> cacheState = cachingDecision.getCachingState();
        double sumQoE = 0;
        for(Request r:allRequest){
            double maxQoE = 0;
            int nearestServerId = userNearestServer.get(r.getUserId());
//            EdgeServer nearestServer = edgeServerGraph.getAllEdgeServer().get(edgeServerGraph.getEdgeServerIdToIndex().get(nearestServerId));
            HashMap<Integer,ArrayList<EdgeServer>> connectedServer = edgeServerGraph.getDistanceRank().get(nearestServerId);
            for(Map.Entry<Integer,ArrayList<EdgeServer>> entry:connectedServer.entrySet()){
                int lantency = entry.getKey();
                if(lantency>maxHop){
                    lantency = 3;
                    maxQoE = Math.max(maxQoE,calculateQoE(3));
                    continue;
                }
                ArrayList<PopularData>  allPopularData = new ArrayList<>();
                ArrayList<EdgeServer> serverArrayList = entry.getValue();
                for(EdgeServer edgeServer:serverArrayList){
                    for(PopularData popularData:cacheState.get(edgeServer)){
                        maxQoE = Math.max(maxQoE,calculateQoE(r.getPopularDataId(),popularData.getId(),lantency));
                    }
                }
            }
            sumQoE += maxQoE;
        }
        cachingDecision.setSumQoE(sumQoE);
        return sumQoE;
    }

    //根据缓存情况和请求，返回QoE的公平系数
    public static double cacheDecisionFIndex(CachingDecision cachingDecision,ArrayList<Request> allRequest){
        Map<EdgeServer, HashSet<PopularData>> cacheState = cachingDecision.getCachingState();
        ArrayList<Double> userQoE = new ArrayList<>();
        for(Request r:allRequest){
            double maxQoE = 0;
            int nearestServerId = userNearestServer.get(r.getUserId());
            HashMap<Integer,ArrayList<EdgeServer>> connectedServer = edgeServerGraph.getDistanceRank().get(nearestServerId);
            for(Map.Entry<Integer,ArrayList<EdgeServer>> entry:connectedServer.entrySet()){
                int lantency = entry.getKey();
                if(lantency>maxHop){
                    lantency = 3;
                    maxQoE = Math.max(maxQoE,calculateQoE(3));
                    continue;
                }
                ArrayList<PopularData>  allPopularData = new ArrayList<>();
                ArrayList<EdgeServer> serverArrayList = entry.getValue();
                for(EdgeServer edgeServer:serverArrayList){
                    for(PopularData popularData:cacheState.get(edgeServer)){
                        maxQoE = Math.max(maxQoE,calculateQoE(r.getPopularDataId(),popularData.getId(),lantency));
                    }
                }
            }
            userQoE.add(maxQoE);
        }
        double FIndex = 1-2*calculateStandardDeviation(userQoE)/(1 / (1 + Math.exp(-6)) - 1 / (1 + Math.exp(6)));
        cachingDecision.setFIndexQoE(FIndex);
        return FIndex;
    }


    //根据缓存情况和请求，返回QoE的公平系数
    public static double cacheDecisionFinalValue(CachingDecision cachingDecision,ArrayList<Request> allRequest,double maxSumQoE){
       double FIndex = cacheDecisionFIndex(cachingDecision,allRequest);
       double sumQoE = cacheDecisionSumQoE(cachingDecision,allRequest);
       return (FIndexWeight*FIndex + SumQoEWeight*(sumQoE/maxSumQoE))/(FIndexWeight+SumQoEWeight);
    }

    //根据存储情况和请求，返回各个用户请求的QoE【目前设置中，一个用户每个时间段只发出一个请求，所以也是用户QoE】
    public static HashMap<Request,Double> cacheDecisionAllUserQoE(CachingDecision cachingDecision,ArrayList<Request> allRequest){
        Map<EdgeServer, HashSet<PopularData>> cacheState = cachingDecision.getCachingState();
        HashMap<Request,Double> userQoE = new HashMap<>();
        for(Request r:allRequest){
            double maxQoE = 0;
            int nearestServerId = userNearestServer.get(r.getUserId());
            HashMap<Integer,ArrayList<EdgeServer>> connectedServer = edgeServerGraph.getDistanceRank().get(nearestServerId);
            for(Map.Entry<Integer,ArrayList<EdgeServer>> entry:connectedServer.entrySet()){
                int lantency = entry.getKey();
                if(lantency>maxHop){
                    lantency = 3;
                    maxQoE = Math.max(maxQoE,calculateQoE(3));
                    continue;
                }
                ArrayList<PopularData>  allPopularData = new ArrayList<>();
                ArrayList<EdgeServer> serverArrayList = entry.getValue();
                for(EdgeServer edgeServer:serverArrayList){
                    for(PopularData popularData:cacheState.get(edgeServer)){
                        maxQoE = Math.max(maxQoE,calculateQoE(r.getPopularDataId(),popularData.getId(),lantency));
                    }
                }
            }
            userQoE.put(r,maxQoE);
        }
        return userQoE;

    }


    //返回序列标准差
    public static double calculateStandardDeviation(ArrayList<Double> numbers) {
        int size = numbers.size();
        if (size < 2) {
            throw new IllegalArgumentException("ArrayList should have at least 2 elements.");
        }
        double sum = 0.0;
        for (Double number : numbers) {
            sum += number;
        }
        double mean = sum / size;
        double squaredDiffSum = 0.0;
        for (Double number : numbers) {
            double diff = number - mean;
            squaredDiffSum += (diff * diff);
        }
        double variance = squaredDiffSum / size;
        return Math.sqrt(variance);
    }



}

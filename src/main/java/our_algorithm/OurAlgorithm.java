package our_algorithm;

import bean.*;
import util.AlgorithmUtils;
import util.DBUtils;
import util.FileUtils;

import java.io.*;
import java.util.*;

import static bean.ExperimentalSetup.similarityThreshold;

public class OurAlgorithm {
    //最大跳数
    public int maxHop;
    //参数设置和最大跳数严重相关
    public double latencyWeight;
    public double SimWeight;
//    public static double delayThreshold = 1;
//    public static double simThreshold = 1;
    public double SumQoEWeight;
    public double FIndexWeight;
    public double Z;
    public int minTimestamp;
    public int maxTimestamp;
    public int maxStorageSpace;
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    List<PopularData> experimentalPopularData;
    HashMap<Integer,Integer> dataIdToIndex = new HashMap<>();
    HashMap<Integer,ArrayList<Request>> predictiveRequest;
    EdgeServerGraph edgeServerGraph;
    Map<Integer,double[]> dataVectorMap;
    Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    Map<Integer,Integer> userNearestServer;
    HashMap<Request,ArrayList<ServerDataPair>> requestToCache = new HashMap();
    HashMap<ServerDataPair,ArrayList<Request>> cacheToRequest = new HashMap<>();
    HashMap<ServerDataPair,Double> remCacheUtility = new HashMap<>();
    AlgorithmUtils algorithmUtils;

    public OurAlgorithm(ExperimentalSetup experimentalSetup) throws IOException {
        this.maxHop = experimentalSetup.getMaxHop();
        this.latencyWeight = experimentalSetup.getLatencyWeight();
        this.SimWeight = experimentalSetup.getSimWeight();
        this.SumQoEWeight = experimentalSetup.getSumQoEWeight();
        this.FIndexWeight = experimentalSetup.getFIndexWeight();
        this.Z = experimentalSetup.getZ();
        this.minTimestamp = experimentalSetup.getBeginTimestamp();
        this.maxTimestamp = experimentalSetup.getEndTimestamp();
        this.maxStorageSpace = experimentalSetup.getMaxStorageSpace();
        this.algorithmUtils = new AlgorithmUtils(experimentalSetup);
        initializeData();
    }

    public void getHawkesEvents(int timestamp,String filePath) {
        ArrayList<Request> historyRequest = (ArrayList<Request>) DBUtils.getAllRequestByTime("request",0,timestamp);
        HashMap<UserDataPair,ArrayList<Integer>> userDataMap = new HashMap<UserDataPair,ArrayList<Integer>>();
        //记录各个服务器被请求的情况
        for(Request r:historyRequest){
            int dataId = r.getPopularDataId();
            int userId = r.getUserId();
            UserDataPair userDataPair = new UserDataPair(userId,dataId);
            if(userDataMap.get(userDataPair)==null){
                userDataMap.put(userDataPair,new ArrayList<>());
            }
            userDataMap.get(userDataPair).add(r.getTimestamp());
        }

        // 创建文件路径和文件名
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<UserDataPair, ArrayList<Integer>> entry : userDataMap.entrySet()) {
                UserDataPair userDataPair = entry.getKey();
                ArrayList<Integer> dataList = entry.getValue();
                int userId = userDataPair.userId;
                int dataId = userDataPair.dataId;

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(userId).append(" - ").append(dataId).append(":");

                for (Integer data : dataList) {
                    stringBuilder.append(data).append(",");
                }
                if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }

                String output = stringBuilder.toString();
                writer.write(output);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void addPredictRequest(){
        String filePath = "D:\\JavaProject\\EdgeComputingCaching\\src\\AlgorithmicData\\hawkes_predicte.txt";
        HashMap<Integer, ArrayList<Integer>> userMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                int userId = Integer.parseInt(parts[0]);
                String[] dataIds = parts[1].split(",");
                ArrayList<Integer> dataIdList = new ArrayList<>();
                for (String dataId : dataIds) {
                    dataIdList.add(Integer.parseInt(dataId));
                }
                userMap.put(userId, dataIdList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int userId : userMap.keySet()) {
            ArrayList<Integer> dataIdList = userMap.get(userId);
            System.out.println("User ID: " + userId + ", Data IDs: " + dataIdList);
        }

        for (Map.Entry<Integer,ArrayList<Integer>> entry:userMap.entrySet()){
            int userId = entry.getKey();
            ArrayList<Integer> dataIdList = entry.getValue();

        }

    }


    public class UserDataPair{
        int userId;
        int dataId;

        public UserDataPair(int userId, int dataId) {
            this.userId = userId;
            this.dataId = dataId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserDataPair)) return false;
            UserDataPair that = (UserDataPair) o;
            return userId == that.userId && dataId == that.dataId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, dataId);
        }
    }

    //缓存对
    public class ServerDataPair implements Comparable<ServerDataPair> {
        int serverId;
        int dataId;

        //对所有请求的效用增量和
        double sumDeltaUtility;

        public ServerDataPair() {}

        public ServerDataPair(int serverId, int dataId, double sumDeltaUtility) {
            this.serverId = serverId;
            this.dataId = dataId;
            this.sumDeltaUtility = sumDeltaUtility;
        }

        public ServerDataPair(int serverId, int dataId) {
            this.serverId = serverId;
            this.dataId = dataId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ServerDataPair)) return false;
            ServerDataPair that = (ServerDataPair) o;
            return serverId == that.serverId && dataId == that.dataId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(serverId, dataId);
        }

        @Override
        public int compareTo(ServerDataPair other) {
            // 按照 sumDeltaUtility 从大到小排序
            return Double.compare(other.sumDeltaUtility, this.sumDeltaUtility);
        }
    }

    public class UserQoEPair implements Comparable<UserQoEPair>{
        int userId;
        int requestId;
        int dataId;
        double QoE;

        public UserQoEPair(int userId, int dataId, double qoE) {
            this.userId = userId;
            this.dataId = dataId;
            QoE = qoE;
        }


        @Override
        public int compareTo(UserQoEPair other) {
            return Double.compare(this.QoE,other.QoE);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserQoEPair)) return false;
            UserQoEPair that = (UserQoEPair) o;
            return userId == that.userId && dataId == that.dataId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, dataId);
        }
    }

    //初始化要用的数据
    public void initializeData() throws IOException {
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        this.predictiveRequest = DBUtils.getRequestByTime("predictive_request",minTimestamp,maxTimestamp);
        ArrayList<Integer> dataIdList = new ArrayList<Integer>();
        for(PopularData pd:this.experimentalPopularData){
            dataIdList.add(pd.getId());
        }
        for(int i=0;i<experimentalPopularData.size();i++){
            dataIdToIndex.put(experimentalPopularData.get(i).getId(),i);
        }
        dataVectorMap = FileUtils.getDataVectorMap("src/AlgorithmicData/data_matrix.txt",dataIdList);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        dataSimilarityMap = algorithmUtils.getDataSimilarityMap(dataVectorMap);
        userNearestServer = algorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
    }

    public CachingDecision findBestDecision(int timestamp){
        //初始化数据
        initAlgorithmicTempData(timestamp);
        //拿到这一轮预测的请求
        ArrayList<Request> predictiveRequest = this.predictiveRequest.get(timestamp);
        //缓存决策
        CachingDecision cachingDecision = new CachingDecision();
        //把edgeServerGraph里面的提出来便于使用
//        HashMap<Integer, List<EdgeServer>> serverGraph = edgeServerGraph.getServerGraph();
//        HashMap<Integer,HashMap<Integer,ArrayList<EdgeServer>>> distanceRank = edgeServerGraph.getDistanceRank();
        int[][] allDistance = edgeServerGraph.getEveryDistance();
        HashMap<Integer,Integer> serverIdToIndex = edgeServerGraph.getEdgeServerIdToIndex();
        HashMap<Integer,Integer> dataIdToIndex = new HashMap<>();
        for(int i=0;i<experimentalPopularData.size();i++){
            dataIdToIndex.put(experimentalPopularData.get(i).getId(),i);
        }
        //总缓存空间
        int sumAvailableSpace = experimentalEdgeServer.size()*maxStorageSpace;
        //缓存对 对每个请求的效用
        HashMap<ServerDataPair,HashMap<Request,Double>> cacheRequestUtility = new HashMap<>();
        //缓存对所有请求的效用增量
        HashMap<ServerDataPair,HashMap<Request,Double>> cacheRequestDeltaUtility = new HashMap<>();
        //缓存对所有请求的总效用增量
        HashMap<ServerDataPair,Double> cacheSumDeltaUtility = new HashMap<>();
        //缓存对的优先队列
        PriorityQueue<ServerDataPair> utilityPriorityQueue = new PriorityQueue<>();
        //当前存储情况下，各个请求的效用
        HashMap<Request,Double> nowRequestUtility = new HashMap<>();
//        HashMap<Request,Double> nextRequestUtility = new HashMap<>();
        for(Request r:predictiveRequest){
            nowRequestUtility.put(r,0.0);
//            nextRequestUtility.put(r,0.0);
        }
        //计算：（1）每个存储对 对请求的效用  （2）每个存储对 对所有请求的总效用
        // 计算相关的缓存对的效用
        for (Map.Entry<ServerDataPair, ArrayList<Request>> entry : cacheToRequest.entrySet()) {
            ServerDataPair cache = entry.getKey();
            ArrayList<Request> requests = entry.getValue();
            HashMap<Request, Double> requestUtility = new HashMap<>();
            double sumUtility = 0.0;
            for (Request request : requests) {
                //最近服务器和缓存了数据的服务器之间的距离
                int nearestServerIndex = serverIdToIndex.get(userNearestServer.get(request.getUserId()));
                int cacheServerIndex = serverIdToIndex.get(cache.serverId);
                int latency = allDistance[nearestServerIndex][cacheServerIndex];
                // 请求的数据和缓存的数据
                double utility = algorithmUtils.calculateQoE(request.getPopularDataId(),cache.dataId,latency);
                requestUtility.put(request, utility);
                sumUtility += utility;
            }
            // 记录增量，当前效用为0，所以增量就是每个请求的效用
            cacheRequestDeltaUtility.put(cache,requestUtility);
            cacheRequestUtility.put(cache, requestUtility);
            cacheSumDeltaUtility.put(cache, sumUtility);
            // 添加队列
            utilityPriorityQueue.add(new ServerDataPair(cache.serverId,cache.dataId,sumUtility));
        }
        //还有空间
        while (sumAvailableSpace>0&&!utilityPriorityQueue.isEmpty()){
            //找到总效用增量最大的缓存对
            ServerDataPair bestServerDataPair = utilityPriorityQueue.poll();
            // 缓存对的情况
            EdgeServer edgeServer = experimentalEdgeServer.get(serverIdToIndex.get(bestServerDataPair.serverId));
            PopularData popularData = experimentalPopularData.get(dataIdToIndex.get(bestServerDataPair.dataId));
            //不能存继续找合适的
            if(edgeServer.getRemainingStorageSpace()<popularData.getSize()){
                continue;
            }else{
                //缓存这个数据
                edgeServer.cachePopularData(popularData);
                sumAvailableSpace -= popularData.getSize();
                remCacheUtility.put(bestServerDataPair,bestServerDataPair.sumDeltaUtility);
                //找到缓存对相关的请求
                ArrayList<Request> relatedRequest = cacheToRequest.get(bestServerDataPair);
                //更新当前效用
                for(Request request:relatedRequest){
                    nowRequestUtility.put(request,Math.max(nowRequestUtility.get(request),cacheRequestUtility.get(bestServerDataPair).get(request)));
                }
                //更新现在各个缓存对 对各个请求的效用增量
                for(Request r:relatedRequest){
                    // 找到请求相关的缓存对
                    ArrayList<ServerDataPair> relatedCache = requestToCache.get(r);
                    for(ServerDataPair serverDataPair:relatedCache){
                        double requestUtility = cacheRequestUtility.get(serverDataPair).get(r);
                        double deletUtility = Math.max(requestUtility-nowRequestUtility.get(r),0);
                        cacheRequestDeltaUtility.get(serverDataPair).put(r,deletUtility);
                    }
                }
                for(Map.Entry<ServerDataPair,HashMap<Request,Double>> entry:cacheRequestDeltaUtility.entrySet()){
                    HashMap<Request,Double> requestUtility = entry.getValue();
                    double sumDeltaUtility = 0;
                    for(Map.Entry<Request,Double> ru:requestUtility.entrySet()){
                        sumDeltaUtility+=ru.getValue();
                    }
                    cacheSumDeltaUtility.put(entry.getKey(),sumDeltaUtility);
                }

                utilityPriorityQueue.clear();
                for(Map.Entry<ServerDataPair,Double> entry:cacheSumDeltaUtility.entrySet()){
                    utilityPriorityQueue.add(new ServerDataPair(entry.getKey().serverId,entry.getKey().dataId,entry.getValue()));
                }
            }
        }

        Map<EdgeServer, HashSet<PopularData>> cachingResult = new HashMap<>();
        //保存第一步的最优解
        for(EdgeServer edgeServer:experimentalEdgeServer){
            ArrayList<PopularData> dataList =  edgeServer.getCachedDataList();
            if(cachingResult.get(edgeServer)==null){
                cachingResult.put(edgeServer,new HashSet<>());
            }
            for(PopularData popularData:dataList){
                cachingResult.get(edgeServer).add(popularData);
            }
        }
        cachingDecision.setCachingState(cachingResult);


        double maxSumQoE = algorithmUtils.cacheDecisionSumQoE(cachingDecision,predictiveRequest);
        double startFIndex = algorithmUtils.cacheDecisionFIndex(cachingDecision,predictiveRequest);
        HashMap<Request,Double> allUserQoE = algorithmUtils.cacheDecisionAllUserQoE(cachingDecision,predictiveRequest);
        PriorityQueue<UserQoEPair> lowQoEUserQueue = new PriorityQueue<>();
        for(Map.Entry<Request,Double> entry:allUserQoE.entrySet()){
            Request r = entry.getKey();
            lowQoEUserQueue.add(new UserQoEPair(r.getUserId(),r.getPopularDataId(),entry.getValue()));
        }

//        Map<EdgeServer, HashSet<PopularData>> startCacheResult = cachingDecision.getCachingState();
//        Map<EdgeServer, HashSet<PopularData>> endCacheResult = cachingDecision.getCachingState();

        /*
            SumQoE = 311.78
            FIndex = 0.7299
            FinalValue = 0.909967
         */
        while(lowQoEUserQueue.size()>allUserQoE.size()/2+1){
            UserQoEPair userQoEPair = lowQoEUserQueue.poll();
            int userId = userQoEPair.userId;
            int dataId = userQoEPair.dataId;
            int dataIndex = dataIdToIndex.get(dataId);
            PopularData toCacheData = experimentalPopularData.get(dataIndex);
            int nearestServerId = userNearestServer.get(userId);
            EdgeServer edgeServer = experimentalEdgeServer.get(edgeServerGraph.getEdgeServerIdToIndex().get(nearestServerId));
            HashSet<PopularData> popularDataSet = cachingDecision.getCachingState().get(edgeServer);
            double beforeFinalValue = algorithmUtils.cacheDecisionFinalValue(cachingDecision,predictiveRequest);
            int minQoEDataId = 0;
            PopularData minQoEData = new PopularData();
            double minDataQoE = 9999;
            for(PopularData popularData:popularDataSet){
                double utility = 0;
                if(remCacheUtility.get(new ServerDataPair(nearestServerId,popularData.getId()))!=null){
                    utility = remCacheUtility.get(new ServerDataPair(nearestServerId,popularData.getId()));
                }else{
                    continue;
                }
                if(utility<minDataQoE&&popularData.getSize()>=toCacheData.getSize()){
                    minDataQoE = utility;
                    minQoEDataId = popularData.getId();
                    minQoEData = popularData;
                }
            }
            if(minDataQoE==9999){
                continue;
            }
            //进行替换
            popularDataSet.remove(new PopularData(minQoEDataId));
            popularDataSet.add(toCacheData);
            //计算，看看总值变化没
            double afterFinalValue = algorithmUtils.cacheDecisionFinalValue(cachingDecision,predictiveRequest);
            //变差了就再换回来
            if(afterFinalValue<beforeFinalValue){
                popularDataSet.remove(toCacheData);
                popularDataSet.add(minQoEData);
                double nowFinalValue = algorithmUtils.cacheDecisionFinalValue(cachingDecision,predictiveRequest);
            }
        }
        double finalSumQoE = algorithmUtils.cacheDecisionSumQoE(cachingDecision,predictiveRequest);
        double finalFIndex = algorithmUtils.cacheDecisionFIndex(cachingDecision,predictiveRequest);
        double result = algorithmUtils.cacheDecisionFinalValue(cachingDecision,predictiveRequest);
        cachingDecision.setFIndexQoE(finalFIndex);
        cachingDecision.setOptimizationObjective(result);
        System.out.println("第 " + timestamp + " 时刻最终结果 SumQoE: "+finalSumQoE+" —— " + "FIndex: "+finalFIndex + " —— "+"FinalValue: "+result);
        return cachingDecision;
    }





    //初始化，每个请求会影响多少个缓存对，以及每个缓存对会影响多少个请求
    public void initAlgorithmicTempData(int timestamp){
        this.requestToCache = new HashMap<>();
        this.cacheToRequest = new HashMap<>();
        ArrayList<Request> predictiveRequest = this.predictiveRequest.get(timestamp);
        HashMap<Integer, List<EdgeServer>> serverGraph = edgeServerGraph.getServerGraph();
        HashMap<Integer,HashMap<Integer,ArrayList<EdgeServer>>> distanceRank = edgeServerGraph.getDistanceRank();
        int[][] allDistance = edgeServerGraph.getEveryDistance();
        HashMap<Integer,Integer> serverIdToIndex = edgeServerGraph.getEdgeServerIdToIndex();
        int sumSpace = experimentalEdgeServer.size()*maxStorageSpace;
        //缓存每个请求可以被那些缓存对影响
        for(Request r:predictiveRequest){
            int dataId = r.getPopularDataId();
            int userId = r.getUserId();
            int serverId = userNearestServer.get(userId);
            HashMap<Integer, ArrayList<EdgeServer>> distanceMap = edgeServerGraph.getDistanceRank().get(serverId);
            ArrayList<EdgeServer> allRelatedServer = new ArrayList<>();
            for(Map.Entry<Integer, ArrayList<EdgeServer>> entry:distanceMap.entrySet()){
                if(entry.getKey()<=maxHop){
                    allRelatedServer.addAll(entry.getValue());
                }
            }
            ArrayList<Integer> allRelatedData = new ArrayList<>();
            HashMap<Integer,Double> relatedDataMap = (HashMap<Integer,Double>)dataSimilarityMap.get(dataId);
            for(Map.Entry<Integer,Double> entry:relatedDataMap.entrySet()){
                if(entry.getValue()>similarityThreshold){
                    allRelatedData.add(entry.getKey());
                }
            }

            ArrayList<ServerDataPair> relatedServerDataPair = new ArrayList<>();
            for(int i=0;i<allRelatedServer.size();i++){
                for(int j=0;j<allRelatedData.size();j++){
                    ServerDataPair serverDataPair = new ServerDataPair(allRelatedServer.get(i).getId(),allRelatedData.get(j));
                    relatedServerDataPair.add(serverDataPair);
                }
            }
            requestToCache.put(r,relatedServerDataPair);
        }

        //缓存每个缓存对可以影响到的请求有哪些
        for (Map.Entry<Request, ArrayList<ServerDataPair>> entry : requestToCache.entrySet()) {
            Request request = entry.getKey();
            ArrayList<ServerDataPair> cachePairList = entry.getValue();
            for (ServerDataPair serverDataPair : cachePairList) {
                if(cacheToRequest.get(serverDataPair)==null){
                    cacheToRequest.put(serverDataPair,new ArrayList<Request>());
                }
                cacheToRequest.get(serverDataPair).add(request);
            }
        }

    }


}


//        //找到总效用增量最大的缓存对
//        while (!utilityPriorityQueue.isEmpty()&&sumAvailableSpace>0){
//            ServerDataPair bestServerDataPair = utilityPriorityQueue.poll();
//            // 缓存对数据未过时
//            if(bestServerDataPair.sumDeltaUtility==cacheSumUtility.get(bestServerDataPair)){
//                EdgeServer edgeServer = experimentalEdgeServer.get(serverIdToIndex.get(bestServerDataPair.serverId));
//                PopularData popularData = experimentalPopularData.get(dataIdToIndex.get(bestServerDataPair.dataId));
//                // 有空间可以存储
//                if(edgeServer.getRemainingStorageSpace()>=popularData.getSize()){
//                    //debug
//                    debugCacheList.add(bestServerDataPair);
//                    //缓存这个数据
//                    edgeServer.cachePopularData(popularData);
//                    sumAvailableSpace -= popularData.getSize();
//                    //找到缓存对相关的请求
//                    HashMap<Request,Double> relatedRequest = cacheRequestUtility.get(bestServerDataPair);
//                    // 效用更新过的请求
//                    ArrayList<Request> updatedRequest = new ArrayList<>();
//                    for(Map.Entry<Request,Double> entry:relatedRequest.entrySet()){
//                        if(entry.getValue()>nextRequestUtility.get(entry.getKey())){
//                            updatedRequest.add(entry.getKey());
//                            nextRequestUtility.put(entry.getKey(),entry.getValue());
//                        }
//                    }
//
//                    HashSet<Integer> debugDataSet = new HashSet<>();
//                    for(Request r:updatedRequest){
//                        debugDataSet.add(r.getPopularDataId());
//                    }
//
//
//                    for(Request r:updatedRequest){
//                        ArrayList<ServerDataPair> relatedCache = requestToCache.get(r);
//                        double bestCacheRequestUtility = relatedRequest.get(r);
//                        for(ServerDataPair serverDataPair:relatedCache){
//                            double relatedCacheRequestUtility = cacheRequestUtility.get(serverDataPair).get(r);
////                            double deltaRequestUtility = nextRequestUtility.get(r)-nowRequestUtility.get(r);
////                            double promotionUtility = relatedCacheRequestUtility - nowRequestUtility.get(r);
//                            double csu = cacheSumUtility.get(serverDataPair);
//                            double nru = nextRequestUtility.get(r);
//                            double nowRU = nowRequestUtility.get(r);
//                            double newSumUtility = 0.0;
////                            if(nowRequestUtility.get(r)==null){
////                                System.out.println("bbb");
////                            }
//                            if(relatedCacheRequestUtility<=nowRequestUtility.get(r)){
//                                continue;
//                            }
//                            if(relatedCacheRequestUtility>nextRequestUtility.get(r)){
//                                newSumUtility = cacheSumUtility.get(serverDataPair) - (relatedCacheRequestUtility-nextRequestUtility.get(r));
//                            }else{
//                                newSumUtility = cacheSumUtility.get(serverDataPair)-(relatedCacheRequestUtility-nowRequestUtility.get(r));
//                            }
////                            double newSumUtility = cacheSumUtility.get(serverDataPair)-(Math.min(nextRequestUtility.get(r),relatedCacheRequestUtility)-nowRequestUtility.get(r));
////                            if(newSumUtility>cacheSumUtility.get(serverDataPair)){
////                                System.out.println("aaa");
////                            }
//                            cacheSumUtility.put(serverDataPair,newSumUtility);
//                            utilityPriorityQueue.add(new ServerDataPair(serverDataPair.serverId,serverDataPair.dataId,newSumUtility));
//                        }
//                    }
//                    for(Map.Entry<Request,Double> entry:relatedRequest.entrySet()){
//                        if(entry.getValue()>nowRequestUtility.get(entry.getKey())){
//                            nowRequestUtility.put(entry.getKey(),entry.getValue());
//                        }
//                    }
//                }
//            }
//        }
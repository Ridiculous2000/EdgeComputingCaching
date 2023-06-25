package our_algorithm;

import bean.*;
import util.AlgorithmUtils;
import util.DBUtils;
import util.FileUtils;

import java.io.IOException;
import java.util.*;

public class OurAlgorithm {
    public static int minTimestamp = 0;
    public static int maxTimestamp = 50;
    public static int maxStorageSpace = 3;
    //���ƶ���ֵ���������ֵ���ٿ���
    public static double similarityThreshold = 0.2;
    //���Ҫ���ǵĶ��ٸ���������
    public static int maxSimilarityNum = 150;
    //���Ŷ���ֵ
    public static int confidenceThreshold = 10;
    //�������
    public static int maxHop = 2;
    //�������ú���������������
    public static double latencyWeight = -1;
    public static double SimWeight = -6;
//    public static double delayThreshold = 1;
//    public static double simThreshold = 1;
    public static double Z = 3;
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    List<PopularData> experimentalPopularData;
    HashMap<Integer,ArrayList<Request>> predictiveRequest;
    EdgeServerGraph edgeServerGraph;
    Map<Integer,double[]> dataVectorMap;
    Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    Map<Integer,Integer> userNearestServer;
    HashMap<Request,ArrayList<ServerDataPair>> requestToCache = new HashMap();
    HashMap<ServerDataPair,ArrayList<Request>> cacheToRequest = new HashMap<>();

    //�����
    public class ServerDataPair implements Comparable<ServerDataPair> {
        int serverId;
        int dataId;

        //�����������Ч��������
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
            // ���� sumDeltaUtility �Ӵ�С����
            return Double.compare(other.sumDeltaUtility, this.sumDeltaUtility);
        }
    }

    //��ʼ��Ҫ�õ�����
    public void initializeData() throws IOException {
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        this.predictiveRequest = DBUtils.getRequestByTime("predictive_request",51,80);
        ArrayList<Integer> dataIdList = new ArrayList<Integer>();
        for(PopularData pd:this.experimentalPopularData){
            dataIdList.add(pd.getId());
        }
        this.dataVectorMap = FileUtils.getDataVectorMap("src/AlgorithmicData/data_matrix.txt",dataIdList);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        dataSimilarityMap = AlgorithmUtils.getDataSimilarityMap(dataVectorMap);
        userNearestServer = AlgorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
    }

    
    public CachingDecision findMaxQoEDecision(int timestamp){
        //��ʼ������
        initAlgorithmicTempData(timestamp);
        //�õ���һ��Ԥ�������
        ArrayList<Request> predictiveRequest = this.predictiveRequest.get(timestamp);
        //�������
        CachingDecision cachingDecision = new CachingDecision();
        //��edgeServerGraph��������������ʹ��
//        HashMap<Integer, List<EdgeServer>> serverGraph = edgeServerGraph.getServerGraph();
//        HashMap<Integer,HashMap<Integer,ArrayList<EdgeServer>>> distanceRank = edgeServerGraph.getDistanceRank();
        int[][] allDistance = edgeServerGraph.getEveryDistance();
        HashMap<Integer,Integer> serverIdToIndex = edgeServerGraph.getEdgeServerIdToIndex();
        HashMap<Integer,Integer> dataIdToIndex = new HashMap<>();
        for(int i=0;i<experimentalPopularData.size();i++){
            dataIdToIndex.put(experimentalPopularData.get(i).getId(),i);
        }
        //�ܻ���ռ�
        int sumAvailableSpace = experimentalEdgeServer.size()*maxStorageSpace;
        //����� ��ÿ�������Ч��
        HashMap<ServerDataPair,HashMap<Request,Double>> cacheRequestUtility = new HashMap<>();
        //����������������Ч��
        HashMap<ServerDataPair,Double> cacheSumUtility = new HashMap<>();
        //����Ե����ȶ���
        PriorityQueue<ServerDataPair> utilityPriorityQueue = new PriorityQueue<>();
        //��ǰ�洢����£����������Ч��
        HashMap<Request,Double> nowRequestUtility = new HashMap<>();
        HashMap<Request,Double> nextRequestUtility = new HashMap<>();
        for(Request r:predictiveRequest){
            nowRequestUtility.put(r,0.0);
            nextRequestUtility.put(r,0.0);
        }
        //���㣺��1��ÿ���洢�� �������Ч��  ��2��ÿ���洢�� �������������Ч��
        for (Map.Entry<ServerDataPair, ArrayList<Request>> entry : cacheToRequest.entrySet()) {
            ServerDataPair cache = entry.getKey();
            ArrayList<Request> requests = entry.getValue();
            HashMap<Request, Double> requestUtility = new HashMap<>();
            double sumUtility = 0.0;
            for (Request request : requests) {
                int nearestServerIndex = serverIdToIndex.get(userNearestServer.get(request.getUserId()));
                int cacheServerId = serverIdToIndex.get(cache.serverId);
                int latency = allDistance[nearestServerIndex][cacheServerId];
                double utility = calculateQoE(request.getPopularDataId(),cache.dataId,latency);
                requestUtility.put(request, utility);
                sumUtility += utility;
            }
            cacheRequestUtility.put(cache, requestUtility);
            cacheSumUtility.put(cache, sumUtility);
            utilityPriorityQueue.add(new ServerDataPair(cache.serverId,cache.dataId,sumUtility));
        }

        ArrayList<ServerDataPair> debugCacheList = new ArrayList<>();

        //�ҵ���Ч���������Ļ����
        while (!utilityPriorityQueue.isEmpty()&&sumAvailableSpace>0){
            ServerDataPair bestServerDataPair = utilityPriorityQueue.poll();
            // ���������δ��ʱ
            if(bestServerDataPair.sumDeltaUtility==cacheSumUtility.get(bestServerDataPair)){
                EdgeServer edgeServer = experimentalEdgeServer.get(serverIdToIndex.get(bestServerDataPair.serverId));
                PopularData popularData = experimentalPopularData.get(dataIdToIndex.get(bestServerDataPair.dataId));
                // �пռ���Դ洢
                if(edgeServer.getRemainingStorageSpace()>=popularData.getSize()){
                    //debug
                    debugCacheList.add(bestServerDataPair);
                    //�����������
                    edgeServer.cachePopularData(popularData);
                    sumAvailableSpace -= popularData.getSize();
                    //�ҵ��������ص�����
                    HashMap<Request,Double> relatedRequest = cacheRequestUtility.get(bestServerDataPair);
                    // Ч�ø��¹�������
                    ArrayList<Request> updatedRequest = new ArrayList<>();
                    for(Map.Entry<Request,Double> entry:relatedRequest.entrySet()){
                        if(entry.getValue()>nextRequestUtility.get(entry.getKey())){
                            updatedRequest.add(entry.getKey());
                            nextRequestUtility.put(entry.getKey(),entry.getValue());
                        }
                    }

                    HashSet<Integer> debugDataSet = new HashSet<>();
                    for(Request r:updatedRequest){
                        debugDataSet.add(r.getPopularDataId());
                    }


                    for(Request r:updatedRequest){
                        ArrayList<ServerDataPair> relatedCache = requestToCache.get(r);
                        double bestCacheRequestUtility = relatedRequest.get(r);
                        for(ServerDataPair serverDataPair:relatedCache){
                            double relatedCacheRequestUtility = cacheRequestUtility.get(serverDataPair).get(r);
//                            double deltaRequestUtility = nextRequestUtility.get(r)-nowRequestUtility.get(r);
//                            double promotionUtility = relatedCacheRequestUtility - nowRequestUtility.get(r);
                            double csu = cacheSumUtility.get(serverDataPair);
                            double nru = nextRequestUtility.get(r);
                            double nowRU = nowRequestUtility.get(r);
                            double newSumUtility = 0.0;
//                            if(nowRequestUtility.get(r)==null){
//                                System.out.println("bbb");
//                            }
                            if(relatedCacheRequestUtility<=nowRequestUtility.get(r)){
                                continue;
                            }
                            if(relatedCacheRequestUtility>nextRequestUtility.get(r)){
                                newSumUtility = cacheSumUtility.get(serverDataPair) - (relatedCacheRequestUtility-nextRequestUtility.get(r));
                            }else{
                                newSumUtility = cacheSumUtility.get(serverDataPair)-(relatedCacheRequestUtility-nowRequestUtility.get(r));
                            }
//                            double newSumUtility = cacheSumUtility.get(serverDataPair)-(Math.min(nextRequestUtility.get(r),relatedCacheRequestUtility)-nowRequestUtility.get(r));
//                            if(newSumUtility>cacheSumUtility.get(serverDataPair)){
//                                System.out.println("aaa");
//                            }
                            cacheSumUtility.put(serverDataPair,newSumUtility);
                            utilityPriorityQueue.add(new ServerDataPair(serverDataPair.serverId,serverDataPair.dataId,newSumUtility));
                        }
                    }
                    for(Map.Entry<Request,Double> entry:relatedRequest.entrySet()){
                        if(entry.getValue()>nowRequestUtility.get(entry.getKey())){
                            nowRequestUtility.put(entry.getKey(),entry.getValue());
                        }
                    }
                }
            }
        }

        double resultSumQoE = 0.0;
        for(ServerDataPair serverDataPair:debugCacheList){
            resultSumQoE+=serverDataPair.sumDeltaUtility;
        }
        System.out.println(resultSumQoE);

        return cachingDecision;

    }

    //��ʼ����ÿ�������Ӱ����ٸ�����ԣ��Լ�ÿ������Ի�Ӱ����ٸ�����
    public void initAlgorithmicTempData(int timestamp){
        ArrayList<Request> predictiveRequest = this.predictiveRequest.get(timestamp);
        HashMap<Integer, List<EdgeServer>> serverGraph = edgeServerGraph.getServerGraph();
        HashMap<Integer,HashMap<Integer,ArrayList<EdgeServer>>> distanceRank = edgeServerGraph.getDistanceRank();
        int[][] allDistance = edgeServerGraph.getEveryDistance();
        HashMap<Integer,Integer> serverIdToIndex = edgeServerGraph.getEdgeServerIdToIndex();
        int sumSpace = experimentalEdgeServer.size()*maxStorageSpace;
        //����ÿ��������Ա���Щ�����Ӱ��
        int debugCount = 0;
        HashSet<Integer> debugDataSet = new HashSet<>();
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

                    ServerDataPair s = new ServerDataPair(20112,30008);
                    if(serverDataPair.equals(s)){
                        debugCount++;
                        debugDataSet.add(r.getPopularDataId());
                    }


                }
            }
            requestToCache.put(r,relatedServerDataPair);
        }

        //����ÿ������Կ���Ӱ�쵽����������Щ
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


    
    //���������е�QoE �� sigmoid(H(x))=1/(1+e^-H(x))
    // H(x) = lw*Latency+s*(1-sim)+Z
    //�������ú�Ľ���� H(x) = -Latency[0-3] + (-6*(1-sim[0-1]))+3
    /*
        �����������ɣ�
        (1) Latency=0,sim=0ʱ��[�����ȡ�����] H(x)=0+-6+3=-3,QoE = 0.047
        (2) Latency=3,sim=0ʱ��[�ƶ˻�ȡ] H(x)=3+ -6 +3 = 0 QoE=0.5
        (3) Latency=0,sim=0.5ʱ��[�ͽ���ȡ��һ���] H(x)=0+-3 + 3 = 0 QoE=0.5
     */
    private double calculateQoE(int requestDataId, int cacheDataId, int latency) {
        double sim = 0;
        if(dataSimilarityMap.get(requestDataId).get(cacheDataId)!=null){
            sim = dataSimilarityMap.get(requestDataId).get(cacheDataId);
        }
        double weightValue = latencyWeight*(latency)+SimWeight*(1 - sim) + Z;
        return 1 / (1 + Math.exp(-weightValue));

    }


}

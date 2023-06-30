package baseLine;

import bean.*;
import util.AlgorithmUtils;
import util.DBUtils;

import java.io.IOException;
import java.util.*;

public class BaseLFU {
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    List<PopularData> experimentalPopularData;
    List<Request> Request;
    //HashMap<Integer, List<EdgeServer>>
    EdgeServerGraph edgeServerGraph = new EdgeServerGraph();
    Map<Integer, double[]> dataVectorMap;
    //用户最近的服务器
    Map<Integer, Integer> useredge;
    //维护一张map<timestamp,map<edgeserverId,map<popularDataId,count>>>
    //map<时间戳，map<边缘服务器id,该服务器接收的请求中各个数据的访问次数map<数据id，访问次数>>>
    Map<Integer,Map<Integer,Map<Integer,Integer>>> UCOTable;
    //将所有时间戳下的服务器的流行数据按照访问次数进行排序维护
    Map<Integer,Map<Integer,Map<Integer,Integer>>> SortedUCOTable;
    //对于每一个时间戳，都有一个服务器群的存储数据状态
    Map<Integer,List<EdgeServer>> edgeCondition;
    //进行实验返回的是<时间戳，<用户id，时延>>
    Map<Integer,Map<Integer,Integer>> userlatency;
    //缓存决策
    CachingDecision cachingDecision = new CachingDecision();
    AlgorithmUtils algorithmUtils;
    //  Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    public void initCachingDecision(int beginTimstamp,int endTimestamp){
        for(int i=beginTimstamp;i<=endTimestamp;i++){
            Map<EdgeServer, HashSet<PopularData>> cachingResult = new HashMap<>();
            List<Request> requests=DBUtils.getAllRequestByTime("request",i,i);
            //保存第一步的最优解
            List<EdgeServer> servers=this.edgeCondition.get(i);
            for(EdgeServer edgeServer:servers){
                ArrayList<PopularData> dataList =  edgeServer.getCachedDataList();
                if(cachingResult.get(edgeServer)==null){
                    cachingResult.put(edgeServer,new HashSet<>());
                }
                for(PopularData popularData:dataList){
                    cachingResult.get(edgeServer).add(popularData);
                }
            }
            cachingDecision.setCachingState(cachingResult);
            double maxSumQoE = algorithmUtils.cacheDecisionSumQoE(cachingDecision, (ArrayList<bean.Request>) requests);
            double finalSumQoE = algorithmUtils.cacheDecisionSumQoE(cachingDecision, (ArrayList<bean.Request>) requests);
            double finalFIndex = algorithmUtils.cacheDecisionFIndex(cachingDecision, (ArrayList<bean.Request>) requests);
            double result = algorithmUtils.cacheDecisionFinalValue(cachingDecision, (ArrayList<bean.Request>) requests);
            cachingDecision.setFIndexQoE(finalFIndex);
            cachingDecision.setOptimizationObjective(result);
            System.out.println("Timestamp"+i+" SumQoE: "+finalSumQoE + " FIndex: "+finalFIndex +"FinalValue: "+result);
        }
    }
    public void initializeData(ExperimentalSetup experimentalSetup) throws IOException {
        algorithmUtils = new AlgorithmUtils(experimentalSetup);
        int beginTimestamp = experimentalSetup.getBeginTimestamp();
        int endTimestamp = experimentalSetup.getEndTimestamp();
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
      //  this.Request = DBUtils.getAllRequestByTime("request",51,80);
        this.useredge = algorithmUtils.getUserNearestServer(experimentalUserList, experimentalEdgeServer);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        this.useredge=algorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        initUCOTable(beginTimestamp,endTimestamp);
        keepUCOTable(beginTimestamp,endTimestamp);
        generateEdgeCondition(beginTimestamp,endTimestamp);
        initCachingDecision(beginTimestamp,endTimestamp);
    }
    //对服务器访问表的初始化
    public void initUCOTable(int beginTimestamp,int endTimestamp){
        this.UCOTable=new HashMap<Integer, Map<Integer, Map<Integer, Integer>>>();
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            Map<Integer,Map<Integer,Integer>> edgeCondition=new HashMap<Integer,Map<Integer,Integer>>();
            for(EdgeServer es:this.experimentalEdgeServer){
                Map<Integer,Integer> pdcount=new HashMap<Integer, Integer>();
                for(PopularData pd:this.experimentalPopularData){
                    pdcount.put(pd.getId(),0);
                }
                edgeCondition.put(es.getId(),pdcount);
            }
            this.UCOTable.put(i,edgeCondition);
        }
    }
    public void keepUCOTable(int beginTimestamp,int endTimestamp){
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            List<Request> requests=new ArrayList<Request>();
            requests=DBUtils.getAllRequestByTime("request",1,i-1);
            for(Request request:requests){
                int uid=request.getUserId();
                int sid=useredge.get(uid);
                int pid=request.getPopularDataId();
                int count=this.UCOTable.get(i).get(sid).get(pid);
                count++;
                UCOTable.get(i).get(sid).put(pid,count);
            }
        }
        this.SortedUCOTable=sortUCOTable(UCOTable);
       // sortMapByCountDescending();
    }
    //对流行数据访问次数表进行排序
    public static Map<Integer, Map<Integer, Map<Integer, Integer>>> sortUCOTable(
            Map<Integer, Map<Integer, Map<Integer, Integer>>> UCOTable) {

        for (Map.Entry<Integer, Map<Integer, Map<Integer, Integer>>> timeEntry : UCOTable.entrySet()) {
            Map<Integer, Map<Integer, Integer>> sidMap = timeEntry.getValue();

            for (Map.Entry<Integer, Map<Integer, Integer>> sidEntry : sidMap.entrySet()) {
                Map<Integer, Integer> pidCountMap = sidEntry.getValue();

                // 创建一个 List 来存储排序后的 pidCount 条目
                List<Map.Entry<Integer, Integer>> pidCountList = new ArrayList<>(pidCountMap.entrySet());

                // 对 pidCountList 进行排序，按照 count 从大到小排序
                pidCountList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

                // 创建一个新的有序 Map 来存储排序后的 pidCount 条目
                LinkedHashMap<Integer, Integer> sortedPidCountMap = new LinkedHashMap<>();

                // 将排序后的 pidCount 条目按照顺序放入 sortedPidCountMap 中
                for (Map.Entry<Integer, Integer> pidCountEntry : pidCountList) {
                    sortedPidCountMap.put(pidCountEntry.getKey(), pidCountEntry.getValue());
                }

                // 将排序后的 pidCountMap 替换为 sortedPidCountMap
                sidEntry.setValue(sortedPidCountMap);
            }
        }

        return UCOTable;
    }
    //对流行数据访问次数表进行排序
    public void sortMapByCountDescending() {
        for (Map<Integer, Map<Integer, Integer>> innerMap : this.UCOTable.values()) {
            // 使用Stream排序
            innerMap.forEach((sid, pidCountMap) -> {
                Map<Integer, Integer> sortedMap = new LinkedHashMap<>();
                pidCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
                pidCountMap.clear();
                pidCountMap.putAll(sortedMap);
            });
        }
    }
    //生成每个时间戳下所有服务器的存储数据的情况
    //    Map<Integer,List<EdgeServer>> edgeCondition;
    public void generateEdgeCondition(int beginTimestamp,int endTimestamp){
        this.edgeCondition=new HashMap<Integer, List<EdgeServer>>();
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            List<EdgeServer> edgeServers=DBUtils.getAllEdgeServer();
            //设置一个流行数据表，标记是否被某个边缘服务器放入，不能多次放入
            Map<Integer, Boolean> populardataCondition=new HashMap<Integer, Boolean>();
            for(PopularData popularData:this.experimentalPopularData){
                populardataCondition.put(popularData.getId(),false);
            }
            Map<Integer, Map<Integer, Integer>> timeMap = this.SortedUCOTable.get(i);
            for(EdgeServer edgeServer:edgeServers){
                int sid=edgeServer.getId();
                Map<Integer, Integer> sidMap = timeMap.get(sid);
                List<PopularData> cachingList=new ArrayList<PopularData>();
                for (Map.Entry<Integer, Integer> entry : sidMap.entrySet()){
                    int pid = entry.getKey();
                    int size=findSizeById(pid);
                    if(populardataCondition.get(pid))
                        continue;
                    else{
                        if(size!=0){
                            int remainsize=edgeServer.getRemainingStorageSpace();
                            if(remainsize>=size){
                                cachingList.add(findById(pid));
                                edgeServer.setRemainingStorageSpace(remainsize-size);
                                populardataCondition.put(pid,true);
                            }
                            else
                                break;
                        }
                    }
                }
                edgeServer.setCachedDataList((ArrayList<PopularData>) cachingList);
            }
            this.edgeCondition.put(i,edgeServers);
        }
    }
    public PopularData findById(int pid){
        PopularData pd=new PopularData();
        for(PopularData popularData:this.experimentalPopularData){
            if(popularData.getId()==pid){
                pd=popularData;
                break;
            }
        }
        return pd;
    }
    public EdgeServer findServerById(int sid,List<EdgeServer> edgeServers){
        EdgeServer es=new EdgeServer();
        for(EdgeServer edgeServer:edgeServers){
            if(sid==edgeServer.getId()){
                es=edgeServer;
            }
        }
        return  es;
    }
    private int findSizeById(int pid) {
        int size=0;
        for(PopularData pd:this.experimentalPopularData){
            if(pd.getId()==pid){
                size=pd.getSize();
                break;
            }
        }
        return size;
    }
    //进行实验返回的是<时间戳，<用户id，时延>>
    public void experiment(int beginTimestamp,int endTimestamp){
        generateEdgeCondition(beginTimestamp,endTimestamp);
        this.userlatency=new HashMap<Integer, Map<Integer, Integer>>();
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            this.userlatency.put(i,new HashMap<Integer, Integer>());
            List<EdgeServer> edgeServerList=this.edgeCondition.get(i);
            List<Request> requests=DBUtils.getAllRequestByTime("request",i,i);
            for(Request request:requests){
                int serverId=useredge.get(request.getUserId());
                int distance=0;
                int findsid=0;
                //edgeServerDistance
                //返回为<边缘服务器id，<距离，距这个边缘服务器距离的服务器id列表>>
                //Map<Integer,Map<Integer,List<Integer>>>
                for(Map.Entry<Integer, List<Integer>> distanceList:edgeServerGraph.getEdgeServerDistance().get(serverId).entrySet()){
                    distance=distanceList.getKey();
                    List<Integer> serverlist=distanceList.getValue();
                    for(Integer sid:serverlist){
                        //ArrayList<PopularData> cachedDataList
                        EdgeServer s=findServerById(sid,edgeServerList);
                        for(PopularData pd:s.getCachedDataList()){
                            if(pd.getId()==request.getPopularDataId())
                                findsid=sid;
                            break;
                        }
                        if(findsid!=0)
                            break;
                    }
                    if(findsid!=0)
                        break;
                }
                if(distance>=edgeServerGraph.getEdgeServerDistance().get(serverId).size()&&findsid==0)
                    distance=100;
                this.userlatency.get(i).put(request.getUserId(),distance);
            }
        }
        initCachingDecision(beginTimestamp,endTimestamp);
    }
    //UCO UCO方法总是选择覆盖请求数据的移动用户最多的边缘服务器来缓存数据，直到所有流行的数据都缓存完毕。
    // CUO方法总是选择覆盖最大用户检索数量的边缘服务器，以便在成本达到预算之前优先缓存数据。
    //初始化edgeCover
    ////服务器覆盖的用户检索数量 存储形式为：<时间戳,<边缘服务器id，该服务器的在这段时间内所覆盖用户的检索数量>>
//    public void initUCO(int beginTimestamp,int endTimestamp) {
//        this.edgeCover=new HashMap<Integer, Map<Integer, Integer>>();
//        for (int i = beginTimestamp; i <=endTimestamp; i++) {
//            //userLatency.put(i,new HashMap<Integer, Integer>());
//            Map<Integer, Integer> searchNum = new HashMap<Integer, Integer>();
//            for (EdgeServer server : this.experimentalEdgeServer) {
//                searchNum.put(server.getId(), 0);
//            }
//            this.edgeCover.put(i,searchNum);
//        }
//        List<Request> request=DBUtils.getAllRequestByTime("request", 1, endTimestamp);
//        for(int i=beginTimestamp;i<=endTimestamp;i++)
//        {
//            for(Request rq:request){
//                if(rq.getTimestamp()<i){
//                    int serverid=useredge.get(rq.getUserId());
//                    int num=edgeCover.get(i).get(serverid);
//                    edgeCover.get(i).put(serverid,++num);
//                }
//            }
//        }
//    }
//对edgeCover<Serverid,RequestNum>进行排序，获得一个倒序的存放ServerId的List

//    public Map<Integer,List<Integer>> sort(Map<Integer, Map<Integer, Integer>> ec) {
//        Map<Integer, List<Integer>> mergedMap = new HashMap<>();
//        // 遍历 ec 的键值对，对于每个 sid，根据 num 的降序排列创建一个新的 List<Integer> 对象
//        for (Map.Entry<Integer, Map<Integer, Integer>> entry : ec.entrySet()) {
//            int time = entry.getKey();
//            Map<Integer, Integer> innerMap = entry.getValue();
//
//            List<Integer> sortedSidList = innerMap.entrySet()
//                    .stream()
//                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
//                    .map(Map.Entry::getKey)
//                    .collect(Collectors.toList());
//
//            mergedMap.put(time, sortedSidList);
//        }
//
//        return mergedMap;
//    }
//根据前beginTimestamp的历史进行UCO策略缓存
//Map<Integer,List<EdgeServer>> edgeCondition;
//服务器覆盖的用户检索数量 存储形式为：<时间戳,<边缘服务器id，该服务器的在这段时间内所覆盖用户的检索数量>>
//即从该时间戳开始前所有的请求都被检索
//Map<Integer, Map<Integer, Integer>> edgeCover;
//    public void UCOCaching(int beginTimestamp,int endTimestamp) {
//        initUCO(beginTimestamp,endTimestamp);
//        Map<Integer,List<Integer>> sortedList = sort(this.edgeCover);
//        this.edgeCondition=new HashMap<Integer,List<EdgeServer>>();
////        for(int i=beginTimestamp;i<=endTimestamp;i++){
////            this.edgeCondition.put(i,new ArrayList<EdgeServer>());
////        }
//        List<PopularData> pds = new ArrayList<PopularData>();
//        for(int i=beginTimestamp;i<=endTimestamp;i++){
//            int count = 0;
//            List<EdgeServer> inputes=new ArrayList<EdgeServer>();
//            List<Integer> sorted=sortedList.get(i);
//            for (Integer server : sorted) {
//                if (count < this.experimentalPopularData.size()) {
//                    EdgeServer es = AlgorithmUtils.findEdgeServerById(this.experimentalEdgeServer, server);
//                    PopularData pd = this.experimentalPopularData.get(count++);
//                    while (es.getRemainingStorageSpace() >= pd.getSize()) {
//                        pds.add(pd);
//                        es.setRemainingStorageSpace(es.getRemainingStorageSpace() - pd.getSize());
//                        if (count == this.experimentalPopularData.size())
//                            break;
//                        else pd = this.experimentalPopularData.get(count++);
//                    }
//                    es.setCachedDataList((ArrayList<PopularData>) pds);
//                    pds = new ArrayList<PopularData>();
//                    inputes.add(es);
//                } else break;
//            }
//            this.edgeCondition.put(i,inputes);
//        }
//        System.out.println("aaaaa");
//    }

    //进行实验返回的是<时间戳，<用户id，时延>>
//    public Map<Integer, Map<Integer, Integer>> experiment(int beginTimestamp, int endTimestamp) throws IOException {
//        this.Request=DBUtils.getAllRequestByTime("request", beginTimestamp, endTimestamp);
//        List<Integer> timePeriod = new ArrayList<Integer>();
//        Map<Integer, Map<Integer, Integer>> userLatency = new HashMap<Integer, Map<Integer, Integer>>();
//        initUCO(beginTimestamp,endTimestamp);
//        UCOCaching(beginTimestamp,endTimestamp);
//        for (int i = beginTimestamp; i <= endTimestamp; i++) {
//            timePeriod.add(i);
//            // edgeServerIdToIndex.put(allEdgeServer.get(i).getId(),i);
//            userLatency.put(i, new HashMap<Integer, Integer>());
//            Map<Integer,Integer> ul=calUserLatency(this.Request,i);
//            userLatency.put(i,ul);
//        }
//        for (Map.Entry<Integer, Map<Integer, Integer>> entry : userLatency.entrySet()) {
//            int userId = entry.getKey();
//            Map<Integer, Integer> latencyMap = entry.getValue();
//
//            for (Map.Entry<Integer, Integer> latencyEntry : latencyMap.entrySet()) {
//                int requestId = latencyEntry.getKey();
//                int latency = latencyEntry.getValue();
//
//                System.out.println(": " + userId + ", Request ID: " + requestId + ", Latency: " + latency);
//            }
//        }
//        return userLatency;
//    }
    //给定所有服务器的存储数据情况和所有的请求情况，返回某一时间戳下的所有<用户id,时延>结果
//    public Map<Integer,Integer> calUserLatency(List<Request> request,int timestamp){
//        Map<Integer, Integer> userLatency = new HashMap<Integer, Integer>();
//        for(Request rq:request){
//            if(rq.getTimestamp()==timestamp){
//                int serverId=useredge.get(rq.getUserId());
//                int distance=0;
//                int findsid=0;
//                //edgeServerDistance
//                //返回为<边缘服务器id，<距离，距这个边缘服务器距离的服务器id列表>>
//                //Map<Integer,Map<Integer,List<Integer>>>
//                for(Map.Entry<Integer, List<Integer>> distanceList:edgeServerGraph.getEdgeServerDistance().get(serverId).entrySet()){
//                    distance=distanceList.getKey();
//                    List<Integer> serverlist=distanceList.getValue();
//                    for(Integer sid:serverlist){
//                        //ArrayList<PopularData> cachedDataList
//                        EdgeServer s=edgeServerGraph.idFindServer(sid);
//                        for(PopularData pd:s.getCachedDataList()){
//                            if(pd.getId()==rq.getPopularDataId())
//                                findsid=sid;
//                            break;
//                        }
//                        if(findsid!=0)
//                            break;
//                    }
//                    if(findsid!=0)
//                        break;
//                }
//                if(distance>=edgeServerGraph.getEdgeServerDistance().get(serverId).size()&&findsid==0)
//                    distance=100;
//                userLatency.put(rq.getUserId(),distance);
//            }
//
//        }
//        // System.out.println("aaaaaa");
//        return userLatency;
//    }
}

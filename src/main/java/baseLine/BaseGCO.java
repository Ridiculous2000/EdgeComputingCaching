package baseLine;

import bean.*;
import util.AlgorithmUtils;
import util.DBUtils;

import java.io.IOException;
import java.util.*;
//GCO方法实现
public class BaseGCO {
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
            List<EdgeServer> servers=this.edgeCondition.get(beginTimstamp);
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
        this.algorithmUtils = new AlgorithmUtils(experimentalSetup);
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        //  this.Request = DBUtils.getAllRequestByTime("request",51,80);
        this.useredge = algorithmUtils.getUserNearestServer(experimentalUserList, experimentalEdgeServer);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        this.useredge=algorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        int beginTimestamp = experimentalSetup.getBeginTimestamp();
        int endTimestamp = experimentalSetup.getEndTimestamp();
        initUCOTable(beginTimestamp,endTimestamp);
        keepUCOTable(beginTimestamp,endTimestamp);
        generateEdgeCondition(beginTimestamp,endTimestamp);
        initCachingDecision(beginTimestamp,endTimestamp);
    }
    //对服务器访问表的初始化
    public void initUCOTable(int beginTimestamp,int endTimestamp){
        UCOTable=new HashMap<Integer, Map<Integer, Map<Integer, Integer>>>();
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            Map<Integer,Map<Integer,Integer>> edgeCondition=new HashMap<Integer,Map<Integer,Integer>>();
            for(EdgeServer es:this.experimentalEdgeServer){
                Map<Integer,Integer> pdcount=new HashMap<Integer, Integer>();
                for(PopularData pd:this.experimentalPopularData){
                    pdcount.put(pd.getId(),0);
                }
                edgeCondition.put(es.getId(),pdcount);
            }
            UCOTable.put(i,edgeCondition);
        }
    }
    public void keepUCOTable(int beginTimestamp,int endTimestamp){
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            //存储在i时间戳前该数据被多少个用户访问过
            //Map<sid,Map<pid,HashSet<uid>>>
            Map<Integer,Map<Integer,HashSet<Integer>>> reqdata=new HashMap<Integer, Map<Integer,HashSet<Integer>>>();
            for(EdgeServer es:this.experimentalEdgeServer){
                Map<Integer,HashSet<Integer>> list=new HashMap<Integer, HashSet<Integer>>();
                for(PopularData pd:this.experimentalPopularData){
                    HashSet<Integer> hslist=new HashSet<Integer>();
                    list.put(pd.getId(),hslist);
                }
                reqdata.put(es.getId(),list);
            }
            List<Request> requests=new ArrayList<Request>();
            requests=DBUtils.getAllRequestByTime("request",1,i-1);
            for(Request request:requests){
                int uid=request.getUserId();
                int sid=useredge.get(uid);
                int pid=request.getPopularDataId();
                reqdata.get(sid).get(pid).add(uid);
//                int count=this.UCOTable.get(i).get(sid).get(pid);
//                count++;
//                UCOTable.get(i).get(sid).put(pid,count);
            }
            //map<时间戳，map<边缘服务器id,该服务器接收的请求中各个数据的访问次数map<数据id，访问次数>>>
            for (Map.Entry<Integer, Map<Integer, HashSet<Integer>>> entry1 : reqdata.entrySet()) {
                int sid1 = entry1.getKey();
                Map<Integer, HashSet<Integer>> innerMap1 = entry1.getValue();

                for (Map.Entry<Integer, HashSet<Integer>> entry2 : innerMap1.entrySet()) {
                    int pid1 = entry2.getKey();
                    HashSet<Integer> set = entry2.getValue();
                    int size = set.size();

                    // 修改data中对应的count值
                    if (this.UCOTable.containsKey(i)) {
                        Map<Integer, Map<Integer, Integer>> innerMap2 = this.UCOTable.get(i);
                        if (innerMap2.containsKey(sid1)) {
                            Map<Integer, Integer> innerMap3 = innerMap2.get(sid1);
                            innerMap3.put(pid1, size);
                        } else {
                            Map<Integer, Integer> innerMap3 = new HashMap<>();
                            innerMap3.put(pid1, size);
                            innerMap2.put(sid1, innerMap3);
                        }
                    } else {
                        Map<Integer, Map<Integer, Integer>> innerMap2 = new HashMap<>();
                        Map<Integer, Integer> innerMap3 = new HashMap<>();
                        innerMap3.put(pid1, size);
                        innerMap2.put(sid1, innerMap3);
                        this.UCOTable.put(i, innerMap2);
                    }
                }
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
}

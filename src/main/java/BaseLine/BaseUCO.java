package BaseLine;

import bean.*;
import util.AlgorithmUtils;
import util.DBUtils;

import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class BaseUCO {
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    List<PopularData> experimentalPopularData;
    List<Request> Request;
    //HashMap<Integer, List<EdgeServer>>
    EdgeServerGraph edgeServerGraph = new EdgeServerGraph();
    Map<Integer, double[]> dataVectorMap;
    //用户最近的服务器
    Map<Integer, Integer> useredge;
    //服务器覆盖的用户检索数量 存储形式为：<时间戳,<边缘服务器id，该服务器的在这段时间内所覆盖用户的检索数量>>
    //即从该时间戳开始前所有的请求都被检索
    Map<Integer, Map<Integer, Integer>> edgeCover;
    //对于每一个时间戳，都有一个服务器群的存储数据状态
    Map<Integer,List<EdgeServer>> edgeCondition;

    //  Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    public void initializeData(int beginTimestamp, int endTimestamp) throws IOException {
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
      //  this.Request = DBUtils.getAllRequestByTime("request",51,80);
        this.useredge = AlgorithmUtils.getUserNearestServer(experimentalUserList, experimentalEdgeServer);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        this.edgeCover=new HashMap<Integer, Map<Integer, Integer>>();
        for(EdgeServer edgeserver:this.experimentalEdgeServer){
            edgeserver.setCachedDataList(new ArrayList<PopularData>());
        }
        //计算各个服务器在某一个timestamp的用户检索数量
        //存储形式为：<时间戳,<边缘服务器id，该服务器的在这段时间内所覆盖用户的检索数量>>
        initUCO(beginTimestamp,endTimestamp);
        //使用UCO决策进行数据缓存
        //  UCOCaching();

    }

    //UCO UCO方法总是选择覆盖请求数据的移动用户最多的边缘服务器来缓存数据，直到所有流行的数据都缓存完毕。
    // CUO方法总是选择覆盖最大用户检索数量的边缘服务器，以便在成本达到预算之前优先缓存数据。
    //初始化edgeCover
    ////服务器覆盖的用户检索数量 存储形式为：<时间戳,<边缘服务器id，该服务器的在这段时间内所覆盖用户的检索数量>>
    public void initUCO(int beginTimestamp,int endTimestamp) {
        this.edgeCover=new HashMap<Integer, Map<Integer, Integer>>();
        for (int i = beginTimestamp; i <=endTimestamp; i++) {
            //userLatency.put(i,new HashMap<Integer, Integer>());
            Map<Integer, Integer> searchNum = new HashMap<Integer, Integer>();
            for (EdgeServer server : this.experimentalEdgeServer) {
                searchNum.put(server.getId(), 0);
            }
            this.edgeCover.put(i,searchNum);
        }
        List<Request> request=DBUtils.getAllRequestByTime("request", 1, endTimestamp);
        for(int i=beginTimestamp;i<=endTimestamp;i++)
        {
            for(Request rq:request){
                if(rq.getTimestamp()<i){
                    int serverid=useredge.get(rq.getUserId());
                    int num=edgeCover.get(i).get(serverid);
                    edgeCover.get(i).put(serverid,++num);
                }
            }
        }
    }
//对edgeCover<Serverid,RequestNum>进行排序，获得一个倒序的存放ServerId的List

    public Map<Integer,List<Integer>> sort(Map<Integer, Map<Integer, Integer>> ec) {
        Map<Integer, List<Integer>> mergedMap = new HashMap<>();
        // 遍历 ec 的键值对，对于每个 sid，根据 num 的降序排列创建一个新的 List<Integer> 对象
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : ec.entrySet()) {
            int time = entry.getKey();
            Map<Integer, Integer> innerMap = entry.getValue();

            List<Integer> sortedSidList = innerMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            mergedMap.put(time, sortedSidList);
        }

        return mergedMap;
    }
//根据前beginTimestamp的历史进行UCO策略缓存
//Map<Integer,List<EdgeServer>> edgeCondition;
//服务器覆盖的用户检索数量 存储形式为：<时间戳,<边缘服务器id，该服务器的在这段时间内所覆盖用户的检索数量>>
//即从该时间戳开始前所有的请求都被检索
//Map<Integer, Map<Integer, Integer>> edgeCover;
    public void UCOCaching(int beginTimestamp,int endTimestamp) {
        initUCO(beginTimestamp,endTimestamp);
        Map<Integer,List<Integer>> sortedList = sort(this.edgeCover);
        this.edgeCondition=new HashMap<Integer,List<EdgeServer>>();
//        for(int i=beginTimestamp;i<=endTimestamp;i++){
//            this.edgeCondition.put(i,new ArrayList<EdgeServer>());
//        }
        int count = 0;
        List<PopularData> pds = new ArrayList<PopularData>();
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            List<EdgeServer> inputes=new ArrayList<EdgeServer>();
            List<Integer> sorted=sortedList.get(i);
            for (Integer server : sorted) {
                if (count < this.experimentalPopularData.size()) {
                    EdgeServer es = AlgorithmUtils.findEdgeServerById(this.experimentalEdgeServer, server);
                    PopularData pd = this.experimentalPopularData.get(count++);
                    while (es.getRemainingStorageSpace() >= pd.getSize()) {
                        pds.add(pd);
                        es.setRemainingStorageSpace(es.getRemainingStorageSpace() - pd.getSize());
                        if (count == this.experimentalPopularData.size())
                            break;
                        else pd = this.experimentalPopularData.get(count++);
                    }
                    es.setCachedDataList((ArrayList<PopularData>) pds);
                    pds = new ArrayList<PopularData>();
                    inputes.add(es);
                } else break;
            }
            edgeCondition.put(i,inputes);
        }
        System.out.println("aaaaa");
    }

    //进行实验返回的是<时间戳，<用户id，时延>>
    public Map<Integer, Map<Integer, Integer>> experiment(int beginTimestamp, int endTimestamp) throws IOException {
        this.Request=DBUtils.getAllRequestByTime("request", beginTimestamp, endTimestamp);
        List<Integer> timePeriod = new ArrayList<Integer>();
        Map<Integer, Map<Integer, Integer>> userLatency = new HashMap<Integer, Map<Integer, Integer>>();
        initUCO(beginTimestamp,endTimestamp);
        UCOCaching(beginTimestamp,endTimestamp);
        for (int i = beginTimestamp; i <= endTimestamp; i++) {
            timePeriod.add(i);
            // edgeServerIdToIndex.put(allEdgeServer.get(i).getId(),i);
            userLatency.put(i, new HashMap<Integer, Integer>());
            Map<Integer,Integer> ul=calUserLatency(this.Request,i);
            userLatency.put(i,ul);
        }
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : userLatency.entrySet()) {
            int userId = entry.getKey();
            Map<Integer, Integer> latencyMap = entry.getValue();

            for (Map.Entry<Integer, Integer> latencyEntry : latencyMap.entrySet()) {
                int requestId = latencyEntry.getKey();
                int latency = latencyEntry.getValue();

                System.out.println(": " + userId + ", Request ID: " + requestId + ", Latency: " + latency);
            }
        }
        return userLatency;
    }
    //给定所有服务器的存储数据情况和所有的请求情况，返回某一时间戳下的所有<用户id,时延>结果
    public Map<Integer,Integer> calUserLatency(List<Request> request,int timestamp){
        Map<Integer, Integer> userLatency = new HashMap<Integer, Integer>();
        for(Request rq:request){
            if(rq.getTimestamp()==timestamp){
                int serverId=useredge.get(rq.getUserId());
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
                        EdgeServer s=edgeServerGraph.idFindServer(sid);
                        for(PopularData pd:s.getCachedDataList()){
                            if(pd.getId()==rq.getPopularDataId())
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
                userLatency.put(rq.getUserId(),distance);
            }

        }
        // System.out.println("aaaaaa");
        return userLatency;
    }
}

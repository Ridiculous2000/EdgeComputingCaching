package BaseLine;

import bean.*;
import util.AlgorithmUtils;
import util.DBUtils;

import java.io.IOException;
import java.util.*;

public class BaseLRU {
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    List<PopularData> experimentalPopularData;
    List<bean.Request> Request;
    //HashMap<Integer, List<EdgeServer>>
    EdgeServerGraph edgeServerGraph=new EdgeServerGraph();
    Map<Integer,double[]> dataVectorMap;
    //用户最近的服务器
    Map<Integer,Integer> useredge;
    //服务器覆盖的用户检索数量 存储形式为：<时间戳,<边缘服务器id，该服务器的在这段时间内所覆盖用户的检索数量>>
    //丢弃最少使用的内容并缓存最近请求的内容
    Map<Integer,Map<Integer,Integer>> edgeCover;
    //  Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    //总是在缓存满时用新请求的视频替换最近请求最少的视频
    public void initializeData(int beginTimestamp,int endTimestamp) throws IOException {
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        //this.Request = DBUtils.getAllRequestByTime("predictive_request",51,80);
        this.useredge= AlgorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        //计算各个服务器在某一个timestamp的用户检索数量
        //存储形式为：<时间戳,<边缘服务器id，该服务器的在这段时间内所覆盖用户的检索数量>>
        initLRU(beginTimestamp,endTimestamp);
        //  UCOCaching();

    }

    public void initLRU(int beginTimestamp,int endTimestamp){
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            //userLatency.put(i,new HashMap<Integer, Integer>());
            Map<Integer,Integer> searchNum=new HashMap<Integer, Integer>();
            for(EdgeServer server:this.experimentalEdgeServer){
                searchNum.put(server.getId(),0);
            }
            edgeCover.put(i,searchNum);

        }
        this.Request=DBUtils.getAllRequestByTime("request",beginTimestamp,endTimestamp);
        for(Request rq:Request){
            //id,uid,pid,timestamp
            int es=useredge.get(rq.getUserId());
            // userLatency.get(timeStamp).put(rq.getUserId(),distance);
            int num=edgeCover.get(rq.getTimestamp()).get(useredge.get(rq.getUserId()));
            edgeCover.get(rq.getTimestamp()).put(useredge.get(rq.getUserId()),num++);
        }
    }
    public List<Integer> sort(Map<Integer,Map<Integer,Integer>> ec){
        // 创建一个 Comparator 对象来对 Num 进行排序
        Comparator<Map.Entry<Integer, Integer>> comparator = (entry1, entry2) ->
                entry2.getValue().compareTo(entry1.getValue());
        //获取原始 Map 中的所有条目，并将其存储到一个 List 中
        List<Map.Entry<Integer, Map<Integer, Integer>>> entries = new ArrayList<>(ec.entrySet());
        Collections.sort(entries, (entry1, entry2) ->
                comparator.compare(entry1.getValue().entrySet().iterator().next(),
                        entry2.getValue().entrySet().iterator().next()));
        List<Integer> sortedSidList = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : entries) {
            sortedSidList.add(entry.getValue().entrySet().iterator().next().getKey());
        }
        return sortedSidList;
    }
    public void LRUCaching(int beginTimestamp,int endTimestamp){
        initLRU(beginTimestamp,endTimestamp);
        List<Integer> sorted=sort(this.edgeCover);
        for(Integer i:sorted){
            List<PopularData> pds=new ArrayList<PopularData>();
            for(PopularData pd:this.experimentalPopularData){
                EdgeServer es=AlgorithmUtils.findEdgeServerById(this.experimentalEdgeServer,i);
                if(es.getRemainingStorageSpace()>=pd.getSize()){

                }
            }
        }
    }
    //进行实验返回的是<时间戳，<用户id，时延>>
    public Map<Integer,Map<Integer,Integer>> experiment(int beginTimestamp,int endTimestamp)throws IOException{
        List<Integer> timePeriod=new ArrayList<Integer>();
        Map<Integer,Map<Integer,Integer>> userLatency=new HashMap<Integer,Map<Integer,Integer>>();
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            timePeriod.add(i);
            // edgeServerIdToIndex.put(allEdgeServer.get(i).getId(),i);
            userLatency.put(i,new HashMap<Integer, Integer>());
        }
        for(Request rq:Request){
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
            int timeStamp=rq.getTimestamp();
            userLatency.get(timeStamp).put(rq.getUserId(),distance);
        }
        System.out.println("aaaaaa");
        return userLatency;
    }
}

package BaseLine;

import bean.*;
import sun.awt.geom.AreaOp;
import util.AlgorithmUtils;
import util.DBUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class BaseRandom {
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    List<PopularData> experimentalPopularData;
    List<Request> Request;
    //HashMap<Integer, List<EdgeServer>>
    EdgeServerGraph edgeServerGraph=new EdgeServerGraph();
    Map<Integer,double[]> dataVectorMap;
    //用户最近的服务器
    Map<Integer,Integer> useredge;
    //  Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    //缓存决策
    CachingDecision cachingDecision = new CachingDecision();
    //对于每一个时间戳，都有一个服务器群的存储数据状态
    Map<Integer,List<EdgeServer>> edgeCondition;
    public void initializeData(int beginTimestamp,int endTimestamp) throws IOException {
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        this.Request = DBUtils.getAllRequestByTime("request",beginTimestamp,endTimestamp);
        this.useredge= AlgorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        generateEdgeCondition(beginTimestamp,endTimestamp);
        initCachingDecision(beginTimestamp,endTimestamp);
    }
    public void generateEdgeCondition(int beginTimestamp,int endTimestamp){
        this.edgeCondition=new HashMap<Integer, List<EdgeServer>>();
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            List<EdgeServer> edgeServers=DBUtils.getAllEdgeServer();
            List<EdgeServer> list=randomCaching(edgeServers);
            this.edgeCondition.put(i,list);
        }
    }
    //缓存数据随机放置
    public List<EdgeServer> randomCaching(List<EdgeServer> edgeServers){
        for(EdgeServer server:edgeServers){
            Random random = new Random();
            List<PopularData> pds=new ArrayList<PopularData>();
            //设置一个流行数据表，标记是否被某个边缘服务器放入，不能多次放入
            Map<Integer, Boolean> populardataCondition=new HashMap<Integer, Boolean>();
            for(PopularData popularData:this.experimentalPopularData){
                populardataCondition.put(popularData.getId(),false);
            }
            //每次随机找10次数据填入边缘服务器，若无法填满则停止添加
            int count=0;
            while(server.getRemainingStorageSpace()>=1&&count<10&&experimentalPopularData.size()>0){

                int randomIndex =random.nextInt(experimentalPopularData.size());
                PopularData pd=experimentalPopularData.get(randomIndex);
                count++;
                if(pd.getSize()>server.getRemainingStorageSpace()||populardataCondition.get(pd.getId())){
                    continue;
                }else{
                    pds.add(findById(pd.getId()));
                    server.setRemainingStorageSpace(server.getRemainingStorageSpace()-pd.getSize());
                    populardataCondition.put(pd.getId(),true);
                }
            }
            server.setCachedDataList((ArrayList<PopularData>) pds);
        }
        return edgeServers;
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
    public void initCachingDecision(int beginTimstamp,int endTimestamp){
        Map<EdgeServer, HashSet<PopularData>> cachingResult = new HashMap<>();

        //保存第一步的最优解
        for(int i=beginTimstamp;i<=endTimestamp;i++){
            List<Request> requests=DBUtils.getAllRequestByTime("request",i,i);
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
            double maxSumQoE = AlgorithmUtils.cacheDecisionSumQoE(cachingDecision, (ArrayList<bean.Request>) requests);
            double finalSumQoE = AlgorithmUtils.cacheDecisionSumQoE(cachingDecision, (ArrayList<bean.Request>) requests);
            double finalFIndex = AlgorithmUtils.cacheDecisionFIndex(cachingDecision, (ArrayList<bean.Request>) requests);
            double result = AlgorithmUtils.cacheDecisionFinalValue(cachingDecision, (ArrayList<bean.Request>) requests,400);
            cachingDecision.setFIndexQoE(finalFIndex);
            cachingDecision.setOptimizationObjective(result);
            System.out.println("Timestamp"+i+" SumQoE: "+finalSumQoE + " FIndex: "+finalFIndex +"FinalValue: "+result);

        }
         }
    //进行实验返回的是<时间戳，<用户id，时延>>
    public Map<Integer,Map<Integer,Integer>> experiment(int beginTimestamp,int endTimestamp)throws IOException{
        List<Integer> timePeriod=new ArrayList<Integer>();
        Map<Integer,Map<Integer,Integer>> userLatency=new HashMap<Integer,Map<Integer,Integer>>();
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            timePeriod.add(i);
            List<EdgeServer> edgeServerList=this.edgeCondition.get(i);
            // edgeServerIdToIndex.put(allEdgeServer.get(i).getId(),i);
            userLatency.put(i,new HashMap<Integer, Integer>());
            for(Request rq:this.Request){
                if(rq.getTimestamp()==i){
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
                            EdgeServer s=findServerById(sid,edgeServerList);
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

            }
        }
        return userLatency;
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
//    public int bfs(int startEdge){
//        boolean[] visited = new boolean[experimentalEdgeServer.size()]; // 记录顶点是否被访问
//        Queue<Integer> queue = new LinkedList<>();
//        visited[startEdge] = true;
//        queue.offer(startEdge);
//        while (!queue.isEmpty()) {
//            int currentVertex = queue.poll();
//            System.out.print(currentVertex + " ");
//            List<Integer> neighbors =edgeServerGraph.get(currentVertex);
//                for (int neighbor : neighbors) {
//                if (!visited[neighbor]) {
//                    visited[neighbor] = true;
//                    queue.offer(neighbor);
//                }
//            }
//        }
//    }
}


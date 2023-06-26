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
    public void initializeData(int beginTimestamp,int endTimestamp) throws IOException {
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        this.Request = DBUtils.getAllRequestByTime("request",beginTimestamp,endTimestamp);
        this.useredge= AlgorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
       // randomCaching();
    }
    //缓存数据随机放置
    public void randomCaching(){
        for(EdgeServer server:experimentalEdgeServer){
            Random random = new Random();
            List<PopularData> pds=new ArrayList<PopularData>();
            //每次随机找10次数据填入边缘服务器，若无法填满则停止添加
            int count=0;
            System.out.println(experimentalPopularData.size());
            while(server.getRemainingStorageSpace()>=1&&count<10&&experimentalPopularData.size()>0){

                int randomIndex =random.nextInt(experimentalPopularData.size());
                PopularData pd=experimentalPopularData.get(randomIndex);
                count++;
                if(pd.getSize()>server.getRemainingStorageSpace()){
                    continue;
                }else{
                    experimentalPopularData.remove(pd);
                    server.setRemainingStorageSpace(server.getRemainingStorageSpace()-pd.getSize());
                    pds.add(pd);
                }
            }
            server.setCachedDataList((ArrayList<PopularData>) pds);
        }
    }
    //进行实验返回的是<时间戳，<用户id，时延>>
    public Map<Integer,Map<Integer,Integer>> experiment(int beginTimestamp,int endTimestamp)throws IOException{
        List<Integer> timePeriod=new ArrayList<Integer>();
        Map<Integer,Map<Integer,Integer>> userLatency=new HashMap<Integer,Map<Integer,Integer>>();
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            for(EdgeServer server:this.experimentalEdgeServer){
                server.renewCachedDataList();
            }
            randomCaching();
            timePeriod.add(i);
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

            }
        }
        System.out.println("aa");
        return userLatency;
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


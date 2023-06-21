package BaseLine;

import bean.*;
import util.AlgorithmUtils;
import util.DBUtils;

import java.io.IOException;
import java.util.*;

public class BaseRandom {
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    List<PopularData> experimentalPopularData;
    List<Request> predictiveRequest;
    EdgeServerGraph edgeServerGraph;
    Map<Integer,double[]> dataVectorMap;
    //用户最近的服务器
    Map<Integer,Integer> useredge;
    //  Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    public void InitializeData() throws IOException {
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        this.predictiveRequest = DBUtils.getAllRequestByTime("predictive_request",51,80);
        this.useredge= AlgorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
    }
    //缓存数据随机放置
    public void randomCaching(){
        for(EdgeServer server:experimentalEdgeServer){
            Random random = new Random();
            List<PopularData> pds=new ArrayList<PopularData>();
            //每次随机找10次数据填入边缘服务器，若无法填满则停止添加
            int count=0;
            while(server.getRemainingStorageSpace()>=1&&count<10){
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
        for(int i=beginTimestamp;i<=endTimestamp;i++){
            timePeriod.add(i);
        }
        Map<Integer,Map<Integer,Integer>> userLatency=new HashMap<Integer,Map<Integer,Integer>>();
        for(Integer time:timePeriod){
            for(EdgeServer edgeServer:experimentalEdgeServer){
                for(Request rq:predictiveRequest){
                    int serverId=useredge.get(rq.getUserId());

                }
            }
        }

    }
}


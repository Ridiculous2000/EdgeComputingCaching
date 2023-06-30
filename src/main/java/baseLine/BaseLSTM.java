package baseLine;

import bean.EdgeServer;
import bean.ExperimentalSetup;
import bean.Request;
import bean.User;
import our_algorithm.OurAlgorithm;
import util.AlgorithmUtils;
import util.DBUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseLSTM {

    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    Map<Integer,Integer> userNearestServer;
    AlgorithmUtils algorithmUtils;

    public void getServerHistroy(ExperimentalSetup experimentalSetup) throws IOException {
        algorithmUtils = new AlgorithmUtils(experimentalSetup);
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        userNearestServer = algorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        ArrayList<Request> historyRequest = (ArrayList<Request>) DBUtils.getAllRequestByTime("request",0,experimentalSetup.getBeginTimestamp());
        HashMap<OurAlgorithm.UserDataPair,ArrayList<Integer>> userDataMap = new HashMap<OurAlgorithm.UserDataPair,ArrayList<Integer>>();

        int[][][] remMap = new int [1000][100][500];
        //记录各个服务器被请求的情况
        for(Request r:historyRequest){
            int dataId = r.getPopularDataId();
            int userId = r.getUserId();
            int serverId = userNearestServer.get(userId);
            int timestamp = r.getTimestamp();
            int serverIndex = idToIndex(serverId);
            int dataIndex = idToIndex(dataId);
            if(dataIndex>500||serverIndex>1000){
                System.out.println("aaa");
            }

            remMap[serverIndex][timestamp][dataIndex] = 1;
        }
        String fileName = "src/AlgorithmicData/server_history.txt"; // Replace with your desired file name
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            HashMap<Integer,int[][]> serverMap = new HashMap<>();
            for(EdgeServer edgeServer:experimentalEdgeServer){
                int serverIndex = idToIndex(edgeServer.getId());
                serverMap.put(edgeServer.getId(),remMap[serverIndex]);
                int[][] history = remMap[serverIndex];
                for(int i=1;i<experimentalSetup.getBeginTimestamp();i++){
                    StringBuilder sb = new StringBuilder();
                    sb.append(edgeServer.getId()+":");
                    for(int j=0;j<history[i].length;j++){
                        sb.append(history[i][j]);
                        if (j < history[i].length - 1) {
                            sb.append(",");
                        }
                    }
                    writer.write(sb.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int idToIndex(int id){
        if(id>=30000){
            return id - 30000;
        }else{
            return id - 20000;
        }
    }

}

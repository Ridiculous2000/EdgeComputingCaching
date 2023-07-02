package baseLine;

import bean.*;
import our_algorithm.OurAlgorithm;
import util.AlgorithmUtils;
import util.DBUtils;

import java.io.*;
import java.util.*;

public class BaseLSTM {

    HashMap<Integer,ArrayList<Request>> allUserRequest;
    HashMap<Integer,Integer> dataIdToIndex = new HashMap<>();
    List<User> experimentalUserList;
    List<EdgeServer> experimentalEdgeServer;
    Map<Integer,Integer> userNearestServer;
    AlgorithmUtils algorithmUtils;
    List<PopularData> experimentalPopularData;
    List<Request> Request;
    //HashMap<Integer, List<EdgeServer>>
    EdgeServerGraph edgeServerGraph=new EdgeServerGraph();
    Map<Integer,double[]> dataVectorMap;
    //�û�����ķ�����
    Map<Integer,Integer> useredge;
    //  Map<Integer,Map<Integer,Double>> dataSimilarityMap;
    //�������
    CachingDecision cachingDecision = new CachingDecision();
    //����ÿһ��ʱ���������һ��������Ⱥ�Ĵ洢����״̬
    Map<Integer,List<EdgeServer>> edgeCondition;
    public void initializeData(ExperimentalSetup experimentalSetup) throws IOException {
        int beginTimestamp = experimentalSetup.getBeginTimestamp();
        int endTimestamp = experimentalSetup.getEndTimestamp();
        this.algorithmUtils = new AlgorithmUtils(experimentalSetup);
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        this.experimentalPopularData = DBUtils.getAllPopularData();
        this.Request = DBUtils.getAllRequestByTime("request",beginTimestamp,endTimestamp);
        this.useredge= algorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        edgeServerGraph = new EdgeServerGraph();
        edgeServerGraph.initGraph((ArrayList<EdgeServer>) this.experimentalEdgeServer);
        this.allUserRequest = DBUtils.getRequestByTime("request",0,101);
        for(int i=0;i<experimentalPopularData.size();i++){
            dataIdToIndex.put(experimentalPopularData.get(i).getId(),i);
        }
        for(EdgeServer edgeServer:experimentalEdgeServer){
            edgeServer.setMaximumStorageSpace(experimentalSetup.getMaxStorageSpace());
            edgeServer.setRemainingStorageSpace(experimentalSetup.getMaxStorageSpace());
        }
        getServerHistroy(experimentalSetup);
    }


    public void getServerHistroy(ExperimentalSetup experimentalSetup) throws IOException {
        algorithmUtils = new AlgorithmUtils(experimentalSetup);
        this.experimentalUserList = DBUtils.getAllUser();
        this.experimentalEdgeServer = DBUtils.getAllEdgeServer();
        userNearestServer = algorithmUtils.getUserNearestServer(experimentalUserList,experimentalEdgeServer);
        ArrayList<Request> historyRequest = (ArrayList<Request>) DBUtils.getAllRequestByTime("request",0,experimentalSetup.getBeginTimestamp());
        HashMap<OurAlgorithm.UserDataPair,ArrayList<Integer>> userDataMap = new HashMap<OurAlgorithm.UserDataPair,ArrayList<Integer>>();

        int[][][] remMap = new int [200][100][100];
        //��¼��������������������
        for(Request r:historyRequest){
            int dataId = r.getPopularDataId();
            int userId = r.getUserId();
            int serverId = userNearestServer.get(userId);
            int timestamp = r.getTimestamp();
            int serverIndex = idToIndex(serverId);
            int dataIndex = idToIndex(dataId);
            if(dataIndex>300||serverIndex>200){
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
                    sb.append("\n");
                    writer.write(sb.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getCacheDesition(ExperimentalSetup experimentalSetup){
        Map<EdgeServer, HashSet<PopularData>> cachingResult = new HashMap<>();
        Map<Integer, List<List<Integer>>> dataMap = readPredictData("D:\\JavaProject\\EdgeComputingCaching\\src\\AlgorithmicData\\processed_data.txt");
        for (int i=0;i<experimentalSetup.getEndTimestamp()-experimentalSetup.getBeginTimestamp();i++){
            ArrayList<Request> allRequest = allUserRequest.get(i+experimentalSetup.getBeginTimestamp());
            for(EdgeServer server:experimentalEdgeServer){
                if(cachingResult.get(server)==null){
                    cachingResult.put(server,new HashSet<PopularData>());
                }
                List<Integer> dataToCache = dataMap.get(server.getId()).get(i);
                for(int j=0;j<dataToCache.size();j++){
                    int dataId = dataToCache.get(j)+30000;
                    int dataIndex = dataIdToIndex.get(dataId);
                    PopularData popularData = experimentalPopularData.get(dataIndex);
                    if(server.getRemainingStorageSpace()>=popularData.getSize()){
                        server.setRemainingStorageSpace(server.getRemainingStorageSpace()-popularData.getSize());
                        cachingResult.get(server).add(popularData);
                    }
                }

            }
            cachingDecision.setCachingState(cachingResult);
            double sumQoE = algorithmUtils.cacheDecisionSumQoE(cachingDecision,allRequest);
            double FIndex = algorithmUtils.cacheDecisionFIndex(cachingDecision,allRequest);
            double FinalValue = algorithmUtils.cacheDecisionFinalValue(cachingDecision,allRequest);
            AlgorithmResult algorithmResult = new AlgorithmResult("LSTM",sumQoE,FIndex,FinalValue);
            System.out.print(algorithmResult);
        }

    }


    public static Map<Integer, List<List<Integer>>> readPredictData(String filePath) {
        Map<Integer, List<List<Integer>>> dataMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int serverId = 0;
            List<List<Integer>> dataList = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    // �������У���������ӵ������ֵ���
                    dataMap.put(serverId, dataList);
                    // ����serverId��dataList
                    serverId = 0;
                    dataList = new ArrayList<>();
                } else if (line.contains(":")) {
                    // ��ȡserverId
                    serverId = Integer.parseInt(line.split(":")[0]);
                } else {
                    // ��ȡһά�����ֵ
                    List<Integer> data = new ArrayList<>();
                    String[] values = line.split(",");
                    for (String value : values) {
                        data.add(Integer.parseInt(value));
                    }
                    dataList.add(data);
                }
            }
            // �������һ������
            dataMap.put(serverId, dataList);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataMap;
    }

    public int idToIndex(int id){
        if(id>=30000){
            return id - 30000;
        }else{
            return id - 20000;
        }
    }

}

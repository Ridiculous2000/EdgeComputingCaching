package util;

import bean.EdgeServer;
import bean.Request;
import bean.User;
import our_algorithm.OurAlgorithm;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class AlgorithmUtils {
    //���ƶ���ֵ���������ֵ���ٿ���
    public static double similarityThreshold = 0.3;
    //���Ҫ���ǵĶ��ٸ���������
    public static int maxSimilarityNum = 15;
    //���Ŷ���ֵ
    public static int confidenceThreshold = 10;
    //���ݴ������ľ�γ�ȣ����ؾ��룬��λ�� m
    public static HashMap<Integer,HashSet<Integer>> userRequestData = new HashMap<>();
    static {
        ArrayList<Request> allRequest = (ArrayList<Request>) DBUtils.getAllRequestByTime("request",OurAlgorithm.minTimestamp,OurAlgorithm.maxTimestamp);
        for(Request r:allRequest){
            if(r.getTimestamp()<OurAlgorithm.minTimestamp||r.getTimestamp()>OurAlgorithm.maxTimestamp){
                continue;
            }
            if(!userRequestData.containsKey(r.getPopularDataId())){
                userRequestData.put(r.getPopularDataId(),new HashSet<>());
            }
            userRequestData.get(r.getPopularDataId()).add(r.getUserId());
        }
    }

    public static double calculateDistance(Object obj1,Object obj2){
        double lat1 = 50.0;
        double lon1 = 50.0;
        double lat2 = -50.0;
        double lon2 = -50.0;
        try {
            Class<?> class1 = obj1.getClass();
            Method getLat1 = class1.getMethod("getLatitude");
            Method getLon1 = class1.getMethod("getLongitude");
            lat1 = (double) getLat1.invoke(obj1);
            lon1 = (double) getLon1.invoke(obj1);

            Class<?> class2 = obj2.getClass();
            Method getLat2 = class2.getMethod("getLatitude");
            Method getLon2 = class2.getMethod("getLongitude");
            lat2 = (double) getLat2.invoke(obj2);
            lon2 = (double) getLon2.invoke(obj2);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return  calculateDistance(lat1, lon1, lat2, lon2);
    }

    // ���ݾ�γ�ȣ����ؾ��루mΪ��λ��,����ܽ�ֱ�ӽ���Ϊƽ�棬����37.8��144��λ�ľ�γ�����ݼ���
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dx = (lon2 - lon1)*82600;
        double dy = (lat2 - lat1)*111000;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

    //��ȡ��������֮������ƶȣ�dataId1 - dataId2 - sim.
    // minTime��maxTimeд���ɵ���OurAlgorithmUtils�ģ���Ϊ����ֽ�Ľ����ʱ�������ǰ󶨵�
    public static Map<Integer, Map<Integer, Double>> getDataSimilarityMap(Map<Integer,double[]> dataVectorMap) {
        Map<Integer, Map<Integer, Double>> similarityMap = new HashMap<>();
        for (Map.Entry<Integer, double[]> firstEntry : dataVectorMap.entrySet()) {
            int firstDataId = firstEntry.getKey();
            double[] firstDataVector = firstEntry.getValue();
            Map<Integer, Double> similarDataMap = new HashMap<>();
            for (Map.Entry<Integer, double[]> lastEntry : dataVectorMap.entrySet()) {
                int lastDataId = lastEntry.getKey();
                double[] lastDataVector = lastEntry.getValue();
                // �������ƶ�
                double similarity = calculateSimilarity(firstDataId,lastDataId,firstDataVector, lastDataVector, OurAlgorithm.minTimestamp,OurAlgorithm.maxTimestamp);
                if (similarity > similarityThreshold && firstDataId != lastDataId && similarDataMap.size()<maxSimilarityNum) {
                    similarDataMap.put(lastDataId,similarity);
                }
            }
            similarityMap.put(firstDataId, similarDataMap);
        }
        return similarityMap;
    }

    //�������ƶ�
    private static double calculateSimilarity(int dataId1,int dataId2,double[] dataVector1, double[] dataVector2,int minTimestamp,int maxTimestamp) {
        if(dataId1==dataId2){
            return 1;
        }
        //�õ�������û��Ľ���
        HashSet<Integer> userForData1 = userRequestData.get(dataId1);
        HashSet<Integer> userForData2 = userRequestData.get(dataId2);
//        HashSet<Integer> userForData3 = DBUtils.getUserSetByDataId("request",dataId1,minTimestamp,maxTimestamp);
//        HashSet<Integer> userForData4 = DBUtils.getUserSetByDataId("request",dataId2,minTimestamp,maxTimestamp);
        HashSet<Integer> intersection = new HashSet<>(userForData1);
        intersection.retainAll(userForData2);
        // size of jiao!
        int intersectionSize = intersection.size();
        //�������ƶ�
        double cosSim = calculateCosSim(dataVector1,dataVector2);
        double similarity = cosSim;
        //��������
        if(intersectionSize<confidenceThreshold){
            similarity *= intersectionSize/confidenceThreshold;
        }
        return similarity;
    }

    //�����������ƶ�
    private static double calculateCosSim(double[] dataVector1, double[] dataVector2) {
        if (dataVector1.length != dataVector2.length) {
            throw new IllegalArgumentException("Vector lengths are not equal.");
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < dataVector1.length; i++) {
            dotProduct += dataVector1[i] * dataVector2[i];
            norm1 += Math.pow(dataVector1[i], 2);
            norm2 += Math.pow(dataVector2[i], 2);
        }
        // ������������
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        // �����������ƶ�
        double cosineSimilarity = dotProduct / (norm1 * norm2);
        return cosineSimilarity;
    }


    //Ϊÿ���û�ƥ������ķ��������û����͵�����Ĭ�Ϸ��͵�����ķ���������
    public static Map<Integer, Integer> getUserNearestServer(List<User> experimentalUserList, List<EdgeServer> experimentalEdgeServer) {
        HashMap<Integer,Integer> userNearestServer = new HashMap<Integer,Integer>();
        for (User user : experimentalUserList) {
            double minDistance = Double.MAX_VALUE;
            int nearestEdgeServerId = -1;
            for (EdgeServer edgeServer : experimentalEdgeServer) {
                double distance = calculateDistance(user, edgeServer);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestEdgeServerId = edgeServer.getId();
                }
            }
            if (nearestEdgeServerId != -1) {
                userNearestServer.put(user.getId(), nearestEdgeServerId);
            }
        }
        return  userNearestServer;
    }
}

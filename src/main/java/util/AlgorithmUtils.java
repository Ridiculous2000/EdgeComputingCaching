package util;

import bean.EdgeServer;
import bean.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlgorithmUtils {
    //根据传入对象的经纬度，返回距离，单位是 m
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

    // 根据经纬度，返回距离（m为单位）,距离很近直接近似为平面，基于37.8和144点位的经纬度数据计算
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dx = (lon2 - lon1)*82600;
        double dy = (lat2 - lat1)*111000;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

//    public static Map<Integer, Map<Integer, Double>> getDataSimilarityMap(Map<Integer,double[]> dataVectorMap) {
//        Map<Integer, ArrayList<Double>> similarDataMap = new HashMap<>();
//        // 遍历每个数据对象
//        for (Map.Entry<Integer, double[]> entry : dataVectorMap.entrySet()) {
//            int dataId = entry.getKey();
//            double[] currentVector = entry.getValue();
//            ArrayList<Double> similarDataList = new ArrayList<>();
//            // 遍历其他数据对象进行相似度计算
//            for (Map.Entry<Integer, double[]> otherEntry : dataVectorMap.entrySet()) {
//                int otherDataId = otherEntry.getKey();
//                double[] otherVector = otherEntry.getValue();
//                if (dataId != otherDataId) {  // 排除当前数据对象自身
//                    double similarity = calculateCosSim(currentVector, otherVector);
//                    if (similarity > 0.5) {
//                        similarDataList.add(otherDataId);
//                    }
//                }
//            }
//            // 根据相似度降序排序并截取前十位
//            similarDataList.sort((a, b) -> Double.compare(calculateCosSim(dataVectorMap.get(dataId), dataVectorMap.get(b)),
//                    calculateCosSim(dataVectorMap.get(dataId), dataVectorMap.get(a))));
//            if (similarDataList.size() > 10) {
//                similarDataList = new ArrayList<>(similarDataList.subList(0, 10));
//            }
//
//            similarDataMap.put(dataId, similarDataList);
//        }
//
//        return similarDataMap;
//    }

    private static double calculateCosSim(double[] doubles, double[] doubles1) {
        return 0;
    }
}

package util;

import bean.Request;
import bean.User;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {


    public static int maxUserNum = 850;
    public static int maxDataNum = 600;
    public static int dataVectorDimension = 10;
    //读取file，“，”拆分后存入 List<String[]>
    public static List<String[]> readCSV(String filePath) {
        List<String[]> dataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                dataList.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public static void writeRequestSequenceToFile(String filePath){
        List<User> allUser = DBUtils.getAllUser();
        HashMap<Integer,ArrayList<Request>> requestByTime = DBUtils.getRequestByTime("request",0,50);
        HashMap<Integer,int[]> userRequestHistory = new HashMap<>();
        for(User user:allUser){
            userRequestHistory.put(user.getId(),new int[50]);
        }
        for(Map.Entry<Integer,ArrayList<Request>> entry:requestByTime.entrySet()){
            ArrayList<Request> requestList = entry.getValue();
            for(Request request:requestList){
                int userId = request.getUserId();
                int dataId = request.getPopularDataId();
                userRequestHistory.get(userId)[entry.getKey()-1] = dataId;
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<Integer, int[]> entry : userRequestHistory.entrySet()) {
                int key = entry.getKey();
                int[] values = entry.getValue();
                StringBuilder lineBuilder = new StringBuilder();
                lineBuilder.append(key).append(":");
                for (int value : values) {
                    lineBuilder.append(value).append(",");
                }
                lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                String line = lineBuilder.toString();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static HashMap<Integer, ArrayList<Integer>> readUserBehaviorFromFile(String fileName) {
        HashMap<Integer, ArrayList<Integer>> resultMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    int key = Integer.parseInt(parts[0]);
                    String[] valuesStr = parts[1].split(",");
                    ArrayList<Integer> values = new ArrayList<>();
                    for (String valueStr : valuesStr) {
                        values.add(Integer.parseInt(valueStr));
                    }
                    resultMap.put(key, values);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return resultMap;
    }



    //写入矩阵
    public static void writeMatrixToFile(int[][] matrix, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    writer.write(String.valueOf(matrix[i][j]));
                    if (j != matrix[i].length - 1) {
                        writer.write(", ");
                    }
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //读出矩阵
    public static List<List<Double>> readMatrixFromFile(String filePath) {
        List<List<Double>> matrixData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                List<Double> row = new ArrayList<>();
                for (String value : values) {
                    row.add(Double.parseDouble(value.trim()));
                }
                matrixData.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrixData;
    }

    // 获取 dataId,vector 的map
    public static HashMap<Integer,double[]> getDataVectorMap(String filePath,ArrayList<Integer> dataIdList) throws IOException {
        HashMap<Integer,double[]> dataVectorMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            double[][] dataMatrix = new double[maxDataNum][dataVectorDimension];
            int row = 0;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                for (int col = 0; col < values.length; col++) {
                    dataMatrix[col][row] = Double.parseDouble(values[col].trim());
                }
                row++;
            }
            for(int dataId:dataIdList){
                dataVectorMap.put(dataId,dataMatrix[OtherUtils.getIndexById(dataId,"PopularData")]);
            }
            return dataVectorMap;
        }
    }

}


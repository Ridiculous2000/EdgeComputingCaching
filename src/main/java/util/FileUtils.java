package util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {


    public static int maxUserNum = 850;
    public static int maxDataNum = 400;
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


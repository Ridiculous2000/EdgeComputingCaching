package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    //��ȡfile����������ֺ���� List<String[]>
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

    //д�����
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

    //��������
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

}


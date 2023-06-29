package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class OtherUtils {
    public static int maxUserNum = 850;
    public static int maxDataNum = 600;
    //读取时间段内请求，构建评分矩阵，并写入score.txt
    public static void writeRequestMatrix(int minTime, int maxTime) {
        int[][] requestMatrix = new int[maxUserNum][maxDataNum];
        // 建立数据库连接
        try (Connection connection = DBUtils.getConnection()) {
            Statement stmt = connection.createStatement();
            // 查询满足条件的请求
            String query = "SELECT * FROM request WHERE timestamp <= " + maxTime +" AND timestamp >="+minTime;
            ResultSet resultSet = stmt.executeQuery(query);
            // 统计请求数量并更新矩阵
            while (resultSet.next()) {
                int userIndex = getIndexById(resultSet.getInt("user_id"),"User");
                int dataIndex = getIndexById(resultSet.getInt("popular_data_id"),"PopularData");
                requestMatrix[userIndex][dataIndex]++;
            }
            FileUtils.writeMatrixToFile(requestMatrix,"src/AlgorithmicData/cumulative_request_matrix.txt");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //id转索引
    public static int getIndexById(int id,String type){
        if(type.equals("User")){
            return id-1000;
        }else if (type.equals("PopularData")){
            return id - 30000;
        }
        return 0;
    }

    public static boolean doubleEqual(double a, double b) {
        return Math.abs(a - b) < 1e-6;
    }
}

package util;

import bean.EdgeServer;
import bean.PopularData;
import bean.Request;
import bean.User;

import java.sql.*;
import java.util.*;


/*
 * JDBC工具类
 * */
// 此文件已经被忽略，后续不在这个文件进行修改
public class DBUtils {
    private static String driverClass;
    private static String url;
    private static String username;
    private static String password;

    static{

//        driverClass = "com.mysql.cj.jdbc.Driver";
//        url = "jdbc:mysql://localhost:3306/edge_computing_caching?useUnicode=true&useSSL=false&characterEncoding=UTF-8&serverTimezone=GMT%2B8";
//        username = "root";
//        password = "12345678";
        driverClass = "com.mysql.cj.jdbc.Driver";
        url = "jdbc:mysql://localhost:3306/edgecomputing?useUnicode=true&useSSL=false&characterEncoding=UTF-8&serverTimezone=GMT%2B8";
        username = "root";
        password = "123456";
        try {
            //注册驱动
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //获取Connection
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(url, username, password);
    }

    //获取所有用户信息
    public static List<User> getAllUser() {
        String tableName = "user";
        List<User> allUser = new ArrayList<User>();
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            String selectUserSql = SqlUtils.generateSelectSQL(tableName);
            ResultSet resultSet = stmt.executeQuery(selectUserSql);
            while (resultSet.next()) {
                User u = new User(resultSet.getInt("id"), resultSet.getDouble("latitude"), resultSet.getDouble("longitude"));
                allUser.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return allUser;
    }

    //读取所有PopularData信息
    public static List<PopularData> getAllPopularData() {
        String tableName = "popular_data";
        List<PopularData> allPopularData = new ArrayList<PopularData>();
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            String selectPopularDataSql = SqlUtils.generateSelectSQL(tableName);
            ResultSet resultSet = stmt.executeQuery(selectPopularDataSql);
            while (resultSet.next()) {
                PopularData popularData = new PopularData(resultSet.getInt("id"),resultSet.getInt("size"));
                allPopularData.add(popularData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return allPopularData;
    }


    //获取所有EdgeServer信息
    public static List<EdgeServer> getAllEdgeServer() {
        String tableName = "edge_server";
        List<EdgeServer> allEdgeServer = new ArrayList<EdgeServer>();
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            String selectEdgeServerSql = SqlUtils.generateSelectSQL(tableName);
            ResultSet resultSet = stmt.executeQuery(selectEdgeServerSql);
            while (resultSet.next()) {
                EdgeServer es = new EdgeServer();
                es.setId(resultSet.getInt("id"));
                es.setLatitude(resultSet.getDouble("latitude"));
                es.setLongitude(resultSet.getDouble("longitude"));
                es.setCoveringRadius(resultSet.getInt("covering_radius"));
                es.setMaximumStorageSpace(resultSet.getInt("maximum_storage_space"));
                es.setRemainingStorageSpace(resultSet.getInt("remaining_storage_space"));
                allEdgeServer.add(es);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return allEdgeServer;
    }


    public static HashMap<Integer,ArrayList<Request>> getRequestByTime(String tableName,int minTimestamp,int maxTimestamp){
        ArrayList<Request> requests = (ArrayList<Request>) getAllRequestByTime(tableName,minTimestamp,maxTimestamp);
        HashMap<Integer, ArrayList<Request>> requestsByTimestamp = new HashMap<>();
        for (Request request : requests) {
            int timestamp = request.getTimestamp();
            ArrayList timestampRequests = requestsByTimestamp.getOrDefault(timestamp, new ArrayList<>());
            timestampRequests.add(request);
            requestsByTimestamp.put(timestamp, timestampRequests);
        }
        return requestsByTimestamp;
    }

    //可以读取 预测的请求 或 实际的请求
    public static List<Request> getAllRequestByTime(String tableName,int minTimestamp,int maxTimestamp){
        ArrayList<Request> allRequest = new ArrayList<Request>();
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            String selectRequestSql = "SELECT * FROM " + tableName + " WHERE timestamp <= " + maxTimestamp +" AND timestamp >="+minTimestamp;
            ResultSet resultSet = stmt.executeQuery(selectRequestSql);
            while (resultSet.next()) {
                Request r = new Request();
                r.setId(resultSet.getInt("id"));
                r.setUserId(resultSet.getInt("user_id"));
                r.setPopularDataId(resultSet.getInt("popular_data_id"));
                r.setTimestamp(resultSet.getInt("timestamp"));
                allRequest.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return allRequest;
    }

    public static HashSet<Integer> getUserSetByDataId(String tableName,int dataId1,int minTimestamp,int maxTimestamp) {
        HashSet<Integer> userSet = new HashSet<Integer>();
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            String selectRequestSql = "SELECT user_id FROM " + tableName + " WHERE popular_data_id = " + dataId1 + " AND timestamp <= " + maxTimestamp +" AND timestamp >="+minTimestamp;
            ResultSet resultSet = stmt.executeQuery(selectRequestSql);
            while (resultSet.next()) {
                userSet.add(resultSet.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return userSet;
    }



}
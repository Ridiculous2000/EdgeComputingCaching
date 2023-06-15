package data_generation;

import bean.EdgeServer;
import bean.User;
import util.FileUtils;
import util.JDBCUtils;
import util.SqlUtils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InitBasicData {

    //添加所有User信息到数据库
    public void insertBasicUser() {
        List<User> userList = readUserCSV("src/eua-dataset/users/users-melbcbd-generated.csv");
        Connection connection = null;
        try {
            connection = JDBCUtils.getConnection();
            Statement stmt = connection.createStatement();
            for(User user:userList){
                String sql = SqlUtils.generateInsertSQL("basic_user",user);
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertBasicEdgeServer(){
        List<EdgeServer> edgeServerList = readEdgeServerCSV("src/eua-dataset/edge-servers/site-optus-melbCBD.csv");
        Connection connection = null;
        try {
            connection = JDBCUtils.getConnection();
            Statement stmt = connection.createStatement();
            for(EdgeServer edgeServer:edgeServerList){
                String sql = "INSERT INTO basic_server (id,latitude,longitude) values("+edgeServer.getId()+","+edgeServer.getLatitude()+","+edgeServer.getLongitude()+")";
                stmt.executeUpdate(sql);
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
    }

    //  读取边缘服务器数据
    public List<EdgeServer> readEdgeServerCSV(String filePath){
        List<EdgeServer> edgeServerList = new ArrayList<>();
        List<String[]> dataList = FileUtils.readCSV(filePath);
        boolean firstLine = true;
        int id = 20000;
        for (String[] data : dataList) {
            if (data.length >= 2 && !firstLine) {
                double latitude = Double.parseDouble(data[1]);
                double longitude = Double.parseDouble(data[2]);
                EdgeServer edgeServer = new EdgeServer();
                edgeServer.setId(id);
                edgeServer.setLatitude(latitude);
                edgeServer.setLongitude(longitude);
                edgeServerList.add(edgeServer);
                id++;
            }else{
                firstLine = false;
            }
        }
        return edgeServerList;
    }

    //读取User的CSV，存入User
    public List<User> readUserCSV(String filePath) {
        List<User> userList = new ArrayList<>();
        List<String[]> dataList = FileUtils.readCSV(filePath);
        boolean firstLine = true;
        int id = 1000;
        for (String[] data : dataList) {
            if (data.length >= 2 && !firstLine) {
                double latitude = Double.parseDouble(data[0]);
                double longitude = Double.parseDouble(data[1]);
                User user = new User(id,latitude, longitude);
                userList.add(user);
                id++;
            }else{
                firstLine = false;
            }
        }
        return userList;
    }

}

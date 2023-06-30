package data_generation;

import bean.*;
import util.DBUtils;
import util.ProbabilityUtils;
import util.SqlUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class GenerateData {

    //从待选用户表中添加实验用户
    public void addExperimentalUser(int needNum){
        String basicTableName = "basic_user";
        String tableName = "user";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //删除之前表中的信息
            String deleteSql = SqlUtils.generateDeleteAllDataSQL(tableName);
            stmt.executeUpdate(deleteSql);
            //获取所有基本信息
            String selectUserSql = SqlUtils.generateSelectSQL(basicTableName);
            ResultSet resultSet = stmt.executeQuery(selectUserSql);
            List<User> allUser = new ArrayList<User>();
            while (resultSet.next()){
                User u = new User(resultSet.getInt("id"),resultSet.getDouble("latitude"),resultSet.getDouble("longitude"));
                allUser.add(u);
            }
            int allNum = allUser.size();
            //随机筛选出来一定数量要用的信息，并写入表中
            boolean[] choseList = ProbabilityUtils.randomSelect(allNum,needNum);
            for(int i=0;i<allNum;i++){
                if(choseList[i]){
                    String mySQL = SqlUtils.generateInsertSQL(allUser.get(i));
                    stmt.executeUpdate(mySQL);
                }
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //添加实验服务器
    public void addExperimentalServer(int needNum,int maximumStorageSpace){
        String basicTableName = "basic_server";
        String tableName = "edge_server";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //删除之前表中的信息
            String deleteSql = SqlUtils.generateDeleteAllDataSQL(tableName);
            stmt.executeUpdate(deleteSql);
            //获取所有基本信息
            String selectServerSql = SqlUtils.generateSelectSQL(basicTableName);
            ResultSet resultSet = stmt.executeQuery(selectServerSql);
            List<EdgeServer> allServer = new ArrayList<EdgeServer>();
            while (resultSet.next()){
                EdgeServer es = new EdgeServer();
                es.setId(resultSet.getInt("id"));
                es.setLatitude(resultSet.getDouble("latitude"));
                es.setLongitude(resultSet.getDouble("longitude"));
                es.setMaximumStorageSpace(maximumStorageSpace);
                es.setRemainingStorageSpace(maximumStorageSpace);
                allServer.add(es);
            }
            int allNum = allServer.size();
            //随机筛选出来一定数量要用的信息，并写入表中
            boolean[] choseList = ProbabilityUtils.randomSelect(allNum,needNum);
            for(int i=0;i<allNum;i++){
                if(choseList[i]){
                    String mySQL = SqlUtils.generateInsertSQL(allServer.get(i));
                    stmt.executeUpdate(mySQL);
                }
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //生成数据(直接生成数据，数据大小随机分布【1-n/2】)
    public void newPopularData(int needNum,int maxDataSize){
        String tableName = "popular_data";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //删除之前信息
            String deleteSql = SqlUtils.generateDeleteAllDataSQL(tableName);
            stmt.executeUpdate(deleteSql);
            //随机生成数据写入表中
            for(int i=0;i<needNum;i++){
                Random random = new Random();
                int dataSize =  random.nextInt(maxDataSize) + 1;
                PopularData popularData = new PopularData(30000+i,dataSize);
                String mySQL = SqlUtils.generateInsertSQL(popularData);
                stmt.executeUpdate(mySQL);
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 生成请求(先为每个用户生成一个对数据的喜好排名，
    // 这个排名是随机生成的，然后为每个用户按照喜好排名去按照zipf分布生成请求的概率
    // 这些生成的数据写入sql里面 用户id 数据id 概率)
    // 然后生成N个请求，随机把这些请求分发给各个用户，然后每个用户再按照概率去生成请求指向的数据
    // 每个回合重复生成请求和分配请求的过程，用户的喜好部分的txt文件，我们只要用户不变，这个数据就不变

    /**
     * 生成用户对数据的喜好排名，根据排名，基于zipf分布生成请求的概率，然后写入sql
     */
    public void newUserDataProbability(double zipfS){
        String tableName = "user_data_probability";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //删除之前信息
            String deleteSql = SqlUtils.generateDeleteAllDataSQL(tableName);
            stmt.executeUpdate(deleteSql);
            //查询数据表
            List<Integer> dataIdRank = new ArrayList<Integer>();
            List<PopularData> allPopularData = DBUtils.getAllPopularData();
            for (PopularData pd:allPopularData){
                dataIdRank.add(pd.getId());
            }
            //查询用户表
            List<User> allUserList = DBUtils.getAllUser();
            //为每个用户生成一个随机序列
            HashMap<Integer,ArrayList<Integer>> userPreference = new HashMap<>();
            for(User u:allUserList){
                Collections.shuffle(dataIdRank);
                ArrayList<Integer> up = new ArrayList<>(dataIdRank);
                userPreference.put(u.getId(),up);
            }
            //根据随机序列与zipF分布生成概率Map
            Map<Integer,Map<Integer,Double>> userDataProbability = new HashMap<Integer,Map<Integer,Double>>();
            for(Map.Entry<Integer,ArrayList<Integer>> upEntry:userPreference.entrySet()){
                Map<Integer,Double> upZipF = ProbabilityUtils.getZipFProbability(upEntry.getValue(),zipfS);
                userDataProbability.put(upEntry.getKey(),upZipF);
            }
            //把概率Map写入表中
            for(Map.Entry<Integer,Map<Integer,Double>> udp:userDataProbability.entrySet()){
                for(Map.Entry<Integer,Double> dp:udp.getValue().entrySet()){
                    UserDataProbability tempUDP = new UserDataProbability(udp.getKey(),dp.getKey(),dp.getValue());
                    String insertUDP = SqlUtils.generateInsertSQL(tempUDP);
                    stmt.executeUpdate(insertUDP);
                }
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


    //根据概率分布为各个时间段生成请求
    public void newRequest(List<Integer> timestampList){
        String tableName = "request";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //获取概率Map
            String selectServerSql = SqlUtils.generateSelectSQL("UserDataProbability");
            ResultSet resultSet = stmt.executeQuery(selectServerSql);
            Map<Integer,Map<Integer,Double>> userDataProbability = new HashMap<Integer,Map<Integer, Double>>();
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                int dataId = resultSet.getInt("data_id");
                double probability = resultSet.getDouble("probability");
                // 检查外层 Map 中是否已经存在该 userID
                if (!userDataProbability.containsKey(userId)) {
                    userDataProbability.put(userId, new HashMap<Integer, Double>());
                }
                // 将数据存入内层 Map
                userDataProbability.get(userId).put(dataId, probability);
            }

            //根据用户请求数据的概率，为每个时间段生成数据
            List<Request> requestList = new ArrayList<Request>();
            Random random = new Random();
            //生成各个时间段的请求
            for(int timestamp:timestampList){
                Request deleteR = new Request();
                deleteR.setTimestamp(timestamp);
                //删除之前表中的信息
                String deleteSql = SqlUtils.generateDeleteSQL(deleteR);
                stmt.executeUpdate(deleteSql);
                //每个用户生成一个请求
                for (Map.Entry<Integer, Map<Integer, Double>> userEntry : userDataProbability.entrySet()) {
                    int userID = userEntry.getKey();
                    Map<Integer, Double> dataProbMap = userEntry.getValue();
                    //轮盘赌生成数据
                    double randomNumber = random.nextDouble();
                    double cumulativeProbability = 0.0;
                    for (Map.Entry<Integer, Double> dataProbEntry : dataProbMap.entrySet()) {
                        int dataID = dataProbEntry.getKey();
                        double probability = dataProbEntry.getValue();
                        cumulativeProbability += probability;
                        if (cumulativeProbability >= randomNumber) {
                            Request r = new Request(userID,dataID,timestamp);
                            requestList.add(r);
                            break;
                        }
                    }
                }
            }
            for(Request r:requestList){
                String insertRequestSQL = SqlUtils.generateInsertSQL(r);
                stmt.executeUpdate(insertRequestSQL);
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}

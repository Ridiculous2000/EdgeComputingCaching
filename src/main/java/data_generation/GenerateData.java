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

    //�Ӵ�ѡ�û��������ʵ���û�
    public void addExperimentalUser(int needNum){
        String basicTableName = "basic_user";
        String tableName = "user";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //ɾ��֮ǰ���е���Ϣ
            String deleteSql = SqlUtils.generateDeleteAllDataSQL(tableName);
            stmt.executeUpdate(deleteSql);
            //��ȡ���л�����Ϣ
            String selectUserSql = SqlUtils.generateSelectSQL(basicTableName);
            ResultSet resultSet = stmt.executeQuery(selectUserSql);
            List<User> allUser = new ArrayList<User>();
            while (resultSet.next()){
                User u = new User(resultSet.getInt("id"),resultSet.getDouble("latitude"),resultSet.getDouble("longitude"));
                allUser.add(u);
            }
            int allNum = allUser.size();
            //���ɸѡ����һ������Ҫ�õ���Ϣ����д�����
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

    //���ʵ�������
    public void addExperimentalServer(int needNum,int maximumStorageSpace){
        String basicTableName = "basic_server";
        String tableName = "edge_server";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //ɾ��֮ǰ���е���Ϣ
            String deleteSql = SqlUtils.generateDeleteAllDataSQL(tableName);
            stmt.executeUpdate(deleteSql);
            //��ȡ���л�����Ϣ
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
            //���ɸѡ����һ������Ҫ�õ���Ϣ����д�����
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

    //��������(ֱ���������ݣ����ݴ�С����ֲ���1-n/2��)
    public void newPopularData(int needNum,int maxDataSize){
        String tableName = "popular_data";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //ɾ��֮ǰ��Ϣ
            String deleteSql = SqlUtils.generateDeleteAllDataSQL(tableName);
            stmt.executeUpdate(deleteSql);
            //�����������д�����
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

    // ��������(��Ϊÿ���û�����һ�������ݵ�ϲ��������
    // ���������������ɵģ�Ȼ��Ϊÿ���û�����ϲ������ȥ����zipf�ֲ���������ĸ���
    // ��Щ���ɵ�����д��sql���� �û�id ����id ����)
    // Ȼ������N�������������Щ����ַ��������û���Ȼ��ÿ���û��ٰ��ո���ȥ��������ָ�������
    // ÿ���غ��ظ���������ͷ�������Ĺ��̣��û���ϲ�ò��ֵ�txt�ļ�������ֻҪ�û����䣬������ݾͲ���

    /**
     * �����û������ݵ�ϲ����������������������zipf�ֲ���������ĸ��ʣ�Ȼ��д��sql
     */
    public void newUserDataProbability(double zipfS){
        String tableName = "user_data_probability";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //ɾ��֮ǰ��Ϣ
            String deleteSql = SqlUtils.generateDeleteAllDataSQL(tableName);
            stmt.executeUpdate(deleteSql);
            //��ѯ���ݱ�
            List<Integer> dataIdRank = new ArrayList<Integer>();
            List<PopularData> allPopularData = DBUtils.getAllPopularData();
            for (PopularData pd:allPopularData){
                dataIdRank.add(pd.getId());
            }
            //��ѯ�û���
            List<User> allUserList = DBUtils.getAllUser();
            //Ϊÿ���û�����һ���������
            HashMap<Integer,ArrayList<Integer>> userPreference = new HashMap<>();
            for(User u:allUserList){
                Collections.shuffle(dataIdRank);
                ArrayList<Integer> up = new ArrayList<>(dataIdRank);
                userPreference.put(u.getId(),up);
            }
            //�������������zipF�ֲ����ɸ���Map
            Map<Integer,Map<Integer,Double>> userDataProbability = new HashMap<Integer,Map<Integer,Double>>();
            for(Map.Entry<Integer,ArrayList<Integer>> upEntry:userPreference.entrySet()){
                Map<Integer,Double> upZipF = ProbabilityUtils.getZipFProbability(upEntry.getValue(),zipfS);
                userDataProbability.put(upEntry.getKey(),upZipF);
            }
            //�Ѹ���Mapд�����
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


    //���ݸ��ʷֲ�Ϊ����ʱ�����������
    public void newRequest(List<Integer> timestampList){
        String tableName = "request";
        Connection connection = null;
        try {
            connection = DBUtils.getConnection();
            Statement stmt = connection.createStatement();
            //��ȡ����Map
            String selectServerSql = SqlUtils.generateSelectSQL("UserDataProbability");
            ResultSet resultSet = stmt.executeQuery(selectServerSql);
            Map<Integer,Map<Integer,Double>> userDataProbability = new HashMap<Integer,Map<Integer, Double>>();
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                int dataId = resultSet.getInt("data_id");
                double probability = resultSet.getDouble("probability");
                // ������ Map ���Ƿ��Ѿ����ڸ� userID
                if (!userDataProbability.containsKey(userId)) {
                    userDataProbability.put(userId, new HashMap<Integer, Double>());
                }
                // �����ݴ����ڲ� Map
                userDataProbability.get(userId).put(dataId, probability);
            }

            //�����û��������ݵĸ��ʣ�Ϊÿ��ʱ�����������
            List<Request> requestList = new ArrayList<Request>();
            Random random = new Random();
            //���ɸ���ʱ��ε�����
            for(int timestamp:timestampList){
                Request deleteR = new Request();
                deleteR.setTimestamp(timestamp);
                //ɾ��֮ǰ���е���Ϣ
                String deleteSql = SqlUtils.generateDeleteSQL(deleteR);
                stmt.executeUpdate(deleteSql);
                //ÿ���û�����һ������
                for (Map.Entry<Integer, Map<Integer, Double>> userEntry : userDataProbability.entrySet()) {
                    int userID = userEntry.getKey();
                    Map<Integer, Double> dataProbMap = userEntry.getValue();
                    //���̶���������
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

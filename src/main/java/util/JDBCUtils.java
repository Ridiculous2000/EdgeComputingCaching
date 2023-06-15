package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

/*
 * JDBC工具类
 * */
public class JDBCUtils {
    private static String driverClass;
    private static String url;
    private static String username;
    private static String password;

    static{
        driverClass = "com.mysql.cj.jdbc.Driver";
        url = "jdbc:mysql://localhost:3306/edge_computing_caching?useUnicode=true&useSSL=false&characterEncoding=UTF-8&serverTimezone=GMT%2B8";
        username = "root";
        password = "12345678";
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
}
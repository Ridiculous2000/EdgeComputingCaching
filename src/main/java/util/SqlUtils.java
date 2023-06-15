package util;

import java.lang.reflect.Field;

public class SqlUtils {

    //构建InsertSql
    public static String generateInsertSQL(Object object) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        String className = clazz.getSimpleName();
        String tableName = changeHumpToLower(className);
        return generateInsertSQL(tableName,object);
    }

    //构建InsertSql
    public static String generateInsertSQL(String tableName,Object object) throws IllegalAccessException {
        StringBuilder mySql = new StringBuilder();
        Class<?> clazz = object.getClass();
        mySql.append("INSERT INTO ").append(tableName).append(" (");
        // 构建列名
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String columnName = field.getName();
            mySql.append(columnName);
            if (i < fields.length - 1) {
                mySql.append(", ");
            }
        }
        mySql.append(") VALUES (");
        // 构建属性
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            Object value = fields[i].get(object);
            mySql.append(value);
            if (i < fields.length - 1) {
                mySql.append(", ");
            }
        }
        mySql.append(")");
        return mySql.toString();
    }

    //驼峰命名 转 下划线小写
    public static String changeHumpToLower(String HumpName){
        // 将第一个字符转换为小写
        String lowerName = HumpName.substring(0, 1).toLowerCase() + HumpName.substring(1);
        // 添加_,大写转小写
        lowerName = lowerName.replaceAll("([A-Z])", "_$0").toLowerCase();
        return lowerName;
    }
}

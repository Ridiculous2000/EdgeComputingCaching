package util;

import java.lang.reflect.Field;

public class SqlUtils {

    public static String generateSelectSQL(String ClassName){
        return "SELECT * FROM " + changeHumpToLower(ClassName);
    }

    //����InsertSql
    public static String generateInsertSQL(Object object) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        String className = clazz.getSimpleName();
        String tableName = changeHumpToLower(className);
        return generateInsertSQL(tableName,object);
    }

    //����InsertSql
    public static String generateInsertSQL(String tableName,Object object) throws IllegalAccessException {
        StringBuilder mySql = new StringBuilder();
        Class<?> clazz = object.getClass();
        mySql.append("INSERT INTO ").append(tableName);
        // ��������
        Field[] fields = clazz.getDeclaredFields();
        String tableColumn = "(";
        String objectValue = "VALUES (";
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String columnName = changeHumpToLower(field.getName());
            fields[i].setAccessible(true);
            Object value = field.get(object);
            int intValue = 0;
            double doubleValue = 0;
            if (value instanceof Integer) {
                intValue = (int) value;
            } else if (value instanceof Double) {
                doubleValue = (double) value;
            }else{
                continue;
            }
            if(value!=null && intValue+doubleValue>=-900){
                tableColumn += columnName+", ";
                objectValue += value+", ";
            }
        }
        mySql.append(tableColumn.substring(0,tableColumn.lastIndexOf(","))).append(") ").append(objectValue.substring(0,objectValue.lastIndexOf((",")))).append(")");
        return mySql.toString();
    }

    public static String generateDeleteAllDataSQL(String tableName){
        return "DELETE FROM " + tableName;
    }

    public static String generateDeleteSQL(Object object) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        String className = clazz.getSimpleName();
        String tableName = changeHumpToLower(className);
        return generateDeleteSQL(object,tableName);
    }


    public static String generateDeleteSQL(Object object,String tableName) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        StringBuilder mySQL = new StringBuilder();
        mySQL.append("DELETE FROM ").append(tableName).append(" WHERE ");
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String columnName = changeHumpToLower(field.getName());
            field.setAccessible(true);
            Object value = field.get(object);
            int intValue = 0;
            double doubleValue = 0;
            if (value instanceof Integer) {
                intValue = (int) value;
            } else if (value instanceof Double) {
                doubleValue = (double) value;
            }
            if(value!=null && intValue+doubleValue>=-900){
                mySQL.append(columnName).append(" = ").append(value).append(" AND ");
            }
        }
        // ȥ��ĩβ��AND
        mySQL.delete(mySQL.length() - 5, mySQL.length());
        return mySQL.toString();
    }

    //�շ����� ת �»���Сд
    public static String changeHumpToLower(String HumpName){
        // ����һ���ַ�ת��ΪСд
        String lowerName = HumpName.substring(0, 1).toLowerCase() + HumpName.substring(1);
        // ���_,��дתСд
        lowerName = lowerName.replaceAll("([A-Z])", "_$0").toLowerCase();
        return lowerName;
    }
}

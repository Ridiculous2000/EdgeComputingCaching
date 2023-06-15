package util;

import java.lang.reflect.Field;

public class SqlUtils {

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
        mySql.append("INSERT INTO ").append(tableName).append(" (");
        // ��������
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
        // ��������
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

    //�շ����� ת �»���Сд
    public static String changeHumpToLower(String HumpName){
        // ����һ���ַ�ת��ΪСд
        String lowerName = HumpName.substring(0, 1).toLowerCase() + HumpName.substring(1);
        // ���_,��дתСд
        lowerName = lowerName.replaceAll("([A-Z])", "_$0").toLowerCase();
        return lowerName;
    }
}

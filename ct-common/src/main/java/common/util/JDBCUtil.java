package common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtil {

    private static final String MYSQL_DRIVER_CLASS="com.mysql.jdbc.Driver";
    private static final String MYSQL_URL="jdbc:mysql://master:3306/ct?useUnicode=true&characterEncoding=UTF-8";
    private static final String MYSQL_USERNAME="root";
    private static final String MYSQL_PASSWORD="root";
    public static Connection getConnection(){

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USERNAME, MYSQL_PASSWORD);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return conn;
    }
}

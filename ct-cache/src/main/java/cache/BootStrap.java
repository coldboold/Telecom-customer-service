package cache;

import common.util.JDBCUtil;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 利用redis的缓存数据进行索引
 */
public class BootStrap {

    public static void main(String[] args) {

        Connection conn = JDBCUtil.getConnection();
        Jedis jedis = new Jedis("master", 6379);

        PreparedStatement pstat = null;
        ResultSet rs = null;

        try {
            // 首先拿到user的信息整合到redis中去
            String queryUserSql = "select id,tel from ct_user;";
            pstat = conn.prepareStatement(queryUserSql);
            rs = pstat.executeQuery();
            while (rs.next()) {

                int id = rs.getInt(1);
                String tel = rs.getString(2);
                jedis.hset("ct_user", tel, "" + id);
            }
            rs.close();

            // 接着拿date的信息
            String queryDateSql = "select id,year,month,day from ct_date;";
            pstat = conn.prepareStatement(queryDateSql);
            rs = pstat.executeQuery();
            while (rs.next()) {

                int id = rs.getInt(1);
                String year = rs.getString(2);
                String month = rs.getString(3);
                if (month.length() == 1) {
                    month = "0" + month;
                }
                String day = rs.getString(4);
                if (day.length() == 1) {
                    day = "0" + day;
                }
                jedis.hset("ct_date", year + month + day, id + "");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {

            if (jedis != null) {
                jedis.close();
            }

            try {
                if (rs != null) {

                    rs.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            try {
                if (pstat != null) {
                    pstat.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}

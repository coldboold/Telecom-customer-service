package analysis.io;

import common.util.JDBCUtil;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 构造具体输出逻辑
 */
public class MySQLRecordWriter extends RecordWriter<Text, Text> {

    private Connection conn;
    private Jedis jedis;

    public MySQLRecordWriter(TaskAttemptContext context) {

        conn = JDBCUtil.getConnection();
        jedis = new Jedis("master",6379);
    }

    public void write(Text key, Text value)  {

        String[] telDate = key.toString().split("_");
        String tel = telDate[0];
        String date = telDate[1];

        String[] split = value.toString().split("_");
        int sumCall = Integer.parseInt(split[0]);
        int sumDuration = Integer.parseInt(split[1]);
        PreparedStatement pstat = null;
        try {

            String insertSQL = "insert into ct_call (telid,dateid,sumCall,sumDuration) values (?,?,?,?);";
            pstat = conn.prepareStatement(insertSQL);
            pstat.setInt(1, Integer.parseInt(jedis.hget("ct_user", tel)));
            pstat.setInt(2, Integer.parseInt(jedis.hget("ct_date", date)));
            pstat.setInt(3, sumCall);
            pstat.setInt(4, sumDuration);
            System.out.println(pstat.toString());
            pstat.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e){
            e.getLocalizedMessage();
        }finally {
            if (pstat != null) {
                try {
                    pstat.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    public void close(TaskAttemptContext context) throws IOException, InterruptedException {

        if (conn != null) {

            try {
                conn.commit();
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}

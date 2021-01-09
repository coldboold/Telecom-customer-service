package consumer.dao;

import common.bean.BaseDao;
import common.constant.Names;
import common.constant.ValueConstants;
import consumer.bean.Calllog;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HBase数据访问接口
 */
public class HBaseDao extends BaseDao {

    public void init() throws IOException {

        start();
        createNamespaceNX(Names.NAMESPACE.getValue());
        createTableXX(Names.TABLE.getValue(), ValueConstants.REGION_NUMS,Names.COPROCESSOR_CALLEE.getValue(), Names.CF_CALLER.getValue(), Names.CF_CALLEE.getValue());
        end();
    }

    /**
     * 封装calllog对象使用注解将加入的一列数据和表（位置）联系起来
     *
     * @param log
     * @throws Exception
     */
    public void insertData(Calllog log) throws Exception {

        log.setRowKey(genRegionNum(log.getCall1(), log.getCallTime()) + "_"
                + log.getCall1() + "_" + log.getCall2() + "_" + log.getCallTime() + "_" + log.getDuration());
        putData(log);
    }

    public void insertData(String value) throws IOException {

        String[] fields = value.split("\t");
        String call1 = fields[0];
        String call2 = fields[1];
        String callTime = fields[2];
        String duration = fields[3];

        // 主叫用户
        int regionNum = genRegionNum(call1, callTime);
        String rowKey = regionNum + "_" + call1 + "_" + call2 + "_" + callTime + "_" + duration + "_" + "1";
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(Names.CF_CALLER.getValue()), Bytes.toBytes("call1"), Bytes.toBytes(call1));
        put.addColumn(Bytes.toBytes(Names.CF_CALLER.getValue()), Bytes.toBytes("call2"), Bytes.toBytes(call2));
        put.addColumn(Bytes.toBytes(Names.CF_CALLER.getValue()), Bytes.toBytes("callTime"), Bytes.toBytes(callTime));
        put.addColumn(Bytes.toBytes(Names.CF_CALLER.getValue()), Bytes.toBytes("duration"), Bytes.toBytes(duration));
        put.addColumn(Bytes.toBytes(Names.CF_CALLER.getValue()), Bytes.toBytes("flag"), Bytes.toBytes("1"));

        /*
        一条数据发送两次Put这样子效率太低，应该采用协处理器（触发器）
         */
        // 被叫用户
        // 因为查询信息的时候(getStartStopKeys)只根据主叫人的电话号码来查，但是查询某个人的通话时间包含主叫和被叫的时间
        // 使用scan中的filter也可以完成这样子的任务，但是效率不高，所以针对一条信息，生成两条数据插入到HBase中提高效率。
        // 1. 增添一个字段flag， 1：call1为主叫用户；2：call2为主叫用户
        // 2. 添加另外一个列族是为了提升查询速度，减少store的数据大小
        /*int calleeRegionNum = genRegionNum(call2, callTime);
        String calleeRowKey = calleeRegionNum + "_" + call2 + "_" + call1 + "_" + callTime + "_" + duration + "_" + "0";
        Put calleePut = new Put(Bytes.toBytes(calleeRowKey));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("call1"), Bytes.toBytes(call2));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("call2"), Bytes.toBytes(call1));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("callTime"), Bytes.toBytes(callTime));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("duration"), Bytes.toBytes(duration));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("flag"), Bytes.toBytes("0"));
*/
        List<Put> puts = new ArrayList<Put>();
        puts.add(put);
//        puts.add(calleePut);
        putData(Names.TABLE.getValue(), puts);
    }
}

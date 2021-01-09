package coprocessor;

import common.bean.BaseDao;
import common.constant.Names;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 协处理器类
 * 使用协处理器时，要注意创建的表需要加入协处理器
 * 协处理器是在HBase Server上使用的，需要打包到服务器上
 * 同时要记得重启HBase
 */
public class InsertCalleeCoprocessor extends BaseRegionObserver {

    /**
     * 这是HBase上的协处理器方法，在一次Put之后接下来的动作
     *
     * @param e
     * @param put
     * @param edit
     * @param durability
     * @throws IOException
     */
    // 自己编写的未成功，可以尝试等待一会
    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {

        // 1. 获取表对象
        Table table = e.getEnvironment().getTable(TableName.valueOf(Names.TABLE.getValue()));

        // 2. 构造Put
        // 在rowKey中存在很多数据信息，这一点就不具备普适性
        String values = Bytes.toString(put.getRow());
        String[] split = values.split("_");
        String call1 = split[1];
        String call2 = split[2];
        String callTime = split[3];
        String duration = split[4];
        String flag = split[5];

        // 在协处理器中也发生了Put操作，但是此时的Put不引发协处理器再次响应
        // 必须得关闭表连接
        if ("0".equals(flag)) {
            table.close();
            return;
        }
        CoprocessorDao dao = new CoprocessorDao();
        String rowKey = dao.genRegionNums(call2, callTime) + "_" + call2 + "_" + call1 + "_" + callTime + "_" + duration + "_" + "0";

        Put calleePut = new Put(Bytes.toBytes(rowKey));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("call1"), Bytes.toBytes(call2));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("call2"), Bytes.toBytes(call1));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("callTime"), Bytes.toBytes(callTime));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("duration"), Bytes.toBytes(duration));
        calleePut.addColumn(Bytes.toBytes(Names.CF_CALLEE.getValue()), Bytes.toBytes("flag"), Bytes.toBytes("0"));

        // 3. 插入Put
        table.put(calleePut);

        // 4. 关闭资源，否则内存会溢出
        table.close();
    }

    private class CoprocessorDao extends BaseDao {

        public int genRegionNums(String tel, String date) {
            return super.genRegionNum(tel, date);
        }
    }
    /*@Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {

        // 获取表
        Table table = e.getEnvironment().getTable(TableName.valueOf(Names.TABLE.getValue()));

        // 主叫用户的rowkey
        String rowkey = Bytes.toString(put.getRow());
        // 1_133_2019_144_1010_1
        String[] values = rowkey.split("_");

        CoprocessorDao dao = new CoprocessorDao();
        String call1 = values[1];
        String call2 = values[2];
        String calltime = values[3];
        String duration = values[4];
        String flg = values[5];

        if ("1".equals(flg)) {
            // 只有主叫用户保存后才需要触发被叫用户的保存
            String calleeRowkey = dao.getRegionNum(call2, calltime) + "_" + call2 + call1 + "_" + "_" + calltime + "_" + duration + "_0";


            // 保存数据
            Put calleePut = new Put(Bytes.toBytes(calleeRowkey));
            byte[] calleeFamily = Bytes.toBytes(Names.CF_CALLEE.getValue());
            calleePut.addColumn(calleeFamily, Bytes.toBytes("call1"), Bytes.toBytes(call2));
            calleePut.addColumn(calleeFamily, Bytes.toBytes("call2"), Bytes.toBytes(call1));
            calleePut.addColumn(calleeFamily, Bytes.toBytes("calltime"), Bytes.toBytes(calltime));
            calleePut.addColumn(calleeFamily, Bytes.toBytes("duration"), Bytes.toBytes(duration));
            calleePut.addColumn(calleeFamily, Bytes.toBytes("flg"), Bytes.toBytes("0"));
            table.put(calleePut);

            // 关闭表
            table.close();
        }

    }

    private class CoprocessorDao extends BaseDao {

        public int getRegionNum(String tel, String time) {
            return genRegionNum(tel, time);
        }
    }*/
}

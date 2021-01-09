package common.bean;

import common.api.Column;
import common.api.RowKey;
import common.api.TabRef;
import common.constant.Names;
import common.constant.ValueConstants;
import common.util.DateUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 基础数据访问接口
 */
public abstract class BaseDao {

    // 通过ThreadLocal保证同一个线程中可以不重复创建连接和Admin。
    private ThreadLocal<Connection> connHolder = new ThreadLocal<Connection>();
    private ThreadLocal<Admin> adminHolder = new ThreadLocal<Admin>();

    protected void start() throws IOException {

        getConnection();
        getAdmin();
    }

    private Connection getConnection() throws IOException {

        Connection conn = connHolder.get();
        if (conn == null) {

            Configuration conf = HBaseConfiguration.create();
            conn = ConnectionFactory.createConnection(conf);
            connHolder.set(conn);
        }
        return conn;
    }

    private Admin getAdmin() throws IOException {

        Admin admin = adminHolder.get();
        if (admin == null) {
            getConnection();
            admin = connHolder.get().getAdmin();
            adminHolder.set(admin);
        }
        return admin;
    }

    protected void end() throws IOException {

        Admin admin = adminHolder.get();
        if (admin != null) {
            admin.close();
            adminHolder.remove();
        }

        Connection conn = connHolder.get();
        if (conn != null) {
            conn.close();
            connHolder.remove();
        }
    }

    /**
     * 创建命名空间，即使命名空间存在也没有关系
     *
     * @param namespace 命名空间名称
     * @throws IOException
     */
    protected void createNamespaceNX(String namespace) throws IOException {

        Admin admin = getAdmin();
        NamespaceDescriptor namespaceDecriptor = NamespaceDescriptor.create(namespace).build();
        try {
            admin.createNamespace(namespaceDecriptor);
        } catch (NamespaceExistException e) {
        }
    }

    /**
     * 创建表，如果表存在则删除原始的表数据
     *
     * @param name       表名称
     * @param regionNums 分区数
     * @param family     列族
     * @throws IOException
     */
    protected void createTableXX(String name, Integer regionNums, String coprocessorName, String... family) throws IOException {

        Admin admin = getAdmin();
        TableName tableName = TableName.valueOf(name);

        if (admin.tableExists(tableName)) {
            deleteTable(name);
        }
        createTable(name, regionNums, coprocessorName, family);
    }

    private void createTable(String name, Integer regionNums, String coprocessorName, String... family) throws IOException {

        Admin admin = getAdmin();

        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(name));

        if (!"".equals(coprocessorName) && coprocessorName != null) {
            hTableDescriptor.addCoprocessor(coprocessorName);
        }
        if (family == null || family.length == 0) {
            family = new String[]{Names.CF_INFO.getValue()};
        }

        for (String f : family) {

            HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(f);
            hTableDescriptor.addFamily(hColumnDescriptor);
        }
        if (regionNums == null || regionNums <= 1) {
            admin.createTable(hTableDescriptor);
        } else {
            byte[][] bs = genSplitKeys(regionNums);
            admin.createTable(hTableDescriptor, bs);
        }
    }

    private void deleteTable(String name) throws IOException {

        Admin admin = getAdmin();
        TableName tableName = TableName.valueOf(name);
        admin.disableTable(tableName);
        admin.deleteTable(tableName);

    }

    private byte[][] genSplitKeys(Integer regionNums) {

        int splitNums = regionNums - 1;
        byte[][] bs = new byte[splitNums][];

        for (int i = 0; i < splitNums; i++) {

            bs[i] = Bytes.toBytes(i + "|");
        }

        return bs;
    }

    protected void putData(String name, List<Put> put) throws IOException {

        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(name));

        table.put(put);
        table.close();
    }

    protected void putData(String name, Put put) throws IOException {

        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(name));

        table.put(put);
        table.close();
    }

    /**
     * 通过calllog关联表对象，不论每次插入的数据有多少列，只需要修改calllog对象的构造就可以了，具有良好的扩展性
     *
     * @param obj calllog对象
     * @throws Exception
     */
    protected void putData(Object obj) throws Exception {

        Connection conn = getConnection();

        Class clazz = obj.getClass();
        TabRef tabRef = (TabRef) clazz.getAnnotation(TabRef.class);
        String tableName = tabRef.value();
        Table table = conn.getTable(TableName.valueOf(tableName));

        String rowKeyName = "";
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            RowKey rowKey = field.getAnnotation(RowKey.class);
            if (rowKey != null) {
                // 保证能拿到私有变量
                field.setAccessible(true);
                rowKeyName = (String) field.get(obj);
                break;
            }
        }
        Put put = new Put(Bytes.toBytes(rowKeyName));

        for (Field field : fields) {

            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                field.setAccessible(true);
                put.addColumn(Bytes.toBytes(column.family()), Bytes.toBytes(field.getName()), Bytes.toBytes((String) field.get(obj)));
            }
        }

        table.put(put);
        table.close();

    }

    /**
     * 计算得到一条数据的分区编号
     *
     * @param tel  数据的主叫电话
     * @param date 数据的通话日期
     * @return regionNum 分区编号
     */
    protected int genRegionNum(String tel, String date) {

        String userCode = tel.substring(tel.length() - 4);
        String yearMonth = date.substring(0, 6);

        int userCodeHash = userCode.hashCode();
        int yearMonthHash = yearMonth.hashCode();

        // crc 循环冗余校验
        int crc = Math.abs(userCodeHash ^ yearMonthHash);

        int regionNum = crc & ValueConstants.REGION_NUMS;

        return regionNum;
    }

    /**
     * 生成查询数据的startKeys和endKeys
     *
     * @param tel   主叫电话
     * @param start 开始时间
     * @param end   结束时间
     * @return
     */
    protected List<String[]> getStartStopKeys(String tel, String start, String end) {

        List<String[]> list = new ArrayList<String[]>();

        // 获取时间的年月
        String startTime = start.substring(0, 6);
        String endTime = end.substring(0, 6);
        // 使用Calendar类避免跨年
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(DateUtil.parse(startTime, "yyyyMM"));
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(DateUtil.parse(endTime, "yyyyMM"));

        // 之所以这样子获取，是因为不同月份的数据很有可能不在同一个分区
        // 直接指示最大值和最小值，很有可能会漏数据或者出错
        while (startCal.getTimeInMillis() < endCal.getTimeInMillis()) {

            // 正确的key
            // startKey:?_133_201809
            // endKey:?_133_201809|
            String nowTime = DateUtil.format(startCal.getTime(), "yyyyMM");
            int regionNum = genRegionNum(tel, nowTime);
            String startKey = regionNum + "_" + tel + "_" + nowTime;
            String endKey = startKey + "|";
            list.add(new String[]{startKey, endKey});
            startCal.add(Calendar.MONTH, 1);
        }

        return list;
    }


}

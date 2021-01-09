package analysis.mapper;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import java.io.IOException;

public class AnalysisTextMapper extends TableMapper<Text, Text> {

    /**
     * 针对每条HBase的记录，整理发送给reducer
     *
     * @param key
     * @param value
     * @param context
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {

        String rowkey = Bytes.toString(key.get());
        String[] split = rowkey.split("_");
        String call1 = split[1];
        String call2 = split[2];
        String callTime = split[3];
        String duration = split[4];
        String flag = split[5];

        // 一条记录需要发送三次
        String year = callTime.substring(0, 4);
        String month = callTime.substring(0, 6);
        String day = callTime.substring(0, 8);

        // 不仅要发主叫用户的，还需要发被叫用户的，毕竟在读取的时候没有读取被叫用户的
        // 主叫用户
        context.write(new Text(call1 + "_" + year), new Text(duration));
        context.write(new Text(call1 + "_" + month), new Text(duration));
        context.write(new Text(call1 + "_" + day), new Text(duration));

        // 被叫用户
        context.write(new Text(call2 + "_" + year), new Text(duration));
        context.write(new Text(call2 + "_" + month), new Text(duration));
        context.write(new Text(call2 + "_" + day), new Text(duration));
    }
}

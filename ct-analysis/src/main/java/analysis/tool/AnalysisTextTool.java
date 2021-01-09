package analysis.tool;

import analysis.io.MySQLOutputFormat;
import analysis.mapper.AnalysisTextMapper;
import analysis.reducer.AnalysisTextReducer;
import common.constant.Names;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;

/**
 * MapReduce的Tool工具类，主要覆写run方法
 */
public class AnalysisTextTool implements Tool {

    public int run(String[] args) throws Exception {

        // 这里观察源码发现：即使不传入conf，也会自动创建一个conf
        Job job = Job.getInstance();

        job.setJarByClass(AnalysisTextTool.class);
        Scan scan = new Scan();
        // 保证不重复拿数据
        scan.addFamily(Bytes.toBytes(Names.CF_CALLER.getValue()));
        TableMapReduceUtil.initTableMapperJob(Bytes.toBytes(Names.TABLE.getValue()),
                scan, AnalysisTextMapper.class, Text.class, Text.class, job);

        job.setReducerClass(AnalysisTextReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        // 最终输出地是MYSQL，所以添加OutputFormat
        job.setOutputFormatClass(MySQLOutputFormat.class);

        boolean res = job.waitForCompletion(true);
        return res ? 0 : -1;
    }

    public void setConf(Configuration conf) {

    }

    public Configuration getConf() {
        return null;
    }
}

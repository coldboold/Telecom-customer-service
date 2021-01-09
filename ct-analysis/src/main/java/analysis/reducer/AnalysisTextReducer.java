package analysis.reducer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class AnalysisTextReducer extends Reducer<Text, Text, Text, Text> {

    /**
     * 接收到Mapper发来的数据，进行整合处理
     *
     * @param key
     * @param values
     * @param context
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        // key 133-2018
        // value duration(0101)
        int sumCalls = 0;
        int sumDuration = 0;

        for (Text value : values) {

            sumCalls++;
            sumDuration += Integer.parseInt(value.toString());
        }

        context.write(key, new Text(String.valueOf(sumCalls + "_" + sumDuration)));
    }
}

package analysis;

import analysis.tool.AnalysisTextTool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 将HBase数据导入Mysql数据的启动类
 */
public class AnalysisData {

    public static void main(String[] args) throws Exception {

        int run = ToolRunner.run(new AnalysisTextTool(), args);
    }
}

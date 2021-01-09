package common.bean;

import java.io.Closeable;

/**
 * 数据生产者接口
 */
public interface Producer extends Closeable {

    public void setIn(DataIn in);
    public void setOut(DataOut out);
    public void produce();
}

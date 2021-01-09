package common.bean;

import java.io.Closeable;
import java.io.IOException;

/**
 * 输出流接口
 */
public interface DataOut extends Closeable {

    public void setPath(String path);
    public void write(Object data) throws IOException;
    public void write(String data) throws IOException;
}

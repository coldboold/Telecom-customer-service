package producer.io;

import common.bean.DataOut;

import java.io.*;

public class LocalFileDataOut implements DataOut {

    private PrintWriter writer;

    public LocalFileDataOut(String path) {
        setPath(path);
    }

    public void setPath(String path) {

        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void write(Object data) throws IOException {

    }

    public void write(String data) throws IOException {

        writer.println(data);
        writer.flush();
    }

    public void close() throws IOException {

        if (writer != null) {
            writer.close();
        }
    }
}

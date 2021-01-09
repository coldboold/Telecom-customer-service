package producer.io;

import common.bean.Data;
import common.bean.DataIn;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LocalFileDataIn implements DataIn {

    private BufferedReader reader;

    public LocalFileDataIn(String path) {
        setPath(path);
    }

    public void setPath(String path) {
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public Object read() throws IOException {
        return null;
    }

    public <T extends Data> List<T> read(Class<T> clazz) {

        List<T> list = new ArrayList<T>();
        String line;
        try {
            while((line = reader.readLine())!=null){
                T tmp = clazz.newInstance();
                tmp.setValue(line);
                list.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void close() throws IOException {

        if(reader!=null){
            reader.close();
        }
    }
}

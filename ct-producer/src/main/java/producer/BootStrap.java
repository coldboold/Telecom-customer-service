package producer;

import producer.bean.LocalFileProducer;
import producer.io.LocalFileDataIn;
import producer.io.LocalFileDataOut;

import java.io.IOException;

/**
 * 启动对象
 */
public class BootStrap {

    public static void main(String[] args) throws IOException {

        if(args.length < 2){
            System.out.println("参数指示不正确，请检查后再次运行");
            System.exit(-1);
        }

        LocalFileProducer producer = new LocalFileProducer();
        producer.setIn(new LocalFileDataIn(args[0]));
        producer.setOut(new LocalFileDataOut(args[1]));
        producer.produce();
        producer.close();

    }
}

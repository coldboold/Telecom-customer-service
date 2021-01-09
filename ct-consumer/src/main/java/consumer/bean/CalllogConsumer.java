package consumer.bean;

import common.bean.Consumer;
import common.constant.Names;
import consumer.dao.HBaseDao;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

/**
 * 消费者程序
 */
public class CalllogConsumer implements Consumer {

    public void consume() {

        try {

            HBaseDao dao = new HBaseDao();
            dao.init();
            Properties prop = new Properties();
            // 利用类加载器获取配置文件
            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("consumer.properties"));
            KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(prop);

            consumer.subscribe(Collections.singleton(Names.TOPIC.getValue()));

            while (true) {

                ConsumerRecords<String, String> consumerRecords = consumer.poll(100);
                for (ConsumerRecord<String, String> record : consumerRecords) {
                    System.out.println(record.value());
                    dao.insertData(record.value());
//                    Calllog calllog = new Calllog(record.value());
//                    dao.insertData(calllog);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {

    }
}

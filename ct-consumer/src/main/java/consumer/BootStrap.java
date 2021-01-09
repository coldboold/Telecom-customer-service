package consumer;

import consumer.bean.CalllogConsumer;

/**
 * 启动器
 */
public class BootStrap {

    public static void main(String[] args) throws Exception {

        // 基本框架
        CalllogConsumer consumer = new CalllogConsumer();
        consumer.consume();
        consumer.close();
    }
}

package producer.bean;

import common.bean.DataIn;
import common.bean.DataOut;
import common.bean.Producer;
import common.util.DateUtil;
import common.util.NumberUtil;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class LocalFileProducer implements Producer {

    private DataIn in;
    private DataOut out;

    public void setIn(DataIn in) {
        this.in = in;
    }

    public void setOut(DataOut out) {
        this.out = out;
    }

    public void produce() {

        List<Contact> contacts = in.read(Contact.class);
        int contSize = contacts.size();
        String startDate = "20180101000000";
        String endDate = "20190101000000";
        long startTime = DateUtil.parse(startDate, "yyyyMMddHHmmss").getTime();
        long endTime = DateUtil.parse(endDate, "yyyyMMddHHmmss").getTime();

        while (true) {

            // 生成主叫和被叫
            int call1Index = new Random().nextInt(contSize);
            int call2Index = new Random().nextInt(contSize);
            while (call1Index == call2Index) {
                call2Index = new Random().nextInt(contSize);
            }

            Contact call1 = contacts.get(call1Index);
            Contact call2 = contacts.get(call2Index);

            // 生成通话时间
            long callTime = (long) (startTime + (endTime-startTime)*Math.random());
            String callTimeStr = DateUtil.format(new Date(callTime), "yyyyMMddHHmmss");

            // 生成通话随机时长
            String duration = NumberUtil.format(new Random().nextInt(3000),4);

            // 生成通话记录
            Calllog calllog = new Calllog(call1.getTel(), call2.getTel(), callTimeStr, duration);

            try {
                System.out.println(calllog.toString());
                out.write(calllog.toString());
                Thread.sleep(2000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {

        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }
}

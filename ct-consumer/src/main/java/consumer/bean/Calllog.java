package consumer.bean;

import common.api.Column;
import common.api.RowKey;
import common.api.TabRef;

@TabRef(value = "ct:calllog")
public class Calllog {

    @RowKey
    private String rowKey;
    @Column(family = "caller")
    private String call1;
    @Column(family = "caller")
    private String call2;
    @Column(family = "caller")
    private String callTime;
    @Column(family = "caller")
    private String duration;

    public Calllog() {
    }

    public Calllog(String value) {

        String[] split = value.split("\t");
        call1 = split[0];
        call2 = split[1];
        callTime = split[2];
        duration = split[3];
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public String getCall1() {
        return call1;
    }

    public void setCall1(String call1) {
        this.call1 = call1;
    }

    public String getCall2() {
        return call2;
    }

    public void setCall2(String call2) {
        this.call2 = call2;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}

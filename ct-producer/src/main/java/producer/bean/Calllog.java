package producer.bean;

public class Calllog {

    private String call1Tel;
    private String call2Tel;
    private String callTimeStr;
    private String duration;

    public Calllog(String call1Tel, String call2Tel, String callTimeStr, String duration) {
        this.call1Tel = call1Tel;
        this.call2Tel = call2Tel;
        this.callTimeStr = callTimeStr;
        this.duration = duration;
    }

    public String getCall1Tel() {
        return call1Tel;
    }

    public void setCall1Tel(String call1Tel) {
        this.call1Tel = call1Tel;
    }

    public String getCall2Tel() {
        return call2Tel;
    }

    public void setCall2Tel(String call2Tel) {
        this.call2Tel = call2Tel;
    }

    public String getCallTimeStr() {
        return callTimeStr;
    }

    public void setCallTimeStr(String callTimeStr) {
        this.callTimeStr = callTimeStr;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return call1Tel + '\t' + call2Tel + '\t' + callTimeStr + '\t' + duration;
    }
}

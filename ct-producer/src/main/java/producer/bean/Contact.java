package producer.bean;

import common.bean.Data;

public class Contact extends Data {

    private String tel;
    private String name;

    public void setValue(Object val) {
        content = (String) val;
        String[] split = content.split("\t");
        setTel(split[0]);
        setName(split[1]);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}

package common.bean;

public abstract class Data implements Val {

    protected String content;

    public void setValue(Object val) {
        content = (String) val;
    }

    public Object getValue(){
        return content;
    }
}

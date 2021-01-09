package common.constant;

public enum Names {
    NAMESPACE("ct"),
    TOPIC("ct"),
    TABLE("ct:calllog"),
    CF_CALLER("caller"),
    CF_CALLEE("callee"),
    COPROCESSOR_CALLEE("coprocessor.InsertCalleeCoprocessor"),
    CF_INFO("info");

    private String name;

    Names(String name) {

        this.name = name;
    }

    public String getValue(){
        return name;
    }

}

package cn.tklvyou.guiderobot.model;

public class ControllerModel {

    private int type;
    private Object params;


    public ControllerModel(int type, Object params) {
        this.type = type;
        this.params = params;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }
}

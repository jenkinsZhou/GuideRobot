package cn.tklvyou.guiderobot.model;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2019年09月10日16:40
 * @Email: 971613168@qq.com
 */
public class AppConfigInfo {


    /**
     * is_charge : true
     * is_debug : false
     * total_fee : 3.00
     */

    private boolean is_charge;
    private boolean is_debug;
    private String total_fee;

    public boolean isIs_charge() {
        return is_charge;
    }

    public void setIs_charge(boolean is_charge) {
        this.is_charge = is_charge;
    }

    public boolean isIs_debug() {
        return is_debug;
    }

    public void setIs_debug(boolean is_debug) {
        this.is_debug = is_debug;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }
}

package cn.tklvyou.guiderobot.model;

/**
 * @author :JenkinsZhou
 * @description :订单信息
 * @company :途酷科技
 * @date 2019年09月10日17:02
 * @Email: 971613168@qq.com
 */
public class OrderInfo {


    /**
     * wechat : weixin://wxpay/bizpayurl?pr=VmOQGA6
     * alipay : https://qr.alipay.com/bax08998lkzflcbb5zmo004c
     * id : 9
     */

    private String wechat;
    private String alipay;
    private int id;

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getAlipay() {
        return alipay;
    }

    public void setAlipay(String alipay) {
        this.alipay = alipay;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

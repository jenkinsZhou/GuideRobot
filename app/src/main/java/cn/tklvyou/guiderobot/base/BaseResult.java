package cn.tklvyou.guiderobot.base;

import com.blankj.utilcode.util.LogUtils;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.robot.Rotation;

/***
 * 基础数据结构
 */
public class BaseResult<T> {

    private int status;
    private String errmsg;
    private T data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }



}
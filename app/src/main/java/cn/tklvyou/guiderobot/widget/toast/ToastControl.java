package cn.tklvyou.guiderobot.widget.toast;

import android.widget.Toast;


/**
 * @author :JenkinsZhou
 * @description :吐司控制
 * @company :途酷科技
 * @date 2019年06月25日14:18
 * @Email: 971613168@qq.com
 */
public interface ToastControl {

    /**
     * 处理其它异常情况
     *
     * @return
     */
    Toast getToast();

    /**
     * 设置Toast
     *
     * @param toast    ToastUtil 中的Toast
     * @param textView ToastUtil 中的Toast设置的View
     */
    void setToast(Toast toast, RadiusTextView textView);
}

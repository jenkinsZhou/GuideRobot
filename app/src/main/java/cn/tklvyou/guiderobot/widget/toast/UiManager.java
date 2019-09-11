package cn.tklvyou.guiderobot.widget.toast;

import android.app.Application;

import androidx.core.content.ContextCompat;

import cn.tklvyou.guiderobot.manager.FrameLifecycleCallbacks;
import cn.tklvyou.guiderobot.manager.GlideManager;
import cn.tklvyou.guiderobot_new.R;

import static cn.tklvyou.guiderobot.widget.toast.ExceptionConstant.EXCEPTION_NOT_INIT_FAST_MANAGER;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2019年09月11日10:12
 * @Email: 971613168@qq.com
 */
public class UiManager {

    /**
     * ToastUtil相关配置
     */
    private ToastControl mToastControl;
    private static Application mApplication;

    private static String TAG = "UiManager";

    private static volatile UiManager sInstance;

    private UiManager() {
    }

    public static UiManager getInstance() {
        if (sInstance == null) {
            throw new NullPointerException(EXCEPTION_NOT_INIT_FAST_MANAGER);
        }
        return sInstance;
    }



    /**
     * 初始化
     *
     * @param application
     */
    public static UiManager init(Application application) {
        //保证只执行一次初始化属性
        if (mApplication == null && application != null) {
            mApplication = application;
            sInstance = new UiManager();
            //注册activity生命周期
            mApplication.registerActivityLifecycleCallbacks(new FrameLifecycleCallbacks());
            //初始化Toast工具
            ToastUtil.init(mApplication);
            //初始化Glide
            GlideManager.setPlaceholderColor(ContextCompat.getColor(mApplication, R.color.colorPlaceholder));
            GlideManager.setPlaceholderRoundRadius(mApplication.getResources().getDimension(R.dimen.dp_placeholder_radius));
        }
        return getInstance();
    }


    /**
     * 配置ToastUtil
     *
     * @param control
     * @return
     */
    public UiManager setToastControl(ToastControl control) {
        mToastControl = control;
        return this;
    }


    public ToastControl getToastControl() {
        return mToastControl;
    }

}

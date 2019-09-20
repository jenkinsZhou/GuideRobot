package cn.tklvyou.guiderobot.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import cn.tklvyou.arcfaceutils.ArcFaceUtils;
import cn.tklvyou.guiderobot.base.BaseActivity;
import cn.tklvyou.guiderobot.base.MyApplication;
import cn.tklvyou.guiderobot.common.AppConfig;
import cn.tklvyou.guiderobot.log.TourCooLogUtil;
import cn.tklvyou.guiderobot.model.ControllerModel;
import cn.tklvyou.guiderobot.threadpool.ThreadPoolManager;
import cn.tklvyou.guiderobot.widget.toast.ToastUtil;
import cn.tklvyou.guiderobot_new.R;
import cn.tklvyou.serialportlibrary.SerialPort;

import static cn.tklvyou.guiderobot.RobotAction.HEAD_LED_ACTION;
import static cn.tklvyou.guiderobot.RobotAction.HEAD_LED_CLOSE;
import static cn.tklvyou.guiderobot.RobotAction.HEAD_LED_LIGHT;
import static cn.tklvyou.guiderobot.RobotAction.getHeadLedCommand;

/**
 * @author :JenkinsZhou
 * @description :主页
 * @company :途酷科技
 * @date 2019年09月19日16:54
 * @Email: 971613168@qq.com
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    private ArcFaceUtils arcFaceUtils;
    private SerialPort serialPort;
    private Handler handler = new Handler();
    public static final String TAG = "HomeActivity";
    private SurfaceView textureView;
    /**
     * 语音是否说完
     */
    private boolean speakFinish = true;

    @Override
    protected int getActivityLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mContext = this;
        textureView = findViewById(R.id.textureView);
        ImageView btnStartNav = findViewById(R.id.btnStartNav);
        setViewGone(btnStartNav, true);
        btnStartNav.setOnClickListener(this);
        serialPort = ((MyApplication) getApplication()).getLedController();
        initFaceUtil();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartNav:
                skipGuideActivityDelay();
                break;
            default:
                break;
        }
    }


    private void skipGuideActivityDelay() {
        controlSpeakAndLight(false);
        handler.postDelayed(() -> {
            Intent intent = new Intent();
            if (AppConfig.needPay) {
                intent.setClass(mContext, PayActivity.class);
            } else {
                intent.setClass(mContext, GuideActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1000);

    }


    private void initFaceUtil() {
        arcFaceUtils = new ArcFaceUtils(this, 5000);
        arcFaceUtils.activeEngine(isActive -> {
            if (isActive) {
                arcFaceUtils.initUSBMonitor(textureView);
            }
        });
        arcFaceUtils.setIArcFacePeopleListener(() -> {
            //如果说话未结束 则拦截本次人脸识别回调
            if (!speakFinish) {
                return;
            }
            controlSpeakAndLight(true);
        });
    }


    /**
     * 控制说话并亮灯
     */
    private void controlSpeakAndLight(boolean open) {
        ThreadPoolManager.getThreadPoolProxy().execute(() -> {
            try {
                ControllerModel controllerModel = getLedControllerModel(open);
                TourCooLogUtil.d(TAG, "controlSpeakAndLight:是否说话和亮灯:" + open);
                serialPort.sendDataToSerialPort((byte[]) controllerModel.getParams());
                if (open) {
                    speakFinish = false;
                    speckTextSynthesis(AppConfig.defaultSpeak, true);
                }
            } catch (Exception e) {
                TourCooLogUtil.e(TAG, "controlSpeakAndLight()异常---->原因:" + e.toString());
            }
        });

    }


    private ControllerModel getLedControllerModel(boolean toggle) {
        return toggle ? new ControllerModel(HEAD_LED_ACTION, getHeadLedCommand(HEAD_LED_LIGHT)) : new ControllerModel(HEAD_LED_ACTION, getHeadLedCommand(HEAD_LED_CLOSE));
    }


    @Override
    protected void onDestroy() {
        speakFinish = true;
        if (arcFaceUtils != null) {
            arcFaceUtils.onDestroy();
        }
        super.onDestroy();
    }


    @Override
    public void playComplete() {
        super.playComplete();
        //说话结束 因此需要置为说话完成状态
        speakFinish = true;
        //关灯
        controlSpeakAndLight(false);
    }

}

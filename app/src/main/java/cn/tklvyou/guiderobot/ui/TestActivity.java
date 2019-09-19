package cn.tklvyou.guiderobot.ui;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.tklvyou.arcfaceutils.ArcFaceUtils;
import cn.tklvyou.guiderobot.base.BaseActivity;
import cn.tklvyou.guiderobot.widget.toast.ToastUtil;
import cn.tklvyou.guiderobot_new.R;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2019年09月19日21:35
 * @Email: 971613168@qq.com
 */
public class TestActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private ArcFaceUtils arcFaceUtils;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,1);
        surfaceView = findViewById(R.id.surfaceView);
        initFaceUtil();
    }


    private void initFaceUtil() {
        runOnUiThread(() -> {
            arcFaceUtils = new ArcFaceUtils(TestActivity.this, 5000);
            arcFaceUtils.activeEngine(isActive -> {
                if (isActive) {
                    ToastUtil.showSuccess("人脸识别初始化成功");
                    arcFaceUtils.initUSBMonitor(surfaceView);
                }
            });
            arcFaceUtils.setIArcFacePeopleListener(() -> {
                //如果说话未结束 则拦截本次人脸识别回调
                ToastUtil.showSuccess("有人经过");
//            controlSpeakAndLight(true);
            });
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arcFaceUtils.onDestroy();
    }
}

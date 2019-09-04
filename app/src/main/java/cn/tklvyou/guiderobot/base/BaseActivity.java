package cn.tklvyou.guiderobot.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.ctrdemo.AIUITextSynthesis;

import cn.tklvyou.guiderobot.widget.LoadingProgressDialog;
import cn.tklvyou.guiderobot_new.R;

/**
 * Created by Administrator on 2019/4/25.
 */

public abstract class BaseActivity extends AppCompatActivity implements AIUITextSynthesis.ITTSListener {

    private MyApplication application;
    private AIUITextSynthesis aiuiTextSynthesis;
    protected LoadingProgressDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_layout);
        View.inflate(this, getActivityLayoutID(), (ViewGroup) findViewById(R.id.container));
        application = (MyApplication) getApplication();
        aiuiTextSynthesis = application.getAIUITextSynthesis();
        aiuiTextSynthesis.setITTSListener(this);
        dialog = new LoadingProgressDialog(this);
        initView();
    }

    /**
     * 设置Activity的布局ID
     *
     * @return
     */
    protected abstract int getActivityLayoutID();

    protected abstract void initView();

    @Override
    public void playComplete() {

    }

    public void showDialog() {
        dialog.show();
    }

    public void hideDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    @Override
    public void onInitSuccess() {
    }

    @Override
    public void onInitFailed() {
        ToastUtils.showShort("语音组件初始化失败");
    }

    public void speckTextSynthesis(String text, boolean isStop) {
        aiuiTextSynthesis.textSynthesis(text, isStop);
    }


}

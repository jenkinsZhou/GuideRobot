package cn.tklvyou.guiderobot.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.ctrdemo.AIUITextSynthesis;

import cn.tklvyou.guiderobot.manager.GlideManager;
import cn.tklvyou.guiderobot.widget.LoadingProgressDialog;
import cn.tklvyou.guiderobot.widget.dialog.FrameLoadingDialog;
import cn.tklvyou.guiderobot.widget.toast.ToastUtil;
import cn.tklvyou.guiderobot_new.R;

/**
 * Created by Administrator on 2019/4/25.
 */

public abstract class BaseActivity extends AppCompatActivity implements AIUITextSynthesis.ITTSListener {
    /**
     * 语音模块是否可用
     */
    protected boolean voiceModuleEnable = false;
    private MyApplication application;
    private AIUITextSynthesis aiuiTextSynthesis;
    protected LoadingProgressDialog dialog;
    protected FrameLoadingDialog loadingDialog;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_layout);
        View.inflate(this, getActivityLayoutID(), (ViewGroup) findViewById(R.id.container));
        application = (MyApplication) getApplication();
        aiuiTextSynthesis = application.getAIUITextSynthesis();
        aiuiTextSynthesis.setITTSListener(this);
        dialog = new LoadingProgressDialog(this);
        loadingDialog = new FrameLoadingDialog(this, "加载中...");
        loadingDialog.setCancelable(true);
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
        ToastUtil.showSuccess("语音模块初始化成功");
        voiceModuleEnable = true;
    }

    @Override
    public void onInitFailed() {
        ToastUtils.showShort("语音组件初始化失败");
    }

    public void speckTextSynthesis(String text, boolean isStop) {
        aiuiTextSynthesis.textSynthesis(text, isStop);
    }


    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }


    protected void showLoading(String msg) {
        try {
            if (loadingDialog != null && !loadingDialog.isShowing()) {
                if (!TextUtils.isEmpty(msg)) {
                    loadingDialog.setLoadingText(msg);
                }
                loadingDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void closeLoading() {
        try {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    protected void setViewVisible(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    protected void setViewGone(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    protected void loadImage(Object data, ImageView imageView) {
        if (isMainThread()) {
            GlideManager.loadImg(data, imageView);
        } else {
            runOnUiThread(() -> GlideManager.loadImg(data, imageView));
        }
    }
}

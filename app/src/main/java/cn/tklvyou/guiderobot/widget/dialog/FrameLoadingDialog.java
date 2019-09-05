package cn.tklvyou.guiderobot.widget.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import cn.tklvyou.guiderobot_new.R;


public class FrameLoadingDialog extends Dialog {
    public Context context;
    public String loadingText;
    private TextView tvLoadingText;

    public FrameLoadingDialog(Context context) {
        super(context, R.style.frame_loading_dialog);
        this.context = context;
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        window.setWindowAnimations(R.style.DialogWindowStyle);
    }

    public FrameLoadingDialog(Context context, String loadingText) {
        super(context, R.style.frame_loading_dialog);
        this.context = context;
        this.loadingText = loadingText;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogWindowStyle);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_loading_dialog);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        if (TextUtils.isEmpty(loadingText)) {
            tvLoadingText.setVisibility(View.GONE);
        } else {
            tvLoadingText.setText(loadingText);
        }
    }


    public void setLoadingText(String loadingText) {
        if (TextUtils.isEmpty(loadingText)) {
            return;
        }
        this.loadingText = loadingText;
        if (tvLoadingText == null) {
            return;
        }
        tvLoadingText.setText(loadingText);
        View view = getCurrentFocus();
        if (view != null) {
            view.postInvalidate();
        }
    }

    @Override
    public void show() {
        setLoadingText(loadingText);
        super.show();
    }
}
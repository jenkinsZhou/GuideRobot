package cn.tklvyou.guiderobot.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;

import cn.tklvyou.guiderobot.R;


/**
 * 加载框
 */
public class LoadingProgressDialog extends Dialog {

    private Context context;
    private TextView tvTip;
    private ImageView image;
    private String tip;
    private AnimationDrawable frameAnim;

    public LoadingProgressDialog(Context context) {
        this(context, null);
    }


    public LoadingProgressDialog(Context context, String tip) {
        super(context, R.style.Theme_dialog);
        this.context = context;
        this.tip = tip;
    }


    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.common_loading_indicator);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (frameAnim != null && frameAnim.isRunning()) frameAnim.stop();
            }
        });
        tvTip = findViewById(R.id.tip_tv);
        image = findViewById(R.id.image);
        if (tip != null && tip.length() > 0) {
            LogUtils.e(tip);
            tvTip.setText(tip);
        }
        // 通过逐帧动画的资源文件获得AnimationDrawable示例
        frameAnim = (AnimationDrawable) context.getResources().getDrawable(R.drawable.anim_loading);
        image.setBackgroundDrawable(frameAnim);
        if (frameAnim != null && !frameAnim.isRunning()) frameAnim.start();
    }

}
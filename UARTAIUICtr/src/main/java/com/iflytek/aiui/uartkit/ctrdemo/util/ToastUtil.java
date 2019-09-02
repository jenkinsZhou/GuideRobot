package com.iflytek.aiui.uartkit.ctrdemo.util;

import android.content.Context;
import android.widget.Toast;


public class ToastUtil {

    private static Toast sToast;

    private ToastUtil() {
    }

    public static void show(Context context, String text) {
        if (sToast == null) {
            sToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        } else {
            sToast.setText(text);
        }
        sToast.show();
    }
}

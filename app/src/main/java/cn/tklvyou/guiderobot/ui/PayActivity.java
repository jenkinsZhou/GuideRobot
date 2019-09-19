package cn.tklvyou.guiderobot.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

import cn.tklvyou.arcfaceutils.ArcFaceUtils;
import cn.tklvyou.arcfaceutils.interfaces.IArcFacePeopleListener;
import cn.tklvyou.arcfaceutils.interfaces.IArcFaceStatsuListener;
import cn.tklvyou.guiderobot.RobotAction;
import cn.tklvyou.guiderobot.api.RetrofitHelper;
import cn.tklvyou.guiderobot.api.RxSchedulers;
import cn.tklvyou.guiderobot.base.BaseActivity;
import cn.tklvyou.guiderobot.base.MyApplication;
import cn.tklvyou.guiderobot.common.AppConfig;
import cn.tklvyou.guiderobot.log.TourCooLogUtil;
import cn.tklvyou.guiderobot.manager.GlideManager;
import cn.tklvyou.guiderobot.model.ControllerModel;
import cn.tklvyou.guiderobot.model.LocationModel;
import cn.tklvyou.guiderobot.model.Order;
import cn.tklvyou.guiderobot.model.OrderInfo;
import cn.tklvyou.guiderobot.qrcode.QRCodeUtil;
import cn.tklvyou.guiderobot.threadpool.ThreadPoolManager;
import cn.tklvyou.guiderobot.utils.SizeUtil;
import cn.tklvyou.guiderobot.websocket.SimpleListener;
import cn.tklvyou.guiderobot.websocket.SocketListener;
import cn.tklvyou.guiderobot.websocket.WebSocketHandler;
import cn.tklvyou.guiderobot.websocket.response.ErrorResponse;
import cn.tklvyou.guiderobot.widget.toast.ToastUtil;
import cn.tklvyou.guiderobot_new.R;
import cn.tklvyou.serialportlibrary.SerialPort;

import static cn.tklvyou.guiderobot.RobotAction.HEAD_LED_ACTION;
import static cn.tklvyou.guiderobot.RobotAction.HEAD_LED_BREATHE;
import static cn.tklvyou.guiderobot.RobotAction.HEAD_LED_CLOSE;
import static cn.tklvyou.guiderobot.RobotAction.HEAD_LED_LIGHT;
import static cn.tklvyou.guiderobot.RobotAction.getHeadLedCommand;
import static cn.tklvyou.guiderobot.constant.RequestConstant.REQUEST_ERROR;
import static cn.tklvyou.guiderobot.constant.RequestConstant.REQUEST_SUCCESS;

/**
 * @author :JenkinsZhou
 * @description :支付页面
 * @company :途酷科技
 * @date 2019年09月10日11:24
 * @Email: 971613168@qq.com
 */
public class PayActivity extends BaseActivity {
    private static final String PAY_SUCCESS = "1";
    private TextView tvPayAmount;
    private ImageView ivPayTypeAli;
    private ImageView ivPayTypeWeChat;
    private Context mContext;
    public static final String TAG = "PayActivityTag";
    private Handler handler = new Handler();

    @Override
    protected int getActivityLayoutID() {
        return R.layout.activity_pay;
    }

    @Override
    protected void initView() {
        mContext = PayActivity.this;
        tvPayAmount = findViewById(R.id.tvPayAmount);
        ivPayTypeAli = findViewById(R.id.ivPayTypeAli);
        ivPayTypeWeChat = findViewById(R.id.ivPayTypeWeChat);
        WebSocketHandler.getDefault().addListener(socketListener);
        requestOrderInfo(true, true);
    }


    /**
     * 生成二维码并显示
     */
    private void generateQrCodeAndDisplay(String content, boolean isAddLogo, Drawable logo, ImageView imageView) {
        int width = SizeUtil.dp2px(200);
        int height = SizeUtil.dp2px(200);
        if (TextUtils.isEmpty(content)) {
            return;
        }
        String error_correction_level = getResources().getStringArray(R.array.spinarr_error_correction)[2];
        String margin = getResources().getStringArray(R.array.spinarr_margin)[1];
        Bitmap bitmap;
        Bitmap logoBitmap = ImageUtils.drawable2Bitmap(logo);
        bitmap = QRCodeUtil.createQRCodeBitmap(content, width, height, "UTF-8",
                error_correction_level, margin, Color.BLACK, Color.WHITE, isAddLogo ? logoBitmap : null, 0.2F, null);
        if (bitmap == null) {
            return;
        }
        imageView.setImageBitmap(bitmap);
        //将原始图片缩放成ImageView控件的高宽
        Bitmap newBitmap = zoomBitmap(bitmap,
                imageView.getWidth(), imageView.getHeight());
        imageView.setImageBitmap(newBitmap);

    }


    private void loadData(OrderInfo orderInfo) {
        Drawable logoPayAli = ContextCompat.getDrawable(mContext, R.mipmap.img_alipay);
        Drawable logoPayWeChat = ContextCompat.getDrawable(mContext, R.mipmap.img_wechat_pay);
        if (logoPayAli != null) {
            generateQrCodeAndDisplay(orderInfo.getAlipay(), true, logoPayAli, ivPayTypeAli);
        }
        if (logoPayWeChat != null) {
            generateQrCodeAndDisplay(orderInfo.getWechat(), true, logoPayWeChat, ivPayTypeWeChat);
        }
       /* GlideManager.loadImg(orderInfo.getAlipay(),ivPayTypeAli);
        GlideManager.loadImg(orderInfo.getWechat(),ivPayTypeWeChat);*/
        tvPayAmount.setText(AppConfig.payAmont);
    }


    private SocketListener socketListener = new SimpleListener() {
        @Override
        public void onConnected() {
            TourCooLogUtil.i(TAG, "socket连接成功!");
        }

        @Override
        public void onConnectFailed(Throwable e) {
            if (e != null) {
                TourCooLogUtil.e(TAG, "onConnectFailed socket连接失败 原因:" + e.toString());
            } else {
                TourCooLogUtil.e(TAG, "onConnectFailed Throwable == null socket连接失败");
            }
        }

        @Override
        public void onDisconnect() {
            TourCooLogUtil.e(TAG, "onDisconnect() 已断开连接");
        }

        @Override
        public void onSendDataError(ErrorResponse errorResponse) {
            TourCooLogUtil.e(TAG, "onSendDataError() 数据发送失败:" + errorResponse.toString());
            errorResponse.release();
        }

        @Override
        public <T> void onMessage(String message, T data) {
            TourCooLogUtil.i(TAG, "onMessage() 数据接受:message(字符串) = " + message);
            TourCooLogUtil.i("接收到的socket消息", message);
            TourCooLogUtil.i("接收到的socket消息数据", data);
            if (PAY_SUCCESS.equals(message)) {
                requestOrderInfo(false, false);
            }
        }

        @Override
        public <T> void onMessage(ByteBuffer bytes, T data) {
            TourCooLogUtil.i(TAG, "onMessage() 数据接受:message(字节)  = " + bytes);
        }
    };


    /**
     * 请求订单信息
     */
    @SuppressLint("CheckResult")
    private void requestOrderInfo(boolean showLoading, boolean firstLoad) {
        if (showLoading) {
            showLoading("正在获取支付数据");
        }
        RetrofitHelper.getInstance().getServer()
                .requestOrderInfo()
                .compose(RxSchedulers.applySchedulers())
                .subscribe(result -> {
                    closeLoading();
                    switch (result.getStatus()) {
                        case REQUEST_ERROR:
                            ToastUtils.showShort(result.getErrmsg());
                            break;
                        case REQUEST_SUCCESS:
                            loadOrderInfoAndSendMsg(result.getData(), firstLoad);
                            break;
                        default:
                            break;
                    }
                }, throwable -> {
                    closeLoading();
                    TourCooLogUtil.e(TAG, "异常:" + throwable.toString());
                    ToastUtils.showShort("请求失败:" + throwable.toString());
                });
    }

    /**
     * 获取订单信息并发送socket给后台
     *
     * @param orderInfo
     */
    private void loadOrderInfoAndSendMsg(OrderInfo orderInfo, boolean firstLoad) {
        if (orderInfo == null) {
            return;
        }
        loadData(orderInfo);
        Order order = new Order();
        order.setOrder_id(orderInfo.getId());
        Gson gson = new Gson();
        String orderMessage = gson.toJson(order);
        TourCooLogUtil.i(TAG, orderInfo);
        WebSocketHandler.getDefault().send(orderMessage);
        TourCooLogUtil.i(TAG, orderMessage);
        if (firstLoad) {
            return;
        }
        ToastUtil.showSuccess("支付成功");
        skipGuideActivityDelay();
    }

    private void skipGuideActivityDelay() {
        handler.postDelayed(() -> {
            Intent intent = new Intent();
            //跳转至讲解页面
            intent.setClass(mContext, GuideActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }


    /**
     * 图片缩放
     *
     * @param bitmap 对象
     * @param w      要缩放的宽度
     * @param h      要缩放的高度
     * @return newBmp 新 Bitmap对象
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}

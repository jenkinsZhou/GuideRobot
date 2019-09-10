package cn.tklvyou.guiderobot.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

import cn.tklvyou.guiderobot.api.RetrofitHelper;
import cn.tklvyou.guiderobot.api.RxSchedulers;
import cn.tklvyou.guiderobot.base.BaseActivity;
import cn.tklvyou.guiderobot.common.AppConfig;
import cn.tklvyou.guiderobot.log.TourCooLogUtil;
import cn.tklvyou.guiderobot.model.LocationModel;
import cn.tklvyou.guiderobot.model.Order;
import cn.tklvyou.guiderobot.model.OrderInfo;
import cn.tklvyou.guiderobot.qrcode.QRCodeUtil;
import cn.tklvyou.guiderobot.utils.SizeUtil;
import cn.tklvyou.guiderobot.websocket.SimpleListener;
import cn.tklvyou.guiderobot.websocket.SocketListener;
import cn.tklvyou.guiderobot.websocket.WebSocketHandler;
import cn.tklvyou.guiderobot.websocket.response.ErrorResponse;
import cn.tklvyou.guiderobot_new.R;

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
        requestOrderInfo(true);
    }


    /**
     * 生成二维码并显示
     */
    private Bitmap generateQrCodeAndDisplay(String content, boolean isAddLogo, Drawable logo, ImageView imageView) {
        int width = SizeUtil.dp2px(150);
        int height = SizeUtil.dp2px(150);
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        String error_correction_level = getResources().getStringArray(R.array.spinarr_error_correction)[3];
        String margin = getResources().getStringArray(R.array.spinarr_margin)[0];
        Bitmap bitmap;
        if (isAddLogo) {
            Bitmap logoBitmap = ImageUtils.drawable2Bitmap(logo);
            bitmap = QRCodeUtil.createQRCodeBitmap(content, width, height, "UTF-8",
                    error_correction_level, margin, Color.BLACK, Color.WHITE, logoBitmap, 0.2F, null);
            imageView.setImageBitmap(bitmap);
        } else {
            bitmap = QRCodeUtil.createQRCodeBitmap(content, width, height, "UTF-8",
                    error_correction_level, margin, Color.BLACK, Color.WHITE, null, 0.2F, null);
            imageView.setImageBitmap(bitmap);
        }
        return bitmap;
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
            TourCooLogUtil.i("接收到的socket消息",  message);
            TourCooLogUtil.i("接收到的socket消息数据",  data);
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
    private void requestOrderInfo(boolean showLoading) {
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
                            loadOrderInfoAndSendMsg(result.getData());
                            break;
                        default:
                            break;
                    }
                }, throwable -> {
                    closeLoading();
                    ToastUtils.showShort("请求失败:" + throwable.toString());
                });
    }

    /**
     * 获取订单信息并发送socket给后台
     * @param orderInfo
     */
    private void loadOrderInfoAndSendMsg(OrderInfo orderInfo) {
        if (orderInfo == null) {
            return;
        }
        loadData(orderInfo);
        Order order = new Order();
        order.setOrder_id(orderInfo.getId());
        Gson gson = new Gson();
        String orderMessage = gson.toJson(order);
        TourCooLogUtil.i(TAG,orderInfo);
        WebSocketHandler.getDefault().send(orderMessage);
        TourCooLogUtil.i(TAG,orderMessage);
    }

}

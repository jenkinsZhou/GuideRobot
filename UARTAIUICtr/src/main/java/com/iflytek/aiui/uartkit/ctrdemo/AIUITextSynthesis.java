package com.iflytek.aiui.uartkit.ctrdemo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.constant.UARTConstant;
import com.iflytek.aiui.uartkit.core.util.UARTLogger;
import com.iflytek.aiui.uartkit.ctrdemo.processor.OTAProcessor;
import com.iflytek.aiui.uartkit.ctrdemo.processor.SmartConfigProcessor;
import com.iflytek.aiui.uartkit.entity.AIUIPacket;
import com.iflytek.aiui.uartkit.entity.CustomPacket;
import com.iflytek.aiui.uartkit.entity.MsgPacket;
import com.iflytek.aiui.uartkit.listener.EventListener;
import com.iflytek.aiui.uartkit.listener.UARTEvent;
import com.iflytek.aiui.uartkit.util.PacketBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * AIUI语音模块文本合成功能
 * Created by Administrator on 2018/7/13.
 */

public class AIUITextSynthesis {

    private static final String TAG = "AIUITextSynthesis";

    public UARTAgent mAgent;
    private Context context;
    private boolean isStop;

    public AIUITextSynthesis(Context context) {
        this.context = context;
        mAgent = UARTAgent.createAgent("/dev/ttyS0", 115200, new EventListener() {

            @Override
            public void onEvent(UARTEvent event) {
                switch (event.eventType) {
                    case UARTConstant.EVENT_INIT_SUCCESS:
                        Log.d(TAG, "Init UART Success");
                        if(listener != null){
                            listener.onInitSuccess();
                        }
                        break;

                    case UARTConstant.EVENT_INIT_FAILED:
                        Log.e(TAG, "Init UART Failed");
                        if(listener != null){
                            listener.onInitFailed();
                        }
                        break;

                    case UARTConstant.EVENT_MSG:
                        MsgPacket recvPacket = (MsgPacket) event.data;
                        Log.d(TAG,"recvPacket " + recvPacket.toString());
                        processPacket(recvPacket);
                        break;

                    case UARTConstant.EVENT_SEND_FAILED:
                        MsgPacket sendPacket = (MsgPacket) event.data;
                        mAgent.sendMessage(sendPacket);
                    default:
                        Log.w(TAG, "createAgent--->正在说话中:"+event.eventType);
                        if(listener != null){
                            listener.onInitFailed();
                        }
                        break;
                }
            }
        });
    }

    public void textSynthesis(String text,boolean isStop){
        this.isStop = isStop;
        Intent intent = new Intent();
        intent.setAction("com.iflytek.uart.helper.action.cmd");
        intent.putExtra("tts_start",0);
        intent.putExtra("text",text);
        context.sendBroadcast(intent);
    }


    private void processPacket(MsgPacket packet) {
        Log.d(TAG, "type " + packet.getMsgType());
        switch (packet.getMsgType()) {
            case MsgPacket.AIUI_PACKET_TYPE:
                String content = new String(((AIUIPacket) packet).content);
                Log.d(TAG, "recv aiui result" + content);
                proecssAIUIPacket(content);
                break;
            case MsgPacket.CUSTOM_PACKET_TYPE:
                Log.d(TAG, "recv aiui custom data " + Arrays.toString(((CustomPacket)packet).customData));
                break;
            default:
                break;
        }
    }

    private void proecssAIUIPacket(String content) {
        try {
            JSONObject data = new JSONObject(content);
            String type = data.optString("type", "");
            //OTA message
            if (type.equals("ota")) {
                OTAProcessor.process(data);
            } else if (type.equals("smartConfig")) {
                SmartConfigProcessor.process(data);
                Log.d(TAG, "data " + data);
            } else {
                Log.d(TAG, "type: " + type);
                if(type.equalsIgnoreCase("tts_event")){
                    String contentJson = data.optString("content");
                    JSONObject contentData = new JSONObject(contentJson);
                    int eventType = contentData.optInt("eventType");
                    int error = contentData.optInt("error");
                    UARTLogger.log(TAG,"json内容："+contentJson);
                    if(eventType == 1 && error == 0){
                        UARTLogger.log(TAG,"女孩说话结束");
                        if(listener != null){
                            UARTLogger.log(TAG,"女孩说话结束(回调)");
                            listener.playComplete();
                        }
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG,"proecssAIUIPacket()异常---->原因:"+e.toString());
            if(listener != null){
                UARTLogger.log(TAG,"女孩说话结束(回调)");
                listener.playComplete();
            }
        }
    }

    private ITTSListener listener;
    public void setITTSListener(ITTSListener listener){
        this.listener = listener;
    }
    public interface ITTSListener{
        void onInitSuccess();
        void onInitFailed();
        void playComplete();
    }

}

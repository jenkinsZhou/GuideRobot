package com.iflytek.aiui.uartkit.ctrdemo.processor;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * 串口解析AIUI发送SmartConfig消息工具类
 *
 */
public class SmartConfigProcessor {

	private final static String TAG = "SmartConfigProcessor";
	
	private final static int SMART_CONFIG_START_RECEIVE = 9;
	
	private final static int SMART_CONFIG_CONNECTING = 10;
	
	private final static int SMART_CONFIG_STOP_RECEIVE =11;
	
	private final static int SMART_CONFIG_CONNECT_FAIL = 12;
	
	private final static int SMART_CONFIG_RECEIVE_TIMEOUT = 13;
	
	private final static int SMART_CONFIG_RECEIVE_ERROR = 14;
	
	private final static int SMART_CONFIG_CONNECT_SUCCESS = 15;
	
	private final static String KEY_ERRORCODE = "arg2";
	
	
	public static void process(JSONObject smartCfgData) {
		if (null != smartCfgData) {
			try {
				int type = smartCfgData.getInt("smartconfig_type");
				Log.d(TAG, "type "+ type);
				switch (type) {
				case SMART_CONFIG_START_RECEIVE:
					Log.d(TAG, "start_receive");
					break;
				case SMART_CONFIG_CONNECTING:
					Log.d(TAG, "connecting");
					break;
				case SMART_CONFIG_STOP_RECEIVE:
					Log.d(TAG, "stop_receive");
					break;
				case SMART_CONFIG_CONNECT_FAIL:
					int errorCode = smartCfgData.getInt(KEY_ERRORCODE);
					Log.d(TAG, "connect_fail " + errorCode);
					break;
					
				case SMART_CONFIG_RECEIVE_TIMEOUT:
					Log.d(TAG, "receiv_timeout");
					break;
					
				case SMART_CONFIG_RECEIVE_ERROR:
					Log.d(TAG, "receive_error");
					break;
					
				case SMART_CONFIG_CONNECT_SUCCESS:
					Log.d(TAG, "connect_success");
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}
}

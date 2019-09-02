package com.iflytek.aiui.uartkit.ctrdemo.processor;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * 串口解析AIUI发送OTA消息工具类
 *
 */
public class OTAProcessor {

	private static final String TAG = "UART_Controller";

	private  final static int MSG_TYPE_GET_UPDATE_INFO = 0;

	private  final static int MSG_TYPE_ERROR_CODE = 1;

	private  final static int MSG_TYPE_DOWNLOAD_START =2;

	private  final static int MSG_TYPE_DOWNLOAD_STOP = 3;

	private  final static int MSG_TYPE_CHECK_RESULT = 4;

	private  final static int  MSG_TYPE_UPDATE_OFFLINE = 5;

	private  final static int  MSG_DOWNLOAD_PROGRESS = 6;

	private  final static int  MSG_DOWNLOAD_FINISH = 7;

	public static void process (JSONObject otaData) {
		if (null != otaData) {
			try {
				int otatType = otaData.getInt("ota_type");
				int arg1 = otaData.getInt("arg1");
				JSONObject param = null;
				if (otaData.has("data")) {
					param =  otaData.getJSONObject("data");
				}
				switch (otatType) {
				case MSG_TYPE_GET_UPDATE_INFO:

					Log.d(TAG, "update_info: " + param.toString());
					break;
					
				case MSG_TYPE_ERROR_CODE:
					Log.d(TAG, "errorcode ：" + arg1);
					break;
				case MSG_TYPE_DOWNLOAD_START:
					String firmwareVer = "";
					
					try {
						firmwareVer = param.getString("firmware_ver");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Log.d(TAG, "download start");
					break;
					
				case MSG_TYPE_DOWNLOAD_STOP:
					Log.d(TAG, "download stop");
					break;

				case MSG_TYPE_CHECK_RESULT:
					try {
						String firVer 	  = param.getString("update_ver");
						String updateInfo = param.getString("update_info");
						int firSize		  = param.getInt("update_size");

						Log.d(TAG,"find a firmware, version is " + firVer + " firSize is " 
										+ firSize + "updateInfo " + updateInfo);
					} catch (JSONException e) {
						e.printStackTrace();
						Log.e(TAG, "error when extract update check result.");
					}
					break;
					
				case MSG_TYPE_UPDATE_OFFLINE:
					Log.d(TAG, "update_offline");
					break;
					
				case MSG_DOWNLOAD_PROGRESS:
					Log.d(TAG, "download progress :" + arg1);
					break;
					
				case MSG_DOWNLOAD_FINISH:
					Log.d(TAG, "download finish");
					break;
				default:
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}

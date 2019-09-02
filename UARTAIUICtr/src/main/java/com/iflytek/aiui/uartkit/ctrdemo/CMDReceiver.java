package com.iflytek.aiui.uartkit.ctrdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.ctrdemo.util.PacketBuildUtil;
import com.iflytek.aiui.uartkit.entity.ControlPacket;
import com.iflytek.aiui.uartkit.entity.WIFIConfPacket.EncryptMethod;
import com.iflytek.aiui.uartkit.entity.WIFIConfPacket.WIFIStatus;
import com.iflytek.aiui.uartkit.util.PacketBuilder;

public class CMDReceiver extends BroadcastReceiver {
	private static final String TAG = "UARTCMDReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		UARTAgent uartAgent = UARTAgent.getUARTAgent();
		Log.d(TAG, "receiver start");

		if(intent.hasExtra("voice_control")){
			String play_mode = intent.getStringExtra("play_mode");
			if ("enable".equals(play_mode)) {
				uartAgent.sendMessage(PacketBuilder.obtainVoiceCtrPacket(true));
			} else if ("disable".equals(play_mode)) {
				uartAgent.sendMessage(PacketBuilder.obtainVoiceCtrPacket(false));
			}
		}
		
		if(intent.hasExtra("wifi_conf")){
			String ssid = intent.getStringExtra("ssid");
			String passwd = intent.getStringExtra("passwd");
			if(ssid == null || passwd == null){
				Log.d(TAG, "ssid or passwd is empty!");
				return;
			}
			
			uartAgent.sendMessage(PacketBuilder.obtainWIFIConfPacket(WIFIStatus.CONNECTED, EncryptMethod.WPA, ssid, passwd));
		}
		
		if(intent.hasExtra("wifi_status")){
			uartAgent.sendMessage(PacketBuilder.obtainWIFIStatusReqPacket());
		}
		
		if(intent.hasExtra("aiui_conf")){
			String appid = intent.getStringExtra("appid");
			String key = intent.getStringExtra("key");
			String scene = intent.getStringExtra("scene");
			boolean launchDemo = intent.getBooleanExtra("launchDemo", false);
			if(appid == null || key == null || scene == null){
				Log.d(TAG, "appid or key or scene is null!!");
				return;
			}
			uartAgent.sendMessage(PacketBuilder.obtainAIUIConfPacket(appid, key, scene, launchDemo));
		}
		
		if(intent.hasExtra("aiui_conf_update")){
			try {
				JSONObject config = new JSONObject(intent.getStringExtra("config"));
				uartAgent.sendMessage(PacketBuilder.obtainAIUIConfPacket(config));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		
		if(intent.hasExtra("audio_record")){
			uartAgent.sendMessage(PacketBuilder.obtainAIUIAudioRecordCmdPacket());
		}
		
		if(intent.hasExtra("aiui_message")){
			int msgType = intent.getIntExtra("msgType", 1);
			int arg1 = intent.getIntExtra("arg1", 0);
			int arg2 = intent.getIntExtra("arg2", 0);
			String params = intent.getStringExtra("params");
			
			uartAgent.sendMessage(PacketBuilder.obtainAIUICtrPacket(msgType, arg1, arg2, params));
		}
		
		if(intent.hasExtra("aiui_extra")){
			uartAgent.sendMessage(PacketBuilder.obtainCustomPacket(new byte[]{1, 1, 0}));
		}
		
		if(intent.hasExtra("tts_start")){
			Log.i(TAG, "onReceive: 合成文本消息" );
			String ttsText = intent.getStringExtra("text");
			String emotion = intent.hasExtra("emotion") ? 
					intent.getStringExtra("emotion"):"neutral";
					
			HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put("emot", emotion);
			uartAgent.sendMessage(PacketBuilder.obtainTTSStartPacket(ttsText, parameters));
		}
		
		if(intent.hasExtra("tts_stop")){
			Log.d(TAG, "tts_stop");
			uartAgent.sendMessage(PacketBuilder.obtainTTSStopPacket());
		}
		
		//音频测试
		if(intent.hasExtra("audio_test")){
			ControlPacket packet = new ControlPacket();
			packet.controlCMD = "{\"type\": \"factory_test\",\"content\": {\"cmd\": \"rec_test\",\"mic_type\": \"5mic\"}}";
			uartAgent.sendMessage(packet);
		}
		
		if(intent.hasExtra("factory_mode")){
			ControlPacket packet = new ControlPacket();
			packet.controlCMD = "{\"type\":\"factory_mode\",\"content\": {\"cmd\": \"start_factory_test\"}}";
			uartAgent.sendMessage(packet);
		}
		
		//消息过滤测试
		if(intent.hasExtra("filter")) {
			processFilterConfig(intent, uartAgent);
		}
		
		//自定义消息
		if(intent.hasExtra("custom")){
			uartAgent.sendMessage(PacketBuilder.obtainCustomPacket(new byte[]{1,1,2,3,5}));
		}
		
		
		
		//SDK同步接口测试
		if(intent.hasExtra("sync")){
			 processSync(intent, uartAgent);
		}
		
		//OTA 串口接口测试
		if(intent.hasExtra("update_check")){
			uartAgent.sendMessage(PacketBuildUtil.obtainOTASendPacket("update_check"));
			Log.d(TAG, "update_check send.");
		}
		if (intent.hasExtra("start_download")) {
			uartAgent.sendMessage(PacketBuildUtil.obtainOTASendPacket("start_download"));
			Log.d(TAG, "start_download send.");
		}
		if (intent.hasExtra("stop_download")) {
			uartAgent.sendMessage(PacketBuildUtil.obtainOTASendPacket("stop_download"));
			Log.d(TAG, "stop_download send.");
		}
		if (intent.hasExtra("update_offline")) {
			uartAgent.sendMessage(PacketBuildUtil.obtainOTASendPacket("update_offline"));
			Log.d(TAG, "update_offline send.");
		}
		if (intent.hasExtra("get_update_info")) {
			uartAgent.sendMessage(PacketBuildUtil.obtainOTASendPacket("get_update_info"));
			Log.d(TAG, "get_update_info");
		}
		
		//SmartConfig联网 串口测试
		if (intent.hasExtra("smart_config")) {
			String cmd = intent.getStringExtra("cmd");
			if (cmd.equals("start")) {
				String timeout = intent.getStringExtra("timeout");
				uartAgent.sendMessage(PacketBuildUtil.obtainSmartConfigPacket(cmd,Integer.valueOf(timeout)));
				Log.d(TAG, "start " + "timeout " + timeout);
			} else if (cmd.equals("stop")) {
				uartAgent.sendMessage(PacketBuildUtil.obtainSmartConfigPacket(cmd, 0));
				Log.d(TAG, "stop");
			}
		}
	}

	private void processFilterConfig(Intent intent, UARTAgent agent) {
		try {
			JSONObject filterConfig = new JSONObject();
			filterConfig.put("type", "event_filter");
			
			JSONObject content = new JSONObject();
			
			
			if(intent.hasExtra("type_select") || intent.hasExtra("type_unselect")) {
				JSONObject typeFilterConfig = new JSONObject();
				
				if(intent.hasExtra("type_select")){
					JSONArray selects = new JSONArray();
					String selectConfig = intent.getStringExtra("type_select");
					for(String item : selectConfig.split(",")) {
						selects.put(item);
					}
					
					typeFilterConfig.put("select", selects);
				}
				
				if(intent.hasExtra("type_unselect")){
					JSONArray selects = new JSONArray();
					String selectConfig = intent.getStringExtra("type_unselect");
					for(String item : selectConfig.split(",")) {
						selects.put(item);
					}
					
					typeFilterConfig.put("unselect", selects);
				}
				
				content.put("type_filter", typeFilterConfig);
			}
			
			if(intent.hasExtra("sub_select") || intent.hasExtra("sub_unselect")) {
				JSONObject subFilterConfig = new JSONObject();
				
				if(intent.hasExtra("sub_select")){
					JSONArray selects = new JSONArray();
					String selectConfig = intent.getStringExtra("sub_select");
					for(String item : selectConfig.split(",")) {
						selects.put(item);
					}
					
					subFilterConfig.put("select", selects);
				}
				
				if(intent.hasExtra("sub_unselect")){
					JSONArray selects = new JSONArray();
					String selectConfig = intent.getStringExtra("sub_unselect");
					for(String item : selectConfig.split(",")) {
						selects.put(item);
					}
					
					subFilterConfig.put("unselect", selects);
				}
				
				content.put("sub_filter", subFilterConfig);
			}
			
			if(intent.hasExtra("service_select") || intent.hasExtra("service_unselect")) {
				JSONObject serviceFilterConfig = new JSONObject();
				
				if(intent.hasExtra("service_select")){
					JSONArray selects = new JSONArray();
					String selectConfig = intent.getStringExtra("service_select");
					for(String item : selectConfig.split(",")) {
						selects.put(item);
					}
					
					serviceFilterConfig.put("select", selects);
				}
				
				if(intent.hasExtra("service_unselect")){
					JSONArray selects = new JSONArray();
					String selectConfig = intent.getStringExtra("service_unselect");
					for(String item : selectConfig.split(",")) {
						selects.put(item);
					}
					
					serviceFilterConfig.put("unselect", selects);
				}
				
				content.put("service_filter", serviceFilterConfig);
			}
			
			filterConfig.put("content", content);
			
			
			ControlPacket filterPacket = new ControlPacket();
			filterPacket.controlCMD = filterConfig.toString();
			
			agent.sendMessage(filterPacket);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processSync(Intent intent, UARTAgent uartAgent) {
		//状态同步
		if(intent.hasExtra("status")){
			uartAgent.sendMessage(PacketBuildUtil.obtainStatusSyncPacket());	
		}
		
		//个性化数据同步
		if(intent.hasExtra("individual")){
			uartAgent.sendMessage(PacketBuildUtil.obtainIndividualSyncPacket());
		}
		
		//第三方平台账号同步
		if(intent.hasExtra("account")){
			uartAgent.sendMessage(PacketBuildUtil.obtainAccountSyncPacket());
		}
	}

}

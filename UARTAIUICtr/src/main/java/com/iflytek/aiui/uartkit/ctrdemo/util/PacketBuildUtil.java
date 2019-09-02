package com.iflytek.aiui.uartkit.ctrdemo.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiui.uartkit.entity.ControlPacket;
import com.iflytek.aiui.uartkit.entity.MsgPacket;
import com.iflytek.aiui.uartkit.util.PacketBuilder;

public class PacketBuildUtil {

	/**
	 * 构造串口OTA发送控制命令消息内容
	 * @param cmd 命令消息:"update_check","start_download","stop_download","update_offline","get_update_info"
	 * @return
	 */
	public static MsgPacket obtainOTASendPacket(String cmd) {
		try{
			JSONObject content = new JSONObject();
			content.put("cmd", cmd);
			
			JSONObject ret = new JSONObject();
			ret.put("type", "ota");
			ret.put("content", content);
			
			ControlPacket controlPacket = new ControlPacket();
			controlPacket.controlCMD = ret.toString();
			return controlPacket;
		} catch (JSONException e) {
			return null;
		}
	}
	
	/**
	 * 构造SmartConfig控制命令消息内容
	 */
	public static MsgPacket obtainSmartConfigPacket(String cmd, int timeout) {
		try{
			JSONObject content = new JSONObject();
			content.put("cmd", cmd);
			content.put("timeout", timeout);
			
			JSONObject ret = new JSONObject();
			ret.put("type", "smartcfg");
			ret.put("content", content);
			
			ControlPacket controlPacket = new ControlPacket();
			controlPacket.controlCMD = ret.toString();
			return controlPacket;
		} catch (JSONException e) {
			return null;
		}
	}
	

	//同步操作。
	public static int CMD_SYNC = 13;
	
	//同步状态数据。
	static int SYNC_DATA_STATUS = 0;
	
	//同步个性化数据。
	static int SYNC_DATA_INDIVIDUAL = 1;
	
	//第三方账号关联数据。
	static int SYNC_DATA_ACCOUNT = 2;
	
	public static MsgPacket obtainStatusSyncPacket(){
		try {
			JSONObject syncParamsJson = new JSONObject();
			syncParamsJson.put("withSign", "0");
			
			
			JSONObject syncStatusJson = new JSONObject();
			syncStatusJson.put("key1", "val1");
			syncStatusJson.put("key2", "val2");
			
			byte[] syncData = syncStatusJson.toString().getBytes("utf-8");
			
			return PacketBuilder.obtainAIUICtrPacket(
					PacketBuildUtil.CMD_SYNC, SYNC_DATA_STATUS, 0, syncParamsJson.toString(), syncData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static MsgPacket obtainIndividualSyncPacket(){
		try {
			JSONObject syncParamsJson = new JSONObject();
			syncParamsJson.put("withSign", "0");

			syncParamsJson.put("usr_id", "common");
			
			// business_id取值：contact（联系人）、location（地点）和word_list（词表）
			syncParamsJson.put("business_id", "word_list");
			
			// 待同步的数据，字符必须用utf-8编码
			byte[] syncData = "张三\r\n李四\r\n王五".getBytes("utf-8");
			
			return PacketBuilder.
					obtainAIUICtrPacket(PacketBuildUtil.CMD_SYNC, SYNC_DATA_INDIVIDUAL, 
							0, syncParamsJson.toString(), syncData);
		}  catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static MsgPacket obtainAccountSyncPacket(){
		JSONObject syncParamsJson = new JSONObject();
		try {
			syncParamsJson.put("withSign", "0");
			
			JSONObject syncDataJson = new JSONObject();
			syncDataJson.put("tuid", "123");
			syncDataJson.put("tplatform", "rsd");
			
			byte[] syncData = syncDataJson.toString().getBytes("utf-8");
			
			return PacketBuilder.
					obtainAIUICtrPacket(PacketBuildUtil.CMD_SYNC, SYNC_DATA_ACCOUNT, 0, syncParamsJson.toString(), syncData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

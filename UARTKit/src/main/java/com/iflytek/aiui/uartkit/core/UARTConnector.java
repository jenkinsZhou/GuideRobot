package com.iflytek.aiui.uartkit.core;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.R.integer;
import android.util.Log;

/**
 * 
 *   与底层的串口关联的连接类，通过init send destroy几个方法操作底层串口
 *         底层串口获取到数据之后通过反射调用该类的onReceive方法通知数据到来
 */
public class UARTConnector {
	private static final String TAG = "UARTConnector";
	
	static {
		System.loadLibrary("serial_port");
	}

	private static UARTConnector sConnector;
	public static UARTConnector getUARTConnector(){
		if(sConnector == null){
			sConnector = new UARTConnector();
		}
		
		return sConnector;
	}
	
	private SerialPort mUARTDevice;
	private ReadThread mReadThread;
	private List<DataListener> mDataListeners;
	
	private UARTConnector(){
		mDataListeners = new CopyOnWriteArrayList<UARTConnector.DataListener>();
	}

	public int init(String device, int speed){
		try {
			mUARTDevice = new SerialPort(new File(device), speed, 0);
			mReadThread = new ReadThread(); 
			mReadThread.start();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public void registerDataListener(DataListener listener){
		if(listener != null){
			mDataListeners.add(listener);
		}
	}
	
	public void unRegisterDataListener(DataListener listener){
		if(listener != null){
			mDataListeners.remove(listener);
		}
	}

	public int send(byte[] data){
		if(mUARTDevice == null) return -1;
		
		try {
			mUARTDevice.getOutputStream().write(data);
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		
	}

	public void destroy(){
		if(mUARTDevice != null) {
			mUARTDevice.close();
		}
		if(mReadThread != null) {
			mReadThread.interrupt();
		}
		
		sConnector = null;
	}
	
	private native static FileDescriptor open(String path, int baudrate, int flags);
	private native void close();

	private class ReadThread extends Thread {
		private boolean waitLen = true;
		private byte[] recvBuf = new byte[5];
		private int recvIndex = 0;
		
		
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					InputStream input = mUARTDevice.getInputStream();
					if (input == null) return;
					size = input.read(buffer);
					if (size <= 0) continue;
					
					recvData(buffer, 0, size);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}


		private void recvData(byte[] buffer, int offset, int size) {
			//当前buffer剩余可写字节数
			int writeLen = Math.min(recvBuf.length - recvIndex,	size);
			System.arraycopy(buffer, offset, recvBuf, recvIndex, writeLen);
			recvIndex += writeLen;
			
			//当前buffer写满
			if(recvIndex == recvBuf.length){
				if(waitLen){
					//消息头是否符合串口消息格式定义
					if(recvBuf[0] != (byte)0xA5 || recvBuf[1] != (byte)0x01){
						Log.w(TAG, "recv data not SYNC HEAD, drop "
											+ Arrays.toString(recvBuf));
						recvIndex = 0;
					}else{
						Log.d(TAG, "head msg" + Arrays.toString(recvBuf));
						//接收消息头，解析长度
						int msgLen = (recvBuf[4]& 0xFF) << 8 | (recvBuf[3] & 0xFF) + 8;
						byte[] msgBuf = new byte[msgLen];
						//将已接收的消息头拷贝到申请的消息buf中
						System.arraycopy(recvBuf, 0, msgBuf, 0, recvBuf.length);
						recvBuf = msgBuf;
						waitLen = false;
					}
				}else{
					//完整消息接收完成
					for(DataListener listener : mDataListeners){
						listener.onReceive(recvBuf);
					}
					recvIndex = 0;
					recvBuf = new byte[5];
					waitLen = true;
				}
			}

			//buffer left
			if(writeLen != size){
				recvData(buffer, offset + writeLen, size - writeLen);
			}
		}
	}
	
	public static interface DataListener{
		public void onReceive(byte[] data); 
	}
	
}

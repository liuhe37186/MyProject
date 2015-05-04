package com.zed3.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

//import com.zed3.log.MyLog;
//import com.zed3.log.TAG;

public class TrackPlayQueue {
	private BlockingQueue<byte[]> storage = new LinkedBlockingQueue<byte[]>(500);

	public void push(short[] c) throws InterruptedException// 入列
	{
		if (!storage.offer(shortArray2ByteArray(c))) {
//			MyLog.e(TAG.voice_delay, "push error = " + storage.size());
		}else{
//			MyLog.e(TAG.voice_delay, "push ok = " + storage.size());
		}
	}

	public  short[] pop() throws InterruptedException // 数据出列
	{
		byte[] tmp = storage.poll(20, TimeUnit.MILLISECONDS);
		if(tmp != null){
//			MyLog.e(TAG.voice_delay, "pop size = "+storage.size());
		}else{
//			MyLog.e(TAG.voice_delay, "pop size =  0");
			return null;
		}
		return byteArray2ShortArray(tmp);
	}
	public void clear(){
		if(!storage.isEmpty()){
			storage.clear();
		}
	}

	public short[] byteArray2ShortArray(byte[] data) {
		short[] shorts = new short[data.length / 2];
		ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
				.get(shorts);
		return shorts;
	}

	public static byte[] shortArray2ByteArray(short shorts[]) {
		// to turn shorts back to bytes.
		byte[] bytes = new byte[shorts.length * 2];
		ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
				.put(shorts);
		return bytes;
	}
}

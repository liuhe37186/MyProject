package com.zed3.sipua;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.zoolu.tools.MyLog;

public class SyncBufferQueue {
	private BlockingQueue<byte[]> storage = new LinkedBlockingQueue<byte[]>();
	
	public  void push(byte[] c) throws InterruptedException// 入列
	{
		MyLog.e("BlockingQueue", "thread2 called,size =" +storage.size());
		if(!storage.offer(c)){
			MyLog.e("BlockingQueue", "thread2 push faild");
		}
	}

	public  byte[] pop() throws InterruptedException // 数据出列
	{
		MyLog.e("BlockingQueue", "thread3 pop called,size = "+storage.size());
		byte[] val = storage.take();
		return val;
	}

}

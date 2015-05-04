package com.zed3.sipua;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.zoolu.tools.MyLog;

public class TimeOutSyncBufferQueue {
	private BlockingQueue<byte[]> storage = new LinkedBlockingQueue<byte[]>();

	public void push(byte[] c) throws InterruptedException// 入列
	{
		MyLog.e("BlockingQueue", "thread1 push called,size="+storage.size());
		if (!storage.offer(c)) {
			MyLog.e("BlockingQueue", "thread1 push faild");
		}
	}
	public static int count = 0;
	public byte[] pop() throws InterruptedException // 20ms超时出列
	{
		MyLog.e("BlockingQueue", "thread2 pop called");
		byte[] val = storage.poll(20, TimeUnit.MILLISECONDS);
		return val;
	}

}

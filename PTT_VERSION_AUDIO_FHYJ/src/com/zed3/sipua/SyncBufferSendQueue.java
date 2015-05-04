package com.zed3.sipua;

import java.util.LinkedList;
import java.util.Queue;

import org.zoolu.tools.MyLog;

public class SyncBufferSendQueue {

private Queue<byte[]> storage = new LinkedList<byte[]>();
	
	byte[] val = null;

	public synchronized void push(byte[] c) throws InterruptedException// 入列
	{
//		while (storage.size() == 100)// 堆列已满，不能入列
//		{
//			try {
//				this.wait();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		if(storage.size() == 200)
			storage.clear();
		
		storage.offer(c);// 数据入列
		
		MyLog.e("SyncBufferSendQueue", "push count is :" + storage.size());
		
		this.notify();// 通知其他线程（另一个线程）把数据出列
	}

	public synchronized byte[] pop() throws InterruptedException// 数据出列
	{
		while (storage.size() == 0)// 无数据，不能出列
		{
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.notify();// 通知其他线程入列
		
		val = storage.poll();
		if (val == null)
			MyLog.e("SyncBufferSendQueue", "buffer is null");
		
		MyLog.e("SyncBufferSendQueue", "pop count is:" + storage.size());
		
		return val;
	}

	
	
}

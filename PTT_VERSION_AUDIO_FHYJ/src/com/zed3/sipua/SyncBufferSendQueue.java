package com.zed3.sipua;

import java.util.LinkedList;
import java.util.Queue;

import org.zoolu.tools.MyLog;

public class SyncBufferSendQueue {

private Queue<byte[]> storage = new LinkedList<byte[]>();
	
	byte[] val = null;

	public synchronized void push(byte[] c) throws InterruptedException// ����
	{
//		while (storage.size() == 100)// ������������������
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
		
		storage.offer(c);// ��������
		
		MyLog.e("SyncBufferSendQueue", "push count is :" + storage.size());
		
		this.notify();// ֪ͨ�����̣߳���һ���̣߳������ݳ���
	}

	public synchronized byte[] pop() throws InterruptedException// ���ݳ���
	{
		while (storage.size() == 0)// �����ݣ����ܳ���
		{
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.notify();// ֪ͨ�����߳�����
		
		val = storage.poll();
		if (val == null)
			MyLog.e("SyncBufferSendQueue", "buffer is null");
		
		MyLog.e("SyncBufferSendQueue", "pop count is:" + storage.size());
		
		return val;
	}

	
	
}

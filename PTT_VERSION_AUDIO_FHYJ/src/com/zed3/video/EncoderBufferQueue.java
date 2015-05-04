package com.zed3.video;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.zoolu.tools.MyLog;

public class EncoderBufferQueue {

	
	private BlockingQueue<byte[]> storage = new LinkedBlockingQueue<byte[]>(5);
	
	byte[] val = null;
//	Lock lock = new ReentrantLock();  
	public  void push(byte[] c) throws InterruptedException// ����
	{
		MyLog.e("BlockingQueue", "push called!!!"+storage.size());
//		while (storage.size() == 2000)// ������������������
//		{
//			try {
//				this.wait();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		lock.lock();
//		try {
//			if(storage.size() >= 500)
//			{
//				storage.clear();
//				MyLog.e("SyncQueue", "SyncQueue clear size:" + storage.size());
//			}	
		if(!storage.offer(c)){
			MyLog.e("SyncQueue", "push count is :" + storage.size());
		}
//		storage.put(c);
//			storage.offer(c);// ��������
			
			
//			this.notify();// ֪ͨ�����̣߳���һ���̣߳������ݳ���
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally {  
//			 lock.unlock();   
//		} 
	}

	public  byte[] pop() throws InterruptedException // ���ݳ���
	{
//		lock.lock();
//		try {
//			while (storage.size() == 0)// �����ݣ����ܳ���
//			{
//				try {
//					this.wait();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		MyLog.e("BlockingQueue", "pop called!!!"+storage.size());
		val = storage.take();
//		this.notify();// ֪ͨ�����߳�����
//		while(storage.isEmpty()){}
//			val = storage.take();
//			if (val == null)
//				MyLog.e("SyncQueue", "buffer is null");
//			
//			MyLog.e("SyncQueue", "pop count is:" + storage.size());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally{
//			 lock.unlock();
//		}
		return val;
	}
	public void clear(){
		storage.clear();
	}

}

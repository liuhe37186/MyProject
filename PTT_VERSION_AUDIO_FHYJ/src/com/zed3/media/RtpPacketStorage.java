package com.zed3.media;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.zed3.net.RtpPacket;
/**
 * storage for RtpPacket.use for 
 * @author oumogang
 *
 */
public class RtpPacketStorage {
	private boolean isEmpty = true;
	private Lock lock = new ReentrantLock();

	private Condition con4In = lock.newCondition();
	private Condition con4Out = lock.newCondition();

	private Queue<RtpPacket> storage = new LinkedList<RtpPacket>();
	private final String tag = "RtpPacketStorage";

	public RtpPacket get() {
		RtpPacket packet = null;
		try {
			// 获取锁。
			lock.lock();
			isEmpty  = storage.size() == 0;
			/*while*/ if(isEmpty){
				try {
					con4Out.await();
				} catch (InterruptedException e) {
					System.out.println(Thread.currentThread().getName());
					
					Log.i(tag, "get() InterruptedException set flag to exit while");
					isEmpty = false;
				}
			}

//			Log.i(tag, "get() storage.size()"+storage.size() );
			packet = storage.poll();
			
//			isEmpty = true;
//			con4In.signal();

		} finally {
			// 释放锁。
			lock.unlock();
		}
		return packet;
	}
	/**
	 * limitSize  limit the size of the Queue to avoid too longer voice delay。
	 * add by oumogang 2014-04-28
	 */
	public RtpPacket get(int limitSize) {
		RtpPacket packet = null;
		try {
			// 获取锁。
			lock.lock();
			isEmpty  = storage.size() == 0;
			if(isEmpty){
				try {
					con4Out.await();
				} catch (InterruptedException e) {
					System.out.println(Thread.currentThread().getName());
					
					Log.i(tag, "get() InterruptedException set flag to exit while");
					isEmpty = false;
				}
			}
			
//			Log.i(tag, "get() storage.size()"+storage.size() );
			packet = storage.poll();
			if (storage.size()>limitSize) {
				storage.clear();
			}
			
		} finally {
			// 释放锁。
			lock.unlock();
		}
		return packet;
	}

	public void put(RtpPacket packet) {
		try {
			// 获取锁。
			lock.lock();
//			while (!isEmpty){
//				try {
//					con4In.await();
//				} catch (InterruptedException e) {
//					Log.i(tag, "get() InterruptedException set flag to exit while");
//					isEmpty = true;
//				}
//			}
			
			
//			Log.i(tag, "put(packet) storage.size()"+storage.size() );
			if (storage.size()>50) {
				storage.clear();
			}
			storage.offer(packet);
//			isEmpty = false;
			
			con4Out.signal();
		} finally {
			// 释放锁。
			lock.unlock();
		}
	}

	public void clear() {
		// TODO Auto-generated method stub
		lock.lock();
		storage.clear();
		lock.unlock();
		
	}

}

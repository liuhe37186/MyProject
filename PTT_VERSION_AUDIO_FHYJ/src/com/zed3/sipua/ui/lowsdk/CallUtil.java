package com.zed3.sipua.ui.lowsdk;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Looper;

import com.zed3.bluetooth.MyPhoneStateListener;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.location.MemoryMg;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.anta.AntaCallUtil;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;

public class CallUtil {

	private static AlertDialog mAlertDlg;
	public static String mName = "";
	public static String mNumber = "";
	public static long mCallBeginTime = 0 ;
	public static long mCallBeginTime2 = 0;
	public static boolean isDestory = false;
	private static String tag = "CallUtil";
	
	private static Lock lock = new ReentrantLock();

	private Condition con4In = lock.newCondition();
	private Condition con4Out = lock.newCondition();

	public static void makeVideoCall(Context mContext, String numberString,String name) {
		// TODO Auto-generated method stub
		if (!Receiver.mSipdroidEngine.isRegistered()) {
			MyToast.showToast(true, mContext, R.string.notfast_1);
			return;
		}
		if(Build.VERSION.SDK_INT < 16){
			MyToast.showToast(true, mContext, R.string.version_unsupported);
			return;
		}
		if (isInCall()) {
			MyToast.showToast(true, mContext, R.string.vedio_calling_notify);
			return;
		}
		// ���벻��Ϊ��
		if (numberString.length() == 0) {
//			MyToast.showToast(true, mContext, R.string.sipdroid_numnull);
			return;
		}
		// �жϲ��ܺ��б�������
		if (numberString.equals(MemoryMg.getInstance().TerminalNum))// ���pctool���ȡֵ
		{
			MyToast.showToast(true, mContext, R.string.call_notify);
			return;
		}
		UserAgent.ServerCallType = 1;
		call_menu(mContext, numberString,name,true);
	}

	private static void call_menu(Context mContext, String number,String name, boolean flag) {
		//sim���绰�����С�ȥ���л�ͨ���У�voip��ֹȥ�磬�ܽ����磻  add by mou 2014-10-08
		if (checkGsmCallInCall()) {
			MyToast.showToast(true, mContext, R.string.gsm_in_call);
			return;
		}
		
		//���ⷢ�𵥺�ȴ��������������⡣
		AntaCallUtil.reInit();
		
		initNameAndNumber(number,name);
		
		if(!NetChecker.check(mContext, true)){
			DeviceInfo.isEmergency = false;
			return;
		}
		String target = number;
		if (mAlertDlg != null) {
			mAlertDlg.cancel();
		}

		if (flag)
			Receiver.engine(mContext).isMakeVideoCall = 1;
		else
			Receiver.engine(mContext).isMakeVideoCall = 0;

		if (mAlertDlg != null) {
			mAlertDlg.cancel();
		}
		if (target.length() == 0) {
			mAlertDlg = new AlertDialog.Builder(mContext)
					.setMessage(R.string.empty)
					.setTitle(R.string.information).setCancelable(true)
					.show();
			DeviceInfo.isEmergency = false;
			return;
		} else if (!Receiver.engine(mContext).call(target, true)) {
			DeviceInfo.isEmergency = false;
			if (mContext instanceof Activity) {
				mAlertDlg = new AlertDialog.Builder(mContext)
				.setMessage(R.string.notfast_1)
				.setTitle(R.string.information).setCancelable(true)
				.show();
			}else {
				MyToast.showToast(true, mContext, R.string.notfast_1);
			}
			return;
		}
		
	}

	//add by oumogang 2013-05-16
	//��ʼ��ȫ�����ƺͺ���
	public static void initNameAndNumber(String number, String name) {
		// TODO Auto-generated method stub
		//null add by oumogang 2013-07-18
		mName = null;
		mNumber = null;
		
		mNumber = number;
		//modify by oumogang 2013-05-17
		//������Ҫ�ο����б�
//		String contactName = ContactUtil.getUserName(number);
//		if (contactName != null) {
//			mName = contactName;
//		}else if (name.equals(number)) {
//			//������Ҫ�ο����б�
//			String groulistName = GroupListUtil.getUserName(number);
//			if (groulistName != null) {
//				mName = groulistName;
//			}else {
//				mName = number;
//			}
//		}else {
//			mName = name;
//		}
		
		//modify by oumogang 2013-07-18

//		if (name == null || name.equals("")||name.equals(number)) {
//		}
		String contactName = ContactUtil.getUserName(number);
		if (contactName != null) {
			mName = contactName;
		}else {
			String groulistName = GroupListUtil.getUserName(number);
			if (groulistName != null) {
				mName = groulistName;
			}else {
				mName = name;

			}
		}
		
	}

	public static void makeAudioCall(Context mContext, String numberString,String name) {
		// TODO Auto-generated method stub
		//SIM�������ȥ���ͨ��״̬ʱ��VOIP�������롢�������á�
		if (checkGsmCallInCall()) {
			MyToast.showToast(true, mContext, R.string.gsm_in_call);
			return;
		}
		
		// �Ƿ�ע��ɹ�
		if (!Receiver.mSipdroidEngine.isRegistered()) {
			DeviceInfo.isEmergency = false;
			MyToast.showToast(true, mContext, R.string.notfast_1);
			return;
		}

		if (isInCall()) {
			DeviceInfo.isEmergency = false;
			MyToast.showToast(true, mContext, R.string.vedio_calling_notify);
			return;
		}
		// ���벻��Ϊ��
		if (numberString.length() == 0) {
			DeviceInfo.isEmergency = false;
//			MyToast.showToast(true, mContext, R.string.sipdroid_numnull);
			return;
		}
		// �жϲ��ܺ��б�������
		if (numberString.equals(MemoryMg.getInstance().TerminalNum))// ���pctool���ȡֵ
		{
			DeviceInfo.isEmergency = false;
			MyToast.showToast(true, mContext, R.string.call_notify);
			return;
		}
		call_menu(mContext, numberString.trim(),name,false);
	}

	public static boolean checkGsmCallInCall() {
		// TODO Auto-generated method stub
		return MyPhoneStateListener.getInstance().isInCall();
	}

	public static boolean isInCall() {
		// TODO Auto-generated method stub
		return Receiver.call_state != UserAgent.UA_STATE_IDLE;
	}
	
	public static boolean isInCallState(){
		return (Receiver.call_state == UserAgent.UA_STATE_INCALL);
	}
	
	private static final byte[] block4processCall = new byte[0];
	public static void rejectCall() {
		// TODO Auto-generated method stub
		synchronized (block4processCall) {
			StringBuilder builder = new StringBuilder("rejectcall()");
			if (Thread.currentThread().getName().equals("main")) {
				builder.append(" main thread, rejectcall by new thread");
				makeLog(tag, builder.toString());
				new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						// TODO Auto-generated method stub
						Receiver.engine(Receiver.mContext).rejectcall();
						Looper.loop();
					}
				}).start();
			}else {
				builder.append(" not main thread, rejectcall current thread");
				makeLog(tag, builder.toString());
				Receiver.engine(Receiver.mContext).rejectcall();
			}
		}
	}
	public static void answerCall() {
		// TODO Auto-generated method stub
		synchronized (block4processCall) {
			StringBuilder builder = new StringBuilder("answerCall()");
			if (Thread.currentThread().getName().equals("main")) {
				builder.append(" main thread, answerCall by new thread");
				new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						// TODO Auto-generated method stub
						Receiver.engine(Receiver.mContext).answercall();
						Looper.loop();
					}
				}).start();
			}else {
				builder.append(" not main thread, answerCall current thread");
				Receiver.engine(Receiver.mContext).answercall();
			}
			makeLog(tag, builder.toString());
		}
	}
	
	//add by oumogang 2013-11-29
	public static void reInit() {
		// TODO Auto-generated method stub
		lock.lock();
		mName = null;
		mNumber = null;
		lock.unlock();
	}
	public static void  makeLog(String tag,String logMsg) {
		// TODO Auto-generated method stub
		ZMBluetoothManager.getInstance().makeLog(tag, logMsg);
	} 

}

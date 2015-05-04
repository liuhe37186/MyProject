package com.zed3.utils;

import org.zoolu.tools.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.text.TextUtils;

import com.zed3.media.mediaButton.HeadsetPlugReceiver;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.welcome.AutoConfigManager;

public class NetChangedReceiver extends BroadcastReceiver {
	
	//add by hu 2014-5-26
	public static String lastGrpID = "";
	//Add by zzhan 2013-5-10
	private static boolean networkdown = false;
	private static String networkTypeName = "";
	private static String mobileSubTypeName = "";
	//private static State mobileStateOriginal = null;
	//private static State wifiStateOriginal = null;
	private final String TAG = "NetChangedReceiver";
	static {
		// modify by liangzhang 2014-08-22 修改升级黑屏的bug
		ConnectivityManager connMgr = (ConnectivityManager) SipUAApp.mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo(); 
			if (activeInfo != null) {
				networkTypeName = activeInfo.getTypeName();
				if("mobile".equalsIgnoreCase(networkTypeName)){
					mobileSubTypeName = activeInfo.getSubtypeName();
					MyLog.e("zzhan-3-29", "init subTypeName is :" + mobileSubTypeName);
				}
				MyLog.e("zzhan-3-29", "Initialization networkTypeName is :" + networkTypeName);
			}
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		//add by oumogang 2013-07-22
		//need not check modify by mou 2014-12-09
//        if (Receiver.mSipdroidEngine == null) {
//			return;
//		}
        StringBuilder builder = new StringBuilder("NetChangedReceiver#onReceive");
        String action = intent.getAction();
		if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")){
			builder.append(" android.net.conn.CONNECTIVITY_CHANGE");
			//Add by zzhan 2013-5-10
			// modify by liangzhang 2014-08-28 修改升级黑屏的bug
			ConnectivityManager connMgr = (ConnectivityManager) SipUAApp.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
			
			State mobileState = null;
			boolean isMobileAvalilable = false;  
	        boolean isMobileConn = false;
	        boolean isMobile = false; //手机网络
	        NetworkInfo mobileInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	        if (mobileInfo != null) {
		        mobileState = mobileInfo.getState();  
		        isMobileAvalilable = mobileInfo.isAvailable();  
		        isMobileConn = mobileInfo.isConnected();  
		        isMobile = mobileInfo.isConnectedOrConnecting();  
		        MyLog.e("Receiver", "mobile state is : " + mobileState.toString());
	       	}
	        //isWifiAvalilable = true, isWifiConn = true, isWifi = true, when wifi connected but can not go on the web
		    State wifiState = null;
	        boolean isWifiAvalilable = false;  
	        boolean isWifiConn = false;
	        boolean isWifi = false;//wifi网络
	        NetworkInfo wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	        if (wifiInfo != null) {
		        wifiState = wifiInfo.getState();  
		        isWifiAvalilable = wifiInfo.isAvailable();  
		        isWifiConn = wifiInfo.isConnected();  
		        isWifi = wifiInfo.isConnectedOrConnecting();  
		        MyLog.e("Receiver","wifi state is : " + wifiState.toString());  
	        }
	        //active network info  
	        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo(); 
	        builder.append(" NetWorkInfo"+"activeInfo:"+(activeInfo == null? "null":activeInfo.toString()));
	        MyLog.e("zzhan-3-29", "NetWorkInfo"+"activeInfo:"+(activeInfo == null? "null":activeInfo.toString()));
	        if (activeInfo != null) {
	        	MyLog.e("Receiver", "activeInfo is not null.");
		        String typeName = activeInfo.getTypeName();  
		        State activeState = activeInfo.getState();  
		        MyLog.e("Receiver", "active network is : " + typeName);  
		        MyLog.e("Receiver", "active network state is " + activeState.toString()); 
		        if (!networkTypeName.equals(typeName)) {
		        	if (!networkdown) {
		        		LogUtil.makeLog(TAG, Contants.REGISTER_TRACES+"NetChangedReceiver#onReceive() haltNotCloseGps()");
		        		Receiver.engine(context).haltNotCloseGps();
		        		MyLog.e("Receiver", "engine halt.");
		        	}
		        	if(checkloginInfo()){
		        		System.out.println("------NetChangedReceiver-activeInfo!=null-StartEngine-------");
		        		LogUtil.makeLog(TAG, Contants.REGISTER_TRACES+"NetChangedReceiver#onReceive() StartEngine()");
	        			Receiver.engine(context).StartEngine();
	        		}
		        	MyLog.e("Receiver", "engine start.");
		        } else {
		        	if("mobile".equalsIgnoreCase(networkTypeName)){
		        		if(!mobileSubTypeName.equalsIgnoreCase(activeInfo.getSubtypeName())){
		        			if (!networkdown) {
		        				LogUtil.makeLog(TAG, Contants.REGISTER_TRACES+"NetChangedReceiver#onReceive() haltNotCloseGps()");
				        		Receiver.engine(context).haltNotCloseGps();
				        		MyLog.e("Receiver", "engine halt.");
				        		networkdown = true;
				        		MyLog.e("zzhan-3-29", "切换广播 : halt()"+"last subNet:"+mobileSubTypeName+","+"this subNet:"+activeInfo.getSubtypeName());
				        		mobileSubTypeName = activeInfo.getSubtypeName();
				        	}
		        		}
		        	}
		        	if (networkdown){
		        		if("mobile".equalsIgnoreCase(networkTypeName)){
		        			mobileSubTypeName = activeInfo.getSubtypeName();
		        		}
		        		MyLog.e("zzhan-3-29", "切换成功广播: StartEngine()"+"this subNet:"+mobileSubTypeName);
		        		LogUtil.makeLog(TAG, Contants.REGISTER_TRACES+"NetChangedReceiver#onReceive() checkloginInfo()");
		        		if(checkloginInfo()){
		        			System.out.println("------NetChangedReceiver-activeInfo==null-StartEngine-------");
		        			LogUtil.makeLog(TAG, Contants.REGISTER_TRACES+"NetChangedReceiver#onReceive() StartEngine()");
		        			Receiver.engine(context).StartEngine();
		        		}
		        		MyLog.e("Receiver", "engine start.");
		        	}
	        	}
		        networkdown = false;
		        networkTypeName = typeName;
		        //add by hu  发送广播通知activity
		        Intent broadcastIntent = new Intent();
		        broadcastIntent.setAction(Contants.ACTION_NEWWORK_CHANGED);
		        broadcastIntent.putExtra(Contants.NETWORK_STATE, Contants.NETWORK_STATE_GOOD); 
		        context.sendBroadcast(broadcastIntent);
	        } else {
	        	MyLog.e("Receiver", "activeInfo is null.");
	        	if (!networkdown) {
		        	Receiver.engine(context).haltNotCloseGps();
		        	MyLog.e("Receiver", "engine halt.");
		        	 MyLog.e("zzhan-3-29", "切换成功广播:activeInfo = null, halt()");
	        	}
	        	if(Receiver.GetCurUA() != null && Receiver.GetCurUA().GetCurGrp() != null){
	        		NetChangedReceiver.lastGrpID = Receiver.GetCurUA().GetCurGrp().grpID;
	        	}
	        	networkdown = true;
	        	//add by hu  发送广播通知activity
	        	Intent broadcastIntent = new Intent();
		        broadcastIntent.setAction(Contants.ACTION_NEWWORK_CHANGED);
		        broadcastIntent.putExtra(Contants.NETWORK_STATE, Contants.NETWORK_STATE_BAD);
		        context.sendBroadcast(broadcastIntent);
	        }
		}else if (action.equals(Intent.ACTION_SCREEN_ON)) {
			builder.append(" ACTION_SCREEN_ON");
			HeadsetPlugReceiver.onScreamStateChanged(true);
		}else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
			builder.append(" ACTION_SCREEN_OFF");
			HeadsetPlugReceiver.onScreamStateChanged(false);
		}
		LogUtil.makeLog(TAG, builder.toString());
		
	}
	boolean checkloginInfo(){
		AutoConfigManager acm;
		acm = new AutoConfigManager(SipUAApp.mContext);
		return !TextUtils.isEmpty(acm.fetchLocalServer()) &&  !TextUtils.isEmpty(acm.fetchLocalPwd()) && !TextUtils.isEmpty(acm.fetchLocalUserName());
	}
	
	private static NetChangedReceiver sReceiver;
	
	public synchronized static void registerSelf(){
		
		if(sReceiver==null){
			// --------------------------
			IntentFilter infilter = new IntentFilter();
			infilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
			infilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			//亮屏 关屏操作
			infilter.addAction(Intent.ACTION_SCREEN_ON);
			infilter.addAction(Intent.ACTION_SCREEN_OFF);
			
			NetChangedReceiver receiver = new NetChangedReceiver();
			SipUAApp.getAppContext().registerReceiver(receiver, infilter);
			sReceiver = receiver;
		}
		
	}
	
	public synchronized static void unregisterSelf() {
		if(sReceiver!=null){
			try {
				SipUAApp.getAppContext().unregisterReceiver(sReceiver);
			}catch(Exception e){
			}
			sReceiver = null;
		}
	}
	
}

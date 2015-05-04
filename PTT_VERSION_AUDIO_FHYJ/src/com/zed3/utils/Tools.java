package com.zed3.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.zoolu.tools.GroupListInfo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.zed3.flow.FlowRefreshService;
import com.zed3.media.mediaButton.HeadsetPlugReceiver;
import com.zed3.power.MyPowerManager;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.message.AlarmService;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.MyHeartBeatReceiver;
import com.zed3.sipua.ui.OneShotAlarm;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.RegisterService;
import com.zed3.sipua.ui.lowsdk.ContactManager;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;

public class Tools {
	public static boolean isInBg = true;
	
	// add by hu
	
		/**
		 * @param length 输入字符串的长度
		 * @return 返回length长度的字母和数字的混合
		 * 
		 * */
		
		public static String getRandomCharNum(int length) {
			String val = "";
			Random random = new Random();
			for (int i = 0; i < length; i++) {
				String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字

				if ("char".equalsIgnoreCase(charOrNum)) // 字符串
				{
					int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; // 取得大写字母还是小写字母
					val += (char) (choice + random.nextInt(26));
				} else if ("num".equalsIgnoreCase(charOrNum)) // 数字
				{
					val += String.valueOf(random.nextInt(10));
				}
			}

			return val;
		}
	// add by hu
	public static boolean isRunBackGroud(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;

	}

	//add by hu 2013-10-8
	public static String getVersionName(Context ctx){
		PackageInfo packageInfo = null;
		try {
			packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (packageInfo == null?"":packageInfo.versionName);
	}
	public static void FlowAlertDialog(Context context)
	{
		final AlertDialog dlg = new AlertDialog.Builder(context).create();
		dlg.show();
		Window window = dlg.getWindow();
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(R.layout.flowalertalarm);
		ImageView close = (ImageView) window.findViewById(R.id.flowtitle);
		close.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				dlg.cancel();
			}
		});
	}

	
	
	public static double calculateTotal(double db) {
		double x = db / 1024d / 1024d;
		x = (double) (Math.round(x * 100) / 100.0);
		return x;
	}
	public static double calculatePercent(double a, double b) {
		double x = a / b;
		x = (double) (Math.round(x * 100) / 100.0);
		return x;
	}
	
	/**
	 * release something on logout . add by mou 2014-11-02
	 * @param context
	 */
	public static void onPreLogOut() {
		NetChangedReceiver.unregisterSelf();
		HeadsetPlugReceiver.stopReceive(SipUAApp.getAppContext());
		MyPowerManager.getInstance().exit(SipUAApp.getAppContext());
	}
	/**
	 * init something on register success . add by mou 2014-11-02
	 * @param context
	 */
	public static void onRegisterSuccess() {
		NetChangedReceiver.registerSelf();
		HeadsetPlugReceiver.startReceive(SipUAApp.getAppContext());
		MyPowerManager.getInstance().init(SipUAApp.getAppContext());
	}
	public static void exitApp(Context context) {
		//exit , add by oumogang 2014-03-06
		if(MainActivity.getInstance() != null){
			MainActivity.getInstance().finish();
		}
		cleanGrpID();
		SipUAApp.exit();
		Intent it = new Intent(context, FlowRefreshService.class);
		context.stopService(it);
		Intent it_ = new Intent(context, AlarmService.class);
		context.stopService(it_);
		Receiver.engine(context).expire(-1);
		Receiver.onText(Receiver.MISSED_CALL_NOTIFICATION, null, 0, 0);
		// 清空保存的通知栏数据，解决应用退出后，切换系统语言时，出现通知栏更新的问题。 add by lwang 2014-11-13
		SharedPreferences sharedPreferences = context.getSharedPreferences("notifyInfo",  Context.MODE_PRIVATE);
		sharedPreferences.edit().clear().commit();
		// 延迟
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// deleted by hdf 停止
		Receiver.engine(context).halt();
		// 停止服务
		context.stopService(new Intent(context, RegisterService.class));
		// 取消全局定时器
		Receiver.alarm(0, OneShotAlarm.class);
		Receiver.alarm(0, MyHeartBeatReceiver.class);
		// 跳转手机桌面
//		Intent intent_ = new Intent(Intent.ACTION_MAIN);
//		intent_.addCategory(Intent.CATEGORY_HOME);
//		intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(intent_);
		//
		System.exit(0);
	}
	public static void exitApp2(Context context) {
		//exit , add by oumogang 2014-03-06
		if(MainActivity.getInstance() != null){
			MainActivity.getInstance().finish();
		}
		SipUAApp.exit();
		Intent it = new Intent(context, FlowRefreshService.class);
		context.stopService(it);
		Intent it_ = new Intent(context, AlarmService.class);
		context.stopService(it_);
		Receiver.engine(context).expire(-1);
		Receiver.onText(Receiver.MISSED_CALL_NOTIFICATION, null, 0, 0);
		// 延迟
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// deleted by hdf 停止
		Receiver.engine(context).halt();
		// 停止服务
		context.stopService(new Intent(context, RegisterService.class));
		// 取消全局定时器
		Receiver.alarm(0, OneShotAlarm.class);
		Receiver.alarm(0, MyHeartBeatReceiver.class);
		// 跳转手机桌面
//		Intent intent_ = new Intent(Intent.ACTION_MAIN);
//		intent_.addCategory(Intent.CATEGORY_HOME);
//		intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(intent_);
		//
		System.exit(0);
	}
	/**
	 * 正常退出时清除保存的对讲组信息
	 */
	public static void cleanGrpID(){
		Editor edit = PreferenceManager
				.getDefaultSharedPreferences(SipUAApp.getAppContext())
				.edit();
		edit.remove("grpID");
		boolean isCommit = edit.commit();
		System.out.println("----- is commit = " + isCommit);
	}
	/**
	 * 将当前对讲组信息保存到文件当中
	 */
	public static void saveGrpID(String grpID){
		System.out.println("----- saveGrpID = " + grpID);
		Editor it = PreferenceManager
				.getDefaultSharedPreferences(SipUAApp.getAppContext())
				.edit();
		it.putString("grpID", grpID);
		it.commit();
	}
	
	
	
	public static boolean isConnect(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					// 判断当前网络是否已经连接
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("error", e.toString());
		}
		return false;
	}

	public static void bringtoFront(Context ctx) {
		ActivityManager manager = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> task_info = manager.getRunningTasks(20);

		String className = "";

		for (int i = 0; i < task_info.size(); i++) {
			if ("com.zed3.sipua".equals(task_info.get(i).topActivity
					.getPackageName())) {
				className = task_info.get(i).topActivity.getClassName();
				Intent intent = new Intent();
				// 这里是指从后台返回到前台 前两个的是关键
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.addCategory(Intent.ACTION_DEFAULT);
				ComponentName componentName = new ComponentName(
						task_info.get(i).topActivity.getPackageName(),
						className);// 这个没问题
				intent.setComponent(componentName);//
				// intent.setClass(context,
				// Class.forName(className));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_SINGLE_TOP 
						| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				ctx.startActivity(intent);
				break;
			}
			// 如果找不到 会执行一下代码
			if (i >= task_info.size() - 1) {
				Intent inn = new Intent(ctx, MainActivity.class);
//				inn.addCategory(Intent.CATEGORY_LAUNCHER);
//				inn.addCategory(Intent.ACTION_DEFAULT);
				inn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				ctx.startActivity(inn);
				break;
			}
		}

	}
	
	//add by hu2014/2/22
	public static String queryNamebyNum(Context ctx,String num){
		//from grouplist
		HashMap<PttGrp, ArrayList<GroupListInfo>> mGroupListsMap = GroupListUtil.getGroupListsMap();
		PttGrp grp = Receiver.GetCurUA().GetCurGrp();
		if(grp != null){
			ArrayList<GroupListInfo> arrayList = mGroupListsMap.get(grp);
			if(arrayList != null && arrayList.size()>0){
				for(GroupListInfo info:arrayList){
					if(info.GrpNum != null && info.GrpNum.equals(num)){
						return info.GrpName;
					}
				}
			}
		}
		//from local db
		ContactManager cm = new ContactManager(ctx);
		String name = cm.queryNameByNum(num);
		return TextUtils.isEmpty(name)?num:name;
	}
	//add by hu 2014/2/22
	public static byte[] shortArray2ByteArray(short shorts[]) {
		// to turn shorts back to bytes.
		 byte[] bytes = new byte[shorts.length * 2];
		 ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
		 return bytes;
	    }
	public static int[] getWidthHeight(Activity ctx){
		int[] res = new int[2];
		DisplayMetrics dm = new DisplayMetrics();
		//获取屏幕信息
		ctx.getWindowManager().getDefaultDisplay().getMetrics(dm);

		int screenWidth = dm.widthPixels;

		int screenHeigh = dm.heightPixels;
		res[0] =screenWidth;
		res[1] =screenHeigh;
		return res;
	}
	public static double getWidthHeightRate(Activity ctx){
		int[] res = getWidthHeight(ctx);
		return (res[0]*1.0)/(res[1]*1.0);
	}
	public static String getCurrentTag(Context ctx){
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences mypre = ctx.getSharedPreferences(sharedPrefsFile,
				Activity.MODE_PRIVATE);
		boolean isFront = mypre.getString("usevideokey", "0").equals("1");
		String dd="";
		if(isFront){
			if(mypre != null){
				dd = mypre.getString("videoresolutionkey", "3");
			}
		}else{
			if(mypre != null){
				dd = mypre.getString("videoresolutionkey0", "3");
			}
		}
		return getTag(dd);
	}
	private static String getTag(String dd) {
		String tag ="";
		if("2".equals(dd)){
			tag = "720p";
		}else if("3".equals(dd)){
			tag ="d1";
		}else if("4".equals(dd)){
			tag ="cif";
		}else if("5".equals(dd)){
			tag ="qvga";
		}else if("6".equals(dd)){
			tag ="vga";
		}else if("7".equals(dd)){
			tag ="qcif";
		}
		return tag;
	}
	public static  void writeFileToSD(String str) {
		if(TextUtils.isEmpty(str))return;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		str = formatter.format(curDate) + ": " + str;
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			Log.d("TestFile", "SD card is not avaiable/writeable right now.");
			return;
		}
    	try {
    		String pathName=Environment.getExternalStorageDirectory().getPath()+"/videoTest/";
    		String fileName=Build.MODEL+".txt";
    		File path = new File(pathName);
    		File file = new File(pathName + fileName);
    		if( !path.exists()) {
    			Log.d("TestFile", "Create the path:" + pathName);
    			path.mkdir();
    		}
    		if( !file.exists()) {
    			Log.d("TestFile", "Create the file:" + fileName);
    			file.createNewFile();
    		}
    		FileOutputStream stream = new FileOutputStream(file,true);
    		byte[] buf = str.getBytes();
    		stream.write(buf);    		
    		stream.close();
    		
    	} catch(Exception e) {
    		Log.e("TestFile", "Error on writeFilToSD.");
    		e.printStackTrace();
    	}
    }
	public static boolean matchDevice(String build) {
		// TODO Auto-generated method stub
		return Build.MODEL.contains(build);
	}
}

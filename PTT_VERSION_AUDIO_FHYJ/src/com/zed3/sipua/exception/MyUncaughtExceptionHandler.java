package com.zed3.sipua.exception;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;

import com.zed3.sipua.SipUAApp;
import com.zed3.utils.Tools;

public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private MyUncaughtExceptionHandler() {
	};

	private static UncaughtExceptionHandler mDefaultHandler;
	// ����һ��ϵͳ��Ĭ�ϵ��쳣�����handler
	private static MyUncaughtExceptionHandler mMyHandler;
	private static Context mContext;

	public synchronized static MyUncaughtExceptionHandler getInstance(
			Context context) {
		if (mMyHandler == null) {
			mMyHandler = new MyUncaughtExceptionHandler();
			mContext = context;
			mDefaultHandler = Thread.currentThread()
					.getDefaultUncaughtExceptionHandler();
		}
		return mMyHandler;
	}

	public static UncaughtExceptionHandler getDefault(){
		return mDefaultHandler;
	}
	
	/**
	 * ��ĳһ���쳣 û�д�����ʾ�Ĳ����ʱ��, ϵͳ����� Ĭ�ϵ��쳣����Ĵ��� ��������쳣
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (SipUAApp.mContext!=null) {
		}else {
		}
		
		System.out.println("-----uncau excetpion");
//		if (SipUAApp.mContext != null) {
//			MyToast.showToast(true, SipUAApp.mContext, SipUAApp.mContext.getResources().getString(R.string.exception_occur)+ex.getMessage());
//			//System.out.println("�������쳣,���Ǳ��粶����,60"+ex.getMessage());
//		}
		//System.out.println("�������쳣,���Ǳ��粶����,62"+ex.getMessage());
		
		ex.printStackTrace();
		saveExceptionLog(ex);
		//use Tools.exitApp() to exit all . modify by oumogang 2014-03-13
//		SipUAApp.exit();//add by oumogang 2013-09-11
		Tools.exitApp(SipUAApp.mContext);
		android.os.Process.killProcess(android.os.Process.myPid());

			// ����ϵͳ��Ĭ�ϵ��쳣������ ��������쳣
//			mDefaultHandler.uncaughtException(thread, ex);

	}

	public static void saveExceptionLog(Throwable ex) {
		// TODO Auto-generated method stub
		StringWriter wr = new StringWriter();
		PrintWriter pw = new PrintWriter(wr);
		ex.printStackTrace(pw);
		StringBuilder sb = new StringBuilder();

			PackageInfo packinfo;
			try {
				packinfo = SipUAApp.mContext.getPackageManager().getPackageInfo(
						 SipUAApp.mContext.getPackageName(), 0);
				String version = packinfo.versionName;
				sb.append("ExceptionMessages:\n");
				sb.append("VersionName:" + version + "\n");

				String errorlog = wr.toString();
				sb.append(errorlog);
				sb.append("\n");

				// ��ȡ��ǰ�ֻ�����ϵͳ����Ϣ.
				Field[] fields = Build.class.getDeclaredFields();
				for (Field field : fields) {
					field.setAccessible(true);// ��������,���Ի�ȡ˽�г�Ա��������Ϣ
					String name = field.getName();
					String value = field.get(null).toString();
					sb.append(name + "=" + value + "\n");
				}
				
				String time = "time:" + getTimeString();
				sb.append(time+"\n");
				sb.append("===============================\n");
				String log = sb.toString();
				
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					String dirName = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + File.separator + "com.zed3.sipua";
					File dir = new File(dirName);
					if (!dir.exists())
						dir.mkdir();
					
//					File file = new File(Environment.getExternalStorageDirectory(),
//						"com.zed3.sipua" +
//						".log");
					File file = new File(dir, "exceptions.txt");
					
					//add by oumogang 2013-09-11
					if (!file.exists()){
						file.createNewFile();
					}
					FileWriter fileWriter = new FileWriter(file, true);
					fileWriter.write(log);
//				FileOutputStream fos = new FileOutputStream(file);
//				fos.write(log.getBytes());
//				fos.flush();
//				fos.close();
					fileWriter.flush();
					fileWriter.close();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			android.os.Process.killProcess(android.os.Process.myPid());
	}

	private static String getTimeString() {
		// TODO Auto-generated method stub
		SimpleDateFormat formatter = new SimpleDateFormat(
					" yyyy-MM-dd HH:mm:ss ");
		long systemTime = System.currentTimeMillis();
		Date curDate = new Date(systemTime);// ��ȡ��ǰʱ
		return formatter.format(curDate);
	}

}

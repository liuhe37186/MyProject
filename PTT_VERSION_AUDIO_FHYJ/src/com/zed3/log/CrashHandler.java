package com.zed3.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

/**
 * MyLog搜集器
 * 
 * @author user
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public final static String TAG = "CrashHandler";
	private Thread.UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类

	public static CrashHandler MyCrash = null;
	public static RandomAccessFile raf = null;
	static SimpleDateFormat formatx = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");// 用于格式化日期,作为日志文件名的一部分
	private static String str = "";
	private Context mContext = null;
	private static ArrayList<String> fileNameList = null;
	
	private static boolean logLock=false;

	public synchronized static CrashHandler getInstance() {
		if (MyCrash == null)
			MyCrash = new CrashHandler();

		return MyCrash;
	}

	// 在sdcard生成log日志 仅执行一次
	public void init(Context context,boolean flag) {
		Log.e(TAG, "init");
		mContext = context;
		logLock = flag;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
		Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
		
		if(logLock)
			Log.e(TAG, "chk true");
		else
			Log.e(TAG, "chk false");
	}

	private static void InitFile() {
		// raf为null才会创建
		if (raf == null) {
			try {
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd-HH-mm-ss");// 用于格式化日期,作为日志文件名的一部分
				String time = format.format(new Date());
				String fileName = "zed3-" + time + ".log";
				String dirName = "", delName = "";
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					dirName = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + File.separator + "zed3";
					File dir = new File(dirName);
					if (!dir.exists())
						dir.mkdir();

					// 将文件名存到集合里面去
					if (fileNameList == null){
						fileNameList = new ArrayList<String>();
						Log.e(TAG, "new filenamelist");
					}
					fileNameList.add(fileName);
					
					Log.e(TAG, fileNameList.size()+" file count");
					
					if (fileNameList.size() >= 5) {//5
						delName = fileNameList.get(0);// 文件名
						File delFile = new File(dirName + File.separator
								+ delName);
						
						Log.e(TAG, dirName + File.separator + delName);
						
						if (delFile.exists()) {
							if (delFile.delete())// del success
								fileNameList.remove(0);
							else {
								Log.e("CrashHandler", "delete file " + delName
										+ " failed");
							}
						}
					}
					
					// 再判断集合里有几个文件名，然后delete掉 要new一个File类
					File file = new File(dir, fileName);
					raf = new RandomAccessFile(file, "rw");

				}

			} catch (Exception ex) {
				ex.printStackTrace();
				Log.v("System.out", ex.toString());
			}
		}
	}

	// 往log里面写日志
	public synchronized static void SaveLog(String tag, String log) {
		
		if (!logLock)
			return;
		
		try {
			InitFile(); 
			//if (raf != null)  add by oumogang 2013-07-03
			if (raf != null) {
				str = formatx.format(new Date()) + " " + tag + "  " + log;
				raf.writeUTF(str + "\r\n");
				
				if (raf.length() >  3*1024 * 1024)// 如果log超过3M过大 则创建另一个文件
				{    // 结束
					if (raf != null)
						try {
							raf.close();
							raf = null;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					// 再重新创建一个文件
					InitFile();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 结束log抓去工作
	public static void EndLog() {
		
		logLock = false;
		
		if (fileNameList != null) {
			fileNameList.clear();
			fileNameList = null;
			Log.e(TAG, "clear filenamelist");
		}
		
		if (raf != null)
			try {
				raf.close();
				raf = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * 当UncaughtException发生时会转入该重写的方法来处理
	 */
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		if (!handleException(ex) && mDefaultHandler != null) {
			Log.e(TAG,  "uncaughtexception");
			// 如果自定义的没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			Log.e(TAG, "killProcess");
			
			//开关自动关闭
//			SharedPreferences mypre = mContext.getSharedPreferences("com.zed3.sipua_preferences", Activity.MODE_PRIVATE);
//			SharedPreferences.Editor editor = mypre.edit();
//			editor.putBoolean("logOnOffKey", false);
//			editor.commit();
			
			// 退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
			
		}

	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 *            异常信息
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	public boolean handleException(Throwable ex) {
		if (ex == null)
			return false;
		new Thread() {
			public void run() {
				Looper.prepare();
				Looper.loop();
			}
		}.start();

		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		Throwable cause = ex.getCause();
		// 循环着把所有的异常信息写入writer中
		while (cause != null) {
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		pw.close();// 记得关闭

		// 保存日志文件
		SaveLog("system.err", writer.toString());
		return true;
	}
}

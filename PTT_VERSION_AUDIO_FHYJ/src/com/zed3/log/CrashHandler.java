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
 * MyLog�Ѽ���
 * 
 * @author user
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public final static String TAG = "CrashHandler";
	private Thread.UncaughtExceptionHandler mDefaultHandler;// ϵͳĬ�ϵ�UncaughtException������

	public static CrashHandler MyCrash = null;
	public static RandomAccessFile raf = null;
	static SimpleDateFormat formatx = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");// ���ڸ�ʽ������,��Ϊ��־�ļ�����һ����
	private static String str = "";
	private Context mContext = null;
	private static ArrayList<String> fileNameList = null;
	
	private static boolean logLock=false;

	public synchronized static CrashHandler getInstance() {
		if (MyCrash == null)
			MyCrash = new CrashHandler();

		return MyCrash;
	}

	// ��sdcard����log��־ ��ִ��һ��
	public void init(Context context,boolean flag) {
		Log.e(TAG, "init");
		mContext = context;
		logLock = flag;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// ��ȡϵͳĬ�ϵ�UncaughtException������
		Thread.setDefaultUncaughtExceptionHandler(this);// ���ø�CrashHandlerΪ�����Ĭ�ϴ�����
		
		if(logLock)
			Log.e(TAG, "chk true");
		else
			Log.e(TAG, "chk false");
	}

	private static void InitFile() {
		// rafΪnull�Żᴴ��
		if (raf == null) {
			try {
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd-HH-mm-ss");// ���ڸ�ʽ������,��Ϊ��־�ļ�����һ����
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

					// ���ļ����浽��������ȥ
					if (fileNameList == null){
						fileNameList = new ArrayList<String>();
						Log.e(TAG, "new filenamelist");
					}
					fileNameList.add(fileName);
					
					Log.e(TAG, fileNameList.size()+" file count");
					
					if (fileNameList.size() >= 5) {//5
						delName = fileNameList.get(0);// �ļ���
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
					
					// ���жϼ������м����ļ�����Ȼ��delete�� Ҫnewһ��File��
					File file = new File(dir, fileName);
					raf = new RandomAccessFile(file, "rw");

				}

			} catch (Exception ex) {
				ex.printStackTrace();
				Log.v("System.out", ex.toString());
			}
		}
	}

	// ��log����д��־
	public synchronized static void SaveLog(String tag, String log) {
		
		if (!logLock)
			return;
		
		try {
			InitFile(); 
			//if (raf != null)  add by oumogang 2013-07-03
			if (raf != null) {
				str = formatx.format(new Date()) + " " + tag + "  " + log;
				raf.writeUTF(str + "\r\n");
				
				if (raf.length() >  3*1024 * 1024)// ���log����3M���� �򴴽���һ���ļ�
				{    // ����
					if (raf != null)
						try {
							raf.close();
							raf = null;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					// �����´���һ���ļ�
					InitFile();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ����logץȥ����
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
	 * ��UncaughtException����ʱ��ת�����д�ķ���������
	 */
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		if (!handleException(ex) && mDefaultHandler != null) {
			Log.e(TAG,  "uncaughtexception");
			// ����Զ����û�д�������ϵͳĬ�ϵ��쳣������������
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			Log.e(TAG, "killProcess");
			
			//�����Զ��ر�
//			SharedPreferences mypre = mContext.getSharedPreferences("com.zed3.sipua_preferences", Activity.MODE_PRIVATE);
//			SharedPreferences.Editor editor = mypre.edit();
//			editor.putBoolean("logOnOffKey", false);
//			editor.commit();
			
			// �˳�����
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
			
		}

	}

	/**
	 * �Զ��������,�ռ�������Ϣ ���ʹ��󱨸�Ȳ������ڴ����.
	 * 
	 * @param ex
	 *            �쳣��Ϣ
	 * @return true:��������˸��쳣��Ϣ;���򷵻�false.
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
		// ѭ���Ű����е��쳣��Ϣд��writer��
		while (cause != null) {
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		pw.close();// �ǵùر�

		// ������־�ļ�
		SaveLog("system.err", writer.toString());
		return true;
	}
}

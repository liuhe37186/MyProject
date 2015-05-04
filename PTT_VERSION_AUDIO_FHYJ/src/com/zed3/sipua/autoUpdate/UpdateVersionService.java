package com.zed3.sipua.autoUpdate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.zoolu.tools.MyLog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.zed3.flow.FlowStatistics;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

public class UpdateVersionService {
	private static String verUrl = "";
	private static String updateHost = "";
	private static String ptt_Name = "";
	private static String versionName = "";
	private static final String TAG = "updateService";
	private static final int DOWN = 1;// 用于区分正在下载
	private static final int DOWN_FINISH = 0;// 用于区分下载完成
	protected static final int SDCARDNOTEXSIT = 4;
	private String fileSavePath;// 下载新apk的厨房地点
	private int progress, saveNum = 0;// 获取新apk的下载数据量,更新下载滚动条
	private boolean cancelUpdate = false;// 是否取消下载
	private Context context;
	private ProgressBar progressBar;
	private Dialog downLoadDialog;
	private Handler handler = new Handler() {// 更新UI和toast提醒

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch ((Integer) msg.obj) {
			case DOWN:
				progressBar.setProgress(progress);
				break;
			case DOWN_FINISH:
				installAPK();
				break;
			case SDCARDFULL:
				if (downLoadDialog != null) {
					downLoadDialog.dismiss();
					downLoadDialog = null;
				}
				MyToast.showToast(true, SipUAApp.mContext, SipUAApp.mContext
						.getResources().getString(R.string.sd_check));
				break;
			case SDCARDNOTEXSIT:
				MyToast.showToast(true, SipUAApp.mContext, SipUAApp.mContext
						.getResources().getString(R.string.sd_check_2));
				break;
			default:
				break;
			}
		}

	};

	/**
	 * 构造方法
	 * 
	 * @param updateVersionXMLPath
	 *            比较版本的xml文件地址(服务器上的)
	 * @param context
	 *            上下文
	 */
	public UpdateVersionService(Context context, String serverIp) {
		super();
		this.context = context;
		verUrl = getServerUrl(serverIp);
		updateHost = verUrl.substring(0, verUrl.lastIndexOf("/") + 1);
		MyLog.i(TAG, "updateServer=" + verUrl);
	}

	/**
	 * @param setting中设置的服务器地址
	 * @return 真正访问版本升级的地址
	 * 
	 * */
	private String getServerUrl(String serverIp) {
		// http://192.168.100.59/pttUpgrade/android/pttLatest.ver
		// 自动登陆版本升级时使用自动登陆IP add by liangzhang 2014-10-23
		if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
			// autoconfigurl=http://218.249.39.212:8000/ptt_http.php
			// 截取autoconfigurl中的ip
			int start = DeviceInfo.CONFIG_CONFIG_URL.indexOf("//");
			int end =  DeviceInfo.CONFIG_CONFIG_URL.lastIndexOf(":");
			serverIp = DeviceInfo.CONFIG_CONFIG_URL.substring(start + 2, end);
			MyLog.e(TAG, "--autologin serverIp>>" + serverIp);
		}
		return DeviceInfo.CONFIG_UPDATE_URL
				.replace("updateServiceIP", serverIp);
	}

	/**
	 * 检测是否可更新
	 * 
	 * @return
	 */
	public void checkUpdate(boolean flag) {
		if (isUpdate()) {
			showUpdateVersionDialog();// 显示提示对话框
		} else {
			if (flag) {
				MyToast.showToast(true, context, context.getResources()
						.getString(R.string.setting_update_no));
			}
		}
	}

	/**
	 * 更新提示框
	 */
	private void showUpdateVersionDialog() {
		// 构造对话框
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(R.string.setting_update_title);
		builder.setMessage(context.getResources().getString(
				R.string.setting_update_1)
				+ " "+versionName+" "
				+ context.getResources().getString(R.string.setting_update_2));
		// 更新
		builder.setPositiveButton(context.getResources().getString(R.string.update), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// 显示下载对话框
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED))
					showDownloadDialog();
				else {
					// MyToast.showToast(true, Receiver.mContext,
					// "请检查SD卡是否存在并且有读写权限!");
					Message message2 = new Message();
					message2.obj = SDCARDNOTEXSIT;
					handler.sendMessage(message2);
				}
			}
		});
		// 稍后更新
		builder.setNegativeButton(context.getResources().getString(R.string.no_update), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SipUAApp.updateNextTime = true;
				dialog.dismiss();
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * 下载的提示框
	 */
	protected void showDownloadDialog() {
		{
			// 构造软件下载对话框
			AlertDialog.Builder builder = new Builder(context);
			builder.setTitle(R.string.updating);
			// 给下载对话框增加进度条
			final LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.downloaddialog, null);
			progressBar = (ProgressBar) v.findViewById(R.id.updateProgress);
			builder.setView(v);
			// 取消更新
			builder.setNegativeButton(context.getResources().getString(R.string.cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					// 设置取消状态
					cancelUpdate = true;
				}
			});
			downLoadDialog = builder.create();
			downLoadDialog.setCanceledOnTouchOutside(false);
			downLoadDialog.show();
			// 下载文件
			downloadApk();
		}

	}

	/**
	 * 下载apk,不能占用主线程.所以另开的线程
	 */
	private void downloadApk() {
		new downloadApkThread().start();

	}

	/**
	 * 判断是否可更新
	 * 
	 * @return
	 */
	private boolean isUpdate() {
		int versionCode = getVersionCode(context);
		MyLog.i(TAG, "local versionCode =" + versionCode);
		String version = "";
		try {
			// 把version.xml放到网络上，然后获取文件信息
			URL url = new URL(verUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(5 * 1000);
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");// 必须要大写
			if (conn.getResponseCode() == 200) {
				InputStream inputStream = conn.getInputStream();
				String result = inputStream2String(inputStream);
				if (!TextUtils.isEmpty(result)) {
					String[] str = result.split(",");
					if (str.length == 2) {
						version = str[0];
						versionName = version;
						ptt_Name = str[1].trim();
					}
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
			MyLog.e("UpdateVersionService", "fail to connect "+verUrl);
			MyToast.showToast(false, context, context.getString(R.string.check_notify));// 不显示
		}
		if (!TextUtils.isEmpty(version)) {
			String[] versionArray = version.split("\\.");
			if (versionArray.length == 4) {
				String versionStr = formatNum(versionArray[0], 2)
						+ formatNum(versionArray[1], 2)
						+ formatNum(versionArray[2], 2)
						+ formatNum(versionArray[3], 4);
				if (!TextUtils.isEmpty(versionStr)) {
					int serverCode = Integer.valueOf(versionStr);
					MyLog.i(TAG, "server versionCode =" + serverCode);
					if (serverCode > versionCode) {
						return true;
					}
				}
			}
			// String versionStr = version.replace(".", "");

		}
		return false;

	}

	String formatNum(String str, int length) {
		if (length == 2) {
			if (str.length() == 1) {
				return "0" + str;
			}
		} else if (length == 4) {
			if (str.length() < 4) {
				if (str.length() == 1) {
					str = "000" + str;
				} else if (str.length() == 2) {
					str = "00" + str;
				} else if (str.length() == 3) {
					str = "0" + str;
				}
			}
		}
		return str;
	}

	/**
	 * 获取当前版本和服务器版本.如果服务器版本高于本地安装的版本.就更新
	 * 
	 * @param context2
	 * @return
	 */
	private int getVersionCode(Context context2) {
		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo(
					"com.zed3.sipua", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;

	}

	/**
	 * 安装apk文件
	 */
	private void installAPK() {
		File apkfile = new File(fileSavePath, ptt_Name);
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		System.out.println("filepath=" + apkfile.toString() + "  "
				+ apkfile.getPath());

		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		context.startActivity(i);
		if (apkfile.length() > 0) {
			Tools.exitApp2(context);
		}

	}

	/**
	 * 卸载应用程序(没有用到)
	 */
	public void uninstallAPK() {
		Uri packageURI = Uri.parse("package:com.example.updateversion");
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		context.startActivity(uninstallIntent);

	}

	/**
	 * 下载apk的方法
	 * 
	 * @author rongsheng
	 * 
	 */
	public class downloadApkThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					fileSavePath = sdpath + "download";
					URL url = new URL(updateHost + ptt_Name);
					MyLog.i(TAG, "download apk url =" + url);
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setReadTimeout(5 * 1000);// 设置超时时间
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Charser",
							"GBK,utf-8;q=0.7,*;q=0.3");
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(fileSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(fileSavePath, ptt_Name);
					if (apkFile.exists() && apkFile.length() == length) {
						if (downLoadDialog != null) {
							downLoadDialog.dismiss();
							downLoadDialog = null;
						}
						Message message2 = new Message();
						message2.obj = DOWN_FINISH;
						handler.sendMessage(message2);
					} else {
						if (readSDCard() < 30) {// 如果sd卡小于30M的空间，则提示用户不下载
							cancelUpdate = true;
							Message message = new Message();
							message.obj = SDCARDFULL;
							handler.sendMessage(message);
							return;
						}
						FileOutputStream fos = new FileOutputStream(apkFile);
						int count = 0;
						// 缓存
						byte buf[] = new byte[1024];
						// 写入到文件中
						do {
							int numread = is.read(buf);
							count += numread;
							FlowStatistics.DownLoad_APK += numread;
							// 计算进度条位置
							if (length != 0)
								progress = (int) (((float) count / length) * 100);

							// 更新进度
							Message message = new Message();
							message.obj = DOWN;
							handler.sendMessage(message);
							if (numread <= 0) {
								// 下载完成
								// 取消下载对话框显示
								if (downLoadDialog != null) {
									downLoadDialog.dismiss();
									downLoadDialog = null;
								}
								Message message2 = new Message();
								message2.obj = DOWN_FINISH;
								// handler.sendMessage(message2);
								handler.sendMessageDelayed(message2, 2000);
								break;
							}
							// 写入文件
							fos.write(buf, 0, numread);
							saveNum = numread;// 记录最后一次的下载值

						} while (!cancelUpdate);// 点击取消就停止下载.

						// 最后一次的流量

						MyLog.e("UpdateVersionService",
								"download apk count == " + count + " "
										+ FlowStatistics.DownLoad_APK);
						fos.close();
					}
					is.close();
				} else {
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private final static int SDCARDFULL = 3;

	private String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		return baos.toString();
	}

	long readSDCard() { // M
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long blockSize = sf.getBlockSize();
			// long blockCount = sf.getBlockCount();
			long availCount = sf.getAvailableBlocks();
			MyLog.e("UpdateVersionService", "sdcard  left" + availCount
					* blockSize / 1024 / 1024 + "m");
			return availCount * blockSize / 1024 / 1024;
		}
		return 0;
	}

	void readSystem() {
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		// long blockSize = sf.getBlockSize();
		// long blockCount = sf.getBlockCount();
		// long availCount = sf.getAvailableBlocks();
	}

}

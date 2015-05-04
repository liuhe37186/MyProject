/****************************
 * houyuchun create 20120426
 * MessageSender: for send SMS or MMS
 ***************************/
package com.zed3.sipua.message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.UUID;

import org.zoolu.tools.MyLog;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;
import com.zed3.utils.Zed3Log;

public class MessageSender {

	// TAG
	private static final String TAG = "MessageSender";
	// mToValue 联系人
	private  String mToValue;
	// mBodyValue 信息文本内容
	private  String mBodyValue;
	// mAttachmentUri 附件uri
	private Uri mAttachmentUri;
	// 全局变量，用于传递参数
	private final Context mContext;
	// houyuchun add 20120419 begin 
	// mContentType 附件类型(如：image/jpg, text/plain)
	private String mContentType;
	// mAttachName 附件名称
	private String mAttachName;
	// houyuchun add 20120419 end 
	// attach_count 附件个数
	private int attach_count = 0;	
	// DEFAULT_DELIVERY_REPORT_MODE 发送报告默认模式(true:开启  false:关闭)
    private static final boolean DEFAULT_DELIVERY_REPORT_MODE  = true;
    // DEFAULT_READ_REPORT_MODE 阅读报告默认模式(true:开启  false:关闭)
    private static final boolean DEFAULT_READ_REPORT_MODE = true;
    private static final boolean DEFAULT_SAVE_SENT_MESSAGE_MODE = true;
	private static final double DE2RA = 0.01745329252;
	private static final double RA2DE = 57.2957795129;
    private boolean isDeliveryReportOn = true;
    private boolean isReadReportOn = true;
    private boolean isSaveSentMessageOn = true;
    // MMS_SENT_TABLE 数据库表名
    private static final String MMS_SENT_TABLE = "mms_sent";
    private static final String SMS_SENT_TABLE = "sms_sent";
    //草稿箱
    private static final String SMS_DRAFT_TABLE = "sms_draft";
    private static final String MMS_DRAFT_TABLE = "mms_draft";
    
    // 文本类型
    private static final String TEXT_PLAIN = "text/plain";
    private MultiMessage mms;
    // houyuchun add 20120424 begin 
    // mSmsMessageType: 短信类型(普通，预定义和告警短信)
    private int mSmsMessageType = 0;
    // mReportAttrubute: 发送或阅读报告属性
    private String mReportAttrubute = "";
    // houyuchun add 20120424 end 
    // houyuchun add 20120516 begin 
    private String mE_id;
    // houyuchun add 20120516 end
    // houyuchun add 20120625 begin 
	// contacts, 用于保存联系人
	private String[] contacts;
    // houyuchun add 20120625 end 
	// houyuchun add 20120802 begin
	private SmsMmsDatabase database;
	// houyuchun add 20120802 end
	private static String sDataId;
	
	//0为发送成功，1为发送失败，2为发送中，3为发送完成
	public static final int PHOTO_UPLOAD_STATE_UPLOADING = 2;
	public static final int PHOTO_UPLOAD_STATE_FAILED = 1;
	public static final int PHOTO_UPLOAD_STATE_FINISHED = 3;
	public static final int PHOTO_UPLOAD_STATE_SUCCESS = 0;
	public static final int PHOTO_UPLOAD_STATE_OFFLINE_SPACE_FULL = 4;
	
	public MessageSender(Context context) {
		this.mContext = context;
	}
	//guojunfeng add for 短信二次开发接口
//	public MessageSender(Context context , String id) {
//		this.mContext = context;
//		this.mE_id = id ;
//	}
	public MessageSender(Context context, String to, String body){
		mToValue = to;			
		mBodyValue = body;
		mContext = context;
	}
	//guojunfeng add for 短信二次开发接口
	public MessageSender(Context context, String to, String body , String id){
		mToValue = to;			
		mBodyValue = body;
		mContext = context;
		mE_id = id;
	}
	public MessageSender(Context context, String to, String body, Uri uri, String content_type, String fileName,String E_id) {
		mToValue = to;
		mBodyValue = body;
		mAttachmentUri = uri;
		mContext = context;
		mContentType = content_type;
		mAttachName = fileName;
		mE_id = E_id;
	}
	
	public static void setSendDataId(String dataId){
		MessageSender.sDataId = dataId;
	}
	
	public static String getSendDataId(){
		return MessageSender.sDataId;
	}
	
	public void sendMultiMessage() {
		// houyuchun modify 20120625 begin 
		writeMmsInfoByteToTxt();
		contacts = mToValue.split(",");
		// 多个联系人
		if(contacts.length > 1) {
			int firstTag,lastTag;
			for(int i = 0; i < contacts.length; i++) {
				mToValue = contacts[i];
				firstTag = mToValue.indexOf("<");
				lastTag = mToValue.lastIndexOf(">");
				// houyuchun add 20120406 begin 
				if(firstTag != -1 && lastTag != -1) {
					mToValue = mToValue.substring(firstTag+1, lastTag);
					mToValue = mToValue.replace("-", "");
				}			
				// houyuchun add 20120406 end 
				MyLog.i(TAG, "mToValue = "+mToValue);
				if(mToValue == null) {
					return;
				}
				beginSendMultiMessage();
			}
		// 单个联系人
		} else {
			int firstTag = mToValue.indexOf("<");
			int lastTag = mToValue.lastIndexOf(">");
			if(firstTag != -1 && lastTag != -1) {
				mToValue = mToValue.substring(firstTag+1, lastTag);
				mToValue = mToValue.replace("-", "");
			}
			if(mToValue == null) {
				return;
			}
            beginSendMultiMessage();
		}
	}	
	
	// 将要发送的彩信信息写到文本文件  
	//郭俊峰重构于2014-05-24
	private void writeMmsInfoByteToTxt() {		
		OutputStreamWriter out = null;
		InputStream inStream = null;
		InputStream inStream_ = null;
	 	try {
	 		mms = new MultiMessage(mContext);
	 		// 统计附件个数
	 		attach_count = getAttachmentCount();
	 		mms.setAttachments(attach_count);	 		
	 		inStream = mContext.getContentResolver().openInputStream(mAttachmentUri);	 
//	 		byte[] attach_data = readInStream(inStream);
	 		
	 		
	 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1023];
			int length = -1;
			
			// 新建txt文件，保存当前需要发送的彩信内容
	 		//guojunfeng tag
	 		File file_dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()+"/smsmms");
			if(!file_dir.exists()) {
				file_dir.mkdirs();
			}
			File file = new File(file_dir, "mms.txt");
			if(file.exists()) {
				file.delete();
				file.createNewFile();
			}
			int datalength = 0;
			while((length = inStream.read(buffer)) != -1) {
				outStream.reset();
				outStream.write(buffer, 0, length);
				byte[] attach_data = outStream.toByteArray();
				
				// 对附件文件采用base64编码
		 		String data = Base64.encodeToString(attach_data, 0, attach_data.length, Base64.DEFAULT);
		 		datalength += data.getBytes("GBK").length;
	 			
		 		
//	 			buffer.append(Base64.encodeToString(mBodyValue.getBytes("GB2312"), Base64.DEFAULT).trim());
	 			
//	 			strBuffer.append(data);
	 			
//	 			out.write(buffer.toString());
			}
			if(inStream!=null)
				inStream.close();
			mms.setFile_name(mAttachName);
	 		mms.setAttachment_type(mContentType);
			mms.setAttachment_length(datalength);
			StringBuffer strBuffer = new StringBuffer();
			
			if(MultiMessage.CONTENT_TYPE_APPLICATION_STREAM.equals(mContentType)) {
				mms.setBody_type(null);
				mms.setBody_length(0);
				strBuffer.append(mms.getMessageHeader());
			} else {
				strBuffer.append(mms.getMessageHeader());
//				strBuffer.append(mBodyValue);
//				try {
//					// 为了防止Base64编码时自动加换行符，此处使用Base64.NO_WRAP
//					
				
//				String bytes = mBodyValue.getBytes("GBK");//Base64.encodeToString(,Base64.DEFAULT).trim();
				
		 		strBuffer.append(mBodyValue);
//					
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
			}
			
			
 			
 			//标记mms.txt一开始写到的位置
// 			int bufLength = strBuffer.toString().length();
 			// 写文件，将header和body域写进txt
			out = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
 			out.write(strBuffer.toString());
 			
			try {
				inStream_ = mContext.getContentResolver().openInputStream(mAttachmentUri);
				while((length = inStream_.read(buffer)) != -1) {
					outStream.reset();
					outStream.write(buffer, 0, length);
					byte[] attach_data = outStream.toByteArray();
					
					// 对附件文件采用base64编码
			 		String data = Base64.encodeToString(attach_data, 0, attach_data.length, Base64.DEFAULT);
		 			
//		 			buffer.append(Base64.encodeToString(mBodyValue.getBytes("GB2312"), Base64.DEFAULT).trim());
		 			
//		 			strBuffer.append(data);
		 			
//		 			out.write(buffer.toString());
		 			out.write(data);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (outStream != null) {
					try {
						outStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (inStream_ != null) {
					try {
						inStream_.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if (inStream != null) {
					try {
						inStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
	 		
 			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null) {
					out.close();
				}
				if(inStream != null) {
					inStream.close();
				}
			} catch(Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	// houyuchun add 20120419 begin 
	// 获得当前附件个数
	private int getAttachmentCount() {
		if(mToValue != null && !mToValue.equals("")) {
			attach_count += 1;	
			mms.setBody_type(TEXT_PLAIN);
	 		// body_length 文本内容大小
	 		long body_length = 0;
			try {
//				body_length = Base64.encodeToString(mBodyValue.getBytes("GB2312"), Base64.DEFAULT).trim().length();
				//body_length = mBodyValue.getBytes().length;//Base64.encodeToString(mBodyValue.getBytes("GBK"), Base64.DEFAULT).trim().length();
				body_length = mBodyValue.getBytes("GBK").length;
				Log.i("xxxx", "body_length = " + body_length);
				mms.setBody_length(body_length);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 		
		}
		if(mAttachmentUri != null && !mAttachmentUri.toString().equals("")) {
			attach_count += 1;	
		}
		return attach_count;
	}
	
	// 发送信息，将信息保存到已发送信息列表
	public  String getE_id() {
		 String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");    
		 return uuid;    

//		byte[] buf = new byte[32];
//		byte[] temp; 
//		// ip_address: 本机IP 地址
//		String ip_address = getLocalIpAddress();
//		if(ip_address == null || ip_address.equals("")) {
//			MyLog.i(TAG, "getLocalIpAddress fail, ip_address = "+ip_address);
//			Toast.makeText(mContext, "未注册成功", Toast.LENGTH_LONG).show();
//			return null;
//		}
//		String[] ip_spilt = ip_address.split("[.]");
//		// ip_format: 格式化IP地址 ，格式为"%08x"
//		String ip_format = String.format("%02x%02x%02x%02x", Integer.parseInt(ip_spilt[0]), Integer.parseInt(ip_spilt[1]), Integer.parseInt(ip_spilt[2]), Integer.parseInt(ip_spilt[3]));
//		temp = ip_format.getBytes();
//		System.arraycopy(temp, 0, buf, 0, temp.length);
//		// time_format: 格式化UNIX 时间，格式为"%010d"
////		String time_format = String.format("%010d", GpsTools.TimeToUnix());
////		temp = time_format.getBytes();
//		System.arraycopy(temp, 0, buf, 8, temp.length);
//		// 随机数
//		String random_str = getRandomString(14);
//		temp = random_str.getBytes();
//		System.arraycopy(temp, 0, buf, 18, temp.length);	    
//	    return new String(buf);
	}	
	// houyuchun add 20120424 end 
	
	// 获取Android本机IP地址
	public String getLocalIpAddress() {
		String ip = null;
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						ip = inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {	
			MyLog.i(TAG, "getLocalIpAddress error:");
			ex.printStackTrace();
		}
		return ip;
	}
	
	// 获取当前系统时间
	
	// 通过联系人电话匹配电话薄中的联系人，获得联系人信息
//	public String getRecipientInfoByNumber(String contact) {
//		Cursor cursor = null;
//		String recipient_name = null;
//		String[] projection = new String[] {Phone.CONTACT_ID, Phone.NUMBER, Phone.DISPLAY_NAME};
//		try {
//			cursor = mContext.getContentResolver().query(Phone.CONTENT_URI, projection, Phone.NUMBER + "=" + contact, null, null);				
//			if(cursor != null && cursor.moveToNext()) {
//				// 如果存在匹配的联系人
//                if (PhoneNumberUtils.compare(contact, cursor.getString(1))) {
//                	// 获得联系人姓名
//                	recipient_name = cursor.getString(2);
//                }
//			}
//		} catch(Exception e) {
//			MyLog.e(TAG, "getRecipientInfoByNumber error: ");
//			e.printStackTrace();
//		} finally {
//			if(cursor != null) {
//				cursor.close();
//			}
//		}
//		return recipient_name;
//	}
	// houyuchun add 20120419 end 
	
	// 读取附件字节流文件
	public byte[] readInStream(InputStream inStream) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = -1;
		try {
			while((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return outStream.toByteArray();
	}
	
	// houyuchun add 20120424 begin 
	// 发送短信
//	public void sendTextMessage() {
//		// houyuchun modify 20120626 begin
//		contacts = mToValue.split(",");
//		// 多个联系人
//		if(contacts.length > 1) {
//			int firstTag,lastTag;
//			for(int i = 0; i < contacts.length; i++) {
//				mToValue = contacts[i];
//				firstTag = mToValue.indexOf("<");
//				lastTag = mToValue.lastIndexOf(">");
//				// houyuchun add 20120406 begin 
//				if(firstTag != -1 && lastTag != -1) {
//					mToValue = mToValue.substring(firstTag+1, lastTag);
//					mToValue = mToValue.replace("-", "");
//				}			
//				// houyuchun add 20120406 end 
//				MyLog.i(TAG, "mToValue = "+mToValue);
//				if(mToValue == null) {
//					return;
//				}	
//				beginSendTextMessage();
//			}
//		// 单个联系人
//		} else {
//			int firstTag = mToValue.indexOf("<");
//			int lastTag = mToValue.lastIndexOf(">");
//			if(firstTag != -1 && lastTag != -1) {
//				mToValue = mToValue.substring(firstTag+1, lastTag);
//				mToValue = mToValue.replace("-", "");
//			}
//			if(mToValue == null) {
//				return;
//			}
//			beginSendTextMessage();
//		}
//		// houyuchun modify 20120626 end
//	}
	// houyuchun add 20120424 end 

	// houyuchun add 20120424 begin 
	// 获得短信设置项
	private String getSmsReportState() {
		// houyuchun add 20120526 begin 
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
 		// 发送报告状态
 		isDeliveryReportOn = /*pref.getBoolean(Settings.SMS_DELIVERY_REPORT_MODE, DEFAULT_DELIVERY_REPORT_MODE)*/true;
 		// 阅读报告状态
 		isReadReportOn = true;
// 		isReadReportOn = pref.getBoolean(Settings.SMS_READ_REPORT_MODE, DEFAULT_READ_REPORT_MODE);
		// houyuchun add 20120526 end
 		
 		// houyuchun add 20120605 begin 
 		// 是否保存已发送信息
// 		isSaveSentMessageOn = pref.getBoolean(Settings.SMS_SAVE_SENT_MODE, DEFAULT_SAVE_SENT_MESSAGE_MODE);
 		// houyuchun add 20120605 end		
 		MyLog.i(TAG, "delivery report = "+isDeliveryReportOn+",read report = "+ isReadReportOn);
 		if(isDeliveryReportOn) {
 			if(isReadReportOn) {
 				return "65535";
 			} else {
 				return "65533";
 			} 			
 		} else {
 			if(isReadReportOn) {
 				return "65534";
 			} else {
 				return "65532";
 			}
 		}
	}
	// houyuchun add 20120424 end  
	
	// houyuchun add 20120525 begin 
	// 获得彩信设置项
	private String getMmsReportAttribute() {	
		// houyuchun add 20120526 begin 
//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
 		// 发送报告状态
		//guojunfeng tagged 几个参数暂时写死
 		isDeliveryReportOn =true;
 		// 阅读报告状态
 		isReadReportOn =  DEFAULT_READ_REPORT_MODE;
 		// 是否需要保存已发送彩信
 		isSaveSentMessageOn = true;
		// houyuchun add 20120526 end
		
 		if(isDeliveryReportOn) {
 			if(isReadReportOn) {
 				return "65535";
 			} else {
 				return "65533";
 			} 			
 		} else {
 			if(isReadReportOn) {
 				return "65534";
 			} else {
 				return "65532";
 			}
 		}
	}
	// houyuchun add 20120525 end 
	
	// houyuchun add 2012-03-28 begin 
	// 保存信息到已发送信息列表
//	private void saveSmsToSent(String mToValue, String mBodyValue, String E_id) {
//		MyLog.i(TAG, "saveSmsToSent: " 
//	                  + "mToValue = " + mToValue + "\n"
//				      + "mBodyValue = " + mToValue + "\n"
//				      + "E_id = " + E_id);
// 		ContentValues values = new ContentValues();
// 		values.put("E_id", E_id);
// 		values.put("recipient_num", mToValue);
// 		values.put("body", mBodyValue);
// 		values.put("sent_time", getCurrentTime());
// 	    // houyuchun modify 20120802 begin 
// 	    database = new SmsMmsDatabase(mContext);
// 	    database.insert(SMS_SENT_TABLE, values);
// 	    // houyuchun modify 20120802 end 
//	}
	// houyuchun add 2012-03-28 end 
	
	// houyuchun add 20120503 begin 
	// 发送彩信
	private void sendMmsMessage() {
		//check network.add by mou 2015-02-02
		if (!Tools.isConnect(SipUAApp.getAppContext())) {
			updateMmsState(mE_id, PHOTO_UPLOAD_STATE_FAILED);
			MyToast.showToast(true, mContext, R.string.network_exception);
			return;
		}
		// houyuchun modify 20120511 begin 		
		// 发送INFO请求，body 应该为null
		String Result = Receiver.GetCurUA().sendMultiMessage(mToValue, null, mE_id, mReportAttrubute, "mms" , getMmsTxtByte().length);
		if(Result == null) {
//			Toast.makeText(mContext, mContext.getResources().getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
		}
	}
	
	//guojunfeng add for 短信二次开发接口
//	public void sendPreDefineMessageForTwiceDevelopment(final ArrayList<String> contacts, final String msg_body, final String msg_type) {
//		// 判断联系人和内容是否为空
//		if(contacts == null || msg_body == null) {
//			return;
//		}
//		
//		mReportAttrubute = getSmsReportState();
//		MyLog.i(TAG, "sendPreDefineMessage: msg_body = " + msg_body + "\n"
//				+ "msg_type = " + msg_type + "\n"
//				+ "E_id = " + E_id);
//		
//		for(String str : contacts) {
//			String result = Receiver.GetCurUA().SendTextMessage(str, msg_body, E_id, mReportAttrubute, msg_type);
//			if(result == null) {
//				Toast.makeText(mContext, mContext.getResources().getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
//			}
//		}
//	}
//	public void sendTextMessageForTwiceDevelopment() {
//		// houyuchun modify 20120626 begin
//		contacts = mToValue.split(",");
//		// 多个联系人
//		if(contacts.length > 1) {
//			int firstTag,lastTag;
//			for(int i = 0; i < contacts.length; i++) {
//				mToValue = contacts[i];
//				firstTag = mToValue.indexOf("<");
//				lastTag = mToValue.lastIndexOf(">");
//				// houyuchun add 20120406 begin 
//				if(firstTag != -1 && lastTag != -1) {
//					mToValue = mToValue.substring(firstTag+1, lastTag);
//					mToValue = mToValue.replace("-", "");
//				}			
//				// houyuchun add 20120406 end 
//				MyLog.i(TAG, "mToValue = "+mToValue);
//				if(mToValue == null) {
//					return;
//				}	
//				beginSendTextMessageTwiceDevelopment();
//			}
//		// 单个联系人
//		} else {
//			int firstTag = mToValue.indexOf("<");
//			int lastTag = mToValue.lastIndexOf(">");
//			if(firstTag != -1 && lastTag != -1) {
//				mToValue = mToValue.substring(firstTag+1, lastTag);
//				mToValue = mToValue.replace("-", "");
//			}
//			if(mToValue == null) {
//				return;
//			}
//			beginSendTextMessageTwiceDevelopment();
//		}
//	}
	//guojunfeng add for 短信二次接口
//	public void beginSendTextMessageTwiceDevelopment() {
//		mReportAttrubute = getSmsReportState();
//		
//		//黄灯短信放入草稿箱  guojunfeng
//		if (E_id == null) {
//			Toast.makeText(mContext, mContext.getResources().getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
//			return;
//		}
//		
//		if(isSaveSentMessageOn) {
//			saveSmsToSent(mToValue, mBodyValue, E_id);
//		}
//		// Result：用于标识是否发送成功
//		String Result = Receiver.GetCurUA().SendTextMessage(mToValue, mBodyValue, E_id, mReportAttrubute, "Normal");
//		if(Result == null) {
//			Toast.makeText(mContext, mContext.getResources().getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
//			return;
//		} 
//	}
	
	
	// houyuchun add 20120507 begin 
	// 获得彩信文件byte
	public byte[] getMmsTxtByte() {
		// 获得彩信封装的txt文件
//		String file_name = "/mnt/sdcard/smsmms/mms.txt";	
		//guojunfeng tag
		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()+"/smsmms/mms.txt");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MyLog.e(TAG, "getMmsTxtByte"+"   createNewFile() fail");
			}
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		InputStream inStream = null;
		try {
//			inStream = new FileInputStream(file_name);	
			inStream = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int length = -1;
			while((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}
			outStream.close();
			inStream.close();
		} catch(Exception e) {
			MyLog.e(TAG, "getMmsTxtByte error:");
			e.printStackTrace();
		} 
		return outStream.toByteArray();
	 }
	
	// houyuchun add 20120511 begin 
	// getRandomString(): 获得随机字符
	public static String getRandomString(int length) { // length 字符串长度 
		    StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"); 
		    StringBuffer sb = new StringBuffer(); 
		    Random r = new Random(); 
		    int range = buffer.length(); 
		    for (int i = 0; i < length; i ++) { 
		        sb.append(buffer.charAt(r.nextInt(range))); 
		    } 
		    return sb.toString(); 
	}
	// houyuchun add 20120511 end
	
	// houyuchun add 20120619 begin 
	// 发送预定义状态信息  String[] contacts
//	public void sendPreDefineMessage(final ArrayList<String> contacts, final String msg_body, final String msg_type) {
//		// 判断联系人和内容是否为空
//		if(contacts == null || msg_body == null) {
//			return;
//		}
//		
//		E_id = getE_id();
//		mReportAttrubute = getSmsReportState();
//		MyLog.i(TAG, "sendPreDefineMessage: msg_body = " + msg_body + "\n"
//				       + "msg_type = " + msg_type + "\n"
//				       + "E_id = " + E_id);
//		
//		for(String str : contacts) {
//			String result = Receiver.GetCurUA().SendTextMessage(str, msg_body, E_id, mReportAttrubute, msg_type);
//			if(result == null) {
//				Toast.makeText(mContext, mContext.getResources().getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
//			}
//		}
//	}
	// houyuchun add 20120619 end 
	
	// houyuchun add 20120626 being 
//	public void beginSendTextMessage() {
//		// houyuchun add 20120424 begin
//		mReportAttrubute = getSmsReportState();
//		// houyuchun modify 20120829 begin
//        E_id = getE_id();
//		// houyuchun modify 20120829 end
//        
//        //黄灯短信放入草稿箱  guojunfeng
//		if (E_id == null) {
//			ContentValues values = new ContentValues();
//			values.put("recipient_num", mToValue);
//			values.put("body", mBodyValue);
//			values.put("save_time", getCurrentTime());
//			SmsMmsDatabase database = new SmsMmsDatabase(mContext);
//			database.insert(SMS_DRAFT_TABLE, values);
//			return;
//		}
//        
//        // houyuchun add 20120605 begin 
//		if(isSaveSentMessageOn) {
//			saveSmsToSent(mToValue, mBodyValue, E_id);
//		}
//		// houyuchun add 20120605 end 
//		// Result：用于标识是否发送成功
//		String Result = Receiver.GetCurUA().SendTextMessage(mToValue, mBodyValue, E_id, mReportAttrubute, "Normal");
//		if(Result == null) {
//			Toast.makeText(mContext, mContext.getResources().getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
//			return;
//		} 
//		// houyuchun add 20120424 end 
//	}
	
	public void beginSendMultiMessage() {
//		writeMmsInfoByteToTxt();
 		// 保存信息到已发送信息列表
		// houyuchun add 20120605 begin
		mReportAttrubute = getMmsReportAttribute();
//		E_id = getE_id();
		
		//黄灯彩信放入草稿箱   guojunfeng
		if(mE_id==null)
		{
//			ContentValues values= new ContentValues();
//			values.put("recipient_num", mToValue);
//			values.put("body", mBodyValue);
//			values.put("save_time", getCurrentTime());
//			values.put("attachment", mAttachmentUri.toString());
//			values.put("E_id", E_id);
//			SmsMmsDatabase database = new SmsMmsDatabase(mContext);
//			database.insert(MMS_DRAFT_TABLE, values);
//			return;
		}else{
			//guojunfeng 从此处加入发件箱
			ContentValues values= new ContentValues();
			values.put("E_id", mE_id);
			values.put("address", mToValue);
			values.put("body", mBodyValue);
			values.put("date", getCurrentTime());
			values.put("attachment", mAttachmentUri.toString());
			values.put("attachment_name", mAttachName);
			values.put("type", "mms");
			values.put("mark", 1);
			values.put("send", 2);
			MyLog.v("guojunfeng", "..mE_id= "+mE_id);
			SmsMmsDatabase database = new SmsMmsDatabase(mContext);
			database.insert("message_talk", values);
			MyLog.v(TAG, "put in message_talk succsed");
		}
		//guojunfeng 注释
//		if(isSaveSentMessageOn) {
//	 		saveMmsToSent();
//		}
 		// houyuchun add 20120605 begin
 		// houyuchun add 20120503 begin 
 		// 发送彩信信息
 		sendMmsMessage();
 		// houyuchun add 20120503 end
	}
	/**
	 * reupload photo .add by mou 2015-01-07
	 */
	public void reUploadPhoto(String dataId) {
		writeMmsInfoByteToTxt();
		// 保存信息到已发送信息列表
		// houyuchun add 20120605 begin
		mReportAttrubute = getMmsReportAttribute();
//		E_id = getE_id();
		
		//黄灯彩信放入草稿箱   guojunfeng
		if(TextUtils.isEmpty(dataId))
		{
//			ContentValues values= new ContentValues();
//			values.put("recipient_num", mToValue);
//			values.put("body", mBodyValue);
//			values.put("save_time", getCurrentTime());
//			values.put("attachment", mAttachmentUri.toString());
//			values.put("E_id", E_id);
//			SmsMmsDatabase database = new SmsMmsDatabase(mContext);
//			database.insert(MMS_DRAFT_TABLE, values);
//			return;
		}else{
			//guojunfeng 从此处加入发件箱
			mE_id = dataId;
			updateMmsState(dataId, PHOTO_UPLOAD_STATE_UPLOADING);
		}
		//guojunfeng 注释
//		if(isSaveSentMessageOn) {
//	 		saveMmsToSent();
//		}
		// houyuchun add 20120605 begin
		// houyuchun add 20120503 begin 
		// 发送彩信信息
		sendMmsMessage();
		// houyuchun add 20120503 end
	}
	// houyuchun add 20120626 end 
	
	// houyuchun add 20120813 begin 
	// 紧急呼叫,上传GPS信息
//	public void uploadGPSInfoForEmergencyCall() {
//		if (MemoryMg.getInstance().IsUrgentCall) {
//			boolean flag = false;
//			// 判断gps开关是否开启 未开启就打开gps开关
//			if (!GpsTools.GetGspLockState(mContext)) {
//				GpsTools.OpenGPSByCode(mContext);
//				flag = true;
//			}
//
//			MyLog.i(TAG, "uploadGPSInfoForEmergencyCall");
//			GpsManage mge = new GpsManage(mContext);
//			GpsInfo info = mge.GetValueGpsStr();
//			if (info != null) {
//				if (flag)
//					GpsTools.OpenGPSByMySelf(info.gps_x, info.gps_y,
//							info.gps_speed, info.gps_height, info.gps_direction);
//				GpsTools.UploadGPSByTerminal(2, info.gps_x, info.gps_y,
//						info.gps_speed, info.gps_height, info.gps_direction);
//			} else {
//				if (flag)
//					GpsTools.OpenGPSByMySelf(0, 0, 0, 0, 0);
//				GpsTools.UploadGPSByTerminal(2, 0, 0, 0, 0, 0);
//			}
//		}
//		else
//			MyLog.e(TAG, "IsUrgentCall is false");
//	}
	// houyuchun add 20120813 end 
	
	// 倒地网络告警，上传终端GPS信息
//	public void upLoadGPSInfoForAlarm(int type) {
//		if (type != 254) {
//			// 发送紧急告警短信
//			ArrayList<String> arr = PCToolsInfo.getInstance().land_alarm_internet_sent_arraylist;
//			//ArrayList<String> arr =new ArrayList<String>();
//			//arr.add("1011");
//			if (arr != null && arr.size() > 0) {
//				sendPreDefineMessage(
//						arr,
//						MemoryMg.getInstance().TerminalNum
//								+ mContext.getResources().getString(
//										R.string.daodialarmkey),
//						"Emergent-Status");
//			}
//		}
//		//GPS
//		if (MemoryMg.getInstance().IsLand) {
//			boolean flag = false;
//			//判断gps开关是否开启   未开启就打开gps开关
//			if(!GpsTools.GetGspLockState(mContext))
//			{
//				GpsTools.OpenGPSByCode(mContext);
//				flag = true;
//			}
//			
//			if (type == 254)
//				type = 254;
//			else
//				type = 255;
//			
//			MyLog.i(TAG, "upLoadGPSInfoForAlarm"+ type);
//			
//			GpsInfo info = GpsManage.getInstance(mContext).GetValueGpsStr();
//			if (info != null){
//				if (flag)
//					GpsTools.OpenGPSByMySelf(info.gps_x, info.gps_y,
//							info.gps_speed, info.gps_height,info.gps_direction);
//				GpsTools.UploadGPSByTerminal(type, info.gps_x, info.gps_y,
//						info.gps_speed, info.gps_height,info.gps_direction);
//			}
//			else{
//				if (flag)
//					GpsTools.OpenGPSByMySelf(0, 0, 0, 0,0);
//				GpsTools.UploadGPSByTerminal(type, 0, 0, 0, 0,0);
//			}
//			////GpsTools.UploadGPSByTerminal(type, 116, 39, 0, 0,0);
//		}
//	}
	
	// 计算两个坐标点的方位角
	// lat_a: A坐标的纬度
	// lng_a: A坐标的经度
	// lat_b: B坐标的纬度
	// lng_b：B坐标的经度
	// 返回两坐标点的方位角度数
	public int getRodomDerictor(double lat_a, double lng_a, double lat_b, double lng_b) {
    	// result 角度结果
        double result = 0.0;       
        int iLat_a = (int)(0.50 + lat_a * 360000.0);
        int iLat_b = (int)(0.50 + lat_b * 360000.0);
        int iLng_a = (int)(0.50 + lng_a * 360000.0);
        int iLng_b = (int)(0.50 + lng_b * 360000.0);
        
        lat_a *= DE2RA;
        lng_a *= DE2RA;
        lat_b *= DE2RA;
    	lng_b *= DE2RA;
    	
    	// 如果经纬度均相同
    	if((iLat_a == iLat_b) && (iLng_a == iLng_b)) {
    		
    	// 如果经度相同
    	} else if(iLng_a == iLng_b) {
    		if(iLat_a > iLat_b) {
    			result = 180.0;
    		}
    	} else {
    		result = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a) * Math.cos(lat_b) * Math.cos(lng_b-lng_a);
    		result = Math.acos(result);
    		result = Math.cos(lat_b) * Math.sin(lng_b-lng_a) / Math.sin(result);
    		result = Math.asin(result);
    		result *= RA2DE;
    		
    		
//    		result = Math.sqrt(1 - result * result);
//    		result = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / result;
//    		result = Math.asin(result) * 180 / Math.PI;
    		if((iLat_b > iLat_a) && (iLng_b > iLng_a)) {
    			
    		} else if((iLat_b < iLat_a) && (iLng_b < iLng_a)) {
    			result = 180.0 - result;
    		} else if((iLat_b < iLat_a) && (iLng_b > iLng_a)) {
    			result = 180.0 - result;
    		} else if((iLat_b > iLat_a) && (iLng_b < iLng_a)) {
    			result += 360.0;
    		}
    	}
    	return (int)result;
	}
	public String getCurrentTime() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					" yyyy-MM-dd HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());
			String strTime = formatter.format(curDate);
			return strTime;
		} catch (Exception e) {
			
		}
		return null;
	}
	public static synchronized void updateMmsState(final String dataId,final int state){
		Zed3Log.debug("mmsTrace", "SendMessageThread#updateMmsState enter data id = " + dataId);
		if(!TextUtils.isEmpty(dataId)) {
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					final SmsMmsDatabase database = new SmsMmsDatabase(SipUAApp.getAppContext());;
//					ContentValues values = new ContentValues();
//					values.put("send", state);
//					database.update(SmsMmsDatabase.TABLE_MESSAGE_TALK, "type = 'mms' and mark = 1 and E_id = '" + dataId + "'", values);
//				}
//			}).start();
			final SmsMmsDatabase database = new SmsMmsDatabase(SipUAApp.getAppContext());;
			ContentValues values = new ContentValues();
			values.put("send", state);
			database.update(SmsMmsDatabase.TABLE_MESSAGE_TALK, "type = 'mms' and mark = 1 and E_id = '" + dataId + "'", values);
		}
	}
}

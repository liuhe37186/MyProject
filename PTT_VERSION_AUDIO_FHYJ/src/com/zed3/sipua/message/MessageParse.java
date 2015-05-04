package com.zed3.sipua.message;

import java.io.File;
import java.io.FileOutputStream;

import org.zoolu.tools.MyLog;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.zed3.sipua.message.PhotoTransferReceiveActivity.PhotoReceiveMessage;

public class MessageParse {
	
	private static final String TAG = "MessageParse";
	// 彩信版本标签
	private static final String MMS_VERSION = "MMS-Version";
	// 附件数量标签
	private static final String ATTACHMENTS = "attachments";
	// 附件类型
	private static final String CONTENT_TYPE = "Content-Type";
	// 支持的媒体类型
	private static final String TEXT_PLAIN = "text/plain";
	private static final String AUDIO_AMR         = "audio/amr";
    private static final String AUDIO_MP3         = "audio/mp3";
    private static final String AUDIO_MPEG        = "audio/mpeg";
    private static final String APPLICATION_OGG   = "application/ogg";
    private static final String IMAGE_JPEG        = "image/jpeg";
    private static final String IMAGE_JPG         = "image/jpg";
    // houyuchun add 20120625 begin 
    private static final String IMAGE_BMP = "image/**";
    // houyuchun add 20120625 end 
    private static final String TABLE_MMS_INBOX = "mms_inbox";
    // houyuchun add 20120619 begin 
    // 添加支持audio/wav音频文件格式
    private static final String AUDIO_WAV = "audio/wav";
    // houyuchun add 20120619 end
	// 附件大小标签
	private static final String CONTENT_LENGTH = "content-length";
	// 附件文件名称标签
	private static final String FILE_NAME = "filename";
	// 彩信附件长度
	private static int body_length = 0;
	// DOUBLE_BOUNDARY: 文件头和文件体分界标识
	private static final String DOUBLE_BOUNDARY = "\r\n\r\n";
	// 文件各行分界标识
	private static final String BOUNDARY = "\r\n";
	// 彩信文本内容
	private String body = null;
	// 彩信附件内容
	private String attachments = null;
	// 彩信附件名称
	private String file_name = null;
	private Context mContext;
	private String E_id = null;
	// 附件保存路径 uri
	private String attachments_uri = null;
	private String recipient_num = null;
	private SmsMmsDatabase database;
	private String mms_version;
	private int attachment_count;
	private String content_type;
	private String body_type;
	private int attachemnt_length;
	private String mms_header = null;
	
	
		
	public MessageParse(Context context, String E_id, String recipient_num) {
		this.mContext = context;
		this.E_id = E_id;
		this.recipient_num = recipient_num;
	}
	
	public MessageParse() {
		
	}
    
	// 获得彩信附件字节数组
    public static byte[] getBitmap(String imgBase64Str){   
	     byte[] bytes = null;
  	     try {     
  	    	bytes = Base64.decode(imgBase64Str, Base64.NO_WRAP);     
  	     } catch (Exception e) {   
  	    	 MyLog.i(TAG, "getBitmap error: ");
  	    	 e.printStackTrace();   
  	     }   
  	    return bytes;     	 
  	}
   
	// houyuchun add 20120524 begin 
    // 保存彩信信息到收件箱
    public Message saveMmsInfoToInbox() {
    	if(attachments_uri==null||file_name==null){
			return null;
		}
    	// houyuchun modify 20120806 begin 
    	ContentValues values = null;
    	try {
    		database = new SmsMmsDatabase(mContext);
    		values = new ContentValues();
    		values.put("E_id", E_id);
    		values.put("body", body);
    		values.put("sip_name", this.recipient_num);
    		values.put("attachment", attachments_uri);
    		values.put("attachment_name", file_name);
    		values.put("status", 0);
    		Message messageResult = Message.obtain();
			PhotoReceiveMessage message = new PhotoReceiveMessage();
			message.mPhotoPath = attachments_uri;
			message.mBody = body;
			message.mSipName = this.recipient_num;
			message.mReceiveTime = new MessageSender(mContext).getCurrentTime();
			
			values.put("type", "mms");
			
    		values.put("mark", "0");//0为收到的短息 1为发出的短信
    		
    		values.put("date", message.mReceiveTime);
    		
    		
    		database.insert(SmsMmsDatabase.TABLE_MESSAGE_TALK, values);
    		
    		message.sendToTarget();
    		
    		messageResult.obj = message;
    		
    		return messageResult;
    		
    		
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "saveMmsInfoToInbox error:");
			Log.i("xxxx", "MessageParse#saveMmsInfoToInbox exception = " + e.getMessage());
			e.printStackTrace();
			return null;
		}   	 
    
    }
    
    // 通过获得的附件信息创建附件文件
	public void createAttachmentFile(final String attachments) {
		File file_dir = null;
		File file = null;
		FileOutputStream outputStream = null;
		try {
			// 附件名称为空
			if(file_name == null) {
				return;
			}
			// 创建附件保存目录
			file_dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()+"/smsmms/");
			if(!file_dir.exists()) {
				file_dir.mkdirs();
			}			
			// houyuchun add 20120815 begin 
			int index = file_name.indexOf(".");
			if(index == -1) {
				MyLog.e(TAG, "createAttachmentFile, can not get file type");
				return;
			}
			// type 附件文件类型
			String type = file_name.substring(index + 1);
			long current_time = System.currentTimeMillis();
			// save_name 附件保存到sd卡的名称, 格式: 当前时间+文件扩展名
			StringBuffer save_name = new StringBuffer();
			save_name.append(current_time);
			save_name.append(".");
			save_name.append(type);
			MyLog.i(TAG, "save_name = " + save_name);
			// houyuchun add 20120815 end
			// 通过附件名称创建文件
			file = new File(file_dir, save_name.toString());
			if(file.exists()) {
				file.delete();
				file.createNewFile();
			}
			// 获取附件字节数
			byte[] bytes = getBitmap(attachments);
			if(MultiMessage.CONTENT_TYPE_APPLICATION_STREAM.equals(content_type)) {
				bytes = attachments.getBytes();
			} else {
			}
			outputStream = new FileOutputStream(file);
			// 写文件
			outputStream.write(bytes);
			// 获得附件路径
			attachments_uri = Environment.getExternalStorageDirectory()
					.getAbsolutePath()+"/smsmms/" + save_name;
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "createAttachmentFile error: ");
			e.printStackTrace();
		} finally {
			try {
				// 关闭流
				if(outputStream != null) {
					outputStream.close();	
				}
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		}
	}
	// houyuchun add 20120524 end 
	
	// 逐行解析彩信头文件
	private void parseMmsHeader(final String msg_header) {
		if(msg_header == null) {
			return;
		}
		try {
			String[] header_array = msg_header.split(BOUNDARY);
			String line;
			int index;
			for(int i = 0; i < header_array.length; i++) {
				line = header_array[i];
				// 获得彩信版本号
				if(line.indexOf(MMS_VERSION) >= 0) {
					mms_version = "1.0";
				}
				// 获得附件个数
				if(line.indexOf(ATTACHMENTS) > 0) {
					index = line.indexOf("=");
					attachment_count = Integer.parseInt(line.substring(index+1));
				}
				// 获得附件类型
				if(line.indexOf(CONTENT_TYPE) > 0) {
					// amr 录音文件
					if(line.indexOf(AUDIO_AMR) > 0) {
						content_type = AUDIO_AMR;                
					// mp3/mpeg/wav  音乐文件
					} else if(line.indexOf(AUDIO_MP3) > 0 || line.indexOf(AUDIO_MPEG) > 0 || line.indexOf(AUDIO_WAV) > 0) {
						content_type = AUDIO_MP3; 
					// text/plain 文本文件
					} else if(line.indexOf(TEXT_PLAIN) > 0) {
						body_type = TEXT_PLAIN; 
					// jpeg 图片
					} else if(line.indexOf(IMAGE_JPEG) > 0) {
						content_type = IMAGE_JPEG; 
					// jpg 图片
					} else if(line.indexOf(IMAGE_JPG) > 0) {
						content_type = IMAGE_JPG;
					// bmp 图片						
					} else if(line.indexOf(IMAGE_BMP) > 0) {
						content_type = IMAGE_BMP;
					} else {							
						MyLog.i(TAG, "unsupported content_type");
						return;
					}
				}
				// 获取附件长度
				if(line.indexOf(CONTENT_LENGTH) > 0) {
					String[] line_value = line.split(";");
					String file_value = line_value[1];
					if(file_value != null) {
	    				index = file_value.indexOf("=");
	    				if(i == 1) {
	    					body_length = Integer.parseInt(file_value.substring(index+1));
	    				} else {
	    					attachemnt_length = Integer.parseInt(file_value.substring(index+1));
	    				}
					} 
				}
				// 获取附件名称
				if(line.indexOf(FILE_NAME) > 0) {
					index = line.lastIndexOf("=");
					file_name = line.substring(index+1);
				}
			}
			MyLog.i(TAG, "parseMmsHeader: body_length = " + body_length + "\n"
					      + "attachemnt_length = " + attachemnt_length + "\n"
					      + "file_name = " + file_name);
		} catch(Exception e) {
			MyLog.e(TAG, "parseMmsHeader error: ");
			e.printStackTrace();
		}		
	}

	// 解析彩信体
	private void parseMmsBody(final String msg_body) {
		if(msg_body == null) {
			return;
		}
		try {
			body = getMmsTextBody(msg_body);
		    MyLog.i(TAG, "body = " + body);
		    // 附件内容
		    attachments = msg_body.substring(body.length());
		    createAttachmentFile(attachments);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.i(TAG, "parseMmsBody error: ");
			e.printStackTrace();
		}
	}
    
	// 从整个msg_body中截取文本内容
	private String getMmsTextBody(final String msg_body) {
		byte[] body_byte = null;
		String str = null;
		//guojunfeng moidfy 20130409  如果文本是空的，不要去截取，直接返回一个空字符串 
		if(body_length==0){
			return "";
		}
		try {
			for(int i = 1; i < 500; i++) {
				body_byte = msg_body.substring(0, i).getBytes("GBK");
				MyLog.i(TAG, "body_byte.length = " + body_byte.length);
				if(body_byte.length == body_length) {
					str = new String(body_byte, "GBK");
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.i(TAG, "getMmsTextBody error: ");
			e.printStackTrace();
		}
		return str;
	}
	
    // 通过获得的彩信字节数组解析彩信内容
	public int parseMmsInfoFromTxt(byte[] bytes) {
	    try {
	    	if(bytes == null) {
	    		return 0;
	    	} 
	    	// mms_info 文件内容，将字节装换为字符串格式
	    	String mms_info = new String(bytes, "UTF-8");
	    	// DOUBLE_BOUNDARY  文件header和body分界标识
	    	String[] info = mms_info.split(DOUBLE_BOUNDARY);
	    	// header_info 文件头
	    	String header_info = info[0];
	    	// 解析彩信头
	    	parseMmsHeader(header_info);
	    	// body_info 文件体
	    	String body_info = info[1];
	    	// 解析彩信体
	    	parseMmsBody(body_info);
	    	return 1;
	    } catch(Exception e) {
			MyLog.e(TAG, "parseMmsInfoFromTxt error:");
			e.printStackTrace();
			return 0;
	    }
	}
	
	public String getContentType(){
		return content_type;
	}
	
}


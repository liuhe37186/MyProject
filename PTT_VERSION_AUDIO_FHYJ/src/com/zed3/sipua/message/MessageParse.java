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
	// ���Ű汾��ǩ
	private static final String MMS_VERSION = "MMS-Version";
	// ����������ǩ
	private static final String ATTACHMENTS = "attachments";
	// ��������
	private static final String CONTENT_TYPE = "Content-Type";
	// ֧�ֵ�ý������
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
    // ���֧��audio/wav��Ƶ�ļ���ʽ
    private static final String AUDIO_WAV = "audio/wav";
    // houyuchun add 20120619 end
	// ������С��ǩ
	private static final String CONTENT_LENGTH = "content-length";
	// �����ļ����Ʊ�ǩ
	private static final String FILE_NAME = "filename";
	// ���Ÿ�������
	private static int body_length = 0;
	// DOUBLE_BOUNDARY: �ļ�ͷ���ļ���ֽ��ʶ
	private static final String DOUBLE_BOUNDARY = "\r\n\r\n";
	// �ļ����зֽ��ʶ
	private static final String BOUNDARY = "\r\n";
	// �����ı�����
	private String body = null;
	// ���Ÿ�������
	private String attachments = null;
	// ���Ÿ�������
	private String file_name = null;
	private Context mContext;
	private String E_id = null;
	// ��������·�� uri
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
    
	// ��ò��Ÿ����ֽ�����
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
    // ���������Ϣ���ռ���
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
			
    		values.put("mark", "0");//0Ϊ�յ��Ķ�Ϣ 1Ϊ�����Ķ���
    		
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
    
    // ͨ����õĸ�����Ϣ���������ļ�
	public void createAttachmentFile(final String attachments) {
		File file_dir = null;
		File file = null;
		FileOutputStream outputStream = null;
		try {
			// ��������Ϊ��
			if(file_name == null) {
				return;
			}
			// ������������Ŀ¼
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
			// type �����ļ�����
			String type = file_name.substring(index + 1);
			long current_time = System.currentTimeMillis();
			// save_name �������浽sd��������, ��ʽ: ��ǰʱ��+�ļ���չ��
			StringBuffer save_name = new StringBuffer();
			save_name.append(current_time);
			save_name.append(".");
			save_name.append(type);
			MyLog.i(TAG, "save_name = " + save_name);
			// houyuchun add 20120815 end
			// ͨ���������ƴ����ļ�
			file = new File(file_dir, save_name.toString());
			if(file.exists()) {
				file.delete();
				file.createNewFile();
			}
			// ��ȡ�����ֽ���
			byte[] bytes = getBitmap(attachments);
			if(MultiMessage.CONTENT_TYPE_APPLICATION_STREAM.equals(content_type)) {
				bytes = attachments.getBytes();
			} else {
			}
			outputStream = new FileOutputStream(file);
			// д�ļ�
			outputStream.write(bytes);
			// ��ø���·��
			attachments_uri = Environment.getExternalStorageDirectory()
					.getAbsolutePath()+"/smsmms/" + save_name;
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e(TAG, "createAttachmentFile error: ");
			e.printStackTrace();
		} finally {
			try {
				// �ر���
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
	
	// ���н�������ͷ�ļ�
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
				// ��ò��Ű汾��
				if(line.indexOf(MMS_VERSION) >= 0) {
					mms_version = "1.0";
				}
				// ��ø�������
				if(line.indexOf(ATTACHMENTS) > 0) {
					index = line.indexOf("=");
					attachment_count = Integer.parseInt(line.substring(index+1));
				}
				// ��ø�������
				if(line.indexOf(CONTENT_TYPE) > 0) {
					// amr ¼���ļ�
					if(line.indexOf(AUDIO_AMR) > 0) {
						content_type = AUDIO_AMR;                
					// mp3/mpeg/wav  �����ļ�
					} else if(line.indexOf(AUDIO_MP3) > 0 || line.indexOf(AUDIO_MPEG) > 0 || line.indexOf(AUDIO_WAV) > 0) {
						content_type = AUDIO_MP3; 
					// text/plain �ı��ļ�
					} else if(line.indexOf(TEXT_PLAIN) > 0) {
						body_type = TEXT_PLAIN; 
					// jpeg ͼƬ
					} else if(line.indexOf(IMAGE_JPEG) > 0) {
						content_type = IMAGE_JPEG; 
					// jpg ͼƬ
					} else if(line.indexOf(IMAGE_JPG) > 0) {
						content_type = IMAGE_JPG;
					// bmp ͼƬ						
					} else if(line.indexOf(IMAGE_BMP) > 0) {
						content_type = IMAGE_BMP;
					} else {							
						MyLog.i(TAG, "unsupported content_type");
						return;
					}
				}
				// ��ȡ��������
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
				// ��ȡ��������
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

	// ����������
	private void parseMmsBody(final String msg_body) {
		if(msg_body == null) {
			return;
		}
		try {
			body = getMmsTextBody(msg_body);
		    MyLog.i(TAG, "body = " + body);
		    // ��������
		    attachments = msg_body.substring(body.length());
		    createAttachmentFile(attachments);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.i(TAG, "parseMmsBody error: ");
			e.printStackTrace();
		}
	}
    
	// ������msg_body�н�ȡ�ı�����
	private String getMmsTextBody(final String msg_body) {
		byte[] body_byte = null;
		String str = null;
		//guojunfeng moidfy 20130409  ����ı��ǿյģ���Ҫȥ��ȡ��ֱ�ӷ���һ�����ַ��� 
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
	
    // ͨ����õĲ����ֽ����������������
	public int parseMmsInfoFromTxt(byte[] bytes) {
	    try {
	    	if(bytes == null) {
	    		return 0;
	    	} 
	    	// mms_info �ļ����ݣ����ֽ�װ��Ϊ�ַ�����ʽ
	    	String mms_info = new String(bytes, "UTF-8");
	    	// DOUBLE_BOUNDARY  �ļ�header��body�ֽ��ʶ
	    	String[] info = mms_info.split(DOUBLE_BOUNDARY);
	    	// header_info �ļ�ͷ
	    	String header_info = info[0];
	    	// ��������ͷ
	    	parseMmsHeader(header_info);
	    	// body_info �ļ���
	    	String body_info = info[1];
	    	// ����������
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


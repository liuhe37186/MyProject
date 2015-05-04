/****************************
 * houyuchun create 20120426
 * MultiMessage: before send MMS, first wrap the MMS info to a .txt file
 * include the MMS header and body
 ***************************/

package com.zed3.sipua.message;

import android.content.Context;
import android.net.Uri;

public class MultiMessage {		
	
	// TAG
	private static final String TAG = "MultiMessage";
	public static final String CONTENT_TYPE_APPLICATION_STREAM = "application/octet-stream";
	// mContext: 用于传递参数
	private Context mContext;
	public MultiMessage(Context context) {
		this.mContext = context;
	}
	
		// 彩信版本
		private static final String MMS_VERSION = "MMS-Version:1.0";
		// 附件数量
		private static final String ATTACHMENTS_COUNT = "attachments=";
		// 附件类型
		private static final String CONTENT_TYPE = "Content-Type:";
		// 附件大小
		private static final String CONTENT_LENGTH = "content-length=";
		// 附件文件名称
		private static final String FILE_NAME = "filename=";
		// boundary：head域和body域的分界标识
		private static final String BOUNDARY = "\r\n";
		
		StringBuffer headBuffer = new StringBuffer(); 
		
		public int getAttachments() {
			return attachment_count;
		}
		public void setAttachments(int count) {
			this.attachment_count = count;
		}
		public String getFile_name() {
			return file_name;
		}
		public void setFile_name(String file_name) {
			this.file_name = file_name;
		}

		
		public String getAttachment_type() {
			return attachment_type;
		}
		public void setAttachment_type(String attachment_type) {
			this.attachment_type = attachment_type;
		}
		public String getBody_type() {
			return body_type;
		}
		public void setBody_type(String body_type) {
			this.body_type = body_type;
		}
		
		private int attachment_count;
		// houyuchun modify 20120420 begin 
		// attachment_type 附件类型
		private String attachment_type;
		// body_type 文本类型
		private String body_type; 		
		// houyuchun modify 20120420 end 
		private long attachment_length;
		

		public long getAttachment_length() {
			return attachment_length;
		}
		public void setAttachment_length(long attachment_length) {
			this.attachment_length = attachment_length;
		}

		private String file_name;
		private Uri mAttachmentUri;
        private long body_length;
		
		
		
		public long getBody_length() {
			return body_length;
		}
		public void setBody_length(long body_length) {
			this.body_length = body_length;
		}
		public Uri getmAttachmentUri() {
			return mAttachmentUri;
		}
		public void setmAttachmentUri(Uri mAttachmentUri) {
			this.mAttachmentUri = mAttachmentUri;
		}
		
//		public byte[] getHeadByte() {
//			String header =   MMS_VERSION + ";" + ATTACHMENTS_COUNT + attachment_count + BOUNDARY
//					+ CONTENT_TYPE + "text/plain" + ";" + CONTENT_LENGTH + body_length + BOUNDARY
//					+ CONTENT_TYPE + "image/jpeg" + ";" + CONTENT_LENGTH + attach_length + BOUNDARY + BOUNDARY;
//			return header.getBytes();
//		} 
		
	    // houyuchun add 20120419 begin 
		/**
		 * getMessageHeader(): 获取彩信头域数据
		 * 
		 * MMS_VERSION: 彩信版本号，当前默认为1.0
		 * ATTACHMENTS: 附件个数
		 * CONTENT_TYPE: 附件类型(例如：text/plain, image/jpg)
		 * FILE_NAME: 附件名称
		 * CONTENT_LENGTH: 附件大小
		 * BOUNDARY: 分界标识 '\r\n' 
		 */
		public String getMessageHeader() {
			
			// houyuchun modify 20120420 begin
			if(attachment_count > 0) {
				headBuffer.append(MMS_VERSION);
				headBuffer.append(";");
				headBuffer.append(ATTACHMENTS_COUNT);
				headBuffer.append(attachment_count);
				headBuffer.append(BOUNDARY);
				// 带有文本内容
				if(body_type != null && !body_type.equals("")) {
					headBuffer.append(CONTENT_TYPE);
					headBuffer.append(body_type);
					headBuffer.append(";");
					headBuffer.append(CONTENT_LENGTH);
					headBuffer.append(body_length);
					headBuffer.append(BOUNDARY);
				}
				// 带有附件
				if(attachment_type != null && !attachment_type.equals("")) {
					headBuffer.append(CONTENT_TYPE);
					headBuffer.append(attachment_type);
//					headBuffer.append(";");
//					headBuffer.append(FILE_NAME);
//					headBuffer.append(file_name);
					headBuffer.append(";");
					headBuffer.append(CONTENT_LENGTH);
					headBuffer.append(attachment_length);
					headBuffer.append(";");
					headBuffer.append(FILE_NAME);
					headBuffer.append(file_name);
					headBuffer.append(BOUNDARY);
					headBuffer.append(BOUNDARY);
				}
			}
			// houyuchun modify 20120420 end			
			return headBuffer.toString();
		}
}



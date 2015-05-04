/****************************
 * houyuchun create 20120517
 * SmsMmsReceiver: for receive new SMS or MMS
 ***************************/

package com.zed3.sipua.message;

import java.util.ArrayList;

import org.zoolu.tools.MyLog;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zed3.media.TipSoundPlayer;
import com.zed3.media.TipSoundPlayer.Sound;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.toast.MyToast;
import com.zed3.utils.Systems;
import com.zed3.utils.Zed3Log;

public class SmsMmsReceiver extends BroadcastReceiver {

	private static final String TAG = "SmsMmsReceiver";
	private static final int GROUP_NUM = 0;
	private static final int MESSAGE_RECEIVE_OK = 1;
	private static final int MESSAGE_RECEIVE_FAIL = 2;
	private static final int MESSAGE_READ = 3;
	private static final int MESSAGE_SEND_OK = 4;
	private static final int MESSAGE_SEND_FAIL = 5;
	// guojunfeng add for 短信二次开发接口广播
	public static final String ACTION_RECEIVE_SMSMMS_ANSWER = "com.zed3.sipua.development_interface_answer";
	public static final int FLAG_SMS = 10;
	public static final int FLAG_MMS = 11;
	// MESSAGE_REPORT_RECEIVE_OK: 信息发送报告OK
	public static final String MESSAGE_REPORT_RECEIVE_OK = "ReceiveOK";
	public static final String MESSAGE_REPORT_RECEIVE_OK_DATABASE = "ReceiveOK_DataBase";
	// MESSAGE_REPORT_RECEIVE_FAIL: 信息发送报告FAIL
	public static final String MESSAGE_REPORT_RECEIVE_FAIL = "ReceiveFail";
	// MESSAGE_REPORT_READ_OK: 信息已阅报告OK
	public static final String MESSAGE_REPORT_READ_OK = "Read";
	public static final String ACTION_RECEIVE_SMS_MESSAGE = "com.zed3.sipua.sms_receive";
	public static final String ACTION_RECEIVER_MMS_MESSAGE = "com.zed3.sipua.mms_receive";
	public static final String ACTION_GROUP_NUM_TYPE = "com.zed3.sipua.group_num_type";
	public static final String ACTION_DELIVERY_REPORT_REPLY = "com.zed3.sipua.delivery_report";
	public static final String ACTION_READ_REPORT_REPLY = "com.zed3.sipua.read_report";
	// houyuchun add 20120518 begin
	public static final String ACTION_SEND_MESSAGE_OK = "com.zed3.sipua.send_message_ok";
	public static final String ACTION_SEND_MESSAGE_FAIL = "com.zed3.sipua.send_message_fail";
	// houyuchun add 20120518 end
	// houyuchun add 20120618 begin
	public static final String ACTION_KEY_LONG_CLICK = "android.intent.action.NUMBER_KEY_PRESSED";
	public static final String ACTION_RECEIVE_SMSMMS_INTERFACE = "com.zed3.sipua.development_interface";
	//对方离线空间已满
	public static final String ACTION_OFFLINE_SPACE_FULL = "com.zed3.sipua.mms_offline_space_full";
	// 信息类型：预定义状态信息
	private static final String TYPE_PREDEFINE_STATUS = "Predefine-Status";
	// 紧急状态信息
	private static final String TYPE_EMERGENT_STATUS = "Emergent-Status";
	private static final int TYPE_ALARM_EMERGENCY = 6;
	private String pre_define_msg;
	private String[] contact_sent_list;
	private ArrayList<String> contact_sent_arraylist;
	// houyuchun add 20120618 end
	// houyuchun add 20120813 begin
	// emergency call action
	public static final String ACTION_LTE_JOIN_EMERGENCY_GROUP = "android.intent.action.LTE_EMERGENCY_CALL";
	// houyuchun add 20120813 end
	private String E_id;
	ContentValues values = new ContentValues();
	private Context mContext;
	private String recipient_num;
	private int flag = -1;
	// houyuchun add 20120730 begin
	// GROUP_NUMBER_TYPE 组号码类型
	public static final String GROUP_NUMBER_TYPE = "Group";
	private SmsMmsDatabase database;
	private static final String TABLE_SMS_SENT = "sms_sent";
	private static final String TABLE_MMS_SENT = "mms_sent";
	private String where = "";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		mContext = context;
		String intentAction = intent.getAction();
		Systems.log.print("testsound", "SmsMmsReceiver#onReceive enter action = " + intentAction);
		MyLog.i(TAG, "intentAction = " + intentAction);
		// 新的短信信息
		// if(intentAction.equals(ACTION_RECEIVE_SMS_MESSAGE)) {
		// MessagingNotification mNotification = new
		// MessagingNotification(context, intent.getExtras());
		// mNotification.getSmsNewMessageNotification();
		// // 新的彩信信息
		// } else
		if (intentAction.equals(ACTION_RECEIVER_MMS_MESSAGE)) {
			// MessagingNotification mNotification = new
			// MessagingNotification(context, intent.getExtras());
			// mNotification.getMmsNewMessageNotification();
			// 号码为组号码
			
			TipSoundPlayer.getInstance().play(Sound.MESSAGE_ACCEPT);
            
			String contentType = intent.getStringExtra("contentType");
			Log.i("xxxx", "SmsMmsReceive#onReceive content type = " + contentType);
			if(!TextUtils.isEmpty(contentType)) {
				
				context.sendBroadcast(new Intent(PhotoTransferReceiveActivity.ACTION_RECEIVE_MMS));
				
			} else {
				context.sendBroadcast(new Intent(PhotoTransferReceiveActivity.ACTION_RECEIVE_MMS));
			}
			
		} else if (intentAction.equals(ACTION_GROUP_NUM_TYPE)) {
			// houyuchun add 20120730 begin
			mHandler.sendMessage(mHandler.obtainMessage(GROUP_NUM, intent));
			// houyuchun add 20120730 end
			// 信息成功送达到接收方，呈现已送达报告
		} else if (intentAction.equals(ACTION_DELIVERY_REPORT_REPLY)) {
			MyLog.v("guojunfeng", "REPORT_REPLY");
			Bundle bundle = intent.getExtras();
			if (bundle == null) {
				return;
			}
			E_id = bundle.getString("E_id");
			recipient_num = bundle.getString("recipient_num");
			String reply = bundle.getString("reply");
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			// guojunfeng tagged 2014 02 19
			Boolean isSaveSentMessageOn = true;
			// pref.getBoolean(Settings.MMS_SAVE_SENT_MODE,
			// true);
			if (reply.trim().equals(MESSAGE_REPORT_RECEIVE_OK)) {
				SmsMmsDatabase mSmsMmsDatabase = new SmsMmsDatabase(mContext);
				ContentValues mContentValues = new ContentValues();
				mContentValues.put("send", MessageSender.PHOTO_UPLOAD_STATE_SUCCESS);
				mSmsMmsDatabase.update("message_talk", "E_id ='" + E_id + "'",
						mContentValues);
//				MyToast.showToast(
//						true,
//						Receiver.mContext,
//						recipient_num
//								+ Receiver.mContext.getResources().getString(
//										R.string.sent_success));
				//GQT英文版 2014-9-3 
				Configuration configuration = mContext.getResources().getConfiguration();
				String language = configuration.locale.getLanguage();
				String text = "";
				if(language.equals("en")){
					text = mContext.getResources().getString(R.string.sent_success);
				}else if(language.equals("zh")){
					text = recipient_num+" "+mContext.getResources().getString(R.string.sent_success);
				}
				MyToast.showToast(true,Receiver.mContext,text);
				if (mSmsMmsDatabase != null)
					mSmsMmsDatabase.close();
				Intent intent_ = new Intent(MESSAGE_REPORT_RECEIVE_OK_DATABASE);
				mContext.sendBroadcast(intent_);
				// //guojunfeng 发送成功后把草稿箱里的短信放入 begin

				// database = new SmsMmsDatabase(mContext);
				// String type = bundle.getString("type");
				// if(isSaveSentMessageOn && type!=null && type.equals("mms")){
				// // Cursor cur = database.query("mms_draft",
				// "save_time desc ");
				// Cursor cur = database.mQuery("mms_draft", "E_id='"+E_id+"'");
				// if(cur!= null&&cur.getCount()==1&&cur.moveToFirst()){
				// ContentValues value = new ContentValues();
				// value.put("E_id", E_id);
				// value.put("recipient_num", recipient_num);
				// value.put("body", cur.getString(cur.getColumnIndex("body")));
				// value.put("attachment",
				// cur.getString(cur.getColumnIndex("attachment")));
				// String strTime = null;
				// try {
				// SimpleDateFormat formatter = new SimpleDateFormat(
				// " yyyy-MM-dd HH:mm ");
				// Date curDate = new Date(System.currentTimeMillis());// 获取当前时
				// strTime = formatter.format(curDate);
				// } catch(Exception e) {
				// MyLog.v(TAG, "get time exception");
				// }
				// cur.close();
				// value.put("sent_time", strTime);
				// MyLog.v("guojunfeng", "cunruyifasong2");
				// database.insert(TABLE_MMS_SENT, value);
				// database.delete("mms_draft", "E_id='"+E_id+"'");
				// }
				// }
				//
				// //guojunfeng add 2012-11-15 begin for 二次开发接口
				// Intent intent1 = new Intent(ACTION_RECEIVE_SMSMMS_ANSWER);
				// intent1.putExtra("SMSID", E_id);
				// intent1.putExtra("SMSNumber", recipient_num);
				// intent1.putExtra("SMSREPLY", "RECV_OK");
				// mContext.sendBroadcast(intent1);
				// //guojunfeng add 2012-11-15 end for 二次开发接口
				// mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_RECEIVE_OK,
				// recipient_num));
			} else if (reply.equals(MESSAGE_REPORT_RECEIVE_FAIL)) {
				// guojunfeng add 2012-11-15 begin for 二次开发接口
				// Intent intent1 = new Intent(ACTION_RECEIVE_SMSMMS_ANSWER);
				// intent1.putExtra("SMSID", E_id);
				// intent1.putExtra("SMSNumber", recipient_num);
				// intent1.putExtra("SMSREPLY", "RECV_FAIL");
				// mContext.sendBroadcast(intent1);
				// //guojunfeng add 2012-11-15 end for 二次开发接口
				// mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_RECEIVE_FAIL,
				// recipient_num));
				MyToast.showToast(
						true,
						Receiver.mContext,
						Receiver.mContext.getResources().getString(
								R.string.mms_sendFailed_1)
								+ recipient_num
								+ Receiver.mContext.getResources().getString(
										R.string.mms_sendFailed_2));
				
				final SmsMmsDatabase database = new SmsMmsDatabase(SipUAApp.getAppContext());
				ContentValues values = new ContentValues();
				values.put("send",MessageSender.PHOTO_UPLOAD_STATE_FAILED);
				database.update(SmsMmsDatabase.TABLE_MESSAGE_TALK, "E_id ='" + E_id + "'", values);
				
			} else if (reply.equals(MESSAGE_REPORT_READ_OK)) {
				MyToast.showToast(true, Receiver.mContext, recipient_num
						+ Receiver.mContext.getResources().getString(
								R.string.readed));
			} else {
				MyLog.i(TAG, "bail out because of unknow report state: "
						+ reply);
				return;
			}
			// houyuchun add 20120518 begin
			// 信息成功发送到服务器，更新已发送信息的当前状态
		} else if (intentAction.equals(ACTION_SEND_MESSAGE_OK)) {
			MyLog.v("guojunfeng", "MESSAGE_OK");
			Bundle bundle = intent.getExtras();
			if (bundle == null) {
				return;
			}
			E_id = bundle.getString("E_id");
			mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SEND_OK, E_id));
			// houyuchun add 20120618 begin
			// 响应用户长按键值，发送对应预定义状态信息
		}
		else if (intentAction.equals(ACTION_SEND_MESSAGE_FAIL)) {
			mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SEND_FAIL,
					intent));
			// 是否是紧急呼叫
		}
		//提示用户对方的离线空间已满
		else if (intentAction.equals(ACTION_OFFLINE_SPACE_FULL)) {
			String text = intent.getStringExtra("recipient_num") + " " + Receiver.mContext.getResources().getString(R.string.upload_offline_space_full);
			MyToast.showToast(true, Receiver.mContext, text);
		}
		
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// houyuchun add 20120802 begin
			database = new SmsMmsDatabase(mContext);
			// houyuchun add 20120802 end
			switch (msg.what) {
			// 当前号码为组号码
			case GROUP_NUM:
				// houyuchun modify 20120730 begin
				Intent intent = (Intent) msg.obj;
				Bundle bundle = intent.getExtras();
				if (bundle == null) {
					return;
				}
				E_id = bundle.getString("E_id");
				if (E_id == null || E_id.equals("")) {
					return;
				}
				values.put("num_type", 1);
				if (intent.getFlags() == FLAG_MMS) {
					// houyuchun modify 20120802 begin
					where = "E_id = '" + E_id + "'";
					database.update(TABLE_MMS_SENT, where, values);
					// houyuchun modify 20120802 end
				} else if (intent.getFlags() == FLAG_SMS) {
					// houyuchun modify 20120802 begin
					where = "E_id = '" + E_id + "'";
					database.update(TABLE_SMS_SENT, where, values);
					// houyuchun modify 20120802 end
				} else {
					MyLog.e(TAG, "error flag: " + msg.arg1);
					return;
				}
				break;
			// houyuchun modify 20120730 end
			// 信息成功发送到接收方，呈现已送达接收方报告
			case MESSAGE_RECEIVE_OK:
				recipient_num = (String) msg.obj;
				Toast.makeText(mContext, mContext.getResources().getString(R.string.up_success) + recipient_num,
						Toast.LENGTH_LONG).show();
				break;
			// 接收方接收信息失败，呈现送达失败报告
			case MESSAGE_RECEIVE_FAIL:
				recipient_num = (String) msg.obj;
				Toast.makeText(mContext, mContext.getResources().getString(R.string.up_failed) + recipient_num,
						Toast.LENGTH_LONG).show();
				break;
			// 接收方已阅信息，呈现信息已阅报告
			case MESSAGE_READ:
				recipient_num = (String) msg.obj;
				Toast.makeText(mContext, mContext.getResources().getString(R.string.picture_readed) + recipient_num,
						Toast.LENGTH_LONG).show();
				break;
			// houyuchun add 20120518 begin
			// 信息成功发送到服务器后，更新当前已发送信息状态
			case MESSAGE_SEND_OK:
				E_id = (String) msg.obj;
				values.put("status", 1);
				// houyuchun modify 20120802 begin
				where = "E_id = '" + E_id + "'";
				database.update(TABLE_SMS_SENT, where, values);
				// houyuchun modify 20120802 end
				break;
			// houyuchun add 20120625 begin
			// 信息发送失败，更新发送信息状态
			case MESSAGE_SEND_FAIL:
				Intent mIntent = (Intent) msg.obj;
				Bundle mBundle = mIntent.getExtras();
				if (mBundle == null) {
					return;
				}
				String failed_num = mBundle.getString("RECIPIENT_NUM");
				Toast.makeText(mContext, mContext.getString(R.string.send_failed) + failed_num,
						Toast.LENGTH_LONG).show();
				E_id = mBundle.getString("E_ID");
				values.put("status", 2);
				if (mIntent.getFlags() == FLAG_SMS) {
					// houyuchun modify 20120802 begin
					where = "E_id = '" + E_id + "'";
					database.update(TABLE_SMS_SENT, where, values);
					// houyuchun modify 20120802 end
				} else if (mIntent.getFlags() == FLAG_MMS) {
					// db_service.updateTableInfoById(E_id, "mms_sent", values);
				}
				break;
			// houyuchun add 20120625 end
			}

			super.handleMessage(msg);
		}

	};
	private void pttTextMessageTipSound() {
		// Play ptt release sound
		// playsound(R.raw.imreceive);
		playMessageAcceptSound(R.raw.imreceive);
	}

	private void playMessageAcceptSound(int id) {
		MediaPlayer mp = null;
		mp = MediaPlayer.create(SipUAApp.mContext, id);
		if (mp != null) {
			mp.start();
		}
	}
}

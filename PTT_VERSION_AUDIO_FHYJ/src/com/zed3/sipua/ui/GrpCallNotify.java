package com.zed3.sipua.ui;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.media.ToneGenerator;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.media.RtpStreamReceiver_group;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.power.MyPowerManager;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.contant.Contants;
import com.zed3.utils.DialogMessageTool;
import com.zed3.window.MyWindowManager;

public class GrpCallNotify extends Activity {

	private static final String TAG = "GrpCallNotify";
	// One group to another
	private final String ACTION_GROUP_2_GROUP = "com.zed3.sipua.ui_groupcall.group_2_group";
	// One group to another
	private final String ACTION_SINGLE_2_GROUP = "com.zed3.sipua.ui_groupcall.single_2_group";

	private TimeCount time;
	// add by hu
	ToneGenerator toneGenerator = null;

	private Timer mTimer = null; // �����ʾ���棬�������3����ͷ�
	private TimerTask mTask;
	private TextView mTextView;
	private TextView mTextView2;
	private Button mButtonOk;
	private Button mButtonCancel;
	private final int CLOSE_TIMER = 1;
	private boolean mClicked = false;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CLOSE_TIMER:
				if (!mClicked) {
					String grpId;
					// if (SipUAApp.isLowSdk) {
					grpId = GroupCallUtil.getTalkGrp();
					
					PttGrp currentGroup = Receiver.GetCurUA().GetCurGrp();
					if(currentGroup!=null && currentGroup.grpID.equals(grpId) && Receiver.GetCurUA().IsPttMode()){
						Receiver.GetCurUA().answerGroupCall(currentGroup);
					} else {
						PttGrp pttGrp = Receiver.GetCurUA().GetGrpByID(grpId);
						Receiver.GetCurUA().grouphangup(pttGrp);
					}
					
					// } else {
					// grpId = GroupCall.getTalkGrp();
					// }
					
				}
				// add by hu
				if (toneGenerator != null) {
					toneGenerator.stopTone(); // ֹͣ����
					toneGenerator.release();
					toneGenerator = null;
				}

				finish();
				break;
			}
		}
	};

	private TimerTask CreateTask() {
		mTask = new TimerTask() {
			public void run() {
				Message message = new Message();
				message.what = CLOSE_TIMER;
				handler.sendMessage(message);
			}
		};
		return mTask;
	}

	@Override
	protected void onResume() {
		Receiver.engine(this);
		super.onResume();
		mTextView = (TextView) findViewById(R.id.notify_content);
		String grpId = /* SipUAApp.isLowSdk ? */GroupCallUtil.getTalkGrp();
		/* : GroupCall.getTalkGrp(); */
		PttGrp pttGrp = Receiver.GetCurUA().GetGrpByID(grpId);
		// houyuchun modify 20120621 begin
		String string = this.getResources().getString(
				R.string.notify_message_text);
		String name = pttGrp.grpName;
		mWidth = (int)(240*this.getResources().getDisplayMetrics().density+0.5f);
		String message = DialogMessageTool.getString(mWidth, mTextView.getTextSize(), name+" "+string);
		mTextView.setText(message);
//		mTextView.setText(name +" "+ string);
		// houyuchun modify 20120621 end
	}

	private PowerManager.WakeLock mWakelock = null;
	private int mWidth;
	private String mScreanWakeLockKey = TAG;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ���ô�������->�ޱ���
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//������Ļ
		mScreanWakeLockKey = MyPowerManager.getInstance().wakeupScreen(TAG);
		//�������
		MyWindowManager.getInstance().disableKeyguard(this);
		setContentView(R.layout.groupcall_notify);
		// add by hu �������л�����
		// Receiver.GetCurUA().GroupChangeTipSound();
		try {
			int streamType = -1;
			if(Receiver.GetCurUA().IsPttMode()){
				streamType = RtpStreamReceiver_group.stream();
			}else{
				streamType = RtpStreamReceiver_signal.stream();
			}
			toneGenerator = new ToneGenerator(streamType, 100);
		} catch (RuntimeException e) {
			toneGenerator = null;
		}

		if (toneGenerator != null) {
			toneGenerator.startTone(ToneGenerator.TONE_SUP_CALL_WAITING); // ��ʼ���ţ����еȴ�������ʾ��
			try {
				Thread.sleep(1000); // ����ʱ����ƣ�1����
			} catch (InterruptedException e) {
			}
		}
		time = new TimeCount(7000, 1000);//

		time.start();

		mButtonOk = (Button) findViewById(R.id.button_ok);

		mClicked = false;

		mTextView2 = (TextView) findViewById(R.id.textView2);

		mButtonOk.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mClicked = true;
				String action = /* SipUAApp.isLowSdk ? */GroupCallUtil
						.getActionMode() /* : GroupCall.getActionMode() */;
				if (action.equalsIgnoreCase(ACTION_GROUP_2_GROUP)) {

					String grpId = /* SipUAApp.isLowSdk ? */GroupCallUtil
							.getTalkGrp() /* : GroupCall.getTalkGrp() */;
					PttGrp pttGrp = Receiver.GetCurUA().GetGrpByID(grpId);
					Receiver.GetCurUA().answerGroupCall(pttGrp);
				} else if (action.equalsIgnoreCase(ACTION_SINGLE_2_GROUP)) {
					// Modify by zzhan 2013-5-27
					// Receiver.GetCurUA().hangup();
					
					String grpId = /* SipUAApp.isLowSdk ? */GroupCallUtil
							.getTalkGrp() /* : GroupCall.getTalkGrp() */;
					
					Receiver.GetCurUA().hangupWithoutRejoin();
					PttGrp pttGrp = Receiver.GetCurUA().GetGrpByID(grpId);
					
					Receiver.GetCurUA().answerGroupCall(pttGrp);
				}
				// add by oumogang 2013-05-10
				// �Ƿ���Ҫ�������б�
				sendBroadcast(new Intent(Contants.ACTION_CURRENT_GROUP_CHANGED));

				finish();
			}

		});

		mButtonCancel = (Button) findViewById(R.id.button_cancel);
		mButtonCancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mClicked = true;
				String grpId = /* SipUAApp.isLowSdk ? */GroupCallUtil
						.getTalkGrp() /* : GroupCall.getTalkGrp() */;
				
				PttGrp currentGroup = Receiver.GetCurUA().GetCurGrp();
				if(currentGroup!=null && currentGroup.grpID.equals(grpId)  && Receiver.GetCurUA().IsPttMode()){
					
					Receiver.GetCurUA().answerGroupCall(currentGroup);
					
				} else {
					PttGrp pttGrp = Receiver.GetCurUA().GetGrpByID(grpId);
					Receiver.GetCurUA().grouphangup(pttGrp);
				}
				
				finish();
			}

		});
		mTimer = new Timer();
		mTimer.schedule(CreateTask(), 8000);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// add by hu
		if (toneGenerator != null) {
			toneGenerator.stopTone(); // ֹͣ����
			toneGenerator.release();
			toneGenerator = null;
		}
		MyPowerManager.getInstance().releaseScreenWakeLock(mScreanWakeLockKey);
		MyWindowManager.getInstance().reenableKeyguard(this);
	}

	/* ����һ������ʱ���ڲ��� */
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// ��������Ϊ��ʱ��,�ͼ�ʱ��ʱ����
		}

		@Override
		public void onFinish() {// ��ʱ���ʱ����
			mTextView2.setText("(0)");
			// checking.setClickable(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {// ��ʱ������ʾ
			mTextView2.setText("(" + millisUntilFinished / 1000 + ")");
		}
	}
	
	public static void startSelf(Intent intent){
		Context context = SipUAApp.getAppContext();
		Intent startActivity = new Intent(intent);
    	startActivity.setClass(context,GrpCallNotify.class);
	    startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    context.startActivity(startActivity); 
	}
	
}

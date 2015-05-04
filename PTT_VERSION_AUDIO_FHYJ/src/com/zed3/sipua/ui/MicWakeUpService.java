package com.zed3.sipua.ui;

import org.zoolu.tools.MyLog;

import com.zed3.sipua.R;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

//播放音频文件 一直播放的 
public class MicWakeUpService extends Service {

	private final String TAG = "MicWakeUpService";
	MediaPlayer mAudioMediaPlayer = null;

	long time = 0, curTime = 0;
	boolean flag = false;

	// final int sepTime = 10000;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		PlayAudio();

		time = System.currentTimeMillis();

		Thread td = new Thread(new Runnable() {
			@Override
			public void run() {

				while (!flag) {

					curTime = System.currentTimeMillis();

					if (curTime - time > 10000)// 10秒
					{
						MyLog.e(TAG, "is 10 second...?");
						if (mAudioMediaPlayer != null)
							mAudioMediaPlayer.start();
						time = System.currentTimeMillis();
					}
					
					try {
						Thread.sleep(1000);//1秒
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}

			}
		});
		td.start();

		// myHandler.sendMessageDelayed(myHandler.obtainMessage(1), sepTime);

	}

	// Handler myHandler=new Handler(){
	// public void handleMessage(android.os.Message msg) {
	// if(msg.what==1)
	// {
	// MyLog.e(TAG, "is 10 second...?");
	// if (mAudioMediaPlayer != null)
	// mAudioMediaPlayer.start();
	// myHandler.sendMessageDelayed(myHandler.obtainMessage(1), sepTime);
	// }
	// };
	// };

	@Override
	// 可执行多次
	public void onStart(Intent intent, int startId) {

	}

	private void PlayAudio() {
		try {
			MyLog.e(TAG, "PlayAudio");
			mAudioMediaPlayer = MediaPlayer.create(this, R.raw.imreceive);
			mAudioMediaPlayer.setVolume(0, 0);
			// mAudioMediaPlayer.setLooping(true);//循环播放

			mAudioMediaPlayer.start();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MyLog.e(TAG, e.toString());
		}
	}

	private void ReleaseMediaPlayer() {
		flag = true;

		// if(myHandler.hasMessages(1))
		// myHandler.removeMessages(1);

		if (mAudioMediaPlayer != null) {
			mAudioMediaPlayer.release();
			mAudioMediaPlayer = null;
		}
		MyLog.e(TAG, "Releasing media ReleaseMediaPlayer.");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		ReleaseMediaPlayer();
		MyLog.e(TAG, "TestService stop");

	};

}

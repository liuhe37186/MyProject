package com.zed3.video;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.Toast;

public class SensorCheckService extends Service{
	MyOrientationEventListener listener;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		listener = new MyOrientationEventListener(this);
		if (listener.canDetectOrientation()) {
//			if(DeviceVideoInfo.supportRotate)
				listener.enable();
//			else{
//				listener.disable();
//			}
		} else {
//			Toast.makeText(this, "can't orientation", Toast.LENGTH_SHORT)
//					.show();
		}
	}
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	class MyOrientationEventListener extends OrientationEventListener {
		int lastRotation = 0;

		public MyOrientationEventListener(Context context) {
			super(context/*, rate*/);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			int rotation = 0, curAngle = orientation % 360;
			if ((curAngle >= 0 && curAngle < 45)
					|| (curAngle >= 315 && curAngle < 360)) {
				rotation = 0;
			} else if (curAngle >= 45 && curAngle < 135) {
				rotation = 1;
			} else if (curAngle >= 135 && curAngle < 225) {
				rotation = 2;
			} else {
				rotation = 3;
			}
			if (lastRotation != rotation) {
				Log.e("orientationTest", "value = " + rotation);
				lastRotation = rotation;
				DeviceVideoInfo.curAngle = rotation*90;
				sendBroadcast(new Intent("com.zed3.siupa.ui.restartcamera"));
			}
		}

	}
	@Override
	public void onDestroy() {
		if (listener != null) {
			listener.disable();
		}
		super.onDestroy();
	}
}

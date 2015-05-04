package com.zed3.flow;

import com.zed3.sipua.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;

public class NetworkFlow extends Activity{
	private boolean isAdded = false; // �Ƿ�������������
	private static WindowManager wm;
	private static WindowManager.LayoutParams params;
	private Button btn_floatView;
	/**
	 * ����������
	 */
	private void createFloatView() {
		btn_floatView = new Button(getApplicationContext());
        btn_floatView.setText(R.string.sus_window);
        
        wm = (WindowManager) getApplicationContext()
        	.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        
        // ����window type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        /*
         * �������Ϊparams.type = WindowManager.LayoutParams.TYPE_PHONE;
         * ��ô���ȼ��ή��һЩ, ������֪ͨ�����ɼ�
         */
        
        params.format = PixelFormat.RGBA_8888; // ����ͼƬ��ʽ��Ч��Ϊ����͸��
        
        // ����Window flag
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                              | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        /*
         * �����flags���Ե�Ч����ͬ����������
         * ���������ɴ������������κ��¼�,ͬʱ��Ӱ�������¼���Ӧ��
        wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL
                               | LayoutParams.FLAG_NOT_FOCUSABLE
                               | LayoutParams.FLAG_NOT_TOUCHABLE;
         */
        
        // �����������ĳ��ÿ�
        params.width = 100;
        params.height = 100;
        
        // ������������Touch����
        btn_floatView.setOnTouchListener(new OnTouchListener() {
        	int lastX, lastY;
        	int paramX, paramY;
        	
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = params.x;
					paramY = params.y;
					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					params.x = paramX + dx;
					params.y = paramY + dy;
					// ����������λ��
			        wm.updateViewLayout(btn_floatView, params);
					break;
				}
				return true;
			}
		});
        
        wm.addView(btn_floatView, params);
        isAdded = true;
	}
}

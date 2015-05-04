package org.opencore.H264Decode;

import org.zoolu.tools.MyLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class VideoView extends View {
	Bitmap bitmap;
	int mH264Height;
	int mH264Width;
	float mRotate = 0;
	
	public VideoView(Context paramContext) {
		super(paramContext);
	}

	public VideoView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}
	

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		// TODO Auto-generated method stub
//		if (event.getAction() == MotionEvent.ACTION_DOWN) {
//			SetRotate();
//		}
//		return super.onTouchEvent(event);
//	}
//	
//	// »æÖÆÍ¼Ïñ½Ç¶È
//	public void SetRotate() {
//		if (mRotate == 0) {
//			mRotate = 90;
//		} else {
//			mRotate = 0;
//		}
//	}
	
	protected void onDraw(Canvas paramCanvas) {

		super.onDraw(paramCanvas);
		if (this.bitmap == null) {
			return;
		}

		Bitmap localBitmap = this.bitmap;
		//paramCanvas.drawBitmap(localBitmap, 0, 0, null);
		Rect srcRect = new Rect();
		Rect destRect = new Rect();
		srcRect.set(0, 0, mH264Width, mH264Height);
		destRect.set(0, 0, getWidth(), getHeight());
		Matrix m = new Matrix();
		if (mRotate == 0) {
		m.postScale(1f * getWidth() / mH264Width, 1f * getHeight()
					/ mH264Height);
		} else {
			m.postScale(1f * getHeight() / mH264Width, 1f * getWidth()
					/ mH264Height);
			m.postRotate(90, 0, 0);
			m.postTranslate(getWidth(), 0);
		}
		paramCanvas.drawBitmap(localBitmap, m, null);
	}

	//
	public void setBitmap(Bitmap paramBitmap, int mH264Width,
			int mH264Height) {
		this.bitmap = paramBitmap;
		this.mH264Width = mH264Width;
		this.mH264Height = mH264Height;
		postInvalidate();
	}
}

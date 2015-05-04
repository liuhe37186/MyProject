package com.ffmpeg;

import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.zed3.log.MyLog;

public class LivePreview extends View {
	private static final String LOG_TAG = LivePreview.class.getSimpleName();
	private Bitmap showBitmap;
	private float mRotate =0;//90//180//270
	int gWidth = 0, width = 0;
	int gHeight = 0, height = 0, drawLeft = 0, drawTop = 0;
	Bitmap localBitmap = null;
	Bitmap.Config localConfig = Bitmap.Config.RGB_565;
	private static final boolean DEBUG = true;
	
	final RectF dstR = new RectF();
	final RectF deviceR = new RectF();

	public LivePreview(Context paramContext) {
		super(paramContext);
	}

	public LivePreview(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public LivePreview(Context paramContext, AttributeSet paramAttributeSet,
			int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}
	
//	// 计算图像显示角度的？？
//	public boolean onTouchEvent(MotionEvent event) {
//
//		if (event.getAction() == MotionEvent.ACTION_DOWN) {
//			SetRotate();
//		}
//		return super.onTouchEvent(event);
//	}

	// 绘制图像角度
	public void SetRotate(int rag) {
//		if (mRotate == 0) {
//			mRotate = 90;
//		} else {
//			mRotate = 0;
//		}
		mRotate = rag;
	}	
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (this.showBitmap == null)
			return;
		
		// -----
//		RectF destRectF = new RectF();
		gWidth = this.showBitmap.getWidth();
		gHeight = this.showBitmap.getHeight();

		width = getWidth();
		height = getHeight();
		drawLeft = 0;
		drawTop = 0;
		
		Rect srcRect = new Rect();
		Rect destRect = new Rect();
		srcRect.set(0, 0, gWidth, gHeight);
		destRect.set(0, 0, getWidth(), getHeight());
		
		if (mRotate == 0) {
			canvas.drawBitmap(this.showBitmap, srcRect, destRect,null);
		} else if(mRotate == 90){
			
			onDrawRotate(canvas,90,gWidth,gHeight,getWidth(),getHeight());

		} else if(mRotate == 180){
			canvas.translate(getWidth(),getHeight());
			canvas.rotate(180);
			canvas.drawBitmap(this.showBitmap, srcRect, destRect,null);
		}else if(mRotate == 270){
			
			onDrawRotate(canvas,270,gWidth,gHeight,getWidth(),getHeight());

		}
		
		
		
//		if (gWidth * height > width * gHeight) {
//			// Log.i("@@@", "image too tall, correcting");
//			drawTop = height / 2;
//			height = width * gHeight / gWidth;
//			drawTop = drawTop - height / 2;
//			destRectF.set(drawLeft, drawTop, width, height + drawTop);
//
//		} else if (gWidth * height < width * gHeight) {
//			// Log.i("@@@", "image too wide, correcting");
//			drawLeft = width / 2;
//			width = height * gWidth / gHeight;
//			drawLeft = drawLeft - width / 2;
//			destRectF.set(drawLeft, drawTop, width + drawLeft, height);
//		} else {
//			// Log.i("@@@", "aspect ratio is correct: " +
//			// width+"/"+height+"="+
//			// mVideoWidth+"/"+mVideoHeight);
//			destRectF.set(0, 0, width, height);
//		}

		// http://java-admin.iteye.com/blog/792217
		// 有对角度详细介绍，translate（平移），rotate（旋转），scale（缩放）
		// 缩放尺寸为上面计算好的视频分辨率尺寸。

//		Matrix m = new Matrix();
//		if (mRotate == 0) {
//			m.postScale(1f * getWidth() / gWidth, 1f * getHeight() / gHeight);
//		
//		}
//		else {
//			m.postScale(1f * getHeight() / gWidth, 1f * getWidth() / gHeight);
//			m.postRotate(90, 0, 0);
//			m.postTranslate(/*getWidth()*/100, 0);
//		}
//		paramCanvas.drawBitmap(showBitmap, m, null);

//		paramCanvas.drawBitmap(showBitmap, null, destRectF, null);

		
		
		
		//		// 画线
//		Paint paint = new Paint();
//		paint.setStyle(Style.STROKE);// 设置非填充
//		paint.setStrokeWidth(5);// 笔宽5像素
//		paint.setColor(Color.WHITE);// 设置为红笔
//		paint.setAntiAlias(true);// 锯齿不显示
//		// 左
//		paramCanvas
//				.drawLine(drawLeft + 5, drawTop, drawLeft - 5, height, paint);
//		// 右
//		paramCanvas.drawLine(drawLeft + width + 5, drawTop, width + drawLeft,
//				height, paint);

	}

	private void onDrawRotate(Canvas canvas, int degress, int bitmapWidth, int bitmapHeight, int viewWidth, int viewHeight) {
		
		Matrix m = new Matrix();
		
		float sx = getWidth() / (float) gWidth;
		float sy = getWidth() / (float) gHeight;
		Log.i(LOG_TAG, "sx = " + sx + " , sy = " + sy);
		m.setScale(sx, sy);

		m.postRotate(degress);

		RectF deviceR = new RectF();

		RectF bitmapRect = new RectF();
		bitmapRect.set(0, 0, gWidth, gHeight);

		m.mapRect(deviceR, bitmapRect);

		int neww = Math.round(deviceR.width());
		int newh = Math.round(deviceR.height());

		Log.i(LOG_TAG, "new width = " + neww + " , new height = " + newh);
		Log.i(LOG_TAG, "deviceR.left = " + (-deviceR.left)
				+ " , deviceR.top = " + (-deviceR.top));

		canvas.translate(-deviceR.left, -deviceR.top
				+ (getHeight() / 2 - newh / 2));
		canvas.concat(m);
		
		canvas.drawBitmap(showBitmap, 0, 0, null);
	}
	
	private void drawDebugRedLine(Canvas canvas,float startX,float startY,float stopX,float stopY){
		if(DEBUG) {
			Paint mPaint = new Paint();
			mPaint.setColor(Color.RED);
			mPaint.setAntiAlias(true);
			
			canvas.drawLine(startX,startY,stopX,stopY,mPaint);
		}
		
	}
	
	private void drawDebugRedRect(Canvas canvas,float startX,float startY,float stopX,float stopY){
		if(DEBUG) {
			Paint mPaint = new Paint();
			mPaint.setColor(Color.RED);
			mPaint.setAntiAlias(true);
			
			canvas.drawRect(startX,startY,stopX,stopY,mPaint);
		}
		
	}

	// setup1
	public void updateBitmap(int paramInt1, int paramInt2, byte[] buffer) {

		// BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		// bitmapOptions.inSampleSize = 4;

		MyLog.e("LivePreview", "updateBitmap w:" + paramInt1 + " h:"
				+ paramInt2);

		try {
			localBitmap = Bitmap
					.createBitmap(paramInt1, paramInt2, localConfig);

		} catch (OutOfMemoryError e) {
			while (localBitmap == null) {
				System.gc();
				System.runFinalization();
				localBitmap = Bitmap.createBitmap(paramInt1, paramInt2,
						localConfig);
				MyLog.e("LivePreview", "localBitmap recycle");
			}
		}
		// if (localBitmap != null) {
		localBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(buffer));
		this.showBitmap = localBitmap;

		localBitmap = null;
		// }
		postInvalidate();

	}

	// setup2
	public void updatePixels(int[] paramArrayOfInt) {

		if (this.showBitmap == null)
			return;

		this.showBitmap.setPixels(paramArrayOfInt, 0,
				this.showBitmap.getWidth(), 0, 0, this.showBitmap.getWidth(),
				this.showBitmap.getHeight());
		postInvalidate();

	}

}

package com.zed3.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.media.audiofx.Visualizer;
import android.view.View;

public class BaseVisualizerView extends View implements Visualizer.OnDataCaptureListener{

    private static final int DN_W = 400;
    private static final int DN_H = 160;
    private static final int DN_SL = 5;
    private static final int DN_SW = 6;
    
    private int hgap = 0;
    private int vgap = 0;
    private int levelStep = 0;
    private float strokeWidth = 0;
    private float strokeLength = 0;

    /**
     * It is the max level.
     */
    protected final static int MAX_LEVEL = 9;

    /**
     * It is the cylinder number.
     */
    protected final static int CYLINDER_NUM = 25;//ÁÐ  5µÄ±¶Êý

    /**
     * It is the visualizer.
     */
    protected Visualizer mVisualizer = null;

    /**
     * It is the paint which is used to draw to visual effect. 
     */
    protected Paint mPaint = null;

    /**
     * It is the buffer of fft.
     */
    protected byte[] mData = new byte[CYLINDER_NUM];
    protected byte[] mBackgroudData = new byte[CYLINDER_NUM];

    boolean mDataEn = true;
    /**
     * It constructs the base visualizer view.
     * @param context It is the context of the view owner.
     */
    public BaseVisualizerView(Context context) {
        super(context);
        for (int i = 0; i < CYLINDER_NUM; i ++) {
        	mBackgroudData[i] = MAX_LEVEL+2;
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xFFd60d25);
        mPaint.setStrokeJoin(Join.ROUND);
        mPaint.setStrokeCap(Cap.ROUND);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        float w, h, xr, yr;
        w = right - left;
        h = bottom - top;
        xr = w / (float)DN_W;
        yr = h / (float)DN_H;

        strokeWidth = DN_SW * yr;
        strokeLength = DN_SL * xr;
        hgap = (int)((w - strokeLength * CYLINDER_NUM) / (CYLINDER_NUM + 1) );
        vgap = (int)(h / (MAX_LEVEL + 2));

        mPaint.setStrokeWidth(strokeWidth);
    }

    protected void drawCylinder(Canvas canvas, float x, byte value) {
        if (value < 0) value = 0;
        for (int i = 0; i < value; i++) {
            float y = getHeight() - i * vgap - vgap;
            canvas.drawLine(x, y, x + strokeLength, y, mPaint);
        }
    }
    protected void drawCylinderBackGroud(Canvas canvas, float x, byte value) {
        if (value < 0) value = 0;
        for (int i = 0; i < value; i++) {
            float y = getHeight() - i * vgap - vgap;
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(0xFF60646a);
            paint.setStrokeJoin(Join.ROUND);
            paint.setStrokeCap(Cap.ROUND);
            canvas.drawLine(x, y, x + strokeLength, y, paint);
        }
    }
    @Override
    public void onDraw(Canvas canvas) {
    	
        for (int i = 0; i < CYLINDER_NUM; i ++) {
        	drawCylinderBackGroud(canvas, strokeWidth / 2 + hgap + i * (hgap + strokeLength), mBackgroudData[i]);
            drawCylinder(canvas, strokeWidth / 2 + hgap + i * (hgap + strokeLength), mData[i]);
        }
    }

    /**
     * It sets the visualizer of the view. DO set the viaulizer to null when exit the program.
     * @parma visualizer It is the visualizer to set.
     */
    public void setVisualizer(Visualizer visualizer) {
        if (visualizer != null) {
            if (!visualizer.getEnabled()) {
                visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
            }
            levelStep = 128 / MAX_LEVEL;
            visualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2, false, true);
                
        } else {
            
            if (mVisualizer != null) {
                mVisualizer.setEnabled(false);
                mVisualizer.release();
            }
        }
        mVisualizer = visualizer;
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft,
            int samplingRate) {
        byte[] model = new byte[fft.length / 2 + 1];
        if (mDataEn) {

            model[0] = (byte) Math.abs(fft[1]);  
            int j = 1;  
            for (int i = 2; i < fft.length;) {
                model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);  
                i += 2;  
                j++;  
            }
        } else {
            for (int i = 0; i < CYLINDER_NUM; i ++) {
            }
        }
        for (int i = 0; i < CYLINDER_NUM; i ++) {
            final byte a = (byte)(Math.abs(model[CYLINDER_NUM  - i]) / levelStep);

            final byte b = mData[i];
            if (a > b) {
                mData[i] = a;
            } else {
                if (b > 0) {
                    mData[i]--;
                }
            }
        }
        postInvalidate();
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform,
            int samplingRate) {
        // Do nothing.
    }

    /**
     * It enables or disables the data processs.
     * @param en If this value is true it enables the data process..
     */
    public void enableDataProcess(boolean en) {
        mDataEn = en;
    }

	public void setTimes(int times) {
		if(times <= 0){
			for (int i = 0; i < CYLINDER_NUM; i ++) {
			mData[i] = 0;
					}
			 postInvalidate();
			 return;
		}
		if(times < 30) times += 20;
		times = (MAX_LEVEL-2)*(times-20)/30;
		 for (int i = 0; i < CYLINDER_NUM; i ++) {
			 if(i< CYLINDER_NUM /5){
				 mData[i] = (byte)times;
			 }else if(i < 2*CYLINDER_NUM/5){
				 mData[i] = (byte)(1+times);
			 }else if(i < 3*CYLINDER_NUM/5){
				 mData[i] = (byte)(2+times);
			 }else if(i < 4*CYLINDER_NUM/5){
				 mData[i] = (byte)(1+times);
			 }else{
				 mData[i] = (byte)times;
			 }}
		 postInvalidate();
	}
}

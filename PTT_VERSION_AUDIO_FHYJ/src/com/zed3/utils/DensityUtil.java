package com.zed3.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtil {
	  
    private static DisplayMetrics displayMetrics;

	/** 
     * �����ֻ��ķֱ��ʴ� dp �ĵ�λ ת��Ϊ px(����) 
     */  
    public static int dip2px(Context context, float dpValue) {  
    	if (displayMetrics == null) {
    		displayMetrics = context.getResources().getDisplayMetrics();
		}
        final float scale = displayMetrics.density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * �����ֻ��ķֱ��ʴ� px(����) �ĵ�λ ת��Ϊ dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
    	if (displayMetrics == null) {
    		displayMetrics = context.getResources().getDisplayMetrics();
		}
        final float scale = displayMetrics.density;  
        return (int) (pxValue / scale + 0.5f);  
    }

	public static int getDipWidth(Context context) {
		// TODO Auto-generated method stub
		if (displayMetrics == null) {
    		displayMetrics = context.getResources().getDisplayMetrics();
		}
		
		return px2dip(context, displayMetrics.widthPixels);
	}  
	public static int getDipHeight(Context context) {
		// TODO Auto-generated method stub
		if (displayMetrics == null) {
			displayMetrics = context.getResources().getDisplayMetrics();
		}
		
		return px2dip(context, displayMetrics.heightPixels);
	}  
	public static int getPxWidth(Context context) {
		// TODO Auto-generated method stub
		if (displayMetrics == null) {
			displayMetrics = context.getResources().getDisplayMetrics();
		}
		
		return displayMetrics.widthPixels;
	}  
	public static int getPxHeight(Context context) {
		// TODO Auto-generated method stub
		if (displayMetrics == null) {
			displayMetrics = context.getResources().getDisplayMetrics();
		}
		
		return displayMetrics.heightPixels;
	}  
	
	
    
    
}

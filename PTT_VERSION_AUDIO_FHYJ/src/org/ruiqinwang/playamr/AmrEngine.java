package org.ruiqinwang.playamr;

import android.content.Context;

public class AmrEngine {

	private boolean isRecordRunning = false;
	
	public static AmrEngine engine = new AmrEngine();

	private Context mContext;
	
	private AmrEngine(){
		
	}
	
	public static AmrEngine getSingleEngine(){
		return engine;
	}
	
	public boolean isRecordRunning(){
		return isRecordRunning;
	}
	
	public void startRecording(){
		isRecordRunning = true;
	}
	
	public void stopRecording(){
		isRecordRunning = false;
	}
	
	public void setContext(Context context){
		this.mContext = context;
	}
	
	public Context getContext(){
		return mContext;
	}
}

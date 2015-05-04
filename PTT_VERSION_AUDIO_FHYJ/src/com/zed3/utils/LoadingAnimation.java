package com.zed3.utils;

import android.os.Handler;
import android.text.TextUtils;
import android.widget.TextView;

public final class LoadingAnimation extends Handler implements Runnable {
	
	private String mTextSource;
	private TextView mTarget;
	private String mTextTarget;
	private int mAppendCount;
	
	private int mMaxAppendCount = DEFAULT_MAX_APPEND_COUNT;
	
	static final int LOADING = 0X0001;
	static final int DEFAULT_MAX_APPEND_COUNT = 3;
	
	public LoadingAnimation setAppendCount(int max){
		this.mMaxAppendCount = max;
		return this;
	}
	
	public void startAnimation(TextView textView) {
		if(textView != null){
			initParams(textView);
			postAnimation();
		}
	}
	
	private void postAnimation() {
		postDelayed(this, 500);
	}

	private void initParams(TextView textView) {
		mTarget = textView;
		String text = textView.getText().toString();
		mTextTarget = text;
		mTextSource = text;
	}

	private void appliyTargetText(String text){
		TextView targetView = mTarget;
		if(targetView !=null && !TextUtils.isEmpty(text)) {
			targetView.setText(text);
		}
	}
	
	private String appendLoadingSymbol(String text){
		mTextTarget = text + ".";
		++mAppendCount;
		return mTextTarget;
	}
	
	public void stopAnimation(){
		removeCallbacks(this);
		mTarget = null;
		resetAppendCount();
		mTextSource = null;
		mTextTarget = null;
	}
	
	private void resetAppendCount(){
		mAppendCount = 0;
	}
	
	public boolean isStartAnimation(){
		return (mTarget!=null);
	}

	public void run() {
		if(mAppendCount>=mMaxAppendCount) {
			mAppendCount = 0;
			mTextTarget = mTextSource;
			appliyTargetText(mTextSource);
		} else {
			appliyTargetText(appendLoadingSymbol(mTextTarget));
		}
		postDelayed(this, 500);
	}
	
}
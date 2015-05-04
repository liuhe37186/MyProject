package com.zed3.sipua.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ActvityNotify extends Activity{
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Intent startActivity = new Intent();
    	startActivity.setClass(this,GrpCallNotify.class);
	    startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    	startActivity(startActivity); 
    	finish();
	}
}
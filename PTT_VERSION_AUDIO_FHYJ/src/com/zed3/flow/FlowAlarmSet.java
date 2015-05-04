package com.zed3.flow;

import com.zed3.location.MemoryMg;
import com.zed3.sipua.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class FlowAlarmSet extends Activity {
	private final String sharedPrefsFile = "com.zed3.sipua_preferences";

	ImageButton confirmBtn = null;
	ImageButton cancelBtn = null;
	EditText editTxt = null;
	SharedPreferences mypre = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.flowalarmset);

		confirmBtn = (ImageButton) findViewById(R.id.confrim);
		confirmBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String val = editTxt.getText().toString();
				if (!val.toString().equals("")) {
					
					if (Double.parseDouble(val)*1024*1024 <= MemoryMg.getInstance().User_3GTotal) {
						mypre = getSharedPreferences(sharedPrefsFile,
								Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = mypre.edit();
						MemoryMg.getInstance().User_3GFlowOut = Double
								.parseDouble(editTxt.getText().toString());
						editor.putString("3gflowoutval",
								MemoryMg.getInstance().User_3GFlowOut + "");
						
						editor.commit();
					}
					else
						Toast.makeText(FlowAlarmSet.this, "流量值设置错误", Toast.LENGTH_SHORT).show();
				}

				finish();
			}
		});

		cancelBtn = (ImageButton) findViewById(R.id.cancel);
		cancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		editTxt = (EditText) findViewById(R.id.edittxt);
		
		editTxt.setText(MemoryMg.getInstance().User_3GFlowOut+"");

	}

}

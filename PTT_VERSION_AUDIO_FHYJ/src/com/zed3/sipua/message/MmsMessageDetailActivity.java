package com.zed3.sipua.message;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.sipua.R;

public class MmsMessageDetailActivity extends Activity {

	public static final String MESSAGE_BODY = "body";
	public static final String MESSAGE_PIC_PATH = "pic_path";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_mms_message_detail);
		Intent intent = getIntent();
		
		final String body = intent.getStringExtra(MESSAGE_BODY);
		final String picPath = intent.getStringExtra(MESSAGE_PIC_PATH);
		
		if(body!=null){
			TextView bodyView = (TextView) this.findViewById(R.id.message_body);
			bodyView.setText(body);
		}
		
		if(picPath!=null){
			Bitmap bitmap = getBitmap(picPath);
			if(bitmap == null) {
				Toast.makeText(this, "ͼƬ����", Toast.LENGTH_SHORT).show();
				this.finish();
			}
			ImageView imageView = (ImageView) this.findViewById(R.id.message_pic);
			imageView.setImageBitmap(bitmap);
			imageView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					Intent intent = new Intent(Intent.ACTION_VIEW);
					
					File imageFile = new File(picPath);
					intent.setDataAndType(Uri.fromFile(imageFile), "image/*");
					startActivity(intent);
					
				}
			});
		}
		
		findViewById(R.id.btn_home_photo).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private Bitmap getBitmap(String imageFilePath){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// ͨ�����bitmap��ȡͼƬ�Ŀ�͸�
		Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath,
				options);
		if (bitmap == null) {
			System.out.println("bitmapΪ��");
		}
		float realWidth = options.outWidth;
		float realHeight = options.outHeight;
		System.out.println("��ʵͼƬ�߶ȣ�" + realHeight + "���:"
				+ realWidth);
		// �������ű�
		int scale = (int) ((realHeight > realWidth ? realHeight
				: realWidth) / 100);
		if (scale <= 0) {
			scale = 1;
		}
		options.inSampleSize = scale;
		options.inJustDecodeBounds = false;
		// ע�����Ҫ��options.inJustDecodeBounds ��Ϊ
		// false,���ͼƬ��Ҫ��ȡ�����ġ�
		bitmap = BitmapFactory.decodeFile(imageFilePath, options);
		return bitmap;
	}
	
}

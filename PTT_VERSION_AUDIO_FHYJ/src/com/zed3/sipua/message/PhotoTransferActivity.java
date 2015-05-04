package com.zed3.sipua.message;

import java.io.File;
import java.util.UUID;

import org.zoolu.tools.MyLog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.CursorLoader;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.DensityUtil;
import com.zed3.utils.Tools;

public class PhotoTransferActivity extends BaseActivity implements
		OnClickListener {
	private EditText transfer_edit_content;
	private Context mContext;
	private String imageFilePath;
	private Uri imageFileUri;
	private Dialog dialog;
	private ImageView action_imv;
	private ImageView keyboard_img;
	private TextView photo_send;
	private TextView photo_send_cancel;
	private EditText transfer_edit_num;
	private String toValue;
	private String bodyValue;
	private String TAG = "PhotoTransferActivity";
	private boolean isSendMode = false;
	private int CHOOSE_PICTURE = 88;
	private int TAKE_PICTURE = 888;
	private int returncode;
	private View mRootView;
	private Intent mIntent;
	private int tag = 1;
	String E_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mRootView = getLayoutInflater().inflate(R.layout.aa_photo_transfer,
				null);
		setContentView(mRootView);
		mRootView.setOnClickListener(this);
		mContext = this;
		// getWindow().setSoftInputMode(
		// WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		transfer_edit_content = (EditText) findViewById(R.id.transfer_edit_content);
		transfer_edit_content.setOnClickListener(this);
		transfer_edit_content
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
						100) });
		transfer_edit_content
				.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {

						if (hasFocus) {
							// 此处为得到焦点时的处理内容
							MainActivity.dismissPopupWindow();
						} else {
							// 此处为失去焦点时的处理内容
						}
					}
				});
		action_imv = (ImageView) findViewById(R.id.action_imv);
		action_imv.setOnClickListener(this);
		photo_send = (TextView) findViewById(R.id.photo_send);
		photo_send.setOnClickListener(this);
		photo_send_cancel = (TextView) findViewById(R.id.photo_send_cancel);
		photo_send_cancel.setOnClickListener(this);
		keyboard_img = (ImageView) findViewById(R.id.keyboard_img);
		keyboard_img.setOnClickListener(this);
		transfer_edit_num = (EditText) findViewById(R.id.transfer_edit_num);
		if (!DeviceInfo.defaultrecnum.equals("")) {
			transfer_edit_num.setText(DeviceInfo.defaultrecnum);
		}
		transfer_edit_num
				.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							// 此处为得到焦点时的处理内容
							MainActivity.dismissPopupWindow();
						} else {
							// 此处为失去焦点时的处理内容
						}
					}
				});
		mIntent = this.getIntent();

		if (mIntent != null && mIntent.getStringExtra("action") != null
				&& mIntent.getStringExtra("action").equals("resend")) {
			imageFilePath = mIntent.getStringExtra("attachment").split(":/")[1];
			File imageFile = new File(imageFilePath);
			imageFileUri = Uri.fromFile(imageFile);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 通过这个bitmap获取图片的宽和高&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
			Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath, options);
			if (bitmap == null) {
				System.out.println("bitmap为空");
			}
			float realWidth = options.outWidth;
			float realHeight = options.outHeight;
			System.out.println("真实图片高度：" + realHeight + "宽度:" + realWidth);
			// 计算缩放比&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
			int scale = (int) ((realHeight > realWidth ? realHeight : realWidth) / 100);
			if (scale <= 0) {
				scale = 1;
			}
			options.inSampleSize = scale;
			options.inJustDecodeBounds = false;
			// 注意这次要把options.inJustDecodeBounds 设为
			// false,这次图片是要读取出来的。&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
			bitmap = BitmapFactory.decodeFile(imageFilePath, options);
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			System.out.println("缩略图高度：" + h + "宽度:" + w);
			action_imv.setImageBitmap(bitmap);
			toValue = mIntent.getStringExtra("address");
			transfer_edit_num.setText(toValue);
			bodyValue = mIntent.getStringExtra("body");
			transfer_edit_content.setText(bodyValue);
			E_id = getE_id();
			tag++;
			isSendMode = true;

		}
		// 取得屏幕的显示大小
		// Display currentDisplay = getWindowManager().getDefaultDisplay();
		// int dw = currentDisplay.getWidth();
		// int dh = currentDisplay.getHeight();

		// 对拍出的照片进行缩放
		// BitmapFactory.Options bmpFactoryOptions = new
		// BitmapFactory.Options();
		// bmpFactoryOptions.inJustDecodeBounds = true;
		// Bitmap bmp =
		// BitmapFactory.decodeFile(this.getIntent().getStringExtra("path")
		// );

		// int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
		// / (float) dh);
		// int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
		// / (float) dw);
		//
		// if (heightRatio > 1 && widthRatio > 1) {
		//
		// if (heightRatio > widthRatio) {
		//
		// bmpFactoryOptions.inSampleSize = heightRatio;
		// } else {
		// bmpFactoryOptions.inSampleSize = widthRatio;
		// }
		//
		// }
		// imv.setImageBitmap(bmp);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Tools.exitApp(this);
			this.finish();
			// sendBroadcast(new
			// Intent("com.zed3.sipua.exitActivity").putExtra("exit", true));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.photo_send_cancel:
			transfer_edit_content.setText("");
			action_imv.setImageResource(R.drawable.aa_photo_selector);
			isSendMode = false;
			break;
		case R.id.action_imv:
			if (!isSDCard()) {
				MyToast.showToast(true, mContext, getResources().getString(R.string.sd_notify));
			}
			// if (isSendMode) {
			// dialog = new MyDialog(mContext, R.style.MyDialog);
			// dialog.show();
			// dialog.setCanceledOnTouchOutside(true);
			//
			// } else {

			// if(dialog!=null){
			// dialog.dismiss();
			// dialog = null;
			// }
			dialog = new MyDialog(mContext, R.style.MyDialog);
			dialog.show();
			dialog.setCanceledOnTouchOutside(true);

			// }
			break;
		case R.id.photo_send:
			if (!isSDCard()) {
				MyToast.showToast(true, mContext, getResources().getString(R.string.sd_notify));
			}
			if (isSendMode) {
				toValue = transfer_edit_num.getText().toString().trim();
				bodyValue = transfer_edit_content.getText().toString().trim();
				if (toValue == null || toValue.length() == 0) {
					MyToast.showToast(true, mContext, getResources().getString(R.string.enter_ds_number));
					return;
				}
				// MessageSender sender = new MessageSender(mContext, 3000,
				// "大家好！我测试彩信", uri, "image/jpg", imageFilePath);
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						MessageSender sender = new MessageSender(mContext,
								toValue, bodyValue, imageFileUri, "image/jpg",
								E_id.substring(3, 12) + ".jpg", E_id);
						sender.sendMultiMessage();

					}
				}).start();
				// if(tag!=1){
				// Intent intent = new Intent(mContext,
				// PhotoTransferSentActivity.class);
				// startActivity(intent);
				// }
				// if(returncode==CHOOSE_PICTURE){
				// MessageSender sender = new MessageSender(mContext, toValue,
				// bodyValue, imageFileUri, "image/jpg", E_id.substring(3,
				// 12) + ".jpg", E_id);
				// sender.sendMultiMessage();
				// }else{
				// MessageSender sender = new MessageSender(mContext, toValue,
				// bodyValue, imageFileUri, "image/jpg", E_id.substring(3,
				// 12) + ".jpg", E_id);
				// sender.sendMultiMessage();
				// }

				transfer_edit_content.setText("");
				action_imv.setImageResource(R.drawable.aa_photo_selector);
				isSendMode = false;
			} else {
				MyToast.showToast(true, mContext, getResources().getString(R.string.upload_notify_1));
			}
			break;
		case R.id.keyboard_img:
			showPop();
			break;
		}
	}
	private static PopupWindow Setting_Transfer_View = null;
	private View PopupView;
	private ScaleAnimation sa;
	private void showPop(){
		PopupView = View.inflate(mContext, R.layout.photo_fun_list,
				null);
		
		
		Setting_Transfer_View = new PopupWindow(PopupView, 

				DensityUtil.dip2px(mContext, 100), 
				DensityUtil.dip2px(mContext, 120));
		
		PopupView.findViewById(R.id.popup_send).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismissPop();
				Intent keyIntent = new Intent(mContext,
				PhotoTransferSentActivity.class);
				startActivity(keyIntent);
			}
		});
		
		PopupView.findViewById(R.id.popup_receive).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismissPop();
				Intent keyIntent = new Intent(mContext,
				PhotoTransferReceiveActivity.class);
				startActivity(keyIntent);
			}
		});
		
		Setting_Transfer_View.setOutsideTouchable(true);
	
		Setting_Transfer_View.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		sa = new ScaleAnimation(-0.5f, 1.0f, -0.5f, 0.1f);
		sa.setDuration(200);

		View view = this.findViewById(R.id.li_photo);
		
		Setting_Transfer_View.showAtLocation(mRootView, Gravity.RIGHT|Gravity.TOP,0, view.getHeight());
				PopupView.startAnimation(sa);
	}

	private void dismissPop(){
		if(Setting_Transfer_View!=null){
			Setting_Transfer_View.dismiss();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		returncode = requestCode;
		if (resultCode == -1) {
			if (requestCode == TAKE_PICTURE) {
				try {
					// Uri uri = imageFileUri;
					// Bitmap bitmap = null;
					// byte[] b = null;
					// ImageUtils utils = new ImageUtils(this);
					// utils.decodeBoundsInfo(uri);
					// // 压缩图片，获取压缩后的图片byte
					// b = utils.getResizedImageData(
					// utils.getMaxImageWidth(),
					// utils.getMaxImageHeight(),
					// utils.getMaxMessageSize() - utils.MESSAGE_OVERHEAD,
					// uri);
					// if(b == null) {
					// MyLog.e(TAG, "get resize image fail");
					// return;
					// }
					// bitmap = utils.Bytes2Bimap(b);
					// if(bitmap == null) {
					// MyLog.e(TAG, "get bitmap from byte fail");
					// return;
					// }
					// // Uri uri = data.getData();
					//
					// // Bitmap bitmap =
					// BitmapFactory.decodeFile(imageFilePath);
					// // Bitmap bitmap =
					// BitmapFactory.decodeFile(uri.getPath());
					// action_imv.setImageBitmap(bitmap);
					// isSendMode = true;
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					// 通过这个bitmap获取图片的宽和高&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
					Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath,
							options);
					if (bitmap == null) {
						System.out.println("bitmap为空");
					}
					float realWidth = options.outWidth;
					float realHeight = options.outHeight;
					System.out.println("真实图片高度：" + realHeight + "宽度:"
							+ realWidth);
					// 计算缩放比&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
					int scale = (int) ((realHeight > realWidth ? realHeight
							: realWidth) / 100);
					if (scale <= 0) {
						scale = 1;
					}
					options.inSampleSize = scale;
					options.inJustDecodeBounds = false;
					// 注意这次要把options.inJustDecodeBounds 设为
					// false,这次图片是要读取出来的。&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
					bitmap = BitmapFactory.decodeFile(imageFilePath, options);
					int w = bitmap.getWidth();
					int h = bitmap.getHeight();
					System.out.println("缩略图高度：" + h + "宽度:" + w);
					action_imv.setImageBitmap(bitmap);
					isSendMode = true;
					Intent intent = new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					Uri uri = Uri.fromFile(new File(imageFilePath));
					intent.setData(uri);
					mContext.sendBroadcast(intent);
				} catch (Exception e) {
					// TODO: handle exception
					MyLog.e(TAG, e.toString() + "bitmap decode fail");
					e.printStackTrace();
				}
			} else if (requestCode == CHOOSE_PICTURE) {
				// 照片的原始资源地址
				Uri originalUri = data.getData(); // 获得图片的uri
				MyLog.e("PICTURE", originalUri.toString());
				if (originalUri.toString().contains("file")) {
					MyToast.showToast(true, mContext, getResources().getString(R.string.upload_notify_2));
					return;
				}
				String[] proj = { MediaStore.Images.Media.DATA };
				// 好像是android多媒体数据库的封装接口，具体的看Android文档
				// Cursor cursor = managedQuery(originalUri, proj, null, null,
				// null);
				// 按我个人理解 这个是获得用户选择的图片的索引值
				// int column_index = cursor
				// .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				// 将光标移至开头 ，这个很重要，不小心很容易引起越界
				// cursor.moveToFirst();
				// // 最后根据索引值获取图片路径
				// String imageFilePath = cursor.getString(column_index);

				// modify by liangzhang 2014-07-10
				// 使用CurserLoader加载Curser
				CursorLoader cursorLoader = new CursorLoader(this, originalUri,
						proj, null, null, null);
				Cursor cursor = cursorLoader.loadInBackground();
				// 按我个人理解 这个是获得用户选择的图片的索引值
				int column_index = cursor.getColumnIndex(proj[0]);
				// 将光标移至开头 ，这个很重要，不小心很容易引起越界
				cursor.moveToFirst();
				// 最后根据索引值获取图片路径
				String imageFilePath = cursor.getString(column_index);
				System.out.println("----imageFilePath>>" + imageFilePath);
				File imageFile = new File(imageFilePath);
				imageFileUri = Uri.fromFile(imageFile);
				System.out.println("----imageFileUri>>"
						+ imageFileUri.toString());
				// 对图片路径中带中文的Uri进行解码，否则系统找不到资源
				imageFileUri = Uri.parse(Uri.decode("file://" + imageFilePath));
				System.out.println("----imageFileUri>>"
						+ imageFileUri.toString());
				try {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					// 通过这个bitmap获取图片的宽和高&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
					Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath,
							options);
					float realWidth = options.outWidth;
					float realHeight = options.outHeight;
					System.out.println("真实图片高度：" + realHeight + "宽度:"
							+ realWidth);
					// 计算缩放比&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
					int scale = (int) ((realHeight > realWidth ? realHeight
							: realWidth) / 50);
					if (scale <= 0) {
						scale = 1;
					}
					options.inSampleSize = 2;
					options.inJustDecodeBounds = false;
					// 注意这次要把options.inJustDecodeBounds 设为
					// false,这次图片是要读取出来的。&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
					bitmap = BitmapFactory.decodeFile(imageFilePath, options);
					int w = bitmap.getWidth();
					int h = bitmap.getHeight();
					System.out.println("缩略图高度：" + h + "宽度:" + w);
					action_imv.setImageBitmap(bitmap);
					isSendMode = true;
					// Intent intent = new Intent(
					// Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					// Uri uri = Uri.fromFile(new File(imageFilePath));
					// intent.setData(uri);
					// mContext.sendBroadcast(intent);
				} catch (Exception e) {
					// TODO: handle exception
					MyLog.e(TAG, e.toString() + "bitmap decode fail");
					e.printStackTrace();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public class MyDialog extends Dialog implements
			android.view.View.OnClickListener {
		Context context;
		TextView tv_1;
		TextView tv_2;

		public MyDialog(Context context) {
			super(context);
			this.context = context;
		}

		public MyDialog(Context context, int theme) {
			super(context, theme);
			this.context = context;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO
			// Auto-generated
			// method stub
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.aa_dialog);
			tv_1 = (TextView) findViewById(R.id.take_photo);
			tv_2 = (TextView) findViewById(R.id.take_picture);
			// tv_3 = (TextView) findViewById(R.id.take_picture_cancel);
			tv_1.setOnClickListener(this);
			tv_2.setOnClickListener(this);
			// tv_3.setOnClickListener(this);
			// }}
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// switch (v.getId()){
			// case R.id.take_photo:
			// break;
			// case R.id.take_picture:
			// break;
			// }
			if (v.getId() == R.id.take_photo) {
				// 图片存入地址

				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
				File file_dir = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/smsmms");
				if (!file_dir.exists()) {
					file_dir.mkdirs();
				}
				E_id = getE_id();
				imageFilePath = Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ "/smsmms/"
						+ E_id.substring(3, 12)
						+ ".jpg";
				File imageFile = new File(imageFilePath);
				imageFileUri = Uri.fromFile(imageFile);

				Intent take_pic_intent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				take_pic_intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
				((Activity) mContext).startActivityForResult(take_pic_intent,
						TAKE_PICTURE);
				return;
			} else if (v.getId() == R.id.take_picture) {
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
				E_id = getE_id();
				// Intent openAlbumIntent = new
				// Intent(Intent.ACTION_GET_CONTENT);
				// openAlbumIntent.setType("image/*");
				// 换一种打开相册的方式 modify by liangzhang 2014-07-10
				// 各种ACTION的不同需要研究。。。。
				Intent openAlbumIntent = new Intent(Intent.ACTION_PICK,
						Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
				return;
			}
		}
	}

	public String getE_id() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		return uuid;

		// byte[] buf = new byte[32];
		// byte[] temp;
		// // ip_address: 本机IP 地址
		// String ip_address = getLocalIpAddress();
		// if(ip_address == null || ip_address.equals("")) {
		// MyLog.i(TAG, "getLocalIpAddress fail, ip_address = "+ip_address);
		// Toast.makeText(mContext, "未注册成功", Toast.LENGTH_LONG).show();
		// return null;
		// }
		// String[] ip_spilt = ip_address.split("[.]");
		// // ip_format: 格式化IP地址 ，格式为"%08x"
		// String ip_format = String.format("%02x%02x%02x%02x",
		// Integer.parseInt(ip_spilt[0]), Integer.parseInt(ip_spilt[1]),
		// Integer.parseInt(ip_spilt[2]), Integer.parseInt(ip_spilt[3]));
		// temp = ip_format.getBytes();
		// System.arraycopy(temp, 0, buf, 0, temp.length);
		// // time_format: 格式化UNIX 时间，格式为"%010d"
		// // String time_format = String.format("%010d",
		// GpsTools.TimeToUnix());
		// // temp = time_format.getBytes();
		// System.arraycopy(temp, 0, buf, 8, temp.length);
		// // 随机数
		// String random_str = getRandomString(14);
		// temp = random_str.getBytes();
		// System.arraycopy(temp, 0, buf, 18, temp.length);
		// return new String(buf);
	}

	private boolean isSDCard() {
		String status = Environment.getExternalStorageState();
		return status.equals(Environment.MEDIA_MOUNTED);
	}
	
}

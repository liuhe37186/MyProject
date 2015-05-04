package com.zed3.sipua.message;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.asynctask.LoadImageAsyncTask;
import com.zed3.asynctask.LoadImageAsyncTask.LoadImageCallback;
import com.zed3.bitmap.BitmapUtil;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.utils.LogUtil;

public class PhotoTransferReceiveActivity extends Activity {

	private static final String LOG_TAG = PhotoTransferActivity.class.getSimpleName();
	
	private ListView mPhotoReceiveListView;
	private PhotoReceiveAdater mPhotoReceiveAdapter = new PhotoReceiveAdater();
	private InnerHanler mInnerHanler = new InnerHanler();
	
	
	private static final int ON_DATASET_CHANGED = 0x0001;
	private static final int ON_DATASET_LOADED = 0x0002;
	private static final int ON_CLEAR_DATASET = 0x0004;
	
	public static final String ACTION_READ_MMS = "com.zed3.action.READ_MMS";
	public static final String ACTION_RECEIVE_MMS = "com.zed3.action.RECEIVE_MMS";
	
	
	private final class InnerHanler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			if(ON_DATASET_CHANGED==what){
				Object obj = msg.obj;
				if(obj!=null){
					PhotoReceiveMessage message = (PhotoReceiveMessage) obj;
					mPhotoReceiveAdapter.addItem(message);
					showNoDataTip();
					mPhotoReceiveAdapter.notifyDataSetChanged();
				}
			} else if(ON_DATASET_LOADED==what){
				showNoDataTip();
				mPhotoReceiveAdapter.notifyDataSetChanged();
			} else if(ON_CLEAR_DATASET==what){
				mPhotoReceiveAdapter.getList().clear();
				mPhotoReceiveAdapter.notifyDataSetChanged();
				showNoDataTip();
			}
		}

	}
	TextView none_photo_transfer;
	private void showNoDataTip(){
		if(none_photo_transfer!=null){
			if(mPhotoReceiveAdapter.getCount()==0){
				none_photo_transfer.setVisibility(View.VISIBLE);
			} else {
				none_photo_transfer.setVisibility(View.GONE);
			}
		}
	}
	
	public void sendReadMms(){
		Intent intent = new Intent(ACTION_READ_MMS);
		sendBroadcast(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "PhotoTransferReceiveActivity#onCreate enter");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_photo_transfer_receive);
		
		sInstance = this;
		
		updateMmsStatus();
		
		sendReadMms();
		
		mPhotoReceiveListView = (ListView) this.findViewById(R.id.photo_transfer_receive_lstview);
		mPhotoReceiveListView.setAdapter(mPhotoReceiveAdapter);
		mPhotoReceiveListView.setOnScrollListener(mPhotoReceiveAdapter.getOnScrollListener());
		none_photo_transfer = (TextView) findViewById(R.id.none_photo_transfer);
		mPhotoReceiveListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Object obj = mPhotoReceiveAdapter.getItem(arg2);
				
				if(obj!=null){
					PhotoReceiveMessage message = (PhotoReceiveMessage) obj;
							
					String imageFilePath = message.mPhotoPath;
					
					
					Intent intent = new Intent(PhotoTransferReceiveActivity.this,MmsMessageDetailActivity.class);
					intent.putExtra(MmsMessageDetailActivity.MESSAGE_BODY, message.mBody);
					intent.putExtra(MmsMessageDetailActivity.MESSAGE_PIC_PATH, imageFilePath);
					
					startActivity(intent);
					
				}
				
			}
		});
		
		mPhotoReceiveListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				showSelectDialog(getString(R.string.options),arg2);
				
				return true;
			}
		});
		
		findViewById(R.id.btn_home_photo).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		loadData(sInstance);
		
		Log.i(LOG_TAG, "PhotoTransferReceiveActivity#onCreate exit");
		
	}
	
	private void updateMmsStatus() {
		SmsMmsDatabase database = new SmsMmsDatabase(this);
		ContentValues CV = new ContentValues();
		CV.put("status", 1);
		database.update(SmsMmsDatabase.TABLE_MESSAGE_TALK, "type = 'mms' and mark = 0 and status = 0", CV);
	}

	private void loadData(final Context context) {
		AsyncTask<Void, Void, Void> loadDataTask = new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected Void doInBackground(Void... params) {
				
				SmsMmsDatabase database = new SmsMmsDatabase(context);
				Cursor c = database.mQuery(SmsMmsDatabase.TABLE_MESSAGE_TALK, "type = 'mms' and mark = 0", null, "date desc");
				ArrayList<PhotoReceiveMessage> list = new ArrayList<PhotoTransferReceiveActivity.PhotoReceiveMessage>();
				while(c.moveToNext()) {
					
					String E_id = c.getString(c.getColumnIndex("E_id"));
					String body = c.getString(c.getColumnIndex("body"));
					String attachment = c.getString(c.getColumnIndex("attachment"));
					String status = c.getString(c.getColumnIndex("status"));
					String sipName = c.getString(c.getColumnIndex("sip_name"));
					String type = c.getString(c.getColumnIndex("type"));
					String mark = c.getString(c.getColumnIndex("mark"));
					String attachmentName = c.getString(c.getColumnIndex("attachment_name"));
					String date = c.getString(c.getColumnIndex("date"));
					
					Log.i(LOG_TAG, "attachment name = " + attachmentName + " , " +
							"date = " + date + " , " +
							"E_id = " + E_id + " , " +
							"body = " + body + " , " +
							"attachment = " + attachment + " , " +
							"status = " + status + " , " +
							"type = " + type + " , " +
							"mark = " + mark );
					
					PhotoReceiveMessage message = new PhotoReceiveMessage();
					
					message.mEId = E_id;
					message.mBody = body;
					message.mPhotoPath = attachment;
					message.mSipName = sipName;
					message.mReceiveTime = date;
					
					//add to last.
//					list.add(message);
					list.add(list.size(),message);
					
				}
				mPhotoReceiveAdapter.setList(list);
				mInnerHanler.sendEmptyMessage(ON_DATASET_LOADED);
				
				return null;
			}
		};
		
		loadDataTask.execute();
	}

	private void testAdapter(){
		new Handler().postDelayed(new Runnable() {

			public void run() {
				Log.i("dwtag", "run enter");
				
				ArrayList<PhotoReceiveMessage> list = new ArrayList<PhotoTransferReceiveActivity.PhotoReceiveMessage>();
				
				for(int i = 0;i < 10;i++){
					PhotoReceiveMessage message = new PhotoReceiveMessage();
					message.mBody = "body = " + i;
					message.mPhotoPath = "/sdcard/smsmms/1405310837171.jpg";
					list.add(message);
				}
				
				mPhotoReceiveAdapter.setList(list);
				
				mInnerHanler.sendEmptyMessage(PhotoTransferReceiveActivity.ON_DATASET_LOADED);

				Log.i("dwtag", "run exit");
			}
		}, 2*1000);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.i(LOG_TAG, "PhotoTransferReceiveActivity#onDestroy enter");
		
		sInstance = null;
		
		Log.i(LOG_TAG, "PhotoTransferReceiveActivity#onDestroy exit");
		
	}

	
	public class PhotoReceiveAdater extends BaseAdapter {
		// 软引用的集合,软引用集合里面存放的是 bitmap的类型
		private Map<String, SoftReference<Bitmap>> bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
		protected static final String TAG = "PhotoReceiveAdater";
		private ArrayList<PhotoReceiveMessage> mList = new ArrayList<PhotoTransferReceiveActivity.PhotoReceiveMessage>();
		
		public View newView(Context context, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = LayoutInflater.from(context).inflate(
					R.layout.aa_transfer_item, null);
			ViewHolder holder = new ViewHolder();
			holder.item_transfer_tv_content = (TextView) view
					.findViewById(R.id.item_transfer_tv_content);
			holder.item_transfer_tv_time = (TextView) view
					.findViewById(R.id.item_transfer_tv_time);
			holder.item_transfer_imv = (ImageView) view
					.findViewById(R.id.item_transfer_imv);
			holder.item_transfer_tv_person = (TextView) view
					.findViewById(R.id.item_transfer_tv_person);
			holder.item_transfer_tv_sent = (TextView) view
					.findViewById(R.id.item_transfer_tv_sent);
			view.setTag(holder);
			
			holder.item_transfer_tv_sent.setVisibility(View.GONE);
			
			return view;
		}

		public void addItem(PhotoReceiveMessage message) {
			mList.add(message);
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		public void setList(ArrayList<PhotoReceiveMessage> list){
			if(list!=null){
				mList = list;
			}
		}
		
		public ArrayList<PhotoReceiveMessage> getList(){
			return mList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView==null) {
				convertView = newView(PhotoTransferReceiveActivity.this,parent);
			}
			
			PhotoReceiveMessage message = mList.get(position);
			
			ViewHolder holder = (ViewHolder) convertView.getTag();
			
			holder.item_transfer_tv_content.setText(message.mBody);
			holder.item_transfer_tv_time.setText(message.mReceiveTime);
			String showText = GroupListUtil.getUserName(message.mSipName);
			if(TextUtils.isEmpty(showText)) {
				showText = message.mSipName;
			}
			holder.item_transfer_tv_person.setText(getResources().getString(R.string.photo_receive) + showText);			
			String attachment = message.mPhotoPath;
			
			//set empty bitmap and load realiamge by AsyncTask.modify by mou 2015-01-24
			holder.item_transfer_imv.setTag(attachment);
			holder.item_transfer_imv.setImageBitmap(null);
			String[] split = attachment.split("://");
			final String attachmentPath = split[split.length == 1?0:1];
			loadImage(holder,attachmentPath);
//			try {
//				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inJustDecodeBounds = true;
//				// 通过这个bitmap获取图片的宽和高&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
//				Bitmap bitmap = BitmapFactory.decodeFile(attachment, options);
//				if (bitmap == null)
//				{
//					System.out.println("bitmap为空");
//				}
//				float realWidth = options.outWidth;
//				float realHeight = options.outHeight;
//				System.out.println("真实图片高度：" + realHeight + "宽度:" + realWidth);
//				int scale;
//				// 计算缩放比&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
//				if(realWidth==0||realHeight==0){
//					holder.item_transfer_imv.setImageBitmap(null);
//					
//				}else{
//					scale = (int) ((realHeight > realWidth ? realHeight : realWidth) / 200);
//					if (scale <= 0)
//					{
//						scale = 1;
//					}
//					options.inSampleSize = scale;
//				}
//				
//				options.inJustDecodeBounds = false;
//				// 注意这次要把options.inJustDecodeBounds 设为 false,这次图片是要读取出来的。&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
//				bitmap = BitmapFactory.decodeFile(attachment, options);
//				int w = bitmap.getWidth();
//				int h = bitmap.getHeight();
//				holder.item_transfer_imv.setImageBitmap(bitmap);
//				System.out.println("缩略图高度：" + h + "宽度:" + w);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			return convertView;
		}
		protected boolean flinging;
		protected boolean scrolling;
		OnScrollListener onScrollListener = new OnScrollListener() {
			
			private int firstVisibleItem;
			private int visibleItemCount;
			private int totalItemCount;
			StringBuilder builder = new StringBuilder();
			private void clearBuilder() {
				// TODO Auto-generated method stub
				if (builder.length()>0) {
					builder.delete(0, builder.length());
				}
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				clearBuilder();
				builder.append("OnScrollListener#onScrollStateChanged()");
				ListView lv = (ListView) view;
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_FLING:
					builder.append(" SCROLL_STATE_FLING");
					flinging = true;
					break;
				case OnScrollListener.SCROLL_STATE_IDLE:
					builder.append(" SCROLL_STATE_IDLE");
					LogUtil.makeLog(TAG, builder.toString());
					flinging = false;
					scrolling = false;
					int position = lv.getFirstVisiblePosition();
					int count = visibleItemCount;
					builder.append(" getFirstVisiblePosition()/count "+position+"/"+visibleItemCount);
					// 获取listview 里面的某个孩子的view对象lv.getChildAt(index);
//					int count = lv.getChildCount();
					builder.append(" firstVisibleItem/visibleItemCount/totalItemCount : "+firstVisibleItem+"/"+visibleItemCount+"/"+totalItemCount);
					for (int i = 0; i < visibleItemCount; i++) {
						int realPostion  = firstVisibleItem+i;
						builder.append(" realPostion "+realPostion);
						View v  = lv.getChildAt(i);
						final ViewHolder holder = (ViewHolder) v.getTag();
						String attachment = (String) holder.item_transfer_imv.getTag();
						String[] split = attachment.split("://");
						final String attachmentPath = split[split.length == 1?0:1];
						loadImage(holder,attachmentPath);
					}
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					builder.append(" SCROLL_STATE_TOUCH_SCROLL");
					scrolling = true;
					break;
				}
				LogUtil.makeLog(TAG, builder.toString());
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				clearBuilder();
				this.firstVisibleItem = firstVisibleItem;
				this.visibleItemCount = visibleItemCount;
				this.totalItemCount = totalItemCount;
				builder.append(" firstVisibleItem/visibleItemCount/totalItemCount : "+firstVisibleItem+"/"+visibleItemCount+"/"+totalItemCount);
				LogUtil.makeLog(TAG, builder.toString());
			}
		};
		public OnScrollListener getOnScrollListener() {
			// TODO Auto-generated method stub
			return onScrollListener;
		}
		protected synchronized void loadImage(final ViewHolder holder, final String attachmentPath) {
			// TODO Auto-generated method stub
			StringBuilder builder = new StringBuilder(" loadImage()");
			SoftReference<Bitmap> reference = bitmapCache.get(attachmentPath);
			if (reference != null) {
				Bitmap bitmap = reference.get();
				if (bitmap != null) {
					holder.item_transfer_imv.setImageBitmap(bitmap);
					builder.append(" use cache bitmap");
					LogUtil.makeLog(TAG, builder.toString());
					return;
				}else {
					builder.append(" bitmap is null");
				}
			}else {
				builder.append(" reference is null");
			}
			if(flinging||scrolling) {
				builder.append(" scrolling load later");
				LogUtil.makeLog(TAG, builder.toString());
				return;
			}
			builder.append(" use new bitmap");
			LoadImageAsyncTask task = new LoadImageAsyncTask(
					200,200,new LoadImageCallback() {
						public void beforeImageLoad() {
//							holder.item_transfer_imv.setImageResource(R.drawable.icon64);
						}
						public void afterImageLoad(Bitmap bitmap) {
							if (bitmap != null) {
								holder.item_transfer_imv.setImageBitmap(bitmap);
								// 把图片的资源存放到 map的内存集合里面
								// 键 :图片的url
								// 值:图片对应的bitmap
								// bitmapCache.put(messageInfos.getBookicon(),
								// bitmap);
								bitmapCache.put(attachmentPath,
										new SoftReference<Bitmap>(bitmap));
							}else {
//								holder.item_transfer_imv.setImageResource(R.drawable.icon64);
								holder.item_transfer_imv.setImageBitmap(null);
							}
						}
				   });
			task.execute(attachmentPath);
//			LoadImageAsyncTask task = new LoadImageAsyncTask(holder.item_transfer_imv,200,200);
//			task.execute(attachmentPath);
			LogUtil.makeLog(TAG, builder.toString());
		}
	}
	
	public static class PhotoReceiveMessage {
		public String mPhotoPath;
		public String mBody;
		public String mReceiveTime;
		public String mSipName;
		public String mEId;
		
		public void sendToTarget(){
			PhotoTransferReceiveActivity instance = PhotoTransferReceiveActivity.getInstance();
			if(instance!=null) {
				Handler handler = instance.mInnerHanler;
				
				Message msg = handler.obtainMessage();
				msg.what = PhotoTransferReceiveActivity.ON_DATASET_CHANGED;
				msg.obj = this;
				Log.i("xxxx", "PhotoReceiveMessage#sendToTarget enter");
				handler.sendMessage(msg);
			}
				
		}
		
	}
	
	private static class ViewHolder {

		TextView item_transfer_tv_content;
		TextView item_transfer_tv_time;
		TextView item_transfer_tv_person;
		TextView item_transfer_tv_sent;
		ImageView item_transfer_imv;
	}

	private static PhotoTransferReceiveActivity sInstance;

	public static PhotoTransferReceiveActivity getInstance() {
		return sInstance;
	}
	
	public synchronized void showSelectDialog(String title, final int pos) {
		// TODO Auto-generated method stub
		
		AlertDialog.Builder dialog =new AlertDialog.Builder(PhotoTransferReceiveActivity.this);//定义一个弹出框对象
		dialog.setTitle(title);//标题          
		
		dialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				
				switch (keyCode) {
					case KeyEvent.KEYCODE_BACK: {
						dialog.dismiss();
						return false;
					}
				}
				return false;
			}
		});
		dialog.setItems(new String[] { getResources().getString(R.string.delete_message_one),getResources().getString(R.string.delete_all)}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==0) {
					
					ArrayList<PhotoReceiveMessage> list = mPhotoReceiveAdapter.getList();
					if(list!=null){
						PhotoReceiveMessage message = list.get(pos);
						
						list.remove(pos);
						
						String fileName = message.mPhotoPath;
						File file = new File(fileName);
						if(file.exists()) {
							file.delete();
						}
						
						String eId = message.mEId;
						SmsMmsDatabase database = new SmsMmsDatabase(PhotoTransferReceiveActivity.this);
						
						String where = "type = 'mms' and mark = 0 and E_id = '"+eId+"'";
						
						database.delete(SmsMmsDatabase.TABLE_MESSAGE_TALK, where);
						
					}
					
					mPhotoReceiveAdapter.notifyDataSetChanged();
					
				}else if(which==1) {
					
					SmsMmsDatabase database = new SmsMmsDatabase(PhotoTransferReceiveActivity.this);
					
					String where = "type = 'mms' and mark = 0";
					
					database.delete(SmsMmsDatabase.TABLE_MESSAGE_TALK, where);
					
					mInnerHanler.sendEmptyMessage(ON_CLEAR_DATASET);
					
				}
			}
		}).show();
	}
	
}

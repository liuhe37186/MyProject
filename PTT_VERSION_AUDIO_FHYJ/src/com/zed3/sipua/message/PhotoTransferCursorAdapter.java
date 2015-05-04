package com.zed3.sipua.message;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.asynctask.LoadImageAsyncTask;
import com.zed3.asynctask.LoadImageAsyncTask.LoadImageCallback;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.utils.LogUtil;

public class PhotoTransferCursorAdapter extends CursorAdapter {
//	private LayoutInflater mInflater;
	private String TAG = "PhotoTransferCursorAdapter";
	private Context mContext;
	// 软引用的集合,软引用集合里面存放的是 bitmap的类型
	private Map<String, SoftReference<Bitmap>> bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
	private StringBuilder bindViewbuilder = new StringBuilder();
	protected boolean flinging;
	protected boolean scrolling;
	private int messageType;
	private int address;
	private String addressColumnName;
	private String addressTypeStr;
	public PhotoTransferCursorAdapter(Context context, Cursor c, int messageType) {
		super(context, c);
		this.messageType = messageType;
		mContext = context;
//		mInflater = LayoutInflater.from(mContext);
		addressColumnName = messageType == SmsMmsDatabase.TYPE_SEND?"address":"sip_name";
		addressTypeStr = context.getResources().getString(messageType == SmsMmsDatabase.TYPE_SEND?R.string.send_to:R.string.photo_receive);
	}
	

	@Override
	public void changeCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		super.changeCursor(cursor);
	}

	public void setSelectItem(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		bindViewbuilder = new StringBuilder();
		if (bindViewbuilder.length()>0) {
			bindViewbuilder.delete(0, bindViewbuilder.length());
		}
		bindViewbuilder.append(" bindView()");
		int position = cursor.getPosition();
		bindViewbuilder.append(" position "+position);
		String number = cursor.getString(cursor.getColumnIndex(addressColumnName));
		String showText = GroupListUtil.getUserName(number);
		if(TextUtils.isEmpty(showText)) {
			showText = number;
		}
		String attachment = cursor.getString(cursor.getColumnIndex("attachment"));
		String body = cursor.getString(cursor.getColumnIndex("body"));
		String date = cursor.getString(cursor.getColumnIndex("date"));
		final ViewHolder holder  =   (ViewHolder)    convertView.getTag();
		holder.item_transfer_tv_person.setText(addressTypeStr + showText);
		if (messageType == SmsMmsDatabase.TYPE_SEND) {
			int send = cursor.getInt(cursor.getColumnIndex("send"));
			if(send==MessageSender.PHOTO_UPLOAD_STATE_SUCCESS){
				holder.item_transfer_tv_sent.setText(context.getResources().getString(R.string.status_title)+context.getResources().getString(R.string.uploaded));
			}else if(send==MessageSender.PHOTO_UPLOAD_STATE_UPLOADING){
				holder.item_transfer_tv_sent.setText(context.getResources().getString(R.string.status_title)+context.getResources().getString(R.string.uploading));
			}else if(send==MessageSender.PHOTO_UPLOAD_STATE_FAILED) {
				holder.item_transfer_tv_sent.setText(context.getResources().getString(R.string.status_title)+context.getResources().getString(R.string.upload_failed));
			}else if(send==MessageSender.PHOTO_UPLOAD_STATE_FINISHED) {
				holder.item_transfer_tv_sent.setText(context.getResources().getString(R.string.status_title)+context.getResources().getString(R.string.upload_finished));
			}else if(send == MessageSender.PHOTO_UPLOAD_STATE_OFFLINE_SPACE_FULL) {
				holder.item_transfer_tv_sent.setText(context.getResources().getString(R.string.status_title)+context.getResources().getString(R.string.upload_offline_space_full));
			}
		}
		if(body==null||body.length()==0)
			body=SipUAApp.mContext.getResources().getString(R.string.nothing_write);
		holder.item_transfer_tv_content.setText(body);
		holder.item_transfer_tv_time.setText(date);
		
		//set empty bitmap and load realiamge by AsyncTask.modify by mou 2015-01-24
		holder.item_transfer_imv.setTag(attachment);
		holder.item_transfer_imv.setImageBitmap(null);
		String[] split = attachment.split("://");
		final String attachmentPath = split[split.length == 1?0:1];
		loadImage(holder,attachmentPath);
		
		LogUtil.makeLog(TAG, bindViewbuilder.toString());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
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
		holder.item_transfer_tv_sent.setVisibility(messageType == SmsMmsDatabase.TYPE_SEND?View.VISIBLE:View.GONE);
		return view;
	}

//	private void setImageForImageView(String imagePath, ImageView imageView) {
//		Bitmap bitmap = getBitmapFromLruCache(imagePath);
//		if (bitmap != null) {
//			imageView.setImageBitmap(bitmap);
//		} else {
//			imageView.setImageResource(R.drawable.default_image);
//		}
//	}
//
//	public Bitmap getBitmapFromLruCache(String key) {
//		return mLruCache.get(key);
//	}

	private static class ViewHolder {

		TextView item_transfer_tv_content;
		TextView item_transfer_tv_time;
		TextView item_transfer_tv_person;
		TextView item_transfer_tv_sent;
		ImageView item_transfer_imv;
	}
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
//				int count = lv.getChildCount();
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
//						holder.item_transfer_imv.setImageResource(R.drawable.icon64);
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
//							holder.item_transfer_imv.setImageResource(R.drawable.icon64);
							holder.item_transfer_imv.setImageBitmap(null);
						}
					}
			   });
		task.execute(attachmentPath);
//		LoadImageAsyncTask task = new LoadImageAsyncTask(holder.item_transfer_imv,200,200);
//		task.execute(attachmentPath);
		LogUtil.makeLog(TAG, builder.toString());
	}

}

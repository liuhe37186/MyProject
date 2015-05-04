package com.zed3.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.zed3.utils.LogUtil;
/**
 * the util to load iamge to bitmap.
 * @author mou
 *
 */
public class BitmapUtil {

	private static final String TAG = "BitmapUtil";

	/**
	 * load Bitmap.
	 * @param maxWidth
	 * @param maxHeight
	 * @param path
	 * @return
	 */
	public Bitmap loadBitMap(int maxWidth, int maxHeight, String path) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("loadBitMap()");
		builder.append(" maxWidth/maxHeight:"+maxWidth+"/"+maxHeight);
		builder.append(" path:"+path);
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 通过这个options获取图片的宽和高
			BitmapFactory.decodeFile(path, options);
			float realWidth = options.outWidth;
			float realHeight = options.outHeight;
			builder.append(" realWidth/realHeight:"+realWidth+"/"+realHeight);
			int scale;
			// 计算缩放比
			if(realWidth==0||realHeight==0){
//				holder.item_transfer_imv.setImageBitmap(null);
//				return;
				
			}else{
				if (realWidth > realHeight) {
					scale = (int) (realWidth / maxWidth);
					builder.append(" realWidth > realHeight scale="+scale);
				}else {
					scale = (int) (realHeight / maxHeight);
					builder.append(" realWidth <= realHeight scale="+scale);
				}
//					scale = (int) ((realHeight > realWidth ? realHeight : realWidth) / 200);
				if (scale <= 0)
				{
					scale = 1;
				}
				options.inSampleSize = scale;
			}
			
			options.inJustDecodeBounds = false;
			// 注意这次要把options.inJustDecodeBounds 设为 false,这次图片是要读取出来的。
			bitmap = BitmapFactory.decodeFile(path, options);
			if (bitmap == null){
				builder.append(" bitmap is null");
			}else {
				builder.append(" bitmapWidth/bitmapHeight:"+bitmap.getWidth()+"/"+bitmap.getHeight());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			builder.append(" Exception "+e.getMessage());
		}finally{
			LogUtil.makeLog(TAG, builder.toString());
		}
		return bitmap;
	}
	/**
	 * load Bitmap and set imageview.
	 * @param imageView
	 * @param maxWidth
	 * @param maxHeight
	 * @param path
	 * @return
	 */
	public boolean loadImage(ImageView imageView,int maxWidth, int maxHeight, String path) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder("loadBitMap()");
		boolean result = false;
		try {
			Bitmap bitMap = loadBitMap(maxWidth, maxHeight, path);
			if (bitMap != null ) {
				result = true;
			}
			if (imageView != null) {
				imageView.setImageBitmap(bitMap);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			builder.append(" Exception "+e.getMessage());
		}finally{
			builder.append(" return "+result);
			LogUtil.makeLog(TAG, builder.toString());
		}
		return result;
	}
}

package com.zed3.asynctask;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.zed3.bitmap.BitmapUtil;

/**
 * 异步任务的第一次参数 是后台任务执行所需要的参数 String 第三个参数是后台任务执行完毕后的返回值Bitmap
 */
public class LoadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
	//如果我想在下载完毕图片后,设置imageview的内容 ,需要把imageview的引用传递给当前的异步任务
//	private ImageView iv;
//	public LoadImageAsyncTask(ImageView iv) {
//		this.iv = iv;
//	}

	//当用户调用LoadImageAsyncTask下载图片的时候 ,肯定会有一些初始化的操作,肯定也会有一些 下载完毕图片后的操作
	//最好由调用者确定 当前的操作的类型 
	
	//定义一个接口 指定了 图片下载之前的操作和下载之后的操作
	public interface LoadImageCallback {
	   public void 	beforeImageLoad();
	   public void  afterImageLoad(Bitmap bitmap);
	}
	private LoadImageCallback mLoadImageCallback;
	private int dstWidth;
	private int dstHeight;
	private ImageView imageView;
	
	
	/**
	 *  任何调用这个LoadImageAsyncTask 的调用者 都必须把接口的实现类给传递进来
	 * @param mLoadImageCallback
	 */
	public LoadImageAsyncTask(int dstWidth,int dstHeight,LoadImageCallback mLoadImageCallback) {
		this.mLoadImageCallback = mLoadImageCallback;
		this.dstWidth = dstWidth;
		this.dstHeight = dstHeight;
	}
	/**
	 *  任何调用这个LoadImageAsyncTask 的调用者 都必须把接口的实现类给传递进来
	 * @param mLoadImageCallback
	 */
	public LoadImageAsyncTask(ImageView imageView,int dstWidth,int dstHeight) {
		this.imageView = imageView;
		this.dstWidth = dstWidth;
		this.dstHeight = dstHeight;
	}

	/**
	 * 后台线程执行的方法 ,接受的参数 传递进来的参数 是 一个可变长度的参数, 实际上就是一个params的数组
	 */
	@Override
	protected Bitmap doInBackground(String... params) {
		try {
			String iconpath = params[0];
//			URL url = new URL(iconpath);
//			URLConnection conn = url.openConnection();
//			return BitmapFactory.decodeStream(conn.getInputStream());
			Bitmap bitmap = new BitmapUtil().loadBitMap(dstWidth,dstHeight,iconpath);
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 后台任务执行之前调用的方法 运行在主线程里面
	 */
	@Override
	protected void onPreExecute() {
		if (mLoadImageCallback != null) {
			mLoadImageCallback.beforeImageLoad();
		}
		super.onPreExecute();
	}

	/**
	 * 后台任务执行之后调用的方法 运行在主线程里面
	 */
	@Override
	protected void onPostExecute(Bitmap result) {
		//拿到了下载完毕图片对应的bitmap
		if (mLoadImageCallback != null) {
			mLoadImageCallback.afterImageLoad(result);
		}
		super.onPostExecute(result);
	}
}

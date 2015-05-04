package com.zed3.asynctask;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.zed3.bitmap.BitmapUtil;

/**
 * �첽����ĵ�һ�β��� �Ǻ�̨����ִ������Ҫ�Ĳ��� String �����������Ǻ�̨����ִ����Ϻ�ķ���ֵBitmap
 */
public class LoadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
	//����������������ͼƬ��,����imageview������ ,��Ҫ��imageview�����ô��ݸ���ǰ���첽����
//	private ImageView iv;
//	public LoadImageAsyncTask(ImageView iv) {
//		this.iv = iv;
//	}

	//���û�����LoadImageAsyncTask����ͼƬ��ʱ�� ,�϶�����һЩ��ʼ���Ĳ���,�϶�Ҳ����һЩ �������ͼƬ��Ĳ���
	//����ɵ�����ȷ�� ��ǰ�Ĳ��������� 
	
	//����һ���ӿ� ָ���� ͼƬ����֮ǰ�Ĳ���������֮��Ĳ���
	public interface LoadImageCallback {
	   public void 	beforeImageLoad();
	   public void  afterImageLoad(Bitmap bitmap);
	}
	private LoadImageCallback mLoadImageCallback;
	private int dstWidth;
	private int dstHeight;
	private ImageView imageView;
	
	
	/**
	 *  �κε������LoadImageAsyncTask �ĵ����� ������ѽӿڵ�ʵ��������ݽ���
	 * @param mLoadImageCallback
	 */
	public LoadImageAsyncTask(int dstWidth,int dstHeight,LoadImageCallback mLoadImageCallback) {
		this.mLoadImageCallback = mLoadImageCallback;
		this.dstWidth = dstWidth;
		this.dstHeight = dstHeight;
	}
	/**
	 *  �κε������LoadImageAsyncTask �ĵ����� ������ѽӿڵ�ʵ��������ݽ���
	 * @param mLoadImageCallback
	 */
	public LoadImageAsyncTask(ImageView imageView,int dstWidth,int dstHeight) {
		this.imageView = imageView;
		this.dstWidth = dstWidth;
		this.dstHeight = dstHeight;
	}

	/**
	 * ��̨�߳�ִ�еķ��� ,���ܵĲ��� ���ݽ����Ĳ��� �� һ���ɱ䳤�ȵĲ���, ʵ���Ͼ���һ��params������
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
	 * ��̨����ִ��֮ǰ���õķ��� ���������߳�����
	 */
	@Override
	protected void onPreExecute() {
		if (mLoadImageCallback != null) {
			mLoadImageCallback.beforeImageLoad();
		}
		super.onPreExecute();
	}

	/**
	 * ��̨����ִ��֮����õķ��� ���������߳�����
	 */
	@Override
	protected void onPostExecute(Bitmap result) {
		//�õ����������ͼƬ��Ӧ��bitmap
		if (mLoadImageCallback != null) {
			mLoadImageCallback.afterImageLoad(result);
		}
		super.onPostExecute(result);
	}
}

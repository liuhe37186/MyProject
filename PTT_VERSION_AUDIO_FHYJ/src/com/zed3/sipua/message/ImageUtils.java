/**
 * houyuchun create 20120807 begin 
 */

package com.zed3.sipua.message;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.zoolu.tools.MyLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class ImageUtils {

	private static final String TAG = "ImageUtils";
	// mWidth 图片宽度
    private int mWidth;
    // mHeight 图片高度
    private int mHeight;
    private Context mContext;
    /**
     * The quality parameter which is used to compress JPEG images.
     */
    private static final int IMAGE_COMPRESSION_QUALITY = 80;
    /**
     * The minimum quality parameter which is used to compress JPEG images.
     */
    /**
     * Message overhead that reduces the maximum image byte size.
     * 5000 is a realistic overhead number that allows for user to also include
     * a small MIDI file or a couple pages of text along with the picture.
     */
    public static final int MESSAGE_OVERHEAD = 5000;
    private static final int MINIMUM_IMAGE_COMPRESSION_QUALITY = 50;
    private static final int NUMBER_OF_RESIZE_ATTEMPTS = 4;
    // MAX_IMAGE_HEIGHT 图片最大高度
    private static final int MAX_IMAGE_HEIGHT = 480;
    // MAX_IMAGE_WIDTH 图片最大宽度
    private static final int MAX_IMAGE_WIDTH = 640;
    // MAX_MESSAGE_SIZE 彩信最大限制(2M)
    private static final int MAX_MESSAGE_SIZE = 2 * 1024 * 1024;
    private static final int mMaxMessageSize = MAX_MESSAGE_SIZE; 
    private static int mMaxImageHeight = MAX_IMAGE_HEIGHT;      // default value
    private static int mMaxImageWidth = MAX_IMAGE_WIDTH;  
    
    public ImageUtils(Context context) {
    	this.mContext = context;
    }
    
    /**
     * @param mUri 图片URI
     */
    public void decodeBoundsInfo(Uri mUri) {
        InputStream input = null;
        try {
            input = mContext.getContentResolver().openInputStream(mUri);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            // inJustDecodeBounds,设置该属性为true,可获得图片的高度和宽度
            opt.inJustDecodeBounds = true;
            // 此处只是计算图片属性，不会为bitmap开辟内存空间
            BitmapFactory.decodeStream(input, null, opt);
            // 宽度
            mWidth = opt.outWidth;
            // 高度
            mHeight = opt.outHeight;
        } catch (FileNotFoundException e) {
            // Ignore
            MyLog.e(TAG, "IOException caught while opening stream");
            e.printStackTrace();
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                	MyLog.e(TAG, "IOException caught while closing stream");
                	e.printStackTrace();
                }
            }
        }
    }

   /**
    * @param widthLimit   图片宽度限制
    * @param heightLimit  图片高度限制
    * @param byteLimit    图片大小限制
    * @param mUri         图片URI
    * @return             压缩后的图片大小
    */  
    public byte[] getResizedImageData(int widthLimit, int heightLimit, int byteLimit, Uri mUri) {
        int outWidth = mWidth;
        int outHeight = mHeight;

        int scaleFactor = 1;
        while ((outWidth / scaleFactor > widthLimit) || (outHeight / scaleFactor > heightLimit)) {
            scaleFactor *= 2;
        }

        InputStream input = null;
        try {
            ByteArrayOutputStream os = null;
            int attempts = 1;

            do {
                BitmapFactory.Options options = new BitmapFactory.Options();
                // inSampleSize 设置压缩后图片与原始图片的缩放比例
                options.inSampleSize = scaleFactor;
                input = mContext.getContentResolver().openInputStream(mUri);
                // quality 压缩后图片质量
                int quality = IMAGE_COMPRESSION_QUALITY;
                try {
                    Bitmap b = BitmapFactory.decodeStream(input, null, options);
                    if (b == null) {
                        return null;
                    }
                    if (options.outWidth > widthLimit || options.outHeight > heightLimit) {
                        // The decoder does not support the inSampleSize option.
                        // Scale the bitmap using Bitmap library.
                        int scaledWidth = outWidth / scaleFactor;
                        int scaledHeight = outHeight / scaleFactor;
                        MyLog.i(TAG, "getResizedImageData: retry scaling using " +
                                "Bitmap.createScaledBitmap: w=" + scaledWidth +
                                ", h=" + scaledHeight);

                        b = Bitmap.createScaledBitmap(b, outWidth / scaleFactor,
                                outHeight / scaleFactor, false);
                        if (b == null) {
                            return null;
                        }
                    }

                    // Compress the image into a JPG. Start with MessageUtils.IMAGE_COMPRESSION_QUALITY.
                    // In case that the image byte size is still too large reduce the quality in
                    // proportion to the desired byte size. Should the quality fall below
                    // MINIMUM_IMAGE_COMPRESSION_QUALITY skip a compression attempt and we will enter
                    // the next round with a smaller image to start with.
                    os = new ByteArrayOutputStream();
                    b.compress(CompressFormat.JPEG, quality, os);
                    int jpgFileSize = os.size();
                    if (jpgFileSize > byteLimit) {
                        int reducedQuality = quality * byteLimit / jpgFileSize;
                        if (reducedQuality >= MINIMUM_IMAGE_COMPRESSION_QUALITY) {
                            quality = reducedQuality;
                            MyLog.i(TAG, "getResizedImageData: compress(2) w/ quality=" + quality);

                            os = new ByteArrayOutputStream();
                            b.compress(CompressFormat.JPEG, quality, os);
                        }
                    }
                    b.recycle();        // done with the bitmap, release the memory
                } catch (java.lang.OutOfMemoryError e) {
                    MyLog.e(TAG, "getResizedImageData - image too big (OutOfMemoryError), will try "
                            + " with smaller scale factor, cur scale factor: " + scaleFactor);
                    e.printStackTrace();
                    // fall through and keep trying with a smaller scale factor.
                }
                scaleFactor *= 2;
                attempts++;
            } while ((os == null || os.size() > byteLimit) && attempts < NUMBER_OF_RESIZE_ATTEMPTS);

            return os == null ? null : os.toByteArray();
        } catch (FileNotFoundException e) {
            MyLog.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        } catch (java.lang.OutOfMemoryError e) {
        	MyLog.e(TAG, e.getMessage());
        	e.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                	MyLog.e(TAG, e.getMessage());
                	e.printStackTrace();
                }
            }
        }
    }
    
    // 将byte数组装换为bitmap
    public Bitmap Bytes2Bimap(byte[] b) {
    	if (b.length == 0) {
    		return null;
    	}
    		
    	return BitmapFactory.decodeByteArray(b, 0, b.length);
    }
    
    public int getMaxImageHeight() {
        return mMaxImageHeight;
    }

    public int getMaxImageWidth() {
        return mMaxImageWidth;
    }
    
    public int getMaxMessageSize() {
    	return mMaxMessageSize;
    }
    
    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
    
}

package com.video.utils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.zoolu.tools.MyLog;

import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Settings;
import com.zed3.toast.MyToast;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class H264Dec{

	private MediaCodec codec;
//	private Surface surface;
	public boolean bFristDec = false;
	private SurfaceView sfview;
	private int decodeW,decodeH;
	private int type = 2;
	String MIME_TYPE = "video/avc";
	public H264Dec(SurfaceView holderview) {
//		this.surface = holderview.getHolder().getSurface();
//		 try {
//			 File file = new File("/sdcard/videoc_after.3gp");
//			 raf = new RandomAccessFile(file, "rw");
//			 } catch (Exception ex) {
//			 Log.v("System.out", ex.toString());
//			 }
		sfview = holderview;
		SharedPreferences mypre = SipUAApp.mContext.getSharedPreferences(Settings.sharedPrefsFile,
				Activity.MODE_PRIVATE);
		type = mypre.getInt("videoshowtype", 2);
	}
	public void createCodec() {
		if(Build.VERSION.SDK_INT < 16){
			return;
		}
		codec = MediaCodec.createDecoderByType(MIME_TYPE);
		
	}
	public void tryConfig(int width,int height,byte[] sps,byte[] pps){
		if(codec == null) return;
		MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
				width, height);
		if(sps != null || pps != null){
			mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
			mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
		}
		codec.configure(mediaFormat, sfview.getHolder().getSurface(), null, 0);
//		codec.setVideoScalingMode(/*2*/type);
		codec.start(); 
	};
	public void reConfig(int width,int height,byte[] sps,byte[] pps){
//		if (Build.VERSION.SDK_INT <= 17)	//for "CP-DX80"
//	   	{
			releaseCodec();//先释放解码器
			createCodec();//再初始化解码器
//	   	}
														
//		codec.stop();//停止解码器													
		tryConfig(width,height,sps,pps);
//		codec.start(); 
							
	}
	public void releaseCodec() {
//		if(raf != null){
//			try {
//				raf.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		if(codec!= null){
			codec.stop();
			codec.release();
			codec = null;
		}
	}

	private void decodeAndPlayBack(byte[] in, int offset, int length,int type) {
		MyLog.e("video_tag", "decodeAndPlayBack called,in.length = "+in.length);
		ByteBuffer[] inputBuffers = codec.getInputBuffers();
		MyLog.e("video_tag", "inputBuffers size = "+inputBuffers.length);
		int inputBufferIndex = codec.dequeueInputBuffer(-1);
		MyLog.e("video_tag", "inputBufferIndex = "+inputBufferIndex);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(in, offset, length);
			codec.queueInputBuffer(inputBufferIndex, 0, length, 0, type);
		}
		MyLog.e("video_tag", "to release!1111");
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		MyLog.e("video_tag", "to release!2222");
		int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
		if (outputBufferIndex >= 0) {
			codec.releaseOutputBuffer(outputBufferIndex, true);
		}
		MyLog.e("video_tag", "to release!3333 outputBufferIndex="+outputBufferIndex);
	}
	
//	RandomAccessFile raf = null;
	public void PlayDecode(byte[] recBuffer,int type){
//		try {
//			raf.write(recBuffer);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		if(codec != null){
			decodeAndPlayBack(recBuffer, 0, recBuffer.length,type);
			MyLog.e("video_tag", "recBuffer size = "+recBuffer.length);
			}
	}
}

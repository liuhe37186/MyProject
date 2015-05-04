package com.zed3.jni;

import org.zoolu.tools.MyLog;

import com.zed3.log.Logger;

import android.util.Log;

public class VideoUtils {

	private static String tag = "VideoUtils";

	// 定义native方法
	// native static int[] getWithAndHightFromC(/*char*/byte[] sps,int len);
	native static int[] getWithAndHightFromC(byte[] in, int[] out);

	// 加载函数库
	static {

		loadLibrary();

	}

	public static int[] getWithAndHight(/* char */byte[] in/* sps,int len */) {
		// TODO Auto-generated method stub

		// byte[] in1 ={0x42 ,(byte) 0xc0 ,0x1e ,(byte) 0xe9 ,0x03 ,(byte) 0xc0
		// ,(byte) 0xb7 ,0x20 ,0x00 ,0x00 ,0x00 ,0x01 ,0x68 ,(byte) 0xce ,0x06
		// ,(byte) 0xe2};
		// byte[] in1 ={0x42 ,(byte) 0x80 ,0x15 ,(byte) 0xd9 ,0x01 ,0x60 ,(byte)
		// 0x96 ,(byte) 0x84 ,0x00 ,0x00 ,0x03 ,0x00 ,0x04 ,0x00 ,0x00 ,0x03
		// ,0x00 ,0x50 ,0x3c ,0x58 ,(byte) 0xb9 ,0x20};
		
		
		// String string = "";
		// for (int i = 0; i < in.length; i++) {
		// if (i == (in.length-1)) {
		// string = string + in[i]+";";
		// }else {
		// string = string + in[i]+",";
		// }
		// }
		// Logger.i(tag, "BYTE* = "+string);

		int[] out = new int[2];
		out = getWithAndHightFromC(in, out);
		Log.i(tag, "BYTE = " + out[0] + ":" + out[1]);

		return out;
	}

	private static void loadLibrary() {
		// TODO Auto-generated method stub
		try {
			MyLog.e(tag, "ready load h264_wh.so");
			System.loadLibrary("H264_WH");
			MyLog.e(tag, "load h264_wh.so success");
		} catch (Exception e) {
			MyLog.e(tag, "loadLibrary error!" + e.toString());
		}
	}

}

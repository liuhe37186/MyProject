package com.zed3.ace;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;

public class TestTools {
	File tmpFile1,tmpFile2;
	public static int count = 0;
	public static DataOutputStream receiveStream = null;
	static DataOutputStream dos4encodeOut = null;
	static DataOutputStream dos4encodeIn = null;
	static DataOutputStream dos4encodeIn2 = null;
	public static void  write2File(byte[] encodeOut,boolean isMic) {
		short[] encodeIn = byteArray2ShortArray(encodeOut);
		try {
			if (dos4encodeOut == null) {
				String dir = Environment.getExternalStorageDirectory()
						.getAbsolutePath()+"/aecvoice/";
				File temp = new File(dir);
				if(!temp.exists()){
					temp.mkdirs();
				}
				String state = isMic?"MIC":"Speaker";
				String tmp = state+"-"+fromNum+"-"+toNum+"-"+formatTime()+"-"+count;
				String filename = "/aecvoice/" + tmp  + ".pcm";
				File file = new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath() + filename);
				OutputStream os = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				dos4encodeOut = new DataOutputStream(bos);
			}
			for (int j = 0; j < encodeIn.length; j++) {
				dos4encodeOut.writeShort(encodeIn[j]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void write2FileMIC(short[] encodeIn) {
		try {
				if (dos4encodeIn == null) {
					String dir = Environment.getExternalStorageDirectory()
							.getAbsolutePath()+"/aecvoice/";
					File temp = new File(dir);
					if(!temp.exists()){
						temp.mkdirs();
					}
					String state ="MIC";
					String tmp = state+"-"+fromNum+"-"+toNum+"-"+formatTime()+"-"+count;
					String filename = "/aecvoice/" + tmp  + ".pcm";
					File file = new File(Environment.getExternalStorageDirectory()
							.getAbsolutePath() + filename);
					OutputStream os = new FileOutputStream(file);
					BufferedOutputStream bos = new BufferedOutputStream(os);
					dos4encodeIn = new DataOutputStream(bos);
				}
				for (int j = 0; j < encodeIn.length; j++) {
					dos4encodeIn.writeShort(encodeIn[j]);
				}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void  write2File(short[] receive) {
		try {
			if (receiveStream == null) {
				String dir = Environment.getExternalStorageDirectory()
						.getAbsolutePath()+"/audioTest/";
				File temp = new File(dir);
				if(!temp.exists()){
					temp.mkdirs();
				}
				String filename = "/audioTest/" +  "receive.pcm";
				File file = new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath() + filename);
				OutputStream os = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				receiveStream = new DataOutputStream(bos);
			}
			for (int j = 0; j < receive.length; j++) {
				receiveStream.writeShort(receive[j]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void write2FileSpeaker(short[] encodeIn) {
		try {
				if (dos4encodeIn2 == null) {
					String dir = Environment.getExternalStorageDirectory()
							.getAbsolutePath()+"/aecvoice/";
					File temp = new File(dir);
					if(!temp.exists()){
						temp.mkdirs();
					}
					String state = "Speaker";
					String tmp = state+"-"+fromNum+"-"+toNum+"-"+formatTime()+"-"+count;
					String filename = "/aecvoice/" + tmp  + ".pcm";
					File file = new File(Environment.getExternalStorageDirectory()
							.getAbsolutePath() + filename);
					name = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + filename;
					OutputStream os = new FileOutputStream(file);
					BufferedOutputStream bos = new BufferedOutputStream(os);
					dos4encodeIn2 = new DataOutputStream(bos);
				}
				for (int j = 0; j < encodeIn.length; j++) {
					dos4encodeIn2.writeShort(encodeIn[j]);
				}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void release(){
		if(dos4encodeIn != null){
			try {
				dos4encodeIn.flush();
				dos4encodeIn.close();
				dos4encodeIn = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(dos4encodeOut!= null){
			try {
				dos4encodeOut.flush();
				dos4encodeOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			dos4encodeOut = null;
		}
		if(dos4encodeIn2 != null){
			try {
				dos4encodeIn2.flush();
				dos4encodeIn2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			dos4encodeIn2 = null;
		}
		if(receiveStream != null){
			try {
				receiveStream.flush();
				receiveStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			receiveStream = null;
		}
		name ="";
	}
	private static short[] byteArray2ShortArray(byte[] data) {
//	    short[] retVal = new short[data.length];
//	    for (int i = 0; i < retVal.length; i++)
//	        retVal[i] = (short) ((data[i * 2]&0xff) | (data[i * 2+1]&0xff) << 8);
//	    return retVal;
		 short[] shorts = new short[data.length/2];
		// to turn bytes to shorts as either big endian or little endian. 
		ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
		return shorts;
	    }
	private static String fromNum = "from";
	private static String toNum ="to";
	public static void formatFileName(boolean isCallIn,String num){
		if(isCallIn){
			fromNum = num;
			toNum = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(Settings.PREF_USERNAME, Settings.DEFAULT_USERNAME);
		}else{
			fromNum = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(Settings.PREF_USERNAME, Settings.DEFAULT_USERNAME);
			toNum = num;
		}
	}
	private static String name = "";
	public static String getSpeakerName(){
		return name;
	}
	private static String formatTime(){
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(
				"ddHHmm");
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	public static boolean isAECOPen(Context ctx){
		SharedPreferences settings;
		settings = ctx.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		return settings.getBoolean(Settings.PREF_AECSWITCH, true);
	}
	private static boolean toggleState = false;
	public static void toggleEnable() {
		AECManager.enable(toggleState);
		NSManager.enable(toggleState);
		toggleState = !toggleState;
	}
}

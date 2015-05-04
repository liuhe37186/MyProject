/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.zed3.codecs;

import org.audio.audioEngine.SlientCheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zed3.codecs.EncodeRate.Mode;
import com.zed3.log.MyLog;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.net.util.ArrayParser;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.CallActivity;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.Sipdroid;

public class AmrNB extends CodecBase implements Codec {

	private final String tag = "AmrNB";
	
	AmrNB() {
		CODEC_NAME = "AMR";
		CODEC_USER_NAME = "AMR";
		CODEC_DESCRIPTION = "4.75-12.2kbit";
		CODEC_NUMBER = 114;//96;
		//CODEC_FRAME_SIZE = 320;	
		CODEC_DEFAULT_SETTING = "always";

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext);
		String amrModeString = sp.getString(Settings.AMR_MODE, Settings.DEFAULT_AMR_MODE);
		if("Auto".equals(amrModeString)){
			amrRate = MainActivity.mode;
		} else {
			setRate(getModeFromString(amrModeString));
		}
		super.update();
	}

	void load() {
		try {
			System.loadLibrary("amrnb_jni");
			super.load();
		} catch (Throwable e) {
			if (!Sipdroid.release) e.printStackTrace();
		}
	}  
 
	public native int open();
	public native int amrDecode(byte encoded[], short lin[], int size);
	public native int amrEncode(short lin[], int offset, byte encoded[], int size, EncodeRate.Mode mode);
	public native void close();

	public void init() {
		load();
		if (isLoaded())
			open();
	}
	
	public static EncodeRate.Mode amrRate = EncodeRate.Mode.MR475;
	public static EncodeRate.Mode requestAmrRate = EncodeRate.Mode.MR475;
	private final int LEN_MODE0 = 13;
	private final int LEN_MODE1 = 14;
	private final int LEN_MODE2 = 16;
	private final int LEN_MODE3 = 18;
	private final int LEN_MODE4 = 20;
	private final int LEN_MODE5 = 21;
	private final int LEN_MODE6 = 27;
	private final int LEN_MODE7 = 32;
	
	int getBufferSize(EncodeRate.Mode mode){
		int size = 0;
		switch(mode){
		case MR475:
			size = LEN_MODE0;
			break;
		case MR515:
			size = LEN_MODE1;
			break;
		case MR59:
			size = LEN_MODE2;
			break;
		case MR67:
			size = LEN_MODE3;
			break;
		case MR74:
			size = LEN_MODE4;
			break;
		case MR795:
			size = LEN_MODE5;
			break;
		case MR102:
			size = LEN_MODE6;
			break;
		case MR122:
			size = LEN_MODE7;
			break;
			default:
				size = LEN_MODE0;
				break;
		}
		return size;
	}
	
	@Override
	public int encode(short[] lin, int offset, byte[] alaw, int frames) {
//		//TODO ..
		 // add by zdx
		 if("Auto".equals(PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).getString(Settings.AMR_MODE, Settings.DEFAULT_AMR_MODE))) {
				amrRate = requestAmrRate;
		 }
		
		int result  =  1;
		int frameSize = getBufferSize(amrRate);
		int encodeFrameNum = frames/160;
		short[] encodeIn = new short[160];
		byte[] encodeOut = new byte[50];
		for(int i=0;i<encodeFrameNum;i++){//check slient 
			int offest2 = offset+160*i;
			System.arraycopy(lin,offest2, encodeIn, 0, encodeIn.length);
			
			if(slientCheck != null){
				int value = slientCheck.WebRtcVadProcess(8000, ArrayParser.shortArray2ByteArray(encodeIn), 160);
				if(value == 1){
					break;
				}
				if(i == encodeFrameNum-1){
					return 0;
				}
			}
		}
		for(int i=0;i<encodeFrameNum;i++){
			int offest2 = offset+160*i;
			System.arraycopy(lin,offest2, encodeIn, 0, encodeIn.length);
			result += amrEncode(encodeIn, 0, encodeOut, frameSize, amrRate);
			if(i<encodeFrameNum-1){
				alaw[12+i+1] = (byte)(encodeOut[13] | 0x80);
			}else{
				alaw[12+i+1] = encodeOut[13];
			}
			System.arraycopy(encodeOut, 14 , alaw, 13+(frameSize-1)*i+encodeFrameNum, frameSize-1);
			alaw[12]= (byte) (RtpStreamReceiver_signal.judged_cmr & 0x7F);
			alaw[12] =(byte)((alaw[12]<<4) & 0xf0);
//			alaw[12]= (byte) (RtpStreamReceiver_signal.judged_cmr & 0x7F)<<4) & 0xf0)//(byte)0xf0;
//			System.arraycopy(encodeOut, 13, alaw, 13*i+13, 13);
		}
		return result;//amrEncode(lin, offset, alaw, frames, amrRate);
	}
	
	@Override
	public int decode(byte[] buffer, short[] lin, int payloadLength, int frames) {
		return -1;
	}
	
	public void setRate(EncodeRate.Mode mode){
		if(mode != Mode.N_MODES && mode != amrRate){
			amrRate = mode;
			SipUAApp.mContext.sendBroadcast(new Intent(CallActivity.ACTION_AMR_RATE_CHANGE));
		}
	}

	public static Mode getModeFromString(String modeValue) {
		if (modeValue.equals("12.2")) {
			return Mode.MR122;
		} 
		else if (modeValue.equals("10.2")) {
			return Mode.MR102;
		}
		else if (modeValue.equals("7.95")) {
			return Mode.MR795;
		}
		else if (modeValue.equals("7.4")) {
			return Mode.MR74;
		}
		else if (modeValue.equals("6.7")) {
			return Mode.MR67;
		}
		else if (modeValue.equals("5.9")) {
			return Mode.MR59;
		}
		else if (modeValue.equals("5.15")) {
			return Mode.MR515;
		}
		else if (modeValue.equals("4.75")) {
			return Mode.MR475;
		}
		return Mode.N_MODES;
	}
	
	int sizes[] = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 6, 5, 5, 0, 0, 0, 0 };
	
	@Override
	public int decode(byte[] encoded, short[] lin, int size) {
		requestAmrRate = ModeChange.getmode((byte)((encoded[12]>>4)&0x0f));
		int result = 0;
		int frameCount = 0;
		while(true)
		{
			int a = encoded[13+frameCount] & 0x80;
			if(a == 0)
			{
				break;
			}
			frameCount++;
		}
		frameCount++;
		
		byte[] bufferPerFrame = new byte[160];
		int framelen;
		short[] decodeout = new short[160];
		for(int i=0;i<frameCount;i++){
			bufferPerFrame[13] = encoded[13+i];
			if(i<frameCount-1){
				bufferPerFrame[0] &= 0x7f;
			}
			framelen = sizes[(byte)(bufferPerFrame[13]>>3 & 0x0f)];
			System.arraycopy(encoded, 13+frameCount+i*framelen, bufferPerFrame, 14, framelen);
			result += amrDecode(bufferPerFrame, decodeout, framelen+1);
			System.arraycopy(decodeout, 0, lin, 160*i, 160);
		}
//		//TODO .
//		int frameHeader = (byte) (((encoded[12]>>4) << 3) | 0x4);
//		int result = 0;
//		int frameCount = size/13;
//		byte[] decodeIn = new byte[26];
//		short[] decodeOut = new short[160];
//		for(int i= 0;i<frameCount;i++){
//			int offest2 = 13+13*i;
//			System.arraycopy(encoded,offest2, decodeIn, 13, 13);
//			result += amrDecode(decodeIn,decodeOut,13);
//			System.arraycopy(decodeOut, 0, lin, 160*frameCount, 160);
//		}
		return result;//amrDecode(encoded,lin,size);
	}

}

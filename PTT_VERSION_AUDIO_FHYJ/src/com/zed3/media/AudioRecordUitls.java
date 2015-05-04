package com.zed3.media;

import com.zed3.log.Logger;

import android.media.AudioRecord;

public class AudioRecordUitls {
	public static AudioRecord record;
	private static String tag = "AudioRecordUitls";
	private static boolean needLog = true;
	
	private AudioRecordUitls(){}
	
	public synchronized static AudioRecord getRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes){
		if (record!=null) {
			Logger.i(needLog ,tag , "AudioRecordFactory-new AudioRecord record != null");
			releaseRecord(record);
		}else {
			Logger.i(needLog,tag , "AudioRecordFactory-new AudioRecord record == null");
		}
		record = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
		return record;
	}
		
	public synchronized static void releaseRecord(AudioRecord record){
		if (record != null) {
			Logger.i(needLog,tag , "AudioRecordFactory-releaseRecord() record != null ");
			if (record.getState()==AudioRecord.RECORDSTATE_RECORDING) {
				record.stop();
			}
			record.release();
			record = null;
		}else {
			Logger.i(needLog,tag , "AudioRecordFactory-releaseRecord() record == null ");
		}
	}
}

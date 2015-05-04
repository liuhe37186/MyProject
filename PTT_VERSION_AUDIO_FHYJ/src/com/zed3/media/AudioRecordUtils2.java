package com.zed3.media;

import android.media.AudioRecord;

import com.zed3.log.Logger;
/**
 * a good class for more then one thread to use the only one AudioRecord instance,
 *  no problem on phone,but some problem on PAD;
 * @author oumogang
 *
 */
public class AudioRecordUtils2 {
	private static AudioRecord record;
	private static int userNum = 0;
	private static String tag = "AudioRecordUtils";
	private static boolean needLog = true;
	private AudioRecordUtils2(){}
	/**
	 * get the AudioRecord instance to record audio;
	 * @param audioSource
	 * @param sampleRateInHz
	 * @param channelConfig
	 * @param audioFormat
	 * @param bufferSizeInBytes
	 * @return
	 */
	public synchronized static AudioRecord getAudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes){
		if (record==null) {
			Logger.i(needLog ,tag , "AudioRecordUtils-getAudioRecord() userNum == 0 ");
			record = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
		}else {
			Logger.i(needLog,tag , "AudioRecordUtils-getAudioRecord() userNum == "+userNum);
		}
		userNum++;
		return record;
		
	}
	/**
	 * give up to use the AudioRecord instance;
	 */
	public synchronized static void releaseAudioRecord(AudioRecord record){
		if (record == null) {
			Logger.i(needLog,tag , "AudioRecordUtils-releaseRecord() userNum == 0£¬record == null ");
			return;
		}else if (userNum == 1) {
			Logger.i(needLog,tag , "AudioRecordUtils-releaseRecord() userNum == 1 ");
			if (record.getState()==AudioRecord.RECORDSTATE_RECORDING) {
				record.stop();
			}
			record.release();
			record = null;
			AudioRecordUtils2.record = null;
			userNum = 0;
			return;
		}else if (userNum > 1) {
			Logger.i(needLog,tag , "AudioRecordUtils-releaseRecord() userNum > 1 ");
			userNum--;
			return;
		}else {
			return;
		}
		
	}

}

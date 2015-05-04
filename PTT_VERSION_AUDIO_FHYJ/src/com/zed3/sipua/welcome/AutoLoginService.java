package com.zed3.sipua.welcome;

import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Zed3Log;

public class AutoLoginService {
	
	private static final String LOG_TAG = "testcrash";//AutoLoginService.class.getSimpleName();
	
	private static AutoLoginService sDefault = new AutoLoginService();
	
	public static AutoLoginService getDefault(){
		return sDefault;
	}
	
	/**
	 * �������˿�����Ƶ����
	 */
	public void saveVideoSwitch(boolean vidoSwitch) {
		saveBoolean("isVideo", vidoSwitch);
	}
	
	public boolean getVideoSwitch(){
		return getBoolean("isVideo");
	}
	/**
	 * �������˿�������ͨ������
	 */
	public void saveAudioSwitch(boolean audioSwitch) {
		saveBoolean(Settings.PREF_AUDIO_SWITCH, audioSwitch);
	}
	
	public boolean getAudioSwitch(){
		return getBoolean(Settings.PREF_AUDIO_SWITCH);
	}
	/**
	 * ����ͨ����ʽ  0:�ƶ��绰    1:voip�绰
	 */
	public void saveAudioMode(int mode) {
		String modeString = (mode == 0 ? "0" : "1");
		
		saveString(Settings.PHONE_MODE, modeString);
	}
	
	public String getAudioMode(){
		return getString(Settings.PHONE_MODE);
	}
	/**
	 * ��������
	 */
	public void saveAudioConference(boolean autoConference){
		saveBoolean(Settings.PERF_AUDIO_CONFERENCE, autoConference);
	}
	
	public boolean getAudioConference() {
		return getBoolean(Settings.PERF_AUDIO_CONFERENCE);
	}
	
	/**
	 * ��������
	 */
	public void saveStartDevice(String startDevice) {
		saveString("autorunkeybydpmp", startDevice);
	}
	
	public String getStartDevice(){
		return getString("autorunkeybydpmp");
	}
	
	public boolean isStartGQT(){
		String startDevice = getStartDevice();
		if(!TextUtils.isEmpty(startDevice)) {
			if(startDevice.equals("1")) {
				return true;
			}
		}
		return false;
	}
	
	public void saveGpsRemoteMode(int mode){
		saveInt(Settings.PREF_GPS_REMOTE, mode);
	}
	
	public int getGpsRemote(){
		return getInt(Settings.PREF_GPS_REMOTE);
	}
	
	/**
	 * ���������
	 */
	public void saveCheckUpdate(boolean checkUpdate) {
		saveBoolean(Settings.PERF_CHECK_UPDATE, checkUpdate);
	}
	
	public boolean isCheckUpdate(){
		return getBoolean(Settings.PERF_CHECK_UPDATE);
	}
	
	/**
	 * �������
	 */
	public void saveEncryptRemote(boolean encypt) {
		saveBoolean("encyptOnOff", encypt);
	}
	
	public boolean getEncryptRemote(){
		return getBoolean("encyptOnOff");
	}
	
	/**
	 * �Խ���ͼģʽ
	 */
	public void savePttMapMode(boolean mode){
		saveBoolean(Settings.PERF_PTT_MAP, mode);
	}
	
	public boolean getPttMapMode(){
		return getBoolean(Settings.PERF_PTT_MAP);
	}
	
	/**
	 * ͼƬ�Ĵ�
	 */
	public void savePicUpload(boolean picUpload) {
		saveBoolean(Settings.PERF_PIC_UPLOAD, picUpload);
	}
	
	public boolean getPicUpload(){
		return getBoolean(Settings.PERF_PIC_UPLOAD);
	}
	/**
	 * ����Ϣ
	 * @param sptSMS
	 */
	public void saveSupportSMS(boolean sptSMS){
		saveBoolean(Settings.PERF_SMS, sptSMS);
	}
	
	public boolean getSupportSMS(){
		return getBoolean(Settings.PERF_SMS);
	}
	
	public void initDeviceInfo() {
		Zed3Log.debug(LOG_TAG, "AutoLoginService#initDeviceInfo() enter ****************");
		LogUtil.makeLog(LOG_TAG, " initDeviceInfo()");
		//�������˿�����Ƶ����
		DeviceInfo.CONFIG_SUPPORT_VIDEO = getVideoSwitch();
		Zed3Log.debug(LOG_TAG, "��Ƶ����DeviceInfo#CONFIG_SUPPORT_VIDEO = " + DeviceInfo.CONFIG_SUPPORT_VIDEO);
		
		//�������˿�������ͨ������
		DeviceInfo.CONFIG_SUPPORT_AUDIO = getAudioSwitch();
		Zed3Log.debug(LOG_TAG, "����ͨ������DeviceInfo#CONFIG_SUPPORT_AUDIO = " + DeviceInfo.CONFIG_SUPPORT_AUDIO);
		
		//����ͨ����ʽ  0:�ƶ��绰    1:voip�绰
		String audoMode = getAudioMode();
		if(!TextUtils.isEmpty(audoMode)) {
			DeviceInfo.CONFIG_AUDIO_MODE = Integer.parseInt(audoMode);  
		}
		Zed3Log.debug(LOG_TAG, "����ͨ����ʽDeviceInfo#CONFIG_AUDIO_MODE = " + DeviceInfo.CONFIG_AUDIO_MODE);
		
		//��������
		DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE = getAudioConference();
		Zed3Log.debug(LOG_TAG, "��������DeviceInfo#CONFIG_SUPPORT_AUDIO_CONFERENCE = " + DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE);
		
		//��������
		DeviceInfo.AUTORUN_REMOTE = isStartGQT();
		Zed3Log.debug(LOG_TAG, "��������DeviceInfo#AUTORUN_REMOTE = " + DeviceInfo.AUTORUN_REMOTE);
		
		//���������
		DeviceInfo.CONFIG_CHECK_UPGRADE = isCheckUpdate();
		Zed3Log.debug(LOG_TAG, "���������DeviceInfo#CONFIG_CHECK_UPGRADE = " + DeviceInfo.CONFIG_CHECK_UPGRADE);
		
		//�������
		DeviceInfo.ENCRYPT_REMOTE = getEncryptRemote();
		Zed3Log.debug(LOG_TAG, "�������DeviceInfo#ENCRYPT_REMOTE = " + DeviceInfo.ENCRYPT_REMOTE);
		
		//�Խ���ͼģʽ
		DeviceInfo.CONFIG_SUPPORT_PTTMAP = getPttMapMode();
		Zed3Log.debug(LOG_TAG, "�Խ���ͼģʽDeviceInfo#CONFIG_SUPPORT_PTTMAP = " + DeviceInfo.CONFIG_SUPPORT_PTTMAP);
		
		//ͼƬ�Ĵ�  Ϊ0��ʾû��ͼƬ�Ĵ�Ȩ�ޣ�Ϊ1��ʾ��ͼƬ�Ĵ�Ȩ�ޣ�Ϊ�ձ�ʾδ���塣
		DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD = getPicUpload();
		Zed3Log.debug(LOG_TAG, "ͼƬ�Ĵ�DeviceInfo#CONFIG_SUPPORT_PICTURE_UPLOAD = " + DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD);
		
		//����Ϣ
		DeviceInfo.CONFIG_SUPPORT_IM = getSupportSMS();
		Zed3Log.debug(LOG_TAG, "����ϢDeviceInfo#CONFIG_SUPPORT_IM = " + DeviceInfo.CONFIG_SUPPORT_IM);
		
		//GPS
		DeviceInfo.GPS_REMOTE = getGpsRemote();
		Zed3Log.debug(LOG_TAG, "GPS DeviceInfo#GPS_REMOTE = " + DeviceInfo.GPS_REMOTE);
	}
	
	private boolean existControlParams() {
		SharedPreferences defaultSp = getDefaultSharedPreferences();
		Map<String, Object> map = (Map<String, Object>) defaultSp.getAll();
		return (map.size() > 0);
	}

	/**
	 * �Ƿ���ڵ�¼���� 
	 */
	public boolean existLoginParams(){
		AutoConfigManager  ACM = new AutoConfigManager(SipUAApp.mContext);
		return !(TextUtils.isEmpty(ACM.fetchLocalServer())
				&& TextUtils.isEmpty(ACM.fetchLocalPwd())&& TextUtils
					.isEmpty(ACM.fetchLocalUserName()));
	}
	
	private SharedPreferences getDefaultSharedPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(SipUAApp.getAppContext());
	}
	
	private Editor getDefaultSharedPreferencesEdit(){
		SharedPreferences defaultSp = getDefaultSharedPreferences();
		return defaultSp.edit();
	}
	
	public void saveString(String key,String value){
		Editor editor = getDefaultSharedPreferencesEdit();
		
		editor.putString(key, value);
		
		editor.commit();
	}
	
	public void saveBoolean(String key,boolean value){
		Editor editor = getDefaultSharedPreferencesEdit();
		
		editor.putBoolean(key, value);
		
		editor.commit();
	}
	
	public boolean getBoolean(String key){
		return getDefaultSharedPreferences().getBoolean(key, false);
	}
	
	private int getInt(String key) {
		return getDefaultSharedPreferences().getInt(key, -1);
	}
	
	private String getString(String key) {
		return getDefaultSharedPreferences().getString(key, null);
	}
	
	public void saveInt(String key,int value){
		Editor editor = getDefaultSharedPreferencesEdit();
		
		editor.putInt(key, value);
		
		editor.commit();
	}
	
	public static final class WorkerArgs {
		
		private static WorkerArgs sPool = new WorkerArgs();
		
		private String key;
		private String value;
		private Callback mCallback;
		
		public static WorkerArgs pool(){
			sPool.setKey(null);
			sPool.setValue(null);
			sPool.setCallback(null);
			return sPool;
		}
		
		public WorkerArgs setKey(String key){
			this.key = key;
			return this;
		}
		
		public WorkerArgs setValue(String value){
			this.value = value;
			return this;
		}
		
		public Callback getCallback() {
			return mCallback;
		}

		public WorkerArgs setCallback(Callback mCallback) {
			this.mCallback = mCallback;
			return this;
		}

		public String getKey(){
			return this.key;
		}
		
		public String getValue(){
			return this.value;
		}
		
		public interface Callback {
			public void callback(Editor editor);
		}
	}
}

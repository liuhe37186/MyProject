package com.zed3.sipua;
/**
 * @author Administrator
 * assets\config.iniĿ¼��ȡ
 * **/
import java.io.IOException;
import java.util.Properties;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.DeviceInfo;

public class LocalConfigSettings {
	public static void loadSettings(Context context) throws IOException{
		Properties properties = new Properties();
		properties.load(context.getAssets().open("config.ini"));
		DeviceInfo.CONFIG_UPDATE_URL = properties.getProperty("updateurl");//�Զ����µ�ַ
		DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN = properties.getProperty("autologin").trim().equals("1");//�Ƿ��Զ���¼
		DeviceInfo.CONFIG_CONFIG_URL = properties.getProperty("autoconfigurl");//�Զ����õ�ַ
		DeviceInfo.CONFIG_SUPPORT_UNICOM_PASSWORD = properties.getProperty("unicompassword").trim().equals("1");//��ͨ�ͷ�����
		DeviceInfo.CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS = properties.getProperty("unicomflowstatistics").trim().equals("1");//��ͨ����ͳ��
		DeviceInfo.CONFIG_SUPPORT_VIDEO=properties.getProperty("video").trim().equals("1");//�Ƿ�֧����Ƶͨ��
		DeviceInfo.CONFIG_SUPPORT_AUDIO=properties.getProperty("audio").trim().equals("1");//�Ƿ�֧������ͨ��
		DeviceInfo.CONFIG_AUDIO_MODE=Integer.parseInt(properties.getProperty("audiomode").trim());//����ͨ����ʽ
		//������ר�ñ���  �뱾���޹�
		DeviceInfo.CONFIG_SUPPORT_AUTORUN=properties.getProperty("autorun").trim().equals("1");//�Ƿ񿪻�����
		DeviceInfo.CONFIG_CHECK_UPGRADE=properties.getProperty("checkupgrade").trim().equals("1");//������
		DeviceInfo.CONFIG_SUPPORT_ENCRYPT=properties.getProperty("encrypt").trim().equals("1");//�������
		DeviceInfo.CONFIG_SUPPORT_PTTMAP=properties.getProperty("pttmap").trim().equals("1");//��ͼģʽ
		DeviceInfo.CONFIG_GPS=Integer.parseInt(properties.getProperty("gps").trim());//gps��ʽ ������
		DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE=properties.getProperty("audioconference").trim().equals("1");//��������
		DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD=properties.getProperty("pictureupload").trim().equals("1");//ͼƬ�Ĵ�
		DeviceInfo.CONFIG_SUPPORT_IM=properties.getProperty("im").trim().equals("1");//����Ϣ֧��
		DeviceInfo.CONFIG_SUPPORT_EMERGENYCALL=properties.getProperty("emergenycall").trim().equals("1");//һ���澯
		DeviceInfo.CONFIG_SUPPORT_HOMEKEY_BLOCK=properties.getProperty("homekeyblock").trim().equals("1");//Home����
		DeviceInfo.CONFIG_SUPPORT_VAD=properties.getProperty("vad").trim().equals("1");//�������
		SharedPreferences mSharedPreferences = context.getSharedPreferences(Settings.sharedPrefsFile,0);
		//����ͨ�����ͣ��û����ú��ٱ��� add by liangzhang 2014-12-29
		if (mSharedPreferences.getBoolean("isFirstLogin", true)) {
			mSharedPreferences.edit().putString(Settings.PHONE_MODE,
					DeviceInfo.CONFIG_AUDIO_MODE == 0 ? "0" : "1").commit();
		}
		
		if(!DeviceInfo.CONFIG_SUPPORT_VAD){
			
			Editor edit = mSharedPreferences.edit();
			edit.putString(Settings.AUDIO_VADCHK, "0");
		}else{
			Editor edit = mSharedPreferences.edit();
			edit.putString(Settings.AUDIO_VADCHK, "1");
		}
		DeviceInfo.CONFIG_SUPPORT_LOG=properties.getProperty("log").trim().equals("1");//log֧��
		DeviceInfo.CONFIG_SUPPORT_RATE_MONITOR=properties.getProperty("ratemonitor").trim().equals("1");//����������
		DeviceInfo.CONFIG_SUPPORT_REGISTER_INTERNAL=properties.getProperty("registerinternal").trim().equals("1");//ע����
		DeviceInfo.CONFIG_SUPPORT_AEC=properties.getProperty("aec").trim().equals("1");//��������
		DeviceInfo.CONFIG_SUPPORT_NS=properties.getProperty("ns").trim().equals("1");//����
		DeviceInfo.CONFIG_SUPPORT_BLUETOOTH=properties.getProperty("bluetooth").trim().equals("1");//����
	}
}

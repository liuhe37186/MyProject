package com.zed3.sipua;
/**
 * @author Administrator
 * assets\config.ini目录读取
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
		DeviceInfo.CONFIG_UPDATE_URL = properties.getProperty("updateurl");//自动更新地址
		DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN = properties.getProperty("autologin").trim().equals("1");//是否自动登录
		DeviceInfo.CONFIG_CONFIG_URL = properties.getProperty("autoconfigurl");//自动配置地址
		DeviceInfo.CONFIG_SUPPORT_UNICOM_PASSWORD = properties.getProperty("unicompassword").trim().equals("1");//联通客服密码
		DeviceInfo.CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS = properties.getProperty("unicomflowstatistics").trim().equals("1");//联通流量统计
		DeviceInfo.CONFIG_SUPPORT_VIDEO=properties.getProperty("video").trim().equals("1");//是否支持视频通话
		DeviceInfo.CONFIG_SUPPORT_AUDIO=properties.getProperty("audio").trim().equals("1");//是否支持语音通话
		DeviceInfo.CONFIG_AUDIO_MODE=Integer.parseInt(properties.getProperty("audiomode").trim());//语音通话方式
		//服务器专用变量  与本地无关
		DeviceInfo.CONFIG_SUPPORT_AUTORUN=properties.getProperty("autorun").trim().equals("1");//是否开机启动
		DeviceInfo.CONFIG_CHECK_UPGRADE=properties.getProperty("checkupgrade").trim().equals("1");//检查更新
		DeviceInfo.CONFIG_SUPPORT_ENCRYPT=properties.getProperty("encrypt").trim().equals("1");//信令加密
		DeviceInfo.CONFIG_SUPPORT_PTTMAP=properties.getProperty("pttmap").trim().equals("1");//地图模式
		DeviceInfo.CONFIG_GPS=Integer.parseInt(properties.getProperty("gps").trim());//gps方式 服务器
		DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE=properties.getProperty("audioconference").trim().equals("1");//语音会议
		DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD=properties.getProperty("pictureupload").trim().equals("1");//图片拍传
		DeviceInfo.CONFIG_SUPPORT_IM=properties.getProperty("im").trim().equals("1");//短消息支持
		DeviceInfo.CONFIG_SUPPORT_EMERGENYCALL=properties.getProperty("emergenycall").trim().equals("1");//一键告警
		DeviceInfo.CONFIG_SUPPORT_HOMEKEY_BLOCK=properties.getProperty("homekeyblock").trim().equals("1");//Home屏蔽
		DeviceInfo.CONFIG_SUPPORT_VAD=properties.getProperty("vad").trim().equals("1");//静音检测
		SharedPreferences mSharedPreferences = context.getSharedPreferences(Settings.sharedPrefsFile,0);
		//保存通话类型，用户设置后不再保存 add by liangzhang 2014-12-29
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
		DeviceInfo.CONFIG_SUPPORT_LOG=properties.getProperty("log").trim().equals("1");//log支持
		DeviceInfo.CONFIG_SUPPORT_RATE_MONITOR=properties.getProperty("ratemonitor").trim().equals("1");//速率悬浮窗
		DeviceInfo.CONFIG_SUPPORT_REGISTER_INTERNAL=properties.getProperty("registerinternal").trim().equals("1");//注册间隔
		DeviceInfo.CONFIG_SUPPORT_AEC=properties.getProperty("aec").trim().equals("1");//回声消除
		DeviceInfo.CONFIG_SUPPORT_NS=properties.getProperty("ns").trim().equals("1");//降噪
		DeviceInfo.CONFIG_SUPPORT_BLUETOOTH=properties.getProperty("bluetooth").trim().equals("1");//蓝牙
	}
}

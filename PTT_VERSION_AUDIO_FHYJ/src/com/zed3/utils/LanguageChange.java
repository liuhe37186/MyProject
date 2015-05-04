package com.zed3.utils;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.zed3.sipua.ui.Settings;

public class LanguageChange {
	
	public static void upDateLanguage(Context context){
		//GQT 英文版 根据用户的选择，设置应用的语言
		SharedPreferences sharedPreferences = context.getSharedPreferences(Settings.sharedPrefsFile,
						Context.MODE_PRIVATE);
		int languageId  = sharedPreferences.getInt("languageId", 0);
		System.out.println(languageId+"ladddd");
		Resources resources = context.getResources();//获取资源对象
		Configuration config = resources.getConfiguration();//获取配置对象
		DisplayMetrics dm = resources.getDisplayMetrics();//获取屏幕分辨率
		switch (languageId ) {
			case 0:
				config.locale = Locale.getDefault();//跟随系统
				break;
			case 1:
				config.locale = Locale.SIMPLIFIED_CHINESE;//简体中文
				break;
			case 2:
				config.locale = Locale.ENGLISH;//英文
				break;
			default:
				break;
		}
		resources.updateConfiguration(config, dm);//更新资源文件的数据，更新应用的语言
	}

}

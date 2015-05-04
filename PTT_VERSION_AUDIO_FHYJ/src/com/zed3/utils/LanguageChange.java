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
		//GQT Ӣ�İ� �����û���ѡ������Ӧ�õ�����
		SharedPreferences sharedPreferences = context.getSharedPreferences(Settings.sharedPrefsFile,
						Context.MODE_PRIVATE);
		int languageId  = sharedPreferences.getInt("languageId", 0);
		System.out.println(languageId+"ladddd");
		Resources resources = context.getResources();//��ȡ��Դ����
		Configuration config = resources.getConfiguration();//��ȡ���ö���
		DisplayMetrics dm = resources.getDisplayMetrics();//��ȡ��Ļ�ֱ���
		switch (languageId ) {
			case 0:
				config.locale = Locale.getDefault();//����ϵͳ
				break;
			case 1:
				config.locale = Locale.SIMPLIFIED_CHINESE;//��������
				break;
			case 2:
				config.locale = Locale.ENGLISH;//Ӣ��
				break;
			default:
				break;
		}
		resources.updateConfiguration(config, dm);//������Դ�ļ������ݣ�����Ӧ�õ�����
	}

}

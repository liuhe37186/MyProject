package com.zed3.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Settings;

public class DialogMessageTool {
	/**
	 * �Ż�Ӣ��״̬�£���ʾ�Ի�������ʾ��ʾ����ʷֿ������⣨����Ӣ��״̬�µ��ã�
	 * 
	 * @param str
	 *            ��Ҫ��ʾ���ַ���
	 * @param width ��ͼ�Ŀ�ȣ�TextView��
	 * 
	 * @param textSize �����С
	 * 
	 * @return String ��������֮����ַ���
	 * 
	 * 
	 */

	public static String getString(int width, float textSize, String str) {
		// �жϴ�����ַ�����Ӣ�Ļ������ĵ�
		SharedPreferences sharedPreferences = SipUAApp.mContext
				.getSharedPreferences(Settings.sharedPrefsFile,
						Context.MODE_PRIVATE);
		int languageId = sharedPreferences.getInt("languageId", 0);
		Resources resources = SipUAApp.mContext.getResources();// ��ȡ��Դ����
		Configuration config = resources.getConfiguration();// ��ȡ���ö���
		AlertDialog.Builder dialog = new AlertDialog.Builder(SipUAApp.mContext);
		if (languageId == 2 || languageId == 0
				&& config.locale.getDefault().getLanguage().equals("en")) {
			StringBuffer sb = new StringBuffer();// �����洢�����ַ���
			StringBuffer sb2 = new StringBuffer();// �����洢ÿһ�е��ַ���
			str = newString(str);
			String[] s = str.split(" ");// ���ַ��������ָ����ַ�������
			TextPaint paint = new TextPaint();// �����ַ������ʣ����������ַ�����ռ��Ļ�Ŀ��
			paint.setTextSize(textSize);// �����ַ����������С
			float space = paint.measureText(" ");// �����ո��ַ�����ռ����Ļ���
			for (int i = 0; i < s.length; i++) {
				float length = paint.measureText(s[i]);
				float length3 = paint.measureText(sb2.toString());
				if ((length3 + length + space) < width) {
					if (i == 0) {
						sb.append(s[i]);
						sb2.append(s[i]);
					} else {
						sb.append(" " + s[i]);
						sb2.append(" " + s[i]);
					}

				} else {
					sb.append("\n" + s[i]);
					sb2 = new StringBuffer();
					sb2.append(s[i]);
				}
			}
			return sb.toString();
		} else {
			return str;
		}
	}
	
	private static String newString(String str){
		String string = "";
		string = str.replaceAll("\\,", ", ");
		string = string.replaceAll("\\.", ". ");
		string = string.replaceAll("\\?", "? ");
		string = string.replaceAll("\\!", "! ");
		return string;
	}
}

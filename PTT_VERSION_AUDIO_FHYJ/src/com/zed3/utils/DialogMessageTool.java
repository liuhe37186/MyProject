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
	 * 优化英文状态下，提示对话框中显示提示语，单词分开的问题（仅在英文状态下调用）
	 * 
	 * @param str
	 *            需要显示的字符串
	 * @param width 视图的宽度（TextView）
	 * 
	 * @param textSize 字体大小
	 * 
	 * @return String 返回修饰之后的字符串
	 * 
	 * 
	 */

	public static String getString(int width, float textSize, String str) {
		// 判断传入的字符串是英文还是中文的
		SharedPreferences sharedPreferences = SipUAApp.mContext
				.getSharedPreferences(Settings.sharedPrefsFile,
						Context.MODE_PRIVATE);
		int languageId = sharedPreferences.getInt("languageId", 0);
		Resources resources = SipUAApp.mContext.getResources();// 获取资源对象
		Configuration config = resources.getConfiguration();// 获取配置对象
		AlertDialog.Builder dialog = new AlertDialog.Builder(SipUAApp.mContext);
		if (languageId == 2 || languageId == 0
				&& config.locale.getDefault().getLanguage().equals("en")) {
			StringBuffer sb = new StringBuffer();// 用来存储整个字符串
			StringBuffer sb2 = new StringBuffer();// 用来存储每一行的字符串
			str = newString(str);
			String[] s = str.split(" ");// 将字符串整个分隔成字符串数组
			TextPaint paint = new TextPaint();// 定义字符串画笔，用来测量字符串所占屏幕的宽度
			paint.setTextSize(textSize);// 设置字符串的字体大小
			float space = paint.measureText(" ");// 测量空格字符串所占的屏幕宽度
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

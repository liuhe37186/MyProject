package com.zed3.sipua.ui.contact;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MeetingCompareTool implements Comparator<Map<String, Object> > {
	private static MeetingCompareTool ct = null;
	public static MeetingCompareTool getInstance(){
		if(ct == null){
			ct = new MeetingCompareTool();
		}
		return ct;
	}
	public  List<Map<String, Object>> sortByDefault(List<Map<String, Object>> list){
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if(list == null  || list.size() < 1) return result;
		for(int i= 0;i<list.size();i++){
			if(i == 0){
				result.add(list.get(i));
			}else{
				result.add(findPos(result,list.get(i)), list.get(i));
			}
		}
		return result;
	}
	private  int findPos(List<Map<String, Object>> list,Map<String, Object> gli){
		for(int i = 0;i<list.size();i++){
			if(compare(list.get(i), gli)>0){
				return i;
			}
		}
		return list.size();
	}
	@Override
	public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
		 int flag = 0;
		 if (Collator.getInstance(java.util.Locale.CHINA).compare(lhs.get("title"), rhs.get("title")) < 0) {
	            flag = -1;
	        } else if (Collator.getInstance(java.util.Locale.CHINA).compare(lhs.get("title"), rhs.get("title")) > 0) {
	            flag = 1;
	        }
		return flag;
	}
}

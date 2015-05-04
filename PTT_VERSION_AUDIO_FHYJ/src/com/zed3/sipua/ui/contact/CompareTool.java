package com.zed3.sipua.ui.contact;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.zed3.sipua.ui.lowsdk.ContactPerson;

public class CompareTool implements Comparator<ContactPerson> {
	private static CompareTool ct = null;
	public static CompareTool getInstance(){
		if(ct == null){
			ct = new CompareTool();
		}
		return ct;
	}
	public  List<ContactPerson> sortByDefault(List<ContactPerson> list){
		List<ContactPerson> result = new ArrayList<ContactPerson>();
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
	private  int findPos(List<ContactPerson> list,ContactPerson gli){
		for(int i = 0;i<list.size();i++){
			if(compare(list.get(i), gli)>0){
				return i;
			}
		}
		return list.size();
	}
	@Override
	public int compare(ContactPerson lhs, ContactPerson rhs) {
		 int flag = 0;
		 if (Collator.getInstance(java.util.Locale.CHINA).compare(lhs.getContact_name(), rhs.getContact_name()) < 0) {
	            flag = -1;
	        } else if (Collator.getInstance(java.util.Locale.CHINA).compare(lhs.getContact_name(), rhs.getContact_name()) > 0) {
	            flag = 1;
	        }
		return flag;
	}
}

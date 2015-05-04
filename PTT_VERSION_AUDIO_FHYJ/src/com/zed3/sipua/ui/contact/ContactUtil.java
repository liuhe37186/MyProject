package com.zed3.sipua.ui.contact;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;
import org.zoolu.tools.MyLog;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Xml;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.contant.Contants;
import com.zed3.sipua.ui.ParseXML;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.ContactManager;
import com.zed3.sipua.ui.lowsdk.ContactPerson;

public class ContactUtil {

	private static List<Map<String, Object>> mData;
	private static Context mContext;
	// public static final Object USER_NUMBER = "info";
	// public static final String USER_NAME = "title";
	public static final String USER_NAME = "title";
	public static final String USER_NUMBER = "info";

	public static final String OLD_NAME = "oldName";
	public static final String NEW_NAME = "newName";
	public static final String INDEX = "index";

	public static boolean isContactsHasNewChanged = false;
	private static Intent intent;

	static {
		mContext = SipUAApp.mContext;
		mData = new ArrayList<Map<String, Object>>();
	}

	public static List<Map<String, Object>> getUsers() {
		if (mData.size() == 0) {
			ReadContact();
		}
		return mData;
	}

	public static String getUserName(String userNumber) {
		// TODO Auto-generated method stub
//		if (mData.size() == 0) {
//			ReadContact();
//		}
//		String userName = null;
//		for (Map<String, Object> map : mData) {
//			if (map.get(USER_NUMBER).equals(userNumber)) {
//				userName = (String) map.get(USER_NAME);
//				break;
//			}
//		}
		//modify by hu 2013-9-26
		String userName = null;
		ContactManager cm = new ContactManager(mContext);
		userName =cm.queryNameByNum(userNumber);
		if(TextUtils.isEmpty(userName)){
			userName = null;
		}
		return userName/* == null ? userNumber : userName*/;
	}

	public static boolean addContacts(Context applicationContext, String name,
			String number) {
		// TODO Auto-generated method stub
		ContactManager cm = new ContactManager(applicationContext);
		ContactPerson cp = new ContactPerson();
		cp.setContact_num(number);
		if(TextUtils.isEmpty(name)){
			cp.setContact_name(number);
		}else{
			cp.setContact_name(name);
		}
		cm.insertContact(cp);
		
		
		
//		  mData = new ArrayList<Map<String, Object>>(); 
//		  mData.clear();
////		  if(isContactsHasNewChanged) {
////		  
////		  } 
//		  mContext = applicationContext; 
//		  if (isContactsHasNewChanged) {
//			  ReadContact(); 
//		  }
//		 
//		//modify by hu 20130924
//		try {
//			// 如果未保存
//			if (!ContactUtil.add(name, number)) {
//				return false;
//			}
//			saveData();
////			ContactUtil.startContactActivity(Intent.FLAG_ACTIVITY_NEW_TASK);
////			applicationContext.startActivity(intent);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return true;

	}

	private static List<Map<String, Object>> ReadContact() {
		mData.clear();
		try {
			FileInputStream sFile = Receiver.mContext
					.openFileInput("contacts.xml");
			ParseXML parseXML = new ParseXML();
			parseXML.SetData(mData);
			android.util.Xml.parse(sFile, Xml.Encoding.UTF_8, parseXML);
			sFile.close();

		} catch (FileNotFoundException e) {
			MyLog.i("ReadContact", "contacts.xml is null ");
			//e.printStackTrace();

		} catch (IOException e) {

			//e.printStackTrace();

		} catch (SAXException e) {
			//e.printStackTrace();
		}

		return mData;
	}

	public static void saveData() throws FileNotFoundException {
		//
		XmlSerializer serializer = Xml.newSerializer();
		FileOutputStream outStream = SipUAApp.mContext.openFileOutput(
				"contacts.xml", Context.MODE_PRIVATE);

		try {
			serializer.setOutput(outStream, "UTF-8");
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "contactsInfo");
			// 获得数组大小
			for (int i = 0; i < mData.size(); i++) {

				// startTag -> contact
				serializer.startTag("", "contacts");

				serializer.startTag("", "name");
				// serializer.entityRef("111");
				// serializer.comment((String) mData.get(i).get("title"));
				serializer.text((String) mData.get(i).get("title"));
				serializer.endTag("", "name");

				serializer.startTag("", "phone");
				// serializer.entityRef("111");
				// serializer.comment((String) mData.get(i).get("info"));
				serializer.text((String) mData.get(i).get("info"));
				serializer.endTag("", "phone");
				// // 写属性值
				serializer.endTag("", "contacts");
			}
			serializer.endTag("", "contactsInfo");
			serializer.endDocument();
			outStream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		SipUAApp.mContext.sendBroadcast(new Intent(Contants.ACTION_CONTACT_CHANGED));
		isContactsHasNewChanged = true;
	}

	public static void changeName(Context context2, int index2, String newName) {
		// TODO Auto-generated method stub
		mData.get(index2).put(USER_NAME, newName);
		try {
			saveData();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	public static void change(Context context2, int index2, String newName,String newNumber) {
		// TODO Auto-generated method stub
		mData.get(index2).put(USER_NAME, newName);
		mData.get(index2).put(USER_NUMBER, newNumber);
		try {
			saveData();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	public static boolean removeContact(int index) {
		// TODO Auto-generated method stub
		if (index > -1 && index < mData.size()) {
			mData.remove(index);
			try {
				saveData();
				return true;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		return false;
	}

	//add by oumogang 2013-05-10
		public static void replace(ArrayList<CharSequence> charSequenceArrayList) {
			// TODO Auto-generated method stub
			try {
				mData.clear();
				for (CharSequence charSequence : charSequenceArrayList) {
					if (charSequence != null && !charSequence.equals("")) {
						String[] split = charSequence.toString().split(",");
						if (split != null && split.length == 2) {
//							add(split[0], split[1]);
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("info", split[0]);
							map.put("title", split[1]);
							map.put("img", R.drawable.icon_contact);
							mData.add(map);
						}
					}
				}
				saveData();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//add by oumogang 2013-05-10
		public static void deleteAll() {
			// TODO Auto-generated method stub
			mData.clear();
			try {
				saveData();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public static List<Map<String, Object>> copyUsers(List<Map<String, Object>> copyData ) {
			if (mData.size() == 0) {
				ReadContact();
			}
			for (int i = 0; i < mData.size(); i++) {
				copyData.add(mData.get(i));
				
			}
			return copyData;
		}

		public static void removeContact(String contact_num) {
			// TODO Auto-generated method stub
			if (contact_num == null || contact_num.equals("")) {
				return;
			}
			for (int i = 0; i < mData.size(); i++) {
				String num = (String) mData.get(i).get("info");
				if (num.equals(contact_num)) {
					mData.remove(i);
					try {
						saveData();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}
			}
		}

}

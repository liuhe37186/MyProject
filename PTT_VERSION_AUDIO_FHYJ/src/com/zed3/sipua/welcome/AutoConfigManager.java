package com.zed3.sipua.welcome;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.MyLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zed3.flow.FlowStatistics;
import com.zed3.location.MemoryMg;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.LogUtil;

public class AutoConfigManager {
	Context context;
	String userName;
	String pwd;
	IAutoConfigListener listener;

	public AutoConfigManager(Context ctx) {
		context = ctx;
	}

	public void setOnFetchListener(IAutoConfigListener listener) {
		if (listener != null) {
			this.listener = listener;
		}
	}

	public void fetchConfig() {
		parseResponce(get());
	}

	private String fetchUserName() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ServerSet", Context.MODE_PRIVATE);
		return sharedPreferences.getString("UserName", "");
	}

	private String fetchPwd() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ServerSet", Context.MODE_PRIVATE);
		return sharedPreferences.getString("Password", "");
	}

	private String fetchServer() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ServerSet", Context.MODE_PRIVATE);
		return sharedPreferences.getString("IP", "");
	}

	private String fetchPort() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ServerSet", Context.MODE_PRIVATE);
		return sharedPreferences.getString("Port", "");
	}

	private void save(String server, String port, String userName, String pwd) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ServerSet", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();// ��ȡ�༭��
		editor.putString("IP", server);
		editor.putString("Port", port);
		editor.putString("UserName", userName);
		editor.putString("Password", pwd);
		editor.commit();// �ύ�޸�
	}

	/**
	 * <response> <sipserver>192.168.50.147</sipserver> <sipport>5060</sipport>
	 * <user>1001</user> <passwd>0000</passwd> </response>
	 * 
	 * 
	 * **/
	private String get() {
		String strUrl = DeviceInfo.CONFIG_CONFIG_URL.trim();
		String getUrl = packageUrl(strUrl);
		MyLog.e("AutoConfigManager", "url = " + getUrl);
		String result = "";
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				5000);
		HttpGet httpGet = new HttpGet(getUrl);
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code == 200) {
				HttpEntity entity = httpResponse.getEntity();
				result = EntityUtils.toString(entity).trim();
				MyLog.e("AutoConfigManager", "200 ok.HttpResponse string is:"
						+ "\r\n" + result);
				// ����ͳ��
				FlowStatistics.DownLoad_APK += entity.getContentLength();
			} else if (code == 404) {
				if (listener != null)
					listener.FetchConfigFailed();
			}
		} catch (Exception e) {
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			if (listener != null)
				listener.TimeOut();
		}
		return result;
	}

	// ����IOS������udid add by liangzhang 2014-09-18
	// simcardno=&imsi=460012414705635&phoneno=&imei=352205050896488&mac=&udid=
	private String packageUrl(String url) {
		StringBuffer sb = new StringBuffer();
		if (!TextUtils.isEmpty(url) && url.startsWith("http://")) {
			sb.append(url).append("?simcardno=").append(DeviceInfo.SIMNUM)
					.append("&imsi=").append(DeviceInfo.IMSI)
					.append("&phoneno=").append(DeviceInfo.PHONENUM)
					.append("&imei=").append(DeviceInfo.IMEI).append("&mac=")
					.append(DeviceInfo.MACADDRESS).append("&udid=")
					.append(DeviceInfo.UDID);
		}
		// add by liangzhang 2014-09-25 ȥ������url�е�����null�ֶ�
		String result = sb.toString();
		if (!result.equals("")) {
			result = result.replaceAll("null", "").replaceAll("NULL", "");
		}
		return result;
	}

	private void parseResponce(String str) {
		LogUtil.makeLog(" AutoConfigManager ", " parseResponce");
		// ����������ֵΪ��ʱ�����н��� modify by liangzhang 2014-10-14
		if (str == null || str.equals("")) {
			MyLog.e("AutoConfigManager", "responces is null");
			return;
		}
		// MyLog.e("AutoConfigManager", "responces is :" + str);
        //responces is :<response><sipserver>218.249.39.214</sipserver><sipport>5080</sipport>
		//<user>1002</user><passwd>1002</passwd><video>1</video>
		//<simcardno></simcardno><imsi></imsi><phoneno></phoneno>
		//<imei>862136021240790</imei><mac></mac>
		//<name>coolpad 7295+</name><audio></audio>
		//<audiomode></audiomode><autorun></autorun>
		//<checkupgrade></checkupgrade><encrypt></encrypt>
		//<pttmap></pttmap><gps></gps><audioconference></audioconference>
		//<pictureupload></pictureupload></response>
		
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document document = null;
		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		InputSource is = new InputSource();
		String a = "", b = "", c = "", d = "", v = "";
		is.setCharacterStream(new StringReader(str));
		try {
			document = builder.parse(is);
			Element root = document.getDocumentElement();
			NodeList nodes = root.getChildNodes();// .getElementsByTagName("response");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element elem = (Element) (nodes.item(i));
				if (elem.getNodeName().equals("sipserver")) {
					String server = elem.getChildNodes().item(0).getNodeValue();
					a = server;
				} else if (elem.getNodeName().equals("sipport")) {
					String port = elem.getChildNodes().item(0).getNodeValue();
					b = port;
				} else if (elem.getNodeName().equals("user")) {
					String user = elem.getChildNodes().item(0).getNodeValue();
					c = user;
				} else if (elem.getNodeName().equals("passwd")) {
					String passwd = elem.getChildNodes().item(0).getNodeValue();
					d = passwd;
				} else if (elem.getNodeName().equals("video")) {//�������˿�����Ƶ����
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String video = elem.getChildNodes().item(0)
								.getNodeValue();
						v = video;
					} else {
						v = "";
					}
					DeviceInfo.CONFIG_SUPPORT_VIDEO = (v != null && v.equals("1"));
					MyLog.e("AutoConfigManager", "video===>" + v);
					
					AutoLoginService.getDefault().saveVideoSwitch(DeviceInfo.CONFIG_SUPPORT_VIDEO);
					
				}
				else if (elem.getNodeName().equals("audio")) {//�������˿�������ͨ������
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String audio = elem.getChildNodes().item(0)
								.getNodeValue();
						v = audio;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0)
						DeviceInfo.CONFIG_SUPPORT_AUDIO = (v != null && v.equals("1"));
					MyLog.e("AutoConfigManager", "audio===>" + v);
					
					AutoLoginService.getDefault().saveAudioSwitch(DeviceInfo.CONFIG_SUPPORT_AUDIO);
					
				}else if (elem.getNodeName().equals("name")) {//������ʾ
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String name = elem.getChildNodes().item(0)
								.getNodeValue();
						DeviceInfo.AutoVNoName = name;
					}
					
				}
				else if (elem.getNodeName().equals("audiomode")) {
					// ����ͨ����ʽ  0:�ƶ��绰    1:voip�绰
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String val = elem.getChildNodes().item(0)
								.getNodeValue();
						v = val;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0){
						
						DeviceInfo.CONFIG_AUDIO_MODE = Integer.parseInt(v);
//						guojunfeng 0513 ����ͨ������
//						MemoryMg.getInstance().PhoneType = -1;//���α���ͨ������ѡ��
					}
					//����ͨ�����ͣ��û����ú��ٱ��� add by liangzhang 2014-12-29
					if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("isFirstLogin", true)) {
						AutoLoginService.getDefault().saveAudioMode(DeviceInfo.CONFIG_AUDIO_MODE);
					}
					MyLog.e("AutoConfigManager", "audiotype===>" + v);
				}				
				else if (elem.getNodeName().equals("audioconference")) {
					//��������
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String val = elem.getChildNodes().item(0)
								.getNodeValue();
						v = val;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0)
						DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE = (v != null && v
								.equals("1"));
					MyLog.e("AutoConfigManager", "audioconference===>" + v);
					AutoLoginService.getDefault().saveAudioConference(DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE);
				}
				else if (elem.getNodeName().equals("autorun")) {
					// ��������[ֱ���޸�sharedpreference����] key:"autorunkey"
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String val = elem.getChildNodes().item(0)
								.getNodeValue();
						v = val;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0) {
						DeviceInfo.AUTORUN_REMOTE = (v != null && v.equals("1"));
						AutoLoginService.getDefault().saveStartDevice(v);
						if(DeviceInfo.AUTORUN_REMOTE){
							Editor it = PreferenceManager
									.getDefaultSharedPreferences(context).edit();
							it.putString("autorunkey", "1");
							it.putBoolean("tempNum", true);
//							AutoLoginService.getDefault().saveStartDevice("1");
							it.commit();
						}else{
							SharedPreferences sp = PreferenceManager
									.getDefaultSharedPreferences(context);
							if(sp.getBoolean("tempNum", false)){
								Editor it = sp.edit();
								it.putBoolean("tempNum", false);
								it.putString("autorunkey", "0");
								it.commit();
							}
						}
						
						
					}
					MyLog.e("AutoConfigManager", "autorun===>" + v);
				}
				else if (elem.getNodeName().equals("checkupgrade")) {
					// ���������  Ϊ0��ʾû�г��������Ȩ�ޣ�Ϊ1��ʾ�г��������Ȩ�ޣ�Ϊ�ձ�ʾδ���塣
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String val = elem.getChildNodes().item(0)
								.getNodeValue();
						v = val;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0) {
						DeviceInfo.CONFIG_CHECK_UPGRADE = (v != null && v.equals("1"));
					}
					
					AutoLoginService.getDefault().saveCheckUpdate(DeviceInfo.CONFIG_CHECK_UPGRADE);
					MyLog.e("AutoConfigManager", "updatecheck===>" + v);
				}
				else if (elem.getNodeName().equals("encrypt")) {
				    //�������
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String val = elem.getChildNodes().item(0)
								.getNodeValue();
						v = val;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0) {
						DeviceInfo.ENCRYPT_REMOTE = (v != null && v.equals("1"));
						AutoLoginService.getDefault().saveEncryptRemote(DeviceInfo.ENCRYPT_REMOTE);
						if(DeviceInfo.ENCRYPT_REMOTE) {
							SharedPreferences sp = SipUAApp.getAppContext().getSharedPreferences(Settings.sharedPrefsFile, Context.MODE_PRIVATE);
							sp.edit().putBoolean(Settings.PREF_MSG_ENCRYPT, true).commit();
							sp.edit().putBoolean("tempMem", true).commit();
						} else {
							SharedPreferences sp = SipUAApp.getAppContext().getSharedPreferences(Settings.sharedPrefsFile, Context.MODE_PRIVATE);
							if(sp.getBoolean("tempMem", false)){
								sp.edit().putBoolean("tempMem", false).commit();
								sp.edit().putBoolean(Settings.PREF_MSG_ENCRYPT, false).commit();
							}
						}
						MyLog.e("AutoConfigManager", "msgencrypt===>" + v);
					}
				}
				else if (elem.getNodeName().equals("pttmap")) {
				    //�Խ���ͼģʽ
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String val = elem.getChildNodes().item(0)
								.getNodeValue();
						v = val;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0)
				        DeviceInfo.CONFIG_SUPPORT_PTTMAP = (v != null && v.equals("1"));
					MyLog.e("AutoConfigManager", "pttmap===>" + v);
					
					AutoLoginService.getDefault().savePttMapMode(DeviceInfo.CONFIG_SUPPORT_PTTMAP);
					
				}
				else if (elem.getNodeName().equals("gps")) {
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String val = elem.getChildNodes().item(0)
								.getNodeValue();
						v = val;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0) {
						/*
						 * modify by liangzhang 2014-09-09
						 * DPMP�������ã���DeviceInfo.GPS_REMOTE��ֵ�ĺ������£� 0:���߱���λ�ϱ�����
						 * 1:ǿ�ưٶ����ܶ�λ 2:�����κδ��������ñ������� 3��ǿ�ưٶ�GPS��λ 4��ǿ��GPS��λ�ϱ�
						 */
						DeviceInfo.GPS_REMOTE = Integer.parseInt(v);
						
						AutoLoginService.getDefault().saveGpsRemoteMode(DeviceInfo.GPS_REMOTE);
						
						/*
						 * ���ؿ������ã���Settings.PREF_LOCATEMODE��ֵ�ĺ������£� 0:GPS��λ
						 * 1:�ٶ����ܶ�λ 2:�ٶ�GPS��λ 3���Ӳ���λ
						 */
						if (DeviceInfo.GPS_REMOTE == 0) {
							MemoryMg.getInstance().GpsLocationModel = 3;// �ر�gps��λ
							Editor it = PreferenceManager
									.getDefaultSharedPreferences(context)
									.edit();
							it.putInt(Settings.PREF_LOCATEMODE, 3);
							it.commit();
						} else if (DeviceInfo.GPS_REMOTE == 1) {
							// ���ܹرն�λ���رչ��ܽ���
							MemoryMg.getInstance().GpsLocationModel = 1;// �ٶ����ܶ�λ
							Editor it = PreferenceManager
									.getDefaultSharedPreferences(context)
									.edit();
							it.putInt(Settings.PREF_LOCATEMODE, 1);
							it.commit();
						} else if (DeviceInfo.GPS_REMOTE == 3) {
							// ���ܹرն�λ���رչ��ܽ���
							MemoryMg.getInstance().GpsLocationModel = 2;// �ٶ�GPS��λ
							Editor it = PreferenceManager
									.getDefaultSharedPreferences(context)
									.edit();
							it.putInt(Settings.PREF_LOCATEMODE, 2);
							it.commit();
						} else if (DeviceInfo.GPS_REMOTE == 4) {
							// ���ܹرն�λ���رչ��ܽ��� add by liangzhang 2014-09-09
							MemoryMg.getInstance().GpsLocationModel = 0;// GPS��λ
							Editor it = PreferenceManager
									.getDefaultSharedPreferences(context)
									.edit();
							it.putInt(Settings.PREF_LOCATEMODE, 0);
							it.commit();
						} else {// DeviceInfo.GPS_REMOTE == 2ʱ���ñ�������
							//�ָ�ΪĬ��ֵ����config.ini�ļ������õ�ֵ
							int config_mode = 3;// ��¼config.ini�����õ�gpsģʽ
							if (DeviceInfo.CONFIG_GPS == 1) {
								config_mode = 0;
							} else if (DeviceInfo.CONFIG_GPS == 2) {
								config_mode = 1;
							} else if (DeviceInfo.CONFIG_GPS == 3) {
								config_mode = 2;
							} else if (DeviceInfo.CONFIG_GPS == 4) {
								config_mode = 3;
							}
							MemoryMg.getInstance().GpsLocationModel = config_mode;
							Editor it = PreferenceManager
									.getDefaultSharedPreferences(context)
									.edit();
							it.putInt(Settings.PREF_LOCATEMODE, config_mode);
							it.commit();
						}
					}
					MyLog.e("AutoConfigManager", "locationmodelval===>" + v);
				}
				else if (elem.getNodeName().equals("pictureupload")) {
				    //ͼƬ�Ĵ�  Ϊ0��ʾû��ͼƬ�Ĵ�Ȩ�ޣ�Ϊ1��ʾ��ͼƬ�Ĵ�Ȩ�ޣ�Ϊ�ձ�ʾδ���塣
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String val = elem.getChildNodes().item(0)
								.getNodeValue();
						v = val;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0) {
						DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD= (v != null && v.equals("1"));
					}
					MyLog.e("AutoConfigManager", "picmessage===>" + v);
					AutoLoginService.getDefault().savePicUpload(DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD);
				}
				else if (elem.getNodeName().equals("smsswitch")) {
					NodeList nl = elem.getChildNodes();
					if (nl.getLength() > 0) {
						String val = elem.getChildNodes().item(0)
								.getNodeValue();
						v = val;
					} else {
						v = "";
					}
					if (v != null && v.length() > 0) {
						DeviceInfo.CONFIG_SUPPORT_IM= (v != null && v.equals("1"));
					}
					
					AutoLoginService.getDefault().saveSupportSMS(DeviceInfo.CONFIG_SUPPORT_IM);
					MyLog.e("AutoConfigManager", "im===>" + v);
				}
				if (i == nodes.getLength() - 1) {
					save(a, b, c, d);
					if (listener != null) {
						listener.ParseConfigOK();
					}
				}
			}
		} catch (Exception e) {
			MyLog.e("AutoConfigManager", "parse error:"+e.toString());
			if (listener != null)// ����ʧ����ʾ
				listener.parseFailed();
		} 
	}

	public static void LoadSettings(Context context) {
			// ��ȡgvsת��ֱ�������
			MemoryMg.getInstance().GvsTransSize = PreferenceManager
					.getDefaultSharedPreferences(context).getString(
							"gvstransvideosizekey", "5");
			// gps����
			MemoryMg.getInstance().GpsLocationModel = PreferenceManager
					.getDefaultSharedPreferences(context).getInt(
							Settings.PREF_LOCATEMODE,
						Settings.DEFAULT_PREF_LOCATEMODE);
			// gps��λ���
			MemoryMg.getInstance().GpsSetTimeModel = PreferenceManager
					.getDefaultSharedPreferences(context).getInt(
							Settings.PREF_LOCSETTIME,
							Settings.DEFAULT_PREF_LOCSETTIME);
			// gps�ϱ����
			MemoryMg.getInstance().GpsUploadTimeModel = PreferenceManager
					.getDefaultSharedPreferences(context).getInt(
							Settings.PREF_LOCUPLOADTIME,
							Settings.DEFAULT_PREF_LOCUPLOADTIME);
			// ��ȡ������⿪������
			MemoryMg.getInstance().isAudioVAD = (PreferenceManager
					.getDefaultSharedPreferences(context)
					.getString(Settings.AUDIO_VADCHK,
							Settings.DEFAULT_VAD_MODE).equals("0") ? false:true);
			//��ȡע��ʱ����
			SipStack.default_expires = PreferenceManager
					.getDefaultSharedPreferences(context).getInt(
							Settings.PREF_REGTIME_EXPIRES, 1800);
			//���Ѳ���
			MemoryMg.getInstance().isMicWakeUp = PreferenceManager
					.getDefaultSharedPreferences(context).getBoolean(
							Settings.PREF_MICWAKEUP_ONOFF, true);
			//ͨ������ Ĭ��0���ƶ��绰
			MemoryMg.getInstance().PhoneType = Integer.parseInt(PreferenceManager
					.getDefaultSharedPreferences(context).getString(
							Settings.PHONE_MODE, Settings.DEFAULT_PHONE_MODE));
		
	}
	
	// ����MAC��ַ�ֶ� modify by liangzhang 2014-10-14
	public static final String LC_IMEI = "localimei";
	public static final String LC_IMSI = "localimsi";
	public static final String LC_MACADDRESS = "localmacaddress";
	public static final String LC_PHONENUM = "localphoneNum";
	public static final String LC_SIMNUM = "localsimnum";

	public void saveLocalconfig() {
		String imei = DeviceInfo.IMEI;
		String imsi = DeviceInfo.IMSI;
		String macaddress = DeviceInfo.MACADDRESS;
		String phonenum = DeviceInfo.PHONENUM;
		String simnum = DeviceInfo.SIMNUM;

		SharedPreferences settings = context.getSharedPreferences(
				Settings.sharedPrefsFile,
				Context.MODE_PRIVATE);
		Editor edit = settings.edit();
		edit.putString(LC_IMEI, imei);
		edit.putString(LC_IMSI, imsi);
		edit.putString(LC_MACADDRESS, macaddress);
		edit.putString(LC_PHONENUM, phonenum);
		edit.putString(LC_SIMNUM, simnum);
		edit.commit();
	}
	
	/**
	 * �ж��Ƿ�Ϊͬһ���ֻ��豸 modify by liangzhang 2014-10-14
	 * 
	 * @return ��õ�IMEI��MAC��ַ��������һ�����ϴε�¼��������ͬ�򷵻�true�����򷵻�false
	 * */
	public boolean isTheSameHandset() {
		SharedPreferences settings = context.getSharedPreferences(
				Settings.sharedPrefsFile, Context.MODE_PRIVATE);
		String lc_imei = settings.getString(LC_IMEI, "");
		String lc_macaddress = settings.getString(LC_MACADDRESS, "");
		if (lc_imei != null && !lc_imei.equals("")
				&& !lc_imei.equalsIgnoreCase("null")
				&& lc_imei.equals(DeviceInfo.IMEI))
			return true;
		if (lc_macaddress != null && !lc_macaddress.equals("")
				&& !lc_macaddress.equalsIgnoreCase("null")
				&& lc_macaddress.equals(DeviceInfo.MACADDRESS))
			return true;
		return false;
	}

	/**
	 * ����ICCID��IMSI�ж��Ƿ�Ϊͬһ��Sim�� add by liangzhang 2014-10-14
	 * 
	 * @param iccid
	 * @param imsi
	 * @return
	 */
	public boolean isTheSameSimCard(String iccid, String imsi) {
		SharedPreferences settings = context.getSharedPreferences(
				Settings.sharedPrefsFile, Context.MODE_PRIVATE);
		if (iccid != null && !iccid.equals("")
				&& !iccid.equalsIgnoreCase("null")) {
			String lc_simNum = settings.getString(LC_SIMNUM, "");
			if (!lc_simNum.equals(null) && !lc_simNum.equals("")
					&& lc_simNum.equals(iccid))
				return true;
		}
		if (imsi != null && !imsi.equals("")
				&& !imsi.equalsIgnoreCase("null")) {
			String lc_imsi = settings.getString(LC_IMSI, "");
			if (!lc_imsi.equals(null) && !lc_imsi.equals("")
					&& lc_imsi.equals(imsi))
				return true;
		}
		return false;
	}

	public void saveSetting() {
		String user = fetchUserName();
		String pwd = fetchPwd();
		String server = fetchServer();
		String port = fetchPort();

		SharedPreferences settings;
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		settings = context.getSharedPreferences(sharedPrefsFile,
				Context.MODE_PRIVATE);
		Editor edit = settings.edit();
		edit.putString(Settings.PREF_USERNAME, user);
		edit.putString(Settings.PREF_PASSWORD, pwd);
		edit.putString(Settings.PREF_SERVER, server);
		edit.putString(Settings.PREF_PORT, port);
		edit.commit();
	}

	public void saveSetting(String user, String pwd, String server, String port) {
		SharedPreferences settings;
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		settings = context.getSharedPreferences(sharedPrefsFile,
				Context.MODE_PRIVATE);
		Editor edit = settings.edit();
		edit.putString(Settings.PREF_USERNAME, user);
		edit.putString(Settings.PREF_PASSWORD, pwd);
		edit.putString(Settings.PREF_SERVER, server);
		edit.putString(Settings.PREF_PORT, port);
		edit.commit();
	}

	public String fetchLocalUserName() {
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				sharedPrefsFile, Context.MODE_PRIVATE);
		return sharedPreferences.getString(Settings.PREF_USERNAME, "");
	}

	public String fetchLocalPwd() {
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				sharedPrefsFile, Context.MODE_PRIVATE);
		return sharedPreferences.getString(Settings.PREF_PASSWORD, "");
	}

	public String fetchLocalServer() {
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				sharedPrefsFile, Context.MODE_PRIVATE);
		return sharedPreferences.getString(Settings.PREF_SERVER, "");
	}

	public String fetchLocalPort() {
		String sharedPrefsFile = "com.zed3.sipua_preferences";
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				sharedPrefsFile, Context.MODE_PRIVATE);
		return sharedPreferences.getString(Settings.PREF_PORT, "5060");
	}
	
	
}

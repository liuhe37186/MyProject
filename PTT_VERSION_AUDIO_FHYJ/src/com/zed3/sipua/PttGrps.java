/*
 * Add by zzhan
 * 
 * Class PttGrps describe ptt groups attributes.
 */
package com.zed3.sipua;

import java.util.Vector;

import org.zoolu.tools.MyLog;

import android.content.Intent;

import com.zed3.sipua.message.AlarmService;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LogUtil;

public class PttGrps {
	
	private static final String tag = "PttGrps";
	private Vector<PttGrp> grps;
	private PttGrp currentGrp;
	public String signalMulticastIP;
	public int signalMulticastPort;
	
	public PttGrps(){
		LogUtil.makeLog(tag, "new PttGrps()");
		grps = new Vector<PttGrp>();
		currentGrp = null;
		signalMulticastIP = new String();
		signalMulticastPort = 0;
	}
	private PttGrp parseGrpAttributes(String imp_group){
		String[] attributes = imp_group.split(",");
		if (attributes.length != 4)
			return null;
		
		PttGrp grp = new PttGrp();
		grp.grpName = attributes[0];
		grp.grpID = attributes[1];
		grp.level = Integer.valueOf(attributes[2]);
		grp.report_heartbeat = Integer.valueOf(attributes[3]);

		return grp;
	}
	
	public synchronized boolean ParseGrpInfo(String info){
		LogUtil.makeLog(tag, "pttGroupParse(info)");
		grps.clear();
		String category[] = info.split("\r\n");
		if (category.length < 1)
			return false;
		
		for (String str : category) {
			//group
			if (str.startsWith("group: ")){
				str = str.replaceFirst("group: ", "");
				String[] g3_groups = str.split(";");
				for (String g3_group : g3_groups){
					PttGrp grp = parseGrpAttributes(g3_group);
					if (grp == null)
						return false;
					
					grps.add(grp);
				}
			}
			//emergency-call
			else if (str.startsWith("emergency-call: ")){
				str = str.replaceFirst("emergency-call: ", "");
				PttGrp grp = parseGrpAttributes(str);
				if (grp == null)
					return false;
				
				grps.add(grp);
			}
			//guojunfeng add 2014-05-12  
//			group: name1,num1,level1,report-heartbeat1;name2,num2,level2,report-heartbeat2;name3,num3,level3,report-heartbeat3
//			emergency-call: name,num,0,report-heartbeat
//			alarm-svpnumber: svpnum
//			mms-defaultrecnum: num
//			http-port: port
//			https-port£ºport
			else if(str.startsWith("alarm-svpnumber")){
				DeviceInfo.svpnumber = str.split("alarm-svpnumber:")[1].trim();
			}
			else if(str.startsWith("mms-defaultrecnum")){
				DeviceInfo.defaultrecnum = str.split("mms-defaultrecnum:")[1].trim();
			}
			else if(str.startsWith("http-port")){
				DeviceInfo.http_port = str.split("http-port:")[1].trim();
			}
			else if(str.startsWith("https-port")){
				DeviceInfo.https_port = str.split("https-port:")[1].trim();
			}
			MyLog.e(" = ", DeviceInfo.svpnumber+"  "+DeviceInfo.defaultrecnum+
					"  "+DeviceInfo.http_port+"  "+DeviceInfo.https_port);
			
			
		}
		if(DeviceInfo.CONFIG_SUPPORT_EMERGENYCALL&&!DeviceInfo.svpnumber.equals("")&&!DeviceInfo.ISAlarmShowing){
			Intent intent2 = new Intent(SipUAApp.mContext, AlarmService.class);
			SipUAApp.mContext.startService(intent2);
			DeviceInfo.ISAlarmShowing = true;
		}
		return true;
	}
	
	public synchronized PttGrp GetCurGrp(){
		return currentGrp;
	}
	
	public synchronized void SetCurGrp(PttGrp grp){
		LogUtil.makeLog(tag, "SetCurGrp("+(grp==null?"null":grp.toString())+")");
		currentGrp = grp;
	}
	
	public synchronized PttGrp FirstGrp(){
		if (grps.size() > 0){
			return grps.elementAt(0);
		}
		else
			return null;
	}
	
	public synchronized int GetCount(){
		return grps.size();
	}
	
	public synchronized PttGrp GetGrpByID(String ID){
		if (grps.size() <= 0)
			return null;
		
		PttGrp grp = null;
		for (int i = 0; i < grps.size(); i++){
			if (grps.elementAt(i).grpID.equalsIgnoreCase(ID)){
				grp = grps.elementAt(i);
				break;
			}
		}
		
		return grp;
	}
	
	public synchronized PttGrp GetGrpByIndex(int index){
		PttGrp grp = null;
		if (index >= 0 && index < GetCount()){
			grp = grps.elementAt(index);
		}
		
		return grp;
	}
}

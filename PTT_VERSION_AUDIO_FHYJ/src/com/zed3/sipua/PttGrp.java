package com.zed3.sipua;

import java.util.HashMap;

public class PttGrp {
	
	public enum E_Grp_State{
		GRP_STATE_SHOUDOWN,
		GRP_STATE_IDLE,
		GRP_STATE_INITIATING,
		GRP_STATE_TALKING,
		GRP_STATE_LISTENING,
		GRP_STATE_QUEUE;
	}
	
	public String grpName = "--";
	public String grpID = "--";
	public String speaker = "--";
	public String speakerN = "--";
	public int report_heartbeat;
	public int update_heartbeat;
	public int level;
	public long lastRcvTime;
	public E_Grp_State state = E_Grp_State.GRP_STATE_SHOUDOWN;
	public Object oVoid;
	public boolean isCreateSession;
	
	private static HashMap<String, Integer> grpIsAnswsered;
	
	public PttGrp(){
		grpName = new String();
		grpID = new String();
		grpIsAnswsered = new HashMap<String, Integer>();
		isCreateSession = false;
		oVoid = null;
	}
	
	static void SetGrpAnswerState(String grpID, int callID){
		grpIsAnswsered.put(grpID, Integer.valueOf(callID));
	}
	
	static boolean GetGrpAnswerState(String grpID, int callID){
		Object o = grpIsAnswsered.get(grpID);
		if (o == null)
			return false;
		int value = Integer.valueOf(o.toString());
		return (callID == value);
	}
	@Override
	public String toString() {
		synchronized (this) {
            StringBuilder builder = new StringBuilder("PttGrp: ");
            builder.append("state: ").append(state).
            append(" grpName: ").append(grpName).
            append(", grpID: ").append(grpID).
            append(", speakerName: ").append(speaker).
            append(", speakerNumber: ").append(speakerN);
            return builder.toString();
        }
	}

	public void setSpeaker(String speakerName, String speakerNum) {
		// TODO Auto-generated method stub
		speaker = speakerName;
		speakerN = speakerNum;
	}
}

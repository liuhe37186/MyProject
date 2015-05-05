package com.zed3.zhejiang;

public class CallInfo {

	public String name = "";
	public String number = "";
	public int type = -1;
	public int direction = -1;
	public int status = 2;
	public long time = 0;
	public long date = 0;
	@Override
	public String toString() {
		return "CallInfo [name=" + name + ", number=" + number + ", type="
				+ type + ", direction=" + direction + ", status=" + status
				+ ", time=" + time + ", date=" + date + "]";
	}
			
			
}

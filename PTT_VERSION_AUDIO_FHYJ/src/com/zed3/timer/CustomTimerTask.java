package com.zed3.timer;

import com.zed3.net.RtpPacket;

public interface CustomTimerTask extends Runnable{
	public abstract void run();
}

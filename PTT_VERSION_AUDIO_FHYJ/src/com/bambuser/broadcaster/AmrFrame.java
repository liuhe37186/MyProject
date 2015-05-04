package com.bambuser.broadcaster;

public class AmrFrame {

	private byte[] frameData;
	private boolean flag = false;

	public AmrFrame(byte[] frameData, boolean flag) {
		// TODO Auto-generated constructor stub
		this.frameData = frameData;
		this.flag = flag;
	}

	public byte[] getFrameData() {
		return frameData;
	}

	public boolean getFlag() {
		return flag;
	}

}

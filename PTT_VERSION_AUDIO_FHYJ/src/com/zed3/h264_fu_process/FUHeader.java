package com.zed3.h264_fu_process;
/**
 * FU header
 * @author oumogang
 */
public class FUHeader {
	/**byte的后五位 表示 TYPE*/
	private byte TYPE;
	/**byte的第3位 表示 R*/
	private byte R;
	/**byte的第2位 表示 E*/
	private byte E;
	/**byte的第1位 表示 S*/
	private byte S;
	private byte byteValue;
	
	FUHeader(){
		byteValue = 0;
	}
	FUHeader(byte byteValue){
		this.byteValue = byteValue;
	}

	public byte getTYPE() {
		TYPE = byteValue;
		TYPE = FUUtils.getType(TYPE);
		return TYPE;
	}

	public void setTYPE(byte tYPE) {
		byteValue = FUUtils.setType(byteValue, tYPE);
	}

	public byte getR() {
		R = byteValue;
		R = FUUtils.getR(R);
		return R;
	}

	public void setR(byte r) {
		byteValue = FUUtils.setR(byteValue, r);
	}

	public byte getE() {
		E = byteValue;
		E = FUUtils.getE(E);
		return E;
	}

	public void setE(byte e) {
		byteValue = FUUtils.setE(byteValue, e);
	}

	public byte getS() {
		S = byteValue;
		S = FUUtils.getS(S);
		return S;
	}

	public void setS(byte s) {
		byteValue = FUUtils.setS(byteValue, s);
	}
	
	public byte getByte() {
	       return byteValue;
	}
}

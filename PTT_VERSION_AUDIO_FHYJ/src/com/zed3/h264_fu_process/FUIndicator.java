package com.zed3.h264_fu_process;
/**
 * FU indicator
 * @author oumogang
 */
public class FUIndicator {
	/**byte的后五位 表示 TYPE*/
	private byte TYPE;
	/**byte的第2、3位 表示 NRI*/
	private byte NRI;
	/**byte的第1位 表示 TYPE*/
	private byte F;
	
	private byte b;
	
	FUIndicator(){
		b = 0;
	}
	FUIndicator(byte b){
		this.b = b;
	}
	public byte getTYPE() {
		byte type = b;
		return FUUtils.getType(type);
	}
	public void setTYPE(byte tYPE) {
		b = FUUtils.setType(b,tYPE);
	}
	public byte getNRI() {
		byte nri = b;
		return FUUtils.getNri(nri);
	}
	public void setNRI(byte nRI) {
		b = FUUtils.setNri(b,nRI);
	}
	public byte getF() {
		byte f = b;
		return F = FUUtils.getF(f);
	}
	public void setF(byte f) {
		F = f;
		b = FUUtils.setF(b,f);
	}
	
	public byte getByte() {
       return b;
	}
}

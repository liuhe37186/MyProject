package com.zed3.h264_fu_process;
/**
 * FU indicator
 * @author oumogang
 */
public class FUIndicator {
	/**byte�ĺ���λ ��ʾ TYPE*/
	private byte TYPE;
	/**byte�ĵ�2��3λ ��ʾ NRI*/
	private byte NRI;
	/**byte�ĵ�1λ ��ʾ TYPE*/
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

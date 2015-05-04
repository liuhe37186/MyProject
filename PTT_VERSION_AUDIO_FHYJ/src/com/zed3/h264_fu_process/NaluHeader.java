package com.zed3.h264_fu_process;
/**
 * H264 nal头
 * @author oumogang
 */
public class NaluHeader {
	/**byte的前五位 表示 TYPE*/
	private byte TYPE;
	/**byte的前6、7位 表示 NRI*/
	private byte NRI;
	/**byte的最后一位 表示 TYPE*/
	private byte F;
	
	private byte b;
	public NaluHeader(byte b){
		this.b = b;
	}
	public byte getTYPE() {
		byte type = b;
		type = FUUtils.getType(type);
		return type;
	}
	public void setTYPE(byte tYPE) {
		b = FUUtils.setType(b,tYPE);
	}
	public byte getNRI() {
		byte nri = b;
		nri = FUUtils.getNri(nri);
		return nri;
	}
	public void setNRI(byte nRI) {
		b = FUUtils.setNri(b,nRI);
	}
	public byte getF() {
		byte f = b;
		byte f2 = FUUtils.getF(f);
		return /*F = *//*FUUtils.getF(f)*/f2;
	}
	public void setF(byte f) {
		F = f;
		b = FUUtils.setF(b,f);
	}
	
	public byte getByte() {
       return b;
	}
}

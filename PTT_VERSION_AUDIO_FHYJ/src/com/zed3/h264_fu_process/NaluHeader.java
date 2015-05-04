package com.zed3.h264_fu_process;
/**
 * H264 nalͷ
 * @author oumogang
 */
public class NaluHeader {
	/**byte��ǰ��λ ��ʾ TYPE*/
	private byte TYPE;
	/**byte��ǰ6��7λ ��ʾ NRI*/
	private byte NRI;
	/**byte�����һλ ��ʾ TYPE*/
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

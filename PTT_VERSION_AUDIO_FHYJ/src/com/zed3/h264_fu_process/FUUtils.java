package com.zed3.h264_fu_process;

public class FUUtils {
	/** ------------------------ */
	private static String head = "00000000";
	/** ��ȡ��5λ����ǰ��λ��0�� */
	public static byte getType(byte type) {
		return (byte) (type & Byte.parseByte("00011111", 2));
	}

	/**
	 * ��ֵ��5λ����b�ĺ���λ��0������tYPE��
	 * 
	 * @param b
	 * @param tYPE
	 */
	public static /*void*/byte setType(byte b, byte tYPE) {
		b = (byte) (b & (Short.parseShort(head+"11100000", 2)));
		b = (byte) (b+tYPE);
		return b;
	}

	/**
	 * ��ȡ��2��3λ��������2��3λ������λ��0��Ȼ������5λ��
	 * 
	 * @param nri
	 * @return 
	 */
	public static byte getNri(byte nri) {
		nri = (byte) (nri & Byte.parseByte("01100000", 2));
		nri = (byte) (nri>>5);
		return nri;
	}

	/**
	 * ��ֵ��2��3λ
	 * 
	 * ��b�ĵ�6��7λ��0����nRI����5λ������ӣ�
	 * 
	 * @param b
	 * @param nRI
	 * @return 
	 */
	public static byte setNri(byte b, byte nRI) {
		//b = (byte) (b & (Byte.parseByte("10011111", 2)));
		b = (byte) (b & (byte)(Short.parseShort(head+"10011111", 2)));
		b = (byte) (b + (nRI<<5));
		return b;
	}

	/**
	 * ��ȡ��1λ ��
	 * 
	 * ��b�ĺ�7λ��0��Ȼ������7λ��
	 * 
	 * @param f
	 * @return
	 */
	public static byte getF(byte f) {
		/*return (byte) ((f & Short.parseShort(head+"10000000", 2))>>7);*/
		byte result = (byte) (f & Short.parseShort(head+"10000000", 2));
		result = (byte) (result>>7);
		return result;
	}

	/**
	 * ��ֵ��1λ��
	 * 
	 * ��b�İѵ�һλ��0����f����7λ��Ȼ��������ӣ�
	 * 
	 * @param b
	 * @param f
	 * @return 
	 */
	public static byte setF(byte b, byte f) {
		b = (byte) (b & (Byte.parseByte("01111111", 2)));
		b = (byte) (b + (f<<7));
		return b;
	}

	/** ------------------------ */
	/**
	 * R:��3λ 
	 * 
	 * ������3λ������λ��0��Ȼ������5λ��
	 * 
	 * @param r
	 * @return
	 */
	public static byte getR(byte r) {
		r = (byte) (r&Byte.parseByte("00100000",2));
		r = (byte) (r >>5);
		return r/*(byte) ((r&Byte.parseByte("00100000",2))>>5)*/;
	}

	/**
	 * ��ֵ��3λ 
	 * 
	 * b�ĵ�3λ��0,����λ������r����5λ������ӣ�
	 * 
	 * @param b
	 * @param r
	 * @return 
	 */
	public static byte setR(byte b, byte r) {
		b = (byte) (b & (Short.parseShort(head+"11011111", 2)));
		r = (byte) (r << 5);
		b = (byte) (b + r);
		return b;
	}

	/**
	 * E:��2λ 
	 * �ڶ�λ����������λ��0��Ȼ������6λ��
	 * @param e
	 * @return
	 */
	public static byte getE(byte e) {
		return (byte) ((e & Byte.parseByte("01000000", 2)) >> 6);
	}

	/**
	 * ��ֵ��2λ��
	 * 
	 * b�ĵ�2λ��0������������e����6λ������ӣ�
	 * 
	 * @param b
	 * @param e
	 * @return 
	 */
	public static byte setE(byte b, byte e) {
		b = (byte) (b & (Short.parseShort(head+"10111111", 2)));
		e = (byte) (e << 6);
		b = (byte) (b + e);
		return b;
	}

	/**
	 * S:��1λ 
	 * 
	 * ��1λ����������λ��0��Ȼ������7λ��
	 * @param S
	 * @return
	 */
	public static byte getS(byte s) {
		//return (byte) ((s & Byte.parseByte("10000000", 2)) >> 7);
		return /*getF(s)*/(byte) ((s & Byte.parseByte("10000000", 2)) >> 7);
	}

	/**
	 * ��ֵ��1λ:
	 * 
	 * b�ĵ�1λ��0������λ������s����7λ������ӣ�
	 * @param b
	 * @param s
	 * @return 
	 */
	public static byte setS(byte b, byte s) {
		b = (byte) (b & (Byte.parseByte("01111111", 2)));
		s = (byte) (s<<7);
		b = (byte) (b + s);
		return /*setF(b, s)*/b;
	}

}

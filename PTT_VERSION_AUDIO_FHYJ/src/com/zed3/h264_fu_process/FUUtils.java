package com.zed3.h264_fu_process;

public class FUUtils {
	/** ------------------------ */
	private static String head = "00000000";
	/** 获取后5位：把前三位变0； */
	public static byte getType(byte type) {
		return (byte) (type & Byte.parseByte("00011111", 2));
	}

	/**
	 * 赋值后5位：把b的后五位变0，加上tYPE；
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
	 * 获取第2、3位，保留第2、3位，其他位变0，然后右移5位；
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
	 * 赋值第2、3位
	 * 
	 * 把b的第6、7位变0，把nRI左移5位，两相加；
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
	 * 获取第1位 ：
	 * 
	 * 把b的后7位变0，然后右移7位；
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
	 * 赋值第1位：
	 * 
	 * 把b的把第一位变0，把f左移7位，然后两个相加；
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
	 * R:第3位 
	 * 
	 * 保留第3位，其他位变0，然后右移5位；
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
	 * 赋值第3位 
	 * 
	 * b的第3位变0,其他位保留，r左移5位，两相加；
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
	 * E:第2位 
	 * 第二位保留，其他位变0，然后右移6位；
	 * @param e
	 * @return
	 */
	public static byte getE(byte e) {
		return (byte) ((e & Byte.parseByte("01000000", 2)) >> 6);
	}

	/**
	 * 赋值第2位：
	 * 
	 * b的第2位变0，其他保留，e左移6位，两相加；
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
	 * S:第1位 
	 * 
	 * 第1位保留，其他位变0，然后右移7位；
	 * @param S
	 * @return
	 */
	public static byte getS(byte s) {
		//return (byte) ((s & Byte.parseByte("10000000", 2)) >> 7);
		return /*getF(s)*/(byte) ((s & Byte.parseByte("10000000", 2)) >> 7);
	}

	/**
	 * 赋值第1位:
	 * 
	 * b的第1位变0，其他位保留，s左移7位，两相加；
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

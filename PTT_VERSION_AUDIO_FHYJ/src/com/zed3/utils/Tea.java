package com.zed3.utils;

public class Tea { 
	/**
	* value is 0xffffffff
	* filter long to unsigned int
	*/
	public static Long UIFILTER = Long.decode("0xffffffff");
	/**
	* value is 0xff
	* filter short to unsigned byte
	*/
	public static Short UBFILTER = Short.decode("0xff");
	/**
	* Encipher method
	*
	64bit plaintext, 128bit key
	* @param v Plaintext
	* @param k Key
	* @return
	*/
	public static Short[] encipher(Short [] v, Short [] k){
		Long[] vL = shortToLong(v);
		Long[] kL = shortToLong(k);
		Long[] wL = new Long[vL.length];
		Short[] w = null;
		long y = vL[0];
		long z = vL[1];
		long a = kL[0];
		long b = kL[1];
		long c = kL[2];
		long d = kL[3];
		long n = 0x10; /* do encrypt 16 (0x10) times */
		long sum = 0;
		long delta = Long.decode("0x9E3779B9"); /* 0x9E3779B9 - 0x100000000 = -0x61C88647 */
		while (n-- > 0) {
		sum += delta;
		sum &= UIFILTER;
		y += ((z << 4) + a) ^ (z + sum) ^ ((z >> 5) + b);
		y &= UIFILTER;
		z += ((y << 4) + c) ^ (y + sum) ^ ((y >> 5) + d);
		z &= UIFILTER;
		}
		wL[0] = y;
		wL[1] = z;
		w = longToShort(wL);
		return w;
	}
	/**
	* Decipher method
	*
	64bit ciphertext, 128bit key
	* @param v Ciphertext
	* @param k Key
	* @return
	*/
	public static Short[] decipher(Short [] v, Short [] k){
		Long[] vL = shortToLong(v);
		Long[] kL = shortToLong(k);
		Long[] wL = new Long[vL.length];
		Short[] w = null;
		long y = vL[0];
		long z = vL[1];
		long a = kL[0];
		long b = kL[1];
		long c = kL[2];
		long d = kL[3];
		long n = 0x10;
		/* sum = delta << 4, in general sum = delta * n */ 
		long sum = Long.decode("0xE3779B90"); 
		long delta = Long.decode("0x9E3779B9"); // use filter to get unsigned int 
		// use ~x +1 instead -x 
		while (n-- > 0) {
			z += (~(((y << 4) + c) ^ (y + sum) ^ ((y >> 5) + d)) + 1);
			z &= UIFILTER;
			y += (~(((z << 4) + a) ^ (z + sum) ^ ((z >> 5) + b)) + 1);
			y &= UIFILTER;
			sum += ((~delta) + 1);
			sum &= UIFILTER;
		}
		wL[0] = y;
		wL[1] = z;
		w = longToShort(wL);
		return w;
	}
	/**
	* convert bytes to longs, put every 4 bytes to 1 long
	*
	use long instead unsigned int
	* use short instead unsigned byte
	* @param source - the Short [] that save unsigned bytes to convert
	* @return the Long [] save unsigned int
	*/
	public static Long [] shortToLong(Short [] source){
		int sourlen = source.length;
		int turn = sourlen / 4; // how many turns
		int remainder = sourlen % 4; // how many bytes in first turn
		int tarlen = turn + (remainder == 0 ? 0 : 1);
		Long [] target = new Long[tarlen];
		for(int i = 0; i < target.length; i++){ 
			target[i] = Long.parseLong("0"); 
		} 
		int iter = 0; 
		int turnIter = 0; 
		for(turnIter = 0; turnIter < tarlen; turnIter++){ 
			for(iter = 0; iter < 4; iter++){ 
				target[turnIter] <<= 8; 
				if((turnIter != turn - 1) || ((turnIter == turn - 1) && (iter < remainder || remainder == 0))) target[turnIter] += source[turnIter * 4 + iter];
				} 
			} 
		return target; 
	} 
	/** * convert longs to bytes
	*
	cut 1 long(32bit valid to instead unsigned int) to 4 short(8bit valid to instead unsigned byte)
	* use long instead unsigned int
	* use short instead unsigned byte
	* @param source - the Long [] that save unsigned int to convert
	* @return the Short [] that save unsigned bytes
	*/
	public static Short [] longToShort(Long [] source){
		int sourlen = source.length;
		Short [] target = new Short[sourlen * 4];
		int turn = target.length % 4;
		int iter = 0;
		int move = 0;
		for(iter = 0; iter < target.length; iter++){ 
			move = 8 * (3 - (iter % 4)); 
			target[iter] = Short.parseShort(Long.toString((source[iter / 4] & (UBFILTER << move)) >> move));
		}
		return target;
	}
	
	
	
	
	/**
	 * ###########################################################################################################
	 */
	
	//加密
	/**
	 * encrypt
	 * @param content  data to encrypt
	 * @param offset   offset of data
	 * @param key      key of encrypt
	 * @param times    times of encrypt
	 * @return  ciphertext
	 */
	public byte[] encrypt(byte[] content, int offset, int[] key, int times){//times为加密轮数
		times = 32;
		int[] tempInt = byteToInt(content, offset);
		int delta=0x9e3779b9; //这是算法标准给的值
		int y = tempInt[0], z = tempInt[1], sum = 0, i;
		int a = key[0], b = key[1], c = key[2], d = key[3];
	
		for (i = 0; i < times; i++) {
			sum += delta;
			y += ((z<<4) + a) ^ (z + sum) ^ ((z>>5) + b);
			z += ((y<<4) + c) ^ (y + sum) ^ ((y>>5) + d);
		}
		tempInt[0]=y;
		tempInt[1]=z;
		return intToByte(tempInt, 0);
	}
	
	//解密
	/**
	 * decrypt
	 * @param encryptContent   data to decrypt
	 * @param offset           offset of data
	 * @param key			   key of encrypt
	 * @param times            times of decrypt
	 * @return  data
	 */
	public byte[] decrypt(byte[] encryptContent, int offset, int[] key, int times){
		times = 32;
		int[] tempInt = byteToInt(encryptContent, offset);
		int delta=0x9e3779b9; //这是算法标准给的值
		int y = tempInt[0], z = tempInt[1], sum = 0xC6EF3720, i;//32次对应0xC6EF3720
		int a = key[0], b = key[1], c = key[2], d = key[3];
	
		for(i = 0; i <times; i++) {
			z -= ((y<<4) + c) ^ (y + sum) ^ ((y>>5) + d);
			y -= ((z<<4) + a) ^ (z + sum) ^ ((z>>5) + b);
			sum -= delta;
		}
		tempInt[0] = y;
		tempInt[1] = z;
	
		return intToByte(tempInt, 0);
	}
	
	//byte[]型数据转成int[]型数据
	/**
	 * 
	 * @param content
	 * @param offset
	 * @return
	 */
	public int[] byteToInt(byte[] content, int offset){

		int[] result = new int[content.length >> 2]; //除以2的n次方 == 右移n位 即 content.length / 4 == content.length >> 2
		for(int i = 0, j = offset; j < content.length; i++, j += 4){
			result[i] = transform(content[j + 3]) | transform(content[j + 2]) << 8 |
			transform(content[j + 1]) << 16 | (int)content[j] << 24;
		}
		return result;
	
	}
	
	//int[]型数据转成byte[]型数据
	public byte[] intToByte(int[] content, int offset){
		byte[] result = new byte[content.length << 2]; //乘以2的n次方 == 左移n位 即 content.length * 4 == content.length << 2
		for(int i = 0, j = offset; j < result.length; i++, j += 4){
			result[j + 3] = (byte)(content[i] & 0xff);
			result[j + 2] = (byte)((content[i] >> 8) & 0xff);
			result[j + 1] = (byte)((content[i] >> 16) & 0xff);
			result[j] = (byte)((content[i] >> 24) & 0xff);
		}
		return result;
	}
	
	//若某字节被解释成负的则需将其转成无符号正数
	public static int transform(byte temp){
		int tempInt = (int)temp;
		if(tempInt < 0){
			tempInt += 256;
		}
		return tempInt;
	}

//	测试代码如下:
//	public static void main(String[] args){
//
//	int[] KEY = new int[]{//加密解密所用的KEY
//	0x789f5645, 0xf68bd5a4,
//	0x81963ffa, 0x458fac58
//	};
//	Tea tea = new Tea();
//
//	byte[] info = new byte[]{
//
//	1,2,3,4,5,6,7,8
//	};
//	System.out.print("原数据：");
//	for(byte i : info)
//	System.out.print(i + " ");
//	System.out.println();
//
//	byte[] secretInfo = tea.encrypt(info, 0, KEY, 32);
//	System.out.print("加密后的数据：");
//	for(byte i : secretInfo)
//	System.out.print(i + " ");
//	System.out.println();
//
//	byte[] decryptInfo = tea.decrypt(secretInfo, 0, KEY, 32);
//	System.out.print("解密后的数据：");
//	for(byte i : decryptInfo)
//	System.out.print(i + " ");
//
//	}
//	输出结果如下:
//
//	原数据：1 2 3 4 5 6 7 8
//	加密后的数据：92 124 -3 -125 -115 82 21 28
//	解密后的数据：1 2 3 4 5 6 7 8
//
//	这只是一次加密解密操作,如果你想一次处理大于8个字节的数据,需要再封装一下.
//
//	封装后的代码如下:
//	//通过TEA算法加密信息
//	private byte[] encryptByTea(String info){
//	byte[] temp = info.getBytes();
//	int n = 8 - temp.length % 8; //若temp的位数不足8的倍数,需要填充的位数
//	byte[] encryptStr = new byte[temp.length + n];
//	encryptStr[0] = (byte)n;
//	System.arraycopy(temp, 0, encryptStr, n, temp.length);
//	byte[] result = new byte[encryptStr.length];
//	for(int offset = 0; offset < result.length; offset += 8){
//	byte[] tempEncrpt = tea.encrypt(encryptStr, offset, KEY, 32);
//	System.arraycopy(tempEncrpt, 0, result, offset, 8);
//	}
//	return result;
//	}
//	//通过TEA算法解密信息
//	private String decryptByTea(byte[] secretInfo){
//	byte[] decryptStr = null;
//	byte[] tempDecrypt = new byte[secretInfo.length];
//	for(int offset = 0; offset < secretInfo.length; offset += 8){
//	decryptStr = tea.decrypt(secretInfo, offset, KEY, 32);
//	System.arraycopy(decryptStr, 0, tempDecrypt, offset, 8);
//	}
//
//	int n = tempDecrypt[0];
//	return new String(tempDecrypt, n, decryptStr.length - n);
//
//	}

}
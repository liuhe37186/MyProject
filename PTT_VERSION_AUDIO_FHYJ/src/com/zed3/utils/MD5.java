package com.zed3.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

	public static String toMd5(String md5Str) {
		String result = "";
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(md5Str.getBytes("utf-8"));
			result = toHexString(algorithm.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();

		for (int b : bytes) {
			if (b < 0)
				b += 256;
			if (b < 16)
				hexString.append("0");
			hexString.append(Integer.toHexString(b));
		}
		//32Î» hexString.toString()
		return hexString.toString();//16Î»µÄ.substring(8, 24)
	}
	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			return toHexString(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return "";
	}
}

package org.opencore.H264Decode;

public class NativeH264Decoder {
	static {
		String str = "H264Decoder";
		try {
			System.loadLibrary(str);
		} catch (Exception localException) {
		}
	}

	/** @deprecated DecodeAndConvert */
	public static synchronized native byte[] DecodeAndConvert(
			byte[] paramArrayOfByte, int[] paramArrayOfInt);

	public static native int DeinitDecoder();

	public static native int DeinitParser();

	public static native int InitDecoder(int paramInt1, int paramInt2);

	public static native int InitParser(String paramString);

	public static native String getVideoCoding();

	public static native int getVideoHeight();

	public static native int getVideoLength();

	public static native VideoSample getVideoSample(int[] paramArrayOfInt);

	public static native int getVideoWidth();
}

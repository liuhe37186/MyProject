package sinofloat.wvp.core;

public class NativeH264Encoder {
	static {
		String str = "Up";
		try {
			System.loadLibrary(str);

		} catch (UnsatisfiedLinkError localUnsatisfiedLinkError) {
		}
	}

	public static native int DeinitEncoder();

	public static native byte[] EncodeFrame(byte[] paramArrayOfByte,
			long paramLong);

	public static native int InitEncoder(int paramInt1, int paramInt2,
			int paramInt3, int paramInt4);

	public static native int getKeyFrame();

	public static native int getLastEncodeStatus();

}

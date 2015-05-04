package com.bambuser.broadcaster;


public final class AmrEncoder
{
  private static final String LOGTAG = "AmrEncoder";
  public static final int MR102 = 6;
  public static final int MR122 = 7;
  public static final int MR475 = 0;
  public static final int MR515 = 1;
  public static final int MR59 = 2;
  public static final int MR67 = 3;
  public static final int MR74 = 4;
  public static final int MR795 = 5;
  private static byte[] audioData = new byte[320];
  private long privData = 0L;
  public static AmrEncoder amrEncoder;
private String tag = "AmrEncoder";

  static
  {
//    try
//    {
//      Class.forName("com.bambuser.broadcaster.NativeUtils");//init NativeUtils
//    }
//    catch (ClassNotFoundException localClassNotFoundException)
//    {
//    	localClassNotFoundException.printStackTrace();
//    }
		System.loadLibrary("bambuser");
		System.loadLibrary("AmrCodec");
		amrEncoder = new AmrEncoder();
  }

  private AmrEncoder()
  {
    init();
  }

  public final synchronized native void close();

  public final native int encode(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3);

  public void finalize()
  {
    close();
  }

  public final synchronized native void init();

public static AmrEncoder getEncoder() {
	// TODO Auto-generated method stub
	return amrEncoder;
}

}
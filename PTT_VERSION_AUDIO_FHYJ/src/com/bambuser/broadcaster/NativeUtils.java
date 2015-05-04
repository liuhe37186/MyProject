package com.bambuser.broadcaster;

import java.nio.ByteBuffer;

public final class NativeUtils
{
  static
  {
    System.loadLibrary("bambuser");
  }

  public static native void convert422SPTo420SP(ByteBuffer paramByteBuffer1, int paramInt1, int paramInt2, ByteBuffer paramByteBuffer2);
}
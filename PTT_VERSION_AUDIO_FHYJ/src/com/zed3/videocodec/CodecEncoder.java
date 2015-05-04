package com.zed3.videocodec;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

public class CodecEncoder implements Runnable {
	MediaCodec mMediaCodec;
	IFrameListener mListener;
	byte[] buffer;

	public void setInputBuffer(byte[] buffer, IFrameListener listener) {
		this.buffer = buffer;
		this.mListener = listener;
		initMediaCodec();
		this.run();
	}
	@Override
	public void run() {

		if (mMediaCodec == null) {
			return;
		}
		try {
			ByteBuffer[] iBufs = mMediaCodec.getInputBuffers();
			ByteBuffer[] oBufs = mMediaCodec.getOutputBuffers();

			int iIdx = mMediaCodec.dequeueInputBuffer(-1);
			if (iIdx >= 0) {
				ByteBuffer iBuf = iBufs[iIdx];
				iBuf.clear();
				iBuf.put(buffer);
				mMediaCodec.queueInputBuffer(iIdx, 0, buffer.length, 0, 0);
			}

			MediaCodec.BufferInfo bufInfo = new MediaCodec.BufferInfo();
			int oIdx = mMediaCodec.dequeueOutputBuffer(bufInfo,0);
			while (oIdx >= 0) {
				ByteBuffer oBuf = oBufs[oIdx];
				byte[] out = new byte[bufInfo.size];
				oBuf.get(out);
				if(mListener != null){
					mListener.onFrame(out);
				}
				mMediaCodec.releaseOutputBuffer(oIdx, false);
				oIdx = mMediaCodec.dequeueOutputBuffer(bufInfo, 0);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	
	}
	private static final int ColorFormat_NV12 = 1;
	private static final int ColorFormat_NV21 = 2;
	private static final int ColorFormat_I420 = 3;
	private static final int[][] ColorFormatList = {
		{ColorFormat_NV12, MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar},
		{ColorFormat_NV21, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar},
		{ColorFormat_I420, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar},
	};
	private int                  mColorFormat = 0;
	void initMediaCodec() {
		
		mMediaCodec = MediaCodec.createEncoderByType("video/avc");
		MediaFormat mediaFormat = MediaFormat.createVideoFormat(
				"video/avc", 320, 240);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,
				4194304);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,
				10);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,
				5);
		int col_fmt = -1;
		for (int[] colorFormat : ColorFormatList) {
			try {
				mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat[1]);
				mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
				col_fmt = colorFormat[0];
			} catch (Exception e) {
				continue;
			}
		}
		if (col_fmt < 0) {
			mMediaCodec.release();
			throw new UnsupportedOperationException("Not found color format");
		}
		
		mColorFormat = col_fmt;
		mMediaCodec.start();
	}

	private void releaseEncoder() {
		if (mMediaCodec != null) {
			mMediaCodec.stop();
			mMediaCodec.release();
			mMediaCodec = null;
		}
	}
}

package com.zed3.h264_fu_process;

import org.zoolu.tools.MyLog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import com.video.utils.IVideoSizeChange;
import com.zed3.sipua.DialogListener;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.VideoUdpThread;
import com.zed3.toast.MyToast;

public class RtpStack {
	public static final String TAG = "RtpStack";
	private VideoUdpThread vHandle = null;//
	private Context context = null;
	private LinearLayout loadProgressx = null;

	// init
	public RtpStack(SurfaceView videoView, LinearLayout loadProgress,
			final Context context,IVideoSizeChange listener) {
		this.context = context;

		loadProgressx = loadProgress;
		vHandle = new VideoUdpThread(videoView,listener);

		vHandle.setDialogListener(new DialogListener() {
			@Override
			public void DialogCancel(int flag) {
				if (flag == 1) {
					// 关闭
					myHandler.sendEmptyMessage(2);
				}

				if (flag == 0) {
					myHandler.sendEmptyMessage(1);
				}

			}
		});
	}
	public void resetDecode(){
		vHandle.resetDecode();
	}
	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				// exitUnSupportDialog(context, "提示", "解码器暂不支持该视频分辨率");
				MyToast.showToastInBg(true, context, R.string.viewerror);
			} else if (msg.what == 2) {
				if (loadProgressx != null)
					loadProgressx.setVisibility(View.GONE);
			}
		}
	};

	//
	public void CloseUdpSocket() {
		// 关闭
		// if (mDialog.isShowing())
		// mDialog.dismiss();

		if (!UserAgent.Camera_VideoPort.equals("0")) {
			vHandle.CloseUdpSocket();
		}
	}

	// 视频转发时不用编码，只发空包
	public void SendEmptyPacket() {
		byte[] t = "111111111111".getBytes();
		vHandle.sendNewByte(t, t.length);
	}

	// 0204
	int FRANGMENT_BUG_SIZE = 1400;
	byte[] fu = new byte[FRANGMENT_BUG_SIZE + 2];

	/**
	 * 分片：
	 * 
	 * @param inbuffer
	 *            字节流
	 * @param inbufferLen
	 *            字节流长度
	 * @param timeStamp
	 *            时间戳 del
	 * @param payloadType
	 *            负载类型 del
	 */
	public void transmitH264FU(byte[] inbuffer, int inbufferLen) {

		// long a = System.currentTimeMillis();
		// Tools.writeFileToSD("##########transmitH264FU() inbuffer.length ="+inbufferLen);

		if (inbufferLen <= FRANGMENT_BUG_SIZE) {
			vHandle.VideoPacketToH264(inbuffer, inbufferLen, 1);
			return;
		}
		;
		int marker = 0;
		NaluHeader pNaluHdr = new NaluHeader(inbuffer[0]);

		FUIndicator pFuIndicator = new FUIndicator(/* fu[0] */);
		pFuIndicator.setTYPE((byte) 28);
		pFuIndicator.setNRI(pNaluHdr.getNRI());
		pFuIndicator.setF(pNaluHdr.getF());

		FUHeader pFUHdrStart = new FUHeader(/* fu[1] */);
		pFUHdrStart.setTYPE(pNaluHdr.getTYPE());
		pFUHdrStart.setE((byte) 0);
		pFUHdrStart.setR((byte) 0);
		pFUHdrStart.setS((byte) 1);

		FUHeader pFUHdrMid = new FUHeader(/* fu[1] */);
		pFUHdrMid.setTYPE(pNaluHdr.getTYPE());
		pFUHdrMid.setE((byte) 0);
		pFUHdrMid.setR((byte) 0);
		pFUHdrMid.setS((byte) 0);

		FUHeader pFUHdrEnd = new FUHeader(/* fu[1] */);
		pFUHdrEnd.setTYPE(pNaluHdr.getTYPE());
		pFUHdrEnd.setE((byte) 1);
		pFUHdrEnd.setR((byte) 0);
		pFUHdrEnd.setS((byte) 0);

		int k = 0;
		int len = 0;
		k = inbufferLen / FRANGMENT_BUG_SIZE;
		len = inbufferLen % FRANGMENT_BUG_SIZE;
		int t = 0;
		while (t <= k) {
			if (t == 0) {
				System.arraycopy(inbuffer, 0, fu, 1, FRANGMENT_BUG_SIZE);

				fu[0] = pFuIndicator.getByte();
				fu[1] = pFUHdrStart.getByte();

				vHandle.VideoPacketToH264(fu, FRANGMENT_BUG_SIZE + 1, 0);
			}
			// 最后一片
			else if (k == t && len > 0) {
				System.arraycopy(inbuffer, t * FRANGMENT_BUG_SIZE, fu, 2, len);
				fu[0] = pFuIndicator.getByte();
				fu[1] = pFUHdrEnd.getByte();
				vHandle.VideoPacketToH264(fu, len + 2, 1);
			}
			// 中间片
			else if (t < k && 0 != t) {
				System.arraycopy(inbuffer, t * FRANGMENT_BUG_SIZE, fu, 2,
						FRANGMENT_BUG_SIZE);

				fu[0] = pFuIndicator.getByte();
				if ((t == (k - 1)) && (len == 0)) {
					fu[1] = pFUHdrEnd.getByte();
					marker = 1;
				} else {
					fu[1] = pFUHdrMid.getByte();
					marker = 0;
				}
				vHandle.VideoPacketToH264(fu, FRANGMENT_BUG_SIZE + 2, marker);
			}
			t++;
		}
		// Tools.writeFileToSD("##########transmitH264FU()----- end! useTime = "+(long)(System.currentTimeMillis()-a));

	}
}

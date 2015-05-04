package com.zed3.codecs;

import com.zed3.codecs.EncodeRate.Mode;

public class ModeChange {
	/**
	 * 发送端根据接收的对方RTP包中提取CMR值，根据CMR值对自己当前码率作出调整
	 * 
	 * @param cmr
	 *            从接收到的RTP包中提取CMR 值
	 * @return a 新的编码速率
	 */
	public static Mode getmode(byte cmr) {
		Mode a = null;
		switch (cmr) {
		case 0:
			a = Mode.MR475;
			break;
		case 1:
			a = Mode.MR515;
			break;
		case 2:
			a = Mode.MR59;
			break;
		case 3:
			a = Mode.MR67;
			break;
		case 4:
			a = Mode.MR74;
			break;
		case 5:
			a = Mode.MR795;
			break;
		case 6:
			a = Mode.MR102;
			break;
		case 7:
			a = Mode.MR122;
			break;
		case 15:
			a = Mode.MR74;
		}
		return a;
	}

	/**
	 * 根据接收到的对方的RTP包的时延和丢包情况，确定一个CMR值要求对方进行码率调整
	 * 
	 * @param lost
	 *            丢包率%
	 * @param delay
	 *            时延(ms)
	 * @return cmr 值0～7
	 */

	public static byte judge(float lost, float delay, byte last_cmr) {

		byte cmr = 0;
		if (lost >= 0.5) {

			if (last_cmr == 15)
				cmr = 0;
			else if (last_cmr > 0 && last_cmr < 8) {
				cmr = (byte) (last_cmr - 1);
			} else
				cmr = 0;
		} else {

			if (delay >= 289.0) {
				if (last_cmr == 15) {
					cmr = 0;
				} else if (last_cmr > 0 && last_cmr < 8) {
					cmr = (byte) (last_cmr - 1);
				} else
					cmr = 0;

			} else {

				if (last_cmr == 15) {
					cmr = 7;
				} else if (last_cmr >= 0 && last_cmr < 7) {
					cmr = (byte) (last_cmr + 1);
				} else
					cmr = 7;
			}
		}
		return cmr;
	}
	
}

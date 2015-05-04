package com.zed3.codecs;

import com.zed3.codecs.EncodeRate.Mode;

public class ModeChange {
	/**
	 * ���Ͷ˸��ݽ��յĶԷ�RTP������ȡCMRֵ������CMRֵ���Լ���ǰ������������
	 * 
	 * @param cmr
	 *            �ӽ��յ���RTP������ȡCMR ֵ
	 * @return a �µı�������
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
	 * ���ݽ��յ��ĶԷ���RTP����ʱ�ӺͶ��������ȷ��һ��CMRֵҪ��Է��������ʵ���
	 * 
	 * @param lost
	 *            ������%
	 * @param delay
	 *            ʱ��(ms)
	 * @return cmr ֵ0��7
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

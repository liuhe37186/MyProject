package com.zed3.video;

import java.util.Comparator;

public class ReceivePacketCompare implements Comparator<ReceivePacketInfo> {

	@Override
	public int compare(ReceivePacketInfo r1, ReceivePacketInfo r2) {

		if (r1.getSeqNum() > r2.getSeqNum()) {
			if ((r1.getSeqNum() - r2.getSeqNum()) < 0x7fff)
				return 1;
			else
				return -1;
		} else if (r1.getSeqNum() < r2.getSeqNum()) {
			if ((r2.getSeqNum() - r1.getSeqNum()) < 0x7fff)
				return -1;
			else
				return 1;
		} else
			return 0;

	}

}

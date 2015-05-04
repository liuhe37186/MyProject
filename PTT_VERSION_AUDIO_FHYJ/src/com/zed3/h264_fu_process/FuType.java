package com.zed3.h264_fu_process;

public enum FuType {
	FU_TYPE_START/* = 0*/{
		int value = 0;
	},
	FU_TYPE_MIDDLE{
		int value = 1;
	},
	FU_TYPE_END{
		int value = 2;
	},
	FU_TYPE_INVALID{
		int value = -1;
	} /*= -1*/;
}

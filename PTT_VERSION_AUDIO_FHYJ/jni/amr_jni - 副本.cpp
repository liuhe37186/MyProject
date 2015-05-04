/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 *
 * This file is part of Sipdroid (http://www.sipdroid.org)
 *
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <memory.h>
#include <ctype.h>
#include <jni.h>

#include "amr_jni/interf_enc.h"
#include "amr_jni/typedef.h"
#include "amr_jni/sp_enc.h"
#include "amr_jni/interf_dec.h"


#include "amr_jni\interf_dec.h"

#define LOG_TAG "amr_jni"

#ifdef BUILD_FROM_SOURCE
#include <utils/Log.h>
#else
#include <android/log.h>
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOG_TAG, __VA_ARGS__)

#endif



/* Define codec specific settings */
#define BLOCK_LEN       160

#undef DEBUG_AMR

// the header length of the RTP frame (must skip when en/decoding)
#define	RTP_HDR_SIZE	12

static int codec_open = 0;

static JavaVM *gJavaVM;
//const char *kInterfacePath = "org/sipdroid/pjlib/AMR";
const char *kInterfacePath = "com/zed3/pjlib/AMR";

int *AMR0610_enc_state;
int *AMR0610_dec_state;
int dtx;

extern "C"
JNIEXPORT jint JNICALL Java_com_zed3_codecs_AMR_open
  (JNIEnv *env, jobject obj, jint mode) {
//    int ret;
	dtx = (int)mode;

	if (codec_open++ != 0)
		return (jint)0;
	AMR0610_enc_state = Encoder_Interface_init(dtx);
	AMR0610_dec_state = Decoder_Interface_init();

	if ((AMR0610_enc_state = Encoder_Interface_init(dtx)) == NULL)
	{
		fprintf(stderr, "    Cannot create encoder\n");
		exit(2);
	}

	if ((AMR0610_dec_state = Decoder_Interface_init()) == NULL)
	{
		fprintf(stderr, "    Cannot create decoder\n");
		exit(2);
	}

	/*dtx = mode;
		enstate = Encoder_Interface_init(dtx);
		LOGI("initEncoder_mode: %d\n", dtx);
		LOGI("initEncoder_state: %d\n", enstate);
			return (jint)enstate;*/
	return (jint)0;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_zed3_codecs_AMR_encode
    (JNIEnv *env, jobject obj, jshortArray lin, jint offset, jbyteArray encoded, jint size) {

	jshort pre_amp[BLOCK_LEN];//160byte��������������
	jbyte AMR0610_data[BLOCK_LEN];//���������ݻ�����

	int ret,i,frsz=BLOCK_LEN;//frsz ���ڿ������������õ�

	unsigned int lin_pos = 0;//��ȡһ֡�������ݵ�ͷ�Ǳ�

	if (!codec_open)
		return 0;

#ifdef DEBUG_AMR
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
            "encoding frame size: %d\toffset: %d\n", size, offset);
#endif

    //��֡����
	for (i = 0; i < size; i+=BLOCK_LEN) {
#ifdef DEBUG_AMR
		__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
            "encoding frame size: %d\toffset: %d i: %d\n", size, offset, i);
#endif
		//��lin�ϵ����ݣ���offset + i��ʼ������frsz��Ԫ�ص�pre_amp�ϣ�
		env->GetShortArrayRegion(lin, offset + i,frsz, pre_amp);

//		ret=AMR0610_encode(AMR0610_enc_state, (uint8_t *) AMR0610_data, pre_amp, size);
		ret = Encoder_Interface_Encode(AMR0610_enc_state,/*mode*/dtx,
				/*speech*/pre_amp,
				/*serial_data*/AMR0610_data, 0);

#ifdef DEBUG_AMR
			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
				"Enocded Bytes: %d\n", ret);
#endif
        /* Write payload */
			//��AMR0610_data�ϵ����ݣ�������encoded�ϣ���RTP_HDR_SIZE+ lin_pos�Ǳ꿪ʼ������ret��Ԫ�أ�
		env->SetByteArrayRegion(encoded, RTP_HDR_SIZE+ lin_pos, ret, AMR0610_data);
		lin_pos += ret;
	}
#ifdef DEBUG_AMR
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
        "encoding **END** frame size: %d\toffset: %d i: %d lin_pos: %d\n", size, offset, i, lin_pos);
#endif

    return (jint)lin_pos;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_zed3_codecs_AMR_decode
    (JNIEnv *env, jobject obj, jbyteArray encoded, jshortArray lin, jint size) {

	jint frames = (env->GetArrayLength(lin))/BLOCK_LEN;//�ж���֡Ҫ���룻
	jint one_frame_encoded_len = size/frames;//����ǰ��һ֡�ĳ��ȣ�
	jshort post_amp[BLOCK_LEN];
	jbyte AMR0610_data[/*BLOCK_LEN*/one_frame_encoded_len];

	int len;

	if (!codec_open)
		return 0;


	for (int i = 0; i < size; i+=BLOCK_LEN) {
#ifdef DEBUG_AMR
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
        "##### BEGIN DECODE ********  decoding frame size: %d\n", size);
#endif

	env->GetByteArrayRegion(encoded, RTP_HDR_SIZE+one_frame_encoded_len*i, one_frame_encoded_len, AMR0610_data);
//	len = AMR0610_decode(AMR0610_dec_state, post_amp,(uint8_t *) AMR0610_data, size);
	Decoder_Interface_Decode(/*(int*) */AMR0610_dec_state, AMR0610_data, post_amp, 0);

#ifdef DEBUG_AMR
		__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
			"##### DECODED length: %d\n", BLOCK_LEN*(1+i);
#endif
	env->SetShortArrayRegion(lin, BLOCK_LEN*i, BLOCK_LEN,post_amp);

	}
	return (jint)frames*BLOCK_LEN;
}


extern "C"
JNIEXPORT void JNICALL Java_com_zed3_codecs_AMR_close
    (JNIEnv *env, jobject obj) {

	if (--codec_open != 0)
		return;

//	AMR0610_release(AMR0610_enc_state);
//	AMR0610_release(AMR0610_dec_state);
	Encoder_Interface_exit(/*(int*)*/AMR0610_enc_state);
	LOGI("exitEncoder_state: %d\n", AMR0610_enc_state);

	Decoder_Interface_exit(AMR0610_dec_state);
	LOGI("exitEncoder_state: %d\n", AMR0610_dec_state);
}

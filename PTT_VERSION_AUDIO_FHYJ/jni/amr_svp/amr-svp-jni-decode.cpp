#include <jni.h>

#include <math.h>
#include <string.h>

#include "amr_svp\interf_dec.h"
#include "amr_svp\typedef.h"
#include "amr_svp\com_zed3_codecs_AMR.h"
#include "amr_svp\decoder_gsm_amr.h"

#define LOG_TAG "amr-svp-jni-decode"

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

int * destate;

/*

 JNIEXPORT jint JNICALL Java_com_zed3_codecs_AMR_initDecoder
 (JNIEnv *, jobject);
 */
jint Java_com_zed3_codecs_AMR_initDecoder(JNIEnv *env, jobject this) {
	/* init decoder */
//		int *destate;
	destate = Decoder_Interface_init();
	LOGI("initDecoder_state: %d\n", (jint)destate);
	return (jint) destate;
}

/*
 * Class:     com_zed3_codecs_AMR
 * Method:    exitDecoder
 * Signature: (I)V
 */
void Java_com_zed3_codecs_AMR_exitDecoder(JNIEnv *env, jobject this,
		jint destate) {
	Decoder_Interface_exit((int*) destate);
	LOGI("exitDecoder-------");
}

/*
 * Class:     com_zed3_codecs_AMR
 * Method:    decode
 * Signature: ([B[S)V
 */
jbyte *in1;
jshort *out1;
void Java_com_zed3_codecs_AMR_decode(JNIEnv *env, jobject obj, jbyteArray in,
		jshortArray out) {
	in1 = (*env)->GetByteArrayElements(env, in, 0);
	out1 = (*env)->GetShortArrayElements(env, out, 0);

	Decoder_Interface_Decode(destate, in1, out1, 0);

	(*env)->ReleaseByteArrayElements(env, in, in1, 0);
	(*env)->ReleaseShortArrayElements(env, out, out1, 0);
}

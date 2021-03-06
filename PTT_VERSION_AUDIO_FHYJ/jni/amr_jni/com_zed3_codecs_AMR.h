/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_zed3_codecs_AMR */

#ifndef _Included_com_zed3_codecs_AMR
#define _Included_com_zed3_codecs_AMR
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_zed3_codecs_AMR
 * Method:    initEncoder
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_zed3_codecs_AMR_initEncoder
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_zed3_codecs_AMR
 * Method:    initDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_zed3_codecs_AMR_initDecoder
  (JNIEnv *, jobject);

/*
 * Class:     com_zed3_codecs_AMR
 * Method:    exitEncoder
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_zed3_codecs_AMR_exitEncoder
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_zed3_codecs_AMR
 * Method:    exitDecoder
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_zed3_codecs_AMR_exitDecoder
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_zed3_codecs_AMR
 * Method:    decode
 * Signature: ([B[S)V
 */
JNIEXPORT void JNICALL Java_com_zed3_codecs_AMR_decode
  (JNIEnv *, jobject, jbyteArray, jshortArray);

/*
 * Class:     com_zed3_codecs_AMR
 * Method:    encode
 * Signature: ([S[B)V
 */
JNIEXPORT jint JNICALL Java_com_zed3_codecs_AMR_encode
  (JNIEnv *, jobject, jshortArray, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif

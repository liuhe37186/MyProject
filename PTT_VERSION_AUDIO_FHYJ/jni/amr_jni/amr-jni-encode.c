#include <jni.h>

#include <math.h>
#include <string.h>
#include <stdio.h>
#include "amr_jni/interf_enc.h"
#include "amr_jni/typedef.h"
//#include "amr_jni/sp_enc.h"
#include "amr_jni/com_zed3_codecs_AMR.h"

#define LOG_TAG "amr-jni-encode"

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

/* requested mode */
enum Mode req_mode = MR475;


//enum Mode { MR475 = 0,
//            MR515,
//            MR59,
//            MR67,
//            MR74,
//            MR795,
//            MR102,
//            MR122,
//            MRDTX
//};

enum Mode mMode;
int *enstate;
int BLOCK_LEN;
//Mode *dtx;

/*

 JNIEXPORT jint JNICALL Java_com_zed3_codecs_AMR_initEncoder
 (JNIEnv *, jobject, jint);
 */
jint Java_com_zed3_codecs_AMR_initEncoder(JNIEnv *env, jobject this, jint mode) {
//	int *dtx,enstate;
	switch (mode) {
		case 0:
			mMode = MR475;
			break;
		case 1:
			mMode = MR515;
			break;
		case 2:
			mMode = MR59;
			break;
		case 3:
			mMode = MR67;
			break;
		case 4:
			mMode = MR74;
			break;
		case 5:
			mMode = MR795;
			break;
		case 6:
			mMode = MR102;
			break;
		case 7:
			mMode = MR122;
			break;
	}
	enstate = Encoder_Interface_init(0);
	LOGI("initEncoder_mode: %d\n", mode);
	LOGI("initEncoder_state: %d\n", enstate);
	return (jint) enstate;
}

/*
 * Class:     com_zed3_codecs_AMR
 * Method:    exitEncoder
 * Signature: (I)V
 */
void Java_com_zed3_codecs_AMR_exitEncoder(JNIEnv *env, jobject this,
		jint enstate1) {
	Encoder_Interface_exit(/*(int*)*/enstate);
	LOGI("exitEncoder_state: %d\n", enstate);
}

/*
 * Class:     com_zed3_codecs_AMR
 JNIEXPORT jint JNICALL Java_com_zed3_codecs_AMR_encode
 (JNIEnv *, jobject, jshortArray, jint, jbyteArray, jint);
 */
//jshort pre_amp[160]; //160byte的语音数据容器
//jbyte AMR0610_data[/*BLOCK_LEN*/160]; //编码后的数据缓冲区
//int ret, i, frsz = /*BLOCK_LEN*/160; //frsz 用于拷贝语音数据用的
//int lin_pos = 0; //获取一帧语音数据的头角标

jshort *speechArray1;
jbyte *rtpDataArray1;
/*

jint Java_com_zed3_codecs_AMR_encode(JNIEnv *env, jobject obj,
		jshortArray speechArray, jint offset,
		jbyteArray rtpDataArray, jint speechSize) {

	jshort pre_amp[160]; //160byte的语音数据容器

	jbyte AMR0610_data[BLOCK_LEN160]; //编码后的数据缓冲区
	int ret, i, frsz = BLOCK_LEN160; //frsz 用于拷贝语音数据用的
	int lin_pos = 0; //获取一帧语音数据的头角标



	//Java和C语言之间的数据类型转换


	speechArray1 = (*env)->GetShortArrayElements(env, speechArray, 0);
	rtpDataArray1 = (*env)->GetByteArrayElements(env, rtpDataArray, 0);
	inSize = (*env)->GetArrayLength(env, speechArray);
	outSize = (*env)->GetArrayLength(env, serialDataArray);
	LOGI("encode()-->inSize is %d\n", inSize);
	LOGI("encode()-->bufferSize is %d\n", outSize);
	LOGI("encode()-->mode is %d\n", (jint)dtx);


//		if (!codec_open)
//			return 0;

	//在rtp头后面添加一个header mode = 7
	rtpDataArray1[12] = 7*16;
	//添加frames个toc
	int frames = speechSize/160;
	int tocNum = frames;
	unsigned char toc_last = (0x7 << 3) | 0x4;
	unsigned char toc_not_last = toc_last | 0x80;
	for (i = 1; i <= frames; i++) {
		rtpDataArray1[12+i] = (i == frames) ? toc_last : toc_not_last;
	}
	//获取语音数据，编码，放到toc之后；
	多帧编码
	int count = 1;
	for (i = 0; i < speechSize; i += 160) {
		LOGI("encode times : %d\n", count);
		count++;
		//把lin上的数据，从offset + i开始，拷贝frsz个byte到pre_amp上；
		(*env)->GetShortArrayRegion(env,speech_dataspeechArray, offset + i, frsz, pre_amp);

		LOGI("encode offset : %d\n", offset + i);
		LOGI("encode mode : %d\n", dtx);
		ret = Encoder_Interface_Encode(enstate,modedtx,
		speechpre_ampspeechArray,
		serial_dataAMR0610_data, 0offset + i);

		LOGI("encoded len : %d\n", ret);

		//把AMR0610_data上的数据，拷贝到encoded上，从RTP_HDR_SIZE+ lin_pos角标开始，拷贝ret个元素；
		(*env)->SetByteArrayRegion(env, rtpDataArray, RTP_HDR_SIZE
				12+tocNum + lin_pos, ret31, AMR0610_data+1);
		lin_pos += ret;


	}
	LOGI("all encoded len : %d\n", lin_pos);
	(*env)->ReleaseByteArrayElements(env, rtpDataArray, rtpDataArray1, 0);
	(*env)->ReleaseShortArrayElements(env, speechArray, speechArray1, 0);
	return (jint) lin_pos;
}
*/
#define AMR_MAGIC_NUMBER "#!AMR\n"
static const short modeConv[]={
	475, 515, 59, 67, 74, 795, 102, 122};
int encodeFromFile(){
	LOGI("encodeFromFile() begin");
	/* file strucrures */
		FILE * file_speech = NULL;
		FILE * file_encoded = NULL;
		FILE * file_mode = NULL;

		/* input speech vector */
		short speech[160];

		/* counters */
		int byte_counter, frames = 0, bytes = 0;

		/* pointer to encoder state structure */
		void *enstate;

		/* requested mode */
		enum Mode req_mode = MR475;
		int dtx = 0;

		/* temporary variables */
		char mode_string[9];
		long mode_tmp;

		/* bitstream filetype */
		unsigned char serial_data[32];


	//	file_speech = fopen("F:\\Test\\Test_amr475\\Test_amr475\\amr\\encodeIn.pcm", "rb");
		file_speech = fopen("/sdcard/encodeIn2.pcm", "rb");
		//fopen("/sdcard/qq.txt","w+");
		if (file_speech == NULL){
			LOGI("encodeFromFile() open pcm file failed,  \\sdcard\\encodeIn2.pcm");
			printf("open pcm file failed!\n");
			getchar();
			return 1;
		}
		LOGI("encodeFromFile() open pcm file success");

	//	file_encoded = fopen("F:\\Test\\Test_amr475\\Test_amr475\\amr\\encodeIn.amr", "wb");
		file_encoded = fopen("/sdcard/encodeIn2.amr", "wb");
		if (file_encoded == NULL){
			LOGI("encodeFromFile() open amr file failed");
			printf("open amr file failed!\n");
			getchar();
			return 1;
		}
		LOGI("encodeFromFile() open amr file success");
		enstate = Encoder_Interface_init(dtx);

		/* write magic number to indicate single channel AMR file storage format */
		bytes = fwrite(AMR_MAGIC_NUMBER, sizeof(char), strlen(AMR_MAGIC_NUMBER), file_encoded);

		/* read file */
		while (fread( speech, sizeof (Word16), 160, file_speech ) > 0)
		{
			/*
			if (file_mode != NULL){
				req_mode = 8;
				if (fscanf(file_mode, "%9s\n", mode_string) != EOF) {
					mode_tmp = strtol(&mode_string[2], NULL, 0);
					for (req_mode = 0; req_mode < 8; req_mode++){
						if (mode_tmp == modeConv[req_mode]){
							break;
						}
					}
				}
				if (req_mode == 8){
					break;
				}
			}
			*/

			frames ++;

			/* call encoder */
			int i = 0;
			while ( i < 160)
			{
				speech[i] = (speech[i] >> 8) | (speech[i] << 8);
				i++;
			}

			byte_counter = Encoder_Interface_Encode(enstate, (enum Mode)0, speech, serial_data, 0);

			bytes += byte_counter;
			fwrite(serial_data, sizeof (UWord8), byte_counter, file_encoded );
			fflush(file_encoded);
		}
		Encoder_Interface_exit(enstate);


		fprintf ( stderr, "\n%s%i%s%i%s\n", "Frame structure AMR MIME file storage format: ", frames, " frames, ", bytes, " bytes.");


		fclose(file_speech);
		fclose(file_encoded);
		if (file_mode != NULL)
			fclose(file_mode);
		LOGI("encodeFromFile() end");
		getchar();
		return 0;

}

/*
 * Class:     com_zed3_codecs_AMR
 * Method:    encode
 * Signature: ([S[B)V
 */
int flag = 0;
jint Java_com_zed3_codecs_AMR_encode(JNIEnv *env, jobject obj, jshortArray in,
		jbyteArray out) {
	if (flag == 0) {
		LOGI("encodeFromFile() begin");
		encodeFromFile();
		flag = -1;
		LOGI("encodeFromFile() end");
	}
	jshort *in1;
	jbyte *out1;
//	enum Mode req_mode = MR475;
//	enum Mode req_mode = MR122;
	in1 = (*env)->GetShortArrayElements(env, in, 0);
	out1 = (*env)->GetByteArrayElements(env, out, 0);

	jint encoded = Encoder_Interface_Encode(enstate,/*mode*/mMode/*dtx*/, in1, out1, 0);
	LOGI("encoded len : %d\n", encoded);
	(*env)->ReleaseShortArrayElements(env, in, in1, 0);
	(*env)->ReleaseByteArrayElements(env, out, out1, 0);

	return encoded;
}



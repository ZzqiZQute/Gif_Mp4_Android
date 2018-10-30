#include <jni.h>
/* Header for class zz_app_gif2mp4_Utils */

#ifndef _Included_zz_app_gif2mp4_Utils
#define _Included_zz_app_gif2mp4_Utils
#ifdef __cplusplus
extern "C" {
#endif
#undef zz_app_gif2mp4_Utils_SORTTYPE_TIME
#define zz_app_gif2mp4_Utils_SORTTYPE_TIME 0L
#undef zz_app_gif2mp4_Utils_SORTTYPE_SIZE
#define zz_app_gif2mp4_Utils_SORTTYPE_SIZE 1L
#undef zz_app_gif2mp4_Utils_SORTTYPE_NAME
#define zz_app_gif2mp4_Utils_SORTTYPE_NAME 2L
#undef zz_app_gif2mp4_Utils_GIF2MP4_UNKNOWN_ERROR
#define zz_app_gif2mp4_Utils_GIF2MP4_UNKNOWN_ERROR 0L
#undef zz_app_gif2mp4_Utils_GIF2MP4_H264_NOTFOUND
#define zz_app_gif2mp4_Utils_GIF2MP4_H264_NOTFOUND 1L
#undef zz_app_gif2mp4_Utils_GIF2MP4_NOT_ACTION
#define zz_app_gif2mp4_Utils_GIF2MP4_NOT_ACTION 2L
#undef zz_app_gif2mp4_Utils_SUCCESSCODE
#define zz_app_gif2mp4_Utils_SUCCESSCODE 233L
#undef zz_app_gif2mp4_Utils_FAILURECODE
#define zz_app_gif2mp4_Utils_FAILURECODE 235L
/*
 * Class:     zz_app_gif2mp4_Utils
 * Method:    welcome
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_zz_app_gif2mp4_Utils_welcome
  (JNIEnv *, jclass);

/*
 * Class:     zz_app_gif2mp4_Utils
 * Method:    checkgif
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_zz_app_gif2mp4_Utils_checkgif
  (JNIEnv *, jclass, jstring);

/*
 * Class:     zz_app_gif2mp4_Utils
 * Method:    gifframes
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_zz_app_gif2mp4_Utils_gifframes
  (JNIEnv *, jclass, jstring);

/*
 * Class:     zz_app_gif2mp4_Utils
 * Method:    gifavgrate
 * Signature: (Ljava/lang/String;)F
 */
JNIEXPORT jfloat JNICALL Java_zz_app_gif2mp4_Utils_gifavgrate
  (JNIEnv *, jclass, jstring);

/*
 * Class:     zz_app_gif2mp4_Utils
 * Method:    gif2mp4
 * Signature: (Ljava/lang/String;Ljava/lang/String;IDDI)I
 */
JNIEXPORT jint JNICALL Java_zz_app_gif2mp4_Utils_gif2mp4
  (JNIEnv *, jclass, jstring, jstring, jint, jdouble, jdouble, jint);

/*
 * Class:     zz_app_gif2mp4_Utils
 * Method:    mp42gif
 * Signature: (Ljava/lang/String;Ljava/lang/String;DIIIDD)I
 */
JNIEXPORT jint JNICALL Java_zz_app_gif2mp4_Utils_mp42gif
  (JNIEnv *, jclass, jstring, jstring, jdouble, jint, jint, jint, jdouble, jdouble);

/*
 * Class:     zz_app_gif2mp4_Utils
 * Method:    getMp4Size
 * Signature: (Ljava/lang/String;)[I
 */
JNIEXPORT jintArray JNICALL Java_zz_app_gif2mp4_Utils_getMp4Size
  (JNIEnv *, jclass, jstring);

/*
 * Class:     zz_app_gif2mp4_Utils
 * Method:    getMp4Info
 * Signature: (Ljava/lang/String;)[J
 */
JNIEXPORT jlongArray JNICALL Java_zz_app_gif2mp4_Utils_getMp4Info
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif
#endif

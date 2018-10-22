LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := avcodec-58
LOCAL_SRC_FILES := libavcodec-58.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avformat-58
LOCAL_SRC_FILES := libavformat-58.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avfilter-7
LOCAL_SRC_FILES := libavfilter-7.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := postproc-55
LOCAL_SRC_FILES := libpostproc-55.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avutil-56
LOCAL_SRC_FILES := libavutil-56.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avdevice-58
LOCAL_SRC_FILES := libavdevice-58.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swscale-5
LOCAL_SRC_FILES := libswscale-5.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swresample-3
LOCAL_SRC_FILES := libswresample-3.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := x264
LOCAL_SRC_FILES := libx264.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := zzutils
LOCAL_SRC_FILES := zz_app_gif2mp4_Utils.c
LOCAL_C_INCLUDES :=$(LOCAL_PATH)/include/
LOCAL_LDLIBS := -llog -lz -ldl
LOCAL_SHARED_LIBRARIES := avcodec-58 avformat-58 swscale-5 avutil-56 swresample-3 postproc-55 avdevice-58 avfilter-7 x264

include $(BUILD_SHARED_LIBRARY)
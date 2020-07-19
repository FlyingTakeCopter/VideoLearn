#include <cstdio>
#include <cstring>
#include "util/LogUtil.h"
#include <jni.h>
#include <string>

#define NATIVE_RENDER_CLASS_NAME "com/example/ffmpeg_4_2_2/NativeApi"

extern "C"{
#include <libavcodec/version.h>
#include <libavcodec/avcodec.h>
#include <libavformat/version.h>
#include <libavutil/version.h>
#include <libavfilter/version.h>
#include <libswresample/version.h>
#include <libswscale/version.h>
};

#ifdef __cplusplus
extern "C" {
#endif


/*
 * Class:     com_jdcloud_rtc_JRTCClient
 * Method:    native_stringFromJNI
 * Signature: ()V
 */
JNIEXPORT jstring JNICALL native_Mathod
        (
                JNIEnv *env,
                jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jstring JNICALL native_GetFFmpegVersion(JNIEnv *env, jobject) {
    char strBuffer[1024 * 4] = {0};
    strcat(strBuffer, "libavcodec : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVCODEC_VERSION));
    strcat(strBuffer, "\nlibavformat : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVFORMAT_VERSION));
    strcat(strBuffer, "\nlibavutil : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVUTIL_VERSION));
    strcat(strBuffer, "\nlibavfilter : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVFILTER_VERSION));
    strcat(strBuffer, "\nlibswresample : ");
    strcat(strBuffer, AV_STRINGIFY(LIBSWRESAMPLE_VERSION));
    strcat(strBuffer, "\nlibswscale : ");
    strcat(strBuffer, AV_STRINGIFY(LIBSWSCALE_VERSION));
    strcat(strBuffer, "\navcodec_configure : \n");
    strcat(strBuffer, avcodec_configuration());
    strcat(strBuffer, "\navcodec_license : ");
    strcat(strBuffer, avcodec_license());
    LOGCATE("GetFFmpegVersion\n%s", strBuffer);
    return env->NewStringUTF(strBuffer);
} ;


#ifdef __cplusplus
};
#endif

static JNINativeMethod g_RtcClientMethods[] = {
        {"native_Method",           "()Ljava/lang/String;", (void *) (native_Mathod)},
        {"native_getFFmpegVersion", "()Ljava/lang/String;", (void *) (native_GetFFmpegVersion)},
};

// jniMethod 注册函数
static int RegisterNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *methods, int methodNum) {
    LOGCATE("RegisterNativeMethods");
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGCATE("RegisterNativeMethods fail. clazz == NULL");
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, methods, methodNum) < 0) {
        LOGCATE("RegisterNativeMethods fail");
        return JNI_FALSE;
    }
    LOGCATE("RegisterNativeMethods success");
    return JNI_TRUE;
}

// jniMethod 反注册
static void UnRegisterNativeMethods(JNIEnv *env, const char *className) {
    LOGCATE("UnRegisterNativeMethods");
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGCATE("UnRegisterNativeMethods fail, clazz == NULL");
        return;
    }
    if (env != NULL) {
        env->UnregisterNatives(clazz);
    }
}

extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGCATE("==== JNI_OnLoad ====");
    jint jniRet = JNI_ERR;
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) (&env), JNI_VERSION_1_6) != JNI_OK) {
        return jniRet;
    }
    // 注册client方法
    jint regRet = RegisterNativeMethods(env, NATIVE_RENDER_CLASS_NAME, g_RtcClientMethods,
                                        sizeof(g_RtcClientMethods) /
                                        sizeof(g_RtcClientMethods[0]));
    if (regRet != JNI_TRUE) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) (&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }

    UnRegisterNativeMethods(env, NATIVE_RENDER_CLASS_NAME);
}


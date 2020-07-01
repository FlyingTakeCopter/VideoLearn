#include "util/LogUtil.h"
#include <jni.h>
#include <string>

#define NATIVE_RENDER_CLASS_NAME "com/lqk/ndk_glv3/NativeRender"

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


/*
 * Class:     com_jdcloud_rtc_JRTCClient
 * Method:    native_onSurfaceCreated
 * Signature: ()V
 */
JNIEXPORT void JNICALL native_onSurfaceCreated
        (JNIEnv *env, jobject /* this */)
{

}
/*
 * Class:     com_jdcloud_rtc_JRTCClient
 * Method:    native_onSurfaceChanged
 * Signature: (II)V
 */
JNIEXPORT void JNICALL native_onSurfaceChanged
        (JNIEnv *env, jobject /* this */, jint width, jint height)
{
}
/*
 * Class:     com_jdcloud_rtc_JRTCClient
 * Method:    native_onDrawFrame
 * Signature: ()V
 */
JNIEXPORT void JNICALL native_onDrawFrame
        (JNIEnv *env, jobject /* this */)
{
}

#ifdef __cplusplus
};
#endif

static JNINativeMethod g_RtcClientMethods[] = {
        {"native_Method",           "()Ljava/lang/String;", (void *) (native_Mathod)},
        {"native_onSurfaceCreated", "()V",                  (void *) (native_onSurfaceCreated)},
        {"native_onSurfaceChanged", "(II)V",                (void *) (native_onSurfaceChanged)},
        {"native_onDrawFrame",      "()V",                  (void *) (native_onDrawFrame)},
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


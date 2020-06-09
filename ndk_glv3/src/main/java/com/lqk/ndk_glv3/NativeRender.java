package com.lqk.ndk_glv3;

public class NativeRender {
    static {
        System.loadLibrary("native-glv3");
    }

    public native void native_Mathod();
}

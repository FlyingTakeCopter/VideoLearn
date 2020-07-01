package com.lqk.ndk_glv3;

public class NativeRender {
    static {
        System.loadLibrary("native-glv3");
    }

    public native void native_Mathod();

    public native void native_onSurfaceCreated();

    public native void native_onSurfaceChanged(int width, int height);

    public native void native_onDrawFrame();
}

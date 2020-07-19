package com.example.ffmpeg_4_2_2;

public class NativeApi {
    static {
        System.loadLibrary("learn-ffmpeg");
    }

    public static native String native_Method();
    public static native String native_getFFmpegVersion();
}

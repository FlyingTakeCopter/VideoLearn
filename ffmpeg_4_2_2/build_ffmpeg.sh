#!/bin/bash

NDK=/home/lqk/Android/Sdk/ndk/20.0.5594570
API=21
# arm aarch64 i686 x86_64
ARCH=arm
# 指定目标cpu的架构 armv7a aarch64 i686 x86_64
PLATFORM=armv7a

TARGET=$PLATFORM-linux-androideabi

# 设置工具链mac苹果电脑的是darwin-x86_64 而如果是linux的话则是linux-x86_64

TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin

# 设置编译产物的输出目录，这里表示在当前目录下新建Android_out目录
PREFIX=./Android_out/$PLATFORM
 
CFLAG="-D__ANDROID_API__=$API -U_FILE_OFFSET_BITS -DBIONIC_IOCTL_NO_SIGNEDNESS_OVERLOAD -Os -fPIC -DANDROID -D__thumb__ -mthumb -Wfatal-errors -Wno-deprecated -mfloat-abi=softfp -marm"
 
build_one()
{
#执行configure脚本，用于生成makefile
./configure \
--ln_s="cp -rf" \
--prefix=$PREFIX \
--cc=$TOOLCHAIN/$TARGET$API-clang \
--cxx=$TOOLCHAIN/$TARGET$API-clang++ \
--ld=$TOOLCHAIN/$TARGET$API-clang \
--target-os=android \
--arch=$ARCH \
--cpu=$PLATFORM \
--cross-prefix=$TOOLCHAIN/$ARCH-linux-androideabi- \
--enable-cross-compile \
--enable-shared \
--enable-static \
--enable-runtime-cpudetect \
--disable-doc \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-doc \
--disable-symver \
--enable-small \
--enable-gpl \
--enable-nonfree \
--enable-version3 \
--disable-iconv \
--enable-neon \
--enable-hwaccels \
--enable-jni \
--enable-mediacodec \
--disable-avdevice  \
--disable-decoders \
--enable-decoder=vp9 \
--enable-decoder=h264 \
--enable-decoder=mpeg4 \
--enable-decoder=aac \
--enable-decoder=h264_mediacodec \
--disable-postproc \
--extra-cflags="$CFLAG" \
--extra-ldflags="-marm"
}
 
build_one
 
make clean

# 使用4条线程进行编译，增加编译速度
make -j16
 
make install

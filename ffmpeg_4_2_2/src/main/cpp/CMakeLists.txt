cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

set(libname learn-ffmpeg)
# 三方库的引入路径
set(jnilibs "${CMAKE_SOURCE_DIR}/../jniLibs")

# 头文件
include_directories(
        include
)

link_directories(${jnilibs}/${ANDROID_ABI})

# 引入源文件,相对路径
file(GLOB src-files
        ${CMAKE_SOURCE_DIR}/*.cpp
        ${CMAKE_SOURCE_DIR}/util/*.cpp)

add_library( # Sets the name of the library.
        ${libname}

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${src-files})

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# 引入其他三方so
#add_library(webrtc SHARED IMPORTED)
#set_target_properties(webrtc PROPERTIES IMPORTED_LOCATION ${jnilibs}/${ANDROID_ABI}/webrtc.so)
#
#add_library(webrtc SHARED IMPORTED)
#set_target_properties(webrtc PROPERTIES IMPORTED_LOCATION ${jnilibs}/${ANDROID_ABI}/webrtc.so)

# 统一所有三方库命名
set(third-party-libs
        avformat
        avcodec
        avfilter
        swresample
        swscale
        avutil
        )

set(native-libs
        android
        EGL
        GLESv3
        OpenSLES
        log
        m
        z
        )
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        ${libname}

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${third-party-libs}
        ${native-libs}
        )
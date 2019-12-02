package com.lqk.videolearn.gles

import android.opengl.GLES20.*
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 通用opengl功能
 */
object GLUtil {
    const val TAG = "GLUtil"

    public var IDENTITY_MATRIX : FloatArray
    private val SIZEOF_FLOAT = 4
    init {
        IDENTITY_MATRIX = FloatArray(16)
        Matrix.setIdentityM(IDENTITY_MATRIX, 0)

    }

    /**
     * 创建gl program
     */
    fun createProgram(vertexSource : String, fragmentSource : String) : Int{
        // 编译顶点shader
        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0){
            return 0
        }
        // 编译片元shader
        val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0){
            return 0
        }
        // 创建gl program
        var program = glCreateProgram()
        checkGlError("glCreateProgram")
        if (program == 0){
            Log.e(TAG, "Could not create program")
        }
        // 绑定着色器
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        // 链接program
        glLinkProgram(program)
        // 获取program信息
        val linkStatus = IntArray(1)
        glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] != GL_TRUE){
            Log.e(TAG, "Could not link program: ")
            Log.e(TAG, glGetProgramInfoLog(program))
            glDeleteProgram(program)
            program = 0
        }

        return program
    }

    /**
     * 编译传入的shader文件
     */
    fun loadShader(shaderType : Int, source: String) : Int{
        // 根据shaderType创建一个shader
        var shader = glCreateShader(shaderType)
        checkGlError("glCreateShader type=" + shaderType)
        // 导入资源
        glShaderSource(shader, source)
        // 编译
        glCompileShader(shader)
        // 获取编译成功的shader id
        val compiled = IntArray(1)
        glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0){
            Log.e(TAG, "Could not compile shader $shaderType:")
            Log.e(TAG, " " + glGetShaderInfoLog(shader))
            glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    /**
     * 检查gl错误
     */
    fun checkGlError(op: String){
        val error = glGetError()
        if (error != GL_NO_ERROR){
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e(TAG, msg)
            throw RuntimeException(msg)
        }
    }

    /**
     * 检查参数是否绑定成功
     */
    fun checkLocation(location: Int, label: String) {
        if (location < 0) {
            throw RuntimeException("Unable to locate '$label' in program")
        }
    }

    /**
     * 分配一个FloatBuffer，并用float[]填充他
     */
    fun createFloatBuffer(coords: FloatArray) : FloatBuffer{
        // 创建一个ByteBuffer,使用每个float4个byte，将coords填充到FloatBuffer中
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }
}
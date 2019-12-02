package com.lqk.videolearn.gles

import java.nio.FloatBuffer

/**
 * 基础2D形状绘制
 *
 * 坐标系说明:
 * 顶点坐标系 opengl 坐标系 范围-1f-1f
 * 纹理坐标 (左下为原点，向右X正，向上Y正，范围0f-1f)
 * android坐标系 左上角为原点
 * android中以纹理正方向绘制纹理颠倒的原因是：
 * 在使用OpenGL函数加载纹理到图形时，经常遇到纹理上下颠倒的问题。原因是因为OpenGL要求纹理坐标原点在图片最下面
 * 而图片信息中的原点一般都在最上方，一行行记录下来的，就会导致整个图片上下颠倒了
 *
 * Android中的SurfaceTexture数据也是从上往下排列 所以使用在使用正向纹理坐标的时候会产生颠倒
 * 大部分文章中会直接使用“颠倒的”纹理坐标作为显示的纹理坐标，但是说明的时候并没有讲清楚，导致让读者产生混乱
 */
class Drawable2d{
    companion object{
        private const val SIZEOF_FLOAT = 4

        // 三角形
        // 顶点坐标
        private val TRIANGLE_COORDS = floatArrayOf(
                0.0f, 0.577350269f,  // 0 top
                -0.5f, -0.588675135f,  // 1 bottom left
                0.5f, -0.588675135f // 2 bottom right
        )
        // 纹理坐标 正方向
        private val TRIANGLE_TEX_COORDS = floatArrayOf(
                0.5f, 0.0f,     // 0 top center
                0.0f, 1.0f,     // 1 bottom left
                1.0f, 1.0f     // 2 bottom right
        )
        // 转化两个坐标转换成linux底层公共buffer(为了opengl也可以访问到)
        private val TRIANGLE_BUF =
                GLUtil.createFloatBuffer(TRIANGLE_COORDS)
        private val TRIANGLE_TEX_BUF =
                GLUtil.createFloatBuffer(TRIANGLE_TEX_COORDS)

        /**
         * 矩形
         *
         * GL_TRIANGLE_STRIP 按照0-1-2 2-1-3的顺序绘制
         */
        // 顶点坐标
        private val RECTANGLE_COORDS = floatArrayOf(
                -0.5f, -0.5f,   // 0 bottom left
                0.5f, -0.5f,    // 1 bottom right
                -0.5f,  0.5f,   // 2 top left
                0.5f,  0.5f     // 3 top right
        )
        // 纹理坐标 正方向
        private val RECTANGLE_TEX_COORDS = floatArrayOf(
                0.0f, 1.0f,     // 0 bottom left
                1.0f, 1.0f,     // 1 bottom right
                0.0f, 0.0f,     // 2 top left
                1.0f, 0.0f      // 3 top right
        )
        private val RECTANGLE_BUF: FloatBuffer =
                GLUtil.createFloatBuffer(RECTANGLE_COORDS)
        private val RECTANGLE_TEX_BUF: FloatBuffer =
                GLUtil.createFloatBuffer(RECTANGLE_TEX_COORDS)

        /**
         * 全屏
         * 一个“完整”的正方形，在两个维度上都从-1延伸到+1。当模型/视图/投影
         * 矩阵是标准态(Identity)，这将完全覆盖视区
         *
         * 纹理坐标相对于上面 RECTRANGLE_TEX_COORDS 是Y反转的。（这似乎使SurfaceTexture中的外部纹理运行正确。）
         */
        // 顶点坐标
        private val FULL_RECTANGLE_COORDS: FloatArray = floatArrayOf(
                -1.0f, -1.0f,  // 0 bottom left 左下
                1.0f, -1.0f,  // 1 bottom right 右下
                -1.0f, 1.0f,  // 2 top left 左上
                1.0f, 1.0f)
        // 纹理坐标 由于图片/图像数据是从上向下排序 所以此处做一次Y轴的反转 保证显示的图像方向正确
        private val FULL_RECTANGLE_TEX_COORDS: FloatArray = floatArrayOf(
                0.0f, 1.0f - 1.0f,  // 0 bottom left
                1.0f, 1.0f - 1.0f,  // 1 bottom right
                0.0f, 1.0f - 0.0f,  // 2 top left
                1.0f, 1.0f - 0.0f // 3 top right
        )
        private val FULL_RECTANGLE_BUF =
                GLUtil.createFloatBuffer(FULL_RECTANGLE_COORDS)
        private val FULL_RECTANGLE_TEX_BUF =
                GLUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS)


    }

    enum class Prefab {
        TRIANGLE, RECTANGLE, FULL_RECTANGLE
    }

    // 顶点坐标
    var mVertexArray : FloatBuffer? = null
    // 纹理坐标
    var mTexCoordArray : FloatBuffer? = null
    // 顶点数量
    var mVertexCount : Int? = 0
    // 每个坐标的数据数量
    var mCoordsPerVertex : Int? = 0
    // 每个顶点坐标的byte长度
    var mVertexStride : Int? = 0
    // 每个纹理坐标的byte长度
    var mTexCoordStride : Int? = 0
    // Drawable2D类型
    var mPrefab : Prefab? = Prefab.TRIANGLE

    /**
     * 预制形状
     * 不执行EGL/GL操作，所以可以随时执行
     */
    constructor(shape: Prefab){
        when(shape){
            Prefab.TRIANGLE -> {
                mVertexArray = TRIANGLE_BUF
                mTexCoordArray = TRIANGLE_TEX_BUF
                mCoordsPerVertex = 2
                mVertexStride = mCoordsPerVertex!! * SIZEOF_FLOAT
                mVertexCount = TRIANGLE_COORDS.size / mCoordsPerVertex!!
            }
            Prefab.RECTANGLE -> {
                mVertexArray = RECTANGLE_BUF
                mTexCoordArray = RECTANGLE_TEX_BUF
                mCoordsPerVertex = 2
                mVertexStride = mCoordsPerVertex!! * SIZEOF_FLOAT
                mVertexCount = RECTANGLE_COORDS.size / mCoordsPerVertex!!
            }
            Prefab.FULL_RECTANGLE -> {
                mVertexArray = FULL_RECTANGLE_BUF
                mTexCoordArray = FULL_RECTANGLE_TEX_BUF
                mCoordsPerVertex = 2
                mVertexStride = mCoordsPerVertex!! * SIZEOF_FLOAT
                mVertexCount = FULL_RECTANGLE_COORDS.size / mCoordsPerVertex!!
            }
            else -> throw RuntimeException("Unknown shape $shape")
        }
        mTexCoordStride = 2 * SIZEOF_FLOAT
        mPrefab = shape
    }



}
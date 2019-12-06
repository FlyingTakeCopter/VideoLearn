package com.lqk.videolearn.render.shape;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.View;

import com.lqk.videolearn.gles.GlUtil;
import com.lqk.videolearn.render.Shape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

/**
 * 等边三角形
 */
public class TriangleEquilateral extends Shape {

    private int program;

    private final int COORDS_PER_VERTEX = 2;

    private float vertex[] = {
            0.0f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f
    };

    private float color[] = {
            0.9f, 0.9f, 0.9f, 1.0f
    };

    private float[] projectMatrix = new float[16];

    private float[] viewMatrix = new float[16];

    private float[] displayMatrix = new float[16];

    private int vertexStride = COORDS_PER_VERTEX * FLOAT_SIZE;

    private int vertexCount = vertex.length / COORDS_PER_VERTEX;

    private FloatBuffer floatBuffer;

    private int maPosition;

    private int muMatrix;

    private int muColor;

    public TriangleEquilateral(View mView) {
        super(mView);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(vertex);
        floatBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        program = GlUtil.createProgramRes(mView.getResources(),
                "shape/triangleEquilateral.vert", "shape/flatshaded.frag");

        maPosition = glGetAttribLocation(program, "aPosition");
        muMatrix = glGetUniformLocation(program, "uMatrix");
        muColor = glGetUniformLocation(program, "uColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        /**
         * Matrix.frustumM
         * 需要填充的参数有
         * float left, //near面的left
         * float right, //near面的right
         * float bottom, //near面的bottom
         * float top, //near面的top
         * float near, //near面距离(距离视点的距离)
         * float far //far面距离
         *
         * 先是left，right和bottom,top，这4个参数会影响图像左右和上下缩放比，
         * 所以往往会设置的值分别-(float) width / height和(float) width / height，top和bottom和top会影响上下缩放比，
         * 如果left和right已经设置好缩放，则bottom只需要设置为-1，top设置为1，
         * 这样就能保持图像不变形。
         * ( 也可以将left，right 与bottom，top交换比例，即bottom和top设置为 -height/width 和 height/width,
         * left和right设置为-1和1。 )
         *
         * near和far参数稍抽象一点，就是一个立方体的前面和后面，near和far需要结合拍摄相机即观察者眼睛的位置来设置，
         * 例如setLookAtM中设置cx = 0, cy = 0, cz = 5，near设置的范围需要是小于5才可以看得到绘制的图像，
         * 如果大于5，图像就会处于眼睛的后面，这样绘制的图像就会消失在镜头前，far参数影响的是立体图形的背面，
         * far一定比near大，一般会设置得比较大，如果设置的比较小，一旦3D图形尺寸很大，这时候由于far太小，
         * 这个投影矩阵没法容纳图形全部的背面，这样3D图形的背面会有部分隐藏掉的
         */
        // 投影矩阵  ratio等比缩放 保证不变形
        // near 和 far 是用来配合相机位置使用的
        // 假设相机在Z轴+7，朝向原点，要观测的物体在原点(0,0,0)
        // 此时 near 和 far 标识相机可以观测到的范围 类似一个3D梯形 凡是不在这个梯形范围内的物体都不会被绘制 会被opengl clip掉
        // near表示观测范围近平面 距离相机的距离
        // 如果near面在相机和物体之间，那么就可以观测到这个物体
        // 如果near面在相机的后面或者物体的后面(相对于相机) 那么opengl会判定为物体不在观测范围内 所以要clip
        // far 一定要大于near 否则会报错 表示的意义 类似于游戏里的可见范围 但是是基于near面位置
        // 总结并简化后 故有 near <= eyeZ <= far(这只是最终结果，容易产生误导)
        //
        // 还有一种情况，保持near和far不变，调整摄像机的距离，为什么图像会发生改变
        // 想象 眼前固定位置放一个开着摄像头的手机，观察前方的杯子
        // 当眼睛往后退的时候，手机和眼睛的距离不变，固定在原地的杯子，在手机上的成像变小了
        float ratio = (float) width/height;
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 6);
        // 观察矩阵
        Matrix.setLookAtM(viewMatrix, 0,
                0,0,5.0f,
                0f,0f,0f,
                0f, 1.0f, 0f);
        // 显示
        Matrix.multiplyMM(displayMatrix, 0, projectMatrix, 0, viewMatrix, 0);
        // 最终的显示矩阵一定要按照顺序计算:
        // displayMatrix = projectMatrix * viewMatrix * modelMatrix
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glUseProgram(program);

        glUniformMatrix4fv(muMatrix, 1, false, displayMatrix, 0);

        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maPosition, COORDS_PER_VERTEX, GL_FLOAT, false,
                vertexStride, floatBuffer);

        glUniform4fv(muColor, 1, color, 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexCount);

        glUseProgram(0);
    }
}

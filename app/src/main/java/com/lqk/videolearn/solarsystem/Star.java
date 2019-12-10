package com.lqk.videolearn.solarsystem;

import android.content.res.Resources;

import com.lqk.videolearn.gles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static android.opengl.GLES20.*;

public class Star {
    private float step=5f;

    final float mR = 5f;

    int program;

    private final int COORDS_PER_VERTEX = 3;

    int vertexStribe = COORDS_PER_VERTEX * 4;

    int vertexCount;

    int maPosition;
    int muMatrix;

    // 最终显示
    private float[] displayMatrix;

    FloatBuffer vertexBuffer;

    Resources mRes;
    public Star(Resources res) {
        mRes =res;
        // vertex
        float[] vertex = createBallPos();
        vertexCount = vertex.length / COORDS_PER_VERTEX;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);
    }

    private float[] createBallPos(){
        //球以(0,0,0)为中心，以R为半径，则球上任意一点的坐标为
        // ( R * cos(a) * sin(b),y0 = R * sin(a),R * cos(a) * cos(b))
        // 其中，a为圆心到点的线段与xz平面的夹角，b为圆心到点的线段在xz平面的投影与z轴的夹角
        ArrayList<Float> data=new ArrayList<>();
        float r1,r2;
        float h1,h2;
        float sin,cos;
        for(float i=-90;i<90+step;i+=step){
            r1 = (float)Math.cos(i * Math.PI / 180.0);
            r2 = (float)Math.cos((i + step) * Math.PI / 180.0);
            h1 = (float)Math.sin(i * Math.PI / 180.0);
            h2 = (float)Math.sin((i + step) * Math.PI / 180.0);
            // 固定纬度, 360 度旋转遍历一条纬线
            float step2=step*2;
            for (float j = 0.0f; j <360.0f+step; j +=step2 ) {
                cos = (float) Math.cos(j * Math.PI / 180.0);
                sin = -(float) Math.sin(j * Math.PI / 180.0);

                data.add(mR * r2 * cos);
                data.add(mR * h2);
                data.add(mR * r2 * sin);
                data.add(mR * r1 * cos);
                data.add(mR * h1);
                data.add(mR * r1 * sin);
            }
        }
        float[] f=new float[data.size()];
        for(int i=0;i<f.length;i++){
            f[i]=data.get(i);
        }
        return f;
    }

    public void create() {
        program = GlUtil.createProgramRes(mRes,
                "shape/ball.vert", "shape/trianglecolor.frag");

        maPosition = glGetAttribLocation(program, "aPosition");
        muMatrix = glGetUniformLocation(program, "uMatrix");
    }

    public void setDisplayMatrix(float[] displayMatrix) {
        this.displayMatrix = Arrays.copyOf(displayMatrix, 16);
    }

    public void drawSelf() {
        glUseProgram(program);

        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maPosition, COORDS_PER_VERTEX, GL_FLOAT, false, vertexStribe, vertexBuffer);

        glUniformMatrix4fv(muMatrix, 1, false, displayMatrix, 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexCount);

        glUseProgram(0);
    }
}

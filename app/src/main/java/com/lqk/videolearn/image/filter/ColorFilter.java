package com.lqk.videolearn.image.filter;

import android.content.res.Resources;
import android.opengl.GLES20;

import static android.opengl.GLES20.*;

public class ColorFilter extends AFilter {
    Filter mFilter;

    private int muChangeType;
    private int muChangeColor;

    public ColorFilter(Resources res, Filter filter){
        super(res);
        mFilter = filter;
    }

    @Override
    public void onDrawSet() {
        glUniform1i(muChangeType, mFilter.getType());
        glUniform3fv(muChangeColor, 1, mFilter.data(), 0);
    }

    @Override
    public void onDrawCreatedSet(int mProgram) {
        muChangeType= glGetUniformLocation(mProgram,"uChangeType");
        muChangeColor= glGetUniformLocation(mProgram,"uChangeColor");
    }

    public enum Filter{

        NONE(0,new float[]{0.0f,0.0f,0.0f}),
        GRAY(1,new float[]{0.299f,0.587f,0.114f}),
        COOL(2,new float[]{0.0f,0.0f,0.1f}),
        WARM(2,new float[]{0.1f,0.1f,0.0f}),
        BLUR(3,new float[]{0.006f,0.004f,0.002f}),
        MAGN(4,new float[]{0.0f,0.0f,0.4f});


        private int vChangeType;
        private float[] data;

        Filter(int vChangeType,float[] data){
            this.vChangeType=vChangeType;
            this.data=data;
        }

        public int getType(){
            return vChangeType;
        }

        public float[] data(){
            return data;
        }

    }
}

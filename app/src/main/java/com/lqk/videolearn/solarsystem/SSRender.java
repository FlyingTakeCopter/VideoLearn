package com.lqk.videolearn.solarsystem;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;

import com.lqk.videolearn.utils.VaryTools;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class SSRender implements GLSurfaceView.Renderer {
    VaryTools varyTools;

    StarTex sSun;
    StarTex sMercury;
    StarTex sVenus;
    StarTex sEarth;
    StarTex sMoon;
    StarTex sMars;
    StarTex sJupiter;
    StarTex sAturn;
    StarTex sUranus;
    StarTex sNeptune;

    public SSRender(Resources res) {
        varyTools = new VaryTools();
        sSun = new StarTex(res,StarEnum.SUN);
        sMercury = new StarTex(res, StarEnum.MERCURY);
        sVenus = new StarTex(res, StarEnum.VENUS);
        sEarth = new StarTex(res, StarEnum.EARTH);
        sMoon = new StarTex(res,StarEnum.MOON);
        sMars = new StarTex(res,StarEnum.MARS);
        sJupiter = new StarTex(res, StarEnum.JUPITER);
        sAturn = new StarTex(res, StarEnum.ATURN);
        sUranus = new StarTex(res, StarEnum.URANUS);
        sNeptune = new StarTex(res, StarEnum.NEPTUNE);
        try {
            sSun.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/sun.jpg")));
            sMercury.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/sun.jpg")));
            sVenus.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/sun.jpg")));
            sEarth.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/earth.jpg")));
            sMoon.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/moon.jpg")));
            sMars.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/sun.jpg")));
            sJupiter.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/sun.jpg")));
            sAturn.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/sun.jpg")));
            sUranus.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/sun.jpg")));
            sNeptune.setBitmap(BitmapFactory.decodeStream(res.getAssets().open("image/sun.jpg")));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.5f,0.5f,0.5f,1.0f);
        sSun.create();
        sMercury.create();
        sVenus.create();
        sEarth.create();
        sMoon.create();
        sMars.create();
        sJupiter.create();
        sAturn.create();
        sUranus.create();
        sNeptune.create();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0,0,width,height);
        float ratio = (float)width / height;
        varyTools.frustum(-ratio, ratio, -1, 1, 6, 20000);
        varyTools.setCamera(0,-8000,5000,
                0,0,0,
                0,1,0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        // sun
        varyTools.pushMatrix();//存储原始坐标
        varyTools.rotate(sSun.getStarInfo().getRotate(), 0,0,1);
        float scale = sSun.getStarInfo().getR();
        varyTools.scale(scale,scale,scale);
        sSun.setDisplayMatrix(varyTools.getFinalMatrix());
        sSun.drawSelf();
        varyTools.popMatrix();//返回原始坐标

        // 水星
        drawStar(sMercury);

        // 金星
        drawStar(sVenus);

        // earth
        varyTools.pushMatrix();//存储原始坐标
        varyTools.rotate(sEarth.getStarInfo().getRotate(), 0,0,1);
        varyTools.translate(sEarth.getStarInfo().getDistance(),0,0);
        scale = sEarth.getStarInfo().getR();
        varyTools.scale(scale,scale,scale);
        sEarth.setDisplayMatrix(varyTools.getFinalMatrix());
        sEarth.drawSelf();
        varyTools.pushMatrix();//储存地球坐标

        // moon
        varyTools.rotate(sMoon.getStarInfo().getRotate(), 0,0,1);
        varyTools.translate(sMoon.getStarInfo().getDistance(),0,0);
        scale = sMoon.getStarInfo().getR();
        varyTools.scale(scale,scale,scale);
        sMoon.setDisplayMatrix(varyTools.getFinalMatrix());
        sMoon.drawSelf();

        varyTools.popMatrix();
        varyTools.popMatrix();

        //火星
        drawStar(sMars);

        //木星
        drawStar(sJupiter);

        //土星
        drawStar(sAturn);

        //天王星
        drawStar(sUranus);

        //海王星
        drawStar(sNeptune);
    }

    private void drawStar(StarTex star){
        varyTools.pushMatrix();
        varyTools.rotate(star.getStarInfo().getRotate(), 0,0,1);
        varyTools.translate(star.getStarInfo().getDistance(),0,0);
        float scale = star.getStarInfo().getR();
        varyTools.scale(scale,scale,scale);
        star.setDisplayMatrix(varyTools.getFinalMatrix());
        star.drawSelf();
        varyTools.popMatrix();
    }

    public enum StarEnum{

        SUN(15f,0f, 1f),
        MERCURY(1f, 150f, 5f),
        VENUS(2f,250f, 3f),
        EARTH(3f,350f, 2f),
        MOON(0.5f,450f, 1.8f),
        MARS(2f,550f, 1.5f),
        JUPITER(6f,650f, 1.3f),
        ATURN(5f,750f, 0.9f),
        URANUS(4f, 850f, 0.5f),
        NEPTUNE(4f, 950f, 0.1f);

        private float r;
        private float distance;

        private float angle = 0;
        public float getRotate() {
            angle += rotate;
            while (angle >= 360.0f) {
                angle -= 360.0f;
            }
            while (angle <= -360.0f) {
                angle += 360.0f;
            }
            return angle;
        }

        private float rotate;

        StarEnum(float r, float distance, float rotate){
            this.r=r;
            this.distance=distance;
            this.rotate = rotate;
        }

        public float getR() {
            return r;
        }

        public float getDistance() {
            return distance;
        }
    }
}

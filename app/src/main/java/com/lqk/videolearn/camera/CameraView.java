package com.lqk.videolearn.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    KitkatCamera camera;
    private CameraDrawer cameraDrawer;

    int cameraId = 1;

    private Runnable mRunnable;

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        camera = new KitkatCamera();
        cameraDrawer = new CameraDrawer(getResources());
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        cameraDrawer.onSurfaceCreated(gl,config);
        // 修改cameraId
        if (mRunnable != null){
            mRunnable.run();
            mRunnable = null;
        }

        // 打开相机
        camera.open(cameraId);
        cameraDrawer.setCameraId(cameraId);
        // 设置数据宽高
        Point pt = camera.getPreviewSize();
        cameraDrawer.setDataSize(pt.x, pt.y);
        // 给相机设置一个可供相机采集数据输入的实体
        camera.setPreviewTexture(cameraDrawer.getSurfaceTexture());
        // 设置采集到每帧后返回时 激活刷新
        cameraDrawer.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });
        // 开启预览
        camera.preview();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        cameraDrawer.onSurfaceChanged(gl,width,height);
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        cameraDrawer.onDrawFrame(gl);
    }

    @Override
    public void onPause() {
        super.onPause();
        camera.close();
    }

    public void switchCamera(){
        mRunnable = new Runnable() {
            @Override
            public void run() {
                camera.close();
                cameraId = cameraId == 1 ? 0 : 1;
            }
        };
        // 重新onSurfaceCreate
        onPause();
        onResume();
    }
}

//
// Created by lqk on 20-6-30.
//

#ifndef VIDEOLEARN_MYGLRENDERCONTEXT_H
#define VIDEOLEARN_MYGLRENDERCONTEXT_H


#include <GLSampleBase.h>

class MyGLRenderContext {
public:
    MyGLRenderContext();

    virtual ~MyGLRenderContext();

    static MyGLRenderContext* GetInstance();

    static void DestoryInstance();
public:

    void OnSurfaceCreated();

    void OnSurfaceChanged(int widht, int height);

    void OnDrawFrame();

private:
    static MyGLRenderContext*m_pContext;

    GLSampleBase*m_pSample;

    int m_ScreenW;
    int m_ScreenH;
};


#endif //VIDEOLEARN_MYGLRENDERCONTEXT_H

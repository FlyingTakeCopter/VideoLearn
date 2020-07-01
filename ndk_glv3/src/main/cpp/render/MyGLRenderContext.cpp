//
// Created by lqk on 20-6-30.
//

#include "MyGLRenderContext.h"

MyGLRenderContext::MyGLRenderContext() {}

MyGLRenderContext::~MyGLRenderContext() {

}

MyGLRenderContext *MyGLRenderContext::GetInstance()
{
    if (m_pContext == nullptr)
    {
        m_pContext = new MyGLRenderContext;
    }
    return m_pContext;
}

void MyGLRenderContext::DestoryInstance()
{
    if (m_pContext)
    {
        delete m_pContext;
        m_pContext = nullptr;
    }
}

void MyGLRenderContext::OnSurfaceCreated()
{

}

void MyGLRenderContext::OnSurfaceChanged(int widht, int height)
{

}

void MyGLRenderContext::OnDrawFrame()
{

}


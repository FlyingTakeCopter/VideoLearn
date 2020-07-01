//
// Created by lqk on 20-6-30.
//

#ifndef VIDEOLEARN_GLSAMPLEBASE_H
#define VIDEOLEARN_GLSAMPLEBASE_H

#include <GLES3/gl3.h>

class GLSampleBase
{
public:
    GLSampleBase()
    {
        m_VertexShader = 0;
        m_FragmentShader = 0;
        m_ProgramObj = 0;

        m_SurfaceHeight = 0;
        m_SurfaceWidth = 0;
    }

    virtual ~GLSampleBase() {

    }

    virtual void Init() = 0;

    virtual void Draw(int screenW, int screenH) = 0;

    virtual void Destory() = 0;
private:
    GLuint m_VertexShader;
    GLuint m_FragmentShader;
    GLuint m_ProgramObj;
    int m_SurfaceWidth;
    int m_SurfaceHeight;
};

#endif //VIDEOLEARN_GLSAMPLEBASE_H

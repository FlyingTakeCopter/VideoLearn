//
// Created by lqk on 20-7-1.
//

#ifndef VIDEOLEARN_TRIANGLESAMPLE_H
#define VIDEOLEARN_TRIANGLESAMPLE_H


#include "GLSampleBase.h"

class TriangleSample : public GLSampleBase{
public:
    TriangleSample();

    virtual ~TriangleSample();

    virtual void Init();

    virtual void Draw(int screenW, int screenH);

    virtual void Destory();
};


#endif //VIDEOLEARN_TRIANGLESAMPLE_H

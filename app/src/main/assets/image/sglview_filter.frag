precision mediump float;

uniform sampler2D uTexture;
uniform int uChangeType;
uniform vec3 uChangeColor;
uniform int uIsHalf;
uniform float uXY;      //屏幕宽高比

varying vec2 vCoords;
varying vec4 vPos;
varying vec4 vPosition;

void modifyColor(vec4 color){
    color.r=max(min(color.r,1.0),0.0);
    color.g=max(min(color.g,1.0),0.0);
    color.b=max(min(color.b,1.0),0.0);
    color.a=max(min(color.a,1.0),0.0);
}

void main(){
    vec4 nColor=texture2D(uTexture,vCoords);
    if(vPos.x<0.0||uIsHalf==0){
        if(uChangeType==1){    //黑白图片
            float c=nColor.r*uChangeColor.r+nColor.g*uChangeColor.g+nColor.b*uChangeColor.b;
            gl_FragColor=vec4(c,c,c,nColor.a);
        }else if(uChangeType==2){    //简单色彩处理，冷暖色调、增加亮度、降低亮度等
            vec4 deltaColor=nColor+vec4(uChangeColor,0.0);
            modifyColor(deltaColor);
            gl_FragColor=deltaColor;
        }else if(uChangeType==3){    //模糊处理
            nColor+=texture2D(uTexture,vec2(vCoords.x-uChangeColor.r,vCoords.y-uChangeColor.r));
            nColor+=texture2D(uTexture,vec2(vCoords.x-uChangeColor.r,vCoords.y+uChangeColor.r));
            nColor+=texture2D(uTexture,vec2(vCoords.x+uChangeColor.r,vCoords.y-uChangeColor.r));
            nColor+=texture2D(uTexture,vec2(vCoords.x+uChangeColor.r,vCoords.y+uChangeColor.r));
            nColor+=texture2D(uTexture,vec2(vCoords.x-uChangeColor.g,vCoords.y-uChangeColor.g));
            nColor+=texture2D(uTexture,vec2(vCoords.x-uChangeColor.g,vCoords.y+uChangeColor.g));
            nColor+=texture2D(uTexture,vec2(vCoords.x+uChangeColor.g,vCoords.y-uChangeColor.g));
            nColor+=texture2D(uTexture,vec2(vCoords.x+uChangeColor.g,vCoords.y+uChangeColor.g));
            nColor+=texture2D(uTexture,vec2(vCoords.x-uChangeColor.b,vCoords.y-uChangeColor.b));
            nColor+=texture2D(uTexture,vec2(vCoords.x-uChangeColor.b,vCoords.y+uChangeColor.b));
            nColor+=texture2D(uTexture,vec2(vCoords.x+uChangeColor.b,vCoords.y-uChangeColor.b));
            nColor+=texture2D(uTexture,vec2(vCoords.x+uChangeColor.b,vCoords.y+uChangeColor.b));
            //shader采样了周边12个点的颜色加上当前点的颜色，就是13个了，除以13得到均值
            nColor/=13.0;
            gl_FragColor=nColor;
        }else if(uChangeType==4){  //放大镜效果
            float dis=distance(vec2(vPosition.x,vPosition.y/uXY),vec2(uChangeColor.r,uChangeColor.g));
            if(dis<uChangeColor.b){
                nColor=texture2D(uTexture,vec2(vCoords.x/2.0+0.25,vCoords.y/2.0+0.25));
            }
            gl_FragColor=nColor;
        }else{
            gl_FragColor=nColor;
        }
    }else{
        gl_FragColor=nColor;
    }
}
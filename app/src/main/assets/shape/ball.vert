attribute vec4 aPosition;
uniform mat4 uMatrix;
varying vec4 vColor;

void main() {
    gl_Position = uMatrix * aPosition;

    float color;
    if(aPosition.z>0.0){
        color=aPosition.z;
    }else{
        color=-aPosition.z;
    }
    vColor=vec4(color,color,color,1.0);
}

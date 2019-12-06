attribute vec4 aPosition;
uniform mat4 uMatrix;
varying vec4 vColor;

void main() {
    gl_Position = uMatrix * aPosition;
    if(aPosition.z > 0.0){
        vColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);
    }else{
        vColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    }
}

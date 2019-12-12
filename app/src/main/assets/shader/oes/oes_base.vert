attribute vec4 aPosition;
attribute vec2 aCoords;
uniform mat4 uMatrix;
uniform mat4 uCoordMatrix;
varying vec2 textureCoordinate;

void main(){
    gl_Position = uMatrix*aPosition;
    textureCoordinate = (uCoordMatrix*vec4(aCoords,0,1)).xy;
}
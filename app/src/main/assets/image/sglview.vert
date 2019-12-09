attribute vec4 aPosition;
attribute vec2 aCoordinate;
uniform mat4 uMatrix;

varying vec2 vCoords;
varying vec4 vPos;
varying vec4 vPosition;

void main() {
    gl_Position = uMatrix * aPosition;
    vPos = aPosition;
    vCoords = aCoordinate;
    vPosition = uMatrix * aPosition;
}

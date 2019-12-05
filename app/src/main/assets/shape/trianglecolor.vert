attribute vec4 aPosition;
uniform mat4 uMatrix;
varying vec4 vColor;
attribute vec4 aColor;

void main() {
    gl_Position = uMatrix * aPosition;
    vColor = aColor;
}

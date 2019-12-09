attribute vec4 aPosition;
attribute vec2 aCoordinate;
uniform mat4 uMatrix;

varying vec2 vCoords;

void main() {
    gl_Position = uMatrix * aPosition;
    vCoords = aCoordinate;
}

attribute vec4 aPosition;
uniform mat4 uMatrix;

void main() {
    // 矩阵乘法顺序不能错，不满足乘法交换律
    gl_Position = uMatrix * aPosition;
}

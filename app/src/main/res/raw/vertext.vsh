attribute vec2  position;
attribute vec2  inputTextureCoordinate;
varying vec2    textureCoordinate;

void main() {
    textureCoordinate = inputTextureCoordinate;
    gl_Position = vec4 ( position.x, position.y, 0.0, 1.0 );
}
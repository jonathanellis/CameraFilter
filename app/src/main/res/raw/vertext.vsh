attribute vec2  vPosition; // position
attribute vec2  vTexCoord; // inputTextureCoordinate
varying vec2    texCoord; // textureCoordinate

void main() {
    texCoord = vTexCoord;
    gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );
}
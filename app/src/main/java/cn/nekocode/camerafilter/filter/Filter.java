package cn.nekocode.camerafilter.filter;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import cn.nekocode.camerafilter.MyGLUtils;

public class Filter extends Operation {

    private int program;

    private Filter(String vertexShader, String fragmentShader) {
        program = MyGLUtils.buildProgram(vertexShader, fragmentShader);
    }

    public static Filter create(String vertexShader, String fragmentShader) {
        return new Filter(vertexShader, fragmentShader);
    }

    public Filter setFloat(String name, float value) {
        int location = GLES20.glGetUniformLocation(program, name);
        GLES20.glUniform1f(location, value);
        return this;
    }

    public Filter setFloatVec2(String name, float[] value) {
        int location = GLES20.glGetUniformLocation(program, name);
        GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(value));
        return this;
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {
        setupShaderInputs(program,
                new int[]{canvasWidth, canvasHeight},
                new int[]{cameraTexId},
                new int[][]{});
    }

}

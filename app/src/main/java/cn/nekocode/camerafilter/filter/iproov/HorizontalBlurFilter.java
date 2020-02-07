package cn.nekocode.camerafilter.filter.iproov;

import android.content.Context;
import android.opengl.GLES20;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.R;
import cn.nekocode.camerafilter.filter.CameraFilter;

public class HorizontalBlurFilter extends CameraFilter {

    private int program;

    public HorizontalBlurFilter(Context context) {
        super(context);

        // Build shaders
        program = MyGLUtils.buildProgram(Shaders.NO_FILTER_VERTEX_SHADER, Shaders.HorizontalBlurShader);
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {
        setupShaderInputs(program,
                new int[]{canvasWidth, canvasHeight},
                new int[]{cameraTexId},
                new int[][]{});

        int location = GLES20.glGetUniformLocation(program, "h");
        GLES20.glUniform1f(location, 0.005f);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}

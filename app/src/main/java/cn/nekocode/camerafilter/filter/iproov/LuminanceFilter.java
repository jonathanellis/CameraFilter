package cn.nekocode.camerafilter.filter.iproov;

import android.content.Context;
import android.opengl.GLES20;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.filter.CameraFilter;

public class LuminanceFilter extends CameraFilter {

    private int program;

    public LuminanceFilter(Context context) {
        super(context);

        // Build shaders
        program = MyGLUtils.buildProgram(Shaders.NO_FILTER_VERTEX_SHADER, Shaders.LuminanceShader);
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {
        setupShaderInputs(program,
                new int[]{canvasWidth, canvasHeight},
                new int[]{cameraTexId},
                new int[][]{});

    }
}

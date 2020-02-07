package cn.nekocode.camerafilter.filter.iproov;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.R;
import cn.nekocode.camerafilter.filter.CameraFilter;

public class TransformationFilter extends CameraFilter {

    private int program;
    private static final float[] MATRIX = getRotationMatrix(80); // {-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f}; // getRotationMatrix(90);


    private static float[] getRotationMatrix(int angle) {

        double t = Math.toRadians(angle);
        float sinT = (float) Math.sin(t);
        float cosT = (float) Math.cos(t);

        return new float[] {
                1,      0,      0,      0,
                0,      cosT,   -sinT,  0,
                0,      sinT,   cosT,   0,
                0,      0,      0,      1
        };

    }

    private static float[] getRotationMatrix2(int angle) {

        double t = Math.toRadians(angle);
        float sinT = (float) Math.sin(t);
        float cosT = (float) Math.cos(t);

        return new float[] {
                cosT,   0,      sinT,   0,
                0,      1,      0,      0,
                -sinT,  0,      cosT,   0,
                0,      0,      0,      1
        };

    }

    public TransformationFilter(Context context) {
        super(context);

        // Build shaders
        program = MyGLUtils.buildProgram(Shaders.TransformationShader, Shaders.NO_FILTER_FRAGMENT_SHADER);
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {
        setupShaderInputs(program,
                new int[]{canvasWidth, canvasHeight},
                new int[]{cameraTexId},
                new int[][]{});

        int location = GLES20.glGetUniformLocation(program, "transformMatrix");
        GLES20.glUniformMatrix4fv(location, 1, false, MATRIX, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}

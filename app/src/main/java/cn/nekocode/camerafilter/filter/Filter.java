package cn.nekocode.camerafilter.filter;

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.nekocode.camerafilter.MyGLUtils;

public class Filter extends Operation {

    private int program;
    private final ConcurrentLinkedQueue<Runnable> runOnDraw = new ConcurrentLinkedQueue<>();

    private Filter(String vertexShader, String fragmentShader) {
        program = MyGLUtils.buildProgram(vertexShader, fragmentShader);
    }

    public static Filter create(String vertexShader, String fragmentShader) {
        return new Filter(vertexShader, fragmentShader);
    }

    private void runOnDraw(final Runnable runnable) {
        //Changed LinkedList to ConcurrentLinkedQueue
        runOnDraw.add(runnable);

    }

    public Filter setFloat(final String name, final float value) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                int location = GLES20.glGetUniformLocation(program, name);
                GLES20.glUniform1f(location, value);
            }
        });

        return this;
    }

    public Filter setFloatVec2(final String name, final float[] value) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                int location = GLES20.glGetUniformLocation(program, name);
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(value));
            }
        });
        return this;
    }

    protected void runPendingOnDrawTasks() {
        //Changed LinkedList to ConcurrentLinkedQueue
        while (!runOnDraw.isEmpty()) {
            runOnDraw.poll().run();
        }
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {
        setupShaderInputs(program,
                new int[]{canvasWidth, canvasHeight},
                new int[]{cameraTexId},
                new int[][]{});

        runPendingOnDrawTasks();
    }

}

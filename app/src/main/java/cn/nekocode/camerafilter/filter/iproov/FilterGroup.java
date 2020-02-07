package cn.nekocode.camerafilter.filter.iproov;

import android.content.Context;
import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.Arrays;

import cn.nekocode.camerafilter.RenderBuffer;
import cn.nekocode.camerafilter.filter.CameraFilter;

public class FilterGroup extends CameraFilter {

    ArrayList<CameraFilter> filters = new ArrayList<>();

    private RenderBuffer bufA;
    private RenderBuffer bufB;

    public FilterGroup(Context context, CameraFilter... filters) {
        super(context);

        this.filters.addAll(Arrays.asList(filters));
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {

        if (bufA == null || bufA.getWidth() != canvasWidth || bufB.getHeight() != canvasHeight) {
            // Create new textures for buffering
            bufA = new RenderBuffer(canvasWidth, canvasHeight, GLES20.GL_TEXTURE4);
            bufB = new RenderBuffer(canvasWidth, canvasHeight, GLES20.GL_TEXTURE5);
        }

        for (int i = 0; i < filters.size(); i++) {
            CameraFilter filter = filters.get(i);

            int inputTexId;
            RenderBuffer outBuf;

            if (i == 0) {
                inputTexId = cameraTexId;
                outBuf = bufA;
            } else if (i % 2 == 1) {
                inputTexId = bufA.getTexId();
                outBuf = bufB;
            } else {
                inputTexId = bufB.getTexId();
                outBuf = bufA;
            }

            outBuf.bind();
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            filter.onDraw(inputTexId, canvasWidth, canvasHeight);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            outBuf.unbind();

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        }

    }

}

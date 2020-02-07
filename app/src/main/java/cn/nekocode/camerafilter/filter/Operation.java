/*
 * Copyright 2016 nekocode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.nekocode.camerafilter.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.CallSuper;
import android.widget.ProgressBar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.RenderBuffer;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public abstract class Operation {

    static final float SQUARE_COORDS[] = {
            1.0f, -1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f,
    };
    static final float TEXTURE_COORDS[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };
    static FloatBuffer VERTEX_BUF, TEXTURE_COORD_BUF;
    static int BASE_PROGRAM = 0;
    private int program;


    private static final int BUF_ACTIVE_TEX_UNIT = GLES20.GL_TEXTURE8;
    private static RenderBuffer CAMERA_RENDER_BUF;

    private static final float ROATED_TEXTURE_COORDS[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };
    private static FloatBuffer ROATED_TEXTURE_COORD_BUF;

    final long START_TIME = System.currentTimeMillis();
    int iFrame = 0;

    public Operation() {
        // Setup default Buffers
        if (VERTEX_BUF == null) {
            VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            VERTEX_BUF.put(SQUARE_COORDS);
            VERTEX_BUF.position(0);
        }

        if (TEXTURE_COORD_BUF == null) {
            TEXTURE_COORD_BUF = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            TEXTURE_COORD_BUF.put(TEXTURE_COORDS);
            TEXTURE_COORD_BUF.position(0);
        }

        if (ROATED_TEXTURE_COORD_BUF == null) {
            ROATED_TEXTURE_COORD_BUF = ByteBuffer.allocateDirect(ROATED_TEXTURE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ROATED_TEXTURE_COORD_BUF.put(ROATED_TEXTURE_COORDS);
            ROATED_TEXTURE_COORD_BUF.position(0);
        }

        if (BASE_PROGRAM == 0) {
            BASE_PROGRAM = MyGLUtils.buildProgram(Shaders.DEFAULT_VERTEX_SHADER, Shaders.DEFAULT_FRAG_SHADER);
        }

    }

    @CallSuper
    public void onAttach() {
        iFrame = 0;
    }

    final public void draw(int textureId, int canvasWidth, int canvasHeight) {
        // TODO move?
        // Create camera render buffer
        if (CAMERA_RENDER_BUF == null ||
                CAMERA_RENDER_BUF.getWidth() != canvasWidth ||
                CAMERA_RENDER_BUF.getHeight() != canvasHeight) {
            CAMERA_RENDER_BUF = new RenderBuffer(canvasWidth, canvasHeight, BUF_ACTIVE_TEX_UNIT);
        }

        // Use shaders
        GLES20.glUseProgram(BASE_PROGRAM);

        int iChannel0Location = GLES20.glGetUniformLocation(BASE_PROGRAM, "inputImageTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(iChannel0Location, 0);

        int vPositionLocation = GLES20.glGetAttribLocation(BASE_PROGRAM, "position");
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);

        int vTexCoordLocation = GLES20.glGetAttribLocation(BASE_PROGRAM, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(vTexCoordLocation);
        GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, ROATED_TEXTURE_COORD_BUF);

        // Render to texture
        CAMERA_RENDER_BUF.bind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        CAMERA_RENDER_BUF.unbind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        onDraw(CAMERA_RENDER_BUF.getTexId(), canvasWidth, canvasHeight); // <<----- CALL DOWN TO SUBCLASS

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        iFrame++;
    }

    public abstract void onDraw(int cameraTexId, int canvasWidth, int canvasHeight);

    protected static void setupShaderInputs(int program, int[] iResolution, int[] iChannels, int[][] iChannelResolutions) {
        setupShaderInputs(program, VERTEX_BUF, TEXTURE_COORD_BUF, iResolution, iChannels, iChannelResolutions);
    }

    static void setupShaderInputs(int program, FloatBuffer vertex, FloatBuffer textureCoord, int[] iResolution, int[] iChannels, int[][] iChannelResolutions) {
        GLES20.glUseProgram(program);

//        GLES20.glUniform3fv(iResolutionLocation, 1,
//                FloatBuffer.wrap(new float[]{(float) iResolution[0], (float) iResolution[1], 1.0f}));
//
//        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
//        int iGlobalTimeLocation = GLES20.glGetUniformLocation(program, "iGlobalTime");
//        GLES20.glUniform1f(iGlobalTimeLocation, time);
//
//        int iFrameLocation = GLES20.glGetUniformLocation(program, "iFrame");
//        GLES20.glUniform1i(iFrameLocation, iFrame);

        int vPositionLocation = GLES20.glGetAttribLocation(program, "position");
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, vertex);

        int vTexCoordLocation = GLES20.glGetAttribLocation(program, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(vTexCoordLocation);
        GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, textureCoord);

        for (int i = 0; i < iChannels.length; i++) {
            String textureName = (i == 0) ? "inputImageTexture" : "inputImageTexture" + (i+1);
            int sTextureLocation = GLES20.glGetUniformLocation(program, textureName);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, iChannels[i]);
            GLES20.glUniform1i(sTextureLocation, i);
        }

//        float _iChannelResolutions[] = new float[iChannelResolutions.length * 3];
//        for (int i = 0; i < iChannelResolutions.length; i++) {
//            _iChannelResolutions[i * 3] = iChannelResolutions[i][0];
//            _iChannelResolutions[i * 3 + 1] = iChannelResolutions[i][1];
//            _iChannelResolutions[i * 3 + 2] = 1.0f;
//        }
//
//        int iChannelResolutionLocation = GLES20.glGetUniformLocation(program, "iChannelResolution");
//        GLES20.glUniform3fv(iChannelResolutionLocation,
//                _iChannelResolutions.length, FloatBuffer.wrap(_iChannelResolutions));
    }

    public static void release() {
        BASE_PROGRAM = 0;
        CAMERA_RENDER_BUF = null;
    }

    public static RenderBuffer getCameraRenderBuf() {
        return CAMERA_RENDER_BUF;
    }

}

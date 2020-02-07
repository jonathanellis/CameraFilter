package cn.nekocode.camerafilter.filter;

import cn.nekocode.camerafilter.RenderBuffer;

public class TwoInputFilter extends Filter {

    RenderBuffer input2;

    public TwoInputFilter(String vertexShader, String fragmentShader, RenderBuffer input2) {
        super(vertexShader, fragmentShader);

        this.input2 = input2;
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {
        setupShaderInputs(program,
                new int[]{canvasWidth, canvasHeight},
                new int[]{cameraTexId, input2.getTexId()},
                new int[][]{});
    }
}

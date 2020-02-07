package cn.nekocode.camerafilter.filter;

public class Shaders {
    private Shaders(){}
    public static final String FlashingShader = "precision highp float;\nvarying highp vec2 textureCoordinate;\n\nuniform sampler2D inputImageTexture;\n\nuniform lowp vec3 nextRGB;\nuniform lowp vec3 lineRGB;\n\nvoid main()\n{\n    \n     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n    \n    gl_FragColor = vec4((lineRGB * textureColor.rgb * max(sign(1.0 - textureCoordinate.y), 0.0)) //paint the lines with RGB, except for the bottom 25%\n                        + (nextRGB * (1.0 - textureColor.rgb) * max(sign(1.0 - textureCoordinate.y), 0.0)) // paint the background with RGB, except for the bottom 25%\n                        + (nextRGB * max(sign(textureCoordinate.y - 1.0), 0.0)) //paint the bottom 25% with background RGB\n                        , 1.0);\n}\n";
    public static final String InclusionShader = "precision highp float;\n//#gljs varname: 'iProov.webgl.shader.inclusion.fragment', type: 'fragment'\n\nuniform sampler2D inputImageTexture;\nuniform vec2 uWindow;\nuniform float threshold;\n\nvarying vec2 textureCoordinate;\n\nvoid main() {\n\n    vec2 offset = threshold / uWindow;\n\n    float bottomLeftIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(-offset.x, offset.y)).r;\n    float topRightIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(offset.x, -offset.y)).r;\n    float topLeftIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(-offset.x, -offset.y)).r;\n    float bottomRightIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(offset.x, offset.y)).r;\n    float leftIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(-offset.x, 0.0)).r;\n    float rightIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(offset.x, 0.0)).r;\n    float bottomIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(0.0, offset.y)).r;\n    float topIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(0.0, -offset.y)).r;\n    float centerIntensity = texture2D(inputImageTexture, textureCoordinate).r;\n\n    float pixelIntensitySum = bottomLeftIntensity + topRightIntensity + topLeftIntensity + bottomRightIntensity + leftIntensity + rightIntensity + bottomIntensity + topIntensity + centerIntensity;\n    float sumTest = step(1.5, pixelIntensitySum);\n    float pixelTest = step(0.01, centerIntensity);\n\n    gl_FragColor = vec4(vec3(sumTest * pixelTest, sumTest * pixelTest, sumTest * pixelTest), 1.0);\n\n}\n";
    public static final String LuminanceShader = "precision highp float;\n//#gljs varname: 'iProov.webgl.shader.luminance.fragment', type: 'fragment'\n\nconst vec3 W = vec3(0.2125, 0.7154, 0.0721);\n\nuniform sampler2D inputImageTexture;\n\nvarying vec2 textureCoordinate;\n\nvoid main() {\n\n    vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n    float luminance = dot(textureColor.rgb, W);\n\n    gl_FragColor = vec4(vec3(luminance), textureColor.a);\n\n}\n";
    public static final String SobelShader = "precision highp float;\n//#gljs varname: 'iProov.webgl.shader.sobel.fragment', type: 'fragment'\n\nuniform sampler2D inputImageTexture;\nuniform vec2 uWindow;\nuniform float threshold;\n\nvarying vec2 textureCoordinate;\n\nvoid main() {\n\n    vec2 offset = threshold / uWindow;\n\n    float bottomLeftIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(-offset.x, offset.y)).r;\n    float topRightIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(offset.x, -offset.y)).r;\n    float topLeftIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(-offset.x, -offset.y)).r;\n    float bottomRightIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(offset.x, offset.y)).r;\n    float leftIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(-offset.x, 0.0)).r;\n    float rightIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(offset.x, 0.0)).r;\n    float bottomIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(0.0, offset.y)).r;\n    float topIntensity = texture2D(inputImageTexture, textureCoordinate + vec2(0.0, -offset.y)).r;\n\n    vec2 gradientDirection;\n    gradientDirection.x = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;\n    gradientDirection.y = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;\n\n    float gradientMagnitude = length(gradientDirection);\n    vec2 normalizedDirection = normalize(gradientDirection);\n\n    normalizedDirection = sign(normalizedDirection) * floor(abs(normalizedDirection) + 0.617316); // Offset by 1-sin(pi/8) to set to 0 if near axis, 1 if away\n    normalizedDirection = (normalizedDirection + 1.0) * 0.5; // Place -1.0 - 1.0 within 0 - 1.0\n\n    gl_FragColor = vec4(gradientMagnitude, normalizedDirection.x, normalizedDirection.y, 1.0);\n    //gl_FragColor = vec4(gradientMagnitude, gradientMagnitude, gradientMagnitude, 1.0); // Normal edge detection version\n}\n";
    public static final String SuppressionShader = "precision highp float;\n//#gljs varname: 'iProov.webgl.shader.suppression.fragment', type: 'fragment'\n\nuniform sampler2D inputImageTexture;\nuniform highp float texelWidth;\nuniform highp float texelHeight;\nuniform mediump float upperThreshold;\nuniform mediump float lowerThreshold;\n\nvarying highp vec2 textureCoordinate;\n\nvoid main() {\n\n    vec3 currentGradientAndDirection = texture2D(inputImageTexture, textureCoordinate).rgb;\n    vec2 gradientDirection = ((currentGradientAndDirection.gb * 2.0) - 1.0) * vec2(texelWidth, texelHeight);\n\n    float firstSampledGradientMagnitude = texture2D(inputImageTexture, textureCoordinate + gradientDirection).r;\n    float secondSampledGradientMagnitude = texture2D(inputImageTexture, textureCoordinate - gradientDirection).r;\n\n    float multiplier = step(firstSampledGradientMagnitude, currentGradientAndDirection.r);\n    multiplier = multiplier * step(secondSampledGradientMagnitude, currentGradientAndDirection.r);\n\n    float thresholdCompliance = smoothstep(lowerThreshold, upperThreshold, currentGradientAndDirection.r);\n    multiplier = multiplier * thresholdCompliance;\n\n    gl_FragColor = vec4(multiplier, multiplier, multiplier, 1.0);\n\n}\n";
    public static final String VerticalBlurShader = "precision highp float;\n//#gljs varname: 'iProov.webgl.shader.blur.fragment', type: 'fragment'\n\nuniform sampler2D inputImageTexture;\nuniform float v;\n\nvarying vec2 textureCoordinate;\n\nvoid main() {\n    \n    vec4 sum = vec4( 0.0 );\n    \n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y - 4.0 * v ) ) * 0.0276305489;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y - 3.0 * v ) ) * 0.0662822425;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y - 2.0 * v ) ) * 0.123831533;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y - 1.0 * v ) ) * 0.180173814;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y ) ) * 0.204163685;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y + 1.0 * v ) ) * 0.180173814;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y + 2.0 * v ) ) * 0.123831533;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y + 3.0 * v ) ) * 0.0662822425;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y + 4.0 * v ) ) * 0.0276305489;\n    \n    gl_FragColor = sum;\n    \n}\n";
    public static final String HorizontalBlurShader = "precision highp float;\n//#gljs varname: 'iProov.webgl.shader.blur.fragment', type: 'fragment'\n\nuniform sampler2D inputImageTexture;\nuniform float h;\n\nvarying vec2 textureCoordinate;\n\nvoid main() {\n    \n    vec4 sum = vec4( 0.0 );\n    \n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x - 4.0 * h, textureCoordinate.y ) ) * 0.0276305489;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x - 3.0 * h, textureCoordinate.y ) ) * 0.0662822425;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x - 2.0 * h, textureCoordinate.y ) ) * 0.123831533;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x - 1.0 * h, textureCoordinate.y ) ) * 0.180173814;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x, textureCoordinate.y ) ) * 0.204163685;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x + 1.0 * h, textureCoordinate.y ) ) * 0.180173814;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x + 2.0 * h, textureCoordinate.y ) ) * 0.123831533;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x + 3.0 * h, textureCoordinate.y ) ) * 0.0662822425;\n    sum += texture2D( inputImageTexture, vec2( textureCoordinate.x + 4.0 * h, textureCoordinate.y ) ) * 0.0276305489;\n    \n    gl_FragColor = sum;\n    \n}\n";
    public static final String ScannerShader = "precision highp float;\n#define M_PI 3.1415926535897932384626433832795\n\nvarying vec2 textureCoordinate;\nuniform sampler2D inputImageTexture;\nuniform float iTime;                 // shader playback time (in seconds)\nuniform float speed;\n\nvoid main() {\n    float y = 0. + (iTime / 3. * speed);\n    float colorAdd = step(0.995, 1. - abs(textureCoordinate.y - y));\n    gl_FragColor = vec4(texture2D(inputImageTexture, textureCoordinate).rgb + colorAdd, 1.);\n}\n";
    public static final String ShadeShader = "precision highp float;\nuniform sampler2D inputImageTexture;\nuniform sampler2D inputImageTexture2;\nuniform vec4 blend;\nuniform vec4 fade;\n\nvarying vec2 textureCoordinate;\n\nvoid main(void) {\n\n    float step1 = 0.8;\n    float step2 = 0.6;\n    float step3 = 0.3;\n    float step4 = 0.15;\n    float alpha = 1.0;\n\n    vec3 shade = vec3(1.0);\n    vec4 color = vec4(1.0);\n\n    vec4 texelLines = texture2D(inputImageTexture, textureCoordinate);\n    vec4 texelShade = texture2D(inputImageTexture2, textureCoordinate);\n\n    float brightness = (0.2126 * texelShade.r) + (0.7152 * texelShade.g) + (0.0722 * texelShade.b);\n    float brightest = max(max(texelShade.r, texelShade.g), texelShade.b);\n    float dimmest = min(min(texelShade.r, texelShade.g), texelShade.b);\n    float delta = brightest - dimmest;\n\n    if (delta > 0.1) {\n        texelShade = texelShade * (1.0 / brightest);\n    } else {\n        texelShade.rgb = vec3(1.0);\n    }\n\n    if (brightness < step1) {\n        shade = vec3(texelShade.rgb * step1);\n    }\n\n    if (brightness < step2) {\n        shade = vec3(texelShade.rgb * step2);\n    }\n\n    if (brightness < step3) {\n        shade = vec3(texelShade.rgb * step3);\n    }\n\n    if (brightness < step4) {\n        shade = vec3(texelShade.rgb * step4);\n    }\n\n    if (texelLines.rgb == vec3(0.0)) {\n        color = 1.0 - vec4(mix(shade, blend.rgb, blend.a), 0.0);\n    }\n\n    if (fade.x > 0.0) {\n        alpha *= smoothstep(0.0, fade.x * abs(sin(0.5)), vec4(textureCoordinate, 1.0 - textureCoordinate)).x;\n    }\n\n    if (fade.y > 0.0) {\n        alpha *= smoothstep(0.0, fade.y * abs(sin(0.5)), vec4(textureCoordinate, 1.0 - textureCoordinate)).y;\n    }\n\n    if (fade.z > 0.0) {\n        alpha *= smoothstep(0.0, fade.z * abs(sin(0.5)), vec4(textureCoordinate, 1.0 - textureCoordinate)).z;\n    }\n\n    if (fade.w > 0.0) {\n        alpha *= smoothstep(0.0, fade.w * abs(sin(0.5)), vec4(textureCoordinate, 1.0 - textureCoordinate)).w;\n    }\n\n    if (fade != vec4(0.0)) {\n        color = vec4(mix(vec3(0.0), color.rgb, alpha), 1.0);\n    }\n\n    gl_FragColor = color;\n\n}\n";
    public static final String TransformationShader ="attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n\n" +
            "uniform mat4 transformMatrix;\n\nvarying vec2 textureCoordinate;\n\nvoid main()\n{\n gl_Position = transformMatrix * vec4(position.xyz, 1.0);\n textureCoordinate = inputTextureCoordinate.xy;\n}";


    public static final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    public static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    public static String DEFAULT_VERTEX_SHADER =
            "attribute vec2  position;\n" +
            "attribute vec2  inputTextureCoordinate;\n" +
            "varying vec2    textureCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    textureCoordinate = inputTextureCoordinate;\n" +
            "    gl_Position = vec4 ( position.x, position.y, 0.0, 1.0 );\n" +
            "}";

    public static String DEFAULT_FRAG_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "\n" +
            "varying vec2                textureCoordinate;\n" +
            "uniform samplerExternalOES  inputImageTexture;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    public static final String THREE_X_THREE_TEXTURE_SAMPLING_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "uniform highp float texelWidth; \n" +
            "uniform highp float texelHeight; \n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 leftTextureCoordinate;\n" +
            "varying vec2 rightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 topTextureCoordinate;\n" +
            "varying vec2 topLeftTextureCoordinate;\n" +
            "varying vec2 topRightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 bottomTextureCoordinate;\n" +
            "varying vec2 bottomLeftTextureCoordinate;\n" +
            "varying vec2 bottomRightTextureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "\n" +
            "    vec2 widthStep = vec2(texelWidth, 0.0);\n" +
            "    vec2 heightStep = vec2(0.0, texelHeight);\n" +
            "    vec2 widthHeightStep = vec2(texelWidth, texelHeight);\n" +
            "    vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);\n" +
            "\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "    leftTextureCoordinate = inputTextureCoordinate.xy - widthStep;\n" +
            "    rightTextureCoordinate = inputTextureCoordinate.xy + widthStep;\n" +
            "\n" +
            "    topTextureCoordinate = inputTextureCoordinate.xy - heightStep;\n" +
            "    topLeftTextureCoordinate = inputTextureCoordinate.xy - widthHeightStep;\n" +
            "    topRightTextureCoordinate = inputTextureCoordinate.xy + widthNegativeHeightStep;\n" +
            "\n" +
            "    bottomTextureCoordinate = inputTextureCoordinate.xy + heightStep;\n" +
            "    bottomLeftTextureCoordinate = inputTextureCoordinate.xy - widthNegativeHeightStep;\n" +
            "    bottomRightTextureCoordinate = inputTextureCoordinate.xy + widthHeightStep;\n" +
            "}";

}

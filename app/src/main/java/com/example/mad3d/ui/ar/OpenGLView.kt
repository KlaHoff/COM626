package com.example.mad3d.ui.ar

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import freemap.openglwrapper.GLMatrix

class OpenGLView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    var onTextureAvailableCallback: (SurfaceTexture) -> Unit = {}
) : GLSurfaceView(ctx, attrs), GLSurfaceView.Renderer {

    // this will handle our custom textures for AR
    private var textureInterface: ARgpuInterface? = null
    // buffer to store vertex data
    private var vbuf: FloatBuffer? = null
    // this is the surface where the camera feed will be shown
    private var cameraFeedSurfaceTexture: SurfaceTexture? = null
    // buffer to store indices for drawing shapes
    private var ibuf: ShortBuffer? = null

    // matrix to handle orientation changes
    var orientationMatrix = GLMatrix()

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // set clear color and depth
        GLES20.glClearColor(0.0f, 0.0f, 0.3f, 0.0f)
        GLES20.glClearDepthf(1.0f)
        // create the shapes to be drawn
        createShapes()
        val GL_TEXTURE_EXTERNAL_OES = 0x8d65
        val textureId = IntArray(1)
        // generate a texture ID
        GLES20.glGenTextures(1, textureId, 0)
        if (textureId[0] != 0) {
            // bind the texture and set parameters
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId[0])
            GLES20.glTexParameteri(
                GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )
            // create the surface texture for the camera feed
            cameraFeedSurfaceTexture = SurfaceTexture(textureId[0])
            cameraFeedSurfaceTexture?.setOnFrameAvailableListener {
                // request a new frame to be drawn when available
                requestRender()
            }
            // vertex and fragment shaders for the texture
            val texVertexShader =
                "attribute vec4 aVertex;\n" +
                        "varying vec2 vTextureValue;\n" +
                        "void main (void)\n" +
                        "{\n" +
                        "gl_Position = aVertex;\n" +
                        "vTextureValue = vec2(0.5*(1.0 + aVertex.x), 0.5*(1.0-aVertex.y));\n" +
                        "}\n"
            val texFragmentShader =
                "#extension GL_OES_EGL_image_external: require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureValue;\n" +
                        "uniform samplerExternalOES uTexture;\n" +
                        "void main(void)\n" +
                        "{\n" +
                        "gl_FragColor = texture2D(uTexture,vTextureValue);\n" +
                        "}\n"
            // set up the interface to handle the textures
            textureInterface = ARgpuInterface(texVertexShader, texFragmentShader)
            setupTexture(textureId[0])
            textureInterface?.setUniform1i("uTexture", 0)
            Log.d("CAMERAXGL", "setting up texture available callback")
            // notify that the texture is ready
            onTextureAvailableCallback(cameraFeedSurfaceTexture!!)
        }
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        // update the texture with the latest camera image
        cameraFeedSurfaceTexture?.updateTexImage()
        // use the texture interface to draw
        textureInterface?.select()
        if (vbuf != null && ibuf != null) {
            // draw the shape using the vertex and index buffers
            textureInterface?.drawIndexedBufferedData(vbuf!!, ibuf!!, 0, "aVertex")
        }
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // handle orientation changes
        val viewMatrix = orientationMatrix.clone()
        viewMatrix.correctSensorMatrix()
    }

    override fun onSurfaceChanged(unused: GL10, w: Int, h: Int) {
        // adjust the viewport when the surface size changes
        GLES20.glViewport(0, 0, w, h)
    }

    private fun createShapes() {
        // vertices for a rectangle to display the camera feed
        val cameraRect = floatArrayOf(-1f, 1f, 0f, -1f, -1f, 0f, 1f, -1f, 0f, 1f, 1f, 0f)
        // indices for the rectangle
        val indices = shortArrayOf(0, 1, 2, 2, 3, 0)
        // create a buffer for vertex data
        val vbuf0 = ByteBuffer.allocateDirect(cameraRect.size * Float.SIZE_BYTES)
        vbuf0.order(ByteOrder.nativeOrder())
        vbuf = vbuf0.asFloatBuffer()
        vbuf?.apply {
            put(cameraRect)
            position(0)
        }
        // create a buffer for index data
        val ibuf0 = ByteBuffer.allocateDirect(indices.size * Short.SIZE_BYTES)
        ibuf0.order(ByteOrder.nativeOrder())
        ibuf = ibuf0.asShortBuffer()
        ibuf?.apply {
            put(indices)
            position(0)
        }
    }
}

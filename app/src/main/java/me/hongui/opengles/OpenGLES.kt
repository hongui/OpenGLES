package me.hongui.opengles

import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OpenGLES private constructor(private val program:Int){
    private val buffer=ByteBuffer.allocateDirect(POS.size*4).apply {
        order(ByteOrder.nativeOrder())
            .asFloatBuffer().put(POS)
    }

    fun config(width: Int,height: Int){
        GLES30.glViewport(0, 0, width, height)
        GLES20.glClearColor(1f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    fun draw(){
        GLES30.glVertexAttribPointer(0,3,GLES30.GL_FLOAT,false,0,buffer)
        GLES30.glEnableVertexAttribArray(0)

        //画三角形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,3)

        //画线
        //GLES30.glDrawArrays(GLES30.GL_LINE_LOOP,0,3)

        //画点
        //GLES30.glDrawArrays(GLES30.GL_POINTS,0,3)
    }

    fun use(action:OpenGLES.(Int)->Unit){
        GLES30.glUseProgram(program)
        action(program)
        GLES30.glUseProgram(GLES30.GL_NONE)
    }

    companion object {
        @JvmStatic
        private val TAG = "OpenGLES"
        @JvmStatic
        private val VERTEX = 1
        @JvmStatic
        private val FRAGMENT = 2
        @JvmStatic
        private val POS = floatArrayOf(
            -0.5f, -0.5f, 1.0f,
            0.0f, 0.5f, 1.0f,
            0.5f, -0.5f, 1.0f
        )
        @JvmStatic
        private val VERTEX_CODE = """
            #version 300 es
            layout (location = 0) in vec3 aPos;
            void main(){
                gl_Position=vec4(aPos,1.0);
            }
        """.trimIndent()

        @JvmStatic
        private val FRAGMENT_CODE = """
            #version 300 es
            out vec4 fragColor;
            void main(){
                fragColor=vec4(0.0,1.0,0.0,1.0);
            }
        """.trimIndent()

        fun makeOpenGLES(width: Int, height: Int): OpenGLES? {
            val program = GLES30.glCreateProgram()
            if (GLES30.GL_NONE == program) {
                return null
            }
            val vertexShader = createShader(VERTEX, VERTEX_CODE) ?: return null
            val fragmentShader = createShader(FRAGMENT, FRAGMENT_CODE) ?: return null
            val openGLES = OpenGLES(program)
            openGLES.use {
                GLES30.glAttachShader(it,vertexShader)
                GLES30.glAttachShader(it, fragmentShader)
                GLES30.glLinkProgram(it)
                config(width, height)
            }
            return openGLES
        }

        private fun createShader(type: Int, source: String): Int? {
            val shader = GLES30.glCreateShader(
                when (type) {
                    VERTEX -> GLES30.GL_VERTEX_SHADER
                    FRAGMENT -> GLES30.GL_FRAGMENT_SHADER
                    else -> return null
                }
            )
            if (GLES30.GL_NONE == shader) {
                log()
                return null
            }
            GLES30.glShaderSource(shader, source)
            GLES30.glCompileShader(shader)
            if (GLES30.GL_NO_ERROR != GLES30.glGetError()) {
                log()
                return null
            }
            return shader
        }

        private fun log() {
            Log.e(TAG, "OpenGLES error code = ${GLES30.glGetError().toString(16)}")
        }
    }
}
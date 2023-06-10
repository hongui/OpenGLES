package me.hongui.opengles

import android.opengl.EGL14
import android.opengl.EGL15
import android.opengl.EGLConfig
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import me.hongui.opengles.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val thread = HandlerThread("Render")
    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.svTest.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                handler.post {
                    render(holder.surface,width,height)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

        })
    }

    override fun onResume() {
        super.onResume()
        thread.start()
        handler = Handler(thread.looper)
    }

    override fun onPause() {
        super.onPause()
        thread.quit()
    }

    fun render(surface: Surface,width:Int,height:Int) {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (EGL14.EGL_NO_DISPLAY == display) {
            log()
            return
        }
        val versions = IntArray(2)
        var flag = EGL14.eglInitialize(display, versions, 0, versions, 1)
        if (!flag) {
            log()
            return
        }
        Log.i(TAG, "EGL version:major = ${versions[0]}, minor = ${versions[1]}")

        val attr = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_NONE
        )
        val configs=Array<EGLConfig?>(1,{null})
        val numConfig=IntArray(1)
        flag = EGL14.eglChooseConfig(display, attr, 0, configs, 0, 1, numConfig, 0)
        if (!flag) {
            log()
            return
        }
        val config=configs.first()
        val eglSurface=EGL14.eglCreateWindowSurface(display,config,surface, intArrayOf(EGL14.EGL_NONE),0)
        if (EGL14.EGL_NO_SURFACE == eglSurface) {
            log()
            return
        }
        val context=EGL14.eglCreateContext(display,config,EGL14.EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_NONE),0)
        if (EGL14.EGL_NO_CONTEXT == context) {
            log()
            return
        }
        flag = EGL14.eglMakeCurrent(display, eglSurface, eglSurface, context)
        if (!flag) {
            log()
            return
        }
        GLES20.glViewport(0,0,width,height)
        GLES20.glClearColor(1f,0f,0f,1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        EGL14.eglSwapBuffers(display,eglSurface)
    }

    private fun log() {
        Log.e(TAG, "code = ${EGL14.eglGetError()}")
    }

    companion object {
        @JvmStatic
        val TAG = "MainActivity"
    }
}
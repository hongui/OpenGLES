package me.hongui.opengles

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.util.Log
import android.view.Surface

class Egl private constructor(
    val context: EGLContext,
    val display: EGLDisplay,
    val surface: EGLSurface
) {

    fun swapBuffer(){
        EGL14.eglSwapBuffers(display,surface)
    }
    companion object {
        fun makeEGL(surface: Surface): Egl? {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (EGL14.EGL_NO_DISPLAY == display) {
                log()
                return null
            }
            val versions = IntArray(2)
            var flag = EGL14.eglInitialize(display, versions, 0, versions, 1)
            if (!flag) {
                log()
                return null
            }
            Log.i(MainActivity.TAG, "EGL version:major = ${versions[0]}, minor = ${versions[1]}")

            val attr = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_NONE
            )
            val configs = Array<EGLConfig?>(1, { null })
            val numConfig = IntArray(1)
            flag = EGL14.eglChooseConfig(display, attr, 0, configs, 0, 1, numConfig, 0)
            if (!flag) {
                log()
                return null
            }
            val config = configs.first()
            val eglSurface =
                EGL14.eglCreateWindowSurface(
                    display,
                    config,
                    surface,
                    intArrayOf(EGL14.EGL_NONE),
                    0
                )
            if (EGL14.EGL_NO_SURFACE == eglSurface) {
                log()
                return null
            }
            val context = EGL14.eglCreateContext(
                display, config,
                EGL14.EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION,3,EGL14.EGL_NONE), 0
            )
            if (EGL14.EGL_NO_CONTEXT == context) {
                log()
                return null
            }
            flag = EGL14.eglMakeCurrent(display, eglSurface, eglSurface, context)
            if (!flag) {
                log()
                return null
            }
            return Egl(context, display, eglSurface)
        }

        private fun log() {
            Log.e(TAG, "code = ${EGL14.eglGetError()}")
        }

        @JvmStatic
        private val TAG = "Egl"
    }
}
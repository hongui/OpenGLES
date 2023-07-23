package me.hongui.opengles

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
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
        Egl.makeEGL(surface)?.let {
            val opengles=OpenGLES.makeOpenGLES(width, height)?:return
            opengles.use { draw() }
            it.swapBuffer()
        }
    }

    companion object {
        @JvmStatic
        val TAG = "MainActivity"
    }
}
package cn.easyar.samples.helloar.smartgl

import android.content.Context
import cn.easyar.Engine
import fr.arnaudguyon.smartgl.opengl.SmartGLView
import fr.arnaudguyon.smartgl.opengl.SmartGLViewController
import fr.arnaudguyon.smartgl.touch.TouchHelperEvent

/**
 * Created by zijing on 25/10/2017.
 */
class BXSmartGLView(context: Context) : SmartGLView(context), SmartGLViewController {

    val smartAR = SmartAR(context)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        synchronized(smartAR) {
            if (smartAR.initialize()) {
                smartAR.start()
            }
        }
    }

    override fun onDetachedFromWindow() {
        synchronized(smartAR) {
            smartAR.stop()
            smartAR.dispose()
        }
        super.onDetachedFromWindow()
    }

    override fun onResume() {
        super.onResume()
        Engine.onResume()
    }

    override fun onPause() {
        super.onPause()
        Engine.onPause()
    }

    override fun onPrepareView(glView: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {
        smartAR.prepare(glView)
    }

    override fun onReleaseView(p0: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {

    }

    override fun onTick(glView: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {
        smartAR.renderOnSmartGLView(glView)
    }

    override fun onResizeView(p0: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {

    }

    override fun onTouchEvent(p0: fr.arnaudguyon.smartgl.opengl.SmartGLView?, p1: TouchHelperEvent?) {

    }
}
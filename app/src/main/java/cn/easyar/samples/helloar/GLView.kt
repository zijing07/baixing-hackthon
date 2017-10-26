//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.widget.TextView
import cn.easyar.Engine
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.opengles.GL10

class GLView : GLSurfaceView {
    val helloAR: HelloAR

    private constructor(context: Context): super(context)
    constructor(context: Context, textView: TextView): this(context) {
        helloAR.textView = textView
    }

    init {
        setEGLContextFactory(ContextFactory())
        setEGLConfigChooser(ConfigChooser())

        helloAR = HelloAR()

        this.setRenderer(object : GLSurfaceView.Renderer {
            override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                synchronized(helloAR) {
                    helloAR.initGL()
                }
            }

            override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {
                synchronized(helloAR) {
                    helloAR.resizeGL(w, h)
                }
            }

            override fun onDrawFrame(gl: GL10) {
                synchronized(helloAR) {
                    helloAR.render()
                }
            }
        })
        this.setZOrderMediaOverlay(true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        synchronized(helloAR) {
            if (helloAR.initialize()) {
                helloAR.start()
            }
        }
    }

    override fun onDetachedFromWindow() {
        synchronized(helloAR) {
            helloAR.stop()
            helloAR.dispose()
        }
        super.onDetachedFromWindow()
    }

    override fun onResume() {
        super.onResume()
        Engine.onResume()
    }

    override fun onPause() {
        Engine.onPause()
        super.onPause()
    }

    private class ContextFactory : GLSurfaceView.EGLContextFactory {

        override fun createContext(egl: EGL10, display: EGLDisplay, eglConfig: EGLConfig): EGLContext {
            val context: EGLContext
            val attrib = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
            context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib)
            return context
        }

        override fun destroyContext(egl: EGL10, display: EGLDisplay, context: EGLContext) {
            egl.eglDestroyContext(display, context)
        }

        companion object {
            private val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        }
    }

    private class ConfigChooser : GLSurfaceView.EGLConfigChooser {
        override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
            val EGL_OPENGL_ES2_BIT = 0x0004
            val attrib = intArrayOf(EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE)

            val num_config = IntArray(1)
            egl.eglChooseConfig(display, attrib, null, 0, num_config)

            val numConfigs = num_config[0]
            if (numConfigs <= 0)
                throw IllegalArgumentException("fail to choose EGL configs")

            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            egl.eglChooseConfig(display, attrib, configs, numConfigs,
                    num_config)

            for (config in configs) {
                val `val` = IntArray(1)
                var r = 0
                var g = 0
                var b = 0
                var a = 0
                var d = 0
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_DEPTH_SIZE, `val`))
                    d = `val`[0]
                if (d < 16)
                    continue

                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, `val`))
                    r = `val`[0]
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_GREEN_SIZE, `val`))
                    g = `val`[0]
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_BLUE_SIZE, `val`))
                    b = `val`[0]
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_ALPHA_SIZE, `val`))
                    a = `val`[0]
                if (r == 8 && g == 8 && b == 8 && a == 0)
                    return config!!
            }

            return configs[0]!!
        }
    }
}

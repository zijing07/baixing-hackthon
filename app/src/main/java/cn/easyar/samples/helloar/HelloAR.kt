//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar

import android.opengl.GLES20
import android.opengl.GLU
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.easyar.*

open class HelloAR() {
    var camera: CameraDevice? = null
    var streamer: CameraFrameStreamer? = null
    val trackers = ArrayList<ImageTracker>()
    var videobg_renderer: Renderer? = null
    var box_renderer: BoxRenderer? = null
    var viewport_changed = false
    var view_size = Vec2I(0, 0)
    var rotation = 0
    var viewport = Vec4I(0, 0, 1280, 720)
    lateinit var textView: TextView

    private fun loadFromImage(tracker: ImageTracker, path: String) {
        val target = ImageTarget()
        val jstr = """{
  "images" :
  [
    {
      "image" : "$path",
      "name" : "${path.substring(0, path.indexOf("."))}"
    }
  ]
}"""
        target.setup(jstr, StorageType.Assets or StorageType.Json, "")
        tracker.loadTarget(target) { target, status -> Log.i("HelloAR", "load target ($status): ${target.name()} (${target.runtimeID()})") }
    }

    private fun loadFromJsonFile(tracker: ImageTracker, path: String, targetname: String) {
        val target = ImageTarget()
        target.setup(path, StorageType.Assets, targetname)
        tracker.loadTarget(target) { target, status -> Log.i("HelloAR", "load target ($status): ${target.name()} (${target.runtimeID()})") }
    }

    private fun loadAllFromJsonFile(tracker: ImageTracker, path: String) {
        for (target in ImageTarget.setupAll(path, StorageType.Assets)) {
            tracker.loadTarget(target) { target, status -> Log.i("HelloAR", "load target ($status): ${target.name()} (${target.runtimeID()})") }
        }
    }

    fun initialize(): Boolean {
        camera = CameraDevice()
        streamer = CameraFrameStreamer()
        streamer!!.attachCamera(camera)

        var status = true
        status = status and camera!!.open(CameraDeviceType.Default)
        camera!!.setSize(Vec2I(1280, 720))

        if (!status) {
            return status
        }
        val tracker = ImageTracker()
        tracker.attachStreamer(streamer)
        loadFromJsonFile(tracker, "targets.json", "argame")
        loadFromJsonFile(tracker, "targets.json", "idback")
        loadAllFromJsonFile(tracker, "targets2.json")
        loadFromImage(tracker, "namecard.jpg")
        trackers.add(tracker)

        return status
    }

    fun dispose() {
        for (tracker in trackers) {
            tracker.dispose()
        }
        trackers.clear()
        box_renderer = null
        if (videobg_renderer != null) {
            videobg_renderer!!.dispose()
            videobg_renderer = null
        }
        if (streamer != null) {
            streamer!!.dispose()
            streamer = null
        }
        if (camera != null) {
            camera!!.dispose()
            camera = null
        }
    }

    fun start(): Boolean {
        var status = true
        status = status and (camera != null && camera!!.start())
        status = status and (streamer != null && streamer!!.start())
        camera!!.setFocusMode(CameraDeviceFocusMode.Continousauto)
        for (tracker in trackers) {
            status = status and tracker.start()
        }
        return status
    }

    fun stop(): Boolean {
        var status = true
        for (tracker in trackers) {
            status = status and tracker.stop()
        }
        status = status and (streamer != null && streamer!!.stop())
        status = status and (camera != null && camera!!.stop())
        return status
    }

    fun initGL() {
        if (videobg_renderer != null) {
            videobg_renderer!!.dispose()
        }
        videobg_renderer = Renderer()
        box_renderer = BoxRenderer()
        box_renderer!!.init()
    }

    fun resizeGL(width: Int, height: Int) {
        view_size = Vec2I(width, height)
        viewport_changed = true
    }

    private fun updateViewport() {
        val calib = camera?.cameraCalibration() ?: null
        val rotation = calib?.rotation() ?: 0
        if (rotation != this.rotation) {
            this.rotation = rotation
            viewport_changed = true
        }
        if (viewport_changed) {
            var size = Vec2I(1, 1)
            if (camera != null && camera!!.isOpened) {
                size = camera!!.size()
            }
            if (rotation == 90 || rotation == 270) {
                size = Vec2I(size.data[1], size.data[0])
            }
            val scaleRatio = Math.max(view_size.data[0].toFloat() / size.data[0].toFloat(), view_size.data[1].toFloat() / size.data[1].toFloat())
            val viewport_size = Vec2I(Math.round(size.data[0] * scaleRatio), Math.round(size.data[1] * scaleRatio))
            viewport = Vec4I((view_size.data[0] - viewport_size.data[0]) / 2, (view_size.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1])

            if (camera != null && camera!!.isOpened) {
                viewport_changed = false
            }
        }
    }

    fun render() {
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (videobg_renderer != null) {
            val default_viewport = Vec4I(0, 0, view_size.data[0], view_size.data[1])
            GLES20.glViewport(default_viewport.data[0], default_viewport.data[1], default_viewport.data[2], default_viewport.data[3])
            if (videobg_renderer!!.renderErrorMessage(default_viewport)) {
                return
            }
        }

        if (streamer == null) {
            return
        }
        val frame = streamer!!.peek()
        try {
            updateViewport()
            GLES20.glViewport(viewport.data[0], viewport.data[1], viewport.data[2], viewport.data[3])

            videobg_renderer?.render(frame, viewport)

            textView.post {
                textView.visibility = View.GONE
            }

            for (targetInstance in frame.targetInstances()) {
                val status = targetInstance.status()
                if (status == TargetStatus.Tracked) {
                    val target = targetInstance.target()
                    val imagetarget = target as? ImageTarget ?: continue


                    val projectionMatrix = camera!!.projectionGL(0.2f, 500f).data
                    val modelViewMatrix = targetInstance.poseGL().data
                    val viewArray = kotlin.IntArray(4)
                    val windowArray = FloatArray(4)

                    GLU.gluProject(0f, 0f, 0f,
                            modelViewMatrix, 0, projectionMatrix, 0,
                            viewport.data, 0, windowArray, 0)

                    textView.post {
                        val layoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
                        layoutParams.leftMargin = windowArray[0].toInt() - textView.width / 2
                        layoutParams.topMargin = viewport.data[3] - windowArray[1].toInt() - textView.height / 2
                        Log.d("ABC", windowArray.joinToString(","))
                        textView.layoutParams = layoutParams
                        textView.visibility = View.VISIBLE
                    }

//                    box_renderer?.render(camera!!.projectionGL(0.2f, 500f), targetInstance.poseGL(), imagetarget.size())
                }
            }
        } finally {
            frame.dispose()
        }
    }
}

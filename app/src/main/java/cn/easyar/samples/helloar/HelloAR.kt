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
import cn.easyar.*
import cn.easyar.samples.widget.ARCanvasView

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
    lateinit var canvasView: ARCanvasView

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

        for (i in 0..3) {
            val tracker = ImageTracker()
            tracker.attachStreamer(streamer)
            loadFromJsonFile(tracker, "targets.json", "argame")
            loadFromJsonFile(tracker, "targets.json", "idback")
//        loadAllFromJsonFile(tracker, "targets2.json")
            loadFromImage(tracker, "namecard.jpg")
            loadFromImage(tracker, "58.jpg")
            trackers.add(tracker)
        }

        return status
    }

    fun dispose() {
        for (tracker in trackers) {
            tracker.dispose()
        }
        trackers.clear()
        // box_renderer = null
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
//        box_renderer = BoxRenderer()
//        box_renderer!!.init()
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

            canvasView.post {
                canvasView.clearDstPolys()
            }
            for (targetInstance in frame.targetInstances()) {
                val status = targetInstance.status()
                if (status == TargetStatus.Tracked) {
                    val target = targetInstance.target()
                    val imagetarget = target as? ImageTarget ?: continue


                    val projectionMatrix = camera!!.projectionGL(0.2f, 500f).data
                    val modelViewMatrix = targetInstance.poseGL().data

                    val leftTopArray = getScreenCoordinate(LEFT_TOP(), imagetarget, modelViewMatrix, projectionMatrix)
                    val rightTopArray = getScreenCoordinate(RIGHT_TOP(), imagetarget, modelViewMatrix, projectionMatrix)
                    val rightBottomArray = getScreenCoordinate(RIGHT_BOTTOM(), imagetarget, modelViewMatrix, projectionMatrix)
                    val leftBottomArray = getScreenCoordinate(LEFT_BOTTOM(), imagetarget, modelViewMatrix, projectionMatrix)

                    canvasView.post {
                        canvasView.addDstPoly(floatArrayOf(
                                leftTopArray[0], viewport.data[3] - leftTopArray[1],
                                rightTopArray[0], viewport.data[3] - rightTopArray[1],
                                rightBottomArray[0], viewport.data[3] - rightBottomArray[1],
                                leftBottomArray[0], viewport.data[3] - leftBottomArray[1]
                        ))
                    }

                    // box_renderer?.render(camera!!.projectionGL(0.2f, 500f), targetInstance.poseGL(), imagetarget.size())
                }
            }
            canvasView.post {
                canvasView.updateCanvas()
            }
        } finally {
            frame.dispose()
        }
    }

    fun getScreenCoordinate(corner: CORNER, imageTarget: ImageTarget, modelViewMatrix: FloatArray,
                projectionMatrix: FloatArray): FloatArray {
        val windowArray = FloatArray(4)

        val (w, h) = imageTarget.size().data
        var x = w / 2
        var y = h / 2
        val z = x / 1000

        when (corner) {
            is LEFT_TOP -> print (1)
            is LEFT_BOTTOM -> y *= -1
            is RIGHT_BOTTOM -> {
                x *= -1
                y *= -1
            }
            is RIGHT_TOP -> {
                x *= -1
            }
        }

        GLU.gluProject(x, y, z,
                modelViewMatrix, 0, projectionMatrix, 0,
                viewport.data, 0, windowArray, 0)

        return windowArray
    }
}

sealed class CORNER
class LEFT_TOP: CORNER()
class LEFT_BOTTOM: CORNER()
class RIGHT_TOP: CORNER()
class RIGHT_BOTTOM: CORNER()


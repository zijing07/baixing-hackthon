//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import cn.easyar.Engine
import cn.easyar.samples.helloar.smartgl.BXSmartGLView

class MainActivity : AppCompatActivity() {
    companion object {
        //
        // Steps to create the key for this sample:
        //  1. login www.easyar.com
        //  2. create app with
        //      Name: HelloAR
        //      Package Name: cn.easyar.samples.helloar
        //  3. find the created item in the list and show key
        //  4. set key string bellow
        //
        private val key = "zv8SLRfY1dKZoGicYNAxdiENkQRuFf3d3mJB1TNQsFOA3PPhoEzoDICfdG9K42vq2wrcKX56So9bIra2AN2kat2FiDx2vdMXoebDbl6pAvtv1rM8P2YKF6DDvIAIvov4n26tKWVtVEyIN0rY1dhaDsBIycBSxlthGRrCYdw5uZaHS71j5M7MZbSqkq1BXllIjCJtMlVL"
    }

    private var glView: GLView? = null
    private var smartGlView: BXSmartGLView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!Engine.initialize(this, key)) {
            Log.e("HelloAR", "Initialization Failed.")
        }

        glView = GLView(this)
        smartGlView = BXSmartGLView(this)

        requestCameraPermission(object : PermissionCallback {
            override fun onSuccess() {
                findViewById<FrameLayout>(R.id.preview).addView(glView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
//                findViewById<FrameLayout>(R.id.preview).addView(smartGlView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }

            override fun onFailure() {}
        })
    }

    private interface PermissionCallback {
        fun onSuccess()
        fun onFailure()
    }

    private val permissionCallbacks = HashMap<Int, PermissionCallback>()
    private var permissionRequestCodeSerial = 0
    @TargetApi(23)
    private fun requestCameraPermission(callback: PermissionCallback) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                val requestCode = permissionRequestCodeSerial
                permissionRequestCodeSerial += 1
                permissionCallbacks.put(requestCode, callback)
                requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)
            } else {
                callback.onSuccess()
            }
        } else {
            callback.onSuccess()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissionCallbacks.containsKey(requestCode)) {
            val callback = permissionCallbacks[requestCode]!!
            permissionCallbacks.remove(requestCode)
            var executed = false
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true
                    callback.onFailure()
                }
            }
            if (!executed) {
                callback.onSuccess()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        if (glView != null) {
            glView!!.onResume()
        }
    }

    override fun onPause() {
        if (glView != null) {
            glView!!.onPause()
        }
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}

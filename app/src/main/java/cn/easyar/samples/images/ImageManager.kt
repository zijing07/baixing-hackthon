package cn.easyar.samples.images

import android.os.Environment
import android.util.Log
import com.qiniu.common.Zone
import com.qiniu.storage.BucketManager
import com.qiniu.storage.Configuration
import com.qiniu.util.Auth
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by momo on 2017/10/26.
 *
 * 处理图片的上传和下载
 *
 * 文件都存储在 baixing/hackthon 下面
 *
 * baixing/hackthon/camera       : 拍照后的临时文件
 * baixing/hackthon/targets      : 需要识别的对象们
 */

const val TAG = "ImageManager"

const val accessKey = "6xXuUNHLuQA3U31TKxM6FXMLyXWpH1yUqPGm9Yak"
const val secretKey = "zzgoK3U8xa1G5Rw1nj1Qwi5CiC7a8n0fhmXNWBIO"
const val bucketName = "2017-baixing-hackthon"
const val fileKey = ""

val config = Configuration(Zone.zone0())
val auth = Auth.create(accessKey, secretKey)

const val BASE_DIR = "/baixing/hackthon"
const val CAMERA_DIR = "/baixing/hackthon/camera"
const val TARGET_DIR = "/baixing/hackthon/targets"

fun ensureDirExists() {
    val cameraDir = File(getCameraDir())
    if (!cameraDir.exists()) {
        cameraDir.mkdirs()
    }

    val targetDir = File(getTargetDir())
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }
}

fun getCameraDir() = Environment.getExternalStorageDirectory().path + CAMERA_DIR
fun getTargetDir() = Environment.getExternalStorageDirectory().path + TARGET_DIR

fun createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File("${getCameraDir()}/${imageFileName}.jpg")

    return image
}

fun fetchImageList() {

    Thread(Runnable {
        val bucketManager = BucketManager(auth, config)
        val fileListing = bucketManager.listFiles(bucketName, "", "", 100, "")
        fileListing.items.forEach {
            Log.d(TAG, it.key)
        }
    }).start()
}

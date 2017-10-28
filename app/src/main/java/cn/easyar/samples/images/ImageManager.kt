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
import com.qiniu.common.QiniuException
import com.qiniu.storage.model.DefaultPutRet
import com.google.gson.Gson
import com.qiniu.storage.UploadManager
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import android.R.attr.path
import android.content.Context
import cn.easyar.samples.helloar.toast
import com.liulishuo.filedownloader.FileDownloader
import java.net.URLEncoder


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
const val filePrefix = "http://oyiybddqi.bkt.clouddn.com/"

val config = Configuration(Zone.zone0())
val auth = Auth.create(accessKey, secretKey)
val uploadManager = UploadManager(config);

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

fun fetchImageList(context: Context) {
    Thread(Runnable {
        val bucketManager = BucketManager(auth, config)
        val fileListing = bucketManager.listFiles(bucketName, "", "", 100, "")
        fileListing.items.forEach {
            val realPath = "${getTargetDir()}/${it.key}"
            val imageFile = File(realPath)
//            if (!imageFile.exists()) {
                val fileName = URLEncoder.encode(it.key, "utf-8")
                downloadFile("${filePrefix}${fileName}", context, it.key)
//            }
        }
    }).start()
}

fun downloadFile(url: String, context: Context, fileName: String) {
    FileDownloader.setup(context)
    FileDownloader.getImpl().create(url)
            .setPath("${getTargetDir()}/${fileName}")
            .setListener(object : FileDownloadListener() {
                override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {}

                override fun connected(task: BaseDownloadTask?, etag: String?, isContinue: Boolean, soFarBytes: Int, totalBytes: Int) {}

                override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {}

                override fun blockComplete(task: BaseDownloadTask?) {}

                override fun retry(task: BaseDownloadTask?, ex: Throwable?, retryingTimes: Int, soFarBytes: Int) {}

                override fun completed(task: BaseDownloadTask) {
                }

                override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {}

                override fun error(task: BaseDownloadTask, e: Throwable) {}

                override fun warn(task: BaseDownloadTask) {}
            }).start()
}

fun uploadImage(path: String, key: String = path.split("/").last(), success: () -> Any, fail: (msg: String) -> Any) {
    Thread(Runnable {
        try {
            val response = uploadManager.put(path, key, auth.uploadToken(bucketName))
            //解析上传成功的结果
            val putRet = Gson().fromJson(response.bodyString(), DefaultPutRet::class.java)
            Log.d(TAG, putRet.key)
            Log.d(TAG, putRet.hash)
            success()
        } catch (ex: QiniuException) {
            val r = ex.response
            try {
                fail(r.bodyString())
            } catch (ex2: QiniuException) {
                //ignore
            }
        }
    }).start()
}

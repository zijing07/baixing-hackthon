package cn.easyar.samples.images

import android.util.Log
import com.qiniu.common.Zone
import com.qiniu.storage.BucketManager
import com.qiniu.storage.Configuration
import com.qiniu.util.Auth

/**
 * Created by momo on 2017/10/26.
 *
 * 处理图片的上传和下载
 */

val TAG = "ImageManager"

val accessKey = "6xXuUNHLuQA3U31TKxM6FXMLyXWpH1yUqPGm9Yak"
val secretKey = "zzgoK3U8xa1G5Rw1nj1Qwi5CiC7a8n0fhmXNWBIO"
val bucketName = "2017-baixing-hackthon"
val fileKey = ""

val config = Configuration(Zone.zone0())
val auth = Auth.create(accessKey, secretKey)

fun fetchImageList() {

    Thread(Runnable {
        val bucketManager = BucketManager(auth, config)
        val fileListing = bucketManager.listFiles(bucketName, "", "", 100, "")
        fileListing.items.forEach {
            Log.d(TAG, it.key)
        }
    }).start()
}

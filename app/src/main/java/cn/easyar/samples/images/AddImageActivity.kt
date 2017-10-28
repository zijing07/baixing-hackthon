package cn.easyar.samples.images

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.provider.MediaStore
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.ImageView
import cn.easyar.samples.helloar.R
import cn.easyar.samples.helloar.setImage
import cn.easyar.samples.helloar.toast
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException

/**
 * Created by zijing on 28/10/2017
 */
class AddImageActivity: AppCompatActivity() {

    val REQ_TAKE_PICTURE = 1

    lateinit var imageView: ImageView

    var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addimage)

        startCameraActivity()

        imageView = findViewById<ImageView>(R.id.activity_addimage_image)
        findViewById<Button>(R.id.activity_addimage_capture).setOnClickListener {
            startCameraActivity()
        }
        findViewById<Button>(R.id.activity_addimage_edit).setOnClickListener {
            startEdit()
        }
        findViewById<Button>(R.id.activity_addimage_picok).setOnClickListener {

        }
    }

    fun startCameraActivity() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        takePictureIntent.resolveActivity(packageManager) ?: return fail("您没有相机权限")

        photoFile = try { createImageFile() } catch (e: IOException) { null }
        photoFile ?: return fail("您没有文件系统权限")

        val photoURI = Uri.fromFile(photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, REQ_TAKE_PICTURE)
    }

    fun startEdit() {
        try {
            val srcPath = photoFile?.absolutePath ?: return fail("先定图片不存在，再拍一张吧", this::startCameraActivity)
            val srcUri = Uri.fromFile(File(srcPath))
            val desPath = srcPath.replace("camera", "targets")
            val desFile = File(desPath)
            if (!desFile.exists()) desFile.createNewFile()
            val desUri = Uri.fromFile(desFile)

            UCrop.of(srcUri, desUri)
                    .withAspectRatio(16f, 9f)
                    .withMaxResultSize(800, 600)
                    .start(this);
        } catch (ex: Exception) {
            toast("还没有文件权限哦")
        }
    }

    fun startUpload() {
        
    }

    fun fail(message: String, foo: () -> Any = this::finish) {
        toast(message)
        foo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) {
            toast("拍照失败")
            finish()
            return
        }

        when (requestCode) {
            REQ_TAKE_PICTURE -> {
                toast("拍照成功")
                val path = photoFile?.absolutePath ?: return
                imageView.setImage(path)
                startEdit()
            }
            UCrop.REQUEST_CROP -> {
                data ?: return
                val resultUri = UCrop.getOutput(data)
                imageView.setImageURI(resultUri)
            }
            else -> finish()
        }
    }
}
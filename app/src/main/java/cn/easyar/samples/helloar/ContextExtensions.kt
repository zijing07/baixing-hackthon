package cn.easyar.samples.helloar

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.Toast

/**
 * Created by zijing on 28/10/2017
 */

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun ImageView.setImage(path: String) {
    val bitmap = BitmapFactory.decodeFile(path) ?: return
    this.setImageBitmap(bitmap)
}

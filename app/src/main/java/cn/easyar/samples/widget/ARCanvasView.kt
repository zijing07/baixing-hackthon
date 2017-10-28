package cn.easyar.samples.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.view.View
import cn.easyar.samples.helloar.R

class ARCanvasView(context: Context) : View(context) {

    private var dstPolys = ArrayList<FloatArray>()
    var img: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.baixing)
    var imgWidth = img.width
    var imgHeight = img.height
    var srcPoly: FloatArray = floatArrayOf(
            0f, 0f,
            imgWidth.toFloat(), 0f,
            imgWidth.toFloat(), imgHeight.toFloat(),
            0f, imgHeight.toFloat())

    private val imgMatrix: Matrix = Matrix()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        if (dstPolys.size > 0) {
            dstPolys.forEach {
                imgMatrix.reset()
                imgMatrix.setPolyToPoly(srcPoly, 0, it, 0, 4)
                canvas.drawBitmap(img, imgMatrix, null)
            }
        }
    }

    fun addDstPoly(dstPoly: FloatArray) {
        dstPolys.add(dstPoly)
    }

    fun clearDstPolys() {
        dstPolys.clear()
    }

    fun updateCanvas() {
        invalidate()
    }
}
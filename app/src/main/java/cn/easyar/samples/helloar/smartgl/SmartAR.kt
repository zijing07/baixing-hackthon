package cn.easyar.samples.helloar.smartgl

import cn.easyar.samples.helloar.HelloAR
import fr.arnaudguyon.smartgl.opengl.RenderPassObject3D
import fr.arnaudguyon.smartgl.opengl.SmartGLView
import android.content.Context
import cn.easyar.samples.helloar.R
import fr.arnaudguyon.smartgl.tools.WavefrontModel

/**
 * Created by zijing on 25/10/2017.
 */
class SmartAR(val context: Context): HelloAR() {

    val obj = RenderPassObject3D()

    init {
//        renderer.addRenderPass(renderPassObject3D)  // add it only once for all 3D Objects

//        mShipTexture = Texture(context, R.drawable.ship_picture)

//        val model = WavefrontModel.Builder(context, R.raw.spaceship_obj)
//                .addTexture("Material001", mShipTexture)    // "Material001" is defined in the spaceship_obj file
//                .create()
//        mSpaceship = model.toObject3D()
//        mSpaceship.setScale(0.1f, 0.1f, 0.1f)  // Adjust the scale if object is too big / too small
//        mSpaceship.setPos(0, 0, -5)            // move the object in front of the camera
//        renderPassObject3D.addObject(mSpaceship)
    }

    fun prepare(glView: SmartGLView?) {
        glView ?: return
        val renderer = glView.smartGLRenderer
        renderer.addRenderPass(obj)
        val model = WavefrontModel.Builder(context, R.raw.a).create().toObject3D()
        obj.addObject(model)
        renderer.addRenderPass(obj)
    }

    fun renderOnSmartGLView(glView: SmartGLView?) {
        glView ?: return

        val renderer = glView.smartGLRenderer
        val frame = streamer!!.peek()

//        val model = WavefrontModel.Builder(context, R.raw.A)
    }
}
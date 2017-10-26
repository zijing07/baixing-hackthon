package cn.easyar.samples.helloar.smartgl

import android.content.Context
import cn.easyar.samples.helloar.HelloAR
import cn.easyar.samples.helloar.R
import fr.arnaudguyon.smartgl.touch.TouchHelperEvent
import fr.arnaudguyon.smartgl.math.Vector3D
import fr.arnaudguyon.smartgl.opengl.*
import fr.arnaudguyon.smartgl.tools.WavefrontModel


/**
 * Created by zijing on 25/10/2017
 */
class BXSmartGLView(context: Context) : SmartGLView(context), SmartGLViewController {
    init {
        // smart gl init
        controller = this
        setDefaultRenderer(context)
        smartGLRenderer.setClearColor(0.01f, 0.01f, 0.1f, 0f)
        val lightAmbiant = LightAmbiant(1f, 1f, 1f)
        smartGLRenderer.setLightAmbiant(lightAmbiant)

        // easy ar init
    }

    var helloAR: HelloAR? = null
    constructor(context: Context, ar: HelloAR?): this(context) {
        helloAR = ar
    }

    var mObject3D: Object3D? = null
    val mRenderPassObject3D = RenderPassObject3D(RenderPassObject3D.ShaderType.SHADER_TEXTURE_LIGHTS, true, true)
    val mRenderPassObject3DColor = RenderPassObject3D(RenderPassObject3D.ShaderType.SHADER_COLOR, true, false)
    val mRenderPassSprite = RenderPassSprite()

    val mSpriteTexture = Texture(context, android.R.drawable.divider_horizontal_dim_dark)
    val mObjectTexture = Texture(context, R.drawable.space_cruiser_4_color)

    val mSprite = Sprite(120, 120)

    var objectRotation = 0f

    override fun onPrepareView(glView: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {
        // Add RenderPass for Sprites & Object3D
        val renderer = glView?.smartGLRenderer ?: return

        renderer.addRenderPass(mRenderPassObject3D)
        renderer.addRenderPass(mRenderPassObject3DColor)
        renderer.addRenderPass(mRenderPassSprite)

        renderer.setDoubleSided(false)

        val lightColor = SmartColor(1f, 1f, 1f)
        val lightDirection = Vector3D(0f, 1f, 1f)
        lightDirection.normalize()
        val lightParallel = LightParallel(lightColor, lightDirection)
        renderer.setLightParallel(lightParallel)


        mSprite.setPivot(0.5f, 0.5f)
        mSprite.setPos(60f, 60f)
        mSprite.setTexture(mSpriteTexture)
        mSprite.displayPriority = 20
        mRenderPassSprite.addSprite(mSprite)

        val model = WavefrontModel.Builder(context, R.raw.space_ship)
                .addTexture("", mObjectTexture)
                .create()
        mObject3D = model.toObject3D()
        mObject3D?.setScale(0.08f, 0.08f, 0.08f)
        mObject3D?.setPos(0f, 0f, -5f)
        mObject3D?.setRotation(120f, 50f, 120f)
        mRenderPassObject3D.addObject(mObject3D)
    }

    override fun onReleaseView(p0: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {
        mObjectTexture.release()
        mSpriteTexture.release()
    }

    override fun onTick(glView: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {
        val renderer = glView?.smartGLRenderer
        renderer ?: return
        objectRotation += renderer.frameDuration * 10
        mObject3D?.setRotation(objectRotation, objectRotation, objectRotation)
    }

    override fun onResizeView(p0: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {

    }

    override fun onTouchEvent(p0: fr.arnaudguyon.smartgl.opengl.SmartGLView?, p1: TouchHelperEvent?) {

    }
}
package cn.easyar.samples.helloar.smartgl

import android.content.Context
import cn.easyar.samples.helloar.R
import fr.arnaudguyon.smartgl.opengl.SmartGLView
import fr.arnaudguyon.smartgl.opengl.SmartGLViewController
import fr.arnaudguyon.smartgl.touch.TouchHelperEvent
import fr.arnaudguyon.smartgl.opengl.RenderPassSprite
import fr.arnaudguyon.smartgl.opengl.RenderPassObject3D
import fr.arnaudguyon.smartgl.opengl.Sprite
import fr.arnaudguyon.smartgl.opengl.Texture
import fr.arnaudguyon.smartgl.opengl.LightParallel
import fr.arnaudguyon.smartgl.math.Vector3D
import fr.arnaudguyon.smartgl.opengl.SmartColor
import fr.arnaudguyon.smartgl.tools.WavefrontModel
import fr.arnaudguyon.smartgl.opengl.LightAmbiant



/**
 * Created by zijing on 25/10/2017
 */
class BXSmartGLView(context: Context) : SmartGLView(context), SmartGLViewController {

    init {
        controller = this
        setDefaultRenderer(context)

        smartGLRenderer.setClearColor(0.01f, 0.01f, 0.1f, 1f)

        val lightAmbiant = LightAmbiant(0.2f, 0.2f, 0.2f)
        smartGLRenderer.setLightAmbiant(lightAmbiant)
    }

    val mRenderPassObject3D = RenderPassObject3D(RenderPassObject3D.ShaderType.SHADER_TEXTURE_LIGHTS, true, true)
    val mRenderPassObject3DColor = RenderPassObject3D(RenderPassObject3D.ShaderType.SHADER_COLOR, true, false)
    val mRenderPassSprite = RenderPassSprite()

    val mSpriteTexture = Texture(context, android.R.drawable.divider_horizontal_dim_dark)
    val mObjectTexture = Texture(context, R.drawable.space_cruiser_4_color)

    val mSprite = Sprite(120, 120)

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
        val mObject3D = model.toObject3D()
        mObject3D.setScale(0.2f, 0.2f, 0.2f)
        mObject3D.setPos(0f, 0f, -7f)
        mRenderPassObject3D.addObject(mObject3D)
    }

    override fun onReleaseView(p0: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {
        mObjectTexture.release()
        mSpriteTexture.release()
    }

    override fun onTick(glView: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {
    }

    override fun onResizeView(p0: fr.arnaudguyon.smartgl.opengl.SmartGLView?) {

    }

    override fun onTouchEvent(p0: fr.arnaudguyon.smartgl.opengl.SmartGLView?, p1: TouchHelperEvent?) {

    }
}
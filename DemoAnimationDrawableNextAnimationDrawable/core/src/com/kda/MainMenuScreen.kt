package com.kda

import animated.AnimatedHumanDrawable
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*

import com.badlogic.gdx.utils.viewport.FitViewport

import com.badlogic.gdx.scenes.scene2d.ui.List as UIList
import com.badlogic.gdx.utils.Array as UIArray

class MainMenuScreen(private val game: KDA) : ScreenAdapter() {
    private val stage: Stage = Stage(FitViewport(game.screenWidth, game.screenHeight))
    private val player = AnimatedHumanDrawable(game,300f,300f,"right")
    private val sprite = player.viewBox() //Table() actor included Group() with ImageAnimationDrawable as child
    
    override fun show() {
        Gdx.input.inputProcessor = stage
        stage.isDebugAll = true //turn on frames around objects
        sprite.x = 500f //move close to center, or will be placed in left bottom corner of screen
        stage.addActor(sprite) //animated extended image added on stage and will be rendered as part of stage
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        super.render(delta)
        Gdx.gl.glClearColor(0f, 0.5f, 0.5f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        player.calculateAction(delta) //call player method for calculation moving on screen and mirroring animation
        stage.act(delta)
        stage.draw()

    }
}
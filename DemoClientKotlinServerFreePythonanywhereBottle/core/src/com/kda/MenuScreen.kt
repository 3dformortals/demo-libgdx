package com.kda

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net.HttpMethods
import com.badlogic.gdx.Net.HttpRequest
import com.badlogic.gdx.Net.HttpResponse
import com.badlogic.gdx.Net.HttpResponseListener
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.net.HttpParametersUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport

class MenuScreen(private val game:KDA) : ScreenAdapter() {
    val stage: Stage = Stage(FitViewport(game.screenWidth, game.screenHeight)) //for Actors
    val uiskin: Skin = Skin() //val = no need "?" for nullsafety and "!!" that check not null
    val table: Table = Table() //button keeper, no need skin inside constructor in this case
    var button: TextButton? = null //will be created later after uiskin will be configured
    var buttonWriteServer: TextButton? = null
    var buttonReadServer: TextButton? = null
    var labelShowServer: Label? = null
    var label: Label? = null //will be created later after uiskin will be configured
    val buttonStyle: TextButtonStyle = TextButtonStyle() //empty style holder for button
    val labelStyle: LabelStyle = LabelStyle() //empty style holder for label
    val mc = game.memoryCard!! //short name for game data storage
    
    var clicks:Int = 0
    
    /*this functions allow save/load data into preconfigured libgdx app storage when app closed/reopened later*/
    private fun saveData(){ mc.putInteger("clicks",clicks); mc.flush()}/*set new value and fix changes*/
    private fun loadData(){ clicks = mc.getInteger("clicks",0) } //0 default if empty
    
    /**write clicks to free pythonanywhere.com bottlepy server, prepared earlier + label message*/
    private fun writeServer(){
        val req = HttpRequest(HttpMethods.POST)
        req.url = "http://demotest.pythonanywhere.com/write/" /*same as python side server @route value*/
        val par = mapOf("clicks" to "$clicks") /*constructor need form of kotlin mutable map*/
        req.setContent(HttpParametersUtils.convertHttpParameters(par)) /*convert map to url param form of post method*/
        
        Gdx.net.sendHttpRequest(req, object: HttpResponseListener{
            override fun cancelled() {
                labelShowServer!!.setText("write request was cancelled")
            }
    
            override fun failed(t: Throwable?) {
                labelShowServer!!.setText("write request was failed")
            }
    
            override fun handleHttpResponse(httpResponse: HttpResponse?) {
                labelShowServer!!.setText(httpResponse!!.resultAsString) /*data from python bottlepy server side as string*/
            }
        })
    }
    /**read clicks from server and show it as label text*/
    private fun readServer(){
        val req = HttpRequest(HttpMethods.GET)
        req.url = "http://demotest.pythonanywhere.com/read/"
        Gdx.net.sendHttpRequest(req, object: HttpResponseListener{
            override fun cancelled() {
                labelShowServer!!.setText("read request was cancelled")
            }
        
            override fun failed(t: Throwable?) {
                labelShowServer!!.setText("read request was failed")
            }
        
            override fun handleHttpResponse(httpResponse: HttpResponse?) {
                labelShowServer!!.setText(httpResponse!!.resultAsString)
            }
        })
        
    }
    
    override fun show() {

        Gdx.input.inputProcessor = stage //now buttons will change color when pressed ... Actors touching alive
        
        uiskin.addRegions(TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"))) //use downloaded skin
        uiskin.load(Gdx.files.internal("skin/uiskin.json")) //upload fonts into skin, now new fonts can be used

        buttonStyle.font = uiskin.get("default-font", BitmapFont::class.java) //add new font to button style
        buttonStyle.up = uiskin.getDrawable("default-round") //add button up state to button style
        buttonStyle.down = uiskin.getDrawable("default-round-down") //add button down state to button style
        button = TextButton("", uiskin) //button with empty text and uiskin as skin parameter
        button!!.style = buttonStyle //use buttonStyle
        button!!.setText("button") //set new text of button label
        button!!.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                println("button pressed")
                clicks += 1
                saveData() //save data into storage after click, and fix changes
                label!!.setText(clicks.toString())
            }
        })
        
        buttonWriteServer = TextButton("write data to server",uiskin)
        buttonWriteServer!!.style = buttonStyle
        buttonWriteServer!!.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                println("buttonWriteServer pressed")
                writeServer()
            }
        })
        
        buttonReadServer = TextButton("read data from server",uiskin)
        buttonReadServer!!.style = buttonStyle
        buttonReadServer!!.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                println("buttonReadServer pressed")
                readServer()
            }
        })
        
        labelStyle.font = uiskin.get("default-font", BitmapFont::class.java) //font from default skin
        label = Label("", uiskin)
        label!!.style = labelStyle
        loadData() //load data from storage and refresh `clicks` property value
        label!!.setText(clicks.toString())
        
        labelShowServer = Label("fresh label",uiskin)
        labelShowServer!!.style = labelStyle

        table.setSize(game.screenWidth, game.screenHeight)
        table.add(button).expandX().expandY() //add button to first cell of first table row
        table.add(buttonWriteServer).expandX().expandY()
        table.add(buttonReadServer).expandX().expandY()
        table.row() //add new row into table
        table.add(label).expandX().expandY() //add label to first cell of second table row
        table.add(labelShowServer).expandX().expandY().colspan(2)
        stage.addActor(table) //add table with child's into stage

    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0.4f, 0.4f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }
}
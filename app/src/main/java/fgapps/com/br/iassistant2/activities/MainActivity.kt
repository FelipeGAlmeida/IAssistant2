package fgapps.com.br.iassistant2.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import fgapps.com.br.iassistant2.R
import fgapps.com.br.iassistant2.defines.Constants
import fgapps.com.br.iassistant2.defines.Dictionary
import fgapps.com.br.iassistant2.gestures.GestureController
import fgapps.com.br.iassistant2.music.Music
import fgapps.com.br.iassistant2.music.MusicLoader
import fgapps.com.br.iassistant2.services.MusicPlayerService
import fgapps.com.br.iassistant2.utils.Animations
import fgapps.com.br.iassistant2.defines.MediaPlayerStates
import fgapps.com.br.iassistant2.interfaces.*
import fgapps.com.br.iassistant2.services.AIService
import fgapps.com.br.iassistant2.utils.Dimmer
import fgapps.com.br.iassistant2.utils.Permissions
import fgapps.com.br.iassistant2.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
                    MusicChangeListener,
                    VolumeChangeListener,
                    MediaPlayerListener,
                    TouchListener {

    /*** Variables ***/
    private lateinit var mMusicService: MusicPlayerService
    private var mBound: Boolean = false

    private lateinit var mDimmer: Dimmer
    private lateinit var mAI: AIService

    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var mGestureController: GestureController

    private lateinit var background: ImageView
    fun getAppWidth() = background.width
    fun getAppHeight() = background.height

    private var mVolumeHandler: Handler? = null
    private var mVolumeShown: Boolean = false
    private var mButtonHandler: Handler? = null
    private var mButtonShown: Boolean = false
    private var mEditHandler: Handler? = null
    private var mEditShown: Boolean = false

    /*** Functions ***/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBackground()
        setGestures()
        setDimmer()
        setControls()
    }

    private fun setBackground() {
        splashscreen_panel.visibility = View.VISIBLE
        shuffle_btn.drawable.alpha = Constants.SHUFFLE_OFF
        background = background_img
        Glide.with(this)
                .load(R.drawable.back_anim1)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        if(Permissions.checkPermission(this@MainActivity,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Permissions.READ_EXTERNAL_STORAGE_CODE)) MusicLoader.loadAllMusic(this@MainActivity)
                        Handler().postDelayed({
                            Animations.fade(this@MainActivity, splashscreen_panel, Constants.HIDE_SPLASHSCREEN, true)
                        }, Constants.HIDE_SPLASHSCREEN)
                        return false
                    }

                })
                .into(background)
    }

    private fun setGestures(){
        mGestureController = GestureController(this)
        mDetector = GestureDetectorCompat(this, mGestureController)
        mDetector.setOnDoubleTapListener(mGestureController)
        mGestureController.setGestureDetector(mDetector)
    }

    private fun setControls(){
        shuffle_btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                if(mBound) setShuffleView(mMusicService.shuffle())
            }
        })

        command_edit.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0 != null && p0.toString() != "")
                    this@MainActivity.doublePress()
            }

        })

        command_btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                if(mBound) {
                    mAI.checkCommand(command_edit.text.toString())
                    command_edit.setText("")
                }
                Utils.enableKeyboard(this@MainActivity, false, command_edit)
                mEditHandler!!.removeCallbacksAndMessages(null)
                Animations.fade(this@MainActivity, typeCommand_panel, Constants.FADEOUT_COMMANDEDIT, true)
                mEditShown = false
            }
        })

        settings_btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
            }

        })

        repeat_btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {

            }

        })
    }

    private fun setDimmer(){
        mDimmer = Dimmer(this@MainActivity)
        mDimmer.init()
    }

    private fun setAI(){
        Dictionary.init()
        mAI = AIService(this@MainActivity, mMusicService)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(!mBound) return false
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    /*** Set View Changes ***/
    private fun setShuffleView(shuffled: Boolean){
        shuffle_btn.drawable.alpha = when(shuffled){
            true -> Constants.SHUFFLE_ON
            false -> Constants.SHUFFLE_OFF
        }
    }

    private fun setVolumeViews(volume: Float){
        volume_txt.text = ((volume*Constants.STEP_VOLUMEBAR)
                .toInt()*Constants.STEP_VOLUMEBAR)
                .toString()

        val vol_int = (volume * getAppHeight()).toInt()
        when(vol_int == Constants.VOLUME_MUTE){
            true -> {
                mute_img.visibility = View.VISIBLE
                volume_txt.visibility = View.GONE
            }
            false -> {
                mute_img.visibility = View.GONE
                volume_txt.visibility = View.VISIBLE
            }
        }

        val lp = volume_bar.layoutParams
        lp.height = vol_int + Constants.VOLZERO_GAP
        lp.width = (getAppWidth() * Constants.WIDTH_VOLUMEPANEL).toInt()
        volume_bar.layoutParams = lp
    }

    /*** Gesture controller response functions ***/
    override fun nextMusic() {
        mMusicService.next()
    }

    override fun prevMusic() {
        mMusicService.prev()
    }

    override fun volumeChange(volume: Float) {
        if(mVolumeHandler == null){
            mVolumeHandler = Handler()
        }

        if(mVolumeShown) mVolumeHandler!!.removeCallbacksAndMessages(null)
        else Animations.fade(this@MainActivity, volume_panel, Constants.FADEIN_VOLUMEBAR, false)

        mVolumeShown = true

        if(volume > Constants.VOLUME_MUTE) {
            mVolumeHandler!!.postDelayed({
                Animations.fade(this@MainActivity, volume_panel, Constants.FADEOUT_VOLUMEBAR, true)
                mVolumeShown = false
            }, Constants.HIDE_VOLUMEBAR)
        }

        setVolumeViews(volume)
        if(mBound) mMusicService.setVolume(volume)
    }

    override fun singlePress() {
        if(mDimmer.isDimmedDown()) mDimmer.up()
        else {
            // Should init the Voice Recognition
        }
    }

    override fun longPress() {
        if(mDimmer.isDimmedDown()) mDimmer.up()

        if(mButtonHandler == null){
            mButtonHandler = Handler()
        }

        if(mMusicService.getPlayerState() != MediaPlayerStates.IDLE)
            Animations.fade(this@MainActivity, shuffle_btn, Constants.FADEIN_SHUFFLE, !mButtonShown)
        Animations.fade(this@MainActivity, settings_btn, Constants.FADEIN_BUTTONS, mButtonShown)
        Animations.fade(this@MainActivity, repeat_btn, Constants.FADEIN_BUTTONS, mButtonShown)

        if(mButtonShown) {
            mButtonShown = false
            mButtonHandler!!.removeCallbacksAndMessages(null)
            return
        }

        mButtonShown = true
        mButtonHandler!!.postDelayed(
                {
                    Animations.fade(this@MainActivity, settings_btn, Constants.FADEOUT_BUTTONS, mButtonShown)
                    Animations.fade(this@MainActivity, repeat_btn, Constants.FADEOUT_BUTTONS, mButtonShown)
                    if(mMusicService.getPlayerState() != MediaPlayerStates.IDLE)
                        Animations.fade(this@MainActivity, shuffle_btn, Constants.FADEOUT_SHUFFLE, !mButtonShown)
                    mButtonShown = false
                },Constants.HIDE_BUTTONS)
    }

    override fun doublePress() {
        if(mDimmer.isDimmedDown()) mDimmer.up()

        if(mEditHandler == null){
            mEditHandler = Handler()
        }

        if(!mEditShown){
            Animations.fade(this@MainActivity, typeCommand_panel, Constants.FADEIN_COMMANDEDIT, false)
            Utils.enableKeyboard(this@MainActivity, true, command_edit)
            mEditShown = true
        } else mEditHandler!!.removeCallbacksAndMessages(null)

        mEditHandler!!.postDelayed(
                {
                    Animations.fade(this@MainActivity, typeCommand_panel, Constants.FADEOUT_COMMANDEDIT, true)
                    Utils.enableKeyboard(this@MainActivity, false, command_edit)
                    command_edit.setText("")
                    mEditShown = false
                }, Constants.HIDE_COMMANDEDIT)
    }

    /*** Media Player response functions ***/
    override fun stateChanged(state: MediaPlayerStates) {
        Animations.stopBlink()
        when(state){
            MediaPlayerStates.STARTED -> {
                Animations.fade(this@MainActivity, music_panel, Constants.FADEIN_MUSICS, false)
                Animations.fade(this@MainActivity, shuffle_btn, Constants.FADEIN_SHUFFLE, false)
            }
            MediaPlayerStates.PAUSED -> {
                Animations.blink(this@MainActivity, music_panel, Constants.BLINK_PERIOD)
            }
            else -> {}
        }
    }

    override fun musicChanged(curr: Music, prev: Music, next: Music) {
        currentSong_txt.text = curr.name
        if(curr.name == prev.name){
            prevSong_txt.visibility = View.GONE
            nextSong_txt.visibility = View.GONE
        }else {
            prevSong_txt.visibility = View.VISIBLE
            nextSong_txt.visibility = View.VISIBLE
            prevSong_txt.text = prev.name
            nextSong_txt.text = next.name
        }
    }

    /*** Request permission Callback ***/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            Permissions.READ_EXTERNAL_STORAGE_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    MusicLoader.loadAllMusic(this@MainActivity)
                } else {
                    this.finishAndRemoveTask() //Can't proceed without this permission
                }
                return
            }
        }
    }

    /*** Service bounding ***/
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicPlayerService.MusicPlayerBinder
            mMusicService = binder.getService()
            mMusicService.setMainActivity(this@MainActivity)

            setAI()

            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    /*** Back button action ***/
    override fun onBackPressed() {
        return
    }

    /*** Activity overridings ***/
    override fun onStart() {
        super.onStart()
        Intent(this, MusicPlayerService::class.java).also { intent ->
            this@MainActivity.startService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE) // Binds to local service
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection) //Unbinds from local service
        mBound = false
    }
}

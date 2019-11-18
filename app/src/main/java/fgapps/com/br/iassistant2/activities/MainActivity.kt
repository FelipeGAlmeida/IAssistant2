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
import android.util.Log
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
import fgapps.com.br.iassistant2.gestures.GestureController
import fgapps.com.br.iassistant2.music.Music
import fgapps.com.br.iassistant2.music.MusicLoader
import fgapps.com.br.iassistant2.music.MusicPlayerService
import fgapps.com.br.iassistant2.utils.Animations
import fgapps.com.br.iassistant2.defines.MediaPlayerStates
import fgapps.com.br.iassistant2.interfaces.*
import fgapps.com.br.iassistant2.utils.Dimmer
import fgapps.com.br.iassistant2.utils.Permissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
                    MusicChangeListener,
                    VolumeChangeListener,
                    ShowButtonsListener,
                    MediaPlayerListener,
                    TouchListener {

    /*** Variables ***/
    private lateinit var mMusicService: MusicPlayerService
    private var mBound: Boolean = false

    private lateinit var mDimmer: Dimmer

    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var mGestureController: GestureController

    private lateinit var background: ImageView
    fun getAppWidth() = background.width
    fun getAppHeight() = background.height

    private var mVolumeHandler: Handler? = null
    private var mVolumeShown: Boolean = false
    private var mButtonHandler: Handler? = null
    private var mButtonShown: Boolean = false

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
        background = background_img
        Glide.with(this)
                .load(R.drawable.back_anim1)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        Handler().postDelayed({
                            Animations.fade(this@MainActivity, splashscreen_panel, 800, true)
                            if(Permissions.checkPermission(this@MainActivity,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Permissions.READ_EXTERNAL_STORAGE_CODE)) loadMusics()
                        }, 800)
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
        settings_btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
            }

        })

        repeat_btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                if(mBound)
                    if(mMusicService.isPlaying()) mMusicService.pause()
                    else mMusicService.play()
            }

        })
    }

    private fun setVolumeViews(volume: Float){
        volume_txt.text = ((volume*10).toInt()*10).toString()

        val vol_int = (volume * getAppHeight()).toInt()
        when(vol_int == 0){
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
        lp.height = vol_int + 50
        lp.width = (getAppWidth()*0.1).toInt()
        volume_bar.layoutParams = lp
    }

    private fun setDimmer(){
        mDimmer = Dimmer(this@MainActivity)
        mDimmer.init()
    }

    private fun loadMusics(){
        val musics = MusicLoader.loadAllMusic(this)
        for (music in musics) {
            Log.v("MUSICS", "music: ${music.name}")
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    /*** Gesture controller response functions ***/
    override fun nextMusic() {
        if(mBound) mMusicService.next()
    }

    override fun prevMusic() {
        if(mBound) mMusicService.prev()
    }

    override fun volumeChange(volume: Float) {
        if(mVolumeHandler == null){
            mVolumeHandler = Handler()
        }

        if(mVolumeShown) mVolumeHandler!!.removeCallbacksAndMessages(null)
        else Animations.fade(this@MainActivity, volume_panel, 300, false)

        mVolumeShown = true

        if(volume > 0) {
            mVolumeHandler!!.postDelayed({
                Animations.fade(this@MainActivity, volume_panel, 500, true)
                mVolumeShown = false
            }, 1200)
        }

        setVolumeViews(volume)
        if(mBound) mMusicService.setVolume(volume)
    }

    override fun showButtons() {
        if(mButtonHandler == null){
            mButtonHandler = Handler()
        }

        Animations.fade(this@MainActivity, settings_btn, 300, mButtonShown)
        Animations.fade(this@MainActivity, repeat_btn, 300, mButtonShown)

        if(mButtonShown) {
            mButtonShown = false
            mButtonHandler!!.removeCallbacksAndMessages(null)
            return
        }

        mButtonShown = true
        mButtonHandler!!.postDelayed(
                {
                    Animations.fade(this@MainActivity, settings_btn, 300, mButtonShown)
                    Animations.fade(this@MainActivity, repeat_btn, 300, mButtonShown)
                    mButtonShown = false
                },3200)
    }

    override fun singlePress() {
        if(mDimmer.isDimmeredDown()) mDimmer.up()
        else mDimmer.down()
    }

    override fun longPress() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /*** Media Player response functions ***/

    override fun stateChanged(state: MediaPlayerStates) {
        Animations.stopBlink()
        when(state){
            MediaPlayerStates.PREPARING -> {}
            MediaPlayerStates.STARTED -> {
                Animations.fade(this@MainActivity, music_panel, 500, false)
            }
            MediaPlayerStates.PAUSED -> {
                Animations.blink(this@MainActivity, music_panel, 800)
            }
            MediaPlayerStates.IDLE -> {}

        }
    }

    override fun musicChanged(curr: Music, prev: Music, next: Music) {
        prevSong_txt.text = prev.name
        currentSong_txt.text = curr.name
        nextSong_txt.text = next.name
    }

    /*** Request permission Callback ***/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            Permissions.READ_EXTERNAL_STORAGE_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    loadMusics()
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

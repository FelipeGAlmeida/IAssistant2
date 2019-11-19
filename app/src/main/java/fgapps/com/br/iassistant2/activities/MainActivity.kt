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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import fgapps.com.br.iassistant2.R
import fgapps.com.br.iassistant2.defines.Dictionary
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
import java.text.Normalizer

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

        Dictionary.init()
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
        if(mDimmer.isDimmeredDown()) mDimmer.up()

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
        else {
            val s = test_edit.text.toString()

            checkAction(s)
        }
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


    ////////////////// TEST

    fun getPayload(words: MutableList<String>): String{
        var payload = ""
        for(word in words)
            payload += "$word "
        return payload
    }

    fun getKeyCommandOf(list: MutableMap<String, ArrayList<String>>, words: MutableList<String>): String?{
        for(word in words) {
            for (dicts in list) {
                for (dict in dicts.value) {
                    if (word.contains(dict)) {
                        words.remove(word)
                        return dicts.key
                    }
                }
            }
        }
        return null
    }

    private fun checkAction(raw_command: String) {
        if(raw_command.isEmpty()) return

        var command = Normalizer.normalize(raw_command.replace("é","ehh"), Normalizer.Form.NFD)
                .replace(Regex("[^\\p{ASCII}]"), "")
                .toLowerCase()

        val words = command.split(" ").toMutableList()

        val key_verb = getKeyCommandOf(Dictionary.actions, words)
        if(key_verb != null){
            analyseVerb(key_verb, words)
            return
        }

        val key_comp = getKeyCommandOf(Dictionary.complements, words)
        if(key_comp != null){
            analyseComplement(key_comp, null, words)
            return
        }

        val key_extra = getKeyCommandOf(Dictionary.extras, words)
        if(key_extra != null){
            analyseExtra(key_extra, null, null)
            return
        }
    }

    fun analyseVerb(key_verb: String, words: MutableList<String>): String? {

        val key_comp = getKeyCommandOf(Dictionary.complements, words)
        if(key_comp != null){
            analyseComplement(key_comp, key_verb, words)
            return null
        }

        if(words.size == 1) { // Extras is just a word of command, otherwise, is not extras
            val key_extra = getKeyCommandOf(Dictionary.extras, words)
            if (key_extra != null) {
                analyseExtra(key_extra, key_verb, null)
                return null
            }
        }

        when (key_verb) {
            Dictionary.play -> {
                if(words.isEmpty()) { // If this is just a PLAY command
                    if (mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                            !mMusicService.isPlaying()) { // if is paused, play again
                        Toast.makeText(this, "WILL UNPAUSE", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "WHAT TO PLAY?", Toast.LENGTH_LONG).show()
                    }
                } else{
                    val pld = getPayload(words)
                    Toast.makeText(this, "WILL PLAY \"$pld\"", Toast.LENGTH_LONG).show()
                }
            }
            Dictionary.pause -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                        mMusicService.isPlaying()){
                    Toast.makeText(this, "WILL PAUSE", Toast.LENGTH_LONG).show()
                }
            }
            Dictionary.next -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    Toast.makeText(this, "WILL GO TO NEXT", Toast.LENGTH_LONG).show()
                }
            }
            Dictionary.prev -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    Toast.makeText(this, "WILL GO TO PREV", Toast.LENGTH_LONG).show()
                }
            }
        }
        return null
    }

    fun analyseComplement(key_comp: String, key_verb: String?, words: MutableList<String>): String? {

        if(words.size <= 1) { // Extras is just a word of command, otherwise, is not extras
            val key_extra = getKeyCommandOf(Dictionary.extras, words)
            if (key_extra != null) {
                analyseExtra(key_extra, key_verb, key_comp)
                return null
            }
        }

        when (key_comp) {
            Dictionary.music -> {
                when(key_verb){
                    Dictionary.play -> {
                        if(words.size > 0) { // Still has more words to analyse
                            val pld = getPayload(words) // May be a Music name
                            Toast.makeText(this, "WILL PLAY \"$pld\"", Toast.LENGTH_LONG).show()
                        } else { // If there are no more words
                            if(mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                                    !mMusicService.isPlaying()){
                                Toast.makeText(this, "WILL UNPAUSE", Toast.LENGTH_LONG).show()
                            }
                            // Missing information of what to play
                            Toast.makeText(this, "WHAT TO PLAY?", Toast.LENGTH_LONG).show()
                        }

                    }
                    Dictionary.pause -> {
                        if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                                mMusicService.isPlaying()){
                            Toast.makeText(this, "WILL PAUSE", Toast.LENGTH_LONG).show()
                        }
                    }
                    Dictionary.next -> {
                        if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                            Toast.makeText(this, "WILL GO TO NEXT", Toast.LENGTH_LONG).show()
                        }
                    }
                    Dictionary.prev -> {
                        if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                            Toast.makeText(this, "WILL GO TO PREV", Toast.LENGTH_LONG).show()
                        }
                    }
                    null -> {
                        // May be a Music name
                        val pld = getPayload(words)
                        Toast.makeText(this, "WILL PLAY \"$pld\"", Toast.LENGTH_LONG).show()
                    }
                }
            }
            Dictionary.folder -> {
                // May be a Music name
                val pld = getPayload(words)
                Toast.makeText(this, "WILL PLAY FOLDER\"$pld\"", Toast.LENGTH_LONG).show()
            }
            Dictionary.time -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                        mMusicService.isPlaying()){
                    //pause
                }
                Toast.makeText(this, "SAY THE TIME", Toast.LENGTH_LONG).show()
            }
        }
        return null
    }

    fun analyseExtra(key_extra: String, key_verb: String?, key_comp: String?): String? {
        when (key_extra) {
            Dictionary.all -> {
                Toast.makeText(this, "WILL PLAY ALL THE MUSICS", Toast.LENGTH_LONG).show()
            }
            Dictionary.next -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    Toast.makeText(this, "WILL GO TO NEXT", Toast.LENGTH_LONG).show()
                }
            }
            Dictionary.prev -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    Toast.makeText(this, "WILL GO TO PREV", Toast.LENGTH_LONG).show()
                }
            }
            Dictionary.time -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                        mMusicService.isPlaying()){
                    //pause
                }
                Toast.makeText(this, "SAY THE TIME", Toast.LENGTH_LONG).show()
            }
            Dictionary.play -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                        !mMusicService.isPlaying()){
                    Toast.makeText(this, "WILL UNPAUSE", Toast.LENGTH_LONG).show()
                }
            }
        }
        return null
    }
}

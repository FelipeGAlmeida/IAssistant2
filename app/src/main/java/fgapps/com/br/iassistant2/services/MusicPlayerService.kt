package fgapps.com.br.iassistant2.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fgapps.com.br.iassistant2.activities.MainActivity
import fgapps.com.br.iassistant2.defines.MediaPlayerStates
import fgapps.com.br.iassistant2.music.Music
import fgapps.com.br.iassistant2.utils.Utils
import kotlin.collections.ArrayList
import android.graphics.PixelFormat
import android.view.*
import android.widget.ImageButton
import fgapps.com.br.iassistant2.R


class MusicPlayerService : Service(),
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener{

    private val binder = MusicPlayerBinder()

    private var windowManager: WindowManager? = null
    private var floatingControl: View? = null

    private lateinit var mMediaPlayer: MediaPlayer
    private var mMainActivity: MainActivity? = null
    private var mPrevState =MediaPlayerStates.IDLE
    private var mState = MediaPlayerStates.IDLE
    private var mShuffle = false

    private var mPlaylist = ArrayList<Music>()
    private var mPlaylist_bck = ArrayList<Music>()
    private var music_idx = 0

    private var mVolume = 50F

    override fun onCreate() {
        super.onCreate()
        mMediaPlayer = MediaPlayer()
        initMusicPlayer()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initFloatingControl(enable: Boolean) {
        if(mPlaylist.size == 0) return
        if(enable) {
            if (floatingControl == null) {
                windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                floatingControl = inflater.inflate(R.layout.floating_control, null, true)

                (floatingControl?.findViewById(R.id.widgetSpeak_btn) as ImageButton).setOnClickListener {
                    VoiceService.instance?.listen()
                }
                (floatingControl?.findViewById(R.id.widgetClose_btn) as ImageButton).setOnClickListener {
                    initFloatingControl(false)
                }
                (floatingControl?.findViewById(R.id.widgetPrev_btn) as ImageButton).setOnClickListener {
                    prev()
                }
                (floatingControl?.findViewById(R.id.widgetPP_btn) as ImageButton).setOnClickListener {
                    if (isPlaying()) pause()
                    else play()
                }
                (floatingControl?.findViewById(R.id.widgetNext_btn) as ImageButton).setOnClickListener {
                    next()
                }

                val params = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT)

                params.gravity = Gravity.TOP or Gravity.LEFT
                params.x = 30
                params.y = 200

                windowManager!!.addView(floatingControl, params)

                try {
                    floatingControl!!.setOnTouchListener(object : View.OnTouchListener {
                        private var initialX: Int = 0
                        private var initialY: Int = 0
                        private var initialTouchX: Float = 0.toFloat()
                        private var initialTouchY: Float = 0.toFloat()

                        override fun onTouch(v: View, event: MotionEvent): Boolean {
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    initialX = params.x
                                    initialY = params.y
                                    initialTouchX = event.rawX
                                    initialTouchY = event.rawY
                                }
                                MotionEvent.ACTION_UP -> {
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                                    windowManager!!.updateViewLayout(floatingControl, params)
                                }
                            }
                            return false
                        }
                    })
                } catch (e: Exception) {
                    // TODO: handle exception
                }
            }
        } else {
            if (floatingControl != null){
                windowManager?.removeView(floatingControl)
                floatingControl = null
            }
        }
    }

    private fun initMusicPlayer() {
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
        mMediaPlayer.setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())

        mMediaPlayer.setOnPreparedListener(this)
        mMediaPlayer.setOnCompletionListener(this)
        mMediaPlayer.setOnErrorListener(this)
    }

    fun play(idx_toPlay: Int?) {

        if(mPlaylist.size == 0) return //If there's nothing to PLAY, return

        if(idx_toPlay != null) { // Plays a specific MUSIC
            music_idx = idx_toPlay
        }

        mMediaPlayer.reset()
        setState(MediaPlayerStates.IDLE)

        if (music_idx >= mPlaylist.size || music_idx < 0) music_idx = 0
        val playSong: Music = mPlaylist[music_idx] //Get the MUSIC
        val currSong = playSong.id // Get the ID to take the URI
        val trackUri = ContentUris.withAppendedId( // Get the URI to PLAY the correct file
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong)

        try {
            mMediaPlayer.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }

        mMediaPlayer.prepareAsync()
        setState(MediaPlayerStates.PREPARING)

    }

    fun setPlaylist(playlist: ArrayList<Music>) {
        mPlaylist = playlist
        mShuffle = false
        setState(MediaPlayerStates.IDLE) // If we set the Playlist, we need to restart the player
    }

    fun getPlaylist(): ArrayList<Music>{
        return mPlaylist
    }

    fun getCurrentMusicIndex(): Int{
        return music_idx
    }

    fun addToPlaylist(playlist: ArrayList<Music>) {
        for(music in playlist){
            if(!mPlaylist.contains(music)){
                mPlaylist.add(music)
            }
        }
        if(mShuffle){
            mShuffle = false
            shuffle() // Re-shuffle with the new musics added
        }
        notifyMusicChanges()
    }

    fun checkIndexInPlaylist(music: Music): Int{
        return mPlaylist.indexOf(music)
    }

    /*** Player controls ***/
    fun play() {
        if(mPlaylist.size == 0) return
        if(mState == MediaPlayerStates.PAUSED) {
            mMediaPlayer.start()
            setState(MediaPlayerStates.STARTED)
            return
        }
        play(null)
    }

    fun pause() {
        if(mPlaylist.size == 0) return
        if(mState != MediaPlayerStates.STARTED) return

        mMediaPlayer.pause()
        setState(MediaPlayerStates.PAUSED)
    }

    fun stop() {
        if(mPlaylist.size == 0) return
        mMediaPlayer.stop()
        mMediaPlayer.reset()
        setState(MediaPlayerStates.IDLE)
    }

    fun next() {
        if(mPlaylist.size == 0) return
        music_idx++
        if(music_idx >= mPlaylist.size) music_idx = 0
        play(null)
    }

    fun prev() {
        if(mPlaylist.size == 0) return
        music_idx--
        if(music_idx < 0) music_idx = mPlaylist.size-1
        play(null)
    }

    fun setVolume(value: Float) {
        mVolume = value
        mMediaPlayer.setVolume(mVolume, mVolume)
    }

    fun mixSoundRequest(willPlay: Boolean){
        if(willPlay){
            mMediaPlayer.setVolume(0.2F, 0.2F)
            return
        }
        mMediaPlayer.setVolume(mVolume, mVolume)
    }

    fun shuffle(): Boolean{
        val music = mPlaylist[music_idx]
        mShuffle = !mShuffle
        music_idx = when(mShuffle){
            true -> {
                mPlaylist_bck.addAll(mPlaylist)
                mPlaylist.shuffle()
                mPlaylist.remove(music)
                mPlaylist.add(0, music)
                0
            }
            false ->{
                mPlaylist.clear()
                mPlaylist.addAll(mPlaylist_bck)
                mPlaylist.indexOf(music)
            }
        }
        notifyMusicChanges()
        return mShuffle
    }

    fun isPlaying(): Boolean {
        return mMediaPlayer.isPlaying
    }

    fun getPlayerPreviousState(): MediaPlayerStates {
        return mPrevState
    }

    fun getPlayerState(): MediaPlayerStates {
        return mState
    }

    /*** Media Player service functions ***/
    override fun onPrepared(p0: MediaPlayer?) {
        mMediaPlayer.start()
        setState(MediaPlayerStates.STARTED)
        notifyMusicChanges()
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if(mMediaPlayer.currentPosition > 0){
            next()
        }
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        mMediaPlayer.reset()
        setState(MediaPlayerStates.IDLE)
        return false
    }

    private fun setState(state: MediaPlayerStates) {
        mPrevState = mState
        mState = state
        if(mMainActivity != null)
            mMainActivity!!.stateChanged(mState)
    }

    private fun notifyMusicChanges() {
        if (mMainActivity != null) {
            val indexes = Utils.boundMusicIndexes(mPlaylist.size, music_idx)
            mMainActivity!!.musicChanged(mPlaylist[indexes[0]], mPlaylist[indexes[1]], mPlaylist[indexes[2]])
        }
    }

    /*** Binder service code ***/
    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    fun setMainActivity(mainActivity: MainActivity) {
        mMainActivity = mainActivity
    }

    fun createNotification() {
        val notificationIntent = Intent(applicationContext, MusicPlayerService::class.java)
        val pendingIntent = PendingIntent.getService(applicationContext, 0, notificationIntent, 0)

        val builder = NotificationCompat.Builder(applicationContext, "ID")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_assistant)
                .setContentTitle("CONTENT TILE")
                .setContentText("CONTENT TEXT")
                .setTicker("THIS IS THE TICKER")
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("CONTENT TEXT OF THE NOTIFICATION"))
                //.setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("ID", "channel_name", importance).apply {
                description = "description"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(2, builder.build())
        }

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(100, builder.build())
    }

    override fun onUnbind(intent: Intent?): Boolean {
        initFloatingControl(false)
        return super.onUnbind(intent)
    }
}

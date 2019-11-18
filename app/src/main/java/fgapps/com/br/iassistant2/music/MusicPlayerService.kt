package fgapps.com.br.iassistant2.music

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.content.ContentUris
import android.util.Log
import fgapps.com.br.iassistant2.activities.MainActivity
import fgapps.com.br.iassistant2.defines.MediaPlayerStates
import fgapps.com.br.iassistant2.utils.Utils

class MusicPlayerService : Service(),
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener{

    private val binder = MusicPlayerBinder()

    private lateinit var mMediaPlayer: MediaPlayer
    private var mMainActivity: MainActivity? = null
    private var mState = MediaPlayerStates.IDLE

    private var playList = ArrayList<Music>()
    private var playlist_bck = ArrayList<Music>()
    private var music_idx = 0

    override fun onCreate() {
        super.onCreate()
        mMediaPlayer = MediaPlayer()
        initMusicPlayer()
    }

    fun initMusicPlayer(){
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
        mMediaPlayer.setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())

        mMediaPlayer.setOnPreparedListener(this)
        mMediaPlayer.setOnCompletionListener(this)
        mMediaPlayer.setOnErrorListener(this)
    }

    fun play(music: Music?){

        if(playList.size == 0) return //If there's nothing to play, return

        if(music != null) { // Plays a specific music
            music_idx = playList.indexOf(music)
        }

        mMediaPlayer.reset()
        setState(MediaPlayerStates.IDLE)

        if (music_idx >= playList.size) music_idx = 0
        val playSong: Music = playList[music_idx] //Get the music
        val currSong = playSong.id // Get the ID to take the URI
        val trackUri = ContentUris.withAppendedId( // Get the URI to play the correct file
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong)

        try {
            mMediaPlayer.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }

        mMediaPlayer.prepareAsync()
        setState(MediaPlayerStates.PREPARING)

    }

    /*** Player controls ***/

    fun play(){
        if(playList.size == 0){
            playList = MusicLoader.loadAllMusic(this)
            play(null)
            return
        }
        mMediaPlayer.start()
        setState(MediaPlayerStates.STARTED)
    }

    fun pause(){
        mMediaPlayer.pause()
        setState(MediaPlayerStates.PAUSED)
    }

    fun stop(){
        mMediaPlayer.stop()
        mMediaPlayer.reset()
        setState(MediaPlayerStates.IDLE)
    }

    fun next(){
        music_idx++
        if(music_idx >= playList.size) music_idx = 0
        play(null)
    }

    fun prev(){
        music_idx--
        if(music_idx < 0) music_idx = playList.size-1
        play(null)
    }

    fun setVolume(value: Float){
        mMediaPlayer.setVolume(value, value)
    }

    fun isPlaying(): Boolean{
        return mMediaPlayer.isPlaying
    }

    /*** Media Player service functions ***/

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onPrepared(p0: MediaPlayer?) {
        mMediaPlayer.start()
        setState(MediaPlayerStates.STARTED)
        if(mMainActivity != null) {
            var indexes = Utils.boundMusicIndexes(playList.size, music_idx)
            mMainActivity!!.musicChanged(playList[indexes[0]], playList[indexes[1]], playList[indexes[2]])
        }
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

    fun setState(state: MediaPlayerStates){
        mState = state
        if(mMainActivity != null)
            mMainActivity!!.stateChanged(mState)
    }

    /*** Binder service code ***/
    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    fun setMainActivity(mainActivity: MainActivity){
        mMainActivity = mainActivity
    }
}

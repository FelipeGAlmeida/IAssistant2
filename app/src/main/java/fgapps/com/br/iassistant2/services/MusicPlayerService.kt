package fgapps.com.br.iassistant2.services

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
import fgapps.com.br.iassistant2.music.Music
import fgapps.com.br.iassistant2.utils.Utils

class MusicPlayerService : Service(),
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener{

    private val binder = MusicPlayerBinder()

    private lateinit var mMediaPlayer: MediaPlayer
    private var mMainActivity: MainActivity? = null
    private var mState = MediaPlayerStates.IDLE

    private var mPlaylist = ArrayList<Music>()
    //private var playlist_bck = ArrayList<Music>()
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

        if(mPlaylist.size == 0) return //If there's nothing to PLAY, return

        if(music != null) { // Plays a specific MUSIC
            music_idx = mPlaylist.indexOf(music)
        }

        mMediaPlayer.reset()
        setState(MediaPlayerStates.IDLE)

        if (music_idx >= mPlaylist.size) music_idx = 0
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

    fun setPlaylist(playlist: ArrayList<Music>){
        mPlaylist = playlist
    }

    /*** Player controls ***/

    fun play(): Boolean {
        if(mPlaylist.size == 0){
            return false
        }
        if(mState == MediaPlayerStates.PAUSED) {
            mMediaPlayer.start()
            setState(MediaPlayerStates.STARTED)
            return true
        }
        play(null)
        return true
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
        if(music_idx >= mPlaylist.size) music_idx = 0
        play(null)
    }

    fun prev(){
        music_idx--
        if(music_idx < 0) music_idx = mPlaylist.size-1
        play(null)
    }

    fun setVolume(value: Float){
        mMediaPlayer.setVolume(value, value)
    }

    fun isPlaying(): Boolean {
        return mMediaPlayer.isPlaying
    }

    fun getPlayerState(): MediaPlayerStates {
        return mState
    }

    /*** Media Player service functions ***/

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onPrepared(p0: MediaPlayer?) {
        mMediaPlayer.start()
        setState(MediaPlayerStates.STARTED)
        if(mMainActivity != null) {
            val indexes = Utils.boundMusicIndexes(mPlaylist.size, music_idx)
            mMainActivity!!.musicChanged(mPlaylist[indexes[0]], mPlaylist[indexes[1]], mPlaylist[indexes[2]])
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

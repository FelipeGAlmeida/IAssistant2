package fgapps.com.br.iassistant2.services

import android.widget.Toast
import fgapps.com.br.iassistant2.activities.MainActivity
import fgapps.com.br.iassistant2.defines.Dictionary
import fgapps.com.br.iassistant2.defines.MediaPlayerStates
import fgapps.com.br.iassistant2.music.MusicLoader
import fgapps.com.br.iassistant2.utils.Utils

class AIService(mainActivity: MainActivity, musicService: MusicPlayerService){

    private val mActivity = mainActivity
    private val mMusicService = musicService

    private var isWaitingPayload = false
    private var isFolderFromPayload = false
    private var isJumpingPayload = false
    private var shouldAddPayload = false

    fun checkCommand(raw_command: String) {
        if(raw_command.isEmpty()) return

        val command = Utils.normalizeStrings(raw_command, true, true, false)

        if(isWaitingPayload){
            analysePayload(command, isFolderFromPayload, shouldAddPayload, isJumpingPayload)
            isWaitingPayload = false
            isFolderFromPayload = false
            isJumpingPayload = false
            shouldAddPayload = false
            return
        }

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
            analyseExtra(key_extra)
            return
        }
    }

    private fun analyseVerb(key_verb: String, words: MutableList<String>){

        val key_comp = getKeyCommandOf(Dictionary.complements, words)
        if(key_comp != null){
            analyseComplement(key_comp, key_verb, words)
            return
        }

        if(words.size == 1) { // Extras is just a word of command, otherwise, is not extras
            val key_extra = getKeyCommandOf(Dictionary.extras, words)
            if (key_extra != null) {
                analyseExtra(key_extra)
                return
            }
        }

        when (key_verb) {
            Dictionary.GOTO -> {
                if(words.isEmpty()){
                    Toast.makeText(mActivity, "WHICH MUSIC YOU WANT TO JUMP TO?", Toast.LENGTH_LONG).show()
                    isWaitingPayload = true
                    isJumpingPayload = true
                } else {
                    playSpecificSong(getPayload(words))
                }
            }
            Dictionary.PLAY -> {
                if(words.isEmpty()) { // If verb is just a PLAY command
                    if (mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                            !mMusicService.isPlaying()) { // if is paused, PLAY again
                        mMusicService.play()
                    } else {
                        Toast.makeText(mActivity, "WHAT TO PLAY?", Toast.LENGTH_LONG).show()
                        isWaitingPayload = true
                    }
                } else{
                    addMusicsFromPayloadAndPlay(getPayload(words), false, false)
                }
            }
            Dictionary.PAUSE -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                        mMusicService.isPlaying()){
                    mMusicService.pause()
                }
            }
            Dictionary.ADD -> {
                if(words.isEmpty()){
                    Toast.makeText(mActivity, "WHAT TO ADD?", Toast.LENGTH_LONG).show()
                    isWaitingPayload = true
                    shouldAddPayload = true
                } else {
                    addMusicsFromPayloadAndPlay(getPayload(words), false, true)
                }
            }
            Dictionary.NEXT -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    mMusicService.next()
                }
            }
            Dictionary.PREV -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    mMusicService.prev()
                }
            }
        }
        return
    }

    private fun analyseComplement(key_comp: String, key_verb: String?, words: MutableList<String>) {

        if(words.size <= 1) { // Extras is just a word of command, otherwise, is not extras
            val key_extra = getKeyCommandOf(Dictionary.extras, words)
            if (key_extra != null) {
                analyseExtra(key_extra)
                return
            }
        }

        when (key_comp) {
            Dictionary.MUSIC -> {
                when(key_verb){
                    Dictionary.GOTO -> {
                        if(words.isEmpty()){
                            Toast.makeText(mActivity, "WHICH MUSIC YOU WANT TO JUMP TO?", Toast.LENGTH_LONG).show()
                            isWaitingPayload = true
                            isJumpingPayload = true
                        } else {
                            playSpecificSong(getPayload(words))
                        }
                    }
                    Dictionary.PLAY -> {
                        if(words.size > 0) { // Still has more words to analyse
                            addMusicsFromPayloadAndPlay(getPayload(words), false, false)
                        } else { // If there are no more words
                            if(mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                                    !mMusicService.isPlaying()){
                                mMusicService.play()
                            }
                            Toast.makeText(mActivity, "WHAT TO PLAY?", Toast.LENGTH_LONG).show()
                            isWaitingPayload = true
                        }

                    }
                    Dictionary.PAUSE -> {
                        if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                                mMusicService.isPlaying()){
                            mMusicService.pause()
                        }
                    }
                    Dictionary.ADD -> {
                        if(words.isEmpty()){
                            Toast.makeText(mActivity, "WHAT TO ADD?", Toast.LENGTH_LONG).show()
                            isWaitingPayload = true
                            shouldAddPayload = true
                        } else {
                            addMusicsFromPayloadAndPlay(getPayload(words), false, true)
                        }
                    }
                    Dictionary.NEXT -> {
                        if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                            mMusicService.next()
                        }
                    }
                    Dictionary.PREV -> {
                        if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                            mMusicService.prev()
                        }
                    }
                    null -> {
                        // May be a Music name
                        addMusicsFromPayloadAndPlay(getPayload(words), false, false)
                    }
                }
            }
            Dictionary.FOLDER -> {
                when(key_verb){
                    Dictionary.ADD -> {
                        if(words.isEmpty()){
                            Toast.makeText(mActivity, "WHICH FOLDER TO ADD?", Toast.LENGTH_LONG).show()
                            isWaitingPayload = true
                            isFolderFromPayload = true
                            shouldAddPayload = true
                        } else {
                            addMusicsFromPayloadAndPlay(getPayload(words), true, true)
                        }
                    }
                    else -> {
                        if(words.isEmpty()){
                            Toast.makeText(mActivity, "WHICH FOLDER TO PLAY?", Toast.LENGTH_LONG).show()
                            isWaitingPayload = true
                            isFolderFromPayload = true
                        } else {
                            addMusicsFromPayloadAndPlay(getPayload(words), true, false)
                        }
                    }
                }
            }
            Dictionary.TIME -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                        mMusicService.isPlaying()){
                    mMusicService.pause()
                }
                Toast.makeText(mActivity, Utils.getCurrentTime(), Toast.LENGTH_LONG).show()
            }
        }
        return
    }

    private fun analyseExtra(key_extra: String) {
        when (key_extra) {
            Dictionary.ALL -> {
                mMusicService.setPlaylist(MusicLoader.allMusic)
                mMusicService.play()
            }
            Dictionary.NEXT -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    mMusicService.next()
                }
            }
            Dictionary.PREV -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    mMusicService.prev()
                }
            }
            Dictionary.TIME -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                        mMusicService.isPlaying()){
                    mMusicService.pause()
                }
                Toast.makeText(mActivity, Utils.getCurrentTime(), Toast.LENGTH_LONG).show()
            }
            Dictionary.PLAY -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                        !mMusicService.isPlaying()){
                    mMusicService.play()
                }
            }
        }
        return
    }

    private fun analysePayload(payload: String, fromFolder: Boolean, shouldAdd: Boolean, isJump: Boolean){
        var mutablePayload = payload
        if(isJump){
            playSpecificSong(mutablePayload)
            return
        }

        var isFolder = fromFolder
        if(!isFolder) { //If is not from folder command, we check to be sure about to be
            val folder_keys = Dictionary.complements[Dictionary.FOLDER]
            for (dict in folder_keys!!) {
                if (mutablePayload.contains(dict)){
                    mutablePayload = mutablePayload.replace(dict, "").trim()
                    isFolder = true
                    break
                }
            }
        }

        addMusicsFromPayloadAndPlay(mutablePayload, isFolder, shouldAdd)
    }

    private fun playSpecificSong(payload: String){
        for(musicToGo in MusicLoader.getPlaylistFromPayload(payload, false)){
            val indexToGo = mMusicService.checkIndexInPlaylist(musicToGo)
            if(indexToGo >= 0){
                mMusicService.play(indexToGo)
                return
            }
        }
        // If no music was found
        Toast.makeText(mActivity, "MUSIC IS NOT IN THE PLAYLIST", Toast.LENGTH_LONG).show()
    }

    private fun addMusicsFromPayloadAndPlay(payload: String, isFolder: Boolean, shouldAdd: Boolean){
        if(!hasMusics()) return //Check if there are musics to be played

        val playlist = MusicLoader.getPlaylistFromPayload(payload, isFolder)
        if(!playlist.isEmpty()){
            if(shouldAdd) mMusicService.addToPlaylist(playlist)
            else {
                mMusicService.setPlaylist(playlist)
                mMusicService.play()
            }
        }
        else Toast.makeText(mActivity, "NOTHING MATCHES WHAT YOU WANT", Toast.LENGTH_LONG).show()
    }

    private fun getPayload(words: MutableList<String>): String {
        if(words.size == 1) return words[0]

        var payload = ""
        for(word in words)
            payload += "$word "
        return payload.trim()
    }

    private fun getKeyCommandOf(list: MutableMap<String, ArrayList<String>>, words: MutableList<String>): String? {
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

    private fun hasMusics(): Boolean{
        return when(MusicLoader.allMusic.size > 0){
            true -> true
            false -> {
                Toast.makeText(mActivity, "NO MUSICS ON THE PHONE", Toast.LENGTH_LONG).show()
                false
            }
        }
    }
}
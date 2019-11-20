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

    fun checkAction(raw_command: String) {
        if(raw_command.isEmpty()) return

        val command = Utils.normalizeStrings(raw_command, true, true, false)

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
            Dictionary.PLAY -> {
                if(words.isEmpty()) { // If verb is just a PLAY command
                    if (mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                            !mMusicService.isPlaying()) { // if is paused, PLAY again
                        mMusicService.play()
                    } else {
                        Toast.makeText(mActivity, "WHAT TO PLAY?", Toast.LENGTH_LONG).show()
                    }
                } else{
                    addMusicsFromPayloadandPlay(words, Dictionary.MUSIC, false)
                }
            }
            Dictionary.PAUSE -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                        mMusicService.isPlaying()){
                    mMusicService.pause()
                }
            }
            Dictionary.ADD -> {
                addMusicsFromPayloadandPlay(words, Dictionary.MUSIC, true)
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
                    Dictionary.PLAY -> {
                        if(words.size > 0) { // Still has more words to analyse
                            addMusicsFromPayloadandPlay(words, Dictionary.MUSIC, false)
                        } else { // If there are no more words
                            if(mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                                    !mMusicService.isPlaying()){
                                mMusicService.play()
                            }
                            // Missing information of what to PLAY
                            Toast.makeText(mActivity, "WHAT TO PLAY?", Toast.LENGTH_LONG).show()
                        }

                    }
                    Dictionary.PAUSE -> {
                        if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                                mMusicService.isPlaying()){
                            mMusicService.pause()
                        }
                    }
                    Dictionary.ADD -> {
                        addMusicsFromPayloadandPlay(words, Dictionary.MUSIC, true)
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
                        addMusicsFromPayloadandPlay(words, Dictionary.MUSIC, false)
                    }
                }
            }
            Dictionary.FOLDER -> {
                when(key_verb){
                    Dictionary.ADD -> {
                        addMusicsFromPayloadandPlay(words, Dictionary.FOLDER, true)
                    }
                    else -> {
                        addMusicsFromPayloadandPlay(words, Dictionary.FOLDER, false)
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

    private fun addMusicsFromPayloadandPlay(words: MutableList<String>, type: String, shouldAdd: Boolean){
        val pld = getPayload(words)
        val playlist = MusicLoader.getPlaylistFromPayload(pld, type)
        if(!playlist.isEmpty()){
            if(shouldAdd) mMusicService.addToPlaylist(playlist)
            else {
                mMusicService.setPlaylist(playlist)
                mMusicService.play()
            }
        }
        else Toast.makeText(mActivity, "NOTHING MATCHES WITH YOU WANT", Toast.LENGTH_LONG).show()
    }

    private fun getPayload(words: MutableList<String>): String {
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
}
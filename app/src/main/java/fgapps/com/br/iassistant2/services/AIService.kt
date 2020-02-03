package fgapps.com.br.iassistant2.services

import android.Manifest
import android.os.Handler
import android.util.Log
import fgapps.com.br.iassistant2.activities.MainActivity
import fgapps.com.br.iassistant2.defines.Dictionary
import fgapps.com.br.iassistant2.defines.MediaPlayerStates
import fgapps.com.br.iassistant2.defines.VoiceStates
import fgapps.com.br.iassistant2.interfaces.VoiceListener
import fgapps.com.br.iassistant2.music.MusicLoader
import fgapps.com.br.iassistant2.utils.Permissions
import fgapps.com.br.iassistant2.utils.ShPrefs
import fgapps.com.br.iassistant2.utils.Utils
import java.lang.NumberFormatException
import java.util.*

class AIService(mainActivity: MainActivity, musicService: MusicPlayerService): VoiceListener {

    private val mActivity = mainActivity
    private val mMusicService = musicService

    private var mVoice: VoiceService? = null

    private var isWaitingPayload = false
    private var isFolderFromPayload = false
    private var isJumpingPayload = false
    private var shouldAddPayload = false

    private var mCommand = ""
    private var mCommandExtras = -1

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

        mVoice?.speak("Comando não entendido, tente novamente", "Tente usar palavras chaves como \"OUVIR\", \"MÚSICA\", \"PASTA\" ...", false)
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
            Dictionary.LOAD -> {
                mVoice?.speak("É pra já", "processando", false)
                playPreviousPlaylist()
            }
            Dictionary.GOTO -> {
                if(words.isEmpty()){
                    mVoice?.speak("Qual música deseja tocar?", "Toque novamente e diga o nome da música", true)
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
                        //mMusicService.play()
                        mCommand = Dictionary.PLAY
                    } else {
                        mVoice?.speak("O que deseja ouvir?", "Toque e diga a música ou pasta que quer ouvir", true)
                        isWaitingPayload = true
                        return
                    }
                } else{
                    addMusicsFromPayloadAndPlay(getPayload(words), false, false)
                    return
                }
            }
            Dictionary.PAUSE -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                        mMusicService.isPlaying()){
                    //mMusicService.pause()
                    mCommand = Dictionary.PAUSE
                }
            }
            Dictionary.ADD -> {
                if(words.isEmpty()){
                    mVoice?.speak("O que deseja adicionar à playlist?", "Toque e diga a música ou pasta que quer adicionar", true)
                    isWaitingPayload = true
                    shouldAddPayload = true
                } else {
                    addMusicsFromPayloadAndPlay(getPayload(words), false, true)
                }
                return
            }
            Dictionary.NEXT -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    //mMusicService.next()
                    mCommand = Dictionary.NEXT
                }
            }
            Dictionary.PREV -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    //mMusicService.prev()
                    mCommand = Dictionary.PREV
                }
            }
        }
        runCommand(300)
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
                when(key_verb) {
                    Dictionary.LOAD -> {
                        mVoice?.speak("É pra já", "processando", false)
                        playPreviousPlaylist()
                    }
                    Dictionary.GOTO -> {
                        if (words.isEmpty()) {
                            mVoice?.speak("Qual música deseja tocar?", "Toque novamente e diga o nome da música que deseja tocar agora", true)
                            isWaitingPayload = true
                            isJumpingPayload = true
                        } else {
                            playSpecificSong(getPayload(words))
                        }
                        return
                    }
                    Dictionary.PLAY -> {
                        if (words.isEmpty()) { // If verb is just a PLAY command
                            if (mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                                    !mMusicService.isPlaying()) { // if is paused, PLAY again
                                //mMusicService.play()
                                mCommand = Dictionary.PLAY
                            } else {
                                mVoice?.speak("O que deseja ouvir?", "Toque e diga a música ou pasta que quer ouvir", true)
                                isWaitingPayload = true
                                return
                            }
                        } else {
                            addMusicsFromPayloadAndPlay(getPayload(words), false, false)
                            return
                        }
                    }
                    Dictionary.PAUSE -> {
                        if (mMusicService.getPlayerState() == MediaPlayerStates.STARTED &&
                                mMusicService.isPlaying()) {
                            //mMusicService.pause()
                            mCommand = Dictionary.PAUSE
                        }
                    }
                    Dictionary.ADD -> {
                        if (words.isEmpty()) {
                            mVoice?.speak("O que deseja adicionar à playlist?", "Toque e diga a música ou pasta que quer adicionar", true)
                            isWaitingPayload = true
                            shouldAddPayload = true
                            return
                        } else {
                            addMusicsFromPayloadAndPlay(getPayload(words), false, true)
                            return
                        }
                    }
                    Dictionary.NEXT -> {
                        if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                            //mMusicService.next()
                            mCommand = Dictionary.NEXT
                        }
                    }
                    Dictionary.PREV -> {
                        if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                            //mMusicService.prev()
                            mCommand = Dictionary.PREV
                        }
                    }
                    null -> {
                        // May be a Music name
                        addMusicsFromPayloadAndPlay(getPayload(words), false, false)
                        return
                    }
                }
            }
            Dictionary.FOLDER -> {
                when(key_verb){
                    Dictionary.ADD -> {
                        if(words.isEmpty()){
                            mVoice?.speak("Qual pasta deseja adicionar à playlist?", "Toque e diga a pasta que quer adicionar", true)
                            isWaitingPayload = true
                            isFolderFromPayload = true
                            shouldAddPayload = true
                            return
                        } else {
                            addMusicsFromPayloadAndPlay(getPayload(words), true, true)
                            return
                        }
                    }
                    else -> {
                        if(words.isEmpty()){
                            mVoice?.speak("Qual pasta deseja ouvir?", "Toque e diga a pasta que quer ouvir", true)
                            isWaitingPayload = true
                            isFolderFromPayload = true
                            return
                        } else {
                            addMusicsFromPayloadAndPlay(getPayload(words), true, false)
                            return
                        }
                    }
                }
            }
            Dictionary.TIME -> {
                mVoice?.speak(Utils.getCurrentTime(true), "", false)
                return
            }
        }
        runCommand(300)
        return
    }

    private fun analyseExtra(key_extra: String) {
        when (key_extra) {
            Dictionary.ALL -> {
                if(!hasStoragePermission(mActivity)) return

                mMusicService.setPlaylist(MusicLoader.allMusic)
                //mMusicService.play()
                mCommand = Dictionary.PLAY
            }
            Dictionary.NEXT -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    //mMusicService.next()
                    mCommand = Dictionary.NEXT
                }
            }
            Dictionary.PREV -> {
                if (mMusicService.getPlayerState() != MediaPlayerStates.IDLE) {
                    //mMusicService.prev()
                    mCommand = Dictionary.PREV
                }
            }
            Dictionary.TIME -> {
                mVoice?.speak(Utils.getCurrentTime(true), "", false)
                return
            }
            Dictionary.PLAY -> {
                if(mMusicService.getPlayerState() == MediaPlayerStates.PAUSED &&
                        !mMusicService.isPlaying()){
                    //mMusicService.play()
                    mCommand = Dictionary.PLAY
                }
            }
        }
        runCommand(300)
        return
    }

    private fun analysePayload(payload: String, fromFolder: Boolean, shouldAdd: Boolean, isJump: Boolean){
        var mutablePayload = payload
        if(isJump){
            playSpecificSong(mutablePayload)
            return
        }

        var isFolder = fromFolder
        val folder_keys = Dictionary.complements[Dictionary.FOLDER] // Confirm it's folder
        folder_keys?.let {
            for (dict in folder_keys) {
                if (mutablePayload.contains(dict)) {
                    mutablePayload = mutablePayload.replace(dict, "").trim() // Remove folder words
                    isFolder = true
                    break
                }
            }
        }

        val all_keys = Dictionary.extras[Dictionary.ALL] // Check it's all
        all_keys?.let {
            for (dict in all_keys) {
                if (mutablePayload.startsWith(dict) && mutablePayload.split(" ").size == 1) {
                    mMusicService.setPlaylist(MusicLoader.allMusic)
                    mCommand = Dictionary.PLAY
                    runCommand(300)
                    return
                }
            }
        }

        addMusicsFromPayloadAndPlay(mutablePayload, isFolder, shouldAdd)
    }

    fun playPreviousPlaylist(){
        if(!hasStoragePermission(mActivity)) return
        val playlistIds = ArrayList<Long>()
        var music_index = 0

        val stringIds = ShPrefs.loadLastPlayed(mActivity)
        if(stringIds != null){
            var arrayIds = stringIds.split("@@")
            music_index = arrayIds[0].toInt()
            arrayIds = arrayIds[1].split("##")
            for (id in arrayIds){
                try {
                    playlistIds.add(id.toLong())
                } catch (e: NumberFormatException){
                    continue
                }
            }
        }

        mMusicService.setPlaylist(MusicLoader.getPlaylistFromIds(playlistIds))
        mCommand = Dictionary.PLAY
        mCommandExtras = music_index
    }

    private fun playSpecificSong(payload: String){
        if(!hasStoragePermission(mActivity)) return

        for (musicToGo in MusicLoader.getPlaylistFromPayload(payload, false)) {
            val indexToGo = mMusicService.checkIndexInPlaylist(musicToGo)
            if (indexToGo >= 0) {
                mVoice?.speak("É pra já", "processando...", false)
                //mMusicService.play(indexToGo)
                mCommand = Dictionary.PLAY
                mCommandExtras = indexToGo
                return
            }
        }
        // If no music was found
        mVoice?.speak("Esta música não está na playlist", "Diga uma música que está na playlist", false)
    }

    private fun addMusicsFromPayloadAndPlay(payload: String, isFolder: Boolean, shouldAdd: Boolean){
        if(!hasStoragePermission(mActivity)) return
        if(!hasMusics()) return //Check if there are musics to be played

        val playlist = MusicLoader.getPlaylistFromPayload(payload, isFolder)
        if(!playlist.isEmpty()){
            if(shouldAdd){
                mVoice?.speak("Playlist atualizada", "Nova(s) música(s) adicionada(s)", false)
                mMusicService.addToPlaylist(playlist)
            }
            else {
                mVoice?.speak("É pra já", "processando...", false)
                mMusicService.setPlaylist(playlist)
                //mMusicService.play()
                mCommand = Dictionary.PLAY
            }
        }
        else mVoice?.speak("Não encontrei nada com o que disse, tente de novo", "Desculpe, às vezes posso ter entendido errado", false)
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

    private fun hasStoragePermission(mainActivity: MainActivity): Boolean {
        when(Permissions.checkPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)){
            true -> return true
            false -> {
                mVoice?.speak("Conceda a permissão e tente novamente", "Precisamos da permissão para ler as músicas", false)
            }
        }
        return false
    }

    private fun hasMusics(): Boolean{
        return when(MusicLoader.allMusic.size > 0){
            true -> true
            false -> {
                mVoice?.speak("Não existem músicas no dispositivo", "Adicione músicas no seu dispositivo", false)
                false
            }
        }
    }

    fun setVoiceService(voiceService: VoiceService){
        if(mVoice == null) mVoice = voiceService
    }

    override fun onListenAction(listen: String?, state: VoiceStates) {
        if(state == VoiceStates.LISTENING) mMusicService.mixSoundRequest(true)
        else mMusicService.mixSoundRequest(false)
    }

    override fun onSpeakAction(spoke: String?, tip: String?, state: VoiceStates, requiredAction: Boolean) {
        if(state == VoiceStates.SPEAKING) mMusicService.mixSoundRequest(true)
        else mMusicService.mixSoundRequest(false)
        mActivity.runOnUiThread { runCommand(0) }
    }

    private fun runCommand(delay: Long) {
        Handler().postDelayed({
            Log.d("AI", "WILL RUN COMMAND $mCommand")
            when (mCommand) {
                Dictionary.PLAY -> if (mCommandExtras >= 0) mMusicService.play(mCommandExtras)
                else mMusicService.play()
                Dictionary.PAUSE -> mMusicService.pause()
                Dictionary.NEXT -> mMusicService.next()
                Dictionary.PREV -> mMusicService.prev()
            }
            mCommand = ""
            mCommandExtras = -1
        }, delay)
    }
}
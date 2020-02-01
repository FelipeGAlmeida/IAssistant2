package fgapps.com.br.iassistant2.services

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import fgapps.com.br.iassistant2.activities.MainActivity
import android.speech.tts.UtteranceProgressListener
import android.speech.RecognizerIntent
import android.content.Intent
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import fgapps.com.br.iassistant2.defines.VoiceStates
import fgapps.com.br.iassistant2.utils.ShPrefs
import java.util.*
import kotlin.collections.ArrayList


class VoiceService(mainActivity: MainActivity, aiService: AIService): RecognitionListener, UtteranceProgressListener() {

    private val mActivity = mainActivity
    private val mAI = aiService

    var tts : TextToSpeech
    var srg : SpeechRecognizer

    private lateinit var mVoiceList: ArrayList<Voice>
    private lateinit var mVoiceName: ArrayList<String>

    companion object {
        var mStatus = VoiceStates.LISTEN
        var instance: VoiceService? = null
    }

    init {
        srg = SpeechRecognizer.createSpeechRecognizer(mActivity)
        srg.setRecognitionListener(this@VoiceService)

        tts = TextToSpeech(mActivity) { }
        tts.setOnUtteranceProgressListener(this@VoiceService)

        mAI.setVoiceService(this@VoiceService)
        instance = this@VoiceService

        Handler().postDelayed({loadVoices()}, 1000)
    }

    /*** Listen functions ***/
    fun listen(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        srg.startListening(intent)
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        mStatus = VoiceStates.LISTENING
        mAI.onListenAction(null, mStatus)
        mActivity.onListenAction(null, mStatus)
    }

    override fun onRmsChanged(p0: Float) {
        mActivity.setMicColor(p0)
    }

    override fun onBufferReceived(p0: ByteArray?) {
    }

    override fun onPartialResults(p0: Bundle?) {
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onEndOfSpeech() {
    }

    override fun onError(p0: Int) {
        mStatus = VoiceStates.ERROR
        mAI.onListenAction(null, mStatus)
        mActivity.onListenAction(null, mStatus)
    }

    override fun onResults(results: Bundle?) {
        results?.let {
            val info = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            info?.let {
                if(it.isNotEmpty()){
                    mStatus = VoiceStates.LISTEN
                    mActivity.onListenAction(it[0], mStatus)
                    mAI.onListenAction(null, mStatus)
                    mActivity.runOnUiThread{ Handler().postDelayed({ mAI.checkCommand(it[0])}, 500)}
                }
            }
        }
    }

    /*** Speak functions ***/
    fun speak(toSay: String, tip: String, requireAction: Boolean){
        if(ShPrefs.loadFeedbackPreference(mActivity)) {
            mStatus = VoiceStates.SPEAKING
            tts.speak(toSay, TextToSpeech.QUEUE_FLUSH, Bundle.EMPTY, requireAction.toString())
            mActivity.onSpeakAction(toSay, tip, mStatus, false)
        } else {
            mStatus = VoiceStates.SPEAKING
            mActivity.onSpeakAction(toSay, null, mStatus, requireAction)
            Handler().postDelayed({
                mStatus = VoiceStates.SPOKEN
                mAI.onSpeakAction(null, null, mStatus, requireAction)
                mActivity.onSpeakAction(null, null, mStatus, requireAction)
            },1150)
        }
    }

    override fun onError(p0: String?, p1: Int) {
        mStatus = VoiceStates.ERROR
        val requireAction = p0?.toBoolean() ?: false
        mAI.onSpeakAction(null, null, mStatus, requireAction)
        mActivity.onSpeakAction(null, null, mStatus, requireAction)
    }

    override fun onDone(p0: String?) {
        mStatus = VoiceStates.SPOKEN
        val requireAction = p0?.toBoolean() ?: false
        mAI.onSpeakAction(null, null, mStatus, requireAction)
        mActivity.onSpeakAction(null, null, mStatus, requireAction)
    }

    override fun onStart(p0: String?) {
    }

    override fun onError(p0: String?) { //DEPRECATED, BUT MUST IMPLEMENT (??)
    }

    fun stopAction(){
        srg.stopListening()
        if(tts.isSpeaking)
            tts.stop()
        mStatus = VoiceStates.SPOKEN
    }

    fun stopVoiceServices(){
        tts.shutdown()
        srg.destroy()
    }

    /*** Voices ***/
    fun loadVoices() {
        val voices = tts.voices ?: return

        mVoiceName = ArrayList()
        mVoiceList = ArrayList()

        var n = 0
        if(voices.size > 0) {
            for (voice in voices) {
                if (voice.locale == Locale.getDefault() && !voice.isNetworkConnectionRequired) {
                    var extra = "Mulher"
                    val index = voice.name.indexOf("#") + 1 // After the '#' char the name specifies the gender
                    if (index == 0 && !mVoiceName.contains("Padrão")) {
                        mVoiceName.add(0, "Padrão") // If no gender is found (-1), means it is the default one
                        mVoiceList.add(0, voice)
                    } else {
                        n++
                        if (voice.name[index] == 'm') {
                            extra = "Homem"
                        }
                        mVoiceName.add("Opção $n - $extra")
                        mVoiceList.add(voice)
                    }
                }
            }

            tts.voice = mVoiceList[ShPrefs.loadVoicePreference(mActivity)]
        } else {
            mVoiceName.add("Nenhuma voz encontrada")
        }
    }

    fun getVoices(): ArrayList<Voice> {
        return mVoiceList
    }

    fun getVoiceNames(): ArrayList<String> {
        return mVoiceName
    }
}
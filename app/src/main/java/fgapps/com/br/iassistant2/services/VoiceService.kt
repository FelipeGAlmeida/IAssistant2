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
import android.util.Log
import fgapps.com.br.iassistant2.defines.VoiceStates


class VoiceService(mainActivity: MainActivity, aiService: AIService): RecognitionListener, UtteranceProgressListener() {

    private val mActivity = mainActivity
    private val mAI = aiService

    private var tts : TextToSpeech
    private var srg : SpeechRecognizer

    init {
        srg = SpeechRecognizer.createSpeechRecognizer(mActivity)
        srg.setRecognitionListener(this@VoiceService)

        tts = TextToSpeech(mActivity) { }
        tts.setOnUtteranceProgressListener(this@VoiceService)

        mAI.setVoiceService(this@VoiceService)
    }

    /*** Listen functions ***/
    fun listen(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        srg.startListening(intent)
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        Log.v("SRG", "READY FOR SPEECH")
        mAI.onListenAction(null, VoiceStates.LISTENING)
        mActivity.onListenAction(null, VoiceStates.LISTENING)
    }

    override fun onRmsChanged(p0: Float) {
    }

    override fun onBufferReceived(p0: ByteArray?) {
        Log.v("SRG", "BUFFER RECEIVED")
    }

    override fun onPartialResults(p0: Bundle?) {
        Log.v("SRG", "PARTIAL RESULTS")
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        Log.v("SRG", "ON EVENT")
    }

    override fun onBeginningOfSpeech() {
        Log.v("SRG", "ON BEGINNING OF SPEECH")
    }

    override fun onEndOfSpeech() {
        Log.v("SRG", "END OF SPEECH")
    }

    override fun onError(p0: Int) {
        Log.v("SRG", "ON ERROR")
        mAI.onListenAction(null, VoiceStates.ERROR)
        mActivity.onListenAction(null, VoiceStates.ERROR)
    }

    override fun onResults(results: Bundle?) {
        results?.let {
            val info = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            info?.let {
                if(it.isNotEmpty()){
                    mActivity.onListenAction(it[0], VoiceStates.LISTEN)
                    mAI.onListenAction(null, VoiceStates.LISTEN)
                    mActivity.runOnUiThread{ Handler().postDelayed({ mAI.checkCommand(it[0])}, 500)}
                }
            }
        }
    }

    /*** Speak functions ***/
    fun speak(toSay: String, tip: String){
        tts.speak(toSay, TextToSpeech.QUEUE_FLUSH, Bundle.EMPTY, "")
        mActivity.onSpeakAction(toSay, tip, VoiceStates.SPEAKING)
    }

    override fun onError(p0: String?, p1: Int) {
        Log.v("TTS", "ON ERROR - $p0")
        mAI.onSpeakAction(null, null, VoiceStates.ERROR)
        mActivity.onSpeakAction(null, null, VoiceStates.ERROR)
    }

    override fun onDone(p0: String?) {
        Log.v("TTS", "ON DONE - $p0")
        mAI.onSpeakAction(null, null, VoiceStates.SPOKEN)
        mActivity.onSpeakAction(null, null, VoiceStates.SPOKEN)
    }

    override fun onStart(p0: String?) {
        Log.v("TTS", "ON START - $p0")
    }

    override fun onError(p0: String?) { //DEPRECATED, BUT MUST IMPLEMENT (??)
    }
}
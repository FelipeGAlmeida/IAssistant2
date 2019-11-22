package fgapps.com.br.iassistant2.services

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import fgapps.com.br.iassistant2.activities.MainActivity
import android.speech.tts.UtteranceProgressListener
import android.speech.RecognizerIntent
import android.content.Intent
import android.speech.tts.TextToSpeech


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
    }

    fun listen(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        srg.startListening(intent)
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        
    }

    override fun onRmsChanged(p0: Float) {
        
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
        
    }

    override fun onResults(results: Bundle?) {
        results?.let {
            val info = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            info?.let {
                if(it.isNotEmpty()){
                    say(it[0])
                }
            }
        }
    }

    fun say(toSay: String){
        tts.speak(toSay, TextToSpeech.QUEUE_FLUSH, Bundle.EMPTY, "")
    }

    override fun onError(p0: String?) {
        
    }

    override fun onDone(said: String?) {
    }

    override fun onStart(p0: String?) {
        
    }
}
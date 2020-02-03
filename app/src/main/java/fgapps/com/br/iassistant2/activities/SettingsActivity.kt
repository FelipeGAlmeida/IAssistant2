package fgapps.com.br.iassistant2.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import fgapps.com.br.iassistant2.R
import fgapps.com.br.iassistant2.services.VoiceService
import fgapps.com.br.iassistant2.utils.ShPrefs
import kotlinx.android.synthetic.main.activity_settings.*
import kotlin.collections.ArrayList

class SettingsActivity : AppCompatActivity() {

    private lateinit var voiceList: ArrayList<Voice>

    private var canSpeak: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setControls()
    }

    private fun setControls() {
        back_btn.setOnClickListener { onBackPressed() }

        tts_btn.setOnClickListener {
            val intent = Intent();
            intent.action = "com.android.settings.TTS_SETTINGS"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            this.startActivity(intent)
        }

        spn_voices.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if(voiceList.size > 0 && canSpeak) {
                    VoiceService.instance?.let {
                        it.tts.voice = voiceList[position]
                        it.tts.speak("Oi, esse é um exemplo da voz que você selecionou. Obrigado",
                                TextToSpeech.QUEUE_FLUSH, Bundle.EMPTY, "teste de voz")
                        ShPrefs.saveVoicePreference(applicationContext, position)
                    }
                }
                canSpeak = true
            }

            override fun onNothingSelected(p0: AdapterView<*>?) { //Unused
            }

        }

        swt_feedback.setOnCheckedChangeListener { _, b -> ShPrefs.saveFeedbackPreference(applicationContext, b) }
    }

    private fun setVoices(reload: Boolean) {
        VoiceService.instance?.let {
            if(reload) it.loadVoices()

            val spnVoiceNames = it.getVoiceNames()
            voiceList = it.getVoices()

            val adp = object : ArrayAdapter<String>(applicationContext, R.layout.spinner_model, spnVoiceNames){
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, convertView, parent)
                    if(voiceList.size > 0) (v as TextView).setTextColor(Color.parseColor("#137C00"))
                    else (v as TextView).setTextColor(Color.parseColor("#410000"))
                    return v
                }
            }
            adp.setDropDownViewResource(R.layout.dropdown_model)
            spn_voices.adapter = adp
        }
    }

    private fun loadSettings() {
        spn_voices.setSelection(ShPrefs.loadVoicePreference(applicationContext))
        swt_feedback.isChecked = ShPrefs.loadFeedbackPreference(applicationContext)
    }

    override fun onResume() {
        super.onResume()

        setVoices(true)
        loadSettings()
    }
}

package fgapps.com.br.iassistant2.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import fgapps.com.br.iassistant2.R
import fgapps.com.br.iassistant2.services.VoiceService
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*
import kotlin.collections.ArrayList

class SettingsActivity : AppCompatActivity() {

    private lateinit var voiceList: ArrayList<Voice>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        back_btn.setOnClickListener { onBackPressed() }

        var spnVoiceNames = ArrayList<String>()
        voiceList = ArrayList()

        var voices = VoiceService.instance?.tts!!.voices
        var n = 0
        if(voices.size > 0) {
            for (voice in voices) {
                if (voice.locale == Locale.getDefault() && !voice.isNetworkConnectionRequired) {
                    var extra = "Mulher"
                    val index = voice.name.indexOf("#") + 1
                    if (index == 0 && !spnVoiceNames.contains("Padrão")) {
                        spnVoiceNames.add(0, "Padrão")
                        voiceList.add(0, voice)
                    } else {
                        n++
                        if (voice.name[index] == 'm') {
                            extra = "Homem"
                        }
                        spnVoiceNames.add("Opção $n - $extra")
                        voiceList.add(voice)
                    }
                }
            }
        } else {
            spnVoiceNames.add("Nenhuma voz encontrada")
        }

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
        spn_voices.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                VoiceService.instance!!.tts.voice = voiceList[position]
                VoiceService.instance!!.tts.speak("Olá, essa é a voz que você escolheu. Obrigado",
                        TextToSpeech.QUEUE_FLUSH, Bundle.EMPTY, "teste de voz")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }

        tts_btn.setOnClickListener {
            val intent = Intent();
            intent.action = "com.android.settings.TTS_SETTINGS"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            this.startActivity(intent)
        }
    }
}

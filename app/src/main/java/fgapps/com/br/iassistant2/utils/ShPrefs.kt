package fgapps.com.br.iassistant2.utils

import android.content.Context
import android.content.SharedPreferences
import fgapps.com.br.iassistant2.R
import kotlinx.android.synthetic.main.activity_settings.view.*

object ShPrefs {

    lateinit var instance: SharedPreferences

    private fun init(context: Context){
        instance = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    }

    // SETTINGS VOICE

    fun saveVoicePreference(context: Context, id: Int){
        init(context)
        val editor = instance.edit()
        editor.putInt("VOICE", id)
        editor.apply()
    }

    fun loadVoicePreference(context: Context): Int{
        init(context)
        return instance.getInt("VOICE", 0)
    }

    // SETTINGS FEEDBACK

    fun saveFeedbackPreference(context: Context, enabled: Boolean){
        init(context)
        val editor = instance.edit()
        editor.putBoolean("FEEDBACK", enabled)
        editor.apply()
    }

    fun loadFeedbackPreference(context: Context): Boolean{
        init(context)
        return instance.getBoolean("FEEDBACK", true)
    }

    // SETTINGS FLOATING

    fun saveFloatingPreference(context: Context, option: Int){
        init(context)
        val editor = instance.edit()
        editor.putInt("FLOAT", option)
        editor.apply()
    }

    fun loadFloatingPreference(context: Context): Int{
        init(context)
        return instance.getInt("FLOAT", R.id.floatV_rbt)
    }

    // SETTINGS NOTIFICATION

    fun saveNotificationPreference(context: Context, enabled: Boolean){
        init(context)
        val editor = instance.edit()
        editor.putBoolean("NOTIF", enabled)
        editor.apply()
    }

    fun loadNotificationPreference(context: Context): Boolean{
        init(context)
        return instance.getBoolean("NOTIF", true)
    }

    // PLAYLIST IDS

    fun saveLastPlayed(context: Context, playlistIds: String){
        init(context)
        val editor = instance.edit()
        editor.putString("PLAYED", playlistIds)
        editor.apply()
    }

    fun loadLastPlayed(context: Context): String? {
        init(context)
        return instance.getString("PLAYED", null)
    }
}
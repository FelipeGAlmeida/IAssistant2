package fgapps.com.br.iassistant2.music

import android.content.Context
import android.provider.MediaStore
import android.util.Log

class MusicLoader {

    companion object {

        fun loadAllMusic(context: Context): ArrayList<Music>{
            val musicList = ArrayList<Music>()

            val musicResolver = context.contentResolver
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val musicCursor = musicResolver.query(musicUri,
                                        null,
                                        null,
                                        null,
                                        null)

            if(musicCursor != null && musicCursor.moveToFirst()){
                //Get columns
                val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
                val nameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val c = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                do {
                    val thisId = musicCursor.getLong(idColumn)
                    val thisTitle = musicCursor.getString(titleColumn)
                    val thisArtist = musicCursor.getString(artistColumn)
                    val thisName = musicCursor.getString(nameColumn)
                    var thisFolder = musicCursor.getString(c)
                    val path = thisFolder.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    thisFolder = path[path.size - 2]
                    if (!thisName.contains("AUD-") && !thisFolder.contains("WhatsApp Audio"))
                        musicList.add(Music(thisId, thisTitle, thisArtist, thisName.replace(".mp3", ""), thisFolder))
                    else
                        Log.v("MUSIC_LIST", "Song $thisName wasn't added!")
                }while(musicCursor.moveToNext())

                musicCursor.close()
                return musicList
            }
            return musicList
        }

    }
}
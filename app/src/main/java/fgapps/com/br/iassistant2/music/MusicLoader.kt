package fgapps.com.br.iassistant2.music

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import fgapps.com.br.iassistant2.defines.Dictionary
import fgapps.com.br.iassistant2.utils.Utils


class MusicLoader {

    companion object {

        var allMusic: ArrayList<Music> = ArrayList()

        /* ***** Load all the musics in the phone ***** */
        fun loadAllMusic(context: Context) {

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
                        allMusic.add(Music(thisId, thisTitle, thisArtist, thisName.replace(".mp3", ""), thisFolder))
                    else
                        Log.v("MUSIC_LIST", "Song $thisName wasn't added!")
                }while(musicCursor.moveToNext())

                musicCursor.close()
            }
        }

        fun getPlaylistFromPayload(payload: String, type: String): ArrayList<Music>{
            val playlist = ArrayList<Music>()
            for(music in allMusic){

                var source = Dictionary.MUSIC
                when(type){
                    Dictionary.MUSIC -> {
                        source = music.name
                    }
                    Dictionary.FOLDER -> {
                        source = music.folder
                    }
                }

                if(match(source, payload))
                    playlist.add(music)
            }

            if(type == Dictionary.MUSIC){
                sortPlaylist(playlist)
            }

            return playlist
        }

        fun sortPlaylist(playlist: ArrayList<Music>){
            //sort playlist in most match order
        }

        private fun match(raw_source: String, raw_match: String): Boolean {

            val source = Utils.normalizeStrings(raw_source, true, true, false)
            val match = Utils.normalizeStrings(raw_match, true, true, false)

            if (source.toLowerCase().contains(match)) { // If source contains the match, it matches
                return true
            }

            val source_char = source.toCharArray()
            val match_char = match.toCharArray()

            if (source_char[0] != match_char[0]) { // If the start are not equals, the rest isn't too
                return false
            }

            var inc_match = false
            var match_idx = 0
            var source_idx = 0
            var equality = 0
            while (source_idx < source_char.size) {
                if (source_idx < (match_char.size + (match_char.size - equality)) && inc_match) match_idx++
                if (source_char[source_idx] == match_char[match_idx]) {
                    equality++
                    inc_match = true
                } else
                    inc_match = false
                if (match_idx == match_char.size - 1) break
                source_idx++
            }

            return equality >= source_idx - 2
        }

    }
}
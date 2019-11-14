package fgapps.com.br.iassistant2.utils

import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fgapps.com.br.iassistant2.R
import fgapps.com.br.iassistant2.activities.MainActivity

class Permissions {

    companion object {

        const val READ_EXTERNAL_STORAGE_CODE = 1

        fun checkPermission(mainActivity: MainActivity, permission: String, permission_code: Int): Boolean{
            if (ContextCompat.checkSelfPermission(mainActivity,
                    permission)!= PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, permission)) {
                    showPermissionExplanationDialog(mainActivity, "Required permission",
                                                                "Without this, you can't use the app!",
                                                                          permission,
                                                                          permission_code)
                } else {
                    ActivityCompat.requestPermissions(mainActivity,
                            arrayOf(permission), permission_code)
                }
            } else return true
            return false
        }

        private fun showPermissionExplanationDialog(mainActivity: MainActivity, title: String, message: String,
                                                    permission: String, permission_code: Int){
            val builder = AlertDialog.Builder(mainActivity, R.style.CustomDialogTheme)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Ask again") { arg0, arg1 ->  ActivityCompat.requestPermissions(mainActivity,
                                                                            arrayOf(permission), permission_code)}
            builder.setNegativeButton("Close app") { arg0, arg1 -> mainActivity.finishAndRemoveTask() }
            builder.setCancelable(false)
            val alert = builder.create()
            alert.show()
        }
    }
}
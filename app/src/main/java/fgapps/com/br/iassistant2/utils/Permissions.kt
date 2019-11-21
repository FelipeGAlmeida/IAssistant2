package fgapps.com.br.iassistant2.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fgapps.com.br.iassistant2.R
import fgapps.com.br.iassistant2.activities.MainActivity

class Permissions {

    companion object {

        const val REQ_PERMISSION_CODE = 1

        fun checkPermission(mainActivity: MainActivity, permission: String): Boolean{
            if (ContextCompat.checkSelfPermission(mainActivity,
                    permission)!= PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, permission)) {
                    showPermissionExplanationDialog(mainActivity, "Required permission",
                                                                "Without this, you can't use the app!",
                                                                          permission)
                } else {
                    ActivityCompat.requestPermissions(mainActivity,
                            arrayOf(permission), REQ_PERMISSION_CODE)
                }
            } else return true
            return false
        }

        private fun showPermissionExplanationDialog(mainActivity: MainActivity, title: String, message: String,
                                                    permission: String){
            val builder = AlertDialog.Builder(mainActivity, R.style.CustomDialogTheme)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Ok, request again") { arg0, arg1 ->  ActivityCompat.requestPermissions(mainActivity,
                                                                            arrayOf(permission), REQ_PERMISSION_CODE)}
            builder.setNegativeButton("No, thanks") { arg0, arg1 -> }
            builder.setCancelable(false)
            val alert = builder.create()
            alert.show()
        }
    }
}
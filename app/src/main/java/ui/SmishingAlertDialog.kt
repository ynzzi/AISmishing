package ui

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.smishingdetector.R
import android.widget.TextView
import android.widget.Button

object SmishingAlertDialog {
    fun show(context: Context, phoneNumber: String, message: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null)
        val dialog = AlertDialog.Builder(context).setView(view).create()

        val messageText = view.findViewById<TextView    >(R.id.messageText)
        val reportButton = view.findViewById<Button>(R.id.reportButton)
        val ignoreButton = view.findViewById<Button>(R.id.ignoreButton)

        messageText.text = "$phoneNumber \"$message\""

        reportButton.setOnClickListener {
            Toast.makeText(context, "신고되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        ignoreButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

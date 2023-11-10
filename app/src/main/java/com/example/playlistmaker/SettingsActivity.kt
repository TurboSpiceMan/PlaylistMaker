package com.example.playlistmaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.content.Intent
import android.net.Uri
import android.widget.FrameLayout


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

         val backButton = findViewById<ImageView>(R.id.back_settings_button)
            backButton.setOnClickListener{
                MainActivity().onBackPressed()
            }


        val shareMenuItem = findViewById<FrameLayout>(R.id.shareApp)
        shareMenuItem.setOnClickListener{
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app))
            shareIntent.putExtra(Intent.EXTRA_TEXT,getString(R.string.shareText))
            startActivity(Intent.createChooser(shareIntent,getString(R.string.shareIntentTitle)))
        }

        val sendMailToSupport = findViewById<FrameLayout>(R.id.mailToSupport)
        sendMailToSupport.setOnClickListener{
            val mailIntent = Intent(Intent.ACTION_SENDTO)
            mailIntent.data = Uri.parse("mailto:")
            mailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.userEmailAdress)))
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.emailSubject))
            mailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.emailText))
            startActivity(mailIntent)
        }

        val userAgreement = findViewById<FrameLayout>(R.id.userAgreement)
        userAgreement.setOnClickListener{
            val agreementIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.agreementUrl)))
            startActivity(agreementIntent)
        }

    }
}
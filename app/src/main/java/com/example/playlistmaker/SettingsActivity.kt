package com.example.playlistmaker

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.content.Intent
import android.net.Uri
import android.widget.FrameLayout
import android.widget.Switch


class SettingsActivity : AppCompatActivity() {
    @SuppressLint("SuspiciousIndentation", "UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageView>(R.id.back_settings_button)
        backButton.setOnClickListener {
            this.finish()
        }

        val themeSwitcher = findViewById<Switch>(R.id.theme_switch)
        themeSwitcher.isChecked = (applicationContext as App).darkTheme

        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)

            //save theme state
            (applicationContext as App).getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE).edit()
                .putBoolean(SWITCHER_KEY, checked)
                .apply()
        }


        val shareMenuItem = findViewById<FrameLayout>(R.id.shareApp)
        shareMenuItem.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app))
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText))
            startActivity(Intent.createChooser(shareIntent, getString(R.string.shareIntentTitle)))
        }

        val sendMailToSupport = findViewById<FrameLayout>(R.id.mailToSupport)
        sendMailToSupport.setOnClickListener {
            val mailIntent = Intent(Intent.ACTION_SENDTO)
            mailIntent.data = Uri.parse("mailto:")
            mailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.userEmailAdress)))
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.emailSubject))
            mailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.emailText))
            startActivity(mailIntent)
        }

        val userAgreement = findViewById<FrameLayout>(R.id.userAgreement)
        userAgreement.setOnClickListener {
            val agreementIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.agreementUrl)))
            startActivity(agreementIntent)
        }

    }
}
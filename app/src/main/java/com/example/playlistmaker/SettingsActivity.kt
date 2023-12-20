package com.example.playlistmaker

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.content.Intent
import android.net.Uri
import android.widget.FrameLayout
import android.widget.Switch
import androidx.core.content.edit


class SettingsActivity : AppCompatActivity() {
    @SuppressLint("SuspiciousIndentation", "UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //backButton init && listener
        val backButton = findViewById<ImageView>(R.id.back_settings_button)
        backButton.setOnClickListener {
            finish()
        }

        //switch init && switcher listener
        val themeSwitcher = findViewById<Switch>(R.id.theme_switch)
        themeSwitcher.isChecked = (applicationContext as App).darkTheme
        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)

            //save theme state
            getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE).edit {
                putBoolean(SWITCHER_KEY, checked)
                apply()
            }
        }

        //share init && intent
        val shareMenuItem = findViewById<FrameLayout>(R.id.shareApp)
        shareMenuItem.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.shareIntentTitle)))
        }

        //send mail init && intent
        val sendMailToSupport = findViewById<FrameLayout>(R.id.mailToSupport)
        sendMailToSupport.setOnClickListener {
            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.userEmailAdress)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.emailSubject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.emailText))
            }
            startActivity(mailIntent)
        }

        //agreement init && intent
        val userAgreement = findViewById<FrameLayout>(R.id.userAgreement)
        userAgreement.setOnClickListener {
            val agreementIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.agreementUrl)))
            startActivity(agreementIntent)
        }

    }
}
package com.sahilSamiyal.musicPlayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sahilSamiyal.musicPlayer.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About"
        binding.aboutText.text = aboutText()
    }
    private fun aboutText(): String{
        return "Developed By: Sahil Samiyal" +
                "\n\nEmailID : uic.20MCA1476@gmail.com"
    }
}
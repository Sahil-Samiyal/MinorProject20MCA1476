package com.sahilSamiyal.musicPlayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sahilSamiyal.musicPlayer.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playMusic.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)}
            binding.plaVideos.setOnClickListener{
                val intents = Intent(this, VideoActivity2::class.java)
                startActivity(intents) }
    }
}
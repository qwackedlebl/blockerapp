package com.hackathon.blockerapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hackathon.blockerapp.databinding.ActivityHowItWorksBinding

class HowItWorksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHowItWorksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHowItWorksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "How It Works"

        binding.contentText.text = "Here we explain how the side menu and app locking works.\n\nThis is a placeholder — replace with the detailed explanation you provided."
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}


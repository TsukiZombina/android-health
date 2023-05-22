package com.example.healthapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.healthapp.R
import com.example.healthapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //hadle click login
        binding.loginBtn.setOnClickListener {

        }

        //handle click skip login and continue to main screen
        binding.skipBtn.setOnClickListener {

        }
    }
}
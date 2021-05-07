package com.decagon.android.sq007.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.decagon.android.sq007.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSupportNavigateUp(): Boolean {

        val navController = findNavController(R.id.navHost)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

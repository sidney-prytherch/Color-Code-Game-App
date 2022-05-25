package com.sid.app.hitorblow

import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.sid.app.hitorblow.databinding.ActivityMainBinding
import com.sid.app.hitorblow.helper.PreferenceHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //private lateinit var preferenceManager: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
        //preferenceManager = PreferenceHelper(PreferenceManager.getDefaultSharedPreferences(this))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val navController = Navigation.findNavController(this, R.id.nav_host_container)
        setupWithNavController(binding.bottomNav, navController)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    fun getColorsEnabledArray(): BooleanArray {
//        return
//        if (false) {
//            booleanArrayOf(
//                preferenceManager.getBool(PreferenceHelper.RED_ENABLED),
//                preferenceManager.getBool(PreferenceHelper.ORANGE_ENABLED),
//                preferenceManager.getBool(PreferenceHelper.YELLOW_ENABLED),
//                preferenceManager.getBool(PreferenceHelper.GREEN_ENABLED),
//                preferenceManager.getBool(PreferenceHelper.BLUEGREEN_ENABLED),
//                preferenceManager.getBool(PreferenceHelper.LIGHTBLUE_ENABLED),
//                preferenceManager.getBool(PreferenceHelper.DARKBLUE_ENABLED),
//                preferenceManager.getBool(PreferenceHelper.PURPLE_ENABLED),
//                preferenceManager.getBool(PreferenceHelper.PINK_ENABLED)
//            )
//        } else {
           return BooleanArray(9) { true }
//        }
    }

    fun setColorBoolean(key: String, isEnabled: Boolean) {
//        if (false) {
//            preferenceManager.setBool(key, isEnabled)
//        }
    }

    fun getGuessLimitOption(): Int {
//        return if (false) {
//            preferenceManager.getInt(PreferenceHelper.GUESSES, 3)
//        } else 3
        return 3
    }

    fun setGuessLimitOption(guessLimit: Int) {
//        if (false) {
//            preferenceManager.setInt(PreferenceHelper.GUESSES, guessLimit)
//        }
    }
}

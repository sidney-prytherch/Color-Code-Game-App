package com.sid.app.hitorblow.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.sid.app.hitorblow.R
import com.sid.app.hitorblow.databinding.FragmentOptionsBinding
import com.sid.app.hitorblow.databinding.FragmentPlayBinding
import com.sid.app.hitorblow.helper.PreferenceHelper

class OptionsFragment : Fragment() {


    private var _binding: FragmentOptionsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //    private lateinit var mContext: MainActivity
    private lateinit var buttonArray: Array<ImageView>
    private val colorPrefStrings = arrayOf(
        "RED_ENABLED",
        "ORANGE_ENABLED",
        "YELLOW_ENABLED",
        "GREEN_ENABLED",
        "BLUEGREEN_ENABLED",
        "LIGHTBLUE_ENABLED",
        "DARKBLUE_ENABLED",
        "PURPLE_ENABLED",
        "PINK_ENABLED"
    )
    private lateinit var unselectedColorArray: IntArray
    private lateinit var selectedColorArray: IntArray
    private lateinit var colorIsEnabledArray: BooleanArray

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        try {
//            mContext = context as MainActivity
//        } catch (e: ClassCastException) {
//            throw e
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOptionsBinding.inflate(inflater, container, false)
        val mainView = binding.root




        val appContext = requireContext().applicationContext
        var prefs = appContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val guessLimitOption = if (prefs.contains("COUNT")) {
            prefs.getInt("COUNT", 1)
        } else {
            prefs.edit().putInt("COUNT", 1).apply()
            1
        }
        colorIsEnabledArray = if (
            colorPrefStrings.map { s -> prefs.contains(s) }.contains(false)
        ) {
            colorPrefStrings.forEach { s -> prefs.edit().putBoolean(s, true).apply() }
            BooleanArray(9) { true }
        } else {
            colorPrefStrings.map { s -> prefs.getBoolean(s, true) }.toBooleanArray()
        }


        //Getting the sharedPreferences
//        val appContext = requireContext().applicationContext
//        var prefs = appContext.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
//
//        prefs.edit().putString("PRICE", insertedText).apply()

//        colorIsEnabledArray = mContext.getColorsEnabledArray()
//        val guessLimitOption = mContext.getGuessLimitOption()


        val guessSpinner = binding.guessSpinner
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.guess_limit_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        guessSpinner.adapter = adapter

        guessSpinner.setSelection(guessLimitOption)
        guessSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                prefs.edit().putInt("COUNT", position).apply()
            }
        }

        buttonArray = arrayOf(
            binding.red,
            binding.orange,
            binding.yellow,
            binding.green,
            binding.bluegreen,
            binding.lightblue,
            binding.darkblue,
            binding.purple,
            binding.pink
        )

        unselectedColorArray = intArrayOf(
            R.drawable.color_red,
            R.drawable.color_orange,
            R.drawable.color_yellow,
            R.drawable.color_green,
            R.drawable.color_bluegreen,
            R.drawable.color_lightblue,
            R.drawable.color_darkblue,
            R.drawable.color_purple,
            R.drawable.color_pink
        )

        selectedColorArray = intArrayOf(
            R.drawable.selected_color_red_checked,
            R.drawable.selected_color_orange_checked,
            R.drawable.selected_color_yellow_checked,
            R.drawable.selected_color_green_checked,
            R.drawable.selected_color_bluegreen_checked,
            R.drawable.selected_color_lightblue_checked,
            R.drawable.selected_color_darkblue_checked,
            R.drawable.selected_color_purple_checked,
            R.drawable.selected_color_pink_checked,
        )

        for (i in 0 until 9) {
            setUpButton(i, prefs)
        }

        colorIsEnabledArray.forEachIndexed { i, isEnabled ->
            if (isEnabled) {
                buttonArray[i].setImageResource(selectedColorArray[i])
            } else {
                buttonArray[i].setImageResource(unselectedColorArray[i])
            }
        }

        return mainView
    }

    private fun setUpButton(index: Int, prefs: SharedPreferences) {
        buttonArray[index].setOnClickListener {
            if (colorIsEnabledArray[index]) {
                (it as ImageView).setImageResource(unselectedColorArray[index])
                setButtonBooleans(index, false, prefs)
            } else {
                (it as ImageView).setImageResource(selectedColorArray[index])
                setButtonBooleans(index, true, prefs)
            }
        }
    }

    private fun setButtonBooleans(index: Int, isEnabled: Boolean, prefs: SharedPreferences) {
        colorIsEnabledArray[index] = isEnabled
        prefs.edit().putBoolean(colorPrefStrings[index], isEnabled).apply()
    }
}

package com.sid.app.hitorblow.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.sid.app.hitorblow.R
import com.sid.app.hitorblow.databinding.FragmentPlayBinding
import com.sid.app.hitorblow.helper.AnswerParcel
import kotlin.math.min

class PlayFragment : Fragment() {


    private var _binding: FragmentPlayBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mainView: View
    private var solution = intArrayOf()
//    private lateinit var mContext: MainActivity
    private lateinit var colorIsEnabledArray: BooleanArray
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
    private lateinit var colorViewArray: Array<View>
    //private lateinit var guessesLayouts: Array<ConstraintLayout>
    private lateinit var buttonGroups: Array<ButtonGroup>
    private var currentButtonIndex = 0
    private var currentButtonGroupIndex = 0
    private var isInResultsScreen = false
    private var won = false


//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        try {
//            mContext = context as MainActivity
//        } catch (e: ClassCastException) {
//            throw e
//        }
//    }

    @SuppressLint("InflateParams")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        mainView = binding.root
        setHasOptionsMenu(true)

        val appContext = requireContext().applicationContext
        val prefs = appContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        // get options
//        colorIsEnabledArray = mContext.getColorsEnabledArray()
        val guessLimitOption = if (prefs.contains("COUNT")) {
            prefs.getInt("COUNT", 1) + 4
        } else {
            prefs.edit().putInt("COUNT", 1).apply()
            5
        }
        colorIsEnabledArray = if (
            colorPrefStrings.map { s -> prefs.contains(s) }.contains(false)
        ) {
            colorPrefStrings.forEach { s -> prefs.edit().putBoolean(s, true).apply() }
            BooleanArray(9) { true }
        } else {
            colorPrefStrings.map { s -> prefs.getBoolean(s, true) }.toBooleanArray()
        }

        // get relevant views
        val allTextRows = arrayOf(
            binding.row1,
            binding.row2,
            binding.row3,
            binding.row4,
            binding.row5,
            binding.row6,
            binding.row7,
            binding.row8,
            binding.row9
        )

        val allColorRows = arrayOf(
            binding.row1Colors,
            binding.row2Colors,
            binding.row3Colors,
            binding.row4Colors,
            binding.row5Colors,
            binding.row6Colors,
            binding.row7Colors,
            binding.row8Colors,
            binding.row9Colors
        )

        // If there are not enough colors, display the color error text and return
        if (colorIsEnabledArray.filter { it }.size < 4) {
            for (i in allTextRows.indices) {
                allTextRows[i].root.visibility = View.GONE
                allColorRows[i].root.visibility = View.GONE
            }
            binding.makeGuess.visibility = View.GONE
            binding.viewResults.visibility = View.GONE
            binding.restart.visibility = View.GONE
            binding.colorOptions.visibility = View.GONE
            binding.colorError.visibility = View.VISIBLE
            return mainView
        }

        // set up the guess layout rows and create the array of ButtonGroups
        val guessesTextLayouts = Array(guessLimitOption){ i -> allTextRows[i] }//ConstraintLayout(requireContext()) }
        val guessesColorLayouts = Array(guessLimitOption){ i -> allColorRows[i] }//ConstraintLayout(requireContext()) }
        buttonGroups = Array(guessLimitOption) {i ->
            //guessesLayouts[i] = allRows[i].root
            val numberString = "${(i + 1)}."
            guessesTextLayouts[i].guessNumber.text = numberString

            // create the buttonGroups for the current row
            val buttonGroup = ButtonGroup(
                guessesColorLayouts[i].guessOne,
                guessesColorLayouts[i].guessTwo,
                guessesColorLayouts[i].guessThree,
                guessesColorLayouts[i].guessFour,
                guessesTextLayouts[i].hit,
                guessesTextLayouts[i].blow,
                i
            )

            // create the buttons for row's buttonGroup
            buttonGroup.buttons.forEach { buttonNode ->
                buttonNode.button.setOnClickListener {
                    if (!isInResultsScreen && buttonGroup.groupIndex == currentButtonGroupIndex) {
                        buttonGroup.buttons[currentButtonIndex].setIsFocused(false)
                        currentButtonIndex = buttonNode.buttonIndex
                        buttonNode.setIsFocused(true)
                    }
                }
            }

            buttonGroup
        }

        // remove/hide the unused guess rows
        for (i in guessLimitOption until allTextRows.size) {
            allTextRows[i].root.visibility = View.GONE
            allColorRows[i].root.visibility = View.GONE
        }

        // set up the color buttons
        colorViewArray = arrayOf(
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

        // remove unused color buttons, and take the remaining colors, shuffle them, and take the first 4 as the solution
        solution = colorIsEnabledArray.mapIndexed { i, colorIsEnabled ->
            if (!colorIsEnabled) {
                colorViewArray[i].visibility = View.GONE
                -1
            } else {
                colorViewArray[i].setOnClickListener {
                    if (!isInResultsScreen) {
                        currentButtonIndex = buttonGroups[currentButtonGroupIndex].setColor(currentButtonIndex, i)
                    }
                }
                i
            }
        }.filter { it > -1 }.shuffled().take(4).toIntArray()

        binding.viewResults.setOnClickListener {
            val answerParcel = AnswerParcel(solution, won)
            val bundle = bundleOf("answerParcel" to answerParcel)
            Navigation.findNavController(mainView).navigate(R.id.action_play_to_results, bundle)
        }

        binding.restart.setOnClickListener {
            restartGame()
        }

        // set up the guess button
        binding.makeGuess.setOnClickListener {
            val guess = buttonGroups[currentButtonGroupIndex].getCode()
            // only move on if the guess is complete - all 4 places must have a color
            if (guess != null) {
                // calculate the hit and blow count and display it
                val hit = buttonGroups[currentButtonGroupIndex].setHitAndBlow(guess, solution)
                // remove the focus for the button of the buttonGroup
                buttonGroups[currentButtonGroupIndex].buttons[currentButtonIndex].setIsFocused(false)

                // if there are 4 hits, HOORAY!
                if (hit == 4) {
                    won = true
                    goToResults()
                    displayResults()
                    return@setOnClickListener
                }
                // move to the next group. If there is no next group, they lose
                currentButtonGroupIndex++
                currentButtonIndex = 0
                if (currentButtonGroupIndex >= buttonGroups.size) {
                    goToResults()
                    displayResults()
                } else {
                    buttonGroups[currentButtonGroupIndex].buttons[currentButtonIndex].setIsFocused(true)
                }
            }
        }

        if (currentButtonGroupIndex < buttonGroups.size) {
            buttonGroups[currentButtonGroupIndex].buttons[currentButtonIndex].setIsFocused(true)
        }

        return mainView
    }

    private fun displayResults() {
        val answerParcel = AnswerParcel(solution, won)
        val bundle = bundleOf("answerParcel" to answerParcel)
        Navigation.findNavController(mainView).navigate(R.id.action_play_to_results, bundle)
    }

    private fun goToResults() {
        isInResultsScreen = true
        binding.restart.visibility = View.VISIBLE
        binding.viewResults.visibility = View.VISIBLE
        binding.makeGuess.visibility = View.GONE
    }

    fun restartGame() {
        won = false
        isInResultsScreen = false
        binding.restart.visibility = View.GONE
        binding.viewResults.visibility = View.GONE
        binding.makeGuess.visibility = View.VISIBLE
        currentButtonIndex = 0
        currentButtonGroupIndex = 0
        buttonGroups.forEach { buttonGroup ->
            buttonGroup.reset()
        }
        buttonGroups[currentButtonGroupIndex].buttons[currentButtonIndex].setIsFocused(true)
        solution = colorIsEnabledArray.mapIndexed { i, colorIsEnabled ->
            if (!colorIsEnabled) {
                colorViewArray[i].visibility = View.GONE
                -1
            } else {
                colorViewArray[i].setOnClickListener {
                    currentButtonIndex = buttonGroups[currentButtonGroupIndex].setColor(currentButtonIndex, i)
                }
                i
            }
        }.filter { it > -1 }.shuffled().take(4).toIntArray()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.play_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.howToPlay) {
            Navigation.findNavController(mainView).navigate(R.id.action_play_to_how_to_play)
            true
        } else super.onOptionsItemSelected(item)

    }

    class ButtonGroup(view1: ImageView, view2: ImageView, view3: ImageView, view4: ImageView, private val hitView: TextView, private val blowView: TextView, val groupIndex: Int) {
        val buttons = arrayOf(
            ButtonNode(view1, 0),
            ButtonNode(view2, 1),
            ButtonNode(view3, 2),
            ButtonNode(view4, 3)
        )
        fun setColor(currentIndex: Int, colorIndex: Int): Int {
            var nextIndex = currentIndex
            for (index in 0 until 4) {
                if (buttons[index].getColor() == colorIndex) {
                    buttons[index].resetColor()
                }
                if (index > currentIndex && nextIndex == currentIndex && !buttons[index].isColored()) {
                    nextIndex = index
                }
            }
            nextIndex = if (nextIndex != currentIndex) nextIndex else min(currentIndex + 1, 3)
            if (currentIndex != nextIndex) {
                buttons[currentIndex].setColorAndUnsetFocused(colorIndex)
                buttons[nextIndex].setIsFocused(true)
            } else {
                buttons[currentIndex].setColor(colorIndex)
            }
            return nextIndex
        }
        private fun resetColors() {
            for (button in buttons) {
                button.resetColor()
            }
        }
        fun getCode(): IntArray? {
            return buttons.map {
                val color = it.getColor()
                if (color == 9) {
                    return null
                }
                color
            }.toIntArray()
        }
        private fun resetHitAndBlow() {
            hitView.text = ""
            blowView.text = ""
        }
        fun setHitAndBlow(guess: IntArray, solution: IntArray): Int {
            var blow = 0
            var hit = 0
            guess.forEachIndexed { i, guessedColor ->
                if (guessedColor == solution[i]) {
                    hit++
                } else if (solution.contains(guessedColor)) {
                    blow++
                }
            }
            val hitText = """Hit: $hit"""
            val blowText = """Blow: $blow"""
            hitView.text = hitText
            blowView.text = blowText
            return hit
        }
        fun reset() {
            resetColors()
            resetHitAndBlow()
        }
        fun setRowColors(colors: IntArray) {
            buttons.forEachIndexed { i, buttonNode ->
                buttonNode.setColor(colors[i])
            }
        }
    }

    class ButtonNode(val button: ImageView, val buttonIndex: Int) {
        private var isFocused = false
        private var colorIndex = 9
        private val unselectedColorIndex = 9

        fun isColored():Boolean {
            return colorIndex != 9
        }

        fun setColorAndUnsetFocused(index: Int) {
            isFocused = false
            setColor(index)
        }

        fun setColor(index: Int) {
            colorIndex = index
            setColor()
        }

        fun getColor():Int {
            return colorIndex
        }

        fun resetColor() {
            colorIndex = unselectedColorIndex
            isFocused = false
            setColor()
        }

        private fun setColor() {
            button.setImageResource(
                if (isFocused) selectedColorBackground[colorIndex] else unselectedColorBackground[colorIndex]
            )
        }

        fun setIsFocused(focused: Boolean) {
            isFocused = focused
            setColor()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (solution.isNotEmpty()){
            outState.putIntArray("solution", solution)
            outState.putInt("currentButtonGroupIndex", currentButtonGroupIndex)
            outState.putInt("currentButtonIndex", currentButtonIndex)
            outState.putBoolean("isInResultsScreen", isInResultsScreen)
            buttonGroups.forEachIndexed { i, buttonGroup ->
                outState.putIntArray("""buttonGroup$i""", buttonGroup.getCode() ?: intArrayOf(9,9,9,9))
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            buttonGroups[currentButtonGroupIndex].buttons[currentButtonIndex].setIsFocused(false)
            currentButtonGroupIndex = savedInstanceState.getInt("currentButtonGroupIndex")
            currentButtonIndex = savedInstanceState.getInt("currentButtonIndex")
            if (currentButtonIndex < 4 && currentButtonGroupIndex < buttonGroups.size) {
                buttonGroups[currentButtonGroupIndex].buttons[currentButtonIndex].setIsFocused(true)
            }
            solution = savedInstanceState.getIntArray("solution") ?: solution
            isInResultsScreen = savedInstanceState.getBoolean("isInResultsScreen")
            buttonGroups.forEachIndexed { i, buttonGroup ->
                val colors = savedInstanceState.getIntArray("""buttonGroup$i""") ?: intArrayOf(9,9,9,9)
                buttonGroup.setRowColors(colors)
                if (i < currentButtonGroupIndex || (isInResultsScreen && i == currentButtonGroupIndex)) {
                    buttonGroup.setHitAndBlow(colors, solution)
                }
            }
            if (isInResultsScreen) {
                goToResults()
            }
        } else {
            restartGame()
        }
    }

    companion object {

        val unselectedColorBackground = arrayOf(
            R.drawable.color_red,
            R.drawable.color_orange,
            R.drawable.color_yellow,
            R.drawable.color_green,
            R.drawable.color_bluegreen,
            R.drawable.color_lightblue,
            R.drawable.color_darkblue,
            R.drawable.color_purple,
            R.drawable.color_pink,
            R.drawable.color_blank
        )

        val selectedColorBackground = arrayOf(
            R.drawable.selected_color_red,
            R.drawable.selected_color_orange,
            R.drawable.selected_color_yellow,
            R.drawable.selected_color_green,
            R.drawable.selected_color_bluegreen,
            R.drawable.selected_color_lightblue,
            R.drawable.selected_color_darkblue,
            R.drawable.selected_color_purple,
            R.drawable.selected_color_pink,
            R.drawable.selected_color_blank
        )
    }
}

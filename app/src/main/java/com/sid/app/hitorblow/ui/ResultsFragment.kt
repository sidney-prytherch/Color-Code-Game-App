package com.sid.app.hitorblow.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import com.sid.app.hitorblow.R
import com.sid.app.hitorblow.databinding.ResultsFragmentBinding
import com.sid.app.hitorblow.helper.AnswerParcel

class ResultsFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.results_fragment, null)

        val answerParcel = requireArguments().get("answerParcel") as AnswerParcel
        view.findViewById<View>(R.id.colors).findViewById<ImageView>(R.id.guessOne)
            .setImageResource(PlayFragment.unselectedColorBackground[answerParcel.answer[0]])
        view.findViewById<View>(R.id.colors).findViewById<ImageView>(R.id.guessTwo)
            .setImageResource(PlayFragment.unselectedColorBackground[answerParcel.answer[1]])
        view.findViewById<View>(R.id.colors).findViewById<ImageView>(R.id.guessThree)
            .setImageResource(PlayFragment.unselectedColorBackground[answerParcel.answer[2]])
        view.findViewById<View>(R.id.colors).findViewById<ImageView>(R.id.guessFour)
            .setImageResource(PlayFragment.unselectedColorBackground[answerParcel.answer[3]])
        view.findViewById<TextView>(R.id.results).text =
            if (answerParcel.won) getString(R.string.you_win) else getString(R.string.you_lose)

        return AlertDialog.Builder(requireActivity())
            .setView(view)
            .setTitle(R.string.results)
            .setView(view)
            .setNegativeButton(R.string.view_guesses) { _, _ ->
                dialog?.cancel()
            }
            .setPositiveButton(R.string.play_again) { _, _ ->
                (parentFragmentManager.primaryNavigationFragment!! as PlayFragment).restartGame()
            }
            .create()

    }

}
package com.sid.app.hitorblow.helper

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class AnswerParcel(val answer: IntArray, val won:Boolean): Parcelable
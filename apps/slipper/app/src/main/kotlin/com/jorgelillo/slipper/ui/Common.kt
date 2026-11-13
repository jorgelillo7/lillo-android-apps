package com.jorgelillo.slipper.ui

import java.text.NumberFormat

/** 1240 → "1,240" or "1.240", as the phone's language writes it. */
internal fun formatScore(score: Int): String = NumberFormat.getIntegerInstance().format(score)

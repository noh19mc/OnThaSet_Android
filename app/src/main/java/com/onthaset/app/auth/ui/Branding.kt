package com.onthaset.app.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Yellow highway-shield logo with the "ON / THA / SET" stack centered inside.
 * Letter sizes scale proportionally to the shield so it renders cleanly at every
 * placement (64dp on More, 96dp on the legal screen, 120dp on auth).
 */
private val ShieldShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.5f, 0f)
    lineTo(w, h * 0.18f)
    lineTo(w, h * 0.65f)
    quadraticTo(w, h * 0.95f, w * 0.5f, h)
    quadraticTo(0f, h * 0.95f, 0f, h * 0.65f)
    lineTo(0f, h * 0.18f)
    close()
}

@Composable
fun OnThaSetShield(size: Dp = 120.dp) {
    val px = size.value
    // Hand-tuned ratios to match the iOS shield letter proportions.
    val onSize = (px * 0.165f).sp
    val thaSize = (px * 0.135f).sp
    val setSize = (px * 0.20f).sp

    Box(
        modifier = Modifier
            .size(size)
            .clip(ShieldShape)
            .background(SolidColor(Color(0xFFFFD600))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ON", color = Color.Black, fontWeight = FontWeight.Black, fontSize = onSize, style = Tight)
            Text("THA", color = Color.Black, fontWeight = FontWeight.Black, fontSize = thaSize, style = Tight)
            Text("SET", color = Color.Black, fontWeight = FontWeight.Black, fontSize = setSize, style = Tight)
        }
    }
}

// Pulls the line height in tight to the glyph height so the three rows stack flush
// without the default text leading pushing them apart.
private val Tight = TextStyle(lineHeight = 1.0.em)

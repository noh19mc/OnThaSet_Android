package com.onthaset.app.auth.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background

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
fun OnThaSetShield(size: androidx.compose.ui.unit.Dp = 120.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(ShieldShape)
            .background(SolidColor(Color(0xFFFFD600))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ON", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("THA", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text("SET", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
    }
}

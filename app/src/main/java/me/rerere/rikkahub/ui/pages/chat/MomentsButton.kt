package me.rerere.rikkahub.ui.pages.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun MomentsButton(
    hasUnread: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        IconButton(onClick = onClick) {
            MomentsLensIcon(
                tint = LocalContentColor.current,
                modifier = Modifier.size(24.dp)
            )
        }
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
            )
        }
    }
}

@Composable
private fun MomentsLensIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val diameter = min(size.width, size.height)
        val radius = diameter * 0.42f
        val center = Offset(size.width / 2f, size.height / 2f)
        val stroke = Stroke(
            width = diameter * 0.085f,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = stroke,
        )
        repeat(6) { index ->
            val angle = Math.toRadians((index * 60f + 18f).toDouble())
            val nextAngle = Math.toRadians((index * 60f + 48f).toDouble())
            val inner = Offset(
                x = center.x + cos(angle).toFloat() * radius * 0.22f,
                y = center.y + sin(angle).toFloat() * radius * 0.22f,
            )
            val outer = Offset(
                x = center.x + cos(nextAngle).toFloat() * radius * 0.72f,
                y = center.y + sin(nextAngle).toFloat() * radius * 0.72f,
            )
            drawLine(
                color = tint,
                start = inner,
                end = outer,
                strokeWidth = diameter * 0.075f,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(
            color = tint,
            radius = radius * 0.13f,
            center = center,
        )
    }
}

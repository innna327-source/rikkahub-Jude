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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AnonymousQuestionBoxButton(hasUnread: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier) {
        IconButton(onClick = onClick) {
            val tint = LocalContentColor.current
            Canvas(Modifier.size(24.dp)) {
                val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val left = size.width * .16f
                val right = size.width * .84f
                val top = size.height * .24f
                val bottom = size.height * .76f
                drawRoundRect(color = tint, topLeft = Offset(left, top), size = androidx.compose.ui.geometry.Size(right - left, bottom - top), cornerRadius = CornerRadius(3.dp.toPx()), style = stroke)
                drawPath(Path().apply {
                    moveTo(left + 1.dp.toPx(), top + 1.dp.toPx())
                    lineTo(size.width / 2f, size.height * .54f)
                    lineTo(right - 1.dp.toPx(), top + 1.dp.toPx())
                }, tint, style = stroke)
            }
        }
        if (hasUnread) Box(Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp).size(8.dp).clip(CircleShape).background(Color(0xFFE53935)))
    }
}

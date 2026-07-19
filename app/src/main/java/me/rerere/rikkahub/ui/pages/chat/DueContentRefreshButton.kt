package me.rerere.rikkahub.ui.pages.chat

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Refresh01

@Composable
internal fun DueContentRefreshButton(
    processing: Boolean,
    contentDescription: String?,
    color: Color,
    onRefresh: () -> Unit,
) {
    IconButton(
        onClick = onRefresh,
        enabled = !processing,
    ) {
        if (processing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = color,
            )
        } else {
            Icon(
                imageVector = HugeIcons.Refresh01,
                contentDescription = contentDescription,
                tint = color,
            )
        }
    }
}

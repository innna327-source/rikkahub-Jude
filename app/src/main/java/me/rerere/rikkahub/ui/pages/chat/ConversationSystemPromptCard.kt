package me.rerere.rikkahub.ui.pages.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Setting07
import me.rerere.rikkahub.R

@Composable
fun ConversationSystemPromptButton(
    customSystemPrompt: String?,
    onSystemPromptChange: (String?) -> Unit,
    onEditorVisibilityChange: (Boolean) -> Unit = {},
) {
    var editorVisible by rememberSaveable { mutableStateOf(false) }
    var editText by rememberSaveable(customSystemPrompt) {
        mutableStateOf(customSystemPrompt ?: "")
    }

    LaunchedEffect(editorVisible) {
        onEditorVisibilityChange(editorVisible)
    }
    DisposableEffect(Unit) {
        onDispose {
            onEditorVisibilityChange(false)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextButton(
            onClick = {
                editText = customSystemPrompt ?: ""
                editorVisible = true
            },
        ) {
            Icon(
                imageVector = HugeIcons.Setting07,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = if (!customSystemPrompt.isNullOrBlank()) {
                    stringResource(R.string.chat_page_conversation_system_prompt) + " ✎"
                } else {
                    stringResource(R.string.chat_page_conversation_system_prompt)
                },
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }

    if (editorVisible) {
        AlertDialog(
            onDismissRequest = { editorVisible = false },
            modifier = Modifier.imePadding(),
            title = {
                Text(stringResource(R.string.chat_page_conversation_system_prompt))
            },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 360.dp),
                    label = { Text(stringResource(R.string.chat_page_conversation_system_prompt_hint)) },
                    minLines = 6,
                    maxLines = 16,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSystemPromptChange(editText.ifBlank { null })
                        editorVisible = false
                    },
                ) {
                    Text(stringResource(R.string.chat_page_conversation_system_prompt_save))
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (!customSystemPrompt.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                editText = ""
                                onSystemPromptChange(null)
                                editorVisible = false
                            },
                        ) {
                            Text(stringResource(R.string.chat_page_conversation_system_prompt_clear))
                        }
                    }
                    TextButton(
                        onClick = { editorVisible = false },
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            },
        )
    }
}

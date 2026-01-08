package jnu.kulipai.exam.ui.screens.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import jnu.kulipai.exam.R

@Composable
fun SelectionDialog(
    title: String,
    selections: List<String>,
    selected: Int = 0,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        text = {
            Column {
                selections.forEachIndexed { index, text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                onSelect(index)
                                onDismiss()
                            },
                        verticalAlignment = CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == index,
                            onClick = {
                                onSelect(index)
                                onDismiss()
                            }
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}


@Composable
fun InputDialog(
    title: String,
    initialValue: String = "",
    hint: String = "",
    onConfirm: (String) -> Unit,
    onNeutral:  (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 1. 管理输入框的状态
    var text by remember { mutableStateOf(initialValue) }

    // 2. 用于自动获取焦点
    val focusRequester = remember { FocusRequester() }

    // 3. 弹窗显示时请求焦点
    LaunchedEffect(Unit) {
        // 稍微延迟一下，确保 Dialog 已经渲染，防止焦点请求失败
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(hint) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester), // 绑定焦点
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onConfirm(text)
                            onDismiss()
                        }
                    )
                )
            }
        },
        // 确认按钮
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(text)
                    onDismiss()
                }
            ) {
                Text("保存并刷新") // 或者 stringResource(R.string.action_confirm)
            }
        },
        // 取消按钮
        dismissButton = {
            // 1. 中立按钮
            TextButton(
                onClick = {
                    onNeutral(text)
                    onDismiss()
                }
            ) {
                Text("恢复默认")
            }

            // 2. 取消按钮
            TextButton(onClick = onDismiss) {
                Text("取消")
            }

        },


    )
}
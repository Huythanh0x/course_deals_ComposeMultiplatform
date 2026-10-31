package com.thanh0x.coursedeals.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.stringResource
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitDealBottomSheetDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    var urlText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val invalidUrlMessage = stringResource(Res.string.url_error_invalid)

    fun submit() {
        val trimmed = urlText.trim()
        if (trimmed.isEmpty() || !trimmed.contains("udemy.com")) {
            errorMessage = invalidUrlMessage
        } else {
            errorMessage = null
            keyboardController?.hide()
            onSubmit(trimmed)
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.submit_deal_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(Res.string.submit_deal_subhead),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        if (errorMessage != null) errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(stringResource(Res.string.url_hint)) },
                    placeholder = { Text("https://www.udemy.com/course/...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submit() },
                    ),
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        onDismissRequest()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.btn_cancel))
                }

                Spacer(modifier = Modifier.height(0.dp))

                Button(
                    onClick = { submit() },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                ) {
                    Text(stringResource(Res.string.btn_submit))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SubmitDealBottomSheetPreview() {
    CourseDealsTheme {
        SubmitDealBottomSheetDialog(
            onDismissRequest = {},
            onSubmit = {},
        )
    }
}

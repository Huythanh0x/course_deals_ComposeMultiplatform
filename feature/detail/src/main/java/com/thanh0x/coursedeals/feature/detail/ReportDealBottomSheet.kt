package com.thanh0x.coursedeals.feature.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.StringResource
import com.thanh0x.coursedeals.core.ui.resources.stringResource

private data class ReportReasonItem(val resource: StringResource, val isOther: Boolean = false)

private val REPORT_REASONS = listOf(
    ReportReasonItem(Res.string.report_reason_not_free),
    ReportReasonItem(Res.string.report_reason_no_data),
    ReportReasonItem(Res.string.report_reason_expired),
    ReportReasonItem(Res.string.report_reason_other, isOther = true),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDealBottomSheet(
    onDismissRequest: () -> Unit,
    onSubmitReport: (reason: String, otherDetails: String?) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    var selectedReasonRes by remember { mutableStateOf<StringResource?>(null) }
    var otherReasonText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(Res.string.report_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ReportReasonSelector(
                selectedReason = selectedReasonRes,
                onSelectReason = {
                    selectedReasonRes = it
                    isError = false
                },
            )
            if (selectedReasonRes == Res.string.report_reason_other) {
                ReportOtherInput(
                    value = otherReasonText,
                    onValueChange = {
                        otherReasonText = it
                        if (isError && it.isNotBlank()) isError = false
                    },
                    isError = isError,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            ReportActionButtons(
                onCancel = onDismissRequest,
                canSubmit = selectedReasonRes != null,
                onSubmit = {
                    handleReportSubmit(
                        selectedReason = selectedReasonRes,
                        otherReasonText = otherReasonText,
                        onError = { isError = true },
                        onSubmit = onSubmitReport,
                    )
                },
            )
        }
    }
}

@Composable
private fun ReportReasonSelector(
    selectedReason: StringResource?,
    onSelectReason: (StringResource) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
    ) {
        REPORT_REASONS.forEach { item ->
            val reasonLabel = stringResource(item.resource)
            val selected = selectedReason == item.resource
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        onClick = { onSelectReason(item.resource) },
                        role = Role.RadioButton,
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = reasonLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ReportOtherInput(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(stringResource(Res.string.report_other_hint)) },
        isError = isError,
        supportingText = if (isError) {
            { Text(stringResource(Res.string.report_other_hint)) }
        } else {
            null
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    )
}

@Composable
private fun ReportActionButtons(
    onCancel: () -> Unit,
    canSubmit: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        TextButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(Res.string.btn_cancel))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(Res.string.btn_submit), fontWeight = FontWeight.Bold)
        }
    }
}

private fun handleReportSubmit(
    selectedReason: StringResource?,
    otherReasonText: String,
    onError: () -> Unit,
    onSubmit: (String, String?) -> Unit,
) {
    if (selectedReason == null) return
    val isOther = selectedReason == Res.string.report_reason_other
    val trimmed = otherReasonText.trim()
    if (isOther && trimmed.isEmpty()) {
        onError()
        return
    }
    val reasonKey = when (selectedReason) {
        Res.string.report_reason_not_free -> "Not free"
        Res.string.report_reason_no_data -> "No data"
        Res.string.report_reason_expired -> "Expired deal"
        else -> "Other"
    }
    onSubmit(reasonKey, if (isOther) trimmed else null)
}

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.R as CoreR

private data class ReportReasonItem(val resId: Int, val isOther: Boolean = false)

private val REPORT_REASONS = listOf(
    ReportReasonItem(CoreR.string.report_reason_not_free),
    ReportReasonItem(CoreR.string.report_reason_no_data),
    ReportReasonItem(CoreR.string.report_reason_expired),
    ReportReasonItem(CoreR.string.report_reason_other, isOther = true),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDealBottomSheet(
    onDismissRequest: () -> Unit,
    onSubmitReport: (reason: String, otherDetails: String?) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    var selectedReasonResId by remember { mutableStateOf<Int?>(null) }
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
                text = stringResource(CoreR.string.report_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ReportReasonSelector(
                selectedReasonResId = selectedReasonResId,
                onSelectReason = {
                    selectedReasonResId = it
                    isError = false
                },
            )
            if (selectedReasonResId == CoreR.string.report_reason_other) {
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
                canSubmit = selectedReasonResId != null,
                onSubmit = {
                    handleReportSubmit(
                        selectedReasonResId = selectedReasonResId,
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
    selectedReasonResId: Int?,
    onSelectReason: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
    ) {
        REPORT_REASONS.forEach { item ->
            val reasonLabel = stringResource(item.resId)
            val selected = selectedReasonResId == item.resId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        onClick = { onSelectReason(item.resId) },
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
        placeholder = { Text(stringResource(CoreR.string.report_other_hint)) },
        isError = isError,
        supportingText = if (isError) {
            { Text(stringResource(CoreR.string.report_other_hint)) }
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
            Text(stringResource(CoreR.string.btn_cancel))
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
            Text(stringResource(CoreR.string.btn_submit), fontWeight = FontWeight.Bold)
        }
    }
}

private fun handleReportSubmit(
    selectedReasonResId: Int?,
    otherReasonText: String,
    onError: () -> Unit,
    onSubmit: (String, String?) -> Unit,
) {
    if (selectedReasonResId == null) return
    val isOther = selectedReasonResId == CoreR.string.report_reason_other
    val trimmed = otherReasonText.trim()
    if (isOther && trimmed.isEmpty()) {
        onError()
        return
    }
    val reasonKey = when (selectedReasonResId) {
        CoreR.string.report_reason_not_free -> "Not free"
        CoreR.string.report_reason_no_data -> "No data"
        CoreR.string.report_reason_expired -> "Expired deal"
        else -> "Other"
    }
    onSubmit(reasonKey, if (isOther) trimmed else null)
}

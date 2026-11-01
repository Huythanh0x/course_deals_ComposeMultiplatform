package com.thanh0x.coursedeals.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.stringResource

@Suppress("LongParameterList")
@Composable
fun LoginFormSection(
    authMode: AuthMode,
    onSendOtpClick: (email: String) -> Unit,
    onSubmitOtp: (otp: String) -> Unit,
    onBackToEmailClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSubmitLogin: ((email: String, password: String) -> Unit)? = null,
    onSubmitRegister: ((email: String, password: String, fullName: String) -> Unit)? = null,
    onToggleAuthMode: (() -> Unit)? = null,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        when (authMode) {
            AuthMode.LOGIN, AuthMode.REGISTER -> {
                EmailField(value = email, onValueChange = { email = it })
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (onSubmitLogin != null) {
                            onSubmitLogin(email, "")
                        } else {
                            onSendOtpClick(email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(29.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.auth_btn_send_otp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            AuthMode.VERIFY_OTP -> {
                OtpField(value = otp, onValueChange = { otp = it })
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onSubmitOtp(otp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(29.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.auth_btn_verify_otp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        if (onToggleAuthMode != null) {
                            onToggleAuthMode()
                        } else {
                            onBackToEmailClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(text = stringResource(Res.string.auth_back_to_email))
                }
            }
        }
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(Res.string.auth_email_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )
}

@Composable
private fun OtpField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 6) onValueChange(it) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(Res.string.auth_otp_hint)) },
        supportingText = { Text(stringResource(Res.string.auth_otp_helper)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

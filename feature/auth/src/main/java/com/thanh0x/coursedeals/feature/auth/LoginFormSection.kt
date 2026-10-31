package com.thanh0x.coursedeals.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.stringResource

@Suppress("LongParameterList")
@Composable
fun LoginFormSection(
    authMode: AuthMode,
    onSubmitLogin: (email: String, password: String) -> Unit,
    onSubmitRegister: (email: String, password: String, fullName: String) -> Unit,
    onSubmitOtp: (otp: String) -> Unit,
    onToggleAuthMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        AuthInputFields(
            authMode = authMode,
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            fullName = fullName,
            onFullNameChange = { fullName = it },
            otp = otp,
            onOtpChange = { otp = it },
            isPasswordVisible = isPasswordVisible,
            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthSubmitButton(
            authMode = authMode,
            onClick = {
                when (authMode) {
                    AuthMode.LOGIN -> onSubmitLogin(email, password)
                    AuthMode.REGISTER -> onSubmitRegister(email, password, fullName)
                    AuthMode.VERIFY_OTP -> onSubmitOtp(otp)
                }
            },
        )
        Spacer(modifier = Modifier.height(4.dp))
        AuthToggleButton(authMode = authMode, onClick = onToggleAuthMode)
    }
}

@Suppress("LongParameterList")
@Composable
private fun AuthInputFields(
    authMode: AuthMode,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    otp: String,
    onOtpChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
) {
    when (authMode) {
        AuthMode.LOGIN -> {
            EmailField(value = email, onValueChange = onEmailChange)
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = password,
                onValueChange = onPasswordChange,
                isVisible = isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility,
            )
        }
        AuthMode.REGISTER -> {
            FullNameField(value = fullName, onValueChange = onFullNameChange)
            Spacer(modifier = Modifier.height(8.dp))
            EmailField(value = email, onValueChange = onEmailChange)
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = password,
                onValueChange = onPasswordChange,
                isVisible = isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility,
            )
        }
        AuthMode.VERIFY_OTP -> {
            OtpField(value = otp, onValueChange = onOtpChange)
        }
    }
}

@Composable
private fun FullNameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(Res.string.auth_full_name_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )
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
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(Res.string.auth_password_hint)) },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                )
            }
        },
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

@Composable
private fun AuthSubmitButton(
    authMode: AuthMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textRes = when (authMode) {
        AuthMode.LOGIN -> Res.string.auth_btn_sign_in
        AuthMode.REGISTER -> Res.string.auth_btn_create_account
        AuthMode.VERIFY_OTP -> Res.string.auth_btn_verify_otp
    }
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(29.dp),
    ) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun AuthToggleButton(
    authMode: AuthMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textRes = when (authMode) {
        AuthMode.LOGIN -> Res.string.auth_toggle_to_register
        AuthMode.REGISTER -> Res.string.auth_toggle_to_login
        AuthMode.VERIFY_OTP -> Res.string.auth_toggle_back_to_login
    }
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(text = stringResource(textRes))
    }
}

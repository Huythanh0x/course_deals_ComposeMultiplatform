package com.thanh0x.coursedeals.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import com.thanh0x.coursedeals.core.common.BuildConfig
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.stringResource
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
    onEmailClick: () -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
    onRemoveCategory: (String) -> Unit = {},
    onAddKeyword: (String) -> Unit = {},
    onRemoveKeyword: (String) -> Unit = {},
    onDarkModeToggled: (Boolean) -> Unit = {},
    onFingerprintToggled: (Boolean) -> Unit = {},
    onNotificationsToggled: (Boolean) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onDevEmailClick: () -> Unit = {},
    onDevWebsiteClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        // Main Scrollable Profile Content
        val contentModifier = if (uiState.isGuestMode) {
            Modifier
                .fillMaxSize()
                .blur(16.dp)
        } else {
            Modifier.fillMaxSize()
        }

        Box(modifier = contentModifier) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isLandscape = maxWidth > 600.dp

                if (isLandscape) {
                    ProfileContentLandscape(
                        uiState = uiState,
                        onEmailClick = onEmailClick,
                        onAddCategoryClick = onAddCategoryClick,
                        onRemoveCategory = onRemoveCategory,
                        onAddKeyword = onAddKeyword,
                        onRemoveKeyword = onRemoveKeyword,
                        onDarkModeToggled = onDarkModeToggled,
                        onFingerprintToggled = onFingerprintToggled,
                        onNotificationsToggled = onNotificationsToggled,
                        onLogoutClick = onLogoutClick,
                        onDevEmailClick = onDevEmailClick,
                        onDevWebsiteClick = onDevWebsiteClick,
                    )
                } else {
                    ProfileContentPortrait(
                        uiState = uiState,
                        onEmailClick = onEmailClick,
                        onAddCategoryClick = onAddCategoryClick,
                        onRemoveCategory = onRemoveCategory,
                        onAddKeyword = onAddKeyword,
                        onRemoveKeyword = onRemoveKeyword,
                        onDarkModeToggled = onDarkModeToggled,
                        onFingerprintToggled = onFingerprintToggled,
                        onNotificationsToggled = onNotificationsToggled,
                        onLogoutClick = onLogoutClick,
                        onDevEmailClick = onDevEmailClick,
                        onDevWebsiteClick = onDevWebsiteClick,
                    )
                }
            }

            if (uiState.isLoading || uiState.isSyncing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }

        // Guest Mode Auth Gate Overlay
        if (uiState.isGuestMode) {
            GuestAuthGateOverlay(
                onSignInClick = onSignInClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ProfileContentPortrait(
    uiState: ProfileUiState,
    onEmailClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onDarkModeToggled: (Boolean) -> Unit,
    onFingerprintToggled: (Boolean) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onDevEmailClick: () -> Unit,
    onDevWebsiteClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UserHeaderSection(
            uiState = uiState,
            onEmailClick = onEmailClick,
            avatarSize = 64.dp
        )

        Spacer(modifier = Modifier.height(32.dp))

        PreferencesSection(
            favCategories = uiState.favCategories,
            favKeywords = uiState.favKeywords,
            onAddCategoryClick = onAddCategoryClick,
            onRemoveCategory = onRemoveCategory,
            onAddKeyword = onAddKeyword,
            onRemoveKeyword = onRemoveKeyword,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(
            isDarkModeEnabled = uiState.isDarkModeEnabled,
            isFingerprintEnabled = uiState.isFingerprintEnabled,
            isNotificationsEnabled = uiState.isNotificationsEnabled,
            isGuestMode = uiState.isGuestMode,
            onDarkModeToggled = onDarkModeToggled,
            onFingerprintToggled = onFingerprintToggled,
            onNotificationsToggled = onNotificationsToggled,
            modifier = Modifier.fillMaxWidth()
        )

        if (!uiState.isGuestMode) {
            Spacer(modifier = Modifier.height(48.dp))
            LogoutButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        FooterLinksSection(
            onDevEmailClick = onDevEmailClick,
            onDevWebsiteClick = onDevWebsiteClick
        )
    }
}

@Composable
private fun ProfileContentLandscape(
    uiState: ProfileUiState,
    onEmailClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onDarkModeToggled: (Boolean) -> Unit,
    onFingerprintToggled: (Boolean) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onDevEmailClick: () -> Unit,
    onDevWebsiteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left Column (User & Logout & Links)
        Column(
            modifier = Modifier.weight(0.4f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UserHeaderSection(
                uiState = uiState,
                onEmailClick = onEmailClick,
                avatarSize = 80.dp
            )

            if (!uiState.isGuestMode) {
                Spacer(modifier = Modifier.height(32.dp))
                LogoutButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            FooterLinksSection(
                onDevEmailClick = onDevEmailClick,
                onDevWebsiteClick = onDevWebsiteClick
            )
        }

        // Right Column (Preferences & Settings)
        Column(
            modifier = Modifier.weight(0.6f)
        ) {
            PreferencesSection(
                favCategories = uiState.favCategories,
                favKeywords = uiState.favKeywords,
                onAddCategoryClick = onAddCategoryClick,
                onRemoveCategory = onRemoveCategory,
                onAddKeyword = onAddKeyword,
                onRemoveKeyword = onRemoveKeyword,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(
                isDarkModeEnabled = uiState.isDarkModeEnabled,
                isFingerprintEnabled = uiState.isFingerprintEnabled,
                isNotificationsEnabled = uiState.isNotificationsEnabled,
                isGuestMode = uiState.isGuestMode,
                onDarkModeToggled = onDarkModeToggled,
                onFingerprintToggled = onFingerprintToggled,
                onNotificationsToggled = onNotificationsToggled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UserHeaderSection(
    uiState: ProfileUiState,
    onEmailClick: () -> Unit,
    avatarSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(avatarSize * 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Name
        val displayName = when {
            uiState.isGuestMode -> stringResource(Res.string.guest_user_title)
            uiState.displayName.isNotEmpty() -> uiState.displayName
            else -> "User Name"
        }

        Text(
            text = displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Email Container
        val email = when {
            uiState.isGuestMode -> stringResource(Res.string.guest_user_subtitle)
            uiState.email.isNotEmpty() -> uiState.email
            else -> "user@example.com"
        }

        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onEmailClick)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!uiState.isGuestMode) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy Email",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferencesSection(
    favCategories: List<String>,
    favKeywords: List<String>,
    onAddCategoryClick: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isAddingKeyword by remember { mutableStateOf(false) }
    var keywordInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column(modifier = modifier) {
        // Favorite Categories Header
        Text(
            text = stringResource(Res.string.fav_cats_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            favCategories.forEach { category ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text(category) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove category",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onRemoveCategory(category) }
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Add Category Chip
            InputChip(
                selected = false,
                onClick = onAddCategoryClick,
                label = { },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Category",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = InputChipDefaults.inputChipColors(
                    containerColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Favorite Keywords Header
        Text(
            text = stringResource(Res.string.fav_kws_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isAddingKeyword) {
            val submitKeyword = {
                val trimmed = keywordInput.trim()
                if (trimmed.isNotEmpty()) {
                    onAddKeyword(trimmed)
                    keywordInput = ""
                }
                isAddingKeyword = false
            }

            OutlinedTextField(
                value = keywordInput,
                onValueChange = { keywordInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (!it.isFocused && keywordInput.isEmpty()) {
                            isAddingKeyword = false
                        }
                    },
                placeholder = { Text(stringResource(Res.string.kw_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submitKeyword() }),
                trailingIcon = {
                    IconButton(onClick = submitKeyword) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Keyword"
                        )
                    }
                }
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            favKeywords.forEach { keyword ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text(keyword) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove keyword",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onRemoveKeyword(keyword) }
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (!isAddingKeyword) {
                // Add Keyword Chip
                InputChip(
                    selected = false,
                    onClick = {
                        isAddingKeyword = true
                    },
                    label = { },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Keyword",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    isDarkModeEnabled: Boolean,
    isFingerprintEnabled: Boolean,
    isNotificationsEnabled: Boolean,
    isGuestMode: Boolean,
    onDarkModeToggled: (Boolean) -> Unit,
    onFingerprintToggled: (Boolean) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.settings_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Dark Mode Switch
        SettingSwitchRow(
            title = stringResource(Res.string.enable_dark_mode_switch_text),
            checked = isDarkModeEnabled,
            onCheckedChange = onDarkModeToggled
        )

        // Fingerprint Switch
        if (BuildConfig.FEATURE_FINGERPRINT) {
            SettingSwitchRow(
                title = stringResource(Res.string.enable_finger_print_switch_text),
                checked = if (isGuestMode) false else isFingerprintEnabled,
                onCheckedChange = onFingerprintToggled,
                enabled = !isGuestMode
            )
        }

        // Notifications Master Switch
        SettingSwitchRow(
            title = stringResource(Res.string.notif_master),
            checked = isNotificationsEnabled,
            onCheckedChange = onNotificationsToggled
        )

        // Notification Sub-toggles
        if (isNotificationsEnabled) {
            SettingSubSwitchRow(
                title = stringResource(Res.string.notif_cat),
                checked = true,
                onCheckedChange = {}
            )
            SettingSubSwitchRow(
                title = stringResource(Res.string.notif_kw),
                checked = true,
                onCheckedChange = {}
            )
            SettingSubSwitchRow(
                title = stringResource(Res.string.notif_hot),
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun SettingSubSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(start = 32.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun LogoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(
            text = stringResource(Res.string.logout_button_text),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FooterLinksSection(
    onDevEmailClick: () -> Unit,
    onDevWebsiteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.dev_email),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onDevEmailClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.dev_website),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onDevWebsiteClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun GuestAuthGateOverlay(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(58.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.guest_profile_auth_gate_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.guest_profile_auth_gate_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = stringResource(Res.string.sign_in_cta))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    CourseDealsTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                displayName = "Alex Doe",
                email = "alex.doe@example.com",
                favCategories = listOf("Development", "Design"),
                favKeywords = listOf("Kotlin", "Jetpack Compose", "Clean Architecture"),
                isDarkModeEnabled = false,
                isFingerprintEnabled = true,
                isNotificationsEnabled = true
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenGuestPreview() {
    CourseDealsTheme {
        ProfileScreen(
            uiState = ProfileUiState(isGuestMode = true)
        )
    }
}

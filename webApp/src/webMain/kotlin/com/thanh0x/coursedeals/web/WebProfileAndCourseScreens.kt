package com.thanh0x.coursedeals.web

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val cmpNotificationTopics =
    listOf(
        "all_deals" to "All Deals",
        "hot_deals" to "Hot Deals",
        "development" to "Development",
        "design" to "Design",
        "business" to "Business",
        "it_software" to "IT & Software",
        "marketing" to "Marketing",
        "personal_development" to "Personal Development",
        "photography" to "Photography",
        "music" to "Music",
        "health_fitness" to "Health & Fitness",
        "finance" to "Finance",
    )

@Composable
internal fun WebCourseScreen(
    bookmarkedCourses: List<WebCouponSummary>,
    onCouponClick: (WebCouponSummary) -> Unit,
    onOpenUdemyMyCourses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = CmpIcons.SchoolFilled,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "My Enrolled Udemy Courses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Open your Udemy learning library to continue watching redeemed courses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onOpenUdemyMyCourses,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text("Open Udemy Library", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bookmarked Deals (${bookmarkedCourses.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        if (bookmarkedCourses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = CmpIcons.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No bookmarked courses yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the bookmark icon on any course detail screen to save it here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 240.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(bookmarkedCourses, key = { it.courseId }) { course ->
                    CmpCouponCourseCard(
                        coupon = course,
                        onClick = { onCouponClick(course) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun WebProfileScreen(
    displayName: String,
    email: String,
    isSignedIn: Boolean,
    favCategories: List<String>,
    favKeywords: List<String>,
    isDarkModeEnabled: Boolean,
    isNotificationsEnabled: Boolean,
    subscribedTopics: Set<String>,
    onAddCategory: (String) -> Unit,
    onRemoveCategory: (String) -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onDarkModeToggled: (Boolean) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onTopicToggled: (String, Boolean) -> Unit,
    onSignInSuccess: (name: String, userEmail: String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showLoginScreen by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    if (showLoginScreen) {
        WebLoginScreen(
            onSignInSuccess = { name, userEmail ->
                showLoginScreen = false
                onSignInSuccess(name, userEmail)
            },
            onGuestModeClick = {
                showLoginScreen = false
            },
        )
        return
    }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        val isLandscape = maxWidth > 680.dp
        if (isLandscape) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(
                    modifier = Modifier.weight(0.4f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProfileUserHeader(
                        displayName = if (isSignedIn) displayName else "Guest User",
                        email =
                            if (isSignedIn) {
                                email
                            } else {
                                "Sign in to save preferences & sync across devices"
                            },
                        isSignedIn = isSignedIn,
                        avatarSize = 80.dp,
                        onEmailClick = {
                            if (isSignedIn) clipboardManager.setText(AnnotatedString(email))
                        },
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    if (isSignedIn) {
                        ProfileLogoutButton(
                            onClick = onLogoutClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Button(
                            onClick = { showLoginScreen = true },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileFooterLinks()
                }

                Column(modifier = Modifier.weight(0.6f)) {
                    ProfilePreferencesSection(
                        favCategories = favCategories,
                        favKeywords = favKeywords,
                        onAddCategoryClick = { showCategoryPicker = true },
                        onRemoveCategory = onRemoveCategory,
                        onAddKeyword = onAddKeyword,
                        onRemoveKeyword = onRemoveKeyword,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileSettingsSection(
                        isDarkModeEnabled = isDarkModeEnabled,
                        isNotificationsEnabled = isNotificationsEnabled,
                        subscribedTopics = subscribedTopics,
                        onDarkModeToggled = onDarkModeToggled,
                        onNotificationsToggled = onNotificationsToggled,
                        onTopicToggled = onTopicToggled,
                    )
                }
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileUserHeader(
                    displayName = if (isSignedIn) displayName else "Guest User",
                    email =
                        if (isSignedIn) {
                            email
                        } else {
                            "Sign in to save preferences & sync across devices"
                        },
                    isSignedIn = isSignedIn,
                    avatarSize = 64.dp,
                    onEmailClick = {
                        if (isSignedIn) clipboardManager.setText(AnnotatedString(email))
                    },
                )
                Spacer(modifier = Modifier.height(20.dp))
                if (!isSignedIn) {
                    Button(
                        onClick = { showLoginScreen = true },
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                ProfilePreferencesSection(
                    favCategories = favCategories,
                    favKeywords = favKeywords,
                    onAddCategoryClick = { showCategoryPicker = true },
                    onRemoveCategory = onRemoveCategory,
                    onAddKeyword = onAddKeyword,
                    onRemoveKeyword = onRemoveKeyword,
                )
                Spacer(modifier = Modifier.height(24.dp))
                ProfileSettingsSection(
                    isDarkModeEnabled = isDarkModeEnabled,
                    isNotificationsEnabled = isNotificationsEnabled,
                    subscribedTopics = subscribedTopics,
                    onDarkModeToggled = onDarkModeToggled,
                    onNotificationsToggled = onNotificationsToggled,
                    onTopicToggled = onTopicToggled,
                )
                if (isSignedIn) {
                    Spacer(modifier = Modifier.height(32.dp))
                    ProfileLogoutButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                ProfileFooterLinks()
            }
        }
    }

    if (showCategoryPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCategoryPicker = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    text = "Favorite Categories",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    allCmpCategories.forEach { cat ->
                        val selected = favCategories.contains(cat)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) onRemoveCategory(cat) else onAddCategory(cat)
                            },
                            label = { Text(cat) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showCategoryPicker = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun ProfileUserHeader(
    displayName: String,
    email: String,
    isSignedIn: Boolean,
    avatarSize: Dp,
    onEmailClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(avatarSize * 0.6f),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier =
                Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onEmailClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isSignedIn) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = CmpIcons.ContentCopy,
                    contentDescription = "Copy Email",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfilePreferencesSection(
    favCategories: List<String>,
    favKeywords: List<String>,
    onAddCategoryClick: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
) {
    var isAddingKeyword by remember { mutableStateOf(false) }
    var keywordInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Favorite Categories",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.size(16.dp).clickable { onRemoveCategory(category) },
                        )
                    },
                    colors =
                        InputChipDefaults.inputChipColors(
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                )
            }
            InputChip(
                selected = false,
                onClick = onAddCategoryClick,
                label = { Text("Add") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Category",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                },
                colors = InputChipDefaults.inputChipColors(containerColor = Color.Transparent),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Favorite Keywords",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                placeholder = { Text("Add keyword") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submitKeyword() }),
                trailingIcon = {
                    IconButton(onClick = submitKeyword) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Keyword")
                    }
                },
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.size(16.dp).clickable { onRemoveKeyword(keyword) },
                        )
                    },
                    colors =
                        InputChipDefaults.inputChipColors(
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                )
            }
            if (!isAddingKeyword) {
                InputChip(
                    selected = false,
                    onClick = { isAddingKeyword = true },
                    label = { Text("Add Keyword") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Keyword",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun ProfileSettingsSection(
    isDarkModeEnabled: Boolean,
    isNotificationsEnabled: Boolean,
    subscribedTopics: Set<String>,
    onDarkModeToggled: (Boolean) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onTopicToggled: (String, Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        ProfileSwitchRow(
            title = "Dark Mode",
            checked = isDarkModeEnabled,
            onCheckedChange = onDarkModeToggled,
        )
        ProfileSwitchRow(
            title = "Notifications",
            checked = isNotificationsEnabled,
            onCheckedChange = onNotificationsToggled,
        )
        if (isNotificationsEnabled) {
            cmpNotificationTopics.forEach { (topicId, label) ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { onTopicToggled(topicId, !subscribedTopics.contains(topicId)) }
                            .padding(start = 32.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Switch(
                        checked = subscribedTopics.contains(topicId),
                        onCheckedChange = { checked -> onTopicToggled(topicId, checked) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ProfileLogoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border =
            ButtonDefaults
                .outlinedButtonBorder(enabled = true)
                .copy(brush = SolidColor(MaterialTheme.colorScheme.error)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
    ) {
        Text("Log Out", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileFooterLinks() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "huythanh0x@gmail.com",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { openExternalTab("mailto:huythanh0x@gmail.com") }.padding(4.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Terms of Service",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { openExternalTab("/terms") }.padding(4.dp),
            )
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { openExternalTab("/privacy") }.padding(4.dp),
            )
        }
    }
}

@Composable
internal fun WebLoginScreen(
    onSignInSuccess: (name: String, email: String) -> Unit,
    onGuestModeClick: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                imageVector = CmpIcons.LogoMark,
                contentDescription = "Course Deal",
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Course Deal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (errorMsg != null) {
                Text(
                    text = errorMsg ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            if (isRegister) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name (optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMsg = null
                },
                label = { Text("Email") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMsg = null
                },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
                        errorMsg = "Email address is not valid, re-check it !!!"
                    } else {
                        val resolvedName =
                            fullName.trim().ifEmpty {
                                trimmedEmail.substringBefore("@")
                            }
                        onSignInSuccess(resolvedName, trimmedEmail)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(
                    text = if (isRegister) "Create Account" else "Sign In",
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { isRegister = !isRegister }) {
                Text(
                    text =
                        if (isRegister) {
                            "Already have an account? Sign In"
                        } else {
                            "Don't have an account? Register"
                        },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Or continue with",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onSignInSuccess("Vo Huy Thanh", "huythanh0x@gmail.com") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Image(
                    imageVector = CmpIcons.Google,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onGuestModeClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Access as Guest", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

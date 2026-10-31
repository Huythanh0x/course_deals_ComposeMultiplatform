package com.thanh0x.coursedeals.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanh0x.coursedeals.core.common.SystemTimeProvider
import com.thanh0x.coursedeals.core.common.TimeLeft

data class DealItem(
    val id: String,
    val title: String,
    val instructor: String,
    val category: String,
    val rating: Double,
    val reviewCount: Int,
    val originalPrice: String,
    val couponCode: String,
    val expiryHours: Int,
    val description: String,
)

enum class AppTab(val title: String) {
    HOME("Home"),
    COURSES("Courses"),
    PROFILE("Profile")
}

@Composable
fun App() {
    MaterialTheme {
        var currentTab by remember { mutableStateOf(AppTab.HOME) }
        var selectedDeal by remember { mutableStateOf<DealItem?>(null) }
        var isDarkMode by remember { mutableStateOf(true) }

        val deals = remember {
            listOf(
                DealItem(
                    id = "1",
                    title = "The Complete 2026 Web Development Bootcamp",
                    instructor = "Dr. Angela Yu",
                    category = "Development",
                    rating = 4.8,
                    reviewCount = 384210,
                    originalPrice = "\$84.99",
                    couponCode = "WEBDEV2026FREE",
                    expiryHours = 18,
                    description = "Master full-stack web development with HTML, CSS, JavaScript, React, Node.js, and multiplatform paradigms."
                ),
                DealItem(
                    id = "2",
                    title = "Kotlin Multiplatform (KMP) & Compose Multiplatform Masterclass",
                    instructor = "Philipp Lackner",
                    category = "Mobile",
                    rating = 4.9,
                    reviewCount = 29540,
                    originalPrice = "\$79.99",
                    couponCode = "KMPCOMPOSENOW",
                    expiryHours = 24,
                    description = "Build production-grade iOS, Android, and Desktop apps sharing 100% of UI and domain logic using Compose Multiplatform."
                ),
                DealItem(
                    id = "3",
                    title = "Machine Learning A-Z: AI, Python & Deep Learning",
                    instructor = "Kirill Eremenko & Hadelin de Ponteves",
                    category = "Data Science",
                    rating = 4.7,
                    reviewCount = 182390,
                    originalPrice = "\$94.99",
                    couponCode = "AIDATASETFREE",
                    expiryHours = 8,
                    description = "Learn to create Machine Learning Algorithms in Python from two Data Science experts. Code templates included."
                ),
                DealItem(
                    id = "4",
                    title = "Clean Architecture & Koin in Modern Mobile Apps",
                    instructor = "Course Deals Academy",
                    category = "Architecture",
                    rating = 4.9,
                    reviewCount = 12480,
                    originalPrice = "\$49.99",
                    couponCode = "CLEANARCH100",
                    expiryHours = 36,
                    description = "Explore Clean Architecture, Repository Pattern, and Koin Multiplatform Dependency Injection."
                ),
            )
        }

        val bgColor = if (isDarkMode) Color(0xFF111318) else Color(0xFFF9F9FC)
        val surfaceColor = if (isDarkMode) Color(0xFF1E2025) else Color.White
        val textColor = if (isDarkMode) Color.White else Color(0xFF191C20)
        val brandColor = Color(0xFF0B57D0)

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bgColor,
        ) {
            Scaffold(
                containerColor = bgColor,
                bottomBar = {
                    NavigationBar(
                        containerColor = surfaceColor,
                        tonalElevation = 8.dp,
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppTab.HOME,
                            onClick = { currentTab = AppTab.HOME },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = brandColor,
                                selectedTextColor = brandColor,
                                indicatorColor = brandColor.copy(alpha = 0.15f),
                            ),
                        )
                        NavigationBarItem(
                            selected = currentTab == AppTab.COURSES,
                            onClick = { currentTab = AppTab.COURSES },
                            icon = { Icon(Icons.Default.Book, contentDescription = "Courses") },
                            label = { Text("Courses") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = brandColor,
                                selectedTextColor = brandColor,
                                indicatorColor = brandColor.copy(alpha = 0.15f),
                            ),
                        )
                        NavigationBarItem(
                            selected = currentTab == AppTab.PROFILE,
                            onClick = { currentTab = AppTab.PROFILE },
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = brandColor,
                                selectedTextColor = brandColor,
                                indicatorColor = brandColor.copy(alpha = 0.15f),
                            ),
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    when (currentTab) {
                        AppTab.HOME -> HomeFeedTab(
                            deals = deals,
                            surfaceColor = surfaceColor,
                            textColor = textColor,
                            brandColor = brandColor,
                            onDealClick = { selectedDeal = it }
                        )
                        AppTab.COURSES -> CoursesDirectoryTab(
                            deals = deals,
                            surfaceColor = surfaceColor,
                            textColor = textColor,
                            brandColor = brandColor,
                            onDealClick = { selectedDeal = it }
                        )
                        AppTab.PROFILE -> ProfileSettingsTab(
                            surfaceColor = surfaceColor,
                            textColor = textColor,
                            brandColor = brandColor,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = it }
                        )
                    }

                    selectedDeal?.let { deal ->
                        DealDetailModal(
                            deal = deal,
                            surfaceColor = surfaceColor,
                            textColor = textColor,
                            brandColor = brandColor,
                            onDismiss = { selectedDeal = null }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeFeedTab(
    deals: List<DealItem>,
    surfaceColor: Color,
    textColor: Color,
    brandColor: Color,
    onDealClick: (DealItem) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Development", "Mobile", "Data Science", "Architecture")
    val filteredDeals = deals.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
                (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Course Deals",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    )
                    Text(
                        text = "Free 100% discount Udemy coupons",
                        fontSize = 13.sp,
                        color = Color(0xFF9095A0),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(brandColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "CD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search coupons, topics, authors...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandColor,
                    unfocusedBorderColor = Color(0xFF333740),
                ),
                singleLine = true,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) brandColor else surfaceColor)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else textColor,
                        )
                    }
                }
            }
        }

        items(filteredDeals) { deal ->
            DealCard(
                deal = deal,
                surfaceColor = surfaceColor,
                textColor = textColor,
                brandColor = brandColor,
                onClick = { onDealClick(deal) },
            )
        }
    }
}

@Composable
private fun DealCard(
    deal: DealItem,
    surfaceColor: Color,
    textColor: Color,
    brandColor: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(brandColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = deal.category,
                        color = brandColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = "Time left",
                        tint = Color(0xFF80D49C),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${deal.expiryHours}h left",
                        color = Color(0xFF80D49C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = deal.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Instructor: ${deal.instructor}",
                fontSize = 13.sp,
                color = Color(0xFF9095A0),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${deal.rating}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    )
                    Text(
                        text = " (${deal.reviewCount / 1000}k)",
                        fontSize = 12.sp,
                        color = Color(0xFF9095A0),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = deal.originalPrice,
                        fontSize = 13.sp,
                        color = Color(0xFF9095A0),
                        textDecoration = TextDecoration.LineThrough,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00A86B))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "FREE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoursesDirectoryTab(
    deals: List<DealItem>,
    surfaceColor: Color,
    textColor: Color,
    brandColor: Color,
    onDealClick: (DealItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Course Categories",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
            Text(
                text = "Explore top rated free coupons across disciplines",
                fontSize = 13.sp,
                color = Color(0xFF9095A0),
            )
        }

        items(deals) { deal ->
            DealCard(
                deal = deal,
                surfaceColor = surfaceColor,
                textColor = textColor,
                brandColor = brandColor,
                onClick = { onDealClick(deal) },
            )
        }
    }
}

@Composable
private fun ProfileSettingsTab(
    surfaceColor: Color,
    textColor: Color,
    brandColor: Color,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Profile & Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(brandColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "User",
                        tint = brandColor,
                        modifier = Modifier.size(36.dp),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Guest User",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    )
                    Text(
                        text = "Browse & claim deals anonymously",
                        fontSize = 13.sp,
                        color = Color(0xFF9095A0),
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(text = "Dark Mode", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textColor)
                        Text(text = "Toggle dark/light theme", fontSize = 12.sp, color = Color(0xFF9095A0))
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onToggleDarkMode,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = brandColor)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(text = "Target Architecture", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textColor)
                        Text(text = "Compose Multiplatform on iOS 17+", fontSize = 12.sp, color = Color(0xFF9095A0))
                    }
                    Text(text = "Active", color = Color(0xFF80D49C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DealDetailModal(
    deal: DealItem,
    surfaceColor: Color,
    textColor: Color,
    brandColor: Color,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var copied by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(brandColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = deal.category,
                        color = brandColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = textColor)
                }
            }

            Text(
                text = deal.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )

            Text(
                text = "Taught by ${deal.instructor}",
                fontSize = 14.sp,
                color = Color(0xFF9095A0),
            )

            Text(
                text = deal.description,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.9f),
                lineHeight = 20.sp,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = brandColor.copy(alpha = 0.1f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(text = "Coupon Code", fontSize = 11.sp, color = Color(0xFF9095A0))
                        Text(text = deal.couponCode, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = brandColor)
                    }

                    Button(
                        onClick = { copied = true },
                        colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(
                            if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (copied) "Copied!" else "Copy Code")
                    }
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = "Claim")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enroll on Udemy for 100% Free", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

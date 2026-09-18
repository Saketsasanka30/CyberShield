package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.RbacRole
import com.example.ui.CyberShieldViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class CyberModule(
    val title: String,
    val shortName: String,
    val icon: ImageVector
) {
    OVERVIEW("Security Center", "Home", Icons.Default.Shield),
    INCIDENTS("Alerts & Incidents", "Alerts", Icons.Default.NotificationsActive),
    ASSETS("Protected Devices", "Devices", Icons.Default.Devices),
    VULNERABILITIES("Security Checks", "Checks", Icons.Default.CheckCircle),
    NETWORK("Network Scanner", "Network", Icons.Default.Hub),
    THREAT_INTEL("Threat Intelligence", "Threats", Icons.Default.Security),
    IDENTITY_CLOUD("Identity & Cloud", "Cloud", Icons.Default.Cloud),
    COMPLIANCE_RISK("Compliance Standards", "Compliance", Icons.Default.Assessment),
    REPORTS("Security Reports", "Reports", Icons.Default.Description),
    SETTINGS("Settings & Profile", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CyberShieldTheme {
                val viewModel: CyberShieldViewModel = viewModel()
                CyberShieldApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberShieldApp(viewModel: CyberShieldViewModel) {
    var currentModule by rememberSaveable { mutableStateOf(CyberModule.OVERVIEW) }
    var showRoleSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val currentRole by viewModel.currentRole.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    // Primary tabs shown in bottom navigation
    val primaryTabs = listOf(
        CyberModule.OVERVIEW,
        CyberModule.INCIDENTS,
        CyberModule.ASSETS,
        CyberModule.VULNERABILITIES
    )
    val isPrimaryTab = currentModule in primaryTabs

    // Handle back button to return to overview if on another screen
    BackHandler(enabled = currentModule != CyberModule.OVERVIEW) {
        currentModule = CyberModule.OVERVIEW
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        containerColor = CyberBgDark,
        contentWindowInsets = WindowInsets.systemBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "CyberShield",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(
                                color = CyberBlue.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { showRoleSheet = true }
                            ) {
                                Text(
                                    text = currentRole.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyberCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = currentModule.title,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    if (!isPrimaryTab) {
                        IconButton(
                            onClick = { currentModule = CyberModule.OVERVIEW },
                            modifier = Modifier.testTag("top_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = TextPrimary
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showRoleSheet = true },
                            modifier = Modifier.testTag("top_logo_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Security Shield",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp),
                            color = CyberCyan,
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(
                        onClick = { currentModule = CyberModule.SETTINGS },
                        modifier = Modifier.testTag("quick_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentModule == CyberModule.SETTINGS) CyberCyan else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurfaceDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            if (isPrimaryTab) {
                NavigationBar(
                    containerColor = CyberSurfaceDark,
                    contentColor = TextPrimary,
                    tonalElevation = 2.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("main_bottom_navigation")
                ) {
                    primaryTabs.forEach { module ->
                        val isSelected = currentModule == module
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = module.icon,
                                    contentDescription = module.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = module.shortName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            onClick = { currentModule = module },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyberBlue,
                                selectedTextColor = CyberCyan,
                                indicatorColor = CyberBlue.copy(alpha = 0.15f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_${module.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberBgDark)
        ) {
            AnimatedContent(
                targetState = currentModule,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    CyberModule.OVERVIEW -> {
                        OverviewScreen(
                            viewModel = viewModel,
                            onNavigateToVulnerabilities = { currentModule = CyberModule.VULNERABILITIES },
                            onNavigateToIncidents = { currentModule = CyberModule.INCIDENTS },
                            onNavigateToAssets = { currentModule = CyberModule.ASSETS },
                            onNavigateToNetwork = { currentModule = CyberModule.NETWORK },
                            onNavigateToReports = { currentModule = CyberModule.REPORTS },
                            onNavigateToThreats = { currentModule = CyberModule.THREAT_INTEL },
                            onNavigateToSettings = { currentModule = CyberModule.SETTINGS },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CyberModule.ASSETS -> {
                        AssetsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CyberModule.VULNERABILITIES -> {
                        VulnerabilitiesScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CyberModule.INCIDENTS -> {
                        IncidentsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CyberModule.THREAT_INTEL -> {
                        ThreatIntelScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CyberModule.NETWORK -> {
                        NetworkScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CyberModule.IDENTITY_CLOUD -> {
                        IdentityCloudScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CyberModule.COMPLIANCE_RISK -> {
                        ComplianceRiskScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CyberModule.REPORTS -> {
                        ReportsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CyberModule.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Role Switcher & Operator Modal Sheet
    if (showRoleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRoleSheet = false },
            containerColor = CyberSurfaceDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = CyberCardBorder) },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Security Profile & Role",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Switch role to preview permission tiers across the organization.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                RbacRole.values().forEach { role ->
                    val isSelected = currentRole == role
                    Surface(
                        color = if (isSelected) CyberBlue.copy(alpha = 0.15f) else CyberSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyberBlue else CyberCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.switchRole(role)
                                showRoleSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = role.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) CyberCyan else TextPrimary
                                )
                                Text(
                                    text = when (role) {
                                        RbacRole.ADMIN -> "Full read & write access across all tools"
                                        RbacRole.ANALYST -> "Triage alerts and remediate vulnerabilities"
                                        RbacRole.AUDITOR -> "Review compliance reports and logs"
                                        RbacRole.READ_ONLY -> "View-only dashboards"
                                    },
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showRoleSheet = false
                            currentModule = CyberModule.REPORTS
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reports", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            showRoleSheet = false
                            currentModule = CyberModule.SETTINGS
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Settings", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

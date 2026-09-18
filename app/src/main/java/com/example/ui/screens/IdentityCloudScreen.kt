package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.CyberShieldViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityCloudScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val identities by viewModel.identities.collectAsState()
    val cloudFindings by viewModel.cloudFindings.collectAsState()
    val apiEndpoints by viewModel.apiEndpoints.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Identity, 1 = Cloud Posture, 2 = API Security
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("identity_cloud_screen")
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Identity & Cloud",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "IAM accounts, multi-cloud posture & API inventory",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Primary Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberSurfaceDark,
            contentColor = CyberBlue,
            divider = { HorizontalDivider(color = CyberCardBorder) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("IAM (${identities.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                selectedContentColor = CyberBlue,
                unselectedContentColor = TextSecondary
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Cloud Posture (${cloudFindings.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                selectedContentColor = CyberBlue,
                unselectedContentColor = TextSecondary
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("APIs (${apiEndpoints.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                selectedContentColor = CyberBlue,
                unselectedContentColor = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> {
                // Identity & IAM Accounts
                val anomalies = identities.filter { it.anomalyFlag != null }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (anomalies.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberRed.copy(alpha = 0.08f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberRed.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(CyberRed.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PersonOff,
                                            contentDescription = null,
                                            tint = CyberRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "${anomalies.size} Identity Anomalies Detected",
                                            color = CyberRed,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Impossible travel or service accounts lacking MFA",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(identities, key = { it.id }) { account ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (account.anomalyFlag != null) CyberRed.copy(alpha = 0.4f) else CyberCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(CyberSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.AccountCircle,
                                                contentDescription = null,
                                                tint = CyberCyan,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = account.username,
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = account.email,
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    StatusPill(
                                        text = if (account.mfaEnabled) "MFA Active" else "MFA Missing",
                                        color = if (account.mfaEnabled) CyberGreen else CyberRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Role: ${account.role}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Risk: ${account.riskScore}/100",
                                        color = if (account.riskScore > 50) CyberRed else CyberGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (account.anomalyFlag != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = CyberRed.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberRed.copy(alpha = 0.25f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = CyberRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = account.anomalyFlag,
                                                color = CyberRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Cloud Security Posture (CSPM)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cloudFindings, key = { it.id }) { finding ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = CyberBlue.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBlue.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = finding.cloudProvider,
                                                color = CyberBlue,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = finding.resourceName,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    SeverityBadge(severity = finding.severity)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = finding.finding,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Standard: ${finding.complianceStandard}",
                                    color = CyberYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Remediation: ${finding.remediationStep}",
                                    color = CyberGreen,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // API Security Inventory
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(apiEndpoints, key = { it.id }) { api ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = if (api.method == "POST") CyberYellow.copy(alpha = 0.15f) else CyberGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (api.method == "POST") CyberYellow.copy(alpha = 0.3f) else CyberGreen.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Text(
                                                text = api.method,
                                                color = if (api.method == "POST") CyberYellow else CyberGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = api.path,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    SeverityBadge(severity = api.riskAssessment)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = if (api.authRequired) "Auth Enforced" else "Unauthenticated",
                                            color = if (api.authRequired) CyberGreen else CyberRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (api.rateLimitEnabled) "Rate Limited" else "No Rate Limit",
                                            color = if (api.rateLimitEnabled) CyberGreen else CyberYellow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = api.requestVolume24h,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

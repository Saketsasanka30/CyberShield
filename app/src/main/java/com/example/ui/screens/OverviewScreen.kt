package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun OverviewScreen(
    viewModel: CyberShieldViewModel,
    onNavigateToVulnerabilities: () -> Unit,
    onNavigateToIncidents: () -> Unit,
    onNavigateToAssets: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToThreats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val vulnerabilities by viewModel.vulnerabilities.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val criticalVulns = vulnerabilities.filter { it.severity == Severity.CRITICAL }
    val openIncidents = incidents.filter { it.status != IncidentStatus.RESOLVED && it.status != IncidentStatus.CLOSED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("overview_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Posture Score & Main Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_posture_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (criticalVulns.isEmpty()) CyberGreen else CyberOrange)
                                )
                                Text(
                                    text = if (criticalVulns.isEmpty()) "STATUS: SECURE" else "ATTENTION NEEDED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (criticalVulns.isEmpty()) CyberGreen else CyberOrange,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Security Health",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${assets.size} endpoints monitored • ${metrics.assetHealthPercentage}% operational",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        RiskScoreDial(
                            score = metrics.riskScore,
                            grade = metrics.riskGrade,
                            sizeDp = 100.dp
                        )
                    }

                    HorizontalDivider(color = CyberCardBorder)

                    // Primary Scan Action
                    PrimaryButton(
                        text = if (isScanning) "Running Security Audit..." else "Run Safe Security Audit",
                        icon = Icons.Default.Radar,
                        isLoading = isScanning,
                        onClick = { viewModel.runSafeScan("demo.internal.corp") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_audit_button")
                    )
                }
            }
        }

        // Section Title: Key Metrics at a Glance
        item {
            Text(
                text = "Overview Telemetry",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        // 4 Core Metrics (2x2 Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Protected Devices",
                        value = "${assets.size}",
                        subtitle = "${metrics.assetHealthPercentage}% nominal health",
                        icon = Icons.Default.Devices,
                        accentColor = CyberCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAssets
                    )
                    StatMetricCard(
                        title = "Open Alerts",
                        value = "${openIncidents.size}",
                        subtitle = "Active investigations",
                        icon = Icons.Default.NotificationsActive,
                        accentColor = if (openIncidents.isEmpty()) CyberGreen else CyberOrange,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToIncidents
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Critical Checks",
                        value = "${criticalVulns.size}",
                        subtitle = "CVSS >= 9.0 pending patch",
                        icon = Icons.Default.BugReport,
                        accentColor = if (criticalVulns.isEmpty()) CyberGreen else CyberRed,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVulnerabilities
                    )
                    StatMetricCard(
                        title = "Network Endpoints",
                        value = "${metrics.exposedServicesCount}",
                        subtitle = "Authorized monitored ports",
                        icon = Icons.Default.Hub,
                        accentColor = CyberBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToNetwork
                    )
                }
            }
        }

        // Urgent Action Items (if any critical vulnerabilities exist)
        if (criticalVulns.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Urgent Remediation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    TextButton(onClick = onNavigateToVulnerabilities) {
                        Text("View all (${vulnerabilities.size})", color = CyberCyan, fontSize = 12.sp)
                    }
                }
            }

            // Show top 2 critical items
            val topCritical = criticalVulns.take(2)
            items(topCritical.size) { index ->
                val vuln = topCritical[index]
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberRed.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToVulnerabilities() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SeverityBadge(severity = vuln.severity)
                                Text(
                                    text = vuln.cve,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            StatusPill(
                                text = vuln.remediationStatus.label,
                                color = when (vuln.remediationStatus) {
                                    RemediationStatus.RESOLVED -> CyberGreen
                                    RemediationStatus.IN_PROGRESS -> CyberYellow
                                    else -> CyberRed
                                }
                            )
                        }

                        Text(
                            text = vuln.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Device: ${vuln.affectedAsset}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Tap to review",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Infrastructure Device Distribution Chart
        item {
            AssetHealthDonutChart(
                assets = assets,
                onSliceClick = { onNavigateToAssets() }
            )
        }

        // Quick Tools Row
        item {
            Text(
                text = "Security Tools",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecondaryButton(
                    text = "Network Scanner",
                    icon = Icons.Default.Hub,
                    onClick = onNavigateToNetwork,
                    modifier = Modifier.weight(1f)
                )

                SecondaryButton(
                    text = "Security Reports",
                    icon = Icons.Default.Description,
                    onClick = onNavigateToReports,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Activity Timeline
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activity",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Live Sync",
                            fontSize = 11.sp,
                            color = CyberGreen
                        )
                    }

                    ActivityRow(
                        icon = Icons.Default.CheckCircle,
                        iconColor = CyberGreen,
                        title = "Routine safe audit completed",
                        time = "12m ago",
                        detail = "14 internal endpoints assessed without findings"
                    )

                    ActivityRow(
                        icon = Icons.Default.Shield,
                        iconColor = CyberCyan,
                        title = "Firewall perimeter updated",
                        time = "1h ago",
                        detail = "Port 80/443 rate limiting rules applied"
                    )

                    ActivityRow(
                        icon = Icons.Default.Warning,
                        iconColor = CyberOrange,
                        title = "New authentication anomaly logged",
                        time = "3h ago",
                        detail = "Suspicious login attempt from unauthorized subnet"
                    )
                }
            }
        }

        // Bottom spacing for comfortable scrolling
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActivityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    time: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Text(
                text = detail,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

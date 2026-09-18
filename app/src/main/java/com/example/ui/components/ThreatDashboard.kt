package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SecurityAlert
import com.example.data.model.Severity
import com.example.ui.theme.*

enum class ThreatSortOption(val label: String) {
    SEVERITY_DESC("Highest Severity"),
    NEWEST_FIRST("Newest First"),
    ASSET_NAME("Target Asset")
}

/**
 * Threat Dashboard UI component displaying:
 * - Summary of active security threats with severity indicators
 * - Filtering and sorting chip group at the top to toggle visibility by severity (Critical, High, Medium, Low)
 * - List of recent security alerts using Material3 cards with triage actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatDashboard(
    alerts: List<SecurityAlert>,
    onEscalateAlert: (String) -> Unit,
    onToggleHandled: (String) -> Unit,
    modifier: Modifier = Modifier,
    showHeaderCard: Boolean = true,
    initialSeverityFilter: Severity? = null
) {
    var selectedSeverityFilter by remember { mutableStateOf(initialSeverityFilter) }
    var selectedSortOption by remember { mutableStateOf(ThreatSortOption.SEVERITY_DESC) }
    var showOnlyUnhandled by remember { mutableStateOf(false) }

    // Summary calculations
    val totalThreats = alerts.size
    val activeUnhandled = alerts.count { !it.isHandled }
    val criticalCount = alerts.count { it.severity == Severity.CRITICAL }
    val highCount = alerts.count { it.severity == Severity.HIGH }
    val mediumCount = alerts.count { it.severity == Severity.MEDIUM }
    val lowCount = alerts.count { it.severity == Severity.LOW }

    // Filter and sort logic
    val filteredAlerts = alerts
        .filter { alert ->
            val matchesSeverity = selectedSeverityFilter == null || alert.severity == selectedSeverityFilter
            val matchesHandled = !showOnlyUnhandled || !alert.isHandled
            matchesSeverity && matchesHandled
        }
        .sortedWith { a, b ->
            when (selectedSortOption) {
                ThreatSortOption.SEVERITY_DESC -> b.severity.weight.compareTo(a.severity.weight)
                ThreatSortOption.NEWEST_FIRST -> a.id.compareTo(b.id) // Recent IDs
                ThreatSortOption.ASSET_NAME -> a.targetAsset.compareTo(b.targetAsset, ignoreCase = true)
            }
        }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("threat_dashboard_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showHeaderCard) {
            // Summary Card with Severity Indicators
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("threat_summary_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title and status beacon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberRed.copy(alpha = 0.15f))
                                    .border(1.dp, CyberRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = CyberRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "THREAT MONITOR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Active Security Threats",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Surface(
                            color = if (criticalCount > 0) CyberRed.copy(alpha = 0.15f) else CyberGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (criticalCount > 0) CyberRed.copy(alpha = 0.5f) else CyberGreen.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (criticalCount > 0) CyberRed else CyberGreen)
                                )
                                Text(
                                    text = if (criticalCount > 0) "$criticalCount CRITICAL" else "ALL CONTAINED",
                                    color = if (criticalCount > 0) CyberRed else CyberGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Threat metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThreatMiniStat(
                            title = "Active",
                            count = activeUnhandled,
                            color = CyberOrange,
                            modifier = Modifier.weight(1f)
                        )
                        ThreatMiniStat(
                            title = "Critical",
                            count = criticalCount,
                            color = CyberRed,
                            modifier = Modifier.weight(1f)
                        )
                        ThreatMiniStat(
                            title = "High",
                            count = highCount,
                            color = CyberOrange,
                            modifier = Modifier.weight(1f)
                        )
                        ThreatMiniStat(
                            title = "Total",
                            count = totalThreats,
                            color = CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Severity Distribution Indicator Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Severity Distribution Indicator",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$criticalCount Crit • $highCount High • $mediumCount Med • $lowCount Low",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(CyberSurfaceVariant)
                        ) {
                            if (criticalCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(criticalCount.toFloat())
                                        .fillMaxHeight()
                                        .background(CyberRed)
                                )
                            }
                            if (highCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(highCount.toFloat())
                                        .fillMaxHeight()
                                        .background(CyberOrange)
                                )
                            }
                            if (mediumCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(mediumCount.toFloat())
                                        .fillMaxHeight()
                                        .background(CyberYellow)
                                )
                            }
                            if (lowCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(lowCount.toFloat())
                                        .fillMaxHeight()
                                        .background(CyberBlue)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filtering and Sorting Chip Group at top of the Threat Monitor
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("threat_filter_sort_group")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Severity Filter Chips Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "FILTER SEVERITY:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "All" Chip
                    FilterChip(
                        selected = selectedSeverityFilter == null,
                        onClick = { selectedSeverityFilter = null },
                        label = {
                            Text(
                                text = "All ($totalThreats)",
                                fontSize = 11.sp,
                                fontWeight = if (selectedSeverityFilter == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = CyberSurfaceVariant,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("threat_filter_chip_all")
                    )

                    // Severity Chips (Critical, High, Medium, Low)
                    val severities = listOf(Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW)
                    severities.forEach { sev ->
                        val count = alerts.count { it.severity == sev }
                        val isSelected = selectedSeverityFilter == sev
                        val chipColor = when (sev) {
                            Severity.CRITICAL -> CyberRed
                            Severity.HIGH -> CyberOrange
                            Severity.MEDIUM -> CyberYellow
                            Severity.LOW -> CyberBlue
                            else -> CyberGreen
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSeverityFilter = if (selectedSeverityFilter == sev) null else sev
                            },
                            label = {
                                Text(
                                    text = "${sev.label} ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else chipColor)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = CyberSurfaceVariant,
                                labelColor = TextSecondary,
                                iconColor = chipColor
                            ),
                            modifier = Modifier.testTag("threat_filter_chip_${sev.name.lowercase()}")
                        )
                    }
                }

                HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))

                // Sorting Chip Group Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SORT THREATS:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThreatSortOption.values().forEach { option ->
                        val isSelected = selectedSortOption == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSortOption = option },
                            label = {
                                Text(
                                    text = option.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan,
                                containerColor = CyberSurfaceVariant,
                                labelColor = TextMuted
                            ),
                            modifier = Modifier.testTag("threat_sort_chip_${option.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Section header with filtered count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Alerts (${filteredAlerts.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            if (selectedSeverityFilter != null) {
                TextButton(
                    onClick = { selectedSeverityFilter = null },
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Clear Filter", color = CyberCyan, fontSize = 11.sp)
                }
            }
        }

        // List of Recent Alerts using Material3 Cards
        if (filteredAlerts.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No threats matching filter",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Adjust severity filter chips above to view all security alerts.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { selectedSeverityFilter = null },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset Severity Filter", fontSize = 12.sp)
                    }
                }
            }
        } else {
            filteredAlerts.forEach { alert ->
                ThreatAlertCard(
                    alert = alert,
                    onEscalate = { onEscalateAlert(alert.id) },
                    onToggleHandled = { onToggleHandled(alert.id) }
                )
            }
        }
    }
}

@Composable
private fun ThreatMiniStat(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }
    }
}

/**
 * Material 3 Card displaying an individual threat alert with severity indicators & actions
 */
@Composable
fun ThreatAlertCard(
    alert: SecurityAlert,
    onEscalate: () -> Unit,
    onToggleHandled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (alert.severity) {
        Severity.CRITICAL -> CyberRed.copy(alpha = 0.6f)
        Severity.HIGH -> CyberOrange.copy(alpha = 0.5f)
        Severity.MEDIUM -> CyberYellow.copy(alpha = 0.4f)
        else -> CyberCardBorder
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("threat_alert_card_${alert.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row: Severity badge, Alert ID, source tag, and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeverityBadge(severity = alert.severity)
                    Text(
                        text = alert.id,
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = alert.source,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = alert.timestamp,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Title
            Text(
                text = alert.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Description
            Text(
                text = alert.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // Target Asset
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Target: ${alert.targetAsset}",
                    color = CyberCyan,
                    fontSize = 12.sp
                )
            }

            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))

            // Action Items Row: Triage & Incident Escalation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Handled / Active toggle pill
                Surface(
                    color = if (alert.isHandled) CyberGreen.copy(alpha = 0.15f) else CyberOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (alert.isHandled) CyberGreen.copy(alpha = 0.4f) else CyberOrange.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.clickable { onToggleHandled() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (alert.isHandled) Icons.Default.Check else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (alert.isHandled) CyberGreen else CyberOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (alert.isHandled) "CONTAINED / HANDLED" else "ACTIVE TRIAGE",
                            color = if (alert.isHandled) CyberGreen else CyberOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Escalation Button
                Button(
                    onClick = onEscalate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alert.severity == Severity.CRITICAL) CyberRed else CyberCyan,
                        contentColor = if (alert.severity == Severity.CRITICAL) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_escalate_${alert.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Escalate to IR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

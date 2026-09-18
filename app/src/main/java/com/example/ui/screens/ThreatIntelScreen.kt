package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun ThreatIntelScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.alerts.collectAsState()
    val indicators by viewModel.threatIndicators.collectAsState()
    val campaigns by viewModel.threatCampaigns.collectAsState()
    val reports by viewModel.threatReports.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Threat Monitor, 1 = IoCs, 2 = Campaigns, 3 = Reports
    var selectedTypeFilter by remember { mutableStateOf<IocType?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredIndicators = indicators.filter { ioc ->
        val matchesType = selectedTypeFilter == null || ioc.type == selectedTypeFilter
        val matchesSearch = searchQuery.isBlank() ||
                ioc.value.contains(searchQuery, ignoreCase = true) ||
                ioc.threatFamily.contains(searchQuery, ignoreCase = true)
        matchesType && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("threat_intel_screen")
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
                    text = "Threat Intelligence",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Global telemetry, indicators & active campaigns",
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
                text = { Text("Monitor (${alerts.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                selectedContentColor = CyberBlue,
                unselectedContentColor = TextSecondary
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("IoCs (${indicators.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                selectedContentColor = CyberBlue,
                unselectedContentColor = TextSecondary
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Campaigns (${campaigns.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                selectedContentColor = CyberBlue,
                unselectedContentColor = TextSecondary
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Reports (${reports.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                selectedContentColor = CyberBlue,
                unselectedContentColor = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> {
                // Threat Monitor
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("threat_monitor_lazy_column"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        ThreatDashboard(
                            alerts = alerts,
                            onEscalateAlert = { viewModel.escalateAlert(it) },
                            onToggleHandled = { viewModel.toggleAlertHandled(it) },
                            showHeaderCard = true
                        )
                    }
                }
            }

            1 -> {
                // Indicators of Compromise (IoCs)
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search IP, domain or SHA256 hash...",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = { selectedTypeFilter = null },
                            label = { Text("All Types", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberBlue,
                                selectedLabelColor = Color.White,
                                containerColor = CyberSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                    items(IocType.values()) { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                            label = { Text(type.name.replace("_", " "), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberBlue,
                                selectedLabelColor = Color.White,
                                containerColor = CyberSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                if (filteredIndicators.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.SearchOff,
                        title = "No Indicators Found",
                        description = if (searchQuery.isNotBlank()) "No threat indicators matched \"$searchQuery\"." else "No indicators recorded for this filter.",
                        actionLabel = if (searchQuery.isNotBlank()) "Clear Filter" else null,
                        onAction = {
                            searchQuery = ""
                            selectedTypeFilter = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredIndicators, key = { it.id }) { ioc ->
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
                                        Surface(
                                            color = CyberCyan.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = ioc.type.name,
                                                color = CyberCyan,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = "Confidence: ${ioc.confidence}%",
                                            color = if (ioc.confidence >= 90) CyberRed else CyberYellow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = ioc.value,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Family: ${ioc.threatFamily}",
                                        color = CyberOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Last seen: ${ioc.lastSeen}", color = TextMuted, fontSize = 11.sp)
                                        Text(text = "${ioc.matchedEventsCount} hits", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Threat Campaigns
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(campaigns, key = { it.name }) { camp ->
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
                                    Text(text = camp.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    StatusPill(text = "${camp.activeIndicatorsCount} IoCs", color = CyberRed)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Threat Actor: ${camp.actor}", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = camp.summary, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Target Sectors: ${camp.targetSectors.joinToString(", ")}", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Tactics: ${camp.tactics.joinToString(" • ")}", color = CyberYellow, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            3 -> {
                // Threat Reports
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reports, key = { it.id }) { report ->
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
                                    Surface(
                                        color = when (report.tlp) {
                                            "TLP:RED" -> CyberRed.copy(alpha = 0.15f)
                                            "TLP:AMBER" -> CyberOrange.copy(alpha = 0.15f)
                                            else -> CyberGreen.copy(alpha = 0.15f)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            when (report.tlp) {
                                                "TLP:RED" -> CyberRed.copy(alpha = 0.3f)
                                                "TLP:AMBER" -> CyberOrange.copy(alpha = 0.3f)
                                                else -> CyberGreen.copy(alpha = 0.3f)
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = report.tlp,
                                            color = when (report.tlp) {
                                                "TLP:RED" -> CyberRed
                                                "TLP:AMBER" -> CyberOrange
                                                else -> CyberGreen
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(text = report.date, color = TextMuted, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = report.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Source: ${report.source}", color = CyberCyan, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = report.executiveSummary, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

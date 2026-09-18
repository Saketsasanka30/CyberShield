package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
fun ComplianceRiskScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val standards by viewModel.complianceStandards.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Compliance, 1 = Risk Matrix

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("compliance_risk_screen")
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
                    text = "Compliance & Risk",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Security frameworks and quantitative heatmaps",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberSurfaceDark,
            contentColor = CyberCyan,
            divider = { HorizontalDivider(color = CyberCardBorder) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Compliance Frameworks", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("5x5 Risk Matrix", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
        }

        when (selectedTab) {
            0 -> {
                // Compliance Standards list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(standards) { std ->
                        ComplianceStandardCard(standard = std)
                    }
                }
            }

            1 -> {
                // 5x5 Likelihood vs Impact Risk Matrix
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "5x5 Likelihood vs Impact Heatmap",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Quantitative risk evaluation based on NIST SP 800-30 standards. Numbers indicate active threats in each quadrant.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                RiskHeatmapGrid()

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LegendItem(color = CyberGreen, label = "Low (1-4)")
                                    LegendItem(color = CyberYellow, label = "Medium (5-9)")
                                    LegendItem(color = CyberOrange, label = "High (10-16)")
                                    LegendItem(color = CyberRed, label = "Critical (17-25)")
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Key Risk Observations",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• Cell [5,4] (Catastrophic Impact / Likely): SSH backdoor on Staging Jumpbox.",
                                    color = CyberRed,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Text(
                                    text = "• Cell [4,4] (Major Impact / Likely): Phishing campaign targeting administrator logins.",
                                    color = CyberOrange,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Text(
                                    text = "• Cell [3,3] (Moderate Impact / Possible): Unencrypted storage bucket policy drift in test tenant.",
                                    color = CyberYellow,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComplianceStandardCard(standard: ComplianceStandard) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
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
                Text(
                    text = standard.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = CyberGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${standard.scorePercentage}%",
                        color = CyberGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { standard.scorePercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CyberGreen,
                trackColor = CyberSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Controls Passed: ${standard.passedControls}/${standard.totalControls}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide Details" else "Inspect Controls", color = CyberCyan, fontSize = 12.sp)
                }
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    standard.controls.forEach { ctrl ->
                        Surface(
                            color = CyberSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (ctrl.isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (ctrl.isPassed) CyberGreen else CyberRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = ctrl.id, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = ctrl.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Text(text = ctrl.details, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiskHeatmapGrid() {
    val matrixCounts = listOf(
        listOf(0, 1, 2, 3, 1),
        listOf(0, 0, 1, 2, 1),
        listOf(1, 2, 3, 1, 0),
        listOf(2, 3, 1, 0, 0),
        listOf(4, 2, 0, 0, 0)
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (r in 0 until 5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (c in 0 until 5) {
                    val severityScore = (5 - r) * (c + 1)
                    val cellColor = when {
                        severityScore >= 16 -> CyberRed
                        severityScore >= 10 -> CyberOrange
                        severityScore >= 5 -> CyberYellow
                        else -> CyberGreen
                    }
                    val count = matrixCounts[r][c]

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(cellColor.copy(alpha = if (count > 0) 0.35f else 0.12f))
                            .border(1.dp, cellColor.copy(alpha = if (count > 0) 0.8f else 0.25f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (count > 0) {
                            Text(
                                text = "$count",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
    }
}

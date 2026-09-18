package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CyberShieldViewModel
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val reportTypes = listOf(
        "Executive Summary",
        "Vulnerability Assessment",
        "Incident Post-Mortem",
        "Compliance Audit"
    )
    var selectedReportType by remember { mutableStateOf(reportTypes[0]) }
    var reportContent by remember { mutableStateOf(viewModel.getReportContent(selectedReportType)) }

    LaunchedEffect(selectedReportType) {
        reportContent = viewModel.getReportContent(selectedReportType)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen")
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
                    text = "Security Reports",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Executive audits and compliance summaries",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = {
                    reportContent = viewModel.getReportContent(selectedReportType)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_refresh_report")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Report Type Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            items(reportTypes) { type ->
                FilterChip(
                    selected = selectedReportType == type,
                    onClick = { selectedReportType = type },
                    label = { Text(type, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberBlue,
                        selectedLabelColor = Color.White,
                        containerColor = CyberSurfaceDark,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Report Document Canvas
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("report_text_content"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = CyberBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "INTERNAL BRIEFING",
                                color = CyberBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = selectedReportType,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = CyberCardBorder)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                item {
                    Text(
                        text = reportContent,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.CyberShieldViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun NetworkScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val nodes by viewModel.networkNodes.collectAsState()
    val links by viewModel.networkLinks.collectAsState()
    val latestScan by viewModel.latestScanResult.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    var scanTargetInput by remember { mutableStateOf("demo.internal.corp") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("network_screen_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Safe Scanning Policy Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Authorized Audit Scanner",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Non-destructive TCP discovery restricted to internal authorized lab domains.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Live Network Topology Map
        item {
            Text(
                text = "Perimeter Topology & Traffic",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                NetworkTopologyCanvas(nodes = nodes, links = links)
            }
        }

        // Interactive Scanner Trigger Card
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Run Port & Endpoint Audit",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = scanTargetInput,
                        onValueChange = { scanTargetInput = it },
                        label = { Text("Target Hostname or Subnet") },
                        placeholder = { Text("demo.internal.corp") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_target_input")
                    )

                    // Quick Lab Target Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("demo.internal.corp", "10.0.1.0/24", "api.testlab.local").forEach { preset ->
                            SuggestionChip(
                                onClick = { scanTargetInput = preset },
                                label = { Text(preset, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = CyberSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    PrimaryButton(
                        text = if (isScanning) "Scanning Endpoint..." else "Launch Safe Audit",
                        icon = Icons.Default.Radar,
                        isLoading = isScanning,
                        onClick = { viewModel.runSafeScan(scanTargetInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("launch_scan_btn")
                    )

                    // Latest Scan Result
                    latestScan?.let { result ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (result.safetyVerified) CyberGreen.copy(alpha = 0.5f) else CyberRed.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target: ${result.target}",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    StatusPill(
                                        text = if (result.safetyVerified) "PASSED" else "REJECTED",
                                        color = if (result.safetyVerified) CyberGreen else CyberRed
                                    )
                                }

                                Text(
                                    text = "Audit Timestamp: ${result.timestamp}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                if (result.openPortsDetected.isNotEmpty()) {
                                    Text(
                                        text = "Discovered Ports: ${result.openPortsDetected.joinToString(", ")}",
                                        color = CyberBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                result.vulnerabilitiesIdentified.forEach { item ->
                                    Text(
                                        text = "• $item",
                                        color = if (result.safetyVerified) TextSecondary else CyberRed,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Monitored Nodes List
        item {
            Text(
                text = "Monitored Network Endpoints (${nodes.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        items(nodes) { node ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (node.status == HealthStatus.CRITICAL) CyberRed.copy(alpha = 0.5f) else CyberCardBorder
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (node.status) {
                                        HealthStatus.HEALTHY -> CyberGreen
                                        HealthStatus.WARNING -> CyberYellow
                                        HealthStatus.CRITICAL -> CyberRed
                                        HealthStatus.OFFLINE -> TextMuted
                                    }
                                )
                        )
                        Column {
                            Text(
                                text = node.label,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${node.ip} • ${node.zone.name}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    StatusPill(
                        text = node.status.label,
                        color = when (node.status) {
                            HealthStatus.HEALTHY -> CyberGreen
                            HealthStatus.WARNING -> CyberYellow
                            HealthStatus.CRITICAL -> CyberRed
                            HealthStatus.OFFLINE -> TextMuted
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkTopologyCanvas(
    nodes: List<NetworkNode>,
    links: List<NetworkLink>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "packetFlow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "packetFlowPulse"
    )

    Canvas(modifier = modifier.fillMaxSize().padding(16.dp)) {
        val width = size.width
        val height = size.height

        val nodePositions = mapOf(
            "NODE-EXT" to Offset(width * 0.12f, height * 0.5f),
            "NODE-FW" to Offset(width * 0.35f, height * 0.5f),
            "NODE-WEB" to Offset(width * 0.58f, height * 0.25f),
            "NODE-APP" to Offset(width * 0.78f, height * 0.45f),
            "NODE-DB" to Offset(width * 0.90f, height * 0.78f),
            "NODE-STG" to Offset(width * 0.58f, height * 0.78f),
            "NODE-CLOUD" to Offset(width * 0.88f, height * 0.15f)
        )

        // Draw links
        links.forEach { link ->
            val start = nodePositions[link.sourceId]
            val end = nodePositions[link.targetId]

            if (start != null && end != null) {
                val lineColor = if (link.isSuspicious) CyberRed else CyberCyan.copy(alpha = 0.35f)
                val pathEffect = if (link.isSuspicious) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null

                drawLine(
                    color = lineColor,
                    start = start,
                    end = end,
                    strokeWidth = if (link.isSuspicious) 2.5f else 1.5f,
                    pathEffect = pathEffect
                )

                val currentX = start.x + (end.x - start.x) * pulse
                val currentY = start.y + (end.y - start.y) * pulse
                drawCircle(
                    color = if (link.isSuspicious) CyberRed else CyberCyan,
                    radius = 3f,
                    center = Offset(currentX, currentY)
                )
            }
        }

        // Draw nodes
        nodePositions.forEach { (nodeId, pos) ->
            val node = nodes.find { it.id == nodeId }
            val nodeColor = when (node?.status) {
                HealthStatus.CRITICAL -> CyberRed
                HealthStatus.WARNING -> CyberYellow
                else -> CyberCyan
            }

            drawCircle(
                color = nodeColor.copy(alpha = 0.2f),
                radius = 14f,
                center = pos
            )

            drawCircle(
                color = nodeColor,
                radius = 6f,
                center = pos
            )
        }
    }
}

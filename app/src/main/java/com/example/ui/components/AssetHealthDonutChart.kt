package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Asset
import com.example.data.model.HealthStatus
import com.example.ui.theme.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class HealthSliceData(
    val status: HealthStatus,
    val label: String,
    val count: Int,
    val percentage: Float,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float
)

/**
 * Modern Recharts-inspired Donut Chart visualizing Asset Security Health distribution.
 * Features:
 * - Proportional arc angles with clean sector padding
 * - Centered summary telemetry readout with dynamic touch/selection response
 * - Interactive legend items with percentage & count badges
 * - Material3 surface container withDefensive Health index
 */
@Composable
fun AssetHealthDonutChart(
    assets: List<Asset>,
    modifier: Modifier = Modifier,
    onSliceClick: ((HealthStatus) -> Unit)? = null
) {
    val totalAssets = assets.size.coerceAtLeast(1)
    val healthyCount = assets.count { it.healthStatus == HealthStatus.HEALTHY }
    val warningCount = assets.count { it.healthStatus == HealthStatus.WARNING }
    val criticalCount = assets.count { it.healthStatus == HealthStatus.CRITICAL || it.healthStatus == HealthStatus.OFFLINE }

    val healthyPct = (healthyCount.toFloat() / totalAssets) * 100f
    val warningPct = (warningCount.toFloat() / totalAssets) * 100f
    val criticalPct = (criticalCount.toFloat() / totalAssets) * 100f

    // Animated transition for chart appearance
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "donut_chart_animation"
    )

    var selectedStatus by remember { mutableStateOf<HealthStatus?>(null) }

    // Compute angular sectors with 3.5-degree spacing between active slices (Recharts padAngle)
    val rawSlices = listOf(
        Triple(HealthStatus.HEALTHY, healthyCount, CyberGreen),
        Triple(HealthStatus.WARNING, warningCount, CyberYellow),
        Triple(HealthStatus.CRITICAL, criticalCount, CyberRed)
    ).filter { it.second > 0 }

    val padAngle = if (rawSlices.size > 1) 4f else 0f
    val availableAngle = 360f - (padAngle * rawSlices.size)

    var currentAngle = -90f // Start from top
    val slices = rawSlices.map { (status, count, color) ->
        val sweep = (count.toFloat() / totalAssets) * availableAngle
        val slice = HealthSliceData(
            status = status,
            label = when (status) {
                HealthStatus.HEALTHY -> "Healthy"
                HealthStatus.WARNING -> "Warning"
                HealthStatus.CRITICAL -> "Critical"
                else -> "Offline"
            },
            count = count,
            percentage = (count.toFloat() / totalAssets) * 100f,
            color = color,
            startAngle = currentAngle,
            sweepAngle = sweep
        )
        currentAngle += sweep + padAngle
        slice
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("asset_health_donut_chart")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with Recharts styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyberCyan)
                        )
                        Text(
                            text = "DEVICE SECURITY HEALTH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberCyan,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Infrastructure Status",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = when {
                        criticalCount > 0 -> CyberRed.copy(alpha = 0.12f)
                        warningCount > 0 -> CyberYellow.copy(alpha = 0.12f)
                        else -> CyberGreen.copy(alpha = 0.12f)
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            criticalCount > 0 -> CyberRed.copy(alpha = 0.35f)
                            warningCount > 0 -> CyberYellow.copy(alpha = 0.35f)
                            else -> CyberGreen.copy(alpha = 0.35f)
                        }
                    )
                ) {
                    Text(
                        text = "${healthyPct.toInt()}% Healthy",
                        color = when {
                            criticalCount > 0 -> CyberRed
                            warningCount > 0 -> CyberYellow
                            else -> CyberGreen
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Donut Chart & Center Metrics layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Donut Canvas
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .testTag("donut_chart_canvas_box"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        val strokeWidth = 14.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )
                        val arcSize = Size(diameter, diameter)

                        // If no assets or empty, draw subtle background ring
                        if (slices.isEmpty()) {
                            drawArc(
                                color = CyberSurfaceVariant,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                        } else {
                            // Background track ring
                            drawArc(
                                color = Color(0xFF1E293B),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )

                            // Slices
                            slices.forEach { slice ->
                                val isHighlighted = selectedStatus == null || selectedStatus == slice.status
                                val actualStroke = if (selectedStatus == slice.status) strokeWidth * 1.2f else strokeWidth
                                val sliceColor = if (isHighlighted) slice.color else slice.color.copy(alpha = 0.35f)

                                drawArc(
                                    color = sliceColor,
                                    startAngle = slice.startAngle,
                                    sweepAngle = slice.sweepAngle * animationProgress,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = actualStroke, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    // Center statistical text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        val activeSlice = slices.find { it.status == selectedStatus }
                        if (activeSlice != null) {
                            Text(
                                text = "${activeSlice.count}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeSlice.color
                            )
                            Text(
                                text = activeSlice.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Text(
                                text = "${activeSlice.percentage.toInt()}%",
                                fontSize = 11.sp,
                                color = activeSlice.color,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "$totalAssets",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Devices",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Legend Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DonutLegendItem(
                        label = "Healthy",
                        count = healthyCount,
                        percentage = healthyPct,
                        color = CyberGreen,
                        icon = Icons.Default.CheckCircle,
                        isSelected = selectedStatus == HealthStatus.HEALTHY,
                        onClick = {
                            selectedStatus = if (selectedStatus == HealthStatus.HEALTHY) null else HealthStatus.HEALTHY
                            onSliceClick?.invoke(HealthStatus.HEALTHY)
                        },
                        modifier = Modifier.testTag("donut_legend_healthy")
                    )

                    DonutLegendItem(
                        label = "Warning",
                        count = warningCount,
                        percentage = warningPct,
                        color = CyberYellow,
                        icon = Icons.Default.Info,
                        isSelected = selectedStatus == HealthStatus.WARNING,
                        onClick = {
                            selectedStatus = if (selectedStatus == HealthStatus.WARNING) null else HealthStatus.WARNING
                            onSliceClick?.invoke(HealthStatus.WARNING)
                        },
                        modifier = Modifier.testTag("donut_legend_warning")
                    )

                    DonutLegendItem(
                        label = "Critical",
                        count = criticalCount,
                        percentage = criticalPct,
                        color = CyberRed,
                        icon = Icons.Default.Warning,
                        isSelected = selectedStatus == HealthStatus.CRITICAL,
                        onClick = {
                            selectedStatus = if (selectedStatus == HealthStatus.CRITICAL) null else HealthStatus.CRITICAL
                            onSliceClick?.invoke(HealthStatus.CRITICAL)
                        },
                        modifier = Modifier.testTag("donut_legend_critical")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recharts visual status progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CyberSurfaceVariant)
            ) {
                if (healthyCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(healthyCount.toFloat())
                            .fillMaxHeight()
                            .background(CyberGreen)
                    )
                }
                if (warningCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(warningCount.toFloat())
                            .fillMaxHeight()
                            .background(CyberYellow)
                    )
                }
                if (criticalCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(criticalCount.toFloat())
                            .fillMaxHeight()
                            .background(CyberRed)
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutLegendItem(
    label: String,
    count: Int,
    percentage: Float,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) color.copy(alpha = 0.18f) else CyberSurfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else CyberCardBorder.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) color else TextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "(${percentage.toInt()}%)",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

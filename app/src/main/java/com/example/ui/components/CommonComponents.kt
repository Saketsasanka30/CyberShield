package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun SeverityBadge(
    severity: Severity,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, icon) = when (severity) {
        Severity.CRITICAL -> Triple(SeverityCriticalBg, CyberRed, Icons.Default.Warning)
        Severity.HIGH -> Triple(SeverityHighBg, CyberOrange, Icons.Default.PriorityHigh)
        Severity.MEDIUM -> Triple(SeverityMediumBg, CyberYellow, Icons.Default.Info)
        Severity.LOW -> Triple(SeverityLowBg, CyberBlue, Icons.Default.Shield)
        Severity.INFO -> Triple(SeverityInfoBg, CyberGreen, Icons.Default.CheckCircle)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.35f)),
        modifier = modifier.testTag("badge_severity_${severity.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = severity.label,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = severity.label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .testTag("stat_card_${title.replace(" ", "_").lowercase()}")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RiskScoreDial(
    score: Int,
    grade: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 120.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "riskScoreProgress"
    )

    val dialColor = when {
        score >= 85 -> CyberGreen
        score >= 70 -> CyberCyan
        score >= 55 -> CyberYellow
        else -> CyberRed
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("risk_score_dial"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 10.dp.toPx()
            val arcSize = Size(size.width, size.height)

            // Background track
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(CyberBlue, dialColor, dialColor)
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Surface(
                color = dialColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, dialColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = grade,
                    color = dialColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun SocSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
fun SocRoleBadge(
    role: RbacRole,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (role) {
        RbacRole.ADMIN -> Pair(CyberCyan, Icons.Default.AdminPanelSettings)
        RbacRole.ANALYST -> Pair(CyberPurple, Icons.Default.Security)
        RbacRole.AUDITOR -> Pair(CyberYellow, Icons.Default.FactCheck)
        RbacRole.READ_ONLY -> Pair(CyberBlue, Icons.Default.Visibility)
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
            .testTag("rbac_role_badge")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = role.title,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = role.title,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Reusable Mobile First Components

@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = actions
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = CyberBlue,
            contentColor = Color.White,
            disabledContainerColor = CyberSurfaceVariant,
            disabledContentColor = TextMuted
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = ""
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextSecondary)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier,
    testTag: String = "search_bar_input"
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, color = TextMuted, fontSize = 13.sp) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberBlue,
            unfocusedBorderColor = CyberCardBorder,
            focusedContainerColor = CyberSurfaceDark,
            unfocusedContainerColor = CyberSurfaceDark,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag)
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.Shield,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = actionText,
                    onClick = onActionClick,
                    modifier = Modifier.widthIn(min = 140.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    message: String = "",
    description: String = message,
    icon: ImageVector = Icons.Default.Shield,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = title,
        message = if (description.isNotEmpty()) description else message,
        icon = icon,
        actionText = actionLabel,
        onActionClick = onAction,
        modifier = modifier
    )
}



package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

val RbacRole.canAddAsset: Boolean get() = this == RbacRole.ADMIN
val RbacRole.canRemediate: Boolean get() = this == RbacRole.ADMIN || this == RbacRole.ANALYST
val RbacRole.canDeclareIncident: Boolean get() = this == RbacRole.ADMIN || this == RbacRole.ANALYST
val RbacRole.canRunNetworkScan: Boolean get() = this == RbacRole.ADMIN || this == RbacRole.ANALYST
val RbacRole.canExportReports: Boolean get() = this != RbacRole.READ_ONLY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    var selectedSection by remember { mutableStateOf(0) } // 0 = Roles, 1 = Audit Trail, 2 = Policies

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen")
    ) {
        // Screen Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Access control, compliance logs & scanner guardrails",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Section Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedSection,
            containerColor = CyberSurfaceDark,
            contentColor = CyberCyan,
            divider = { HorizontalDivider(color = CyberCardBorder) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("Operator Roles", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("Audit Trail (${auditLogs.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = { Text("Policies", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
        }

        when (selectedSection) {
            0 -> {
                // Role-Based Access Control (RBAC)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Active Operator Identity",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "CyberShield enforces permissions based on the active persona. Tap a role below to switch identities for testing.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )

                                RbacRole.values().forEach { role ->
                                    val isSelected = role == currentRole
                                    Surface(
                                        color = if (isSelected) CyberBlue.copy(alpha = 0.15f) else CyberSurfaceVariant,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) CyberBlue else CyberCardBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.switchRole(role) }
                                            .testTag("role_option_${role.name.lowercase()}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = role.title,
                                                        color = if (isSelected) CyberBlue else TextPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (isSelected) {
                                                        StatusPill(text = "ACTIVE", color = CyberBlue)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = when (role) {
                                                        RbacRole.ADMIN -> "Full access: Manage devices, remediate issues, run scans, declare incidents."
                                                        RbacRole.ANALYST -> "Operational access: Update incidents, transition patch states, perform audits."
                                                        RbacRole.AUDITOR -> "Read-only access: View posture scores, review compliance and historical trails."
                                                        RbacRole.READ_ONLY -> "Executive Viewer: High-level dashboard & report reading only."
                                                    },
                                                    color = TextSecondary,
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }

                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { viewModel.switchRole(role) },
                                                colors = RadioButtonDefaults.colors(selectedColor = CyberBlue)
                                            )
                                        }
                                    }
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
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Current Role Permissions (${currentRole.title})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                PermissionCheckRow("Enroll New Devices", currentRole.canAddAsset)
                                PermissionCheckRow("Remediate Vulnerabilities", currentRole.canRemediate)
                                PermissionCheckRow("Declare & Manage Incidents", currentRole.canDeclareIncident)
                                PermissionCheckRow("Execute Safe Network Scans", currentRole.canRunNetworkScan)
                                PermissionCheckRow("Export Compliance Reports", currentRole.canExportReports)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Audit Trail
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(auditLogs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = log.actor, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Surface(
                                            color = CyberSurfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = log.role.title,
                                                color = TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    StatusPill(
                                        text = log.status,
                                        color = if (log.status == "SUCCESS") CyberGreen else CyberRed
                                    )
                                }

                                Text(text = "${log.action} • ${log.resource}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${log.timestamp} • IP: ${log.ipAddress}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            2 -> {
                // Policies
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Safe Scanner Mandates",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberGreen
                                )
                                Text(
                                    text = "In compliance with cybersecurity policies and safe simulation standards:",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )

                                PolicyRuleItem(
                                    title = "Target Whitelist Restriction",
                                    description = "The scanner verifies targets against authorized lab IP blocks (10.0.0.0/8, 192.168.0.0/16, demo.internal.corp). External arbitrary target requests are strictly blocked."
                                )

                                PolicyRuleItem(
                                    title = "Non-Destructive Protocol Only",
                                    description = "All network probing is restricted to standard TCP SYN discovery and banner polling. Exploits, DDoS packets, and payload executions are permanently disabled."
                                )

                                PolicyRuleItem(
                                    title = "Cryptographic Audit Trail",
                                    description = "Every scan attempt is recorded in the immutable audit log with operator ID, source IP, and target signature."
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
fun PermissionCheckRow(label: String, granted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (granted) CyberGreen else CyberRed,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun PolicyRuleItem(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = CyberBlue, modifier = Modifier.size(16.dp))
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = description, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

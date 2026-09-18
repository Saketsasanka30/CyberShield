package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@Composable
fun VulnerabilitiesScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val vulnerabilities by viewModel.vulnerabilities.collectAsState()
    var selectedSeverityFilter by remember { mutableStateOf<Severity?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<RemediationStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedVulnForDetail by remember { mutableStateOf<Vulnerability?>(null) }

    val openCount = vulnerabilities.count { it.remediationStatus == RemediationStatus.OPEN }
    val patchingCount = vulnerabilities.count { it.remediationStatus == RemediationStatus.PATCHING || it.remediationStatus == RemediationStatus.IN_PROGRESS }
    val resolvedCount = vulnerabilities.count { it.remediationStatus == RemediationStatus.RESOLVED || it.remediationStatus == RemediationStatus.MITIGATED }

    val filteredVulns = vulnerabilities.filter { v ->
        val matchesSeverity = selectedSeverityFilter == null || v.severity == selectedSeverityFilter
        val matchesStatus = when (selectedStatusFilter) {
            null -> true
            RemediationStatus.OPEN -> v.remediationStatus == RemediationStatus.OPEN
            RemediationStatus.PATCHING -> v.remediationStatus == RemediationStatus.PATCHING || v.remediationStatus == RemediationStatus.IN_PROGRESS
            RemediationStatus.RESOLVED -> v.remediationStatus == RemediationStatus.RESOLVED || v.remediationStatus == RemediationStatus.MITIGATED
            else -> v.remediationStatus == selectedStatusFilter
        }
        val matchesSearch = searchQuery.isBlank() ||
                v.cve.contains(searchQuery, ignoreCase = true) ||
                v.title.contains(searchQuery, ignoreCase = true) ||
                v.affectedAsset.contains(searchQuery, ignoreCase = true) ||
                v.remediationOwner.contains(searchQuery, ignoreCase = true)
        matchesSeverity && matchesStatus && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("vulnerabilities_screen")
    ) {
        // Screen Header with Top Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Security Checks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$openCount open issues • $resolvedCount resolved",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("fab_add_vulnerability")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Issue", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Quick Status Filter Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusPillCard(
                title = "Needs Fix",
                count = openCount,
                color = CyberRed,
                isSelected = selectedStatusFilter == RemediationStatus.OPEN,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == RemediationStatus.OPEN) null else RemediationStatus.OPEN
                },
                modifier = Modifier.weight(1f)
            )

            StatusPillCard(
                title = "Patching",
                count = patchingCount,
                color = CyberYellow,
                isSelected = selectedStatusFilter == RemediationStatus.PATCHING,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == RemediationStatus.PATCHING) null else RemediationStatus.PATCHING
                },
                modifier = Modifier.weight(1f)
            )

            StatusPillCard(
                title = "Resolved",
                count = resolvedCount,
                color = CyberGreen,
                isSelected = selectedStatusFilter == RemediationStatus.RESOLVED,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == RemediationStatus.RESOLVED) null else RemediationStatus.RESOLVED
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search by CVE, device or title...",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Severity Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedSeverityFilter == null,
                    onClick = { selectedSeverityFilter = null },
                    label = { Text("All Severities", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberBlue,
                        selectedLabelColor = Color.White,
                        containerColor = CyberSurfaceDark,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            items(Severity.values()) { severity ->
                FilterChip(
                    selected = selectedSeverityFilter == severity,
                    onClick = {
                        selectedSeverityFilter = if (selectedSeverityFilter == severity) null else severity
                    },
                    label = { Text(severity.name, fontSize = 12.sp) },
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

        // Content List or Empty State
        if (filteredVulns.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.CheckCircle,
                title = "No vulnerabilities found",
                description = if (searchQuery.isNotBlank()) "No checks matched \"$searchQuery\"." else "All checks in this category are clear.",
                actionLabel = if (searchQuery.isNotBlank()) "Clear Search" else null,
                onAction = { searchQuery = "" },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("vuln_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredVulns, key = { it.cve }) { vuln ->
                    VulnerabilityItemCard(
                        vuln = vuln,
                        onClick = { selectedVulnForDetail = vuln },
                        onTransition = { newStatus ->
                            viewModel.transitionVulnerabilityStatus(vuln.cve, newStatus)
                        }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    selectedVulnForDetail?.let { vuln ->
        VulnDetailSheet(
            vuln = vuln,
            onDismiss = { selectedVulnForDetail = null },
            onTransition = { newStatus ->
                viewModel.transitionVulnerabilityStatus(vuln.cve, newStatus)
                selectedVulnForDetail = null
            }
        )
    }

    // Add Vulnerability Dialog
    if (showAddDialog) {
        AddVulnDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { cve, title, severity, cvss, asset, owner ->
                viewModel.updateVulnerability(cve, RemediationStatus.OPEN, owner)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun StatusPillCard(
    title: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) color.copy(alpha = 0.18f) else CyberSurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else CyberCardBorder
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) color else TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$count",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun VulnerabilityItemCard(
    vuln: Vulnerability,
    onClick: () -> Unit,
    onTransition: (RemediationStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (vuln.severity == Severity.CRITICAL) CyberRed.copy(alpha = 0.4f) else CyberCardBorder
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("vuln_card_${vuln.cve}")
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

                Surface(
                    color = CyberSurfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "CVSS ${vuln.cvssScore}",
                        color = if (vuln.cvssScore >= 9.0) CyberRed else CyberYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = vuln.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Device: ${vuln.affectedAsset} • Owner: ${vuln.remediationOwner}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(
                    text = vuln.remediationStatus.label,
                    color = when (vuln.remediationStatus) {
                        RemediationStatus.RESOLVED -> CyberGreen
                        RemediationStatus.PATCHING, RemediationStatus.IN_PROGRESS -> CyberYellow
                        else -> CyberRed
                    }
                )

                // 1-Tap Action Transition Button
                when (vuln.remediationStatus) {
                    RemediationStatus.OPEN -> {
                        Button(
                            onClick = { onTransition(RemediationStatus.PATCHING) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Start Patch", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    RemediationStatus.PATCHING, RemediationStatus.IN_PROGRESS -> {
                        Button(
                            onClick = { onTransition(RemediationStatus.RESOLVED) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    RemediationStatus.RESOLVED, RemediationStatus.MITIGATED -> {
                        TextButton(
                            onClick = { onTransition(RemediationStatus.OPEN) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Reopen", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    RemediationStatus.FALSE_POSITIVE -> {
                        TextButton(
                            onClick = { onTransition(RemediationStatus.OPEN) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Reopen", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VulnDetailSheet(
    vuln: Vulnerability,
    onDismiss: () -> Unit,
    onTransition: (RemediationStatus) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CyberSurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = CyberCardBorder) },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = vuln.severity)
                Text(
                    text = "CVSS: ${vuln.cvssScore}",
                    color = if (vuln.cvssScore >= 9.0) CyberRed else CyberYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${vuln.cve}: ${vuln.title}",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = vuln.description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            HorizontalDivider(color = CyberCardBorder)

            DetailRow(label = "Affected Device", value = vuln.affectedAsset)
            DetailRow(label = "Remediation Owner", value = vuln.remediationOwner)
            DetailRow(label = "Current Lifecycle", value = vuln.remediationStatus.label)
            DetailRow(label = "Discovery Date", value = vuln.discoveryDate)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Remediation Action:",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onTransition(RemediationStatus.PATCHING) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("In Progress", fontSize = 12.sp)
                }

                Button(
                    onClick = { onTransition(RemediationStatus.RESOLVED) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Resolve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddVulnDialog(
    onDismiss: () -> Unit,
    onAdd: (cve: String, title: String, severity: Severity, cvss: Double, asset: String, owner: String) -> Unit
) {
    var cve by remember { mutableStateOf("CVE-2024-") }
    var title by remember { mutableStateOf("") }
    var asset by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("Security Team") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        title = {
            Text(
                text = "Log Security Finding",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = cve,
                    onValueChange = { cve = it },
                    label = { Text("CVE Identifier") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Finding Title") },
                    placeholder = { Text("e.g. Remote Code Execution in Web App") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = asset,
                    onValueChange = { asset = it },
                    label = { Text("Affected Device") },
                    placeholder = { Text("e.g. prod-api-cluster") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cve.isNotBlank() && title.isNotBlank() && asset.isNotBlank()) {
                        onAdd(cve, title, Severity.HIGH, 7.5, asset, owner)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Log Issue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

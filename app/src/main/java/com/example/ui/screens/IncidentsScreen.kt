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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentsScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val incidents by viewModel.incidents.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Incidents, 1 = Alerts
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedIncidentForWarRoom by remember { mutableStateOf<Incident?>(null) }

    val filteredIncidents = incidents.filter { inc ->
        searchQuery.isBlank() ||
                inc.title.contains(searchQuery, ignoreCase = true) ||
                inc.affectedAsset.contains(searchQuery, ignoreCase = true) ||
                inc.assignedAnalyst.contains(searchQuery, ignoreCase = true)
    }

    val filteredAlerts = alerts.filter { alert ->
        searchQuery.isBlank() ||
                alert.title.contains(searchQuery, ignoreCase = true) ||
                alert.targetAsset.contains(searchQuery, ignoreCase = true) ||
                alert.source.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("incidents_screen")
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
                    text = "Alerts & Response",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (selectedTab == 0) "${incidents.count { it.status != IncidentStatus.RESOLVED && it.status != IncidentStatus.CLOSED }} active incidents" else "${alerts.count { !it.isHandled }} unhandled alerts",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (selectedTab == 0) {
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("fab_declare_incident")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Incident", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Tab Selector (Incidents vs Alerts)
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberSurfaceDark,
            contentColor = CyberCyan,
            divider = { HorizontalDivider(color = CyberCardBorder) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Incidents (${incidents.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Alerts (${alerts.count { !it.isHandled }})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            )
        }

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = if (selectedTab == 0) "Search incidents by title or asset..." else "Search incoming security alerts...",
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // List Content
        if (selectedTab == 0) {
            if (filteredIncidents.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = "No incidents found",
                    description = if (searchQuery.isNotBlank()) "No incidents matched \"$searchQuery\"." else "All systems operating normally with no active incidents.",
                    actionLabel = if (searchQuery.isNotBlank()) "Clear Search" else null,
                    onAction = { searchQuery = "" },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("incidents_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredIncidents, key = { it.id }) { inc ->
                        IncidentItemCard(
                            incident = inc,
                            onClick = { selectedIncidentForWarRoom = inc }
                        )
                    }
                }
            }
        } else {
            if (filteredAlerts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.NotificationsNone,
                    title = "No alerts found",
                    description = if (searchQuery.isNotBlank()) "No alerts matched \"$searchQuery\"." else "All incoming monitoring signals are clear.",
                    actionLabel = if (searchQuery.isNotBlank()) "Clear Search" else null,
                    onAction = { searchQuery = "" },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("alerts_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredAlerts, key = { it.id }) { alert ->
                        AlertItemCard(
                            alert = alert,
                            onEscalate = { viewModel.escalateAlert(alert.id) }
                        )
                    }
                }
            }
        }
    }

    // War Room Detail Bottom Sheet
    selectedIncidentForWarRoom?.let { inc ->
        IncidentWarRoomSheet(
            incident = inc,
            onDismiss = { selectedIncidentForWarRoom = null },
            onUpdateStatus = { newStatus, notes ->
                viewModel.updateIncidentStatus(inc.id, newStatus, notes)
                selectedIncidentForWarRoom = null
            }
        )
    }

    // Declare Incident Dialog
    if (showCreateDialog) {
        DeclareIncidentDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, severity, asset, analyst, desc, evidence ->
                viewModel.createIncident(title, severity, asset, analyst, desc, evidence)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun IncidentItemCard(
    incident: Incident,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (incident.severity == Severity.CRITICAL) CyberRed.copy(alpha = 0.4f) else CyberCardBorder
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("incident_card_${incident.id}")
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
                    SeverityBadge(severity = incident.severity)
                    Text(
                        text = incident.id,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                StatusPill(
                    text = incident.status.label,
                    color = when (incident.status) {
                        IncidentStatus.NEW -> CyberRed
                        IncidentStatus.INVESTIGATING -> CyberOrange
                        IncidentStatus.CONTAINED -> CyberYellow
                        IncidentStatus.RESOLVED -> CyberGreen
                        IncidentStatus.CLOSED -> TextMuted
                    }
                )
            }

            Text(
                text = incident.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = incident.description,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                lineHeight = 16.sp
            )

            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target: ${incident.affectedAsset}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Analyst: ${incident.assignedAnalyst}",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AlertItemCard(
    alert: SecurityAlert,
    onEscalate: () -> Unit,
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
                    SeverityBadge(severity = alert.severity)
                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = alert.source,
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(text = alert.timestamp, color = TextMuted, fontSize = 11.sp)
            }

            Text(
                text = alert.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = alert.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target: ${alert.targetAsset}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                if (!alert.isHandled) {
                    Button(
                        onClick = onEscalate,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRed, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_escalate_${alert.id}")
                    ) {
                        Text("Escalate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    StatusPill(text = "Escalated", color = CyberGreen)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentWarRoomSheet(
    incident: Incident,
    onDismiss: () -> Unit,
    onUpdateStatus: (IncidentStatus, String) -> Unit
) {
    var resolutionText by remember { mutableStateOf(incident.resolutionNotes) }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = incident.severity)
                StatusPill(
                    text = incident.status.label,
                    color = when (incident.status) {
                        IncidentStatus.RESOLVED -> CyberGreen
                        IncidentStatus.CONTAINED -> CyberYellow
                        else -> CyberOrange
                    }
                )
            }

            Text(
                text = "${incident.id}: ${incident.title}",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = incident.description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            HorizontalDivider(color = CyberCardBorder)

            DetailRow(label = "Target Asset", value = incident.affectedAsset)
            DetailRow(label = "Assigned Analyst", value = incident.assignedAnalyst)
            DetailRow(label = "Evidence Count", value = "${incident.evidenceList.size} items recorded")

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Update Status:",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdateStatus(IncidentStatus.CONTAINED, "Contained via firewall isolation") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Contain", fontSize = 12.sp)
                }

                Button(
                    onClick = { onUpdateStatus(IncidentStatus.RESOLVED, "Issue fully resolved and verified") },
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
fun DeclareIncidentDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, severity: Severity, asset: String, analyst: String, desc: String, evidence: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(Severity.HIGH) }
    var asset by remember { mutableStateOf("") }
    var analyst by remember { mutableStateOf("SOC Lead") }
    var desc by remember { mutableStateOf("") }
    var evidence by remember { mutableStateOf("Security alert logs") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        title = {
            Text(
                text = "Declare Security Incident",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Incident Title") },
                    placeholder = { Text("e.g. Unauthorized access attempt") },
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
                    label = { Text("Affected Device / Target") },
                    placeholder = { Text("e.g. prod-db-primary") },
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
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Summary / Observations") },
                    maxLines = 3,
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
                    if (title.isNotBlank() && asset.isNotBlank()) {
                        onCreate(title, severity, asset, analyst, desc.ifBlank { "Investigating security incident" }, evidence)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberRed, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Declare Incident")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

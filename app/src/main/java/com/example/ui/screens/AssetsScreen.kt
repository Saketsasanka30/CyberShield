package com.example.ui.screens

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
fun AssetsScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val assets by viewModel.assets.collectAsState()
    var selectedTypeFilter by remember { mutableStateOf<AssetType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAssetForDetail by remember { mutableStateOf<Asset?>(null) }

    val filteredAssets = assets.filter { asset ->
        val matchesType = selectedTypeFilter == null || asset.type == selectedTypeFilter
        val matchesSearch = searchQuery.isBlank() ||
                asset.name.contains(searchQuery, ignoreCase = true) ||
                asset.ipOrHost.contains(searchQuery, ignoreCase = true) ||
                asset.owner.contains(searchQuery, ignoreCase = true) ||
                asset.osOrPlatform.contains(searchQuery, ignoreCase = true)
        matchesType && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("assets_screen")
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Monitored Devices",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${filteredAssets.size} total devices enrolled",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("fab_add_asset")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Device", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search devices by name, IP, or owner...",
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Type Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All (${assets.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberBlue,
                        selectedLabelColor = Color.White,
                        containerColor = CyberSurfaceDark,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            items(AssetType.values()) { type ->
                val count = assets.count { it.type == type }
                FilterChip(
                    selected = selectedTypeFilter == type,
                    onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                    label = { Text("${type.label} ($count)", fontSize = 12.sp) },
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

        // Asset List or Empty State
        if (filteredAssets.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Devices,
                title = "No devices found",
                description = if (searchQuery.isNotBlank()) "No devices matched \"$searchQuery\"." else "No devices registered in this category.",
                actionLabel = if (searchQuery.isNotBlank()) "Clear Search" else "Add Device",
                onAction = {
                    if (searchQuery.isNotBlank()) searchQuery = "" else showAddDialog = true
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("assets_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredAssets, key = { it.id }) { asset ->
                    AssetCard(
                        asset = asset,
                        onClick = { selectedAssetForDetail = asset }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    selectedAssetForDetail?.let { asset ->
        AssetDetailSheet(
            asset = asset,
            onDismiss = { selectedAssetForDetail = null }
        )
    }

    // Add Asset Dialog
    if (showAddDialog) {
        AddAssetDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, type, ip, os, owner, ports, isExposed ->
                viewModel.addAsset(name, type, ip, os, owner, ports, isExposed)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AssetCard(
    asset: Asset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusIcon) = when (asset.healthStatus) {
        HealthStatus.HEALTHY -> Pair(CyberGreen, Icons.Default.CheckCircle)
        HealthStatus.WARNING -> Pair(CyberYellow, Icons.Default.Warning)
        HealthStatus.CRITICAL -> Pair(CyberRed, Icons.Default.GppMaybe)
        HealthStatus.OFFLINE -> Pair(TextMuted, Icons.Default.PowerOff)
    }

    val typeIcon = when (asset.type) {
        AssetType.SERVER -> Icons.Default.Storage
        AssetType.DEVICE -> Icons.Default.Laptop
        AssetType.DOMAIN -> Icons.Default.Language
        AssetType.IP_ADDRESS -> Icons.Default.Hub
        AssetType.APPLICATION -> Icons.Default.Apps
        AssetType.API -> Icons.Default.Code
        AssetType.CLOUD_RESOURCE -> Icons.Default.Cloud
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (asset.isExposedToInternet) CyberOrange.copy(alpha = 0.4f) else CyberCardBorder
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("asset_card_${asset.id}")
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = asset.type.label,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = asset.name,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${asset.ipOrHost} • ${asset.osOrPlatform}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = asset.healthStatus.label,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Owner: ${asset.owner}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    if (asset.isExposedToInternet) {
                        Surface(
                            color = CyberOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberOrange.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Public Port",
                                color = CyberOrange,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (asset.vulnerabilityCount > 0) "${asset.vulnerabilityCount} CVEs" else "No issues",
                    color = if (asset.vulnerabilityCount > 0) CyberRed else CyberGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailSheet(
    asset: Asset,
    onDismiss: () -> Unit
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
                Column {
                    Text(
                        text = asset.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${asset.id} • ${asset.type.label}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = when (asset.healthStatus) {
                        HealthStatus.HEALTHY -> CyberGreen.copy(alpha = 0.15f)
                        HealthStatus.WARNING -> CyberYellow.copy(alpha = 0.15f)
                        HealthStatus.CRITICAL -> CyberRed.copy(alpha = 0.15f)
                        HealthStatus.OFFLINE -> CyberSurfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = asset.healthStatus.label,
                        color = when (asset.healthStatus) {
                            HealthStatus.HEALTHY -> CyberGreen
                            HealthStatus.WARNING -> CyberYellow
                            HealthStatus.CRITICAL -> CyberRed
                            HealthStatus.OFFLINE -> TextMuted
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = CyberCardBorder)

            DetailRow(label = "Network Address", value = asset.ipOrHost)
            DetailRow(label = "Platform / OS", value = asset.osOrPlatform)
            DetailRow(label = "Primary Owner", value = asset.owner)
            DetailRow(label = "Open Ports", value = if (asset.openPorts.isEmpty()) "None (Hardened)" else asset.openPorts.joinToString(", "))
            DetailRow(label = "Internet Ingress", value = if (asset.isExposedToInternet) "Publicly Reachable" else "Internal Only (Private Subnet)")
            DetailRow(label = "Patch Level", value = asset.patchStatus)
            DetailRow(label = "Last Audited", value = asset.lastScanned)
            DetailRow(label = "Active Vulnerabilities", value = "${asset.vulnerabilityCount} CVE(s)")

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = "Close Details",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AddAssetDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, type: AssetType, ip: String, os: String, owner: String, ports: List<Int>, isExposed: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AssetType.SERVER) }
    var ip by remember { mutableStateOf("") }
    var os by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var portsStr by remember { mutableStateOf("80, 443") }
    var isExposed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        title = {
            Text(
                text = "Register New Device",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Device Name") },
                    placeholder = { Text("e.g. prod-api-01") },
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
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("IP Address / Hostname") },
                    placeholder = { Text("10.0.1.50") },
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
                    value = os,
                    onValueChange = { os = it },
                    label = { Text("Operating System") },
                    placeholder = { Text("Ubuntu 22.04 LTS") },
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
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text("Custodian / Owner") },
                    placeholder = { Text("DevOps Team") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Exposed to Public Internet", color = TextSecondary, fontSize = 13.sp)
                    Switch(
                        checked = isExposed,
                        onCheckedChange = { isExposed = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberOrange)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && ip.isNotBlank()) {
                        val parsedPorts = portsStr.split(",")
                            .mapNotNull { it.trim().toIntOrNull() }
                        onAdd(name, selectedType, ip, os.ifBlank { "Linux / Cloud" }, owner.ifBlank { "SecOps Team" }, parsedPorts, isExposed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Enroll Device")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

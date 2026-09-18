package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CyberShieldRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val riskScore: Int, // 0 - 100 (higher = safer, e.g. 78/100, Grade B)
    val riskGrade: String,
    val criticalVulnsCount: Int,
    val openIncidentsCount: Int,
    val securityAlertsCount: Int,
    val assetHealthPercentage: Int,
    val exposedServicesCount: Int,
    val patchPendingCount: Int,
    val authAnomaliesCount: Int
)

class CyberShieldViewModel(
    private val repository: CyberShieldRepository = CyberShieldRepository()
) : ViewModel() {

    val currentRole: StateFlow<RbacRole> = repository.currentRole
    val assets: StateFlow<List<Asset>> = repository.assets
    val vulnerabilities: StateFlow<List<Vulnerability>> = repository.vulnerabilities
    val incidents: StateFlow<List<Incident>> = repository.incidents
    val alerts: StateFlow<List<SecurityAlert>> = repository.alerts
    val threatIndicators: StateFlow<List<ThreatIndicator>> = repository.threatIndicators
    val threatCampaigns: StateFlow<List<ThreatCampaign>> = repository.threatCampaigns
    val threatReports: StateFlow<List<ThreatReport>> = repository.threatReports
    val networkNodes: StateFlow<List<NetworkNode>> = repository.networkNodes
    val networkLinks: StateFlow<List<NetworkLink>> = repository.networkLinks
    val identities: StateFlow<List<IdentityAccount>> = repository.identities
    val cloudFindings: StateFlow<List<CloudSecurityFinding>> = repository.cloudFindings
    val apiEndpoints: StateFlow<List<ApiEndpointRecord>> = repository.apiEndpoints
    val complianceStandards: StateFlow<List<ComplianceStandard>> = repository.complianceStandards
    val auditLogs: StateFlow<List<AuditLogEntry>> = repository.auditLogs
    val latestScanResult: StateFlow<SafeScanResult?> = repository.latestScanResult
    val isScanning: StateFlow<Boolean> = repository.isScanning

    // User Message / Snackbar
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    // Dashboard aggregated security metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        vulnerabilities,
        incidents,
        alerts,
        assets,
        identities
    ) { vulns, incs, alrts, asts, idents ->
        val critVulns = vulns.count { it.severity == Severity.CRITICAL && it.remediationStatus != RemediationStatus.RESOLVED }
        val openIncs = incs.count { it.status != IncidentStatus.RESOLVED && it.status != IncidentStatus.CLOSED }
        val activeAlerts = alrts.count { !it.isHandled }
        val healthyAssets = asts.count { it.healthStatus == HealthStatus.HEALTHY }
        val totalAssets = if (asts.isEmpty()) 1 else asts.size
        val assetHealth = (healthyAssets * 100) / totalAssets

        val exposed = asts.count { it.isExposedToInternet }
        val patchPending = asts.count { it.patchStatus.contains("Update", ignoreCase = true) || it.patchStatus.contains("Reboot", ignoreCase = true) }
        val anomalies = idents.count { it.anomalyFlag != null }

        // Dynamic Posture Risk calculation (base 100 minus weighted penalties)
        val penalty = (critVulns * 8) + (openIncs * 6) + (activeAlerts * 2) + (anomalies * 4) + (exposed * 2)
        val score = (100 - penalty).coerceIn(25, 99)
        val grade = when {
            score >= 90 -> "A"
            score >= 80 -> "B+"
            score >= 70 -> "B"
            score >= 60 -> "C"
            else -> "D"
        }

        DashboardMetrics(
            riskScore = score,
            riskGrade = grade,
            criticalVulnsCount = critVulns,
            openIncidentsCount = openIncs,
            securityAlertsCount = activeAlerts,
            assetHealthPercentage = assetHealth,
            exposedServicesCount = exposed,
            patchPendingCount = patchPending,
            authAnomaliesCount = anomalies
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardMetrics(84, "B+", 2, 2, 4, 71, 3, 2, 2)
    )

    fun switchRole(newRole: RbacRole) {
        repository.setRbacRole(newRole)
        _userMessage.value = "Switched active RBAC role to: ${newRole.title}"
    }

    fun addAsset(
        name: String,
        type: AssetType,
        ipOrHost: String,
        osOrPlatform: String,
        owner: String,
        openPorts: List<Int>,
        isExposed: Boolean
    ) {
        val success = repository.addAsset(name, type, ipOrHost, osOrPlatform, owner, openPorts, isExposed)
        if (success) {
            _userMessage.value = "Asset '$name' successfully added to inventory."
        } else {
            _userMessage.value = "Access Denied: Current role does not have asset management permission."
        }
    }

    fun updateVulnerability(cve: String, status: RemediationStatus, owner: String) {
        val success = repository.updateVulnerabilityStatus(cve, status, owner)
        if (success) {
            _userMessage.value = "Vulnerability $cve status updated to ${status.label}."
        } else {
            _userMessage.value = "Access Denied: Auditor/Read-Only roles cannot modify remediation records."
        }
    }

    fun transitionVulnerabilityStatus(cve: String, newStatus: RemediationStatus) {
        val success = repository.transitionVulnerabilityStatus(cve, newStatus)
        if (success) {
            _userMessage.value = "Vulnerability $cve transitioned to ${newStatus.label}."
        } else {
            _userMessage.value = "Access Denied: Cannot modify remediation records with current role."
        }
    }

    fun addVulnerability(
        cve: String,
        title: String,
        severity: Severity,
        cvssScore: Double,
        affectedAsset: String,
        evidence: String,
        owner: String,
        description: String,
        recommendation: String
    ) {
        val success = repository.addVulnerability(
            cve, title, severity, cvssScore, affectedAsset, evidence, owner, description, recommendation
        )
        if (success) {
            _userMessage.value = "CVE record $cve registered successfully."
        } else {
            _userMessage.value = "Access Denied: Insufficient permissions."
        }
    }

    fun createIncident(
        title: String,
        severity: Severity,
        affectedAsset: String,
        analyst: String,
        description: String,
        evidence: String
    ) {
        val success = repository.createIncident(title, severity, affectedAsset, analyst, description, evidence)
        if (success) {
            _userMessage.value = "Security Incident declared & assigned to $analyst."
        } else {
            _userMessage.value = "Failed to declare incident: Check permissions."
        }
    }

    fun addIncidentTimeline(incidentId: String, phase: String, description: String) {
        val success = repository.addIncidentTimelineEvent(incidentId, phase, description)
        if (success) {
            _userMessage.value = "Timeline updated for $incidentId."
        }
    }

    fun updateIncidentStatus(incidentId: String, newStatus: IncidentStatus, notes: String = "") {
        val success = repository.updateIncidentStatus(incidentId, newStatus, notes)
        if (success) {
            _userMessage.value = "Incident $incidentId progressed to ${newStatus.label}."
        } else {
            _userMessage.value = "Access Denied: Insufficient permissions to alter incident state."
        }
    }

    fun escalateAlert(alertId: String) {
        val success = repository.escalateAlertToIncident(alertId)
        if (success) {
            _userMessage.value = "Alert $alertId escalated to active Security Incident."
        }
    }

    fun toggleAlertHandled(alertId: String) {
        repository.toggleAlertHandled(alertId)
    }

    fun runSafeScan(target: String) {
        viewModelScope.launch {
            _userMessage.value = "Initiating safe non-destructive audit scan against $target..."
            val result = repository.runSafeAuthorizedScan(target)
            if (result.safetyVerified) {
                _userMessage.value = "Safe scan completed for ${result.target}. Found ${result.openPortsDetected.size} open ports."
            } else {
                _userMessage.value = "Scan Blocked by Safety Guardrails: Target not authorized."
            }
        }
    }

    fun getReportContent(reportType: String): String {
        return repository.generateReportContent(reportType)
    }
}

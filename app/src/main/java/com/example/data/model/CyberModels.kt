package com.example.data.model

enum class Severity(val label: String, val weight: Int) {
    CRITICAL("Critical", 5),
    HIGH("High", 4),
    MEDIUM("Medium", 3),
    LOW("Low", 2),
    INFO("Info", 1)
}

enum class RemediationStatus(val label: String) {
    OPEN("Open"),
    PATCHING("Patching"),
    RESOLVED("Resolved"),
    IN_PROGRESS("Patching"),
    MITIGATED("Mitigated"),
    FALSE_POSITIVE("False Positive")
}

enum class IncidentStatus(val label: String) {
    NEW("New"),
    INVESTIGATING("Investigating"),
    CONTAINED("Contained"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class AssetType(val label: String) {
    SERVER("Server"),
    DEVICE("Endpoint Device"),
    DOMAIN("Domain / DNS"),
    IP_ADDRESS("IP Address"),
    APPLICATION("Application"),
    API("API Endpoint"),
    CLOUD_RESOURCE("Cloud Resource")
}

enum class HealthStatus(val label: String) {
    HEALTHY("Healthy"),
    WARNING("Warning"),
    CRITICAL("Compromised"),
    OFFLINE("Offline")
}

enum class RbacRole(val title: String, val description: String) {
    ADMIN("Security Admin", "Full read/write, policy enforcement, user management, and scanner execution"),
    ANALYST("SOC Analyst", "Triage alerts, manage vulnerabilities & incidents, run safe scans"),
    AUDITOR("Compliance Auditor", "Read-only access to all compliance controls, reports, and audit logs"),
    READ_ONLY("Executive Viewer", "High-level dashboard & report reading only")
}

data class Asset(
    val id: String,
    val name: String,
    val type: AssetType,
    val ipOrHost: String,
    val osOrPlatform: String,
    val owner: String,
    val openPorts: List<Int>,
    val healthStatus: HealthStatus,
    val vulnerabilityCount: Int,
    val lastScanned: String,
    val isExposedToInternet: Boolean = false,
    val patchStatus: String = "Up to date"
)

data class Vulnerability(
    val cve: String,
    val title: String,
    val severity: Severity,
    val cvssScore: Double,
    val affectedAsset: String,
    val discoveryDate: String,
    var remediationStatus: RemediationStatus,
    var remediationOwner: String,
    val evidence: String,
    val description: String,
    val mitigationRecommendation: String
)

data class TimelineEvent(
    val time: String,
    val phase: String, // Detection, Triage, Containment, Eradication, Post-Mortem
    val description: String,
    val analyst: String
)

data class Incident(
    val id: String,
    val title: String,
    val severity: Severity,
    var status: IncidentStatus,
    var assignedAnalyst: String,
    val affectedAsset: String,
    val createdAt: String,
    val description: String,
    val timeline: MutableList<TimelineEvent>,
    val evidenceList: MutableList<String>,
    var resolutionNotes: String = ""
)

data class SecurityAlert(
    val id: String,
    val title: String,
    val severity: Severity,
    val source: String, // SIEM, EDR, WAF, CloudTrail, IDS
    val timestamp: String,
    val description: String,
    val targetAsset: String,
    var isHandled: Boolean = false
)

enum class IocType { IP, DOMAIN, HASH_SHA256, URL }

data class ThreatIndicator(
    val id: String,
    val type: IocType,
    val value: String,
    val threatFamily: String,
    val confidence: Int, // 0 - 100%
    val lastSeen: String,
    val matchedEventsCount: Int
)

data class ThreatCampaign(
    val name: String,
    val actor: String,
    val targetSectors: List<String>,
    val tactics: List<String>,
    val activeIndicatorsCount: Int,
    val summary: String
)

data class ThreatReport(
    val id: String,
    val title: String,
    val date: String,
    val source: String,
    val tlp: String, // TLP:CLEAR, TLP:AMBER, TLP:RED
    val executiveSummary: String
)

enum class NetworkZone { INTERNET, DMZ, INTERNAL_LAN, DATABASE_CLUSTER, CLOUD_VPC }

data class NetworkNode(
    val id: String,
    val label: String,
    val zone: NetworkZone,
    val ip: String,
    val status: HealthStatus,
    val activeConnections: Int,
    val blockedTrafficCount: Int
)

data class NetworkLink(
    val sourceId: String,
    val targetId: String,
    val protocol: String,
    val isEncrypted: Boolean,
    val isSuspicious: Boolean = false
)

data class IdentityAccount(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val mfaEnabled: Boolean,
    val lastLogin: String,
    val anomalyFlag: String? = null,
    val riskScore: Int
)

data class CloudSecurityFinding(
    val id: String,
    val cloudProvider: String, // AWS, GCP, Azure
    val resourceName: String,
    val finding: String,
    val severity: Severity,
    val complianceStandard: String,
    val remediationStep: String
)

data class ApiEndpointRecord(
    val id: String,
    val path: String,
    val method: String,
    val authRequired: Boolean,
    val rateLimitEnabled: Boolean,
    val requestVolume24h: String,
    val riskAssessment: Severity
)

data class ComplianceControl(
    val id: String,
    val name: String,
    val standard: String,
    val isPassed: Boolean,
    val details: String
)

data class ComplianceStandard(
    val name: String,
    val scorePercentage: Int,
    val totalControls: Int,
    val passedControls: Int,
    val controls: List<ComplianceControl>
)

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val actor: String,
    val role: RbacRole,
    val action: String,
    val resource: String,
    val status: String, // SUCCESS, BLOCKED, FAILED
    val ipAddress: String
)

data class SafeScanResult(
    val target: String,
    val timestamp: String,
    val openPortsDetected: List<String>,
    val bannersGrabbed: List<String>,
    val safetyVerified: Boolean = true,
    val vulnerabilitiesIdentified: List<String>
)

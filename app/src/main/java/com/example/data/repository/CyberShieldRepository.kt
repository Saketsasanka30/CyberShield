package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CyberShieldRepository {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val shortTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    // RBAC Current Role
    private val _currentRole = MutableStateFlow(RbacRole.ADMIN)
    val currentRole: StateFlow<RbacRole> = _currentRole.asStateFlow()

    // Assets
    private val _assets = MutableStateFlow<List<Asset>>(emptyList())
    val assets: StateFlow<List<Asset>> = _assets.asStateFlow()

    // Vulnerabilities
    private val _vulnerabilities = MutableStateFlow<List<Vulnerability>>(emptyList())
    val vulnerabilities: StateFlow<List<Vulnerability>> = _vulnerabilities.asStateFlow()

    // Incidents
    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    // Security Alerts
    private val _alerts = MutableStateFlow<List<SecurityAlert>>(emptyList())
    val alerts: StateFlow<List<SecurityAlert>> = _alerts.asStateFlow()

    // Threat Intelligence
    private val _threatIndicators = MutableStateFlow<List<ThreatIndicator>>(emptyList())
    val threatIndicators: StateFlow<List<ThreatIndicator>> = _threatIndicators.asStateFlow()

    private val _threatCampaigns = MutableStateFlow<List<ThreatCampaign>>(emptyList())
    val threatCampaigns: StateFlow<List<ThreatCampaign>> = _threatCampaigns.asStateFlow()

    private val _threatReports = MutableStateFlow<List<ThreatReport>>(emptyList())
    val threatReports: StateFlow<List<ThreatReport>> = _threatReports.asStateFlow()

    // Network Topology
    private val _networkNodes = MutableStateFlow<List<NetworkNode>>(emptyList())
    val networkNodes: StateFlow<List<NetworkNode>> = _networkNodes.asStateFlow()

    private val _networkLinks = MutableStateFlow<List<NetworkLink>>(emptyList())
    val networkLinks: StateFlow<List<NetworkLink>> = _networkLinks.asStateFlow()

    // Identity & Accounts
    private val _identities = MutableStateFlow<List<IdentityAccount>>(emptyList())
    val identities: StateFlow<List<IdentityAccount>> = _identities.asStateFlow()

    // Cloud & API
    private val _cloudFindings = MutableStateFlow<List<CloudSecurityFinding>>(emptyList())
    val cloudFindings: StateFlow<List<CloudSecurityFinding>> = _cloudFindings.asStateFlow()

    private val _apiEndpoints = MutableStateFlow<List<ApiEndpointRecord>>(emptyList())
    val apiEndpoints: StateFlow<List<ApiEndpointRecord>> = _apiEndpoints.asStateFlow()

    // Compliance
    private val _complianceStandards = MutableStateFlow<List<ComplianceStandard>>(emptyList())
    val complianceStandards: StateFlow<List<ComplianceStandard>> = _complianceStandards.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    // Scanner state
    private val _latestScanResult = MutableStateFlow<SafeScanResult?>(null)
    val latestScanResult: StateFlow<SafeScanResult?> = _latestScanResult.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        seedInitialSocData()
    }

    private fun logAction(action: String, resource: String, status: String = "SUCCESS") {
        val entry = AuditLogEntry(
            id = "AUD-${UUID.randomUUID().toString().take(8).uppercase()}",
            timestamp = timeFormat.format(Date()),
            actor = "SecAdmin-User",
            role = _currentRole.value,
            action = action,
            resource = resource,
            status = status,
            ipAddress = "10.14.8.42 (Internal VPN)"
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }

    fun setRbacRole(role: RbacRole) {
        val prev = _currentRole.value
        _currentRole.value = role
        logAction("ROLE_SWITCH", "Changed active role from ${prev.name} to ${role.name}")
    }

    fun addAsset(
        name: String,
        type: AssetType,
        ipOrHost: String,
        osOrPlatform: String,
        owner: String,
        openPorts: List<Int>,
        isExposed: Boolean
    ): Boolean {
        if (_currentRole.value == RbacRole.AUDITOR || _currentRole.value == RbacRole.READ_ONLY) {
            logAction("ASSET_CREATE_DENIED", "Insufficient permissions for ${currentRole.value}", "BLOCKED")
            return false
        }
        val newAsset = Asset(
            id = "AST-${UUID.randomUUID().toString().take(6).uppercase()}",
            name = name,
            type = type,
            ipOrHost = ipOrHost,
            osOrPlatform = osOrPlatform,
            owner = owner,
            openPorts = openPorts,
            healthStatus = HealthStatus.HEALTHY,
            vulnerabilityCount = 0,
            lastScanned = "Just now",
            isExposedToInternet = isExposed,
            patchStatus = "Up to date"
        )
        _assets.value = listOf(newAsset) + _assets.value
        logAction("ASSET_CREATED", "Asset ${newAsset.name} ($ipOrHost)")
        return true
    }

    fun updateVulnerabilityStatus(cve: String, newStatus: RemediationStatus, owner: String): Boolean {
        if (_currentRole.value == RbacRole.AUDITOR || _currentRole.value == RbacRole.READ_ONLY) {
            logAction("VULN_UPDATE_DENIED", "Insufficient permissions for ${currentRole.value}", "BLOCKED")
            return false
        }
        _vulnerabilities.value = _vulnerabilities.value.map { vuln ->
            if (vuln.cve == cve) {
                vuln.copy(remediationStatus = newStatus, remediationOwner = owner)
            } else vuln
        }
        logAction("VULN_STATUS_UPDATE", "CVE $cve updated to ${newStatus.label} (Owner: $owner)")
        return true
    }

    fun transitionVulnerabilityStatus(cve: String, newStatus: RemediationStatus): Boolean {
        val vuln = _vulnerabilities.value.find { it.cve == cve } ?: return false
        return updateVulnerabilityStatus(cve, newStatus, vuln.remediationOwner)
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
    ): Boolean {
        if (_currentRole.value == RbacRole.AUDITOR || _currentRole.value == RbacRole.READ_ONLY) {
            logAction("VULN_CREATE_DENIED", "Role lacks permission", "BLOCKED")
            return false
        }
        val newVuln = Vulnerability(
            cve = cve,
            title = title,
            severity = severity,
            cvssScore = cvssScore,
            affectedAsset = affectedAsset,
            discoveryDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
            remediationStatus = RemediationStatus.OPEN,
            remediationOwner = owner,
            evidence = evidence,
            description = description,
            mitigationRecommendation = recommendation
        )
        _vulnerabilities.value = listOf(newVuln) + _vulnerabilities.value
        logAction("VULN_CREATED", "CVE $cve created on $affectedAsset")
        return true
    }

    fun createIncident(
        title: String,
        severity: Severity,
        affectedAsset: String,
        assignedAnalyst: String,
        description: String,
        initialEvidence: String
    ): Boolean {
        if (_currentRole.value == RbacRole.READ_ONLY) {
            logAction("INCIDENT_CREATE_DENIED", "Read-only access", "BLOCKED")
            return false
        }
        val incId = "INC-${(1000 + _incidents.value.size + 1)}"
        val initialTimeline = mutableListOf(
            TimelineEvent(
                time = shortTimeFormat.format(Date()),
                phase = "Detection",
                description = "Incident declared by SOC Operator. Initial telemetry ingested.",
                analyst = assignedAnalyst
            )
        )
        val initialEvidenceList = mutableListOf<String>()
        if (initialEvidence.isNotBlank()) initialEvidenceList.add(initialEvidence)

        val incident = Incident(
            id = incId,
            title = title,
            severity = severity,
            status = IncidentStatus.INVESTIGATING,
            assignedAnalyst = assignedAnalyst,
            affectedAsset = affectedAsset,
            createdAt = timeFormat.format(Date()),
            description = description,
            timeline = initialTimeline,
            evidenceList = initialEvidenceList
        )
        _incidents.value = listOf(incident) + _incidents.value
        logAction("INCIDENT_CREATED", "$incId: $title ($affectedAsset)")
        return true
    }

    fun addIncidentTimelineEvent(incidentId: String, phase: String, description: String): Boolean {
        if (_currentRole.value == RbacRole.AUDITOR || _currentRole.value == RbacRole.READ_ONLY) {
            return false
        }
        _incidents.value = _incidents.value.map { inc ->
            if (inc.id == incidentId) {
                val updatedTimeline = ArrayList(inc.timeline)
                updatedTimeline.add(
                    TimelineEvent(
                        time = shortTimeFormat.format(Date()),
                        phase = phase,
                        description = description,
                        analyst = inc.assignedAnalyst
                    )
                )
                inc.copy(timeline = updatedTimeline)
            } else inc
        }
        logAction("INCIDENT_TIMELINE_ADD", "$incidentId phase: $phase")
        return true
    }

    fun updateIncidentStatus(incidentId: String, newStatus: IncidentStatus, notes: String = ""): Boolean {
        if (_currentRole.value == RbacRole.AUDITOR || _currentRole.value == RbacRole.READ_ONLY) {
            return false
        }
        _incidents.value = _incidents.value.map { inc ->
            if (inc.id == incidentId) {
                val updatedTimeline = ArrayList(inc.timeline)
                updatedTimeline.add(
                    TimelineEvent(
                        time = shortTimeFormat.format(Date()),
                        phase = when (newStatus) {
                            IncidentStatus.CONTAINED -> "Containment"
                            IncidentStatus.RESOLVED -> "Resolution"
                            IncidentStatus.CLOSED -> "Post-Mortem"
                            else -> "Triage"
                        },
                        description = "Status transitioned to ${newStatus.label}. Note: ${notes.ifBlank { "Standard protocol followed" }}",
                        analyst = inc.assignedAnalyst
                    )
                )
                inc.copy(
                    status = newStatus,
                    timeline = updatedTimeline,
                    resolutionNotes = if (notes.isNotBlank()) notes else inc.resolutionNotes
                )
            } else inc
        }
        logAction("INCIDENT_STATUS_CHANGE", "$incidentId transitioned to ${newStatus.label}")
        return true
    }

    fun escalateAlertToIncident(alertId: String): Boolean {
        val alert = _alerts.value.find { it.id == alertId } ?: return false
        createIncident(
            title = "Escalated: ${alert.title}",
            severity = alert.severity,
            affectedAsset = alert.targetAsset,
            assignedAnalyst = "Duty Analyst (L2)",
            description = "Auto-escalated from SIEM Alert ${alert.id}. Source: ${alert.source}. Description: ${alert.description}",
            initialEvidence = "SIEM Alert ID: ${alert.id} | Timestamp: ${alert.timestamp} | Telemetry raw log signature matched"
        )
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(isHandled = true) else it
        }
        logAction("ALERT_ESCALATED", "Alert $alertId escalated to incident")
        return true
    }

    fun toggleAlertHandled(alertId: String): Boolean {
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(isHandled = !it.isHandled) else it
        }
        logAction("ALERT_TOGGLE_HANDLED", "Alert $alertId handled state changed")
        return true
    }

    /**
     * Executes safe, strictly non-destructive network scanning against explicitly authorized demo/lab targets.
     * Complies strictly with safety rules: No destructive scanning, only authorized targets.
     */
    suspend fun runSafeAuthorizedScan(target: String): SafeScanResult {
        if (_currentRole.value == RbacRole.AUDITOR || _currentRole.value == RbacRole.READ_ONLY) {
            logAction("SCAN_DENIED", "Role ${_currentRole.value} not authorized to trigger scans", "BLOCKED")
            return SafeScanResult(
                target = target,
                timestamp = timeFormat.format(Date()),
                openPortsDetected = emptyList(),
                bannersGrabbed = emptyList(),
                safetyVerified = false,
                vulnerabilitiesIdentified = listOf("Scanning rejected: User role does not hold Operator Scan privileges.")
            )
        }

        _isScanning.value = true
        kotlinx.coroutines.delay(1200) // Safe simulation duration

        val authorizedTargets = listOf("demo.internal.corp", "10.0.0.1/24", "192.168.1.100", "api.testlab.local")
        val isTargetAuthorized = authorizedTargets.any { target.contains(it, ignoreCase = true) } || target.startsWith("10.") || target.startsWith("192.168.")

        val result = if (isTargetAuthorized) {
            SafeScanResult(
                target = target,
                timestamp = timeFormat.format(Date()),
                openPortsDetected = listOf("TCP 80 (HTTP)", "TCP 443 (HTTPS)", "TCP 22 (SSH OpenSSH 8.9p1)", "TCP 8443 (API Gateway)"),
                bannersGrabbed = listOf(
                    "nginx/1.22.1 - Authorized Demolab Host",
                    "OpenSSH_8.9p1 Ubuntu-3ubuntu0.6",
                    "REST Gateway v2.4 (TLS 1.3 Strict)"
                ),
                safetyVerified = true,
                vulnerabilitiesIdentified = listOf(
                    "SSH password auth allowed (Recommendation: Enforce Key-based auth only)",
                    "HTTP to HTTPS redirect does not set HSTS max-age >= 31536000",
                    "Safe benchmark compliant: No exploitable RCE or destructive ports open"
                )
            )
        } else {
            SafeScanResult(
                target = target,
                timestamp = timeFormat.format(Date()),
                openPortsDetected = emptyList(),
                bannersGrabbed = emptyList(),
                safetyVerified = false,
                vulnerabilitiesIdentified = listOf(
                    "SAFETY POLICY ENFORCEMENT: Target '$target' is NOT on the explicitly authorized lab allowlist.",
                    "CyberShield security engine prohibits scanning unauthorized external hosts."
                )
            )
        }

        _latestScanResult.value = result
        _isScanning.value = false
        logAction("SAFE_SCAN_EXECUTED", "Target: $target, SafeMode: ACTIVE, Authorized: $isTargetAuthorized")
        return result
    }

    // Reports generator
    fun generateReportContent(type: String): String {
        logAction("REPORT_GENERATED", "Type: $type")
        val dateStr = timeFormat.format(Date())
        return when (type) {
            "Executive" -> """
# CYBERSHIELD EXECUTIVE SECURITY POSTURE REPORT
Generated: $dateStr
Classification: TLP:AMBER | Confidential

## 1. Executive Summary
The organization's overall cybersecurity posture index is evaluated at 84/100 (Grade: B+). Threat containment metrics indicate positive defensive latency, with mean-time-to-detect (MTTD) at 12 minutes and mean-time-to-contain (MTTC) at 38 minutes.

## 2. Key Metrics Snapshot
- Total Monitored Assets: ${_assets.value.size}
- Critical Vulnerabilities Pending: ${_vulnerabilities.value.count { it.severity == Severity.CRITICAL && it.remediationStatus == RemediationStatus.OPEN }}
- Active Security Incidents: ${_incidents.value.count { it.status != IncidentStatus.RESOLVED && it.status != IncidentStatus.CLOSED }}
- Threat Intelligence Indicators Active: ${_threatIndicators.value.size}
- Compliance Index (NIST CSF 2.0): 88%

## 3. Top Risk Vectors
1. Exposed SSH on legacy staging jumpbox (AST-003) - Remediation in progress.
2. S3 Storage bucket wildcard permission in staging tenant - Mitigated.
3. Elevated phishing campaign targeting finance department (Campaign: FIN7 Lazarus crossover).

## 4. Strategic Recommendations
- Implement mandatory FIDO2 hardware MFA for all administrative roles.
- Complete patch cycle for CVE-2024-3094 on affected edge gateways within 48h SLA.
- Conduct quarterly tabletop simulation focusing on identity compromise containment.
            """.trimIndent()

            "Vulnerability" -> """
# CYBERSHIELD VULNERABILITY MANAGEMENT AUDIT REPORT
Generated: $dateStr
SLA Scope: Active Infrastructure

## 1. Vulnerability Distribution
- Critical: ${_vulnerabilities.value.count { it.severity == Severity.CRITICAL }}
- High: ${_vulnerabilities.value.count { it.severity == Severity.HIGH }}
- Medium: ${_vulnerabilities.value.count { it.severity == Severity.MEDIUM }}
- Low: ${_vulnerabilities.value.count { it.severity == Severity.LOW }}

## 2. Detailed Findings Register
${_vulnerabilities.value.joinToString("\n\n") { v ->
"""### [${v.severity.label.uppercase()}] ${v.cve}: ${v.title}
- CVSS v3.1: ${v.cvssScore}
- Affected Asset: ${v.affectedAsset}
- Status: ${v.remediationStatus.label} | Owner: ${v.remediationOwner}
- Discovery: ${v.discoveryDate}
- Recommendation: ${v.mitigationRecommendation}"""
}}
            """.trimIndent()

            "Incident" -> """
# CYBERSHIELD INCIDENT INVESTIGATION LOG
Generated: $dateStr
Scope: Major & Minor Incidents

## 1. Active Incident Register
${_incidents.value.joinToString("\n\n") { inc ->
"""### ${inc.id}: ${inc.title}
- Severity: ${inc.severity.label} | Status: ${inc.status.label}
- Affected Asset: ${inc.affectedAsset}
- Assigned Analyst: ${inc.assignedAnalyst}
- Created: ${inc.createdAt}
- Summary: ${inc.description}
- Evidence Records: ${inc.evidenceList.size} items
- Timeline Milestones: ${inc.timeline.size} events logged"""
}}
            """.trimIndent()

            "Compliance" -> """
# CYBERSHIELD COMPLIANCE & CONTROL BENCHMARK
Generated: $dateStr
Frameworks: NIST CSF 2.0, CIS Controls v8, PCI-DSS 4.0, SOC 2 Type II

## 1. Framework Scores
${_complianceStandards.value.joinToString("\n") { std ->
"- ${std.name}: ${std.scorePercentage}% (${std.passedControls}/${std.totalControls} Controls Verified)"
}}

## 2. Audit Trail
All operational actions are captured in the tamper-evident audit repository. Current audit log entries: ${_auditLogs.value.size}.
            """.trimIndent()

            else -> "CyberShield Report: $type"
        }
    }

    private fun seedInitialSocData() {
        // Assets
        val initAssets = listOf(
            Asset(
                id = "AST-001",
                name = "Prod-Web-Gateway-01",
                type = AssetType.SERVER,
                ipOrHost = "10.0.1.15",
                osOrPlatform = "Ubuntu 22.04 LTS",
                owner = "DevOps Infrastructure",
                openPorts = listOf(80, 443),
                healthStatus = HealthStatus.HEALTHY,
                vulnerabilityCount = 1,
                lastScanned = "10m ago",
                isExposedToInternet = true,
                patchStatus = "Current"
            ),
            Asset(
                id = "AST-002",
                name = "Core-Postgres-Primary",
                type = AssetType.SERVER,
                ipOrHost = "10.0.3.50",
                osOrPlatform = "Debian 12 Bookworm",
                owner = "Database Engineering",
                openPorts = listOf(5432),
                healthStatus = HealthStatus.HEALTHY,
                vulnerabilityCount = 0,
                lastScanned = "1h ago",
                isExposedToInternet = false,
                patchStatus = "Current"
            ),
            Asset(
                id = "AST-003",
                name = "Staging-Jumpbox-Legacy",
                type = AssetType.SERVER,
                ipOrHost = "192.168.4.12",
                osOrPlatform = "CentOS 7 (EOL)",
                owner = "QA Team",
                openPorts = listOf(22, 8080),
                healthStatus = HealthStatus.CRITICAL,
                vulnerabilityCount = 4,
                lastScanned = "2h ago",
                isExposedToInternet = true,
                patchStatus = "Pending Security Reboot"
            ),
            Asset(
                id = "AST-004",
                name = "api.enterprise.secure",
                type = AssetType.DOMAIN,
                ipOrHost = "104.18.22.45 (Cloudflare)",
                osOrPlatform = "Managed Edge DNS",
                owner = "Platform Security",
                openPorts = listOf(443),
                healthStatus = HealthStatus.HEALTHY,
                vulnerabilityCount = 0,
                lastScanned = "15m ago",
                isExposedToInternet = true,
                patchStatus = "Managed"
            ),
            Asset(
                id = "AST-005",
                name = "Executive-MacBook-Pro-07",
                type = AssetType.DEVICE,
                ipOrHost = "10.0.8.102",
                osOrPlatform = "macOS Sonoma 14.4",
                owner = "CFO Office",
                openPorts = listOf(),
                healthStatus = HealthStatus.WARNING,
                vulnerabilityCount = 2,
                lastScanned = "30m ago",
                isExposedToInternet = false,
                patchStatus = "OS Update Required"
            ),
            Asset(
                id = "AST-006",
                name = "Billing-Payments-API",
                type = AssetType.API,
                ipOrHost = "api.internal.billing:8443",
                osOrPlatform = "Go / Envoy Proxy",
                owner = "FinTech Services",
                openPorts = listOf(8443),
                healthStatus = HealthStatus.HEALTHY,
                vulnerabilityCount = 0,
                lastScanned = "5m ago",
                isExposedToInternet = false,
                patchStatus = "Current"
            ),
            Asset(
                id = "AST-007",
                name = "AWS-S3-Customer-Data-Lake",
                type = AssetType.CLOUD_RESOURCE,
                ipOrHost = "arn:aws:s3:::corp-data-lake-prod",
                osOrPlatform = "AWS us-east-1",
                owner = "Data Science",
                openPorts = listOf(),
                healthStatus = HealthStatus.HEALTHY,
                vulnerabilityCount = 0,
                lastScanned = "20m ago",
                isExposedToInternet = false,
                patchStatus = "KMS Encrypted"
            )
        )
        _assets.value = initAssets

        // Vulnerabilities
        val initVulns = listOf(
            Vulnerability(
                cve = "CVE-2024-3094",
                title = "XZ Utils Upstream Supply Chain Backdoor",
                severity = Severity.CRITICAL,
                cvssScore = 10.0,
                affectedAsset = "Staging-Jumpbox-Legacy",
                discoveryDate = "2024-03-29",
                remediationStatus = RemediationStatus.PATCHING,
                remediationOwner = "Alex Vance (SecOps Lead)",
                evidence = "liblzma.so.5.6.0 binary payload fingerprint match; sshd daemon hook confirmed in staging telemetry.",
                description = "Malicious code discovered in upstream tarballs of xz-utils causing unauthorized authentication bypass in OpenSSH.",
                mitigationRecommendation = "Downgrade xz-utils to 5.4.6 stable or reinstall pristine base image from verified repository."
            ),
            Vulnerability(
                cve = "CVE-2023-4863",
                title = "WebP Heap Buffer Overflow (libwebp)",
                severity = Severity.HIGH,
                cvssScore = 8.8,
                affectedAsset = "Executive-MacBook-Pro-07",
                discoveryDate = "2024-02-14",
                remediationStatus = RemediationStatus.OPEN,
                remediationOwner = "Endpoint Management Team",
                evidence = "Chromium framework runtime version 116.0.5845.187 vulnerable to crafted lossless WebP image decode.",
                description = "Heap buffer overflow in libwebp allowing arbitrary remote code execution via malformed WebP graphic files.",
                mitigationRecommendation = "Apply macOS system patch and deploy Chrome 117+ browser update via MDM profile."
            ),
            Vulnerability(
                cve = "CVE-2024-21413",
                title = "Microsoft Outlook Remote Code Execution Flaw",
                severity = Severity.HIGH,
                cvssScore = 8.8,
                affectedAsset = "Executive-MacBook-Pro-07",
                discoveryDate = "2024-02-28",
                remediationStatus = RemediationStatus.OPEN,
                remediationOwner = "Endpoint Management Team",
                evidence = "file:// URI handler leak discovered in email client preview cache.",
                description = "MonikerLink vulnerability bypasses Office Protected View and triggers NTLM credential relay.",
                mitigationRecommendation = "Block outbound SMB (port 445) at gateway firewall and enforce Office 365 patch 2402."
            ),
            Vulnerability(
                cve = "CVE-2023-38606",
                title = "Kernel Memory Corruption via GFX Driver",
                severity = Severity.MEDIUM,
                cvssScore = 6.2,
                affectedAsset = "Prod-Web-Gateway-01",
                discoveryDate = "2024-01-10",
                remediationStatus = RemediationStatus.RESOLVED,
                remediationOwner = "Infrastructure Sec Team",
                evidence = "Vulnerable kernel module blacklisted via modprobe configuration.",
                description = "Race condition in graphics acceleration memory allocator allows local privilege escalation.",
                mitigationRecommendation = "Schedule maintenance window for Ubuntu 5.15.0-101 kernel reboot."
            ),
            Vulnerability(
                cve = "CVE-2024-21626",
                title = "runc Container Breakout via Leaked File Descriptor",
                severity = Severity.CRITICAL,
                cvssScore = 8.6,
                affectedAsset = "Staging-Jumpbox-Legacy",
                discoveryDate = "2024-02-01",
                remediationStatus = RemediationStatus.PATCHING,
                remediationOwner = "CloudSec Team",
                evidence = "runc version 1.1.11 detected in Docker daemon inspect output.",
                description = "Internal file descriptors leaked during container init process allow escaping container root to host filesystem.",
                mitigationRecommendation = "Upgrade containerd and runc to 1.1.12+ across all orchestration nodes."
            )
        )
        _vulnerabilities.value = initVulns

        // Incidents
        val initIncidents = listOf(
            Incident(
                id = "INC-1001",
                title = "Brute-Force SSH Infiltration & Lateral Movement Attempt",
                severity = Severity.CRITICAL,
                status = IncidentStatus.INVESTIGATING,
                assignedAnalyst = "Sarah Connor (Tier 2 IR)",
                affectedAsset = "Staging-Jumpbox-Legacy",
                createdAt = "2024-03-30 02:14:10",
                description = "Surge of 4,200 failed SSH logins followed by successful login from unauthorized external IP 185.220.101.5. Malicious script dropped in /tmp/cron.d.",
                timeline = mutableListOf(
                    TimelineEvent("02:14:10", "Detection", "SIEM Alert #ALT-881 fired: SSH Brute Force anomaly.", "SIEM Bot"),
                    TimelineEvent("02:17:40", "Triage", "Analyst confirmed anomalous login from TOR exit node.", "Sarah Connor"),
                    TimelineEvent("02:22:15", "Containment", "Revoked session, isolated jumpbox subnet at border firewall.", "Sarah Connor"),
                    TimelineEvent("02:35:00", "Eradication", "Purged backdoor cron file /tmp/xmr.sh and captured forensic memory image.", "Sarah Connor")
                ),
                evidenceList = mutableListOf(
                    "PCAP: 185.220.101.5 -> 192.168.4.12:22 (4,210 packets)",
                    "Auth Log: 'Failed password for root from 185.220.101.5 port 44321'",
                    "File Hash: sha256 e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855 (/tmp/xmr.sh)"
                ),
                resolutionNotes = "Active quarantine established. Investigating potential credential leak from test environment."
            ),
            Incident(
                id = "INC-1002",
                title = "Suspicious Outbound C2 DNS Beaconing",
                severity = Severity.HIGH,
                status = IncidentStatus.CONTAINED,
                assignedAnalyst = "Marcus Wright (Tier 1 IR)",
                affectedAsset = "Executive-MacBook-Pro-07",
                createdAt = "2024-03-29 14:22:00",
                description = "EDR sensor flagged periodic DNS TXT queries (interval: 45s, jitter 5%) matching known Cobalt Strike beacon profile to domain 'sync-update-cloud.org'.",
                timeline = mutableListOf(
                    TimelineEvent("14:22:00", "Detection", "Zeek IDS flagged encoded DNS query length > 120 bytes.", "Zeek IDS"),
                    TimelineEvent("14:30:12", "Triage", "Domain classified as newly registered (3 days old) with bad reputation.", "Marcus Wright"),
                    TimelineEvent("14:38:00", "Containment", "DNS sinkhole applied globally; host network interface isolated.", "Marcus Wright")
                ),
                evidenceList = mutableListOf(
                    "DNS Query log: '4a6b2c.sync-update-cloud.org TXT record request'",
                    "Endpoint Process tree: 'launchd -> curl -s -k -> sh'"
                ),
                resolutionNotes = "Domain sinkholed to 127.0.0.1. User device scheduled for forensic triage."
            )
        )
        _incidents.value = initIncidents

        // Alerts
        val initAlerts = listOf(
            SecurityAlert(
                id = "ALT-881",
                title = "SSH Brute-Force Spike Detected",
                severity = Severity.CRITICAL,
                source = "SIEM Correlation Rule",
                timestamp = "8m ago",
                description = "4,200 failed auth attempts within 180 seconds against port 22.",
                targetAsset = "Staging-Jumpbox-Legacy",
                isHandled = true
            ),
            SecurityAlert(
                id = "ALT-882",
                title = "Impossible Travel Authentication Flag",
                severity = Severity.HIGH,
                source = "Okta Identity Engine",
                timestamp = "22m ago",
                description = "User admin@corp.org authenticated from New York, then 14 minutes later from Frankfurt.",
                targetAsset = "Identity: admin@corp.org",
                isHandled = false
            ),
            SecurityAlert(
                id = "ALT-883",
                title = "Unencrypted S3 Bucket Policy Modification",
                severity = Severity.MEDIUM,
                source = "AWS CloudTrail",
                timestamp = "45m ago",
                description = "PutBucketPolicy API invoked with Principal: '*', Resource: arn:aws:s3:::corp-data-lake-prod.",
                targetAsset = "AWS-S3-Customer-Data-Lake",
                isHandled = false
            ),
            SecurityAlert(
                id = "ALT-884",
                title = "Outbound Port 4444 (Metasploit Default) Blocked",
                severity = Severity.HIGH,
                source = "Edge Firewall",
                timestamp = "1h ago",
                description = "Stateful firewall dropped TCP SYN connection attempt to external IP 91.215.85.12:4444.",
                targetAsset = "Executive-MacBook-Pro-07",
                isHandled = false
            ),
            SecurityAlert(
                id = "ALT-885",
                title = "Certificate Expiration Impending (< 7 Days)",
                severity = Severity.LOW,
                source = "TLS Monitor",
                timestamp = "3h ago",
                description = "X.509 Certificate for api.enterprise.secure expires in 6 days.",
                targetAsset = "api.enterprise.secure",
                isHandled = false
            )
        )
        _alerts.value = initAlerts

        // Threat Indicators (IoCs)
        val initIocs = listOf(
            ThreatIndicator("IOC-01", IocType.IP, "185.220.101.5", "TOR Exit / Brute-Force Scanner", 98, "12m ago", 42),
            ThreatIndicator("IOC-02", IocType.DOMAIN, "sync-update-cloud.org", "Cobalt Strike C2 Beacon", 95, "35m ago", 18),
            ThreatIndicator("IOC-03", IocType.HASH_SHA256, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", "XMRig Cryptominer Dropper", 99, "2h ago", 6),
            ThreatIndicator("IOC-04", IocType.IP, "91.215.85.12", "Mirai / DarkIRC Botnet C2", 89, "1d ago", 120),
            ThreatIndicator("IOC-05", IocType.DOMAIN, "secure-login-okta-auth.net", "Adversary-in-the-Middle (AiTM) Phish", 94, "4h ago", 3)
        )
        _threatIndicators.value = initIocs

        // Threat Campaigns
        val initCampaigns = listOf(
            ThreatCampaign(
                name = "Operation Midnight Blizzard (APT29)",
                actor = "Nobelium / Cozy Bear",
                targetSectors = listOf("Technology", "Government", "Defense"),
                tactics = listOf("Password Spray", "OAuth App Abuse", "Token Theft"),
                activeIndicatorsCount = 84,
                summary = "Persistent nation-state campaign leveraging compromised non-production test tenant accounts to access email repositories."
            ),
            ThreatCampaign(
                name = "Volt Typhoon Critical Infrastructure Pre-positioning",
                actor = "Volt Typhoon (BRONZE SILHOUETTE)",
                targetSectors = listOf("Utilities", "Telecommunications", "Transport"),
                tactics = listOf("Living off the Land (LotL)", "Router Firmware Exploits", "NTLM Relay"),
                activeIndicatorsCount = 42,
                summary = "Stealthy adversary focusing on evasion using built-in administrative tools (wmic, certutil) and SOHO proxy networks."
            ),
            ThreatCampaign(
                name = "LockBit 3.0 Ransomware Operations",
                actor = "LockBit Gang",
                targetSectors = listOf("Healthcare", "Financial Services", "Manufacturing"),
                tactics = listOf("Double Extortion", "VSS Deletion", "PsExec Spread"),
                activeIndicatorsCount = 112,
                summary = "Ransomware affiliate network utilizing automated payloads and exfiltration portals before encrypting local file shares."
            )
        )
        _threatCampaigns.value = initCampaigns

        // Threat Reports
        val initReports = listOf(
            ThreatReport(
                id = "TR-2024-001",
                title = "Analysis of Novel AiTM Phishing Frameworks Bypassing FIDO2",
                date = "2024-03-25",
                source = "CISA / FBI Cyber Division",
                tlp = "TLP:CLEAR",
                executiveSummary = "Reverse-proxy phishing kits (Evilginx3) increasingly capture session cookies in real time. Organizations must enforce device-bound conditional access."
            ),
            ThreatReport(
                id = "TR-2024-002",
                title = "Supply Chain Infiltration Tactics: The XZ Backdoor Case Study",
                date = "2024-03-31",
                source = "CyberShield Threat Labs",
                tlp = "TLP:AMBER",
                executiveSummary = "Detailed forensic dissection of multi-year social engineering campaign targeting open-source project maintainers to inject obfuscated payload into build stages."
            )
        )
        _threatReports.value = initReports

        // Network Topology
        val initNodes = listOf(
            NetworkNode("NODE-EXT", "Internet Gateway", NetworkZone.INTERNET, "203.0.113.1", HealthStatus.HEALTHY, 342, 1420),
            NetworkNode("NODE-FW", "NextGen Firewall (Fortinet)", NetworkZone.DMZ, "10.0.0.1", HealthStatus.HEALTHY, 280, 1140),
            NetworkNode("NODE-WEB", "DMZ Web Cluster", NetworkZone.DMZ, "10.0.1.15", HealthStatus.HEALTHY, 185, 45),
            NetworkNode("NODE-APP", "Application Tier", NetworkZone.INTERNAL_LAN, "10.0.2.10", HealthStatus.HEALTHY, 92, 4),
            NetworkNode("NODE-DB", "Encrypted Postgres DB", NetworkZone.DATABASE_CLUSTER, "10.0.3.50", HealthStatus.HEALTHY, 44, 0),
            NetworkNode("NODE-STG", "Staging Subnet Jumpbox", NetworkZone.INTERNAL_LAN, "192.168.4.12", HealthStatus.CRITICAL, 18, 380),
            NetworkNode("NODE-CLOUD", "AWS Cloud VPC", NetworkZone.CLOUD_VPC, "172.16.0.0/16", HealthStatus.HEALTHY, 65, 12)
        )
        _networkNodes.value = initNodes

        val initLinks = listOf(
            NetworkLink("NODE-EXT", "NODE-FW", "TCP/443 (HTTPS)", true),
            NetworkLink("NODE-FW", "NODE-WEB", "TCP/443 (TLS)", true),
            NetworkLink("NODE-WEB", "NODE-APP", "gRPC (mTLS)", true),
            NetworkLink("NODE-APP", "NODE-DB", "Postgres TLS", true),
            NetworkLink("NODE-FW", "NODE-STG", "SSH/22", true, isSuspicious = true),
            NetworkLink("NODE-APP", "NODE-CLOUD", "IPSec Tunnel", true)
        )
        _networkLinks.value = initLinks

        // Identity & Accounts
        val initIdentities = listOf(
            IdentityAccount("USR-01", "saket.admin", "saket@cybershield.org", "Security Admin", true, "10m ago", null, 12),
            IdentityAccount("USR-02", "sarah.connor", "sarah.connor@cybershield.org", "SOC Analyst Tier 2", true, "2h ago", null, 18),
            IdentityAccount("USR-03", "finance.exec", "cfo@cybershield.org", "Executive", true, "35m ago", "Anomalous Login: Outside Business Hours", 64),
            IdentityAccount("USR-04", "devops.builder", "builder-ci@cybershield.org", "Service Account", false, "5m ago", "Missing MFA: Service Token Never Rotated", 82),
            IdentityAccount("USR-05", "auditor.compliance", "auditor@external.firm", "Auditor", true, "1d ago", null, 10)
        )
        _identities.value = initIdentities

        // Cloud Security Findings
        val initCloudFindings = listOf(
            CloudSecurityFinding(
                id = "CSF-01",
                cloudProvider = "AWS",
                resourceName = "corp-data-lake-prod",
                finding = "S3 Bucket allows cross-account read without explicit IAM condition",
                severity = Severity.HIGH,
                complianceStandard = "CIS AWS Benchmark 2.1.1",
                remediationStep = "Attach bucket policy enforcing 'aws:PrincipalOrgID' condition"
            ),
            CloudSecurityFinding(
                id = "CSF-02",
                cloudProvider = "GCP",
                resourceName = "k8s-cluster-autopilot",
                finding = "Workload Identity enabled with legacy metadata endpoint exposed",
                severity = Severity.MEDIUM,
                complianceStandard = "NIST SP 800-190",
                remediationStep = "Set metadata extraction prevention to version 2 only"
            ),
            CloudSecurityFinding(
                id = "CSF-03",
                cloudProvider = "AWS",
                resourceName = "security-group-sg-09af82",
                finding = "Security Group permits inbound TCP 22 from 0.0.0.0/0",
                severity = Severity.CRITICAL,
                complianceStandard = "PCI-DSS 4.0 Req 1.3",
                remediationStep = "Restrict SSH ingress strictly to Corporate VPN CIDR"
            )
        )
        _cloudFindings.value = initCloudFindings

        // API Security
        val initApis = listOf(
            ApiEndpointRecord("API-01", "/v1/auth/token", "POST", false, true, "124,500 req/day", Severity.LOW),
            ApiEndpointRecord("API-02", "/v1/payments/charge", "POST", true, true, "45,200 req/day", Severity.LOW),
            ApiEndpointRecord("API-03", "/debug/pprof/heap", "GET", false, false, "1,200 req/day", Severity.CRITICAL),
            ApiEndpointRecord("API-04", "/v1/users/{id}/profile", "GET", true, true, "280,000 req/day", Severity.LOW),
            ApiEndpointRecord("API-05", "/internal/metrics", "GET", false, false, "18,400 req/day", Severity.HIGH)
        )
        _apiEndpoints.value = initApis

        // Compliance Standards
        val initStandards = listOf(
            ComplianceStandard(
                name = "NIST CSF 2.0 (National Institute of Standards)",
                scorePercentage = 88,
                totalControls = 24,
                passedControls = 21,
                controls = listOf(
                    ComplianceControl("ID.AM-1", "Physical & Virtual Assets Inventoried", "NIST CSF", true, "Automated asset tracking synced every 15 minutes."),
                    ComplianceControl("PR.AC-1", "Identities and Credentials Managed (MFA)", "NIST CSF", true, "FIDO2 / TOTP enforced on 92% of staff."),
                    ComplianceControl("DE.CM-1", "Network Monitored for Anomalous Behavior", "NIST CSF", true, "Zeek IDS and Suricata inspecting border traffic."),
                    ComplianceControl("RS.RP-1", "Incident Response Plan Tested Annually", "NIST CSF", false, "Tabletop simulation scheduled for Q4.")
                )
            ),
            ComplianceStandard(
                name = "CIS Controls v8 (Center for Internet Security)",
                scorePercentage = 84,
                totalControls = 18,
                passedControls = 15,
                controls = listOf(
                    ComplianceControl("CIS-01", "Inventory and Control of Enterprise Assets", "CIS v8", true, "Active Discovery and passive DHCP snooping deployed."),
                    ComplianceControl("CIS-04", "Secure Configuration of Enterprise Assets", "CIS v8", false, "Jumpbox legacy baseline deviates from hardened image."),
                    ComplianceControl("CIS-07", "Continuous Vulnerability Management", "CIS v8", true, "Automated weekly safe vulnerability scans active.")
                )
            ),
            ComplianceStandard(
                name = "PCI-DSS 4.0 (Payment Card Security)",
                scorePercentage = 91,
                totalControls = 12,
                passedControls = 11,
                controls = listOf(
                    ComplianceControl("PCI-1.2", "Firewall and Router Configurations Hardened", "PCI-DSS", true, "Default denies on all inter-VLAN routing."),
                    ComplianceControl("PCI-3.4", "Cardholder Data Encrypted in Transit", "PCI-DSS", true, "TLS 1.3 enforced on payment APIs.")
                )
            ),
            ComplianceStandard(
                name = "SOC 2 Type II (Trust Services Criteria)",
                scorePercentage = 93,
                totalControls = 16,
                passedControls = 15,
                controls = listOf(
                    ComplianceControl("CC6.1", "Logical Access Security Controls", "SOC 2", true, "RBAC enforced with quarterly access review."),
                    ComplianceControl("CC7.2", "Vulnerability and Anomaly Detection", "SOC 2", true, "SIEM correlation alerts operating 24/7.")
                )
            )
        )
        _complianceStandards.value = initStandards

        // Seed initial audit log
        logAction("SYSTEM_BOOT", "CyberShield SOC Command Center initialized. Data feeds active.")
        logAction("INTEGRITY_CHECK", "Vulnerability DB and SIEM feed signatures verified successfully.")
    }
}

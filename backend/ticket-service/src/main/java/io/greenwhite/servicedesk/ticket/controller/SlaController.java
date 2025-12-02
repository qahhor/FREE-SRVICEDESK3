package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.common.dto.ApiResponse;
import io.greenwhite.servicedesk.ticket.dto.*;
import io.greenwhite.servicedesk.ticket.service.SlaMonitorService;
import io.greenwhite.servicedesk.ticket.service.SlaPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for SLA management endpoints
 */
@RestController
@RequestMapping("/sla")
@RequiredArgsConstructor
@Slf4j
public class SlaController {

    private final SlaPolicyService policyService;
    private final SlaMonitorService monitorService;

    // ===================== SLA Policies =====================

    @GetMapping("/policies")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<SlaPolicyResponse>>> getAllPolicies() {
        log.info("GET /sla/policies - Fetching all SLA policies");
        List<SlaPolicyResponse> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @GetMapping("/policies/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<SlaPolicyResponse>>> getActivePolicies() {
        log.info("GET /sla/policies/active - Fetching active SLA policies");
        List<SlaPolicyResponse> policies = policyService.getActivePolicies();
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @PostMapping("/policies")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SlaPolicyResponse>> createPolicy(
            @Valid @RequestBody SlaPolicyRequest request) {
        log.info("POST /sla/policies - Creating SLA policy: {}", request.getName());
        SlaPolicyResponse policy = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SLA policy created successfully", policy));
    }

    @GetMapping("/policies/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<SlaPolicyResponse>> getPolicy(@PathVariable UUID id) {
        log.info("GET /sla/policies/{} - Fetching SLA policy", id);
        SlaPolicyResponse policy = policyService.getPolicy(id);
        return ResponseEntity.ok(ApiResponse.success(policy));
    }

    @PutMapping("/policies/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SlaPolicyResponse>> updatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody SlaPolicyRequest request) {
        log.info("PUT /sla/policies/{} - Updating SLA policy", id);
        SlaPolicyResponse policy = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("SLA policy updated successfully", policy));
    }

    @DeleteMapping("/policies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable UUID id) {
        log.info("DELETE /sla/policies/{} - Deleting SLA policy", id);
        policyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("SLA policy deleted successfully", null));
    }

    // ===================== Business Hours =====================

    @GetMapping("/business-hours")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<SlaBusinessHoursResponse>>> getAllBusinessHours() {
        log.info("GET /sla/business-hours - Fetching all business hours");
        List<SlaBusinessHoursResponse> businessHours = policyService.getAllBusinessHours();
        return ResponseEntity.ok(ApiResponse.success(businessHours));
    }

    @PostMapping("/business-hours")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SlaBusinessHoursResponse>> createBusinessHours(
            @Valid @RequestBody SlaBusinessHoursRequest request) {
        log.info("POST /sla/business-hours - Creating business hours: {}", request.getName());
        SlaBusinessHoursResponse businessHours = policyService.createBusinessHours(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business hours created successfully", businessHours));
    }

    @GetMapping("/business-hours/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<SlaBusinessHoursResponse>> getBusinessHours(@PathVariable UUID id) {
        log.info("GET /sla/business-hours/{} - Fetching business hours", id);
        SlaBusinessHoursResponse businessHours = policyService.getBusinessHours(id);
        return ResponseEntity.ok(ApiResponse.success(businessHours));
    }

    @PutMapping("/business-hours/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SlaBusinessHoursResponse>> updateBusinessHours(
            @PathVariable UUID id,
            @Valid @RequestBody SlaBusinessHoursRequest request) {
        log.info("PUT /sla/business-hours/{} - Updating business hours", id);
        SlaBusinessHoursResponse businessHours = policyService.updateBusinessHours(id, request);
        return ResponseEntity.ok(ApiResponse.success("Business hours updated successfully", businessHours));
    }

    // ===================== Holidays =====================

    @GetMapping("/holidays")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<SlaBusinessHoursResponse.SlaHolidayResponse>>> getAllHolidays() {
        log.info("GET /sla/holidays - Fetching all holidays");
        List<SlaBusinessHoursResponse.SlaHolidayResponse> holidays = policyService.getAllHolidays();
        return ResponseEntity.ok(ApiResponse.success(holidays));
    }

    @GetMapping("/holidays/business-hours/{businessHoursId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<SlaBusinessHoursResponse.SlaHolidayResponse>>> getHolidaysByBusinessHours(
            @PathVariable UUID businessHoursId) {
        log.info("GET /sla/holidays/business-hours/{} - Fetching holidays for business hours", businessHoursId);
        List<SlaBusinessHoursResponse.SlaHolidayResponse> holidays = policyService.getHolidaysByBusinessHours(businessHoursId);
        return ResponseEntity.ok(ApiResponse.success(holidays));
    }

    @PostMapping("/holidays")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SlaBusinessHoursResponse.SlaHolidayResponse>> createHoliday(
            @Valid @RequestBody SlaHolidayRequest request) {
        log.info("POST /sla/holidays - Creating holiday: {} on {}", request.getName(), request.getDate());
        SlaBusinessHoursResponse.SlaHolidayResponse holiday = policyService.createHoliday(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Holiday created successfully", holiday));
    }

    @DeleteMapping("/holidays/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable UUID id) {
        log.info("DELETE /sla/holidays/{} - Deleting holiday", id);
        policyService.deleteHoliday(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Holiday deleted successfully", null));
    }

    // ===================== Escalations =====================

    @GetMapping("/escalations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<SlaEscalationResponse>>> getAllEscalations() {
        log.info("GET /sla/escalations - Fetching all escalations");
        List<SlaEscalationResponse> escalations = policyService.getAllEscalations();
        return ResponseEntity.ok(ApiResponse.success(escalations));
    }

    @GetMapping("/escalations/policy/{policyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<SlaEscalationResponse>>> getEscalationsByPolicy(
            @PathVariable UUID policyId) {
        log.info("GET /sla/escalations/policy/{} - Fetching escalations for policy", policyId);
        List<SlaEscalationResponse> escalations = policyService.getEscalationsByPolicy(policyId);
        return ResponseEntity.ok(ApiResponse.success(escalations));
    }

    @PostMapping("/escalations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SlaEscalationResponse>> createEscalation(
            @Valid @RequestBody SlaEscalationRequest request) {
        log.info("POST /sla/escalations - Creating escalation: {}", request.getName());
        SlaEscalationResponse escalation = policyService.createEscalation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Escalation created successfully", escalation));
    }

    @GetMapping("/escalations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<SlaEscalationResponse>> getEscalation(@PathVariable UUID id) {
        log.info("GET /sla/escalations/{} - Fetching escalation", id);
        SlaEscalationResponse escalation = policyService.getEscalation(id);
        return ResponseEntity.ok(ApiResponse.success(escalation));
    }

    @PutMapping("/escalations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SlaEscalationResponse>> updateEscalation(
            @PathVariable UUID id,
            @Valid @RequestBody SlaEscalationRequest request) {
        log.info("PUT /sla/escalations/{} - Updating escalation", id);
        SlaEscalationResponse escalation = policyService.updateEscalation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Escalation updated successfully", escalation));
    }

    @DeleteMapping("/escalations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteEscalation(@PathVariable UUID id) {
        log.info("DELETE /sla/escalations/{} - Deleting escalation", id);
        policyService.deleteEscalation(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Escalation deleted successfully", null));
    }

    // ===================== SLA Metrics & Reports =====================

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<SlaMetricsResponse>> getSlaMetrics() {
        log.info("GET /sla/metrics - Fetching SLA metrics dashboard");
        SlaMetricsResponse metrics = monitorService.getSlaMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/reports/compliance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SlaMetricsResponse>> getSlaComplianceReport() {
        log.info("GET /sla/reports/compliance - Generating SLA compliance report");
        SlaMetricsResponse metrics = monitorService.getSlaMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/reports/breaches")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Long>> getSlaBreachCount() {
        log.info("GET /sla/reports/breaches - Getting breach count");
        long breachCount = monitorService.getBreachedTickets().size();
        return ResponseEntity.ok(ApiResponse.success(breachCount));
    }
}

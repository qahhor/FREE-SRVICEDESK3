package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.exception.ResourceNotFoundException;
import io.greenwhite.servicedesk.ticket.dto.*;
import io.greenwhite.servicedesk.ticket.model.*;
import io.greenwhite.servicedesk.ticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing SLA policies and related entities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlaPolicyService {

    private final SlaPolicyRepository policyRepository;
    private final SlaPriorityRepository priorityRepository;
    private final SlaBusinessHoursRepository businessHoursRepository;
    private final SlaHolidayRepository holidayRepository;
    private final SlaEscalationRepository escalationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // ===================== SLA Policy CRUD =====================

    @Transactional(readOnly = true)
    public List<SlaPolicyResponse> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SlaPolicyResponse> getActivePolicies() {
        return policyRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SlaPolicyResponse getPolicy(UUID id) {
        SlaPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Policy not found with id: " + id));
        return mapToResponse(policy);
    }

    @Transactional
    public SlaPolicyResponse createPolicy(SlaPolicyRequest request) {
        SlaPolicy policy = SlaPolicy.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        // Set business hours if provided
        if (request.getBusinessHoursId() != null) {
            SlaBusinessHours businessHours = businessHoursRepository.findById(request.getBusinessHoursId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Business Hours not found with id: " + request.getBusinessHoursId()));
            policy.setBusinessHours(businessHours);
        }

        // Handle default policy uniqueness
        if (policy.getIsDefault()) {
            policyRepository.findByIsDefaultTrue().ifPresent(existingDefault -> {
                existingDefault.setIsDefault(false);
                policyRepository.save(existingDefault);
            });
        }

        // Set projects if provided
        if (request.getProjectIds() != null && !request.getProjectIds().isEmpty()) {
            List<Project> projects = request.getProjectIds().stream()
                    .map(projectId -> projectRepository.findById(projectId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Project not found with id: " + projectId)))
                    .collect(Collectors.toList());
            policy.setProjects(projects);
        }

        policy = policyRepository.save(policy);

        // Create priorities
        if (request.getPriorities() != null) {
            for (SlaPolicyRequest.SlaPriorityRequest priorityRequest : request.getPriorities()) {
                SlaPriority priority = SlaPriority.builder()
                        .policy(policy)
                        .priority(priorityRequest.getPriority())
                        .firstResponseMinutes(priorityRequest.getFirstResponseMinutes())
                        .resolutionMinutes(priorityRequest.getResolutionMinutes())
                        .nextResponseMinutes(priorityRequest.getNextResponseMinutes())
                        .firstResponseEnabled(priorityRequest.getFirstResponseEnabled() != null ?
                                priorityRequest.getFirstResponseEnabled() : true)
                        .resolutionEnabled(priorityRequest.getResolutionEnabled() != null ?
                                priorityRequest.getResolutionEnabled() : true)
                        .build();
                priorityRepository.save(priority);
            }
        }

        log.info("Created SLA Policy: {}", policy.getName());
        return getPolicy(policy.getId());
    }

    @Transactional
    public SlaPolicyResponse updatePolicy(UUID id, SlaPolicyRequest request) {
        SlaPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Policy not found with id: " + id));

        policy.setName(request.getName());
        policy.setDescription(request.getDescription());

        if (request.getActive() != null) {
            policy.setActive(request.getActive());
        }

        // Handle default policy change
        if (request.getIsDefault() != null && request.getIsDefault() && !policy.getIsDefault()) {
            policyRepository.findByIsDefaultTrue().ifPresent(existingDefault -> {
                if (!existingDefault.getId().equals(id)) {
                    existingDefault.setIsDefault(false);
                    policyRepository.save(existingDefault);
                }
            });
            policy.setIsDefault(true);
        } else if (request.getIsDefault() != null && !request.getIsDefault()) {
            policy.setIsDefault(false);
        }

        // Update business hours
        if (request.getBusinessHoursId() != null) {
            SlaBusinessHours businessHours = businessHoursRepository.findById(request.getBusinessHoursId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Business Hours not found with id: " + request.getBusinessHoursId()));
            policy.setBusinessHours(businessHours);
        }

        // Update projects
        if (request.getProjectIds() != null) {
            List<Project> projects = request.getProjectIds().stream()
                    .map(projectId -> projectRepository.findById(projectId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Project not found with id: " + projectId)))
                    .collect(Collectors.toList());
            policy.setProjects(projects);
        }

        policy = policyRepository.save(policy);

        // Update priorities
        if (request.getPriorities() != null) {
            // Delete existing priorities using bulk delete
            priorityRepository.deleteByPolicyId(id);

            // Create new priorities
            for (SlaPolicyRequest.SlaPriorityRequest priorityRequest : request.getPriorities()) {
                SlaPriority priority = SlaPriority.builder()
                        .policy(policy)
                        .priority(priorityRequest.getPriority())
                        .firstResponseMinutes(priorityRequest.getFirstResponseMinutes())
                        .resolutionMinutes(priorityRequest.getResolutionMinutes())
                        .nextResponseMinutes(priorityRequest.getNextResponseMinutes())
                        .firstResponseEnabled(priorityRequest.getFirstResponseEnabled() != null ?
                                priorityRequest.getFirstResponseEnabled() : true)
                        .resolutionEnabled(priorityRequest.getResolutionEnabled() != null ?
                                priorityRequest.getResolutionEnabled() : true)
                        .build();
                priorityRepository.save(priority);
            }
        }

        log.info("Updated SLA Policy: {}", policy.getName());
        return getPolicy(id);
    }

    @Transactional
    public void deletePolicy(UUID id) {
        SlaPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Policy not found with id: " + id));

        // Delete related priorities
        priorityRepository.deleteByPolicyId(id);

        // Delete related escalations
        escalationRepository.deleteByPolicyId(id);

        policyRepository.delete(policy);
        log.info("Deleted SLA Policy: {}", policy.getName());
    }

    // ===================== Business Hours CRUD =====================

    @Transactional(readOnly = true)
    public List<SlaBusinessHoursResponse> getAllBusinessHours() {
        return businessHoursRepository.findAll().stream()
                .map(this::mapToBusinessHoursResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SlaBusinessHoursResponse getBusinessHours(UUID id) {
        SlaBusinessHours businessHours = businessHoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business Hours not found with id: " + id));
        return mapToBusinessHoursResponse(businessHours);
    }

    @Transactional
    public SlaBusinessHoursResponse createBusinessHours(SlaBusinessHoursRequest request) {
        SlaBusinessHours businessHours = SlaBusinessHours.builder()
                .name(request.getName())
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .schedule(request.getSchedule())
                .build();

        businessHours = businessHoursRepository.save(businessHours);
        log.info("Created Business Hours: {}", businessHours.getName());
        return mapToBusinessHoursResponse(businessHours);
    }

    @Transactional
    public SlaBusinessHoursResponse updateBusinessHours(UUID id, SlaBusinessHoursRequest request) {
        SlaBusinessHours businessHours = businessHoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business Hours not found with id: " + id));

        businessHours.setName(request.getName());
        if (request.getTimezone() != null) {
            businessHours.setTimezone(request.getTimezone());
        }
        businessHours.setSchedule(request.getSchedule());

        businessHours = businessHoursRepository.save(businessHours);
        log.info("Updated Business Hours: {}", businessHours.getName());
        return mapToBusinessHoursResponse(businessHours);
    }

    // ===================== Holiday CRUD =====================

    @Transactional(readOnly = true)
    public List<SlaBusinessHoursResponse.SlaHolidayResponse> getAllHolidays() {
        return holidayRepository.findAll().stream()
                .map(this::mapToHolidayResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SlaBusinessHoursResponse.SlaHolidayResponse> getHolidaysByBusinessHours(UUID businessHoursId) {
        return holidayRepository.findByBusinessHoursId(businessHoursId).stream()
                .map(this::mapToHolidayResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SlaBusinessHoursResponse.SlaHolidayResponse createHoliday(SlaHolidayRequest request) {
        SlaBusinessHours businessHours = businessHoursRepository.findById(request.getBusinessHoursId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Hours not found with id: " + request.getBusinessHoursId()));

        SlaHoliday holiday = SlaHoliday.builder()
                .name(request.getName())
                .date(request.getDate())
                .recurring(request.getRecurring() != null ? request.getRecurring() : false)
                .businessHours(businessHours)
                .build();

        holiday = holidayRepository.save(holiday);
        log.info("Created Holiday: {} on {}", holiday.getName(), holiday.getDate());
        return mapToHolidayResponse(holiday);
    }

    @Transactional
    public void deleteHoliday(UUID id) {
        SlaHoliday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with id: " + id));
        holidayRepository.delete(holiday);
        log.info("Deleted Holiday: {}", holiday.getName());
    }

    // ===================== Escalation CRUD =====================

    @Transactional(readOnly = true)
    public List<SlaEscalationResponse> getAllEscalations() {
        return escalationRepository.findAll().stream()
                .map(this::mapToEscalationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SlaEscalationResponse> getEscalationsByPolicy(UUID policyId) {
        return escalationRepository.findByPolicyId(policyId).stream()
                .map(this::mapToEscalationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SlaEscalationResponse getEscalation(UUID id) {
        SlaEscalation escalation = escalationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation not found with id: " + id));
        return mapToEscalationResponse(escalation);
    }

    @Transactional
    public SlaEscalationResponse createEscalation(SlaEscalationRequest request) {
        SlaPolicy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SLA Policy not found with id: " + request.getPolicyId()));

        SlaEscalation escalation = SlaEscalation.builder()
                .policy(policy)
                .name(request.getName())
                .type(request.getType())
                .triggerMinutesBefore(request.getTriggerMinutesBefore())
                .action(request.getAction())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        // Set notify users
        if (request.getNotifyUserIds() != null && !request.getNotifyUserIds().isEmpty()) {
            escalation.setNotifyUsers(new HashSet<>(request.getNotifyUserIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "User not found with id: " + userId)))
                    .collect(Collectors.toList())));
        }

        // Set reassign to user
        if (request.getReassignToUserId() != null) {
            User reassignTo = userRepository.findById(request.getReassignToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getReassignToUserId()));
            escalation.setReassignTo(reassignTo);
        }

        escalation = escalationRepository.save(escalation);
        log.info("Created Escalation: {} for policy {}", escalation.getName(), policy.getName());
        return mapToEscalationResponse(escalation);
    }

    @Transactional
    public SlaEscalationResponse updateEscalation(UUID id, SlaEscalationRequest request) {
        SlaEscalation escalation = escalationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation not found with id: " + id));

        escalation.setName(request.getName());
        escalation.setType(request.getType());
        escalation.setTriggerMinutesBefore(request.getTriggerMinutesBefore());
        escalation.setAction(request.getAction());

        if (request.getActive() != null) {
            escalation.setActive(request.getActive());
        }

        // Update notify users
        if (request.getNotifyUserIds() != null) {
            escalation.setNotifyUsers(new HashSet<>(request.getNotifyUserIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "User not found with id: " + userId)))
                    .collect(Collectors.toList())));
        }

        // Update reassign to user
        if (request.getReassignToUserId() != null) {
            User reassignTo = userRepository.findById(request.getReassignToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getReassignToUserId()));
            escalation.setReassignTo(reassignTo);
        } else {
            escalation.setReassignTo(null);
        }

        escalation = escalationRepository.save(escalation);
        log.info("Updated Escalation: {}", escalation.getName());
        return mapToEscalationResponse(escalation);
    }

    @Transactional
    public void deleteEscalation(UUID id) {
        SlaEscalation escalation = escalationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation not found with id: " + id));
        escalationRepository.delete(escalation);
        log.info("Deleted Escalation: {}", escalation.getName());
    }

    // ===================== Mapping Methods =====================

    private SlaPolicyResponse mapToResponse(SlaPolicy policy) {
        List<SlaPriority> priorities = priorityRepository.findByPolicyId(policy.getId());

        return SlaPolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .description(policy.getDescription())
                .isDefault(policy.getIsDefault())
                .active(policy.getActive())
                .businessHours(policy.getBusinessHours() != null ?
                        mapToBusinessHoursResponse(policy.getBusinessHours()) : null)
                .priorities(priorities.stream()
                        .map(p -> SlaPolicyResponse.SlaPriorityResponse.builder()
                                .id(p.getId())
                                .priority(p.getPriority())
                                .firstResponseMinutes(p.getFirstResponseMinutes())
                                .resolutionMinutes(p.getResolutionMinutes())
                                .nextResponseMinutes(p.getNextResponseMinutes())
                                .firstResponseEnabled(p.getFirstResponseEnabled())
                                .resolutionEnabled(p.getResolutionEnabled())
                                .build())
                        .collect(Collectors.toList()))
                .projects(policy.getProjects() != null ? policy.getProjects().stream()
                        .map(p -> SlaPolicyResponse.ProjectSummary.builder()
                                .id(p.getId())
                                .key(p.getKey())
                                .name(p.getName())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }

    private SlaBusinessHoursResponse mapToBusinessHoursResponse(SlaBusinessHours businessHours) {
        List<SlaHoliday> holidays = holidayRepository.findByBusinessHoursId(businessHours.getId());

        return SlaBusinessHoursResponse.builder()
                .id(businessHours.getId())
                .name(businessHours.getName())
                .timezone(businessHours.getTimezone())
                .schedule(businessHours.getSchedule())
                .holidays(holidays.stream()
                        .map(this::mapToHolidayResponse)
                        .collect(Collectors.toList()))
                .createdAt(businessHours.getCreatedAt())
                .updatedAt(businessHours.getUpdatedAt())
                .build();
    }

    private SlaBusinessHoursResponse.SlaHolidayResponse mapToHolidayResponse(SlaHoliday holiday) {
        return SlaBusinessHoursResponse.SlaHolidayResponse.builder()
                .id(holiday.getId())
                .name(holiday.getName())
                .date(holiday.getDate())
                .recurring(holiday.getRecurring())
                .build();
    }

    private SlaEscalationResponse mapToEscalationResponse(SlaEscalation escalation) {
        return SlaEscalationResponse.builder()
                .id(escalation.getId())
                .name(escalation.getName())
                .policyId(escalation.getPolicy().getId())
                .type(escalation.getType())
                .triggerMinutesBefore(escalation.getTriggerMinutesBefore())
                .action(escalation.getAction())
                .notifyUsers(escalation.getNotifyUsers() != null ? escalation.getNotifyUsers().stream()
                        .map(u -> SlaEscalationResponse.UserSummary.builder()
                                .id(u.getId())
                                .email(u.getEmail())
                                .fullName(u.getFullName())
                                .build())
                        .collect(Collectors.toSet()) : new HashSet<>())
                .reassignTo(escalation.getReassignTo() != null ?
                        SlaEscalationResponse.UserSummary.builder()
                                .id(escalation.getReassignTo().getId())
                                .email(escalation.getReassignTo().getEmail())
                                .fullName(escalation.getReassignTo().getFullName())
                                .build() : null)
                .active(escalation.getActive())
                .createdAt(escalation.getCreatedAt())
                .updatedAt(escalation.getUpdatedAt())
                .build();
    }
}

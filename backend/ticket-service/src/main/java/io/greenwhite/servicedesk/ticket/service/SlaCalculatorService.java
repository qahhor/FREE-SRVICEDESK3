package io.greenwhite.servicedesk.ticket.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.greenwhite.servicedesk.ticket.model.SlaBusinessHours;
import io.greenwhite.servicedesk.ticket.model.SlaHoliday;
import io.greenwhite.servicedesk.ticket.repository.SlaHolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating SLA times considering business hours and holidays
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlaCalculatorService {

    private final SlaHolidayRepository holidayRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Calculate due date considering business hours and holidays
     *
     * @param startTime     The start time
     * @param targetMinutes Target minutes for SLA
     * @param businessHours Business hours configuration
     * @return The calculated due date
     */
    public LocalDateTime calculateDueDate(
            LocalDateTime startTime,
            int targetMinutes,
            SlaBusinessHours businessHours) {

        if (businessHours == null || businessHours.getSchedule() == null) {
            // No business hours defined, use 24/7
            return startTime.plusMinutes(targetMinutes);
        }

        ZoneId zoneId = ZoneId.of(businessHours.getTimezone() != null ? businessHours.getTimezone() : "UTC");
        ZonedDateTime currentTime = startTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId);

        int remainingMinutes = targetMinutes;
        Map<String, Map<String, String>> schedule = parseSchedule(businessHours.getSchedule());

        while (remainingMinutes > 0) {
            // Check if current day is a holiday
            if (isHoliday(currentTime.toLocalDate(), businessHours)) {
                currentTime = getNextBusinessDay(currentTime, schedule, businessHours);
                continue;
            }

            String dayOfWeek = currentTime.getDayOfWeek().toString().toLowerCase();
            Map<String, String> daySchedule = schedule.get(dayOfWeek);

            if (daySchedule == null || daySchedule.isEmpty()) {
                // No business hours for this day
                currentTime = getNextBusinessDay(currentTime, schedule, businessHours);
                continue;
            }

            LocalTime startOfDay = LocalTime.parse(daySchedule.get("start"), TIME_FORMATTER);
            LocalTime endOfDay = LocalTime.parse(daySchedule.get("end"), TIME_FORMATTER);
            LocalTime currentLocalTime = currentTime.toLocalTime();

            if (currentLocalTime.isBefore(startOfDay)) {
                // Before business hours, move to start of business hours
                currentTime = currentTime.with(startOfDay);
                currentLocalTime = startOfDay;
            }

            if (currentLocalTime.isAfter(endOfDay) || currentLocalTime.equals(endOfDay)) {
                // After business hours, move to next business day
                currentTime = getNextBusinessDay(currentTime, schedule, businessHours);
                continue;
            }

            // Calculate available minutes today
            int availableMinutesToday = (int) Duration.between(currentLocalTime, endOfDay).toMinutes();

            if (remainingMinutes <= availableMinutesToday) {
                // SLA due date is within today's business hours
                currentTime = currentTime.plusMinutes(remainingMinutes);
                remainingMinutes = 0;
            } else {
                // Use all remaining time today and move to next business day
                remainingMinutes -= availableMinutesToday;
                currentTime = getNextBusinessDay(currentTime, schedule, businessHours);
            }
        }

        return currentTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Calculate elapsed business minutes between two times
     *
     * @param startTime     The start time
     * @param endTime       The end time
     * @param businessHours Business hours configuration
     * @return The elapsed business minutes
     */
    public int calculateElapsedMinutes(
            LocalDateTime startTime,
            LocalDateTime endTime,
            SlaBusinessHours businessHours) {

        if (businessHours == null || businessHours.getSchedule() == null) {
            // No business hours defined, use 24/7
            return (int) Duration.between(startTime, endTime).toMinutes();
        }

        ZoneId zoneId = ZoneId.of(businessHours.getTimezone() != null ? businessHours.getTimezone() : "UTC");
        ZonedDateTime current = startTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId);
        ZonedDateTime end = endTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId);

        int totalMinutes = 0;
        Map<String, Map<String, String>> schedule = parseSchedule(businessHours.getSchedule());

        while (current.isBefore(end)) {
            // Check if current day is a holiday
            if (isHoliday(current.toLocalDate(), businessHours)) {
                current = getNextBusinessDay(current, schedule, businessHours);
                continue;
            }

            String dayOfWeek = current.getDayOfWeek().toString().toLowerCase();
            Map<String, String> daySchedule = schedule.get(dayOfWeek);

            if (daySchedule == null || daySchedule.isEmpty()) {
                current = getNextBusinessDay(current, schedule, businessHours);
                continue;
            }

            LocalTime startOfDay = LocalTime.parse(daySchedule.get("start"), TIME_FORMATTER);
            LocalTime endOfDay = LocalTime.parse(daySchedule.get("end"), TIME_FORMATTER);
            LocalTime currentLocalTime = current.toLocalTime();

            if (currentLocalTime.isBefore(startOfDay)) {
                current = current.with(startOfDay);
                currentLocalTime = startOfDay;
            }

            if (currentLocalTime.isAfter(endOfDay) || currentLocalTime.equals(endOfDay)) {
                current = getNextBusinessDay(current, schedule, businessHours);
                continue;
            }

            // Determine the end point for this day
            ZonedDateTime endOfToday = current.with(endOfDay);
            ZonedDateTime effectiveEnd = end.isBefore(endOfToday) ? end : endOfToday;

            if (effectiveEnd.isAfter(current)) {
                totalMinutes += (int) Duration.between(current, effectiveEnd).toMinutes();
            }

            if (end.isBefore(endOfToday) || end.equals(endOfToday)) {
                break;
            }

            current = getNextBusinessDay(current, schedule, businessHours);
        }

        return totalMinutes;
    }

    /**
     * Check if current time is within business hours
     *
     * @param time          The time to check
     * @param businessHours Business hours configuration
     * @return true if within business hours
     */
    public boolean isWithinBusinessHours(LocalDateTime time, SlaBusinessHours businessHours) {
        if (businessHours == null || businessHours.getSchedule() == null) {
            return true; // No business hours defined, always within
        }

        ZoneId zoneId = ZoneId.of(businessHours.getTimezone() != null ? businessHours.getTimezone() : "UTC");
        ZonedDateTime zonedTime = time.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId);

        // Check if it's a holiday
        if (isHoliday(zonedTime.toLocalDate(), businessHours)) {
            return false;
        }

        Map<String, Map<String, String>> schedule = parseSchedule(businessHours.getSchedule());
        String dayOfWeek = zonedTime.getDayOfWeek().toString().toLowerCase();
        Map<String, String> daySchedule = schedule.get(dayOfWeek);

        if (daySchedule == null || daySchedule.isEmpty()) {
            return false;
        }

        LocalTime startOfDay = LocalTime.parse(daySchedule.get("start"), TIME_FORMATTER);
        LocalTime endOfDay = LocalTime.parse(daySchedule.get("end"), TIME_FORMATTER);
        LocalTime currentLocalTime = zonedTime.toLocalTime();

        return !currentLocalTime.isBefore(startOfDay) && currentLocalTime.isBefore(endOfDay);
    }

    /**
     * Get next business hour start
     *
     * @param from          The starting time
     * @param businessHours Business hours configuration
     * @return The next business hour start time
     */
    public LocalDateTime getNextBusinessHourStart(LocalDateTime from, SlaBusinessHours businessHours) {
        if (businessHours == null || businessHours.getSchedule() == null) {
            return from; // No business hours defined
        }

        ZoneId zoneId = ZoneId.of(businessHours.getTimezone() != null ? businessHours.getTimezone() : "UTC");
        ZonedDateTime current = from.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId);
        Map<String, Map<String, String>> schedule = parseSchedule(businessHours.getSchedule());

        // Check if currently within business hours
        if (isWithinBusinessHours(from, businessHours)) {
            return from;
        }

        // Find next business hour start
        for (int i = 0; i < 14; i++) { // Check up to 14 days ahead
            if (isHoliday(current.toLocalDate(), businessHours)) {
                current = current.plusDays(1).with(LocalTime.MIN);
                continue;
            }

            String dayOfWeek = current.getDayOfWeek().toString().toLowerCase();
            Map<String, String> daySchedule = schedule.get(dayOfWeek);

            if (daySchedule != null && !daySchedule.isEmpty()) {
                LocalTime startOfDay = LocalTime.parse(daySchedule.get("start"), TIME_FORMATTER);
                LocalTime currentLocalTime = current.toLocalTime();

                if (currentLocalTime.isBefore(startOfDay)) {
                    return current.with(startOfDay)
                            .withZoneSameInstant(ZoneId.systemDefault())
                            .toLocalDateTime();
                }

                // If we're past today's start, check if we're before end
                LocalTime endOfDay = LocalTime.parse(daySchedule.get("end"), TIME_FORMATTER);
                if (currentLocalTime.isBefore(endOfDay)) {
                    return current.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                }
            }

            // Move to next day
            current = current.plusDays(1).with(LocalTime.MIN);
        }

        // Fallback
        return from;
    }

    private ZonedDateTime getNextBusinessDay(
            ZonedDateTime current,
            Map<String, Map<String, String>> schedule,
            SlaBusinessHours businessHours) {

        ZonedDateTime nextDay = current.plusDays(1).with(LocalTime.MIN);

        for (int i = 0; i < 14; i++) { // Check up to 14 days ahead
            if (isHoliday(nextDay.toLocalDate(), businessHours)) {
                nextDay = nextDay.plusDays(1);
                continue;
            }

            String dayOfWeek = nextDay.getDayOfWeek().toString().toLowerCase();
            Map<String, String> daySchedule = schedule.get(dayOfWeek);

            if (daySchedule != null && !daySchedule.isEmpty()) {
                LocalTime startOfDay = LocalTime.parse(daySchedule.get("start"), TIME_FORMATTER);
                return nextDay.with(startOfDay);
            }

            nextDay = nextDay.plusDays(1);
        }

        return nextDay;
    }

    private boolean isHoliday(LocalDate date, SlaBusinessHours businessHours) {
        if (businessHours == null || businessHours.getId() == null) {
            return false;
        }

        List<SlaHoliday> holidays = holidayRepository.findByBusinessHoursIdAndDate(
                businessHours.getId(), date);
        return !holidays.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> parseSchedule(String scheduleJson) {
        try {
            return objectMapper.readValue(scheduleJson,
                    new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (Exception e) {
            log.error("Failed to parse business hours schedule: {}", e.getMessage());
            return Map.of();
        }
    }
}

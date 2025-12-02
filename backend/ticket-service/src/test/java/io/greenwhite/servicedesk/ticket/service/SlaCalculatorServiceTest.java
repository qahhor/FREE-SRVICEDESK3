package io.greenwhite.servicedesk.ticket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.greenwhite.servicedesk.ticket.model.SlaBusinessHours;
import io.greenwhite.servicedesk.ticket.model.SlaHoliday;
import io.greenwhite.servicedesk.ticket.repository.SlaHolidayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SlaCalculatorService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SlaCalculatorService Tests")
class SlaCalculatorServiceTest {

    @Mock
    private SlaHolidayRepository holidayRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SlaCalculatorService calculatorService;

    private SlaBusinessHours businessHours;
    private UUID businessHoursId;

    @BeforeEach
    void setUp() {
        businessHoursId = UUID.randomUUID();
        
        // Standard business hours: Mon-Fri 09:00-18:00
        String schedule = """
            {
                "monday": {"start": "09:00", "end": "18:00"},
                "tuesday": {"start": "09:00", "end": "18:00"},
                "wednesday": {"start": "09:00", "end": "18:00"},
                "thursday": {"start": "09:00", "end": "18:00"},
                "friday": {"start": "09:00", "end": "18:00"}
            }
            """;

        businessHours = SlaBusinessHours.builder()
                .name("Standard Business Hours")
                .timezone("UTC")
                .schedule(schedule)
                .build();
        businessHours.setId(businessHoursId);
    }

    @Test
    @DisplayName("Should calculate due date within same business day")
    void shouldCalculateDueDateWithinSameDay() {
        // Given: Start at Monday 10:00, target 60 minutes
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 8, 10, 0); // Monday
        int targetMinutes = 60;

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        LocalDateTime dueDate = calculatorService.calculateDueDate(startTime, targetMinutes, businessHours);

        // Then: Due date should be Monday 11:00
        assertThat(dueDate).isEqualTo(LocalDateTime.of(2024, 1, 8, 11, 0));
    }

    @Test
    @DisplayName("Should calculate due date spanning multiple days")
    void shouldCalculateDueDateSpanningMultipleDays() {
        // Given: Start at Monday 17:00, target 120 minutes (2 hours)
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 8, 17, 0); // Monday 5pm
        int targetMinutes = 120;

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        LocalDateTime dueDate = calculatorService.calculateDueDate(startTime, targetMinutes, businessHours);

        // Then: Due date should be Tuesday 10:00 (1 hour remaining Monday + 1 hour Tuesday)
        assertThat(dueDate).isEqualTo(LocalDateTime.of(2024, 1, 9, 10, 0));
    }

    @Test
    @DisplayName("Should skip weekends when calculating due date")
    void shouldSkipWeekendsWhenCalculatingDueDate() {
        // Given: Start at Friday 17:00, target 120 minutes (2 hours)
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 12, 17, 0); // Friday 5pm
        int targetMinutes = 120;

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        LocalDateTime dueDate = calculatorService.calculateDueDate(startTime, targetMinutes, businessHours);

        // Then: Due date should be Monday 10:00 (1 hour Friday + skip weekend + 1 hour Monday)
        assertThat(dueDate).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    @Test
    @DisplayName("Should return start time plus minutes when no business hours configured")
    void shouldReturnSimpleCalculationWithoutBusinessHours() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 8, 10, 0);
        int targetMinutes = 60;

        // When
        LocalDateTime dueDate = calculatorService.calculateDueDate(startTime, targetMinutes, null);

        // Then: Simple addition without business hours consideration
        assertThat(dueDate).isEqualTo(LocalDateTime.of(2024, 1, 8, 11, 0));
    }

    @Test
    @DisplayName("Should calculate elapsed business minutes correctly")
    void shouldCalculateElapsedMinutesCorrectly() {
        // Given: From Monday 10:00 to Monday 12:00
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 8, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 8, 12, 0);

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        int elapsedMinutes = calculatorService.calculateElapsedMinutes(startTime, endTime, businessHours);

        // Then
        assertThat(elapsedMinutes).isEqualTo(120);
    }

    @Test
    @DisplayName("Should exclude non-business hours from elapsed time")
    void shouldExcludeNonBusinessHoursFromElapsedTime() {
        // Given: From Friday 17:00 to Monday 10:00 (1 hour Friday + 1 hour Monday = 2 hours)
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 12, 17, 0); // Friday 5pm
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 0);   // Monday 10am

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        int elapsedMinutes = calculatorService.calculateElapsedMinutes(startTime, endTime, businessHours);

        // Then: 1 hour on Friday + 1 hour on Monday = 120 minutes
        assertThat(elapsedMinutes).isEqualTo(120);
    }

    @Test
    @DisplayName("Should identify time within business hours")
    void shouldIdentifyTimeWithinBusinessHours() {
        // Given: Monday 10:00 (during business hours)
        LocalDateTime time = LocalDateTime.of(2024, 1, 8, 10, 0);

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        boolean isWithinBusinessHours = calculatorService.isWithinBusinessHours(time, businessHours);

        // Then
        assertThat(isWithinBusinessHours).isTrue();
    }

    @Test
    @DisplayName("Should identify time outside business hours")
    void shouldIdentifyTimeOutsideBusinessHours() {
        // Given: Monday 20:00 (after business hours)
        LocalDateTime time = LocalDateTime.of(2024, 1, 8, 20, 0);

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        boolean isWithinBusinessHours = calculatorService.isWithinBusinessHours(time, businessHours);

        // Then
        assertThat(isWithinBusinessHours).isFalse();
    }

    @Test
    @DisplayName("Should identify weekend as outside business hours")
    void shouldIdentifyWeekendAsOutsideBusinessHours() {
        // Given: Saturday 12:00
        LocalDateTime time = LocalDateTime.of(2024, 1, 13, 12, 0);

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        boolean isWithinBusinessHours = calculatorService.isWithinBusinessHours(time, businessHours);

        // Then
        assertThat(isWithinBusinessHours).isFalse();
    }

    @Test
    @DisplayName("Should get next business hour start when before business hours")
    void shouldGetNextBusinessHourStartWhenBeforeBusinessHours() {
        // Given: Monday 07:00 (before business hours)
        LocalDateTime time = LocalDateTime.of(2024, 1, 8, 7, 0);

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        LocalDateTime nextStart = calculatorService.getNextBusinessHourStart(time, businessHours);

        // Then: Should return Monday 09:00
        assertThat(nextStart).isEqualTo(LocalDateTime.of(2024, 1, 8, 9, 0));
    }

    @Test
    @DisplayName("Should return current time if within business hours")
    void shouldReturnCurrentTimeIfWithinBusinessHours() {
        // Given: Monday 10:00 (during business hours)
        LocalDateTime time = LocalDateTime.of(2024, 1, 8, 10, 0);

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        LocalDateTime nextStart = calculatorService.getNextBusinessHourStart(time, businessHours);

        // Then
        assertThat(nextStart).isEqualTo(time);
    }

    @Test
    @DisplayName("Should skip holidays when calculating due date")
    void shouldSkipHolidaysWhenCalculatingDueDate() {
        // Given: Start on day before a holiday
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 8, 17, 0); // Monday 5pm
        int targetMinutes = 120;
        
        // Tuesday is a holiday
        LocalDate holidayDate = LocalDate.of(2024, 1, 9);
        SlaHoliday holiday = SlaHoliday.builder()
                .name("Test Holiday")
                .date(holidayDate)
                .recurring(false)
                .businessHours(businessHours)
                .build();

        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), eq(LocalDate.of(2024, 1, 8))))
                .thenReturn(Collections.emptyList());
        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), eq(holidayDate)))
                .thenReturn(List.of(holiday));
        when(holidayRepository.findByBusinessHoursIdAndDate(eq(businessHoursId), eq(LocalDate.of(2024, 1, 10))))
                .thenReturn(Collections.emptyList());

        // When
        LocalDateTime dueDate = calculatorService.calculateDueDate(startTime, targetMinutes, businessHours);

        // Then: Due date should skip Tuesday and be Wednesday 10:00
        assertThat(dueDate).isEqualTo(LocalDateTime.of(2024, 1, 10, 10, 0));
    }
}

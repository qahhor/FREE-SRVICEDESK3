package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.SlaHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for SlaHoliday entity
 */
@Repository
public interface SlaHolidayRepository extends JpaRepository<SlaHoliday, UUID> {

    List<SlaHoliday> findByBusinessHoursId(UUID businessHoursId);

    /**
     * Find holidays by business hours ID and date, including recurring holidays.
     * Uses native query for date extraction as JPQL date functions vary by database.
     */
    @Query(value = "SELECT * FROM sla_holidays h WHERE h.business_hours_id = :businessHoursId " +
           "AND (h.holiday_date = :date OR (h.recurring = true AND " +
           "EXTRACT(MONTH FROM h.holiday_date) = EXTRACT(MONTH FROM CAST(:date AS DATE)) AND " +
           "EXTRACT(DAY FROM h.holiday_date) = EXTRACT(DAY FROM CAST(:date AS DATE))))",
           nativeQuery = true)
    List<SlaHoliday> findByBusinessHoursIdAndDate(
            @Param("businessHoursId") UUID businessHoursId,
            @Param("date") LocalDate date);
}

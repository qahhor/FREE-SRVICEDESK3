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

    @Query("SELECT h FROM SlaHoliday h WHERE h.businessHours.id = :businessHoursId " +
           "AND (h.date = :date OR (h.recurring = true AND " +
           "FUNCTION('EXTRACT', MONTH, h.date) = FUNCTION('EXTRACT', MONTH, :date) AND " +
           "FUNCTION('EXTRACT', DAY, h.date) = FUNCTION('EXTRACT', DAY, :date)))")
    List<SlaHoliday> findByBusinessHoursIdAndDate(
            @Param("businessHoursId") UUID businessHoursId,
            @Param("date") LocalDate date);
}

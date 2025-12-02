package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * SLA Holiday entity for defining holidays in business hours
 */
@Entity
@Table(name = "sla_holidays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaHoliday extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Builder.Default
    private Boolean recurring = false;

    @ManyToOne
    @JoinColumn(name = "business_hours_id")
    private SlaBusinessHours businessHours;
}

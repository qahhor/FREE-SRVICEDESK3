package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * SLA Business Hours entity for defining business hours configuration
 */
@Entity
@Table(name = "sla_business_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaBusinessHours extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 100)
    @Builder.Default
    private String timezone = "UTC";

    /**
     * JSON schedule in format: {"monday": {"start": "09:00", "end": "18:00"}, ...}
     */
    @Column(columnDefinition = "TEXT")
    private String schedule;

    @OneToMany(mappedBy = "businessHours", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SlaHoliday> holidays = new ArrayList<>();
}

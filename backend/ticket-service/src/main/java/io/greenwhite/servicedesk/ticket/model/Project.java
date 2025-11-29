package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Project entity for multi-project support
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "default_team_id")
    private Team defaultTeam;

    @Column(nullable = false)
    private Boolean active = true;

    @Column
    private String color;

    @Column
    private String icon;
}

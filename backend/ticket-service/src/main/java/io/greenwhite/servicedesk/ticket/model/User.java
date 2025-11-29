package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.enums.UserRole;
import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * User entity
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column
    private String phone;

    @Column
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @ManyToMany(mappedBy = "members")
    private Set<Team> teams = new HashSet<>();

    @Column(length = 5)
    private String language = "en";

    @Column
    private String timezone = "UTC";

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

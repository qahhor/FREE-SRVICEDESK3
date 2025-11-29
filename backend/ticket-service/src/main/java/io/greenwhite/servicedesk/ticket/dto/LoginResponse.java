package io.greenwhite.servicedesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Login response DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserDTO user;

    public LoginResponse(String accessToken, UserDTO user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}

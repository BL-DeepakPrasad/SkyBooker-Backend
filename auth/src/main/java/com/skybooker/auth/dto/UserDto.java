package com.skybooker.auth.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Integer userId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;
    private String role;
    private String provider;
    private boolean isActive;
    private String passportNumber;
    private String nationality;
    private LocalDateTime createdAt;
}

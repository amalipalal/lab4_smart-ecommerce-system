package org.example.dto.order;

public record CustomerDetails(
        String firstName,
        String lastName,
        String email,
        String phone
) {}

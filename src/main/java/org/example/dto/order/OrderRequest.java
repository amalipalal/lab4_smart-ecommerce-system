package org.example.dto.order;

import java.util.UUID;

public record OrderRequest(
        UUID productId,
        int quantity,
        String shippingCountry,
        String shippingCity,
        String shippingCode,
        String postalCode
) {}

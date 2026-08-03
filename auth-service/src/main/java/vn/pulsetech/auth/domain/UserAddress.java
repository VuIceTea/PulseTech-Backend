package vn.pulsetech.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_addresses")
public record UserAddress(
        @Id String id,
        String userId,
        String fullName,
        String phone,
        String addressLine,
        String ward,
        String district,
        String city,
        boolean isDefault
) {}

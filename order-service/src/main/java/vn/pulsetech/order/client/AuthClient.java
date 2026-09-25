package vn.pulsetech.order.client;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@Component
public class AuthClient {
    private final RestClient restClient;
    private final String internalApiKey;
    
    public AuthClient(RestClient authRestClient, @Value("${app.internal-api-key:}") String internalApiKey) {
        this.restClient = authRestClient;
        this.internalApiKey = internalApiKey;
    }

    public void addRewardPoints(String email, int points) {
        try {
            restClient.post()
                    .header("X-Internal-Api-Key", internalApiKey)
                    .uri(builder -> builder.path("/api/auth/users/reward-points")
                            .queryParam("email", email)
                            .queryParam("points", points)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Ignore error so it doesn't fail the order process, maybe log it
            System.err.println("Failed to add reward points for " + email + ": " + e.getMessage());
        }
    }
}

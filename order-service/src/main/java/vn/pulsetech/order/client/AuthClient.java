package vn.pulsetech.order.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthClient {
    private final RestClient restClient;
    
    public AuthClient(RestClient authRestClient) {
        this.restClient = authRestClient;
    }

    public void addRewardPoints(String email, int points) {
        try {
            restClient.post()
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

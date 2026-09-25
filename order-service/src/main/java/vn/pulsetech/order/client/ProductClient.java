package vn.pulsetech.order.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Component
public class ProductClient {
    private final RestClient restClient;
    public ProductClient(RestClient productRestClient) { this.restClient = productRestClient; }

    public ProductSnapshot getRequiredProduct(String productId) {
        try {
            ProductSnapshot product = restClient.get().uri("/api/products/{id}", productId)
                    .retrieve().body(ProductSnapshot.class);
            if (product == null) throw unavailable();
            return product;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm không tồn tại: " + productId);
            }
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    public void decreaseStock(String productId, String storage, int quantity) {
        try {
            restClient.post().uri("/internal/products/{id}/stock/decrease", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("storage", storage, "quantity", quantity))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Không thể trừ tồn kho cho biến thể " + storage + " của sản phẩm " + productId);
            }
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    public void increaseStock(String productId, String storage, int quantity, String reason) {
        try {
            restClient.post().uri("/internal/products/{id}/stock/increase", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("storage", storage, "quantity", quantity, "reason", reason != null ? reason : "RESTOCK"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {}
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product Service tạm thời không khả dụng");
    }

    public record ProductSnapshot(String id, String name, long basePrice, int discount, String image,
            List<ColorVariant> colors, List<StorageVariant> storages) {
        public record ColorVariant(String name, String hex, String image) {}
        public record StorageVariant(String name, long priceOffset, Integer stock) {}
    }
}

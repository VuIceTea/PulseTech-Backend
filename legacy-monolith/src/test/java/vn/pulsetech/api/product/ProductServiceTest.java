package vn.pulsetech.api.product;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ProductServiceTest {
    private final ProductService service;

    ProductServiceTest() throws Exception {
        service = new ProductService(new ObjectMapper());
    }

    @Test
    void loadsAndFiltersProductsFromResource() {
        assertThat(service.findAll(null, null, null, null, null)).hasSize(13);
        assertThat(service.findAll("phone", "Apple", "iphone", null, null))
                .extracting(product -> product.path("id").asText())
                .containsExactlyInAnyOrder("iphone-15-pro-max", "iphone-15");
    }

    @Test
    void findsProductById() {
        assertThat(service.findById("iphone-15-pro-max")).isPresent();
        assertThat(service.findById("missing-product")).isEmpty();
    }
}
package vn.pulsetech.product.config;

import org.springframework.web.util.HtmlUtils;
import vn.pulsetech.product.domain.Product;

final class ProductContentDefaults {
    private ProductContentDefaults() {}

    static String forProduct(Product product) {
        String name = escape(product.name(), "Sản phẩm");
        Product.ProductSpec specs = product.specs();

        if ("phone".equals(product.category()) || "tablet".equals(product.category()) || "laptop".equals(product.category())) {
            String cpu = escape(specs == null ? null : specs.cpu(), "thế hệ mới nhất");
            String ram = escape(specs == null ? null : specs.ram(), "dung lượng lớn");
            String battery = escape(specs == null ? null : specs.battery(), "thời lượng bền bỉ");
            String screen = escape(specs == null ? null : specs.screen(), "kích thước lớn, độ phân giải cao");
            String camera = escape(specs == null ? null : specs.camera(), "chất lượng cao");

            return """
                    <h2>Thiết kế sang trọng, thời thượng và đẳng cấp</h2>
                    <p><strong>%s</strong> kết hợp vật liệu cao cấp với ngôn ngữ thiết kế tinh tế. Các đường nét được hoàn thiện tỉ mỉ, mang lại cảm giác cầm nắm thoải mái và vẻ ngoài bền đẹp trong quá trình sử dụng.</p>
                    <h2>Hiệu năng mạnh mẽ, thách thức mọi giới hạn</h2>
                    <p>Thiết bị được trang bị bộ xử lý <strong>%s</strong> cùng <strong>%s</strong>, đáp ứng tốt nhu cầu đa nhiệm, giải trí và làm việc. Pin <strong>%s</strong> giúp duy trì trải nghiệm ổn định trong ngày.</p>
                    <h2>Nâng tầm trải nghiệm thị giác và nhiếp ảnh</h2>
                    <p>Màn hình <strong>%s</strong> cho hình ảnh rõ nét và sống động. Hệ thống camera <strong>%s</strong> hỗ trợ ghi lại những khoảnh khắc chi tiết trong nhiều điều kiện sử dụng.</p>
                    """.formatted(name, cpu, ram, battery, screen, camera).strip();
        }

        return """
                <h2>Chất lượng hoàn thiện cao cấp và bền bỉ</h2>
                <p><strong>%s</strong> được hoàn thiện từ vật liệu phù hợp với nhu cầu sử dụng hằng ngày, chú trọng độ bền, tính tiện dụng và thiết kế hài hòa.</p>
                <h2>Tương thích và trải nghiệm sử dụng</h2>
                <p>Sản phẩm được tối ưu để kết nối và hoạt động ổn định với các thiết bị tương thích. Mọi sản phẩm do PulseTech cung cấp đều trải qua quy trình kiểm tra trước khi đến tay khách hàng.</p>
                """.formatted(name).strip();
    }

    private static String escape(String value, String fallback) {
        String normalized = value == null || value.isBlank() ? fallback : value;
        return HtmlUtils.htmlEscape(normalized);
    }
}

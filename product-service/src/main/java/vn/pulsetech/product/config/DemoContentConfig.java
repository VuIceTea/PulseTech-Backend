package vn.pulsetech.product.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.pulsetech.product.domain.content.*;
import vn.pulsetech.product.repository.content.*;
import java.util.List;

@Configuration
public class DemoContentConfig {

    @Bean
    public CommandLineRunner initContentData(
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            BannerRepository bannerRepository,
            FilterRepository filterRepository,
            NavigationRepository navigationRepository,
            MegaMenuRepository megaMenuRepository,
            FooterLinkRepository footerLinkRepository,
            StoreRepository storeRepository,
            ArticleRepository articleRepository,
            vn.pulsetech.product.repository.ReviewRepository reviewRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                categoryRepository.saveAll(List.of(
                        new Category("cat-1", "Điện thoại", "Smartphone", 5, 1),
                        new Category("cat-2", "Máy tính bảng", "Tablet", 2, 2),
                        new Category("cat-3", "Laptop", "Laptop", 1, 3),
                        new Category("cat-4", "Âm thanh", "Headphones", 1, 4),
                        new Category("cat-5", "Phụ kiện", "Accessories", 4, 5)));
            }

            if (brandRepository.count() == 0) {
                brandRepository.saveAll(List.of(
                        new Brand("br-1", "Apple", "", 1),
                        new Brand("br-2", "Samsung", "", 2),
                        new Brand("br-3", "Xiaomi", "", 3),
                        new Brand("br-4", "Oppo", "", 4),
                        new Brand("br-5", "Asus", "", 5),
                        new Brand("br-6", "Dell", "", 6),
                        new Brand("br-7", "Sony", "", 7),
                        new Brand("br-8", "Marshall", "", 8)));
            }

            if (bannerRepository.count() == 0) {
                bannerRepository.saveAll(List.of(
                        new Banner("ban-1",
                                "https://images.unsplash.com/photo-1695048133142-1a20484d2569?q=80&w=800&auto=format&fit=crop",
                                "IPHONE 15 PRO MAX", "Titan cực đỉnh - Hiệu năng vượt trội",
                                "Trợ giá lên đời đến 2 triệu • Trả góp 0%", "from-zinc-900 to-zinc-800",
                                "/products/iphone-15-pro-max", "main", 1),
                        new Banner("ban-2",
                                "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?q=80&w=800&auto=format&fit=crop",
                                "GALAXY S24 ULTRA", "Quyền năng Galaxy AI trong tay bạn",
                                "Giảm ngay 7 triệu • Tặng củ sạc nhanh 45W", "from-blue-950 to-indigo-950",
                                "/products/samsung-galaxy-s24-ultra", "main", 2),
                        new Banner("ban-3",
                                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?q=80&w=800&auto=format&fit=crop",
                                "IPAD PRO M4 (2024)", "Đột phá siêu mỏng • Tandem OLED đỉnh cao",
                                "Ưu đãi học sinh sinh viên giảm thêm 500k", "from-slate-900 to-slate-800",
                                "/products/ipad-pro-m4", "main", 3)));
            }

            if (navigationRepository.count() == 0) {
                navigationRepository.saveAll(List.of(
                        new Navigation("nav-1", "Điện thoại", "/products?category=phone", "Smartphone", 1),
                        new Navigation("nav-2", "Tablet", "/products?category=tablet", "Tablet", 2),
                        new Navigation("nav-3", "Laptop", "/products?category=laptop", "Laptop", 3),
                        new Navigation("nav-4", "Âm thanh", "/products?category=audio", "Headphones", 4),
                        new Navigation("nav-5", "Phụ kiện", "/products?category=accessory", "Watch", 5),
                        new Navigation("nav-6", "Máy cũ", "/products?category=secondhand", "Recycle", 6)));
            }

            if (filterRepository.count() == 0) {
                filterRepository.saveAll(List.of(
                        new Filter("fil-1", "usage", "Nhu cầu sử dụng",
                                List.of("Học tập - Văn phòng", "Giải trí", "Đồ họa - Sáng tạo", "Chơi game"),
                                List.of("tablet", "laptop")),
                        new Filter("fil-2", "os", "Hệ điều hành",
                                List.of("iPadOS", "Android", "iOS", "Windows", "MacOS"),
                                List.of("phone", "tablet", "laptop")),
                        new Filter("fil-3", "ram", "RAM", List.of("4 GB", "8 GB", "16 GB", "32 GB"),
                                List.of("phone", "tablet", "laptop")),
                        new Filter("fil-4", "storage", "Bộ nhớ trong",
                                List.of("64 GB", "128 GB", "256 GB", "512 GB", "1 TB"),
                                List.of("phone", "tablet", "laptop")),
                        new Filter("fil-5", "hz", "Tần số quét", List.of("60 Hz", "90 Hz", "120 Hz", "144 Hz"),
                                List.of("phone", "tablet", "laptop")),
                        new Filter("fil-6", "chipset", "Chipset",
                                List.of("Apple A-series", "Snapdragon", "Exynos", "MediaTek"),
                                List.of("phone", "tablet")),
                        new Filter("fil-7", "watch_brand", "Thương hiệu",
                                List.of("Apple", "Samsung", "Garmin", "Huawei", "Amazfit", "Xiaomi", "Coros"),
                                List.of("accessory_đồng hồ"))));
            }

            if (megaMenuRepository.count() < 7) {
                megaMenuRepository.deleteAll();
                megaMenuRepository.saveAll(List.of(
                        new MegaMenu("mm-1", "Điện thoại di động", "Smartphone", "/products?category=phone", List.of(
                                new MegaMenu.MegaMenuSection("Hãng sản xuất", List.of(
                                        new MegaMenu.MegaMenuLink("iPhone (Apple)", "/products?brand=Apple"),
                                        new MegaMenu.MegaMenuLink("Samsung Galaxy", "/products?brand=Samsung"),
                                        new MegaMenu.MegaMenuLink("Xiaomi Redmi", "/products?brand=Xiaomi"),
                                        new MegaMenu.MegaMenuLink("OPPO / Realme", "/products?brand=OPPO"))),
                                new MegaMenu.MegaMenuSection("Mức giá", List.of(
                                        new MegaMenu.MegaMenuLink("Dưới 5 triệu", "/products?price=under5"),
                                        new MegaMenu.MegaMenuLink("Từ 5 - 10 triệu", "/products?price=5to10"),
                                        new MegaMenu.MegaMenuLink("Từ 10 - 20 triệu", "/products?price=10to20"),
                                        new MegaMenu.MegaMenuLink("Trên 20 triệu", "/products?price=over20")))),
                                1),
                        new MegaMenu("mm-2", "Máy tính bảng (Tablet)", "Tablet", "/products?category=tablet", List.of(
                                new MegaMenu.MegaMenuSection("Thương hiệu", List.of(
                                        new MegaMenu.MegaMenuLink("iPad (Apple)",
                                                "/products?brand=Apple&category=tablet"),
                                        new MegaMenu.MegaMenuLink("Samsung Galaxy Tab",
                                                "/products?brand=Samsung&category=tablet"),
                                        new MegaMenu.MegaMenuLink("Xiaomi Pad",
                                                "/products?brand=Xiaomi&category=tablet")))),
                                2),
                        new MegaMenu("mm-3", "Phụ kiện công nghệ", "Headphones", "/products?category=accessory",
                                List.of(
                                        new MegaMenu.MegaMenuSection("Phụ kiện di động", List.of(
                                                new MegaMenu.MegaMenuLink("Sạc, cáp chính hãng",
                                                        "/products?category=accessory_charge"),
                                                new MegaMenu.MegaMenuLink("Pin sạc dự phòng",
                                                        "/products?category=accessory_battery"),
                                                new MegaMenu.MegaMenuLink("Ốp lưng, bao da",
                                                        "/products?category=accessory_case")))),
                                3),
                        new MegaMenu("mm-4", "Smartwatch (Đồng hồ)", "Watch", "/products?category=watch", List.of(
                                new MegaMenu.MegaMenuSection("Thương hiệu nổi bật", List.of(
                                        new MegaMenu.MegaMenuLink("Apple Watch",
                                                "/products?brand=Apple&category=watch"),
                                        new MegaMenu.MegaMenuLink("Samsung Galaxy Watch",
                                                "/products?brand=Samsung&category=watch"),
                                        new MegaMenu.MegaMenuLink("Garmin / Coros",
                                                "/products?brand=Garmin&category=watch")))),
                                4),
                        new MegaMenu("mm-5", "Laptop & Màn hình", "Laptop", "/products?category=laptop", List.of(
                                new MegaMenu.MegaMenuSection("Phân loại Laptop", List.of(
                                        new MegaMenu.MegaMenuLink("MacBook (Apple)",
                                                "/products?brand=Apple&category=laptop"),
                                        new MegaMenu.MegaMenuLink("Laptop Gaming", "/products?category=laptop_gaming"),
                                        new MegaMenu.MegaMenuLink("Laptop Văn phòng",
                                                "/products?category=laptop_office")))),
                                5),
                        new MegaMenu("mm-6", "Thiết bị âm thanh", "Volume2", "/products?category=audio", List.of(
                                new MegaMenu.MegaMenuSection("Âm thanh chất lượng", List.of(
                                        new MegaMenu.MegaMenuLink("Tai nghe Bluetooth / True Wireless",
                                                "/products?category=audio_tws"),
                                        new MegaMenu.MegaMenuLink("Tai nghe Chụp tai (Over-ear)",
                                                "/products?category=audio_headphone"),
                                        new MegaMenu.MegaMenuLink("Loa Bluetooth di động",
                                                "/products?category=audio_speaker")))),
                                6),
                        new MegaMenu("mm-7", "Thu cũ đổi mới", "RefreshCw", "/trade-in", List.of(
                                new MegaMenu.MegaMenuSection("Chương trình ưu đãi", List.of(
                                        new MegaMenu.MegaMenuLink("Định giá máy cũ online", "/trade-in"),
                                        new MegaMenu.MegaMenuLink("Chính sách trợ giá lên đời", "/trade-in")))),
                                7)));
            }

            if (footerLinkRepository.count() == 0
                    || footerLinkRepository.findAll().stream().anyMatch(f -> "Về công ty".equals(f.title()))) {
                footerLinkRepository.deleteAll();
                footerLinkRepository.saveAll(List.of(
                        new FooterLink("fl-1", "TỔNG ĐÀI HỖ TRỢ", List.of(
                                new FooterLink.LinkItem("Gọi mua hàng: 1800.2097", "tel:18002097"),
                                new FooterLink.LinkItem("Khiếu nại: 1800.2098", "tel:18002098"),
                                new FooterLink.LinkItem("Bảo hành: 1800.2099", "tel:18002099"),
                                new FooterLink.LinkItem("Giờ phục vụ: 7:30 – 22:00 (Hàng ngày)", "#")), 1),
                        new FooterLink("fl-2", "CHÍNH SÁCH MUA HÀNG", List.of(
                                new FooterLink.LinkItem("Quy định bảo hành", "/policies?type=warranty"),
                                new FooterLink.LinkItem("Giao hàng & thanh toán", "/policies?type=shipping"),
                                new FooterLink.LinkItem("Đổi trả sản phẩm lỗi", "/policies?type=return"),
                                new FooterLink.LinkItem("Điều khoản dịch vụ", "/policies?type=terms")), 2)));
            }

            if (storeRepository.count() == 0) {
                storeRepository.saveAll(List.of(
                        new Store("st-1", "PulseTech Quận 1", "123 Lê Lợi, Q.1, TP.HCM", "0123456789",
                                "https://maps.google.com/?q=123", "08:00 - 22:00", 1),
                        new Store("st-2", "PulseTech Cầu Giấy", "456 Xuân Thủy, Cầu Giấy, Hà Nội", "0987654321",
                                "https://maps.google.com/?q=456", "08:00 - 21:00", 2)));
            }

            if (articleRepository.count() == 0) {
                articleRepository.saveAll(List.of(
                        new Article("art-1", "Đánh giá chi tiết iPhone 15 Pro Max", "danh-gia-iphone-15-pro-max",
                                "Siêu phẩm đáng mua nhất năm...", "Nội dung bài viết...",
                                "https://images.unsplash.com/photo-1695048133142-1a20484d2569", "Admin",
                                java.time.LocalDateTime.now(), "Review", 1500)));
            }

            if (reviewRepository.count() == 0) {
                reviewRepository.saveAll(List.of(
                        new vn.pulsetech.product.domain.Review("rev-1", "iphone-15-pro-max", "user-1", "Nguyễn Văn A",
                                5, "Sản phẩm tuyệt vời!", java.time.LocalDateTime.now())));
            }
        };
    }
}

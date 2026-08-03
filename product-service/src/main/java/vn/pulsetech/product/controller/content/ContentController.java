package vn.pulsetech.product.controller.content;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.pulsetech.product.domain.content.*;
import vn.pulsetech.product.repository.content.*;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final BannerRepository bannerRepository;
    private final FilterRepository filterRepository;
    private final NavigationRepository navigationRepository;
    private final MegaMenuRepository megaMenuRepository;
    private final FooterLinkRepository footerLinkRepository;
    private final StoreRepository storeRepository;
    private final ArticleRepository articleRepository;
    private final PolicyRepository policyRepository;

    public ContentController(
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            BannerRepository bannerRepository,
            FilterRepository filterRepository,
            NavigationRepository navigationRepository,
            MegaMenuRepository megaMenuRepository,
            FooterLinkRepository footerLinkRepository,
            StoreRepository storeRepository,
            ArticleRepository articleRepository,
            PolicyRepository policyRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.bannerRepository = bannerRepository;
        this.filterRepository = filterRepository;
        this.navigationRepository = navigationRepository;
        this.megaMenuRepository = megaMenuRepository;
        this.footerLinkRepository = footerLinkRepository;
        this.storeRepository = storeRepository;
        this.articleRepository = articleRepository;
        this.policyRepository = policyRepository;
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findAllByOrderByOrderAsc();
    }

    @GetMapping("/brands")
    public List<Brand> getBrands() {
        return brandRepository.findAllByOrderByOrderAsc();
    }

    @GetMapping("/banners")
    public List<Banner> getBanners() {
        return bannerRepository.findAllByOrderByOrderAsc();
    }

    @GetMapping("/filters")
    public List<Filter> getFilters() {
        return filterRepository.findAll();
    }

    @GetMapping("/navigation")
    public List<Navigation> getNavigation() {
        return navigationRepository.findAllByOrderByOrderAsc();
    }

    @GetMapping("/mega-menus")
    public List<MegaMenu> getMegaMenus() {
        return megaMenuRepository.findAll(); // Assuming ordering is handled or not needed
    }

    @GetMapping("/footer-links")
    public List<FooterLink> getFooterLinks() {
        return footerLinkRepository.findAll();
    }

    @GetMapping("/stores")
    public List<Store> getStores() {
        return storeRepository.findAll();
    }

    @GetMapping("/articles")
    public List<Article> getArticles() {
        return articleRepository.findAll();
    }

    @GetMapping("/policies")
    public List<Policy> getPolicies() {
        return policyRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Policy::orderIndex))
                .toList();
    }
}

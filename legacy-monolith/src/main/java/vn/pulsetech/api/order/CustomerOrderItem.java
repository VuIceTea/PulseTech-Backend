package vn.pulsetech.api.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_order_items")
public class CustomerOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productId;
    private String name;
    private long price;
    private int qty;
    private String image;
    private String color;
    private String storage;
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    protected CustomerOrderItem() {}

    public CustomerOrderItem(String productId, String name, long price, int qty, String image, String color, String storage) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.image = image;
        this.color = color;
        this.storage = storage;
    }

    void attachTo(CustomerOrder order) { this.order = order; }
    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public int getQty() { return qty; }
    public String getImage() { return image; }
    public String getColor() { return color; }
    public String getStorage() { return storage; }
}

package vn.pulsetech.order.domain;

import java.util.UUID;

public class CustomerOrderItem {
    private String id;
    private String productId;
    private String name;
    private long price;
    private int qty;
    private String image;
    private String color;
    private String storage;

    protected CustomerOrderItem() {}

    public CustomerOrderItem(String productId, String name, long price, int qty, String image, String color, String storage) {
        this.id = UUID.randomUUID().toString();
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.image = image;
        this.color = color;
        this.storage = storage;
    }

    public String getId() { return id; }
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public int getQty() { return qty; }
    public String getImage() { return image; }
    public String getColor() { return color; }
    public String getStorage() { return storage; }
}
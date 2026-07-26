package vn.pulsetech.order.domain;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String id; // productId
    private String name;
    private long price;
    private String image;
    private int quantity;
    private String color;
    private String storage;

    public CartItem() {}

    public CartItem(String id, String name, long price, String image, int quantity, String color, String storage) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
        this.color = color;
        this.storage = storage;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
}

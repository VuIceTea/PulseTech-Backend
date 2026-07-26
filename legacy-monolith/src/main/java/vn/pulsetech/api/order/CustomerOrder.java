package vn.pulsetech.api.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    private String id;
    @Column(nullable = false)
    private int status;
    @Column(nullable = false)
    private String customerName;
    @Column(nullable = false)
    private String customerPhone;
    @Column(nullable = false, length = 1000)
    private String address;
    @Column(nullable = false)
    private String paymentMethod;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private long totalPrice;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CustomerOrderItem> items = new ArrayList<>();

    protected CustomerOrder() {}

    public CustomerOrder(String id, String customerName, String customerPhone, String address, String paymentMethod) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.createdAt = LocalDateTime.now();
        this.status = 0;
    }

    public void addItem(CustomerOrderItem item) { items.add(item); item.attachTo(this); totalPrice += item.getPrice() * item.getQty(); }
    public void applyDiscount(int percent) { totalPrice = Math.round(totalPrice * (100 - percent) / 100.0); }
    public void addShipping(long amount) { totalPrice += amount; }
    public void setStatus(int status) { this.status = status; }
    public String getId() { return id; }
    public int getStatus() { return status; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getAddress() { return address; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getTotalPrice() { return totalPrice; }
    public List<CustomerOrderItem> getItems() { return items; }
}

package vn.pulsetech.order.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
public class CustomerOrder {
    @Id private String id;
    private int status;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String address;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private long totalPrice;
    private String transactionNo;
    private String bankCode;
    private String payDate;
    private List<CustomerOrderItem> items = new ArrayList<>();

    protected CustomerOrder() {}

    public CustomerOrder(String id, String customerName, String customerEmail, String customerPhone, String address, String paymentMethod) {
        this.id = id;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(CustomerOrderItem item) { items.add(item); totalPrice += item.getPrice() * item.getQty(); }
    public void applyDiscount(int percent) { totalPrice = Math.round(totalPrice * (100 - percent) / 100.0); }
    public void addShipping(long amount) { totalPrice += amount; }
    public void setStatus(int status) { this.status = status; }
    public String getId() { return id; }
    
    public int getStatus() { return status; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getAddress() { return address; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getTotalPrice() { return totalPrice; }
    public String getTransactionNo() { return transactionNo; }
    public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public String getPayDate() { return payDate; }
    public void setPayDate(String payDate) { this.payDate = payDate; }
    public List<CustomerOrderItem> getItems() { return items; }
}
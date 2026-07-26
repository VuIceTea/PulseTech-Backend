package vn.pulsetech.order.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.order.service.OrderService;
import vn.pulsetech.order.service.PaymentService;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/payment")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final OrderService orderService;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @GetMapping("/vnpay_return")
    public ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> allParams) {
        String orderId = allParams.get("vnp_TxnRef");
        String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
        
        boolean isValid = paymentService.verifySignature(allParams);
        System.out.println("VNPAY Return Params: " + allParams);
        System.out.println("VNPAY Signature Valid: " + isValid);
        System.out.println("VNPAY Response Code: " + vnp_ResponseCode);
        
        if (isValid) {
            if ("00".equals(vnp_ResponseCode)) {
                // Payment successful
                orderService.updateOrderPaymentInfo(orderId, allParams.get("vnp_TransactionNo"), allParams.get("vnp_BankCode"), allParams.get("vnp_PayDate"));
                orderService.updateOrderStatus(orderId, 1); // 1 = PAID
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(frontendUrl + "/cart?payment_success=true&orderId=" + orderId))
                        .build();
            } else {
                // Payment failed or canceled
                orderService.updateOrderStatus(orderId, 2); // 2 = FAILED
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(frontendUrl + "/cart?payment_success=false"))
                        .build();
            }
        } else {
            // Invalid signature
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/cart?payment_success=false&error=invalid_signature"))
                    .build();
        }
    }
}

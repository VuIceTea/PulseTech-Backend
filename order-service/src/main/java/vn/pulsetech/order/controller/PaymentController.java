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
                orderService.updateOrderStatus(orderId, 4); // 4 = cancelled/failed
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

    @GetMapping("/momo_return")
    public ResponseEntity<Void> momoReturn(@RequestParam Map<String, String> allParams) {
        String orderId = allParams.get("orderId");
        boolean successful = orderId != null
                && "0".equals(allParams.get("resultCode"))
                && paymentService.verifyMomoSignature(allParams);
        if (successful) {
            orderService.updateOrderPaymentInfo(orderId, allParams.get("transId"), "MOMO", allParams.get("responseTime"));
            orderService.updateOrderStatus(orderId, 1);
        } else if (orderId != null) {
            orderService.updateOrderStatus(orderId, 4);
        }
        return paymentRedirect(successful, orderId);
    }

    @PostMapping("/momo_ipn")
    public ResponseEntity<Void> momoIpn(@RequestBody Map<String, Object> allParams) {
        String orderId = allParams.get("orderId") == null ? null : String.valueOf(allParams.get("orderId"));
        boolean successful = orderId != null
                && "0".equals(String.valueOf(allParams.get("resultCode")))
                && paymentService.verifyMomoSignature(allParams);
        if (successful) {
            orderService.updateOrderPaymentInfo(orderId, String.valueOf(allParams.get("transId")), "MOMO",
                    String.valueOf(allParams.get("responseTime")));
            orderService.updateOrderStatus(orderId, 1);
        } else if (orderId != null) {
            orderService.updateOrderStatus(orderId, 4);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stripe_return")
    public ResponseEntity<Void> stripeReturn(@RequestParam String orderId, @RequestParam("session_id") String sessionId) {
        boolean successful = paymentService.verifyStripeSession(sessionId, orderId);
        if (successful) {
            orderService.updateOrderPaymentInfo(orderId, sessionId, "STRIPE", null);
            orderService.updateOrderStatus(orderId, 1);
        } else {
            orderService.updateOrderStatus(orderId, 4);
        }
        return paymentRedirect(successful, orderId);
    }

    private ResponseEntity<Void> paymentRedirect(boolean successful, String orderId) {
        String location = frontendUrl + "/cart?payment_success=" + successful;
        if (successful && orderId != null) {
            location += "&orderId=" + orderId;
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(location)).build();
    }
}

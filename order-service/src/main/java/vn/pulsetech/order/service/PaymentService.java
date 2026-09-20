package vn.pulsetech.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentService {
    @Value("${app.vnpay.tmn-code:T8HCS73G}")
    private String vnp_TmnCode;

    @Value("${app.vnpay.hash-secret:HAUC5ADXRA6O9YM7YGQSQYE10779GDI7}")
    private String vnp_HashSecret;

    @Value("${app.vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnp_PayUrl;
    
    @Value("${app.vnpay.return-url:http://localhost:8080/api/orders/payment/vnpay_return}")
    private String vnpReturnUrl;

    @Value("${app.payment.callback-base-url:http://localhost:8080/api/orders/payment}")
    private String paymentCallbackBaseUrl;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.momo.url:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoUrl;

    @Value("${app.momo.partner-code:}")
    private String momoPartnerCode;

    @Value("${app.momo.access-key:}")
    private String momoAccessKey;

    @Value("${app.momo.secret-key:}")
    private String momoSecretKey;

    @Value("${app.stripe.secret-key:}")
    private String stripeSecretKey;

    private final RestClient restClient = RestClient.create();

    public String createPaymentUrl(String paymentMethod, String orderId, long amount) {
        return switch (paymentMethod.toUpperCase(Locale.ROOT)) {
            case "VNPAY" -> createVnpayPaymentUrl(orderId, amount);
            case "MOMO" -> createMomoPaymentUrl(orderId, amount);
            case "STRIPE" -> createStripePaymentUrl(orderId, amount);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không được hỗ trợ");
        };
    }

    private String createVnpayPaymentUrl(String orderId, long amount) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", "ThanhToan_" + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        
        // This IP is fixed for simplicity as we don't haveHttpServletRequest easily accessible here
        vnp_Params.put("vnp_IpAddr", "127.0.0.1"); 
        
        vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        
        return vnp_PayUrl + "?" + queryUrl;
    }

    private String createMomoPaymentUrl(String orderId, long amount) {
        requireConfig(momoPartnerCode, "MOMO_PARTNER_CODE");
        requireConfig(momoAccessKey, "MOMO_ACCESS_KEY");
        requireConfig(momoSecretKey, "MOMO_SECRET_KEY");
        if (amount < 1_000 || amount > 50_000_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MoMo chỉ hỗ trợ đơn hàng từ 1.000đ đến 50.000.000đ");
        }

        String requestId = orderId + "-" + System.currentTimeMillis();
        String orderInfo = "Thanh toán đơn hàng " + orderId;
        String redirectUrl = paymentCallbackBaseUrl + "/momo_return";
        String ipnUrl = paymentCallbackBaseUrl + "/momo_ipn";
        String extraData = "";
        String rawSignature = "accessKey=" + momoAccessKey + "&amount=" + amount + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl + "&orderId=" + orderId + "&orderInfo=" + orderInfo
                + "&partnerCode=" + momoPartnerCode + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId + "&requestType=captureWallet";

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", momoPartnerCode);
        payload.put("requestId", requestId);
        payload.put("amount", amount);
        payload.put("orderId", orderId);
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", redirectUrl);
        payload.put("ipnUrl", ipnUrl);
        payload.put("requestType", "captureWallet");
        payload.put("extraData", extraData);
        payload.put("autoCapture", true);
        payload.put("lang", "vi");
        payload.put("signature", hmac("HmacSHA256", momoSecretKey, rawSignature));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post().uri(momoUrl)
                    .contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().body(Map.class);
            String payUrl = value(response, "payUrl");
            if (payUrl.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "MoMo không tạo được giao dịch: " + value(response, "message"));
            }
            return payUrl;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không thể kết nối cổng thanh toán MoMo", exception);
        }
    }

    private String createStripePaymentUrl(String orderId, long amount) {
        requireConfig(stripeSecretKey, "STRIPE_SECRET_KEY");
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("mode", "payment");
        form.add("success_url", paymentCallbackBaseUrl + "/stripe_return?orderId=" + orderId
                + "&session_id={CHECKOUT_SESSION_ID}");
        form.add("cancel_url", frontendUrl + "/cart?payment_success=false");
        form.add("client_reference_id", orderId);
        form.add("metadata[orderId]", orderId);
        form.add("line_items[0][price_data][currency]", "vnd");
        form.add("line_items[0][price_data][product_data][name]", "Đơn hàng PulseTech " + orderId);
        form.add("line_items[0][price_data][unit_amount]", String.valueOf(amount));
        form.add("line_items[0][quantity]", "1");

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post().uri("https://api.stripe.com/v1/checkout/sessions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + stripeSecretKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(Map.class);
            String checkoutUrl = value(response, "url");
            if (checkoutUrl.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe không trả về trang thanh toán");
            }
            return checkoutUrl;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không thể kết nối cổng thanh toán Stripe", exception);
        }
    }

    public boolean verifyMomoSignature(Map<String, ?> params) {
        if (momoAccessKey.isBlank() || momoSecretKey.isBlank()) return false;
        String rawSignature = "accessKey=" + momoAccessKey + "&amount=" + value(params, "amount")
                + "&extraData=" + value(params, "extraData") + "&message=" + value(params, "message")
                + "&orderId=" + value(params, "orderId") + "&orderInfo=" + value(params, "orderInfo")
                + "&orderType=" + value(params, "orderType") + "&partnerCode=" + value(params, "partnerCode")
                + "&payType=" + value(params, "payType") + "&requestId=" + value(params, "requestId")
                + "&responseTime=" + value(params, "responseTime") + "&resultCode=" + value(params, "resultCode")
                + "&transId=" + value(params, "transId");
        return hmac("HmacSHA256", momoSecretKey, rawSignature).equalsIgnoreCase(value(params, "signature"));
    }

    public boolean verifyStripeSession(String sessionId, String orderId) {
        requireConfig(stripeSecretKey, "STRIPE_SECRET_KEY");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> session = restClient.get()
                    .uri("https://api.stripe.com/v1/checkout/sessions/{id}", sessionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + stripeSecretKey)
                    .retrieve().body(Map.class);
            return "paid".equalsIgnoreCase(value(session, "payment_status"))
                    && orderId.equals(value(session, "client_reference_id"));
        } catch (RestClientException exception) {
            return false;
        }
    }

    public boolean verifySignature(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null) return false;

        Map<String, String> signedParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!"vnp_SecureHash".equals(entry.getKey()) && !"vnp_SecureHashType".equals(entry.getKey())) {
                signedParams.put(entry.getKey(), entry.getValue());
            }
        }

        List<String> fieldNames = new ArrayList<>(signedParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = signedParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String calculatedHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        return secureHash.equalsIgnoreCase(calculatedHash);
    }

    private String hmacSHA512(String key, String data) {
        return hmac("HmacSHA512", key, data);
    }

    private String hmac(String algorithm, String key, String data) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            mac.init(secretKey);
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo chữ ký thanh toán", ex);
        }
    }

    private void requireConfig(String value, String variableName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Cổng thanh toán chưa được cấu hình: thiếu " + variableName);
        }
    }

    private String value(Map<String, ?> values, String key) {
        if (values == null || values.get(key) == null) return "";
        return String.valueOf(values.get(key));
    }
}

package vn.pulsetech.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VerificationEmailService {
    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String mailFrom;

    public VerificationEmailService(JavaMailSender mailSender,
                                    @Value("${app.frontend-url}") String frontendUrl,
                                    @Value("${app.mail-from}") String mailFrom) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.mailFrom = mailFrom;
    }

    @Async
    public void send(String recipient, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(recipient);
        message.setSubject("Xác thực tài khoản PulseTech");
        message.setText("Chào bạn,\n\nHãy xác thực tài khoản PulseTech bằng liên kết sau:\n"
                + verificationUrl
                + "\n\nLiên kết có hiệu lực trong 24 giờ. Nếu bạn không đăng ký tài khoản, hãy bỏ qua email này.");
        mailSender.send(message);
    }
}
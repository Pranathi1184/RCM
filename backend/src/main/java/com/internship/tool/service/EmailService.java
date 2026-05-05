package com.internship.tool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.notification.recipient-email:darkfalcon80@gmail.com}")
    private String toEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOverdueAlert(String title, String deadline) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[OVERDUE ALERT] Regulatory Change: " + title);
            message.setText("⚠️ OVERDUE ALERT\n\nThe regulatory change '" + title + "' was due on " + deadline + " and is now overdue.\n\nPlease take immediate action.\n\n-- Regulatory Change Management System");
            mailSender.send(message);
            logger.info(">>> EMAIL SENT: [OVERDUE ALERT] '{}' was due on {}", title, deadline);
        } catch (Exception e) {
            logger.error(">>> EMAIL FAILED: Could not send overdue alert for '{}': {}", title, e.getMessage());
        }
    }

    public void sendAdvanceDeadlineAlert(String title, String deadline) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[UPCOMING DEADLINE] Regulatory Change: " + title);
            message.setText("📅 UPCOMING DEADLINE\n\nThe regulatory change '" + title + "' is due in 7 days on " + deadline + ".\n\nPlease ensure timely action.\n\n-- Regulatory Change Management System");
            mailSender.send(message);
            logger.info(">>> EMAIL SENT: [UPCOMING DEADLINE] '{}' due on {}", title, deadline);
        } catch (Exception e) {
            logger.error(">>> EMAIL FAILED: Could not send deadline alert for '{}': {}", title, e.getMessage());
        }
    }

    public void sendWeeklySummary(int activeCount, int overdueCount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[WEEKLY SUMMARY] Regulatory Changes Report");
            message.setText("📊 WEEKLY SUMMARY\n\nActive Changes: " + activeCount + "\nOverdue Changes: " + overdueCount + "\n\nPlease review and take action on overdue items.\n\n-- Regulatory Change Management System");
            mailSender.send(message);
            logger.info(">>> EMAIL SENT: [WEEKLY SUMMARY] Active: {}, Overdue: {}", activeCount, overdueCount);
        } catch (Exception e) {
            logger.error(">>> EMAIL FAILED: Could not send weekly summary: {}", e.getMessage());
        }
    }
}

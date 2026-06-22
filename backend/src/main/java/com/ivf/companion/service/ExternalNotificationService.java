package com.ivf.companion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalNotificationService {

    /**
     * Mocks sending an email notification
     */
    public void sendEmail(String toAddress, String subject, String body) {
        log.info("================ EMAIL NOTIFICATION ================");
        log.info("TO: {}", toAddress);
        log.info("SUBJECT: {}", subject);
        log.info("BODY:\n{}", body);
        log.info("====================================================");
    }

    /**
     * Mocks sending an SMS notification via a provider like Twilio
     */
    public void sendSms(String phoneNumber, String message) {
        log.info("================= SMS NOTIFICATION =================");
        log.info("PHONE: {}", phoneNumber);
        log.info("MESSAGE: {}", message);
        log.info("====================================================");
    }
}

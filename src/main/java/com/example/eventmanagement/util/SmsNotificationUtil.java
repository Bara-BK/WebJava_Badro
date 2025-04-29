package com.example.eventmanagement.util;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsNotificationUtil {
    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String FROM_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");

    public static void sendConfirmationSms(String participantName, String eventName, String ticketCode, String recipientPhone) throws Exception {
        if (recipientPhone == null || !recipientPhone.matches("\\+\\d{10,15}")) {
            throw new IllegalArgumentException("Invalid phone number format. Use international format (e.g., +21612345678)");
        }

        if (ACCOUNT_SID == null || AUTH_TOKEN == null || FROM_NUMBER == null) {
            throw new IllegalStateException("Twilio credentials are not set. Ensure TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_PHONE_NUMBER are configured as environment variables.");
        }

        // Initialize Twilio client
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        // Prepare message
        String messageBody = String.format(
            "Hello %s, your registration for %s is confirmed! Your ticket code is %s.",
            participantName, eventName, ticketCode
        );

        // Send SMS
        Message message = Message.creator(
            new PhoneNumber(recipientPhone),
            new PhoneNumber(FROM_NUMBER),
            messageBody
        ).create();

        System.out.println("SMS sent successfully to " + recipientPhone + ": SID=" + message.getSid());
    }
}
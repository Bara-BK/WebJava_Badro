package tn.badro.util;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import tn.badro.entities.Event;
import tn.badro.entities.Participation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Utility class for sending SMS notifications about events
 */
public class SmsNotificationUtil {
    private static final String CONFIG_FILE = "sms_config.properties";
    private static String ACCOUNT_SID;
    private static String AUTH_TOKEN;
    private static String FROM_NUMBER;
    private static boolean initialized = false;
    
    /**
     * Initialize the SMS client with credentials from configuration file
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initialize() {
        if (initialized) return true;
        
        try {
            if (!Files.exists(Paths.get(CONFIG_FILE))) {
                System.err.println("SMS config file not found: " + CONFIG_FILE);
                return false;
            }
            
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get(CONFIG_FILE)));
            
            ACCOUNT_SID = props.getProperty("twilio.account_sid");
            AUTH_TOKEN = props.getProperty("twilio.auth_token");
            FROM_NUMBER = props.getProperty("twilio.from_number");
            
            if (ACCOUNT_SID == null || AUTH_TOKEN == null || FROM_NUMBER == null) {
                System.err.println("Missing SMS configuration properties in " + CONFIG_FILE);
                return false;
            }
            
            // Initialize Twilio client
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            initialized = true;
            return true;
        } catch (IOException e) {
            System.err.println("Error loading SMS configuration: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send a confirmation SMS for event registration
     * @param participation The participation record
     * @param event The event details
     * @return true if SMS was sent successfully
     */
    public static boolean sendRegistrationConfirmation(Participation participation, Event event) {
        if (!initialize()) return false;
        if (participation.getTelephoneNumber() == null) {
            System.err.println("Cannot send SMS: No phone number provided");
            return false;
        }
        
        try {
            String phoneNumber = "+" + participation.getTelephoneNumber(); // Assuming the number is stored without "+"
            String messageBody = String.format(
                "Thank you for registering for \"%s\"! Your ticket code is: %s. Event date: %s. Don't forget to bring your ticket!",
                event.getTitre(),
                participation.getTicketCode(),
                formatDate(event.getDate())
            );
            
            Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(FROM_NUMBER),
                messageBody
            ).create();
            
            System.out.println("SMS sent with SID: " + message.getSid());
            return true;
        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send a reminder SMS for an upcoming event
     * @param participation The participation record
     * @param event The event details
     * @param daysRemaining Number of days remaining until the event
     * @return true if SMS was sent successfully
     */
    public static boolean sendEventReminder(Participation participation, Event event, int daysRemaining) {
        if (!initialize()) return false;
        if (participation.getTelephoneNumber() == null) {
            System.err.println("Cannot send SMS: No phone number provided");
            return false;
        }
        
        try {
            String phoneNumber = "+" + participation.getTelephoneNumber();
            String messageBody;
            
            if (daysRemaining == 0) {
                messageBody = String.format(
                    "REMINDER: \"%s\" is TODAY! Location: %s, Time: %s. Don't forget your ticket code: %s",
                    event.getTitre(),
                    event.getLieu() != null ? event.getLieu() : "TBA",
                    event.getHeure() != null ? event.getHeure().toString() : "TBA",
                    participation.getTicketCode()
                );
            } else {
                messageBody = String.format(
                    "REMINDER: \"%s\" is in %d day%s! Date: %s, Location: %s. Your ticket code is: %s",
                    event.getTitre(),
                    daysRemaining,
                    daysRemaining == 1 ? "" : "s",
                    formatDate(event.getDate()),
                    event.getLieu() != null ? event.getLieu() : "TBA",
                    participation.getTicketCode()
                );
            }
            
            Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(FROM_NUMBER),
                messageBody
            ).create();
            
            System.out.println("Reminder SMS sent with SID: " + message.getSid());
            return true;
        } catch (Exception e) {
            System.err.println("Error sending reminder SMS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static String formatDate(java.time.LocalDate date) {
        if (date == null) return "TBA";
        return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }
    
    /**
     * Create a sample SMS configuration file
     * @throws IOException if file cannot be written
     */
    public static void createSampleConfig() throws IOException {
        if (Files.exists(Paths.get(CONFIG_FILE))) {
            System.out.println("Config file already exists: " + CONFIG_FILE);
            return;
        }
        
        String content = "# Twilio SMS Configuration\n" +
                        "twilio.account_sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n" +
                        "twilio.auth_token=your_auth_token\n" +
                        "twilio.from_number=+1234567890\n";
        
        Files.write(Paths.get(CONFIG_FILE), content.getBytes());
        System.out.println("Created sample SMS configuration file: " + CONFIG_FILE);
        System.out.println("Please edit this file with your Twilio credentials");
    }
}
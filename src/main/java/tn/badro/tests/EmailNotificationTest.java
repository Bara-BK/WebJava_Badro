package tn.badro.tests;

import tn.badro.entities.Preferences;
import tn.badro.entities.User;
import tn.badro.services.EmailService;
import tn.badro.services.NotificationService;

/**
 * Test class to verify email notification functionality
 */
public class EmailNotificationTest {
    
    public static void main(String[] args) {
        System.out.println("Testing email notification service...");
        
        // Test direct email sending
        testDirectEmailSending();
        
        // Test notification service
        testNotificationService();
    }
    
    private static void testDirectEmailSending() {
        // Create email service
        EmailService emailService = new EmailService();
        
        // Test HTML email
        String testSubject = "BADRO Test Email";
        String testBody = "<!DOCTYPE html><html><head><title>Test Email</title></head>" +
                "<body style='font-family: Arial; color: #333;'>" +
                "<h1 style='color: #307D91;'>Test Email</h1>" +
                "<p>This is a test email to verify the email notification system.</p>" +
                "<p>If you can read this, the email notification system is working!</p>" +
                "</body></html>";
        
        boolean sent = emailService.sendNotificationEmail("your-email@example.com", testSubject, testBody);
        
        System.out.println("Direct email test: " + (sent ? "SUCCESS" : "FAILED"));
    }
    
    private static void testNotificationService() {
        // Create mock data
        User testUser = createTestUser();
        Preferences userPrefs = createTestPreferences("Winter", "Canada", "Computer Science");
        Preferences matchPrefs = createTestPreferences("Winter", "Canada", "Computer Science");
        
        // Create notification service
        NotificationService notificationService = new NotificationService();
        
        // Test preference match notification
        boolean matchSent = notificationService.sendPreferenceMatchNotification(testUser, userPrefs, matchPrefs);
        System.out.println("Preference match notification test: " + (matchSent ? "SUCCESS" : "FAILED"));
        
        // Test system notification
        boolean systemSent = notificationService.sendSystemNotification(testUser, 
                "System Notification Test", 
                "This is a test system notification. If you see this, the notification system is working correctly.");
        System.out.println("System notification test: " + (systemSent ? "SUCCESS" : "FAILED"));
    }
    
    private static User createTestUser() {
        User user = new User();
        user.setPrenom("Test");
        user.setNom("User");
        user.setEmail("your-email@example.com"); // Change this to your email
        return user;
    }
    
    private static Preferences createTestPreferences(String climate, String country, String domain) {
        Preferences prefs = new Preferences(
                climate,             // climate
                country,             // country
                domain,              // domain
                "English",           // preferred language
                "In-person classes", // teaching mode
                "Public",            // university type
                "Sports, Music",     // cultural activities
                "B2"                 // language level
        );
        return prefs;
    }
} 
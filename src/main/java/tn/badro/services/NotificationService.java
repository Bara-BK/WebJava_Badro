package tn.badro.services;

import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.entities.Preferences;
import tn.badro.entities.User;

import java.time.Year;

/**
 * Service for managing both in-app and email notifications
 */
public class NotificationService {
    private final EmailService emailService;
    private final NotificationManager notificationManager;
    
    public NotificationService() {
        this.emailService = new EmailService();
        this.notificationManager = NotificationManager.getInstance();
    }
    
    /**
     * Send both in-app and email notification for preference match
     * 
     * @param user User to notify
     * @param userPref User's preferences
     * @param matchingPref Matching preferences
     * @return true if both notifications sent successfully
     */
    public boolean sendPreferenceMatchNotification(User user, Preferences userPref, Preferences matchingPref) {
        if (user == null || user.getEmail() == null || userPref == null || matchingPref == null) {
            System.err.println("Cannot send preference match notification: missing required data");
            return false;
        }
        
        // Create notification message
        String notificationMessage = "New preference match found: " + 
                userPref.getCountry() + " - " + userPref.getDomain();
        
        // Add in-app notification
        notificationManager.addNotification(new Notification(notificationMessage));
        
        // Create and send email notification
        String subject = "BADRO - New Preference Match Found";
        String emailBody = createMatchingPreferencesEmail(
                user.getPrenom() + " " + user.getNom(), 
                userPref, 
                matchingPref);
        
        boolean emailSent = emailService.sendNotificationEmail(user.getEmail(), subject, emailBody);
        
        System.out.println("Notification for preference match: "
                + (emailSent ? "Email sent successfully" : "Email sending failed") 
                + " to " + user.getEmail());
        
        return emailSent;
    }
    
    /**
     * Send both in-app and email notification for generic system event
     * 
     * @param user User to notify
     * @param title Notification title
     * @param message Notification message
     * @return true if both notifications sent successfully
     */
    public boolean sendSystemNotification(User user, String title, String message) {
        if (user == null || user.getEmail() == null) {
            System.err.println("Cannot send system notification: user or email is null");
            return false;
        }
        
        // Add in-app notification
        notificationManager.addNotification(new Notification(message));
        
        // Create and send email notification
        String emailBody = createSystemNotificationEmail(
                user.getPrenom() + " " + user.getNom(), 
                title,
                message);
        
        boolean emailSent = emailService.sendNotificationEmail(user.getEmail(), "BADRO - " + title, emailBody);
        
        System.out.println("System notification: "
                + (emailSent ? "Email sent successfully" : "Email sending failed") 
                + " to " + user.getEmail());
        
        return emailSent;
    }
    
    /**
     * Creates an email template for preference matching notifications
     */
    private String createMatchingPreferencesEmail(String fullName, Preferences userPref, Preferences matchingPref) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Preference Match Found</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
                "    <div style=\"background: #307D91; padding: 20px; text-align: center; color: white; font-size: 22px; font-weight: bold;\">\n" +
                "        BADRO - Preference Match Found\n" +
                "    </div>\n" +
                "    <div style=\"padding: 20px; border: 1px solid #ddd; border-top: none;\">\n" +
                "        <h2 style=\"color: #307D91;\">Congratulations!</h2>\n" +
                "        <p>Dear <b>" + fullName + "</b>,</p>\n" +
                "        <p>Good news! We've found a match for your preferences. This means there's another user with similar study abroad interests.</p>\n" +
                "        \n" +
                "        <div style=\"background-color: #f0f7fa; padding: 15px; border: 1px solid #ddd; margin: 20px 0;\">\n" +
                "            <h3 style=\"color: #307D91; margin-top: 0;\">Matching Preferences Details:</h3>\n" +
                "            <table style=\"width: 100%; border-collapse: collapse;\">\n" +
                "                <tr style=\"background-color: #eaf3f7;\">\n" +
                "                    <th style=\"padding: 8px; text-align: left; border-bottom: 1px solid #ddd;\">Preference</th>\n" +
                "                    <th style=\"padding: 8px; text-align: left; border-bottom: 1px solid #ddd;\">Value</th>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">Country</td>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">" + userPref.getCountry() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">Climate</td>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">" + userPref.getClimat_pref() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">Domain</td>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">" + userPref.getDomain() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">Language</td>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">" + userPref.getPreferred_language() + " (" + userPref.getLanguage_level() + ")</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">Teaching Mode</td>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">" + userPref.getTeaching_mode() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">University Type</td>\n" +
                "                    <td style=\"padding: 8px; border-bottom: 1px solid #eee;\">" + userPref.getUniversity_type() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 8px;\">Cultural Activities</td>\n" +
                "                    <td style=\"padding: 8px;\">" + userPref.getCultural_activities() + "</td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "        \n" +
                "        <p>This match suggests that you may be interested in similar study abroad programs. You can log in to your BADRO account to explore more details and discover potential study opportunities that match your preferences.</p>\n" +
                "        \n" +
                "        <hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">\n" +
                "        <p>Best regards,<br>\n" +
                "        The BADRO Team</p>\n" +
                "    </div>\n" +
                "    <div style=\"background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #777;\">\n" +
                "        <p>This is an automated message. Please do not reply to this email.</p>\n" +
                "        <p>&copy; " + Year.now().getValue() + " BADRO. All rights reserved.</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
    
    /**
     * Creates an email template for system notifications
     */
    private String createSystemNotificationEmail(String fullName, String title, String message) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>" + title + "</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
                "    <div style=\"background: #307D91; padding: 20px; text-align: center; color: white; font-size: 22px; font-weight: bold;\">\n" +
                "        BADRO - " + title + "\n" +
                "    </div>\n" +
                "    <div style=\"padding: 20px; border: 1px solid #ddd; border-top: none;\">\n" +
                "        <h2 style=\"color: #307D91;\">" + title + "</h2>\n" +
                "        <p>Dear <b>" + fullName + "</b>,</p>\n" +
                "        <p>" + message + "</p>\n" +
                "        <hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">\n" +
                "        <p>Best regards,<br>\n" +
                "        The BADRO Team</p>\n" +
                "    </div>\n" +
                "    <div style=\"background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #777;\">\n" +
                "        <p>This is an automated message. Please do not reply to this email.</p>\n" +
                "        <p>&copy; " + Year.now().getValue() + " BADRO. All rights reserved.</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
} 
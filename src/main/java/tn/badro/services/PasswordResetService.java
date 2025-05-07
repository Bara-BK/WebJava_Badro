package tn.badro.services;

import tn.badro.entities.User;
import tn.badro.tools.MyDataBase;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetService {
    
    private final MyDataBase dbConnection;
    private final UserService userService;
    private final EmailService emailService;
    
    public PasswordResetService() {
        this.dbConnection = MyDataBase.getInstance();
        this.userService = new UserService();
        this.emailService = new EmailService();
    }
    
    /**
     * Generate a password reset token for the user and store it in the database.
     * This is an alias for requestPasswordReset for backwards compatibility.
     * 
     * @param user The user requesting password reset
     */
    public void createResetToken(User user) {
        requestPasswordReset(user);
    }
    
    /**
     * Generate a password reset token for the user and store it in the database.
     * 
     * @param user The user requesting password reset
     */
    public void requestPasswordReset(User user) {
        // Generate a unique token
        String token = UUID.randomUUID().toString();
        
        // Set expiration to 24 hours from now
        Timestamp expiration = Timestamp.valueOf(LocalDateTime.now().plusHours(24));
        
        // Store the token in the database
        userService.createResetToken(user.getId(), token, expiration);
        
        // Generate token file as fallback
        generateTokenFile(user, token);
        
        // Send token via email
        sendResetEmail(user, token);
    }
    
    /**
     * Verify if a reset token is valid
     * 
     * @param token The token to verify
     * @return The user associated with the token if valid, null otherwise
     */
    public User verifyResetToken(String token) {
        return userService.findUserByResetToken(token);
    }
    
    /**
     * Reset user's password and invalidate the token
     * 
     * @param token The reset token
     * @param newPassword The new password to set
     * @return true if successful, false otherwise
     */
    public boolean resetPassword(String token, String newPassword) {
        User user = verifyResetToken(token);
        if (user == null) {
            return false;
        }
        
        // Update the password
        userService.updatePasswordByUserId(user.getId(), newPassword);
        
        // Invalidate the token
        userService.invalidateResetToken(token);
        
        // Send confirmation email
        sendPasswordChangedEmail(user);
        
        return true;
    }
    
    /**
     * Generates a reset token for the given email and sends it via email
     * @param email The email to send the reset token to
     * @return true if token was generated successfully, false otherwise
     */
    public boolean generateResetToken(String email) {
        try {
            // First find the user by email
            User user = userService.findByEmail(email);
            if (user == null) {
                System.out.println("No user found with email: " + email);
                return false;
            }
            
            // Generate a random token
            String token = UUID.randomUUID().toString();
            
            // Store token in database with expiration
            LocalDateTime expirationTime = LocalDateTime.now().plusHours(24);
            
            try {
                Connection connection = dbConnection.getCnx();
                String sql = "INSERT INTO password_reset_token (user_id, token, expiration, used) VALUES (?, ?, ?, ?)";
                
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, user.getId());
                    statement.setString(2, token);
                    statement.setTimestamp(3, Timestamp.valueOf(expirationTime));
                    statement.setBoolean(4, false);
                    
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        // Save token to file as fallback
                        generateTokenFile(user, token);
                        
                        // Send token via email
                        sendResetEmail(user, token);
                        
                        return true;
                    } else {
                        System.err.println("Failed to insert token into database");
                        return false;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Database error while generating reset token: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Unexpected error generating reset token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Sends a password reset email to the user
     */
    private void sendResetEmail(User user, String token) {
        String subject = "Password Reset Request";
        String emailHtml = createPasswordResetEmailHtml(user.getPrenom() + " " + user.getNom(), token);
        
        // Send the email
        boolean emailSent = emailService.sendEmail(user.getEmail(), subject, emailHtml, true);
        
        if (emailSent) {
            System.out.println("Password reset email sent successfully to: " + user.getEmail());
        } else {
            System.err.println("Failed to send password reset email. Token file created as fallback.");
        }
    }
    
    /**
     * Sends a password changed confirmation email
     */
    private void sendPasswordChangedEmail(User user) {
        String subject = "Password Changed Successfully";
        String emailHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Password Changed</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
                "    <div style=\"background: #4caf50; padding: 20px; text-align: center; color: white; font-size: 22px; font-weight: bold;\">\n" +
                "        BADRO - Password Changed\n" +
                "    </div>\n" +
                "    <div style=\"padding: 20px; border: 1px solid #ddd; border-top: none;\">\n" +
                "        <h2 style=\"color: #4caf50;\">Password Changed Successfully</h2>\n" +
                "        <p>Dear <b>" + user.getPrenom() + " " + user.getNom() + "</b>,</p>\n" +
                "        <p>Your password for your BADRO account has been changed successfully.</p>\n" +
                "        <div style=\"text-align: center; margin: 20px 0; font-size: 48px; color: #4caf50;\">\n" +
                "            ✓\n" +
                "        </div>\n" +
                "        <p>You can now log in using your new password.</p>\n" +
                "        <div style=\"background-color: #fff8e1; border-left: 4px solid #ffb300; padding: 15px; margin: 20px 0;\">\n" +
                "            <p><b>Security Notice:</b> If you did not request this password change, please contact support immediately.</p>\n" +
                "        </div>\n" +
                "        <hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">\n" +
                "        <p>Best regards,<br>\n" +
                "        The BADRO Team</p>\n" +
                "    </div>\n" +
                "    <div style=\"background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #777;\">\n" +
                "        <p>This is an automated message. Please do not reply to this email.</p>\n" +
                "        <p>&copy; " + java.time.Year.now().getValue() + " BADRO. All rights reserved.</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        
        emailService.sendEmail(user.getEmail(), subject, emailHtml, true);
    }
    
    /**
     * Creates HTML content for password reset email
     */
    private String createPasswordResetEmailHtml(String fullName, String token) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Password Reset</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
                "    <div style=\"background: #3f51b5; padding: 20px; text-align: center; color: white; font-size: 22px; font-weight: bold;\">\n" +
                "        BADRO - Password Reset\n" +
                "    </div>\n" +
                "    <div style=\"padding: 20px; border: 1px solid #ddd; border-top: none;\">\n" +
                "        <h2 style=\"color: #3f51b5;\">Password Reset Request</h2>\n" +
                "        <p>Dear <b>" + fullName + "</b>,</p>\n" +
                "        <p>We received a request to reset your password for your BADRO account.</p>\n" +
                "        <p><b>Your password reset token is:</b></p>\n" +
                "        <div style=\"background-color: #f5f5f5; padding: 15px; border: 1px solid #ddd; margin: 20px 0; text-align: center; font-family: monospace; font-size: 18px; font-weight: bold;\">\n" +
                "            " + token + "\n" +
                "        </div>\n" +
                "        <p>To reset your password:</p>\n" +
                "        <ol>\n" +
                "            <li>Copy the token above</li>\n" +
                "            <li>Enter it in the password reset form</li>\n" +
                "            <li>Create your new password</li>\n" +
                "        </ol>\n" +
                "        <p><b>Note:</b> This token will expire in 24 hours.</p>\n" +
                "        <p>If you did not request a password reset, please ignore this email.</p>\n" +
                "        <hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">\n" +
                "        <p>Best regards,<br>\n" +
                "        The BADRO Team</p>\n" +
                "    </div>\n" +
                "    <div style=\"background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #777;\">\n" +
                "        <p>This is an automated message. Please do not reply to this email.</p>\n" +
                "        <p>&copy; " + java.time.Year.now().getValue() + " BADRO. All rights reserved.</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
    
    /**
     * Generates a file containing the reset token
     */
    private void generateTokenFile(User user, String token) {
        // Try multiple locations to ensure file creation works
        boolean fileSaved = false;
        
        // 1. Try user home directory
        try {
            String userHome = System.getProperty("user.home");
            String homeTokenFile = userHome + File.separator + "reset_token.txt";
            
            try (FileWriter tokenWriter = new FileWriter(homeTokenFile)) {
                tokenWriter.write("Your password reset token is: " + token);
            }
            fileSaved = true;
            System.out.println("Token file created at: " + homeTokenFile);
        } catch (IOException e) {
            System.err.println("Could not write to home directory: " + e.getMessage());
        }
        
        // 2. Try Desktop if available
        try {
            String userHome = System.getProperty("user.home");
            String desktopPath = userHome + File.separator + "Desktop";
            File desktopDir = new File(desktopPath);
            
            if (desktopDir.exists() && desktopDir.isDirectory()) {
                String desktopTokenFile = desktopPath + File.separator + "reset_token.txt";
                try (FileWriter tokenWriter = new FileWriter(desktopTokenFile)) {
                    tokenWriter.write("Your password reset token is: " + token);
                }
                fileSaved = true;
                System.out.println("Token file created at: " + desktopTokenFile);
            }
        } catch (IOException e) {
            System.err.println("Could not write to Desktop: " + e.getMessage());
        }
        
        // 3. Try current directory as last resort
        if (!fileSaved) {
            try {
                String currentDirFile = "reset_token.txt";
                try (FileWriter tokenWriter = new FileWriter(currentDirFile)) {
                    tokenWriter.write("Your password reset token is: " + token);
                }
                System.out.println("Token file created in current directory: " + currentDirFile);
                fileSaved = true;
            } catch (IOException e) {
                System.err.println("Could not write to current directory: " + e.getMessage());
            }
        }
        
        // Always display token in console
        System.out.println("=========================================================");
        System.out.println("PASSWORD RESET TOKEN FOR: " + user.getEmail());
        System.out.println("TOKEN: " + token);
        System.out.println("=========================================================");
    }
}

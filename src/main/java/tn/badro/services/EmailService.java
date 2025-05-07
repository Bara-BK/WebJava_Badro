package tn.badro.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.UUID;

/**
 * Simple email service with fallback to file storage.
 * For password reset tokens when email sending fails.
 */
public class EmailService {
    
    private final String username = "guesmijacem8@gmail.com";
    private final String password = "couu mgqn mzpe ojnh"; // Gmail app password
    private final String host = "smtp.gmail.com";
    private final int port = 587;
    
    private static final String TOKEN_DIR = "reset_tokens";
    private static final String SENT_EMAILS_DIR = "sent_emails";
    
    public EmailService() {
        createTokenDirectory();
        createSentEmailsDirectory();
    }
    
    /**
     * Creates the token directory if it doesn't exist
     */
    private void createTokenDirectory() {
        File tokenDir = new File(TOKEN_DIR);
        if (!tokenDir.exists()) {
            boolean created = tokenDir.mkdirs();
            if (created) {
                System.out.println("Created directory for reset tokens: " + tokenDir.getAbsolutePath());
            }
        }
    }
    
    /**
     * Creates the sent emails directory if it doesn't exist
     */
    private void createSentEmailsDirectory() {
        File emailsDir = new File(SENT_EMAILS_DIR);
        if (!emailsDir.exists()) {
            boolean created = emailsDir.mkdirs();
            if (created) {
                System.out.println("Created directory for sent emails: " + emailsDir.getAbsolutePath());
            }
        }
    }
    
    /**
     * Sends an email or saves token to file if email fails
     */
    public boolean sendEmail(String to, String subject, String body, boolean isHtml) {
        try {
            // Extract token and save to file first as backup
            String token = extractTokenFromBody(body);
            saveTokenToFile(to, token);
            
            // For HTML emails, make sure the token is visible in the subject
            if (isHtml && token != null) {
                subject = subject + " - Token: " + token;
            }
            
            // Simplify command with correct MIME Content-Type
            String escapedBody = body.replace("'", "''").replace("\r\n", " ").replace("\n", " ");
            String escapedSubject = subject.replace("'", "''");
            
            // Define the mime type based on isHtml flag
            String contentType = isHtml ? "text/html" : "text/plain";
            
            String command = "powershell -Command \"" +
                    "$ErrorActionPreference = 'Stop'; " +
                    "try { " +
                    "$secpasswd = ConvertTo-SecureString '" + password.replace("'", "''") + "' -AsPlainText -Force; " +
                    "$cred = New-Object System.Management.Automation.PSCredential('" + username + "', $secpasswd); " +
                    
                    // Create the mail message with proper content type
                    "$msg = New-Object System.Net.Mail.MailMessage; " +
                    "$msg.From = '" + username + "'; " +
                    "$msg.To.Add('" + to + "'); " +
                    "$msg.Subject = '" + escapedSubject + "'; " +
                    "$msg.Body = '" + escapedBody + "'; " +
                    "$msg.IsBodyHtml = " + (isHtml ? "$true" : "$false") + "; " +
                    
                    // Send using SmtpClient directly instead of Send-MailMessage
                    "$smtp = New-Object System.Net.Mail.SmtpClient('" + host + "', " + port + "); " +
                    "$smtp.EnableSsl = $true; " +
                    "$smtp.Credentials = $cred; " +
                    "$smtp.Send($msg); " +
                    
                    "Write-Output 'Email sent successfully'; " +
                    "} catch { Write-Error $_.Exception.Message }\"";
            
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);
            
            // Read output
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = outputReader.readLine()) != null) {
                System.out.println(line);
            }
            
            // Read error if any
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
                System.err.println(line);
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && !errorOutput.toString().contains("Error")) {
                System.out.println("\n==========================================================");
                System.out.println("EMAIL SENT SUCCESSFULLY");
                System.out.println("----------------------------------------------------------");
                System.out.println("From: " + username);
                System.out.println("To: " + to);
                System.out.println("Subject: " + subject);
                System.out.println("==========================================================\n");
                return true;
            } else {
                System.err.println("Failed to send email. Error code: " + exitCode);
                System.out.println("Using token file fallback method.");
                System.out.println("TOKEN SAVED TO: " + TOKEN_DIR + File.separator + to.replace("@", "_at_") + "_" + token + ".txt");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Email sending error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Extract token from email body (if present)
     */
    private String extractTokenFromBody(String body) {
        // Look for a UUID pattern in the body
        String token = UUID.randomUUID().toString(); // Default fallback
        
        if (body != null && body.contains("token")) {
            // Try to extract from HTML with div class="token"
            int tokenStart = body.indexOf("class=\"token\">") + 14;
            if (tokenStart > 14) {
                int tokenEnd = body.indexOf("</div>", tokenStart);
                if (tokenEnd > tokenStart) {
                    String potentialToken = body.substring(tokenStart, tokenEnd).trim();
                    if (potentialToken.matches("[a-zA-Z0-9\\-]+")) {
                        token = potentialToken;
                    }
                }
            }
        }
        
        return token;
    }
    
    /**
     * Save token to a file as a fallback
     */
    private void saveTokenToFile(String email, String token) {
        try {
            // Create a unique filename based on email and token
            String filename = email.replace("@", "_at_") + "_" + token + ".txt";
            File tokenFile = new File(TOKEN_DIR, filename);
            
            // Write token information to file
            String content = "PASSWORD RESET TOKEN FOR: " + email + "\n" +
                             "TOKEN: " + token + "\n" +
                             "CREATED: " + new Date() + "\n";
            
            try (FileWriter writer = new FileWriter(tokenFile)) {
                writer.write(content);
            }
            
            System.out.println("Token saved to file: " + tokenFile.getAbsolutePath());
            
            // Also print to console
            System.out.println("\n=========================================================");
            System.out.println("PASSWORD RESET TOKEN FOR: " + email);
            System.out.println("TOKEN: " + token);
            System.out.println("=========================================================\n");
        } catch (IOException e) {
            System.err.println("Failed to save token to file: " + e.getMessage());
        }
    }
    
    /**
     * Simplified method to send notification emails without token handling
     * 
     * @param to Email address to send to
     * @param subject Email subject
     * @param htmlBody HTML content of the email
     * @return True if email sent successfully
     */
    public boolean sendNotificationEmail(String to, String subject, String htmlBody) {
        try {
            // Always use HTML for notifications
            String contentType = "text/html";
            
            // Save a copy of the email
            saveEmailToFile(to, subject, htmlBody, contentType);
            
            // Escape special characters in subject and body for PowerShell
            String escapedBody = htmlBody.replace("'", "''").replace("\r\n", " ").replace("\n", " ");
            String escapedSubject = subject.replace("'", "''");
            
            String command = "powershell -Command \"" +
                    "$ErrorActionPreference = 'Stop'; " +
                    "try { " +
                    "$secpasswd = ConvertTo-SecureString '" + password.replace("'", "''") + "' -AsPlainText -Force; " +
                    "$cred = New-Object System.Management.Automation.PSCredential('" + username + "', $secpasswd); " +
                    
                    // Create the mail message with proper content type
                    "$msg = New-Object System.Net.Mail.MailMessage; " +
                    "$msg.From = '" + username + "'; " +
                    "$msg.To.Add('" + to + "'); " +
                    "$msg.Subject = '" + escapedSubject + "'; " +
                    "$msg.Body = '" + escapedBody + "'; " +
                    "$msg.IsBodyHtml = $true; " +
                    
                    // Send using SmtpClient directly instead of Send-MailMessage
                    "$smtp = New-Object System.Net.Mail.SmtpClient('" + host + "', " + port + "); " +
                    "$smtp.EnableSsl = $true; " +
                    "$smtp.Credentials = $cred; " +
                    "$smtp.Send($msg); " +
                    
                    "Write-Output 'Email sent successfully'; " +
                    "} catch { Write-Error $_.Exception.Message }\"";
            
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);
            
            // Read output
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = outputReader.readLine()) != null) {
                System.out.println(line);
            }
            
            // Read error if any
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
                System.err.println(line);
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && !errorOutput.toString().contains("Error")) {
                System.out.println("\n==========================================================");
                System.out.println("NOTIFICATION EMAIL SENT SUCCESSFULLY");
                System.out.println("----------------------------------------------------------");
                System.out.println("From: " + username);
                System.out.println("To: " + to);
                System.out.println("Subject: " + subject);
                System.out.println("==========================================================\n");
                return true;
            } else {
                System.err.println("Failed to send notification email. Error code: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Email sending error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Save a copy of the sent email to a file
     */
    private void saveEmailToFile(String to, String subject, String body, String contentType) {
        try {
            // Create a unique filename based on email and timestamp
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = to.replace("@", "_at_") + "_" + timestamp + ".html";
            File emailFile = new File(SENT_EMAILS_DIR, filename);
            
            // Build the email content with headers
            StringBuilder emailContent = new StringBuilder();
            emailContent.append("From: ").append(username).append("\n");
            emailContent.append("To: ").append(to).append("\n");
            emailContent.append("Subject: ").append(subject).append("\n");
            emailContent.append("Date: ").append(new java.util.Date()).append("\n");
            emailContent.append("Content-Type: ").append(contentType).append("\n\n");
            emailContent.append(body);
            
            try (FileWriter writer = new FileWriter(emailFile)) {
                writer.write(emailContent.toString());
            }
            
            System.out.println("Email copy saved to file: " + emailFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save email copy to file: " + e.getMessage());
        }
    }
} 
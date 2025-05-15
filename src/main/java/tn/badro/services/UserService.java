package tn.badro.services;

import tn.badro.entities.User;
import tn.badro.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UserService {
    private final MyDataBase dbConnection;

    public UserService() {
        this.dbConnection = MyDataBase.getInstance();
    }

    /**
     * Hashes a password using SHA-256 with a random salt
     * @param password The plain text password to hash
     * @return The hashed password with salt, format: salt:hash
     */
    public String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            // Create MessageDigest instance for SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // Add salt to digest
            md.update(salt);
            
            // Get the hashed password
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convert salt and hash to Base64 for storage
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);
            
            // Return salt:hash format
            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verifies a password against its stored hash
     * @param password The plain text password to verify
     * @param storedHash The stored password hash (salt:hash)
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(String password, String storedHash) {
        try {
            // Split stored hash to get salt and hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            String saltBase64 = parts[0];
            String storedHashBase64 = parts[1];
            
            // Decode Base64
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            
            // Create MessageDigest instance for SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // Add salt to digest
            md.update(salt);
            
            // Calculate hash of provided password
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convert to Base64 for comparison
            String newHashBase64 = Base64.getEncoder().encodeToString(hashedPassword);
            
            // Compare hashes
            return newHashBase64.equals(storedHashBase64);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error verifying password", e);
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (Connection conn = dbConnection.getCnx();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setNumtlf(rs.getString("numtlf"));
                user.setAge(rs.getInt("age"));
                user.setRoles(rs.getString("roles"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users: " + e.getMessage(), e);
        }
        return users;
    }

    public Optional<User> getUserById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setNumtlf(rs.getString("numtlf"));
                user.setAge(rs.getInt("age"));
                user.setRoles(rs.getString("roles"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void createUser(User user) {
        String sql = "INSERT INTO user (nom, prenom, email, password, numtlf, age, roles) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Hash the password before storing it
            String hashedPassword = hashPassword(user.getPassword());

            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, user.getNumtlf());
            pstmt.setInt(6, user.getAge());
            pstmt.setString(7, user.getRoles());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }
    
    // Alias for createUser to match UserDAO interface
    public void addUser(User user) throws SQLException {
        createUser(user);
    }

    public void updateUser(int id, User user) {
        // Check if password was changed
        User existingUser = getUserById(id).orElse(null);
        String passwordToUse = user.getPassword();
        
        if (existingUser != null && !passwordToUse.equals(existingUser.getPassword())) {
            // If password was changed, hash the new password
            passwordToUse = hashPassword(passwordToUse);
        }
        
        String sql = "UPDATE user SET nom = ?, prenom = ?, email = ?, password = ?, numtlf = ?, age = ?, roles = ? WHERE id = ?";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, passwordToUse);
            pstmt.setString(5, user.getNumtlf());
            pstmt.setInt(6, user.getAge());
            pstmt.setString(7, user.getRoles());
            pstmt.setInt(8, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }
    
    // Overloaded version of updateUser to match UserDAO interface
    public void updateUser(User user) {
        updateUser(user.getId(), user);
    }

    public void deleteUser(int id) {
        String sql = "DELETE FROM user WHERE id = ?";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }
    
    // Create password reset token
    public void createResetToken(int userId, String token, Timestamp expiration) {
        String query = "INSERT INTO password_reset_token (user_id, token, expiration, used) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, token);
            pstmt.setTimestamp(3, expiration);
            pstmt.setBoolean(4, false);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Find user by reset token
    public User findUserByResetToken(String token) {
        String query = "SELECT u.* FROM user u JOIN password_reset_token prt ON u.id = prt.user_id " +
                "WHERE prt.token = ? AND prt.expiration > CURRENT_TIMESTAMP AND (prt.used = FALSE OR prt.used IS NULL)";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setNumtlf(rs.getString("numtlf"));
                    user.setAge(rs.getInt("age"));
                    user.setRoles(rs.getString("roles"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Invalidate reset token
    public void invalidateResetToken(String token) {
        String query = "UPDATE password_reset_token SET used = TRUE WHERE token = ?";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update password by user id
    public void updatePasswordByUserId(int userId, String newPassword) {
        String query = "UPDATE user SET password = ? WHERE id = ?";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, hashPassword(newPassword));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Method to find a user by email
    public User findByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setNumtlf(rs.getString("numtlf"));
                    user.setAge(rs.getInt("age"));
                    user.setRoles(rs.getString("roles"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Method to list all users (alias for getAllUsers to match UserDAO interface)
    public List<User> listUsers() throws SQLException {
        return getAllUsers();
    }
    
    public User authenticateUser(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ?";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    
                    // Verify the password
                    if (verifyPassword(password, storedHash)) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setNom(rs.getString("nom"));
                        user.setPrenom(rs.getString("prenom"));
                        user.setEmail(rs.getString("email"));
                        user.setPassword(storedHash); // Store the hashed password
                        user.setNumtlf(rs.getString("numtlf"));
                        user.setAge(rs.getInt("age"));
                        user.setRoles(rs.getString("roles"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
} 
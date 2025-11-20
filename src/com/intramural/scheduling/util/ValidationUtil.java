package com.intramural.scheduling.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

public class ValidationUtil {
    
    // Regex patterns
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\d{10}$|^\\d{3}-\\d{3}-\\d{4}$");
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate phone number format
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleaned = phone.replaceAll("[^0-9]", "");
        return cleaned.length() == 10;
    }
    
    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasUpper && hasLower && hasDigit;
    }
    
    /**
     * Validate time range (start before end)
     */
    public static boolean isValidTimeRange(LocalTime start, LocalTime end) {
        return start != null && end != null && start.isBefore(end);
    }
    
    /**
     * Validate date range (start before or equal to end)
     */
    public static boolean isValidDateRange(LocalDate start, LocalDate end) {
        return start != null && end != null && !start.isAfter(end);
    }
    
    /**
     * Validate hours per week (0-40)
     */
    public static boolean isValidHoursPerWeek(int hours) {
        return hours >= 0 && hours <= 40;
    }
    
    /**
     * Validate performance rating (0.0-5.0)
     */
    public static boolean isValidPerformanceRating(double rating) {
        return rating >= 0.0 && rating <= 5.0;
    }
    
    /**
     * Check if string is not null or empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Validate positive number
     */
    public static boolean isPositive(int number) {
        return number > 0;
    }
    
    /**
     * Validate non-negative number
     */
    public static boolean isNonNegative(double number) {
        return number >= 0;
    }
    
    /**
     * Get validation error message for email
     */
    public static String getEmailError() {
        return "Invalid email format. Example: user@example.com";
    }
    
    /**
     * Get validation error message for phone
     */
    public static String getPhoneError() {
        return "Invalid phone format. Use 10 digits or XXX-XXX-XXXX";
    }
    
    /**
     * Get validation error message for username
     */
    public static String getUsernameError() {
        return "Username must be 3-20 characters (letters, numbers, underscore only)";
    }
    
    /**
     * Get validation error message for password
     */
    public static String getPasswordError() {
        return "Password must be at least 8 characters with uppercase, lowercase, and digit";
    }
    
    /**
     * Get validation error message for time range
     */
    public static String getTimeRangeError() {
        return "Start time must be before end time";
    }
    
    /**
     * Get validation error message for date range
     */
    public static String getDateRangeError() {
        return "Start date must be before or equal to end date";
    }
}
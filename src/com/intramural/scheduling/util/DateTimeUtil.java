package com.intramural.scheduling.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class DateTimeUtil {
    
    // Common date/time formatters
    public static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter TIME_FORMAT = 
        DateTimeFormatter.ofPattern("h:mm a");
    public static final DateTimeFormatter DATETIME_FORMAT = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a");
    public static final DateTimeFormatter SHORT_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    /**
     * Get the start of the week (Monday) for a given date
     */
    public static LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
    
    /**
     * Get the end of the week (Sunday) for a given date
     */
    public static LocalDate getWeekEnd(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
    
    /**
     * Format date for display
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }
    
    /**
     * Format time for display
     */
    public static String formatTime(LocalTime time) {
        return time.format(TIME_FORMAT);
    }
    
    /**
     * Format date and time for display
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMAT);
    }
    
    /**
     * Check if two time ranges overlap
     */
    public static boolean timeRangesOverlap(LocalTime start1, LocalTime end1,
                                           LocalTime start2, LocalTime end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2) ||
                 end1.equals(start2) || start1.equals(end2));
    }
    
    /**
     * Calculate duration in hours between two times
     */
    public static double calculateHours(LocalTime start, LocalTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        return minutes / 60.0;
    }
    
    /**
     * Check if a date is within a range (inclusive)
     */
    public static boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
    
    /**
     * Get all dates between start and end (inclusive)
     */
    public static java.util.List<LocalDate> getDateRange(LocalDate start, LocalDate end) {
        java.util.List<LocalDate> dates = new java.util.ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }
    
    /**
     * Get current season based on month
     */
    public static String getCurrentSeason() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 9 && month <= 12) return "FALL";
        if (month >= 1 && month <= 5) return "SPRING";
        return "SUMMER";
    }
    
    /**
     * Check if date is today
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }
    
    /**
     * Check if date is in the future
     */
    public static boolean isFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }
    
    /**
     * Check if date is in the past
     */
    public static boolean isPast(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }
    
    /**
     * Get day name (e.g., "Monday")
     */
    public static String getDayName(DayOfWeek day) {
        return day.toString().charAt(0) + day.toString().substring(1).toLowerCase();
    }
    
    /**
     * Parse date string in common format
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, SHORT_DATE_FORMAT);
        } catch (Exception e) {
            return LocalDate.parse(dateStr);
        }
    }
    
    /**
     * Parse time string in common format
     */
    public static LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, TIME_FORMAT);
        } catch (Exception e) {
            return LocalTime.parse(timeStr);
        }
    }
}
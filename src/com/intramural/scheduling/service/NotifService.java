package com.intramural.scheduling.service;

import com.intramural.scheduling.dao.NotificationDAO;
import com.intramural.scheduling.model.Employee;
import com.intramural.scheduling.model.Schedule;
import com.intramural.scheduling.model.Tracking;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationService {
    private NotificationDAO notificationDAO;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("h:mm a");
    
    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }
    
    /**
     * Notify employee of new shift assignment
     */
    public void notifyShiftAssignment(int userId, Schedule.Game game, 
                                     Schedule.Shift shift) throws SQLException {
        String message = String.format(
            "You have been assigned to %s on %s at %s. Location: %s",
            shift.getPositionLabel(),
            game.getGameDate().format(DATE_FORMATTER),
            game.getStartTime().format(TIME_FORMATTER),
            game.getLocation()
        );
        
        sendNotification(userId, message, Tracking.NotificationType.INFO);
    }
    
    /**
     * Notify employee of shift cancellation
     */
    public void notifyShiftCancellation(int userId, Schedule.Game game, 
                                       Schedule.Shift shift) throws SQLException {
        String message = String.format(
            "Your shift (%s) on %s at %s has been cancelled.",
            shift.getPositionLabel(),
            game.getGameDate().format(DATE_FORMATTER),
            game.getStartTime().format(TIME_FORMATTER)
        );
        
        sendNotification(userId, message, Tracking.NotificationType.WARNING);
    }
    
    /**
     * Notify employee of shift change
     */
    public void notifyShiftChange(int userId, Schedule.Game oldGame, 
                                 Schedule.Game newGame) throws SQLException {
        String message = String.format(
            "Your shift has been moved from %s at %s to %s at %s.",
            oldGame.getGameDate().format(DATE_FORMATTER),
            oldGame.getStartTime().format(TIME_FORMATTER),
            newGame.getGameDate().format(DATE_FORMATTER),
            newGame.getStartTime().format(TIME_FORMATTER)
        );
        
        sendNotification(userId, message, Tracking.NotificationType.WARNING);
    }
    
    /**
     * Notify employee approaching hour limit
     */
    public void notifyHourLimitApproaching(int userId, double currentHours, 
                                          int maxHours) throws SQLException {
        String message = String.format(
            "You are approaching your weekly hour limit (%.1f/%.1f hours used).",
            currentHours, (double) maxHours
        );
        
        sendNotification(userId, message, Tracking.NotificationType.WARNING);
    }
    
    /**
     * Notify time-off request approval
     */
    public void notifyTimeOffApproved(int userId, String dateRange) 
            throws SQLException {
        String message = String.format(
            "Your time-off request for %s has been approved.",
            dateRange
        );
        
        sendNotification(userId, message, Tracking.NotificationType.INFO);
    }
    
    /**
     * Notify time-off request denial
     */
    public void notifyTimeOffDenied(int userId, String dateRange, String reason) 
            throws SQLException {
        String message = String.format(
            "Your time-off request for %s has been denied. Reason: %s",
            dateRange, reason
        );
        
        sendNotification(userId, message, Tracking.NotificationType.WARNING);
    }
    
    /**
     * Notify schedule published
     */
    public void notifySchedulePublished(int userId, String cycleRange) 
            throws SQLException {
        String message = String.format(
            "The schedule for %s has been published. Check your assignments!",
            cycleRange
        );
        
        sendNotification(userId, message, Tracking.NotificationType.INFO);
    }
    
    /**
     * Send generic notification
     */
    public void sendNotification(int userId, String message, 
                                 Tracking.NotificationType type) 
                                 throws SQLException {
        Tracking.Notification notification = 
            new Tracking.Notification(userId, message, type);
        notificationDAO.insert(notification);
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<Tracking.Notification> getUnreadNotifications(int userId) 
            throws SQLException {
        return notificationDAO.getUnreadByUser(userId);
    }
    
    /**
     * Mark notification as read
     */
    public void markAsRead(int notificationId) throws SQLException {
        notificationDAO.markAsRead(notificationId);
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(int userId) throws SQLException {
        notificationDAO.markAllAsReadForUser(userId);
    }
    
    /**
     * Get notification count for user
     */
    public int getUnreadCount(int userId) throws SQLException {
        return notificationDAO.getUnreadCount(userId);
    }
    
    /**
     * Delete old notifications (cleanup utility)
     */
    public void deleteOldNotifications(int daysOld) throws SQLException {
        notificationDAO.deleteOlderThan(daysOld);
    }
    
    /**
     * Broadcast notification to multiple users
     */
    public void broadcastNotification(List<Integer> userIds, String message,
                                     Tracking.NotificationType type) 
                                     throws SQLException {
        for (int userId : userIds) {
            sendNotification(userId, message, type);
        }
    }
}
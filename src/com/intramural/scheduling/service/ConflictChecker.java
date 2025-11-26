package com.intramural.scheduling.service;

import com.intramural.scheduling.model.Availability;
import com.intramural.scheduling.model.Employee;
import com.intramural.scheduling.model.Schedule;
import com.intramural.scheduling.model.TimeOffRequest;
import com.intramural.scheduling.model.Tracking;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ConflictChecker {
    
    /**
     * Check all hard constraints for an employee assignment
     * Returns list of violation messages (empty if valid)
     */
    public List<String> checkHardConstraints(Employee employee, 
                                             Schedule.Game game,
                                             List<Availability.Seasonal> availability,
                                             List<Availability.PermanentConflict> conflicts,
                                             List<TimeOffRequest> approvedTimeOffs,
                                             List<Schedule.Game> employeeExistingGames,
                                             Tracking.WeeklyHours weeklyHours) {
        List<String> violations = new ArrayList<>();
        
        // Check if employee is active
        if (!employee.isActiveStatus()) {
            violations.add("Employee is not active");
            return violations;
        }
        
        // 1. Check seasonal availability
        if (!hasSeasonalAvailability(availability, game)) {
            violations.add("Outside seasonal availability window");
        }
        
        // 2. Check permanent conflicts
        if (hasPermanentConflict(conflicts, game)) {
            violations.add("Conflicts with permanent schedule (class/job)");
        }
        
        // 3. Check approved time-off
        if (hasApprovedTimeOff(approvedTimeOffs, game)) {
            violations.add("Has approved time-off for this date/time");
        }
        
        // 4. Check for double-booking
        if (hasTimeOverlap(employeeExistingGames, game)) {
            violations.add("Already scheduled during this time");
        }
        
        // 5. Check weekly hour limit
        double gameHours = game.getDurationHours();
        if (weeklyHours != null && 
            !weeklyHours.canAccommodate(gameHours, employee.getMaxHoursPerWeek())) {
            violations.add(String.format(
                "Exceeds weekly limit (%.1f/%.1f hours)", 
                weeklyHours.getTotalScheduledHours() + gameHours,
                employee.getMaxHoursPerWeek()
            ));
        }
        
        return violations;
    }
    
    /**
     * Check if employee has seasonal availability for this game
     * MVP MODE: Empty availability list = assume available
     */
    private boolean hasSeasonalAvailability(List<Availability.Seasonal> availability,
                                           Schedule.Game game) {
        // MVP MODE: If no availability data provided, assume employee is available
        // This allows the system to work without detailed availability configuration
        if (availability == null || availability.isEmpty()) {
            System.out.println("[MVP MODE] No availability data - assuming employee is available");
            return true;  // ✅ FIXED: Changed from false to true for MVP
        }
        
        LocalDateTime gameStart = game.getStartDateTime();
        LocalTime startTime = game.getStartTime();
        LocalTime endTime = game.getEndTime();
        
        return availability.stream().anyMatch(avail -> 
            avail.getDayOfWeek() == gameStart.getDayOfWeek() &&
            avail.overlapsWith(startTime, endTime)
        );
    }
    
    /**
     * Check if game conflicts with permanent conflicts
     */
    private boolean hasPermanentConflict(List<Availability.PermanentConflict> conflicts,
                                        Schedule.Game game) {
        if (conflicts == null || conflicts.isEmpty()) {
            return false;
        }
        
        LocalDateTime gameStart = game.getStartDateTime();
        LocalTime gameEnd = game.getEndTime();
        
        return conflicts.stream().anyMatch(conflict ->
            conflict.conflictsWith(gameStart, gameEnd)
        );
    }
    
    /**
     * Check if employee has approved time-off during this game
     */
    private boolean hasApprovedTimeOff(List<TimeOffRequest> timeOffs,
                                      Schedule.Game game) {
        if (timeOffs == null || timeOffs.isEmpty()) {
            return false;
        }
        
        return timeOffs.stream()
            .filter(req -> req.getStatus() == TimeOffRequest.Status.APPROVED)
            .anyMatch(req -> req.conflictsWith(
                game.getStartDateTime(), 
                game.getEndDateTime()
            ));
    }
    
    /**
     * Check if employee is already scheduled during this time
     */
    private boolean hasTimeOverlap(List<Schedule.Game> existingGames, 
                                  Schedule.Game newGame) {
        if (existingGames == null || existingGames.isEmpty()) {
            return false;
        }
        
        LocalDateTime newStart = newGame.getStartDateTime();
        LocalDateTime newEnd = newGame.getEndDateTime();
        
        return existingGames.stream().anyMatch(existing -> {
            LocalDateTime existingStart = existing.getStartDateTime();
            LocalDateTime existingEnd = existing.getEndDateTime();
            
            // Check for overlap: games overlap if one starts before the other ends
            return !(newEnd.isBefore(existingStart) || 
                    newStart.isAfter(existingEnd) ||
                    newEnd.equals(existingStart) || 
                    newStart.equals(existingEnd));
        });
    }
    
    /**
     * Quick validation - returns true if employee can be assigned
     */
    public boolean isValidAssignment(Employee employee, 
                                    Schedule.Game game,
                                    List<Availability.Seasonal> availability,
                                    List<Availability.PermanentConflict> conflicts,
                                    List<TimeOffRequest> approvedTimeOffs,
                                    List<Schedule.Game> employeeExistingGames,
                                    Tracking.WeeklyHours weeklyHours) {
        List<String> violations = checkHardConstraints(
            employee, game, availability, conflicts, 
            approvedTimeOffs, employeeExistingGames, weeklyHours
        );
        
        return violations.isEmpty();
    }
}
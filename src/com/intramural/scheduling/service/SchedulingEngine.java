package com.intramural.scheduling.service;

import com.intramural.scheduling.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class SchedulingEngine {
    private ConflictChecker conflictChecker;
    
    // Scoring weights for soft constraints
    private static final double WEIGHT_EXPERTISE = 30.0;
    private static final double WEIGHT_PERFORMANCE = 20.0;
    private static final double WEIGHT_HOUR_BALANCE = 25.0;
    private static final double WEIGHT_PREFERENCE = 15.0;
    private static final double WEIGHT_EXPERIENCE = 10.0;
    
    // NEW: Penalty for being already recommended
    private static final double PENALTY_ALREADY_RECOMMENDED = 50.0;
    
    public SchedulingEngine() {
        this.conflictChecker = new ConflictChecker();
    }
    
    /**
     * ORIGINAL METHOD: Kept for backward compatibility
     */
    public List<SchedulingRecommendation> generateRecommendations(
            Schedule.Shift shift,
            Schedule.Game game,
            Sport sport,
            List<Employee> eligibleEmployees,
            Map<Integer, List<Availability.Seasonal>> availabilityMap,
            Map<Integer, List<Availability.PermanentConflict>> conflictsMap,
            Map<Integer, List<TimeOffRequest>> timeOffMap,
            Map<Integer, List<Schedule.Game>> existingGamesMap,
            Map<Integer, Tracking.WeeklyHours> weeklyHoursMap) {
        
        // Call new method with empty exclusion set
        return generateRecommendationsWithExclusions(
            shift, game, sport, eligibleEmployees,
            availabilityMap, conflictsMap, timeOffMap,
            existingGamesMap, weeklyHoursMap,
            new HashSet<>()
        );
    }
    
    /**
     * NEW METHOD: Generate recommendations with employee exclusion tracking
     * This prevents the same employees from being recommended for all positions
     */
    public List<SchedulingRecommendation> generateRecommendationsWithExclusions(
            Schedule.Shift shift,
            Schedule.Game game,
            Sport sport,
            List<Employee> eligibleEmployees,
            Map<Integer, List<Availability.Seasonal>> availabilityMap,
            Map<Integer, List<Availability.PermanentConflict>> conflictsMap,
            Map<Integer, List<TimeOffRequest>> timeOffMap,
            Map<Integer, List<Schedule.Game>> existingGamesMap,
            Map<Integer, Tracking.WeeklyHours> weeklyHoursMap,
            Set<Integer> alreadyRecommendedAsOptionA) {
        
        List<SchedulingRecommendation> recommendations = new ArrayList<>();
        
        // Filter by position type eligibility
        List<Employee> filtered = eligibleEmployees.stream()
            .filter(emp -> {
                if (shift.getPositionType() == Schedule.PositionType.SUPERVISOR) {
                    return emp.isSupervisorEligible();
                }
                return true; // All employees can be referees
            })
            .collect(Collectors.toList());
        
        System.out.println("Eligible employees after position filter: " + filtered.size());
        
        // Score each employee
        for (Employee employee : filtered) {
            SchedulingRecommendation rec = scoreEmployee(
                employee, 
                shift, 
                game, 
                sport,
                availabilityMap.getOrDefault(employee.getEmployeeId(), new ArrayList<>()),
                conflictsMap.getOrDefault(employee.getEmployeeId(), new ArrayList<>()),
                timeOffMap.getOrDefault(employee.getEmployeeId(), new ArrayList<>()),
                existingGamesMap.getOrDefault(employee.getEmployeeId(), new ArrayList<>()),
                weeklyHoursMap.get(employee.getEmployeeId())
            );
            
            // NEW: Apply penalty if already recommended as Option A
            if (alreadyRecommendedAsOptionA.contains(employee.getEmployeeId())) {
                rec.addScoreComponent("Already Recommended Penalty", -PENALTY_ALREADY_RECOMMENDED);
                rec.addWarning("Already recommended for another position in this game");
                System.out.println("  Applied penalty to " + employee.getFirstName() + " " + 
                                 employee.getLastName() + " (already recommended)");
            }
            
            // Only include valid recommendations (no hard constraint violations)
            if (rec.isValid()) {
                recommendations.add(rec);
                System.out.println("  " + employee.getFirstName() + " " + employee.getLastName() + 
                                 " - Score: " + String.format("%.2f", rec.getScore()));
            } else {
                System.out.println("  " + employee.getFirstName() + " " + employee.getLastName() + 
                                 " - REJECTED: " + rec.getViolations());
            }
        }
        
        // Sort by score (descending)
        recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        // Return top 2
        return recommendations.stream()
            .limit(2)
            .collect(Collectors.toList());
    }
    
    /**
     * Score an employee for a specific shift using soft constraints
     */
    private SchedulingRecommendation scoreEmployee(
            Employee employee,
            Schedule.Shift shift,
            Schedule.Game game,
            Sport sport,
            List<Availability.Seasonal> availability,
            List<Availability.PermanentConflict> conflicts,
            List<TimeOffRequest> timeOffs,
            List<Schedule.Game> existingGames,
            Tracking.WeeklyHours weeklyHours) {
        
        SchedulingRecommendation rec = new SchedulingRecommendation(employee);
        
        // Check hard constraints first
        List<String> violations = conflictChecker.checkHardConstraints(
            employee, game, availability, conflicts, timeOffs, 
            existingGames, weeklyHours
        );
        
        if (!violations.isEmpty()) {
            violations.forEach(rec::addViolation);
            return rec;
        }
        
        // Calculate soft constraint scores
        
        // 1. Sport expertise score
        double expertiseScore = calculateExpertiseScore(employee, sport.getSportId());
        rec.addScoreComponent("Expertise", expertiseScore * WEIGHT_EXPERTISE);
        
        // 2. Performance rating score
        double performanceScore = employee.getPerformanceRating() / 5.0; // Normalize to 0-1
        rec.addScoreComponent("Performance", performanceScore * WEIGHT_PERFORMANCE);
        
        // 3. Hour balance score (prefer employees with fewer hours)
        double balanceScore = calculateHourBalanceScore(
            weeklyHours, 
            employee.getMaxHoursPerWeek()
        );
        rec.addScoreComponent("Hour Balance", balanceScore * WEIGHT_HOUR_BALANCE);
        
        // 4. Availability preference score
        double preferenceScore = calculatePreferenceScore(availability, game);
        rec.addScoreComponent("Preference Match", preferenceScore * WEIGHT_PREFERENCE);
        
        // 5. Experience score (based on past assignments)
        double experienceScore = calculateExperienceScore(existingGames);
        rec.addScoreComponent("Experience", experienceScore * WEIGHT_EXPERIENCE);
        
        // Add warnings for near-limit situations
        if (weeklyHours != null && 
            weeklyHours.isApproachingLimit(employee.getMaxHoursPerWeek(), 0.85)) {
            rec.addWarning(String.format(
                "Approaching weekly limit (%.1f/%.1f hours)",
                weeklyHours.getTotalScheduledHours(),
                employee.getMaxHoursPerWeek()
            ));
        }
        
        return rec;
    }
    
    /**
     * Calculate expertise score (0.0 - 1.0)
     */
    private double calculateExpertiseScore(Employee employee, int sportId) {
        if (!employee.hasExpertiseIn(sportId)) {
            return 0.0;
        }
        
        Employee.ExpertiseLevel level = employee.getExpertiseLevel(sportId);
        switch (level) {
            case EXPERT: return 1.0;
            case INTERMEDIATE: return 0.7;
            case BEGINNER: return 0.4;
            default: return 0.0;
        }
    }
    
    /**
     * Calculate hour balance score (0.0 - 1.0)
     * Higher score for employees with fewer scheduled hours
     */
    private double calculateHourBalanceScore(Tracking.WeeklyHours weeklyHours, 
                                            int maxHours) {
        if (weeklyHours == null || maxHours == 0) {
            return 1.0; // No hours scheduled yet
        }
        
        double utilization = weeklyHours.getTotalScheduledHours() / maxHours;
        return Math.max(0.0, 1.0 - utilization);
    }
    
    /**
     * Calculate preference score (0.0 - 1.0)
     * Higher score if the shift falls during preferred availability
     */
    private double calculatePreferenceScore(List<Availability.Seasonal> availability,
                                           Schedule.Game game) {
        if (availability == null || availability.isEmpty()) {
            return 0.5;
        }
        
        boolean hasPreferredSlot = availability.stream()
            .filter(Availability.Seasonal::isPreferred)
            .anyMatch(avail -> 
                avail.getDayOfWeek() == game.getStartDateTime().getDayOfWeek() &&
                avail.overlapsWith(game.getStartTime(), game.getEndTime())
            );
        
        return hasPreferredSlot ? 1.0 : 0.5;
    }
    
    /**
     * Calculate experience score (0.0 - 1.0)
     * Based on number of previous assignments
     */
    private double calculateExperienceScore(List<Schedule.Game> existingGames) {
        if (existingGames == null) {
            return 0.3;
        }
        
        int assignmentCount = existingGames.size();
        
        // Normalize: 0 shifts = 0.3, 10+ shifts = 1.0
        if (assignmentCount == 0) return 0.3;
        if (assignmentCount >= 10) return 1.0;
        
        return 0.3 + (assignmentCount / 10.0) * 0.7;
    }
    
    /**
     * Batch process all shifts for a scheduling cycle
     */
    public Map<Integer, List<SchedulingRecommendation>> generateAllRecommendations(
            Schedule.Cycle cycle,
            List<Employee> allEmployees,
            Map<Integer, Sport> sportsMap,
            Map<Integer, List<Availability.Seasonal>> availabilityMap,
            Map<Integer, List<Availability.PermanentConflict>> conflictsMap,
            Map<Integer, List<TimeOffRequest>> timeOffMap,
            Map<Integer, Tracking.WeeklyHours> weeklyHoursMap) {
        
        Map<Integer, List<SchedulingRecommendation>> allRecommendations = new HashMap<>();
        Map<Integer, List<Schedule.Game>> existingGamesMap = new HashMap<>();
        
        // Process each game schedule in the cycle
        for (Schedule.Game game : cycle.getGameSchedules()) {
            Sport sport = sportsMap.get(game.getSportId());
            
            // Track already-recommended employees for this game
            Set<Integer> alreadyRecommendedAsOptionA = new HashSet<>();
            
            // Process each shift in the game
            for (Schedule.Shift shift : game.getShifts()) {
                List<SchedulingRecommendation> recs = generateRecommendationsWithExclusions(
                    shift,
                    game,
                    sport,
                    allEmployees,
                    availabilityMap,
                    conflictsMap,
                    timeOffMap,
                    existingGamesMap,
                    weeklyHoursMap,
                    alreadyRecommendedAsOptionA
                );
                
                allRecommendations.put(shift.getShiftId(), recs);
                
                // Update recommendations in shift and track Option A
                if (recs.size() >= 1) {
                    int optionAId = recs.get(0).getEmployee().getEmployeeId();
                    int optionBId = recs.size() >= 2 ? 
                        recs.get(1).getEmployee().getEmployeeId() : optionAId;
                    shift.setRecommendations(optionAId, optionBId);
                    
                    // Track Option A to avoid in next shifts
                    alreadyRecommendedAsOptionA.add(optionAId);
                }
            }
        }
        
        return allRecommendations;
    }
    
    /**
     * Get conflict checker instance
     */
    public ConflictChecker getConflictChecker() {
        return conflictChecker;
    }
}
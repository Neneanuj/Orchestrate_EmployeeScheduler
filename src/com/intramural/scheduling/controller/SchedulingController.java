package com.intramural.scheduling.controller;

import com.intramural.scheduling.dao.*;
import com.intramural.scheduling.model.*;
import com.intramural.scheduling.service.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class SchedulingController {
    private SchedulingEngine schedulingEngine;
    private GameScheduleDAO gameScheduleDAO;
    private ShiftDAO shiftDAO;
    private EmployeeDAO employeeDAO;
    private SportDAO sportDAO;
    private WeeklyHoursDAO weeklyHoursDAO;
    private HoursTracker hoursTracker;
    
    private Schedule.Cycle currentCycle;
    private Map<Integer, List<SchedulingRecommendation>> recommendations;
    
    public SchedulingController() {
        this.schedulingEngine = new SchedulingEngine();
        this.gameScheduleDAO = new GameScheduleDAO();
        this.shiftDAO = new ShiftDAO();
        this.employeeDAO = new EmployeeDAO();
        this.sportDAO = new SportDAO();
        this.weeklyHoursDAO = new WeeklyHoursDAO();
        this.hoursTracker = new HoursTracker();
        this.recommendations = new HashMap<>();
    }
    
    public void createCycle(LocalDate startDate, LocalDate endDate) {
        this.currentCycle = new Schedule.Cycle(startDate, endDate);
    }
    
    public Schedule.Cycle getCurrentCycle() {
        return currentCycle;
    }
    
    /**
     * Auto-generate recommendations for a single game - SIMPLIFIED FOR MVP
     */
    public void autoGenerateRecommendations(Schedule.Game game) throws SQLException {
        System.out.println("=== Auto-Generating Recommendations ===");
        System.out.println("Game: " + game.getGameDate() + " at " + game.getLocation());
        
        // Ensure game has shifts generated
        game.generateShifts();
        
        // Load shifts from database
        List<Schedule.Shift> dbShifts = shiftDAO.getByGameSchedule(game.getScheduleId());
        if (!dbShifts.isEmpty()) {
            game.getShifts().clear();
            game.getShifts().addAll(dbShifts);
        }
        
        System.out.println("Total shifts: " + game.getShifts().size());
        
        // Get all active employees
        List<Employee> allEmployees = employeeDAO.getAllActive();
        System.out.println("Active employees: " + allEmployees.size());
        
        if (allEmployees.isEmpty()) {
            System.out.println("No employees available for recommendations");
            throw new SQLException("No active employees found. Please add employees first.");
        }
        
        // Get sport info
        Sport sport = sportDAO.getById(game.getSportId());
        
        // Simple data maps - NO availability/conflicts for MVP
        Map<Integer, List<Availability.Seasonal>> availabilityMap = new HashMap<>();
        Map<Integer, List<Availability.PermanentConflict>> conflictsMap = new HashMap<>();
        Map<Integer, List<TimeOffRequest>> timeOffMap = new HashMap<>();
        Map<Integer, List<Schedule.Game>> existingGamesMap = new HashMap<>();
        Map<Integer, Tracking.WeeklyHours> weeklyHoursMap = new HashMap<>();
        
        // Initialize empty maps for each employee
        for (Employee emp : allEmployees) {
            availabilityMap.put(emp.getEmployeeId(), new ArrayList<>());
            conflictsMap.put(emp.getEmployeeId(), new ArrayList<>());
            timeOffMap.put(emp.getEmployeeId(), new ArrayList<>());
            existingGamesMap.put(emp.getEmployeeId(), new ArrayList<>());
            
            // Get weekly hours
            try {
                LocalDate weekStart = hoursTracker.getWeekStartDate(game.getGameDate());
                Tracking.WeeklyHours hours = weeklyHoursDAO.getByEmployeeAndWeek(
                    emp.getEmployeeId(), weekStart);
                weeklyHoursMap.put(emp.getEmployeeId(), hours);
            } catch (Exception e) {
                Tracking.WeeklyHours hours = new Tracking.WeeklyHours(
                    emp.getEmployeeId(), game.getGameDate());
                weeklyHoursMap.put(emp.getEmployeeId(), hours);
            }
        }
        
        // Generate recommendations for each shift
        int recommendationsGenerated = 0;
        for (Schedule.Shift shift : game.getShifts()) {
            System.out.println("\n--- Generating for Shift " + shift.getShiftId() + 
                             " (" + shift.getPositionType() + " #" + shift.getPositionNumber() + ") ---");
            
            List<SchedulingRecommendation> recs = schedulingEngine.generateRecommendations(
                shift, game, sport, allEmployees,
                availabilityMap, conflictsMap, timeOffMap,
                existingGamesMap, weeklyHoursMap
            );
            
            System.out.println("Generated " + recs.size() + " recommendations");
            
            if (recs.size() >= 1) {
                Integer optionA = recs.get(0).getEmployee().getEmployeeId();
                Integer optionB = recs.size() >= 2 ? 
                    recs.get(1).getEmployee().getEmployeeId() : optionA;
                
                shift.setRecommendations(optionA, optionB);
                
                System.out.println("Option A: " + recs.get(0).getEmployee().getFirstName() + 
                                 " " + recs.get(0).getEmployee().getLastName() + 
                                 " (Score: " + String.format("%.2f", recs.get(0).getScore()) + ")");
                
               if (recs.size() >= 2 && recs.get(1).getEmployee().getEmployeeId() != optionA) {
                    System.out.println("Option B: " + recs.get(1).getEmployee().getFirstName() + 
                                     " " + recs.get(1).getEmployee().getLastName() + 
                                     " (Score: " + String.format("%.2f", recs.get(1).getScore()) + ")");
                }
                
                // Save to database
                shiftDAO.updateRecommendations(
                    shift.getShiftId(), optionA, optionB
                );
                
                // Store recommendations
                recommendations.put(shift.getShiftId(), recs);
                recommendationsGenerated++;
            } else {
                System.out.println("No valid recommendations found for this shift");
            }
        }
        
        System.out.println("\n=== Summary ===");
        System.out.println("Recommendations generated for " + recommendationsGenerated + 
                         " out of " + game.getShifts().size() + " shifts");
    }
    
    /**
     * Generate recommendations for all shifts in current cycle
     */
    public void generateRecommendations() throws SQLException {
        if (currentCycle == null) {
            throw new IllegalStateException("No active cycle");
        }
        
        System.out.println("Generating recommendations for all shifts in cycle...");
        
        // Reload games from database
        loadGameSchedules();
        
        for (Schedule.Game game : currentCycle.getGameSchedules()) {
            autoGenerateRecommendations(game);
        }
        
        System.out.println("All recommendations generated!");
    }
    
    public List<Schedule.Game> loadGameSchedules() throws SQLException {
        if (currentCycle == null) {
            return new ArrayList<>();
        }
        
        List<Schedule.Game> games = gameScheduleDAO.getByCycle(
            currentCycle.getCycleStart(),
            currentCycle.getCycleEnd()
        );
        
        for (Schedule.Game game : games) {
            game.generateShifts();
            
            // Load shifts from database
            List<Schedule.Shift> shifts = shiftDAO.getByGameSchedule(game.getScheduleId());
            game.getShifts().clear();
            game.getShifts().addAll(shifts);
        }
        
        currentCycle.getGameSchedules().clear();
        currentCycle.getGameSchedules().addAll(games);
        
        return games;
    }
    
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        if (currentCycle == null) {
            return stats;
        }
        
        List<Schedule.Game> games = currentCycle.getGameSchedules();
        int totalGames = games.size();
        int fullyStaffed = 0;
        int partiallyStaffed = 0;
        int unstaffed = 0;
        
        for (Schedule.Game game : games) {
            int assigned = game.getAssignedStaffCount();
            int total = game.getTotalStaffNeeded();
            
            if (assigned == total) {
                fullyStaffed++;
            } else if (assigned > 0) {
                partiallyStaffed++;
            } else {
                unstaffed++;
            }
        }
        
        stats.put("total", totalGames);
        stats.put("fullyStaffed", fullyStaffed);
        stats.put("partiallyStaffed", partiallyStaffed);
        stats.put("unstaffed", unstaffed);
        
        return stats;
    }
    
    public List<SchedulingRecommendation> getRecommendations(int shiftId) {
        return recommendations.getOrDefault(shiftId, new ArrayList<>());
    }
}
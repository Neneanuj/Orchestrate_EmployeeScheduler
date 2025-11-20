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
    private EmployeeDAO employeeDAO;
    private AvailabilityDAO availabilityDAO;
    private TimeOffDAO timeOffDAO;
    private WeeklyHoursDAO weeklyHoursDAO;
    private HoursTracker hoursTracker;
    
    private Schedule.Cycle currentCycle;
    private Map<Integer, List<SchedulingRecommendation>> recommendations;
    
    public SchedulingController() {
        this.schedulingEngine = new SchedulingEngine();
        this.gameScheduleDAO = new GameScheduleDAO();
        this.employeeDAO = new EmployeeDAO();
        this.availabilityDAO = new AvailabilityDAO();
        this.timeOffDAO = new TimeOffDAO();
        this.weeklyHoursDAO = new WeeklyHoursDAO();
        this.hoursTracker = new HoursTracker();
        this.recommendations = new HashMap<>();
    }
    
    /**
     * Create new scheduling cycle
     */
    public void createCycle(LocalDate startDate, LocalDate endDate) {
        this.currentCycle = new Schedule.Cycle(startDate, endDate);
    }
    
    /**
     * Get current cycle
     */
    public Schedule.Cycle getCurrentCycle() {
        return currentCycle;
    }
    
    /**
     * Load game schedules for current cycle
     */
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
        }
        
        currentCycle.getGameSchedules().clear();
        currentCycle.getGameSchedules().addAll(games);
        
        return games;
    }
    
    /**
     * Generate recommendations for all shifts
     */
    public void generateRecommendations() throws SQLException {
        if (currentCycle == null) {
            throw new IllegalStateException("No active cycle");
        }
        
        List<Employee> allEmployees = employeeDAO.getAllActive();
        
        Map<Integer, List<Availability.Seasonal>> availabilityMap = new HashMap<>();
        Map<Integer, List<Availability.PermanentConflict>> conflictsMap = new HashMap<>();
        Map<Integer, List<TimeOffRequest>> timeOffMap = new HashMap<>();
        Map<Integer, Tracking.WeeklyHours> weeklyHoursMap = new HashMap<>();
        
        Availability.Season currentSeason = getCurrentSeason();
        int currentYear = currentCycle.getCycleStart().getYear();
        
        for (Employee emp : allEmployees) {
            int empId = emp.getEmployeeId();
            
            availabilityMap.put(empId, 
                availabilityDAO.getByEmployee(empId, currentSeason, currentYear));
            
            conflictsMap.put(empId,
                availabilityDAO.getConflictsByEmployee(empId));
            
            timeOffMap.put(empId, 
                timeOffDAO.getApprovedByEmployee(empId, 
                    currentCycle.getCycleStart(), 
                    currentCycle.getCycleEnd()));
            
            LocalDate weekStart = HoursTracker.getWeekStartDate(
                currentCycle.getCycleStart());
            weeklyHoursMap.put(empId, 
                weeklyHoursDAO.getByEmployeeAndWeek(empId, weekStart));
        }
        
        Map<Integer, Sport> sportsMap = new HashMap<>();
        // Load sports data
        
        this.recommendations = schedulingEngine.generateAllRecommendations(
            currentCycle,
            allEmployees,
            sportsMap,
            availabilityMap,
            conflictsMap,
            timeOffMap,
            weeklyHoursMap
        );
    }
    
    /**
     * Assign employee to shift
     */
    public void assignShift(Schedule.Shift shift, int employeeId, 
                           Schedule.Game game) throws SQLException {
        shift.assignEmployee(employeeId);
        hoursTracker.assignShift(employeeId, game);
    }
    
    /**
     * Unassign shift
     */
    public void unassignShift(Schedule.Shift shift, Schedule.Game game) 
            throws SQLException {
        if (shift.getAssignedEmployeeId() != null) {
            int employeeId = shift.getAssignedEmployeeId();
            shift.unassign();
            hoursTracker.unassignShift(employeeId, game);
        }
    }
    
    /**
     * Get recommendations for shift
     */
    public List<SchedulingRecommendation> getRecommendations(int shiftId) {
        return recommendations.getOrDefault(shiftId, new ArrayList<>());
    }
    
    /**
     * Publish schedule
     */
    public void publishSchedule() {
        if (currentCycle != null) {
            currentCycle.setPublished(true);
        }
    }
    
    /**
     * Get cycle statistics
     */
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
    
    private Availability.Season getCurrentSeason() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 9 && month <= 12) return Availability.Season.FALL;
        if (month >= 1 && month <= 5) return Availability.Season.SPRING;
        return Availability.Season.SUMMER;
    }
}
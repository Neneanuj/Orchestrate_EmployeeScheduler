package com.intramural.scheduling.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Schedule {
    
    public enum PositionType {
        SUPERVISOR, REFEREE
    }
    
    public enum AssignmentStatus {
        UNASSIGNED, RECOMMENDED, ASSIGNED, CONFIRMED
    }
    
    // Game Schedule
    public static class Game {
        private int scheduleId;
        private int sportId;
        private LocalDate gameDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private int requiredSupervisors;
        private int requiredReferees;
        private LocalDate scheduleCycleStart;
        private LocalDate scheduleCycleEnd;
        private int createdBy;
        private LocalDateTime createdAt;
        private List<Shift> shifts;
        
        public Game(int sportId, LocalDate gameDate, LocalTime startTime,
                   LocalTime endTime, String location, int requiredSupervisors,
                   int requiredReferees, LocalDate cycleStart, 
                   LocalDate cycleEnd, int createdBy) {
            this.sportId = sportId;
            this.gameDate = gameDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.requiredSupervisors = requiredSupervisors;
            this.requiredReferees = requiredReferees;
            this.scheduleCycleStart = cycleStart;
            this.scheduleCycleEnd = cycleEnd;
            this.createdBy = createdBy;
            this.createdAt = LocalDateTime.now();
            this.shifts = new ArrayList<>();
        }
        
        public LocalDateTime getStartDateTime() {
            return LocalDateTime.of(gameDate, startTime);
        }
        
        public LocalDateTime getEndDateTime() {
            return LocalDateTime.of(gameDate, endTime);
        }
        
        public double getDurationHours() {
            long minutes = Duration.between(startTime, endTime).toMinutes();
            return minutes / 60.0;
        }
        
        public void generateShifts() {
            shifts.clear();
            for (int i = 1; i <= requiredSupervisors; i++) {
                shifts.add(new Shift(scheduleId, PositionType.SUPERVISOR, i));
            }
            for (int i = 1; i <= requiredReferees; i++) {
                shifts.add(new Shift(scheduleId, PositionType.REFEREE, i));
            }
        }
        
        public int getTotalStaffNeeded() {
            return requiredSupervisors + requiredReferees;
        }
        
        public int getAssignedStaffCount() {
            return (int) shifts.stream()
                .filter(shift -> shift.getAssignmentStatus() == AssignmentStatus.ASSIGNED)
                .count();
        }
        
        public boolean isFullyStaffed() {
            return getAssignedStaffCount() == getTotalStaffNeeded();
        }
        
        // Getters and setters
        public int getScheduleId() { return scheduleId; }
        public void setScheduleId(int id) { this.scheduleId = id; }
        public int getSportId() { return sportId; }
        public LocalDate getGameDate() { return gameDate; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public String getLocation() { return location; }
        public int getRequiredSupervisors() { return requiredSupervisors; }
        public int getRequiredReferees() { return requiredReferees; }
        public LocalDate getScheduleCycleStart() { return scheduleCycleStart; }
        public LocalDate getScheduleCycleEnd() { return scheduleCycleEnd; }
        public int getCreatedBy() { return createdBy; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public List<Shift> getShifts() { return shifts; }
    }
    
    // Shift
    public static class Shift {
        private int shiftId;
        private int gameScheduleId;
        private PositionType positionType;
        private int positionNumber;
        private Integer assignedEmployeeId;
        private Integer recommendationAId;
        private Integer recommendationBId;
        private AssignmentStatus assignmentStatus;
        private LocalDateTime assignedAt;
        
        private Employee assignedEmployee;
        private Employee recommendationA;
        private Employee recommendationB;
        
        public Shift(int gameScheduleId, PositionType positionType, int positionNumber) {
            this.gameScheduleId = gameScheduleId;
            this.positionType = positionType;
            this.positionNumber = positionNumber;
            this.assignmentStatus = AssignmentStatus.UNASSIGNED;
        }
        
        public void assignEmployee(int employeeId) {
            this.assignedEmployeeId = employeeId;
            this.assignmentStatus = AssignmentStatus.ASSIGNED;
            this.assignedAt = LocalDateTime.now();
        }
        
        public void unassign() {
            this.assignedEmployeeId = null;
            this.assignmentStatus = AssignmentStatus.UNASSIGNED;
            this.assignedAt = null;
        }
        
        public void setRecommendations(int optionAId, int optionBId) {
            this.recommendationAId = optionAId;
            this.recommendationBId = optionBId;
            if (this.assignmentStatus == AssignmentStatus.UNASSIGNED) {
                this.assignmentStatus = AssignmentStatus.RECOMMENDED;
            }
        }
        
        public boolean isAssigned() {
            return assignmentStatus == AssignmentStatus.ASSIGNED || 
                   assignmentStatus == AssignmentStatus.CONFIRMED;
        }
        
        public String getPositionLabel() {
            return positionType.toString() + " " + positionNumber;
        }
        
        // Getters and setters
        public int getShiftId() { return shiftId; }
        public void setShiftId(int id) { this.shiftId = id; }
        public int getGameScheduleId() { return gameScheduleId; }
        public PositionType getPositionType() { return positionType; }
        public int getPositionNumber() { return positionNumber; }
        public Integer getAssignedEmployeeId() { return assignedEmployeeId; }
        public Integer getRecommendationAId() { return recommendationAId; }
        public Integer getRecommendationBId() { return recommendationBId; }
        public AssignmentStatus getAssignmentStatus() { return assignmentStatus; }
        public void setAssignmentStatus(AssignmentStatus status) { 
            this.assignmentStatus = status; 
        }
        public LocalDateTime getAssignedAt() { return assignedAt; }
        public Employee getAssignedEmployee() { return assignedEmployee; }
        public void setAssignedEmployee(Employee emp) { this.assignedEmployee = emp; }
        public Employee getRecommendationA() { return recommendationA; }
        public void setRecommendationA(Employee emp) { this.recommendationA = emp; }
        public Employee getRecommendationB() { return recommendationB; }
        public void setRecommendationB(Employee emp) { this.recommendationB = emp; }
    }
    
    // Scheduling Cycle
    public static class Cycle {
        private LocalDate cycleStart;
        private LocalDate cycleEnd;
        private LocalDate timeOffDeadline;
        private LocalDate schedulePublishDate;
        private boolean isPublished;
        private List<Game> gameSchedules;
        
        public Cycle(LocalDate cycleStart, LocalDate cycleEnd) {
            this.cycleStart = cycleStart;
            this.cycleEnd = cycleEnd;
            this.timeOffDeadline = cycleStart.minusDays(3);
            this.schedulePublishDate = cycleStart.minusDays(2);
            this.isPublished = false;
            this.gameSchedules = new ArrayList<>();
        }
        
        public boolean isTimeOffWindowOpen() {
            return LocalDate.now().isBefore(timeOffDeadline);
        }
        
        public boolean containsDate(LocalDate date) {
            return !date.isBefore(cycleStart) && !date.isAfter(cycleEnd);
        }
        
        public long getDaysInCycle() {
            return Duration.between(
                cycleStart.atStartOfDay(), 
                cycleEnd.atStartOfDay()
            ).toDays() + 1;
        }
        
        // Getters and setters
        public LocalDate getCycleStart() { return cycleStart; }
        public LocalDate getCycleEnd() { return cycleEnd; }
        public LocalDate getTimeOffDeadline() { return timeOffDeadline; }
        public LocalDate getSchedulePublishDate() { return schedulePublishDate; }
        public boolean isPublished() { return isPublished; }
        public void setPublished(boolean published) { this.isPublished = published; }
        public List<Game> getGameSchedules() { return gameSchedules; }
        public void addGameSchedule(Game schedule) { 
            gameSchedules.add(schedule); 
        }
    }
}
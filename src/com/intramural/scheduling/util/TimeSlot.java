package com.intramural.scheduling.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with time slots in scheduling
 */
public class TimeSlot {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public TimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public DayOfWeek getDay() { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    
    /**
     * Check if this time slot overlaps with another
     */
    public boolean overlapsWith(TimeSlot other) {
        if (this.day != other.day) {
            return false;
        }
        return DateTimeUtil.timeRangesOverlap(
            this.startTime, this.endTime,
            other.startTime, other.endTime
        );
    }
    
    /**
     * Get duration in hours
     */
    public double getDurationHours() {
        return DateTimeUtil.calculateHours(startTime, endTime);
    }
    
    /**
     * Generate standard time slots for a week
     * (e.g., Mon-Fri 6PM-10PM, Sat-Sun 9AM-5PM)
     */
    public static List<TimeSlot> generateWeekdayEveningSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(18, 0); // 6 PM
        LocalTime end = LocalTime.of(22, 0);   // 10 PM
        
        slots.add(new TimeSlot(DayOfWeek.MONDAY, start, end));
        slots.add(new TimeSlot(DayOfWeek.TUESDAY, start, end));
        slots.add(new TimeSlot(DayOfWeek.WEDNESDAY, start, end));
        slots.add(new TimeSlot(DayOfWeek.THURSDAY, start, end));
        slots.add(new TimeSlot(DayOfWeek.FRIDAY, start, end));
        
        return slots;
    }
    
    /**
     * Generate weekend time slots
     */
    public static List<TimeSlot> generateWeekendSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);  // 9 AM
        LocalTime end = LocalTime.of(17, 0);   // 5 PM
        
        slots.add(new TimeSlot(DayOfWeek.SATURDAY, start, end));
        slots.add(new TimeSlot(DayOfWeek.SUNDAY, start, end));
        
        return slots;
    }
    
    /**
     * Create hourly time slots for a day
     */
    public static List<TimeSlot> generateHourlySlots(DayOfWeek day, 
                                                     LocalTime dayStart,
                                                     LocalTime dayEnd) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime current = dayStart;
        
        while (current.plusHours(1).isBefore(dayEnd) || 
               current.plusHours(1).equals(dayEnd)) {
            slots.add(new TimeSlot(day, current, current.plusHours(1)));
            current = current.plusHours(1);
        }
        
        return slots;
    }
    
    @Override
    public String toString() {
        return String.format("%s %s-%s", 
            DateTimeUtil.getDayName(day),
            DateTimeUtil.formatTime(startTime),
            DateTimeUtil.formatTime(endTime)
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TimeSlot)) return false;
        
        TimeSlot other = (TimeSlot) obj;
        return this.day == other.day &&
               this.startTime.equals(other.startTime) &&
               this.endTime.equals(other.endTime);
    }
    
    @Override
    public int hashCode() {
        return day.hashCode() + startTime.hashCode() + endTime.hashCode();
    }
}
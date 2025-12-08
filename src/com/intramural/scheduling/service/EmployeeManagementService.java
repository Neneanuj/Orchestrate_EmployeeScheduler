package com.intramural.scheduling.service;

import com.intramural.scheduling.dao.*;
import com.intramural.scheduling.model.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class EmployeeManagementService {
    private UserDao userDAO;
    private EmployeeDAO employeeDAO;
    private AvailabilityDAO availabilityDAO;
    private WeeklyHoursDAO weeklyHoursDAO;
    private EmployeeExpertiseDAO expertiseDAO;
    private AuthenticationService authService;
    
    public EmployeeManagementService() {
        this.userDAO = new UserDao();
        this.employeeDAO = new EmployeeDAO();
        this.availabilityDAO = new AvailabilityDAO();
        this.weeklyHoursDAO = new WeeklyHoursDAO();
        this.expertiseDAO = new EmployeeExpertiseDAO();
        this.authService = new AuthenticationService();
    }
    
    /**
     * PRIMARY METHOD: Create employee with availability and sports
     * This is the ONLY method you need to call
     */
    public Employee createEmployee(
            String firstName, 
            String lastName,
            boolean isSupervisor,
            List<Availability.Seasonal> availabilities,
            List<Integer> sportIds) 
            throws SQLException, IllegalArgumentException {
        
        // Validate
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        // BUG-F003: Validate first name contains only valid characters
        if (!firstName.matches("^[a-zA-Z\\s'-]+$")) {
            throw new IllegalArgumentException("First name must contain only letters, spaces, hyphens, or apostrophes");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        // BUG-F003: Validate last name contains only valid characters
        if (!lastName.matches("^[a-zA-Z\\s'-]+$")) {
            throw new IllegalArgumentException("Last name must contain only letters, spaces, hyphens, or apostrophes");
        }
        if (sportIds == null || sportIds.isEmpty()) {
            throw new IllegalArgumentException("At least one sport must be selected");
        }
        
        // BUG-F005: Check for duplicate employee names
        try {
            if (employeeDAO.nameExists(firstName.trim(), lastName.trim())) {
                throw new IllegalArgumentException(
                    "An employee named '" + firstName.trim() + " " + lastName.trim() + 
                    "' already exists. Please use a different name or add a middle initial.");
            }
        } catch (SQLException e) {
            throw new SQLException("Error checking for duplicate names: " + e.getMessage(), e);
        }
        
        try {
            // 1. Create dummy user account
            String username = generateUsername(firstName, lastName);
            String password = UUID.randomUUID().toString();
            String passwordHash = authService.hashPassword(password);
            
            User user = new User(0, username, passwordHash, User.UserRole.STAFF, "");
            userDAO.insert(user);
            
            // 2. Create employee
            Employee employee = new Employee(0, user.getUserId(), firstName, lastName);
            employee.setSupervisorEligible(isSupervisor);
            employee.setMaxHoursPerWeek(20);
            employee.setActiveStatus(true);
            employeeDAO.insert(employee);
            
            // 3. Save availability
            if (availabilities != null && !availabilities.isEmpty()) {
                for (Availability.Seasonal avail : availabilities) {
                    Availability.Seasonal availWithId = new Availability.Seasonal(
                        employee.getEmployeeId(),
                        avail.getSeason(),
                        avail.getYear(),
                        avail.getDayOfWeek(),
                        avail.getStartTime(),
                        avail.getEndTime()
                    );
                    availabilityDAO.insert(availWithId);
                }
            }
            
            // 4. Save sport expertise
            for (Integer sportId : sportIds) {
                expertiseDAO.insert(employee.getEmployeeId(), sportId, 
                                  Employee.ExpertiseLevel.INTERMEDIATE);
            }
            
            // 5. Initialize weekly hours tracking
            LocalDate weekStart = HoursTracker.getWeekStartDate(LocalDate.now());
            Tracking.WeeklyHours initialHours = new Tracking.WeeklyHours(
                employee.getEmployeeId(), weekStart
            );
            weeklyHoursDAO.insert(initialHours);
            
            return employee;
            
        } catch (SQLException e) {
            System.err.println("Error creating employee: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Generate unique username from name
     */
    private String generateUsername(String firstName, String lastName) throws SQLException {
        String base = (firstName + lastName.charAt(0)).toLowerCase()
                     .replaceAll("[^a-z0-9]", "");
        
        String username = base;
        int counter = 1;
        
        while (userDAO.usernameExists(username)) {
            username = base + counter++;
        }
        
        return username;
    }
    
    /**
     * Get all employees with expertise loaded
     */
    public List<Employee> getAllEmployeesWithExpertise() throws SQLException {
        List<Employee> employees = employeeDAO.getAllActive();
        
        for (Employee emp : employees) {
            List<Integer> sportIds = expertiseDAO.getSportIdsByEmployee(emp.getEmployeeId());
            for (Integer sportId : sportIds) {
                Employee.ExpertiseLevel level = expertiseDAO.getExpertiseLevel(
                    emp.getEmployeeId(), sportId
                );
                emp.addExpertise(new Employee.SportExpertise(0, sportId, level));
            }
        }
        
        return employees;
    }
    
    /**
     * Get employee by ID
     */
    public Employee getEmployee(int employeeId) throws SQLException {
        return employeeDAO.getById(employeeId);
    }
    
    /**
     * Update employee
     */
    public void updateEmployee(Employee employee) throws SQLException {
        employeeDAO.update(employee);
    }
}
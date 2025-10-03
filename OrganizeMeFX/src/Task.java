import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Task {
    public enum Priority {
        LOW("Low"), MEDIUM("Medium"), HIGH("High"), URGENT("Urgent");
        
        private final String displayName;
        
        Priority(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public enum RecurringType {
        NONE("None"), DAILY("Daily"), WEEKLY("Weekly"), MONTHLY("Monthly");
        
        private final String displayName;
        
        RecurringType(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    private String title;
    private boolean completed;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Priority priority;
    private RecurringType recurringType;
    private LocalDateTime createdDate;
    private LocalDateTime completedDate;
    
    // Constructor
    public Task(String title) {
        this.title = title;
        this.completed = false;
        this.priority = Priority.MEDIUM;
        this.recurringType = RecurringType.NONE;
        this.createdDate = LocalDateTime.now();
    }
    
    // Full constructor
    public Task(String title, LocalDate dueDate, LocalTime dueTime, Priority priority, RecurringType recurringType) {
        this.title = title;
        this.completed = false;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.recurringType = recurringType != null ? recurringType : RecurringType.NONE;
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { 
        this.completed = completed;
        if (completed) {
            this.completedDate = LocalDateTime.now();
        } else {
            this.completedDate = null;
        }
    }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public LocalTime getDueTime() { return dueTime; }
    public void setDueTime(LocalTime dueTime) { this.dueTime = dueTime; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public RecurringType getRecurringType() { return recurringType; }
    public void setRecurringType(RecurringType recurringType) { this.recurringType = recurringType; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
    
    // Utility methods
    public boolean isOverdue() {
        if (completed || dueDate == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDateTime = dueTime != null ? 
            LocalDateTime.of(dueDate, dueTime) : 
            dueDate.atTime(23, 59, 59);
            
        return now.isAfter(dueDateTime);
    }
    
    public String getFormattedDueDate() {
        if (dueDate == null) return "";
        
        String dateStr = dueDate.format(DateTimeFormatter.ofPattern("MMM dd"));
        if (dueTime != null) {
            dateStr += " " + dueTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return dateStr;
    }
    
    public String getPriorityColor() {
        switch (priority) {
            case URGENT: return "#ff4444";
            case HIGH: return "#ff8800";
            case MEDIUM: return "#ffcc00";
            case LOW: return "#88cc88";
            default: return "#cccccc";
            
        }
    }
    
    public Task createNextRecurrence() {
        if (recurringType == RecurringType.NONE || dueDate == null) {
            return null;
        }
        
        LocalDate nextDueDate = switch (recurringType) {
            case DAILY -> dueDate.plusDays(1);
            case WEEKLY -> dueDate.plusWeeks(1);
            case MONTHLY -> dueDate.plusMonths(1);
            default -> null;
        };
        
        if (nextDueDate != null) {
            return new Task(title, nextDueDate, dueTime, priority, recurringType);
        }
        
        return null;
    }
    
    // Serialization for file storage
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(completed ? "1" : "0").append(",");
        sb.append(escapeCommas(title)).append(",");
        sb.append(dueDate != null ? dueDate.toString() : "").append(",");
        sb.append(dueTime != null ? dueTime.toString() : "").append(",");
        sb.append(priority.name()).append(",");
        sb.append(recurringType.name()).append(",");
        sb.append(createdDate.toString());
        if (completedDate != null) {
            sb.append(",").append(completedDate.toString());
        }
        return sb.toString();
    }
    
    public static Task fromFileString(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 7) return null;
        
        try {
            boolean completed = "1".equals(parts[0]);
            String title = unescapeCommas(parts[1]);
            LocalDate dueDate = parts[2].isEmpty() ? null : LocalDate.parse(parts[2]);
            LocalTime dueTime = parts[3].isEmpty() ? null : LocalTime.parse(parts[3]);
            Priority priority = Priority.valueOf(parts[4]);
            RecurringType recurringType = RecurringType.valueOf(parts[5]);
            LocalDateTime createdDate = LocalDateTime.parse(parts[6]);
            
            Task task = new Task(title, dueDate, dueTime, priority, recurringType);
            task.setCreatedDate(createdDate);
            task.setCompleted(completed);
            
            if (parts.length > 7 && !parts[7].isEmpty()) {
                task.setCompletedDate(LocalDateTime.parse(parts[7]));
            }
            
            return task;
        } catch (Exception e) {
            System.err.println("Error parsing task from file: " + e.getMessage());
            return null;
        }
    }
    
    private String escapeCommas(String str) {
        return str.replace(",", "\\,");
    }
    
    private static String unescapeCommas(String str) {
        return str.replace("\\,", ",");
    }
    
    @Override
    public String toString() {
        return title + (dueDate != null ? " (Due: " + getFormattedDueDate() + ")" : "");
    }
}
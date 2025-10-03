import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TodoTask {
    
    public enum Priority {
        LOW("#10b981"),    // Green
        MEDIUM("#f59e0b"), // Yellow
        HIGH("#ef4444");   // Red
        
        private final String color;
        
        Priority(String color) {
            this.color = color;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    public enum RecurringType {
        NONE, DAILY, WEEKLY, MONTHLY
    }
    
    private String title;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Priority priority;
    private RecurringType recurringType;
    private boolean completed;
    private LocalDateTime createdDate;
    
    // Constructor
    public TodoTask(String title, LocalDate dueDate, LocalTime dueTime, Priority priority, RecurringType recurringType) {
        this.title = title;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.recurringType = recurringType != null ? recurringType : RecurringType.NONE;
        this.completed = false;
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters
    public String getTitle() { return title; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalTime getDueTime() { return dueTime; }
    public Priority getPriority() { return priority; }
    public RecurringType getRecurringType() { return recurringType; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    
    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setDueTime(LocalTime dueTime) { this.dueTime = dueTime; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setRecurringType(RecurringType recurringType) { this.recurringType = recurringType; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    // Utility methods
    public String getPriorityColor() {
        return priority.getColor();
    }
    
    public boolean isOverdue() {
        if (completed || dueDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today)) {
            return true;
        }
        if (dueDate.equals(today) && dueTime != null) {
            return LocalTime.now().isAfter(dueTime);
        }
        return false;
    }
    
    public String getFormattedDueDate() {
        if (dueDate == null) {
            return "No due date";
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append(dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        
        if (dueTime != null) {
            formatted.append(" at ").append(dueTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        
        return formatted.toString();
    }
    
    public TodoTask createNextRecurrence() {
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
            return new TodoTask(title, nextDueDate, dueTime, priority, recurringType);
        }
        
        return null;
    }
    
    // File serialization methods
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("|");
        sb.append(dueDate != null ? dueDate.toString() : "").append("|");
        sb.append(dueTime != null ? dueTime.toString() : "").append("|");
        sb.append(priority.name()).append("|");
        sb.append(recurringType.name()).append("|");
        sb.append(completed).append("|");
        sb.append(createdDate.toString());
        return sb.toString();
    }
    
    public static TodoTask fromFileString(String fileString) {
        try {
            String[] parts = fileString.split("\\|");
            if (parts.length < 7) {
                System.err.println("Invalid task format: " + fileString);
                return null;
            }
            
            String title = parts[0];
            LocalDate dueDate = parts[1].isEmpty() ? null : LocalDate.parse(parts[1]);
            LocalTime dueTime = parts[2].isEmpty() ? null : LocalTime.parse(parts[2]);
            Priority priority = Priority.valueOf(parts[3]);
            RecurringType recurringType = RecurringType.valueOf(parts[4]);
            boolean completed = Boolean.parseBoolean(parts[5]);
            LocalDateTime createdDate = LocalDateTime.parse(parts[6]);
            
            TodoTask task = new TodoTask(title, dueDate, dueTime, priority, recurringType);
            task.setCompleted(completed);
            task.createdDate = createdDate;
            
            return task;
        } catch (Exception e) {
            System.err.println("Error parsing task from string: " + fileString);
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "TodoTask{" +
                "title='" + title + '\'' +
                ", dueDate=" + dueDate +
                ", dueTime=" + dueTime +
                ", priority=" + priority +
                ", recurringType=" + recurringType +
                ", completed=" + completed +
                ", createdDate=" + createdDate +
                '}';
    }
}
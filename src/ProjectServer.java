import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static Map<String, ClientHandler> usernames = new ConcurrentHashMap<>();
    private static Map<Integer, ProjectTask> tasks = new ConcurrentHashMap<>();
    private static AtomicInteger taskIdCounter = new AtomicInteger(1);
    
    public static void main(String[] args) {
        System.out.println("Project Management Server starting on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started successfully!");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    public static synchronized void broadcastMessage(String message, ClientHandler sender) {
        System.out.println("Broadcasting: " + message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    
    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        if (client.getUsername() != null) {
            usernames.remove(client.getUsername());
            broadcastMessage("SYSTEM:" + client.getUsername() + " left the project", client);
        }
        System.out.println("Client disconnected. Active clients: " + clients.size());
    }
    
    public static synchronized boolean addUsername(String username, ClientHandler client) {
        if (usernames.containsKey(username)) {
            return false;
        }
        usernames.put(username, client);
        client.setUsername(username);
        broadcastMessage("SYSTEM:" + username + " joined the project", client);
        return true;
    }
    
    public static synchronized Set<String> getActiveUsers() {
        return new HashSet<>(usernames.keySet());
    }
    
    public static synchronized int addTask(String title, String description, String assignedBy) {
        int taskId = taskIdCounter.getAndIncrement();
        ProjectTask task = new ProjectTask(taskId, title, description, assignedBy);
        tasks.put(taskId, task);
        
        String taskMessage = "TASK_ADDED:" + taskId + "|" + title + "|" + description + "|" + assignedBy + "|PENDING|";
        broadcastMessage(taskMessage, null);
        
        return taskId;
    }
    
    public static synchronized boolean completeTask(int taskId, String completedBy) {
        ProjectTask task = tasks.get(taskId);
        if (task != null && task.getStatus() == TaskStatus.PENDING) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedBy(completedBy);
            task.setCompletedDate(new Date());
            
            String taskMessage = "TASK_COMPLETED:" + taskId + "|" + task.getTitle() + "|" + completedBy;
            broadcastMessage(taskMessage, null);
            return true;
        }
        return false;
    }
    
    public static synchronized boolean deleteTask(int taskId, String deletedBy) {
        ProjectTask task = tasks.get(taskId);
        if (task != null) {
            tasks.remove(taskId);
            
            String taskMessage = "TASK_DELETED:" + taskId + "|" + task.getTitle() + "|" + deletedBy;
            broadcastMessage(taskMessage, null);
            return true;
        }
        return false;
    }
    
    public static synchronized List<ProjectTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }
}

enum TaskStatus {
    PENDING, COMPLETED
}

class ProjectTask {
    private int id;
    private String title;
    private String description;
    private String assignedBy;
    private String completedBy;
    private TaskStatus status;
    private Date createdDate;
    private Date completedDate;
    
    public ProjectTask(int id, String title, String description, String assignedBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assignedBy = assignedBy;
        this.status = TaskStatus.PENDING;
        this.createdDate = new Date();
    }
    
    // Getters and setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAssignedBy() { return assignedBy; }
    public String getCompletedBy() { return completedBy; }
    public TaskStatus getStatus() { return status; }
    public Date getCreatedDate() { return createdDate; }
    public Date getCompletedDate() { return completedDate; }
    
    public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setCompletedDate(Date completedDate) { this.completedDate = completedDate; }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Handle username setup
            out.println("SYSTEM:Enter your username:");
            String usernameInput;
            while ((usernameInput = in.readLine()) != null) {
                if (usernameInput.startsWith("USERNAME:")) {
                    String requestedUsername = usernameInput.substring(9);
                    if (ProjectServer.addUsername(requestedUsername, this)) {
                        out.println("SYSTEM:Welcome to the project, " + requestedUsername + "!");
                        sendUserList();
                        sendAllTasks();
                        break;
                    } else {
                        out.println("SYSTEM:Username already taken. Please choose another:");
                    }
                }
            }
            
            // Handle messages and commands
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("ADD_TASK:")) {
                    handleAddTask(message.substring(9));
                } else if (message.startsWith("COMPLETE_TASK:")) {
                    handleCompleteTask(message.substring(14));
                } else if (message.startsWith("DELETE_TASK:")) {
                    handleDeleteTask(message.substring(12));
                } else if (message.startsWith("MESSAGE:")) {
                    String chatMessage = message.substring(8);
                    String formattedMessage = "MESSAGE:" + username + ": " + chatMessage;
                    ProjectServer.broadcastMessage(formattedMessage, this);
                }
            }
        } catch (IOException e) {
            System.out.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void handleAddTask(String taskData) {
        String[] parts = taskData.split("\\|", 2);
        if (parts.length == 2) {
            String title = parts[0];
            String description = parts[1];
            int taskId = ProjectServer.addTask(title, description, username);
            System.out.println("Task added by " + username + ": " + title);
        }
    }
    
    private void handleCompleteTask(String taskIdStr) {
        try {
            int taskId = Integer.parseInt(taskIdStr);
            boolean success = ProjectServer.completeTask(taskId, username);
            if (success) {
                System.out.println("Task " + taskId + " completed by " + username);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid task ID: " + taskIdStr);
        }
    }
    
    private void handleDeleteTask(String taskIdStr) {
        try {
            int taskId = Integer.parseInt(taskIdStr);
            boolean success = ProjectServer.deleteTask(taskId, username);
            if (success) {
                System.out.println("Task " + taskId + " deleted by " + username);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid task ID: " + taskIdStr);
        }
    }
    
    private void sendAllTasks() {
        List<ProjectTask> tasks = ProjectServer.getAllTasks();
        for (ProjectTask task : tasks) {
            String taskMessage = "TASK_ADDED:" + task.getId() + "|" + task.getTitle() + "|" + 
                                task.getDescription() + "|" + task.getAssignedBy() + "|" + 
                                task.getStatus() + "|" + 
                                (task.getCompletedBy() != null ? task.getCompletedBy() : "");
            sendMessage(taskMessage);
        }
    }
    
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    public void sendUserList() {
        Set<String> users = ProjectServer.getActiveUsers();
        sendMessage("USERS:" + String.join(",", users));
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client resources: " + e.getMessage());
        }
        ProjectServer.removeClient(this);
    }
}
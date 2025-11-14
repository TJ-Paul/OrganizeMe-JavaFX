import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProjectController implements Initializable {
    
    @FXML private VBox taskArea;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField taskTitleInput;
    @FXML private TextArea taskDescriptionInput;
    @FXML private Button addTaskButton;
    @FXML private Label connectionStatus;
    @FXML private ListView<String> userList;
    @FXML private Label userCount;
    @FXML private TextField messageInput;
    @FXML private Button sendMessageButton;
    @FXML private VBox chatArea;
    @FXML private ScrollPane chatScrollPane;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean connected = false;
    private Stage primaryStage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Auto-scroll to bottom when new content is added
        taskArea.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });
        
        chatArea.heightProperty().addListener((obs, oldVal, newVal) -> {
            chatScrollPane.setVvalue(1.0);
        });
        
        // Initially disable input until connected
        taskTitleInput.setDisable(true);
        taskDescriptionInput.setDisable(true);
        addTaskButton.setDisable(true);
        messageInput.setDisable(true);
        sendMessageButton.setDisable(true);
        
        // Set up enter key handlers
        taskTitleInput.setOnKeyPressed(this::handleTaskKeyPressed);
        messageInput.setOnKeyPressed(this::handleMessageKeyPressed);
        
        connectionStatus.setText("Disconnected");
        connectionStatus.getStyleClass().add("status-disconnected");
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    public void showConnectionDialog() {
        TextInputDialog serverDialog = new TextInputDialog("localhost");
        serverDialog.setTitle("Connect to Project Server");
        serverDialog.setHeaderText("Server Connection");
        serverDialog.setContentText("Enter server address:");
        
        Optional<String> serverResult = serverDialog.showAndWait();
        if (serverResult.isPresent()) {
            String serverAddress = serverResult.get();
            
            TextInputDialog usernameDialog = new TextInputDialog();
            usernameDialog.setTitle("Username");
            usernameDialog.setHeaderText("Enter Username");
            usernameDialog.setContentText("Choose your username:");
            
            Optional<String> usernameResult = usernameDialog.showAndWait();
            if (usernameResult.isPresent()) {
                String username = usernameResult.get().trim();
                if (!username.isEmpty()) {
                    connectToServer(serverAddress, username);
                } else {
                    showConnectionDialog(); // Retry if empty username
                }
            } else {
                System.exit(0); // Exit if user cancels
            }
        } else {
            System.exit(0); // Exit if user cancels
        }
    }
    
    private void connectToServer(String serverAddress, String username) {
        try {
            socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            this.username = username;
            connected = true;
            
            Platform.runLater(() -> {
                connectionStatus.setText("Connected to " + serverAddress);
                connectionStatus.getStyleClass().removeAll("status-disconnected");
                connectionStatus.getStyleClass().add("status-connected");
                taskTitleInput.setDisable(false);
                taskDescriptionInput.setDisable(false);
                addTaskButton.setDisable(false);
                messageInput.setDisable(false);
                sendMessageButton.setDisable(false);
                primaryStage.setTitle("Project Manager - " + username);
            });
            
            // Start listening for messages
            Thread messageListener = new Thread(this::listenForMessages);
            messageListener.setDaemon(true);
            messageListener.start();
            
            // Send username to server
            out.println("USERNAME:" + username);
            
        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Failed to connect to server");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
                showConnectionDialog(); // Retry connection
            });
        }
    }
    
    private void listenForMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                final String finalMessage = message;
                Platform.runLater(() -> handleServerMessage(finalMessage));
            }
        } catch (IOException e) {
            if (connected) {
                Platform.runLater(() -> {
                    addSystemMessage("Connection lost to server");
                    connectionStatus.setText("Disconnected");
                    connectionStatus.getStyleClass().removeAll("status-connected");
                    connectionStatus.getStyleClass().add("status-disconnected");
                    taskTitleInput.setDisable(true);
                    taskDescriptionInput.setDisable(true);
                    addTaskButton.setDisable(true);
                    messageInput.setDisable(true);
                    sendMessageButton.setDisable(true);
                });
            }
        }
    }
    
    private void handleServerMessage(String message) {
        if (message.startsWith("SYSTEM:")) {
            addSystemMessage(message.substring(7));
        } else if (message.startsWith("MESSAGE:")) {
            String chatMessage = message.substring(8);
            addChatMessage(chatMessage);
        } else if (message.startsWith("USERS:")) {
            updateUserList(message.substring(6));
        } else if (message.startsWith("TASK_ADDED:")) {
            handleTaskAdded(message.substring(11));
        } else if (message.startsWith("TASK_COMPLETED:")) {
            handleTaskCompleted(message.substring(15));
        } else if (message.startsWith("TASK_DELETED:")) {
            handleTaskDeleted(message.substring(13));
        }
    }
    
    private void handleTaskAdded(String taskData) {
        String[] parts = taskData.split("\\|");
        if (parts.length >= 5) {
            int taskId = Integer.parseInt(parts[0]);
            String title = parts[1];
            String description = parts[2];
            String assignedBy = parts[3];
            String status = parts[4];
            String completedBy = parts.length > 5 ? parts[5] : "";
            
            addTaskToUI(taskId, title, description, assignedBy, status, completedBy);
        }
    }
    
    private void handleTaskCompleted(String taskData) {
        String[] parts = taskData.split("\\|");
        if (parts.length >= 3) {
            int taskId = Integer.parseInt(parts[0]);
            String title = parts[1];
            String completedBy = parts[2];
            
            addSystemMessage("Task completed: \"" + title + "\" by " + completedBy);
            updateTaskStatus(taskId, "COMPLETED", completedBy);
        }
    }
    
    private void handleTaskDeleted(String taskData) {
        String[] parts = taskData.split("\\|");
        if (parts.length >= 3) {
            int taskId = Integer.parseInt(parts[0]);
            String title = parts[1];
            String deletedBy = parts[2];
            
            addSystemMessage("Task deleted: \"" + title + "\" by " + deletedBy);
            removeTaskFromUI(taskId);
        }
    }
    
    private void updateTaskStatus(int taskId, String status, String completedBy) {
        // Find and update the task in the UI
        for (int i = 0; i < taskArea.getChildren().size(); i++) {
            if (taskArea.getChildren().get(i) instanceof VBox) {
                VBox taskBox = (VBox) taskArea.getChildren().get(i);
                if (taskBox.getUserData() != null && taskBox.getUserData().equals(taskId)) {
                    // Update the task box to show completed status
                    taskBox.getStyleClass().removeAll("task-pending");
                    taskBox.getStyleClass().add("task-completed");
                    
                    // Update the buttons
                    for (int j = 0; j < taskBox.getChildren().size(); j++) {
                        if (taskBox.getChildren().get(j) instanceof HBox) {
                            HBox buttonBox = (HBox) taskBox.getChildren().get(j);
                            buttonBox.getChildren().clear();
                            
                            Label completedLabel = new Label("✓ Completed by " + completedBy);
                            completedLabel.getStyleClass().add("completed-label");
                            buttonBox.getChildren().add(completedLabel);
                        }
                    }
                    break;
                }
            }
        }
    }
    
    private void removeTaskFromUI(int taskId) {
        taskArea.getChildren().removeIf(node -> {
            if (node instanceof VBox) {
                VBox taskBox = (VBox) node;
                return taskBox.getUserData() != null && taskBox.getUserData().equals(taskId);
            }
            return false;
        });
    }
    
    private void updateUserList(String userListString) {
        userList.getItems().clear();
        if (!userListString.isEmpty()) {
            String[] users = userListString.split(",");
            for (String user : users) {
                userList.getItems().add(user);
            }
            userCount.setText(users.length + " users online");
        } else {
            userCount.setText("0 users online");
        }
    }
    
    @FXML
    private void handleAddTask() {
        String title = taskTitleInput.getText().trim();
        String description = taskDescriptionInput.getText().trim();
        
        if (!title.isEmpty() && connected) {
            out.println("ADD_TASK:" + title + "|" + description);
            taskTitleInput.clear();
            taskDescriptionInput.clear();
        } else if (title.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Task");
            alert.setHeaderText("Task title cannot be empty");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && connected) {
            // Don't add the message locally - let the server broadcast it back
            out.println("MESSAGE:" + message);
            messageInput.clear();
        }
    }
    
    private void handleTaskKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleAddTask();
        }
    }
    
    private void handleMessageKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleSendMessage();
        }
    }
    
    private void addTaskToUI(int taskId, String title, String description, String assignedBy, String status, String completedBy) {
        VBox taskBox = new VBox(5);
        taskBox.setUserData(taskId);
        taskBox.setPadding(new Insets(10));
        taskBox.getStyleClass().add(status.equals("COMPLETED") ? "task-completed" : "task-pending");
        
        Label titleLabel = new Label("Task #" + taskId + ": " + title);
        titleLabel.getStyleClass().add("task-title");
        
        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("task-description");
        
        Label assignedLabel = new Label("Assigned by: " + assignedBy);
        assignedLabel.getStyleClass().add("task-assigned");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        if (status.equals("PENDING")) {
            Button completeButton = new Button("Mark Complete");
            completeButton.getStyleClass().add("complete-button");
            completeButton.setOnAction(e -> {
                out.println("COMPLETE_TASK:" + taskId);
            });
            
            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setOnAction(e -> {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Delete Task");
                confirmAlert.setHeaderText("Are you sure you want to delete this task?");
                confirmAlert.setContentText("Task: " + title);
                
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    out.println("DELETE_TASK:" + taskId);
                }
            });
            
            buttonBox.getChildren().addAll(completeButton, deleteButton);
        } else {
            Label completedLabel = new Label("✓ Completed by " + completedBy);
            completedLabel.getStyleClass().add("completed-label");
            
            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setOnAction(e -> {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Delete Task");
                confirmAlert.setHeaderText("Are you sure you want to delete this task?");
                confirmAlert.setContentText("Task: " + title);
                
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    out.println("DELETE_TASK:" + taskId);
                }
            });
            
            buttonBox.getChildren().addAll(completedLabel, deleteButton);
        }
        
        taskBox.getChildren().addAll(titleLabel, descriptionLabel, assignedLabel, buttonBox);
        taskArea.getChildren().add(taskBox);
    }
    
    private void addSystemMessage(String message) {
        HBox messageBox = createChatMessageBox(message, "system-message", Pos.CENTER);
        chatArea.getChildren().add(messageBox);
    }
    
    private void addChatMessage(String message) {
        // Parse the message to see if it's from the current user
        String[] parts = message.split(": ", 2);
        if (parts.length == 2 && parts[0].equals(username)) {
            // This is our own message coming back from server
            HBox messageBox = createChatMessageBox(message, "user-message", Pos.CENTER_RIGHT);
            chatArea.getChildren().add(messageBox);
        } else {
            // This is from another user
            HBox messageBox = createChatMessageBox(message, "contact-message", Pos.CENTER_LEFT);
            chatArea.getChildren().add(messageBox);
        }
    }
    
    private HBox createChatMessageBox(String message, String styleClass, Pos alignment) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(alignment);
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        
        VBox messageBubble = new VBox();
        messageBubble.getStyleClass().add(styleClass);
        messageBubble.setPadding(new Insets(8, 12, 8, 12));
        messageBubble.setMaxWidth(300);
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("message-text");
        
        Label timeLabel = new Label(getCurrentTime());
        timeLabel.getStyleClass().add("time-label");
        
        messageBubble.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(messageBubble);
        
        return messageBox;
    }
    
    private String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
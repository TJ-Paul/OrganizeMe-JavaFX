# OrganizeMe Batch Scripts

This document contains the batch files needed to run the OrganizeMe application and server.

---

### Bat file code for running 
## "OrganizeMe app"
```batch
@echo off
REM Change directory to the parent folder of the script
cd /d "%~dp0"

REM Define relative path to JavaFX SDK
set FX_SDK_PATH=lib/javafx-sdk-25/lib

REM Change console name
title OrganizeMe console

REM Run the JavaFX JAR with relative paths
java --enable-native-access=javafx.graphics --module-path "%FX_SDK_PATH%" --add-modules javafx.controls,javafx.fxml -jar src/OrganizeMeJavaFX.jar
```

---

### Bat file code for running 
## "OrganizeMe server"
```batch
@echo off
REM Change directory to the parent folder of the script
cd /d "%~dp0"

REM Name the terminal window
title OrganizeMe Server Console

REM Define relative path to JavaFX SDK
set FX_SDK_PATH=lib/javafx-sdk-25/lib

REM Run the JavaFX JAR with relative paths
java --enable-native-access=javafx.graphics --module-path "%FX_SDK_PATH%" --add-modules javafx.controls,javafx.fxml -jar src/OrganizeMeServer.jar

pause
```

---

## VSCode Configuration

To run the OrganizeMe application directly from Visual Studio Code, you need to configure the `launch.json` file with the appropriate VM arguments.

### Setup Instructions

1. Create a `.vscode` folder in your project root (if it doesn't exist)
2. Inside the `.vscode` folder, create a file named `launch.json`
3. Copy and paste the following configuration:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "OrganizeMe App",
            "request": "launch",
            "mainClass": "App",
            "projectName": "OrganizeMeJavaFX_5ff4a90a",
            "vmArgs": "--module-path lib/javafx-sdk-25/lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics"
        }
    ]
}
```

4. Save the file

### Running from VSCode

1. Open your project in Visual Studio Code
2. Press `F5` or click **Run > Start Debugging**
3. Alternatively, use the **Run and Debug** panel (Ctrl+Shift+D)
4. Select "OrganizeMe App" from the dropdown
5. Click the green play button

### Important Notes

- Ensure the `mainClass` matches your application's main class name
- Update `projectName` if your project has a different identifier
- The `vmArgs` parameter must point to the correct JavaFX SDK path

---


## Setup Instructions

1. Ensure JavaFX SDK 25 is located in `lib/javafx-sdk-25/lib`
2. Place your JAR files in the `src` directory:
   - `OrganizeMeServer.jar` for the server
   - `OrganizeMeJavaFX.jar` for the application
3. Run the appropriate batch file to start the server or application
   - create new `Run.txt` file
   - copy-paste code: [OrganizeMe app](#organizeme-app)
   - save as `Run.bat` file
   - create new `Server.txt` file
   - copy-paste code: [OrganizeMe Server](#organizeme-server)
   - save as `Server.bat` file

## Requirements

- Java Runtime Environment (JRE) with JavaFX support
- JavaFX SDK 25
- OrganizeMe JAR files

---

## Breakdown of the Script

• `@echo off`: Hides the commands from being displayed in the command prompt.

• `cd /d "%~dp0"`: Changes the current directory to where the batch file is located, ensuring portability. ○ `set FX_SDK_PATH=lib/javafx-sdk-25/lib`: Sets a variable for the path to your JavaFX libraries.

• `title OrganizeMe console`: This changes the console title to "OrganizeMe console"

• `start "OrganizeMe Server" java ...`: This is the key line. ○ `start "OrganizeMe Server"`: Launches a new command prompt window with the title "OrganizeMe Server". This gives your server its own dedicated console, making it easy to identify and manage. ○ `java ...`: The rest of the command launches your Java application.

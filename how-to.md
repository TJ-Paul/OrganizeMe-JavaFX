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

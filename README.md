# Basic communication
To run the simplest version of communication between your computer and your Android Application git checkout to d3ec24c and follow the instructions.
 ```
git checkout d3ec24c
```

## Setting Up the Project

1. **Local Properties**:
   - Before starting the Android Application, create a `local.properties` file in the Android project root, and add:
     ```
     sdk.dir=/path/to/your/android/sdk
     ```
     (Usually `/home/your-username/Android/Sdk` for Ubuntu)

2. **Backend Configuration**:
   - In `backend.py`, replace the IP address in `s.connect(('10.0.0.35', 12345))` with your Android device's IP ("Settings" > "About phone" > "ID address").

4. **Running the Application**:
   - Start the backend server in Terminal 1:
     ```
     python3 backend.py
     ```
   - Start the frontend in Terminal 2:
     ```
     python3 frontend.py
     ```
     Or send a POST command via curl:
     ```
     curl -X POST http://localhost:5000/take_photo
     ```

## Outputs

- **Logcat**: Check the `SocketServer` tag in Logcat for outputs. It should display `Connection accepted`.
- **Terminal 1**: Should display `Received: Command received`.
- **Terminal 2**: Should display   `"message": "Photo command sent.", "status": "success"`.

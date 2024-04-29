package com.example.picture

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import android.os.Handler
import android.os.HandlerThread
import android.Manifest

class MainActivity : AppCompatActivity() {

    private val serverJob = Job()
    private val serverScope = CoroutineScope(Dispatchers.IO + serverJob)
    private lateinit var cameraHandler: Handler
    private lateinit var cameraThread: HandlerThread
    private lateinit var cameraDevice: CameraDevice
    private lateinit var imageReader: ImageReader
    private var captureSession: CameraCaptureSession? = null
    private var isCameraReady = false
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SocketServer", "Initiated")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupCamera()
        startServer()
    }
    private fun setupCamera() {
        cameraThread = HandlerThread("CameraHandlerThread").apply {
            start()
        }
        cameraHandler = Handler(cameraThread.looper)

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0] // Assuming the device has a camera

        // Check CAMERA permission
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        Log.e("SocketServer", "CameraDevice set")
                        setupImageReader()
                        isCameraReady = true
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        camera.close()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        Log.e("SocketServer", "Error opening camera: $error")
                    }
                }, cameraHandler)
            } catch (e: SecurityException) {
                Log.e("SocketServer", "SecurityException in opening camera: ${e.message}")
            }
        } else {
            // Request the CAMERA permission
            requestCameraPermission()
        }
    }
    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    private fun setupImageReader() {
        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                val buffer = image.planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                image.close()
                Log.e("SocketServer", "setupImageReader set")
                // Handle the byte array (data) which contains the JPEG image
            }, cameraHandler)
        }
        createCaptureSession()
    }
    private fun createCaptureSession() {
        cameraDevice.createCaptureSession(listOf(imageReader.surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                // Now, takePicture is not called here. It will be called only when the TAKE_PHOTO command is received.
                Log.d("SocketServer", "Capture session configured.")
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("SocketServer", "Failed to configure capture session")
            }
        }, cameraHandler)
    }

    private fun takePicture() {
        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
            addTarget(imageReader.surface)
            set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        }
        captureSession?.capture(captureRequestBuilder.build(), null, cameraHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
        serverJob.cancel()
        cameraThread.quitSafely()
    }

    private fun startServer() {
        serverScope.launch {
            try {
                ServerSocket(12345).use { serverSocket ->
                    while (isActive) {
                        val socket = serverSocket.accept()
                        Log.d("SocketServer", "Connection accepted")

                        val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                        val command = input.readLine().trim()
                        Log.d("SocketServer", "Received command: '$command'")
                        if (command == "TAKE_PHOTO") {
                            takePicture()
                        }

                        socket.close()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
package com.example.picture

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import android.os.Handler
import android.os.HandlerThread

import androidx.activity.enableEdgeToEdge

import kotlinx.coroutines.*

import java.net.Socket

//import java.net.ServerSocket
//import java.net.Socket
//import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val serverJob = Job()
    private val serverScope = CoroutineScope(Dispatchers.IO + serverJob)
    private lateinit var cameraHandler: Handler
    private lateinit var cameraThread: HandlerThread

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
        // Setup camera capture session and image capture logic
    }
    private fun takePicture() {
        // Here you should implement camera capture logic
        Log.d("Camera", "Taking picture...")
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
package com.example.picture

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.*
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket

//import java.net.ServerSocket
//import java.net.Socket
//import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val serverJob = Job()
    private val serverScope = CoroutineScope(Dispatchers.IO + serverJob)

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
        startServer()
    }    private fun startServer() {
        serverScope.launch {
            try {
                ServerSocket(12345).use { serverSocket ->
                    while (isActive) {
                        val socket = serverSocket.accept()
                        Log.d("SocketServer", "Connection accepted")

                        val output = socket.getOutputStream()
                        val writer = PrintWriter(BufferedWriter(OutputStreamWriter(output)), true)

                        writer.println("Command received")

                        // handleCommand(socket)

                        socket.close()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
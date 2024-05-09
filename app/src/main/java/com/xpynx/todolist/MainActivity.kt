package com.xpynx.todolist

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xpynx.todolist.fragment.HighPriorityFragment
import fragment.TodoListFragment

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        loadFragment(TodoListFragment())

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_BOOT_COMPLETED),
                PERMISSION_REQUEST_CODE
            )
        } else {
            createNotificationChannel()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_notes -> {
                    loadFragment(TodoListFragment())
                }
                R.id.menu_priority -> {
                    loadFragment(HighPriorityFragment())
                }
            }
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createNotificationChannel()
            } else {
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "CHANNEL_ID"
            val channelName = "My Notification Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channelDescription = "This is my notification channel"

            val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

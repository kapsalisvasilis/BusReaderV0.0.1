package com.example.busreaderv001

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.busreaderv001.ui.theme.BusReaderV001Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for storage permissions when the app starts
        checkStoragePermission()

        setContent {
            MyApp()
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                Log.d("Permissions", "Storage permissions not granted.")

                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } else {
                Log.d("PERMISSIONS", "Permissions granted.")
            }
        } else {
            Log.d("PERMISSIONS", "Permissions not required for this Android version.")
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "start") {
        composable("start") { StartPage(navController) }
        composable("library") { LibraryPage() }
        composable("read") { ReadPage() }
        composable("settings") { SettingsPage() }
    }
}

@Composable
fun StartPage(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate("library") },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Library")
        }
        Button(
            onClick = { navController.navigate("read") },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Read")
        }
        Button(
            onClick = { navController.navigate("settings") },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Settings")
        }
    }
}

package com.example.busreaderv001

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadPage(filePath: String?, onBack: () -> Unit) {
    val context = LocalContext.current

    // Decode the filePath from navigation arguments
    val decodedFilePath = filePath?.let { Uri.decode(it) }
    Log.d("ReadPage", "Decoded FilePath from navigation: $decodedFilePath")

    // Validate navigation file path
    val navigationFileValid = decodedFilePath?.let {
        val file = File(it)
        file.exists() && file.canRead()
    } ?: false

    // Prioritize active file path if navigation file path is invalid
    val activeFilePath = remember(decodedFilePath) {
        if (navigationFileValid) decodedFilePath else getActiveTextFilePath(context)
    }

    Log.d("ReadPage", "Using active file path: $activeFilePath")

    // Load the content of the file
    val fileContent = remember(activeFilePath) {
        activeFilePath?.let {
            val file = File(it)
            if (file.exists() && file.canRead()) {
                Log.d("ReadPage", "File exists and is readable: $it")
                try {
                    file.readLines()
                } catch (e: Exception) {
                    Log.e("ReadPage", "Error reading file: ${e.message}", e)
                    listOf("Error reading file: ${e.message}")
                }
            } else {
                if (!file.exists()) Log.e("ReadPage", "File does not exist at path: $it")
                if (!file.canRead()) Log.e("ReadPage", "File exists but cannot be read at path: $it")
                listOf("File does not exist or cannot be read.")
            }
        } ?: listOf("No file selected. Please choose a file from the Library.")
    }

    var textSize by remember { mutableStateOf(18.sp) }
    val scrollState = rememberLazyListState()
    val maxTextSize = 36.sp
    val minTextSize = 12.sp
    var autoScrollSpeed by remember { mutableStateOf(100L) }
    var isAutoScrolling by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isAutoScrolling) {
        while (isAutoScrolling) {
            coroutineScope.launch {
                scrollState.animateScrollToItem(
                    index = scrollState.firstVisibleItemIndex + 1,
                    scrollOffset = 0
                )
            }
            delay(autoScrollSpeed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Read Page", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { if (textSize < maxTextSize) textSize *= 1.1f }) {
                        Text("A+", fontSize = 18.sp)
                    }
                    IconButton(onClick = { if (textSize > minTextSize) textSize /= 1.1f }) {
                        Text("A-", fontSize = 18.sp)
                    }
                    IconButton(onClick = { isAutoScrolling = !isAutoScrolling }) {
                        Text(if (isAutoScrolling) "Stop" else "Auto", fontSize = 18.sp)
                    }
                    IconButton(onClick = { autoScrollSpeed = (autoScrollSpeed - 50).coerceAtLeast(50L) }) {
                        Text("Faster", fontSize = 18.sp)
                    }
                    IconButton(onClick = { autoScrollSpeed += 50L }) {
                        Text("Slower", fontSize = 18.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(fileContent) { line ->
                    Text(
                        text = line,
                        color = Color.White,
                        fontSize = textSize,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            val progress = (scrollState.firstVisibleItemIndex.toFloat() / fileContent.size) * 100
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomStart)
                    .background(Color.DarkGray)
            ) {
                Text(
                    text = "Progress: ${progress.toInt()}%",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                )
            }
        }
    }
}

// Retrieve the active text file path from SharedPreferences
private const val PREFS_NAME = "AppPreferences"
private const val ACTIVE_TEXT_FILE_KEY = "activeTextFilePath"

private fun getActiveTextFilePath(context: Context): String? {
    val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPref.getString(ACTIVE_TEXT_FILE_KEY, null)
}
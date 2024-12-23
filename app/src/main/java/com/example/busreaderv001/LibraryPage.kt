package com.example.busreaderv001

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

@Composable
fun LibraryPage() {
    val context = LocalContext.current
    var epubPath by remember { mutableStateOf<String?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = saveFileFromUri(context, uri)
            epubPath = path
            Toast.makeText(context, "File selected: $path", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Library Page", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = {
            filePickerLauncher.launch(arrayOf("application/epub+zip"))
        }) {
            Text("ADD +")
        }

        epubPath?.let { path ->
            Button(
                onClick = {
                    val outputPath = "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/output.txt"
                    EpubToPdf.extractTextFromEpub(path, outputPath)
                    Toast.makeText(context, "Text saved to: $outputPath", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Extract Text")
            }
        }
    }
}

private fun saveFileFromUri(context: Context, uri: Uri): String {
    val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    } ?: "unknown.epub"

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return file.absolutePath
}

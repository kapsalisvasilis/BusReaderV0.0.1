package com.example.busreaderv001

import androidx.navigation.NavController

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import android.util.Log


// Top-lvl  constants
private const val BOOK_LIST_FILE = "book_list.txt"
private const val SELECTED_BOOK_FILE = "selected_book.txt"  //selected book t-lvl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryPage(navController: NavController, onBack: () -> Unit) {
    val context = LocalContext.current
    var bookList by remember { mutableStateOf(loadBookList(context)) }
    var selectedBook by remember { mutableStateOf(loadSelectedBook(context)) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {

            val fileName = getFileNameFromUri(context, uri)
            val path = saveFileFromUri(context, uri, fileName)

            val outputFileName = fileName.replace(".epub", ".txt")
            val outputPath = "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/$outputFileName"


            EpubToPdf.extractTextFromEpub(path, outputPath)
            val cleanedText = cleanTextFile(outputPath)
            File(outputPath).writeText(cleanedText.joinToString("\n"))
            Toast.makeText(context, "Text cleaned and saved to: $outputPath", Toast.LENGTH_SHORT).show()
            bookList = bookList + fileName.replace(".epub", "")
            saveBookList(context, bookList)


        } else {
            Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // uni read
                    Button(
                        onClick = {
                            val activeFilePath = getActiveTextFilePath(context)
                            if (activeFilePath != null && File(activeFilePath).exists()) {
                                Log.d("LibraryPage", "Navigating to ReadPage with filePath: $activeFilePath")

                                navController.navigate("read?filePath=${Uri.encode(activeFilePath)}")
                            } else {
                                Toast.makeText(
                                    context,
                                    "No active file found. Select a book from the Library.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Text("Read")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp)) // Add extra spacing below AppBar

                // Display the list of books
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    items(bookList.size) { index ->
                        val bookName = bookList[index]
                        val isSelected = selectedBook == bookName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    selectedBook = bookName // Mark this book as selected
                                    saveSelectedBook(context, bookName) // Persist the selected book
                                    val textFilePath = "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/$bookName.txt"
                                    saveActiveTextFilePath(context, textFilePath) // Save the active text file path
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (isSelected) "1" else "0"} - $bookName",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp)
                            )
                            Button(
                                onClick = {
                                    deleteBook(context, bookName)
                                    bookList = bookList - bookName
                                    saveBookList(context, bookList)

                                    val activeFilePath = getActiveTextFilePath(context)
                                    if (activeFilePath != null && activeFilePath.endsWith("$bookName.txt")) {
                                        saveActiveTextFilePath(context, null)
                                    }

                                    if (selectedBook == bookName) {

                                        selectedBook = null
                                        saveSelectedBook(context, null)
                                    }

                                    Toast.makeText(context, "$bookName deleted", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Delete")
                            }
                        }

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }
                }

            }

            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("application/epub+zip"))
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("+", fontSize = 24.sp)
            }
        }
    }
}

private fun saveSelectedBook(context: Context, bookName: String?) {
    val file = File(context.filesDir, SELECTED_BOOK_FILE)
    if (bookName != null) {
        file.writeText(bookName)
    } else {
        file.delete() //if no book is selected, remove the file
    }
}
private const val PREFS_NAME = "AppPreferences"
private const val ACTIVE_TEXT_FILE_KEY = "activeTextFilePath"

private fun saveActiveTextFilePath(context: Context, filePath: String?) {
    val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPref.edit().putString(ACTIVE_TEXT_FILE_KEY, filePath).apply()
}

private fun getActiveTextFilePath(context: Context): String? {
    val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPref.getString(ACTIVE_TEXT_FILE_KEY, null)
}
private fun loadSelectedBook(context: Context): String? {
    val file = File(context.filesDir, SELECTED_BOOK_FILE)
    return if (file.exists()) {
        file.readText()
    } else {
        null
    }
}

private fun saveFileFromUri(context: Context, uri: Uri, fileName: String): String {
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return file.absolutePath
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    } ?: "unknown.epub"
}


private fun deleteBook(context: Context, bookName: String) {
    val filePath = "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/$bookName.txt"
    val file = File(filePath)

    if (file.exists()) {
        if (file.delete()) {
            Toast.makeText(context, "$bookName.txt deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to delete $bookName.txt", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "$bookName.txt not found", Toast.LENGTH_SHORT).show()
    }
}


private fun saveBookList(context: Context, bookList: List<String>) {
    val file = File(context.filesDir, BOOK_LIST_FILE)
    file.writeText(bookList.joinToString("\n"))
}

private fun loadBookList(context: Context): List<String> {
    val file = File(context.filesDir, BOOK_LIST_FILE)
    return if (file.exists()) {
        file.readLines()
    } else {
        emptyList()
    }
}

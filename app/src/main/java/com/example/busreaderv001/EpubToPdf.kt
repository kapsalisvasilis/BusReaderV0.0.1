package com.example.busreaderv001

import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileWriter
import java.io.InputStream

object EpubToPdf {

    fun extractTextFromEpub(epubFilePath: String, outputTextFilePath: String) {
        try {
            // Open the EPUB file
            val epubFile = File(epubFilePath)
            if (!epubFile.exists()) {
                println("ERROR: EPUB file does not exist at path: $epubFilePath")
                return
            }

            println("Reading EPUB file: $epubFilePath")
            val epubInputStream: InputStream = epubFile.inputStream()

            // Parse the EPUB file
            val book = EpubReader().readEpub(epubInputStream)

            // Extract text from each resource (e.g., XHTML files)
            val extractedText = StringBuilder()
            book.spine.spineReferences.forEach { spineRef ->
                val resource = spineRef.resource
                val content = resource.reader.readText()
                extractedText.append(content).append("\n")
            }

            // Write the extracted text to the output file
            val outputFile = File(outputTextFilePath)
            FileWriter(outputFile).use { writer ->
                writer.write(extractedText.toString())
            }

            // Print the first line of the text file
            val firstLine = outputFile.useLines { it.firstOrNull() ?: "File is empty" }
            println("First line of the output file: $firstLine")

            println("Text successfully extracted and saved to $outputTextFilePath")

        } catch (e: Exception) {
            e.printStackTrace()
            println("An error occurred: ${e.message}")
        }
    }
}
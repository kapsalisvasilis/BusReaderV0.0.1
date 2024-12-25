package com.example.busreaderv001

import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileWriter
import java.io.InputStream

object EpubToPdf {

    fun extractTextFromEpub(epubFilePath: String, outputTextFilePath: String) {
        try {

            //epub cast
            val epubFile = File(epubFilePath)
            if (!epubFile.exists()) {
                println("ERROR: EPUB file does not exist at path: $epubFilePath")
                return
            }

            println("Reading EPUB file: $epubFilePath")
            val epubInputStream: InputStream = epubFile.inputStream()

            // epub parse
            val book = EpubReader().readEpub(epubInputStream)

            val extractedText = StringBuilder()
            book.spine.spineReferences.forEach { spineRef ->
                val resource = spineRef.resource
                val content = resource.reader.readText()
                extractedText.append(content).append("\n")
            }

            val outputFile = File(outputTextFilePath)
            FileWriter(outputFile).use { writer ->
                writer.write(extractedText.toString())
            }


            val firstLine = outputFile.useLines { it.firstOrNull() ?: "File is empty" }
            println("First line of the output file: $firstLine")

            println("Text successfully extracted and saved to $outputTextFilePath")

        } catch (e: Exception) {
            e.printStackTrace()
            println("An error occurred: ${e.message}")
        }
    }
}
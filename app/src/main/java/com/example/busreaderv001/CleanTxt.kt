package com.example.busreaderv001

import java.io.File
fun cleanTextFile(filePath: String): List<String> {
    val file = File(filePath)

    if (!file.exists() || !file.isFile) {
        throw IllegalArgumentException("Invalid file path: $filePath")
    }

    val cleanedLines = mutableListOf<String>()
    val sentenceBuffer = StringBuilder()

    file.forEachLine { line ->
        // Remove unwanted elements as before
        var cleanedLine = line.replace(Regex("""<\?xml.*?\?>"""), "") // Remove XML declarations
        cleanedLine = cleanedLine.replace(Regex("""\{.*?\}"""), "") // Remove curly braces
        cleanedLine = cleanedLine.replace(Regex("""<.*?>"""), "") // Remove HTML tags
        cleanedLine = cleanedLine.replace(Regex("""\[.*?\]"""), "") // Remove square brackets
        cleanedLine = cleanedLine.replace(Regex("""href=["'].*?["']"""), "") // Remove href attributes
        cleanedLine = cleanedLine.replace(Regex("""xmlns:[a-z]+=["'].*?["']"""), "") // Remove xmlns attributes
        cleanedLine = cleanedLine.trim() // Remove extra whitespace

        if (cleanedLine.isEmpty()) return@forEachLine // Skip empty lines

        // Check if the line is purely numeric
        if (cleanedLine.matches(Regex("""^\d+$"""))) {
            // Flush any accumulated sentence buffer before adding the number
            if (sentenceBuffer.isNotEmpty()) {
                cleanedLines.add(sentenceBuffer.toString().trim())
                sentenceBuffer.clear()
            }
            cleanedLines.add(cleanedLine) // Add the number as its own line
        } else {
            // Split the line into sentences
            val sentences = cleanedLine.split(Regex("""(?<=[.!?])\s+"""))
            for (sentence in sentences) {
                if (sentence.matches(Regex("""^\d+$"""))) {
                    // If the sentence is a number, add it as is
                    cleanedLines.add(sentence)
                } else {
                    // Append to buffer if the sentence is incomplete
                    if (sentence.endsWith(".") || sentence.endsWith("?") || sentence.endsWith("!")) {
                        // Sentence is complete, add to lines
                        cleanedLines.add((sentenceBuffer.append(sentence)).toString().trim())
                        sentenceBuffer.clear()
                    } else {
                        // Sentence is incomplete, accumulate it
                        sentenceBuffer.append(" ").append(sentence)
                    }
                }
            }
        }
    }

    // Flush any remaining sentence buffer
    if (sentenceBuffer.isNotEmpty()) {
        cleanedLines.add(sentenceBuffer.toString().trim())
    }

    return cleanedLines
}

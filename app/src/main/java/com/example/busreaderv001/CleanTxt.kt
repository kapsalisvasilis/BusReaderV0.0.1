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
        //remove non texted
        var cleanedLine = line.replace(Regex("""<\?xml.*?\?>"""), "") // Remove XML declarations
        cleanedLine = cleanedLine.replace(Regex("""\{.*?\}"""), "") // Remove curly braces
        cleanedLine = cleanedLine.replace(Regex("""<.*?>"""), "") // Remove HTML tags
        cleanedLine = cleanedLine.replace(Regex("""\[.*?]"""), "") // Remove square brackets
        cleanedLine = cleanedLine.replace(Regex("""href=["'].*?["']"""), "") // Remove href attributes
        cleanedLine = cleanedLine.replace(Regex("""xmlns:[a-z]+=["'].*?["']"""), "") // Remove xmlns attributes
        cleanedLine = cleanedLine.trim() // Remove extra whitespace

        if (cleanedLine.isEmpty()) return@forEachLine // Skip empty lines

        //check if the line is purely numeric
        if (cleanedLine.matches(Regex("""^\d+$"""))) {


            if (sentenceBuffer.isNotEmpty()) {
                cleanedLines.add(sentenceBuffer.toString().trim())
                sentenceBuffer.clear()
            }
            cleanedLines.add(cleanedLine) //add the number as its own line
        } else {
            //split into sentences
            val sentences = cleanedLine.split(Regex("""(?<=[.!?])\s+"""))
            for (sentence in sentences) {
                if (sentence.matches(Regex("""^\d+$"""))) {
                    //if the sentence is a number, add it as is
                    cleanedLines.add(sentence)
                } else {
                    //append to buffer if the sentence is incomplete
                    if (sentence.endsWith(".") || sentence.endsWith("?") || sentence.endsWith("!")) {
                        //sentence complete, add to lines
                        cleanedLines.add((sentenceBuffer.append(sentence)).toString().trim())
                        sentenceBuffer.clear()
                    } else {
                        // Sentence incomplete aopedb
                        sentenceBuffer.append(" ").append(sentence)
                    }
                }
            }
        }
    }

    if (sentenceBuffer.isNotEmpty()) {
        cleanedLines.add(sentenceBuffer.toString().trim())
    }

    return cleanedLines
}

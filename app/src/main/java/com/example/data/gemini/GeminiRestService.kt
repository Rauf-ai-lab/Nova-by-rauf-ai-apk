package com.example.data.gemini

import com.example.domain.model.ConceptExplanation
import com.example.domain.model.FlashcardItem
import com.example.domain.model.QuestionType
import com.example.domain.model.QuickRevisionGuide
import com.example.domain.model.QuizQuestion
import com.example.domain.model.QuizSession
import com.example.domain.model.ShortAnswerEvaluation
import com.example.domain.model.SolutionStep
import com.example.domain.model.StepByStepSolution
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiEndpoints {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRestRequest
    ): GeminiRestResponse
}

class GeminiRestService {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiEndpoints = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeminiApiEndpoints::class.java)

    companion object {
        const val MODEL_FLASH = "gemini-3.5-flash"
        const val MODEL_PRO = "gemini-3.1-pro-preview"

        const val ZOYA_SYSTEM_PROMPT = """You are ZOYA, the personal AI Study Companion inside the NOVA BY RAUF Android application.
Your personality:
- Young, sharp, confident, witty, friendly, motivating, and emotionally responsive.
- You teach students with deep clarity rather than dumping blind answers.
- When solving problems or explaining concepts, break them down intuitively.
- Keep tone uplifting, engaging, with light study humor ("Okay genius, let's break this down", "One step at a time, we're studying, not fighting a final boss").
- Focus exclusively on studying, academic mastery, active recall, problem solving, quizzes, and revision."""
    }

    suspend fun validateApiKey(apiKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val testRequest = GeminiRestRequest(
                contents = listOf(
                    RestContent(parts = listOf(RestPart(text = "Respond with 'OK' if you receive this.")))
                ),
                generationConfig = RestGenerationConfig(temperature = 0.1f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, testRequest)
            if (response.candidates?.isNotEmpty() == true) {
                Result.success(true)
            } else if (response.error != null) {
                Result.failure(Exception(response.error.message ?: "Invalid API Key or quota error"))
            } else {
                Result.failure(Exception("Could not validate key with Gemini server."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chatWithZoya(apiKey: String, userMessage: String, history: List<Pair<String, String>> = emptyList()): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentList = mutableListOf<RestContent>()
            for ((role, text) in history.takeLast(6)) {
                contentList.add(RestContent(role = if (role == "user") "user" else "model", parts = listOf(RestPart(text = text))))
            }
            contentList.add(RestContent(role = "user", parts = listOf(RestPart(text = userMessage))))

            val request = GeminiRestRequest(
                contents = contentList,
                systemInstruction = RestContent(parts = listOf(RestPart(text = ZOYA_SYSTEM_PROMPT))),
                generationConfig = RestGenerationConfig(temperature = 0.7f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Zoya couldn't formulate a response right now. Please try again!"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun explainConcept(
        apiKey: String,
        topic: String,
        depthLevel: String
    ): Result<ConceptExplanation> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Explain the academic concept "$topic" at the "$depthLevel" level.
Format your answer strictly with these exact section headers:
[QUICK_SUMMARY]
One or two punchy sentences summarizing the core idea in Zoya's encouraging style.

[DEEP_EXPLANATION]
Detailed, step-by-step academic explanation of the mechanism/principles.

[ANALOGY]
A memorable real-world analogy to make it stick instantly.

[KEY_POINTS]
- Bullet point 1
- Bullet point 2
- Bullet point 3

[COMMON_MISTAKES]
- Common trap 1
- Common trap 2

[PRACTICE_DOUBT]
A quick reflection question or challenge for the student."""

            val request = GeminiRestRequest(
                contents = listOf(RestContent(parts = listOf(RestPart(text = prompt)))),
                systemInstruction = RestContent(parts = listOf(RestPart(text = ZOYA_SYSTEM_PROMPT))),
                generationConfig = RestGenerationConfig(temperature = 0.6f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            Result.success(parseConceptExplanation(topic, depthLevel, raw))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun solveProblemStepByStep(
        apiKey: String,
        problemStatement: String,
        subject: String
    ): Result<StepByStepSolution> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Solve this $subject problem step-by-step for a student.
Problem: "$problemStatement"

Structure your response strictly as:
[CONCEPT]
Identified core principle / formula

[APPROACH]
Strategic plan before calculating

[STEP 1: Title]
Action / math working
WHY: Explanation of why we did this step

[STEP 2: Title]
Action / math working
WHY: Explanation of why we did this step

[STEP 3: Title]
Action / math working
WHY: Explanation of why we did this step

[FINAL_ANSWER]
Exact final result with units

[VERIFICATION]
Quick sanity check

[PRACTICE_CHALLENGE]
A similar question for the student to practice."""

            val request = GeminiRestRequest(
                contents = listOf(RestContent(parts = listOf(RestPart(text = prompt)))),
                systemInstruction = RestContent(parts = listOf(RestPart(text = ZOYA_SYSTEM_PROMPT))),
                generationConfig = RestGenerationConfig(temperature = 0.3f)
            )
            val response = api.generateContent(MODEL_PRO, apiKey, request)
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            Result.success(parseProblemSolution(problemStatement, subject, raw))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateQuiz(
        apiKey: String,
        topic: String,
        count: Int = 4,
        difficulty: String = "Medium",
        questionTypeFilter: String = "All Types"
    ): Result<QuizSession> = withContext(Dispatchers.IO) {
        try {
            val typeInstructions = when (questionTypeFilter) {
                "Multiple Choice" -> "Generate ONLY Multiple Choice questions."
                "True / False" -> "Generate ONLY True/False questions."
                "Fill in the Blank" -> "Generate ONLY Fill-in-the-blanks questions (with a clear blank indicated by _______)."
                "Short Answer" -> "Generate ONLY Short Answer conceptual questions."
                else -> "Include a rich, balanced mix of: 1) Multiple Choice, 2) True/False, 3) Fill-in-the-blanks, and 4) Short Answer questions."
            }

            val prompt = """Generate $count academic study quiz questions for "$topic" at difficulty level "$difficulty".
$typeInstructions

For each question, strictly output in this format:
---QUESTION---
TYPE: MULTIPLE_CHOICE (or TRUE_FALSE, FILL_IN_BLANKS, SHORT_ANSWER)
QUESTION: The full question text (or sentence with _______ for fill in the blanks)
OPTIONS:
A) Option A (Required for MULTIPLE_CHOICE; for TRUE_FALSE use "A) True\nB) False")
B) Option B
C) Option C
D) Option D
CORRECT: A (or B, C, D for choices; or "True"/"False" for TRUE_FALSE; or the exact word/phrase for FILL_IN_BLANKS and model answer for SHORT_ANSWER)
EXPLANATION: Zoya's thorough, encouraging breakdown of why this answer is correct and the underlying scientific/academic principle.
MISTAKES: Detailed explanation of common student mistakes, traps, or why incorrect options/misconceptions are flawed.
WEAK_CONCEPT: Specific concept or subtopic name the student should revise if they struggled with this question."""

            val request = GeminiRestRequest(
                contents = listOf(RestContent(parts = listOf(RestPart(text = prompt)))),
                systemInstruction = RestContent(parts = listOf(RestPart(text = ZOYA_SYSTEM_PROMPT))),
                generationConfig = RestGenerationConfig(temperature = 0.5f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            val questions = parseQuizQuestions(raw, topic)
            Result.success(QuizSession(topic, difficulty, questions, questionTypeFilter))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun evaluateShortAnswer(
        apiKey: String,
        question: String,
        modelAnswer: String,
        studentAnswer: String
    ): Result<ShortAnswerEvaluation> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Evaluate this student's short answer response in your persona as Zoya.
Question: "$question"
Model Expected Answer: "$modelAnswer"
Student's Answer: "$studentAnswer"

Provide evaluation strictly in this format:
[GRADE] Full Credit (or Partial Credit or Needs Review)
[IS_CORRECT] true (or false)
[EXPLANATION] Zoya's explanation of what was accurate and the key conceptual truth.
[MISTAKES] What was missed, inaccurate, or imprecise in student's response.
[WEAK_CONCEPT] The specific subtopic the student needs to reinforce."""

            val request = GeminiRestRequest(
                contents = listOf(RestContent(parts = listOf(RestPart(text = prompt)))),
                systemInstruction = RestContent(parts = listOf(RestPart(text = ZOYA_SYSTEM_PROMPT))),
                generationConfig = RestGenerationConfig(temperature = 0.3f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            val grade = raw.lines().firstOrNull { it.startsWith("[GRADE]") }?.removePrefix("[GRADE]")?.trim() ?: "Needs Review"
            val isCorrect = raw.lines().firstOrNull { it.startsWith("[IS_CORRECT]") }?.contains("true", true) == true || grade.contains("Full", true)
            
            fun extractSection(tag: String, nextTag: String?): String {
                val start = raw.indexOf(tag)
                if (start == -1) return ""
                val contentStart = start + tag.length
                val end = if (nextTag != null) raw.indexOf(nextTag, contentStart) else raw.length
                return if (end != -1) raw.substring(contentStart, end).trim() else raw.substring(contentStart).trim()
            }

            val explanation = extractSection("[EXPLANATION]", "[MISTAKES]").ifEmpty { "Good effort on this conceptual question." }
            val mistakes = extractSection("[MISTAKES]", "[WEAK_CONCEPT]").ifEmpty { "Review the core definition and key keywords." }
            val weakConcept = extractSection("[WEAK_CONCEPT]", null).ifEmpty { "Core principles" }

            Result.success(
                ShortAnswerEvaluation(
                    isCorrect = isCorrect,
                    scoreGrade = grade,
                    explanation = explanation,
                    mistakeAnalysis = mistakes,
                    weakConcept = weakConcept
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateFlashcards(
        apiKey: String,
        topic: String,
        count: Int = 5
    ): Result<List<FlashcardItem>> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Generate $count active-recall study flashcards for "$topic".
Format strictly as:
---CARD---
Q: Front question / term to define
A: Back concise, high-impact answer
DIFF: Easy/Medium/Hard"""

            val request = GeminiRestRequest(
                contents = listOf(RestContent(parts = listOf(RestPart(text = prompt)))),
                systemInstruction = RestContent(parts = listOf(RestPart(text = ZOYA_SYSTEM_PROMPT))),
                generationConfig = RestGenerationConfig(temperature = 0.5f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            val cards = parseFlashcards(topic, raw)
            Result.success(cards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateQuickRevision(
        apiKey: String,
        topic: String
    ): Result<QuickRevisionGuide> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Generate an intensive rapid-fire study revision guide for "$topic".
Format strictly as:
[SUBJECT]
Subject Name

[DEFINITIONS]
- Term: Definition

[FORMULAS]
- Formula / Law name: Formula equation or statement

[HIGH_YIELD_FACTS]
- High yield fact 1
- High yield fact 2

[WEAK_SPOTS]
- Common misconception 1
- Tricky trap 2"""

            val request = GeminiRestRequest(
                contents = listOf(RestContent(parts = listOf(RestPart(text = prompt)))),
                systemInstruction = RestContent(parts = listOf(RestPart(text = ZOYA_SYSTEM_PROMPT))),
                generationConfig = RestGenerationConfig(temperature = 0.5f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            Result.success(parseRevisionGuide(topic, raw))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Parsing helpers
    private fun parseConceptExplanation(topic: String, depthLevel: String, raw: String): ConceptExplanation {
        fun extractSection(header: String, nextHeader: String? = null): String {
            val startIdx = raw.indexOf(header)
            if (startIdx == -1) return ""
            val contentStart = startIdx + header.length
            val endIdx = if (nextHeader != null) raw.indexOf(nextHeader, contentStart) else raw.length
            return if (endIdx != -1) raw.substring(contentStart, endIdx).trim() else raw.substring(contentStart).trim()
        }

        val summary = extractSection("[QUICK_SUMMARY]", "[DEEP_EXPLANATION]").ifEmpty { "Key concept overview for $topic." }
        val deep = extractSection("[DEEP_EXPLANATION]", "[ANALOGY]").ifEmpty { raw }
        val analogy = extractSection("[ANALOGY]", "[KEY_POINTS]").ifEmpty { "Think of this as a connected network of ideas working in sync." }
        val keyPointsRaw = extractSection("[KEY_POINTS]", "[COMMON_MISTAKES]")
        val commonMistakesRaw = extractSection("[COMMON_MISTAKES]", "[PRACTICE_DOUBT]")
        val practice = extractSection("[PRACTICE_DOUBT]").ifEmpty { "How would you explain $topic to a friend?" }

        val keyPoints = keyPointsRaw.lines().map { it.trim().removePrefix("-").trim() }.filter { it.isNotBlank() }
        val mistakes = commonMistakesRaw.lines().map { it.trim().removePrefix("-").trim() }.filter { it.isNotBlank() }

        return ConceptExplanation(
            topic = topic,
            depthLevel = depthLevel,
            quickSummary = summary,
            deepExplanation = deep,
            realWorldAnalogy = analogy,
            keyPointsAndFormulas = keyPoints.ifEmpty { listOf("Core principle of $topic", "Key application") },
            commonMistakes = mistakes.ifEmpty { listOf("Assuming answers without checking conditions") },
            quickPracticeDoubt = practice
        )
    }

    private fun parseProblemSolution(problem: String, subject: String, raw: String): StepByStepSolution {
        fun extract(header: String, next: String?): String {
            val start = raw.indexOf(header)
            if (start == -1) return ""
            val contentStart = start + header.length
            val end = if (next != null) raw.indexOf(next, contentStart) else raw.length
            return if (end != -1) raw.substring(contentStart, end).trim() else raw.substring(contentStart).trim()
        }

        val concept = extract("[CONCEPT]", "[APPROACH]").ifEmpty { "$subject Problem Solving" }
        val approach = extract("[APPROACH]", "[STEP 1").ifEmpty { "Deconstruct given values, identify equations, and solve systematically." }
        val finalAnswer = extract("[FINAL_ANSWER]", "[VERIFICATION]").ifEmpty { "Calculated answer based on steps." }
        val verification = extract("[VERIFICATION]", "[PRACTICE_CHALLENGE]").ifEmpty { "Check units and boundary conditions." }
        val challenge = extract("[PRACTICE_CHALLENGE]", null).ifEmpty { "Try solving with doubled initial values!" }

        val steps = mutableListOf<SolutionStep>()
        val stepRegex = Regex("""\[STEP\s*(\d+):?\s*([^\]]*)\]([\s\S]*?)(?=\[STEP|\Q[FINAL_ANSWER]\E|$)""")
        val matches = stepRegex.findAll(raw)
        var count = 1
        for (match in matches) {
            val num = match.groupValues[1].toIntOrNull() ?: count
            val title = match.groupValues[2].trim().ifEmpty { "Step $num" }
            val body = match.groupValues[3].trim()
            val whySplit = body.split(Regex("""(?i)WHY:"""))
            val calculation = whySplit[0].trim()
            val why = if (whySplit.size > 1) whySplit[1].trim() else "To isolate key variables."
            steps.add(SolutionStep(num, title, calculation, why))
            count++
        }

        if (steps.isEmpty()) {
            steps.add(SolutionStep(1, "Execute Solution", raw.take(500), "Understand the primary equation"))
        }

        return StepByStepSolution(
            problem = problem,
            subject = subject,
            identifiedConcept = concept,
            strategyApproach = approach,
            steps = steps,
            finalAnswer = finalAnswer,
            verificationNote = verification,
            practiceChallenge = challenge
        )
    }

    private fun parseQuizQuestions(raw: String, topic: String): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val blocks = raw.split("---QUESTION---").filter { it.isNotBlank() }

        var idGen = 1L
        for (block in blocks) {
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) continue

            var qType = QuestionType.MULTIPLE_CHOICE
            var qText = ""
            val options = mutableListOf<String>()
            var correctIdx = 0
            var correctAnswerText = ""
            var explanation = ""
            var mistakes = ""
            var weakConcept = topic

            for (line in lines) {
                when {
                    line.startsWith("TYPE:", ignoreCase = true) -> {
                        val typeStr = line.substring(5).trim().uppercase()
                        qType = when {
                            typeStr.contains("TRUE") || typeStr.contains("FALSE") -> QuestionType.TRUE_FALSE
                            typeStr.contains("FILL") || typeStr.contains("BLANK") -> QuestionType.FILL_IN_BLANKS
                            typeStr.contains("SHORT") || typeStr.contains("ANSWER") -> QuestionType.SHORT_ANSWER
                            else -> QuestionType.MULTIPLE_CHOICE
                        }
                    }
                    line.startsWith("QUESTION:", ignoreCase = true) -> {
                        qText = line.substring(9).trim()
                    }
                    line.startsWith("A)") || line.startsWith("A.") -> options.add(line.substring(2).trim())
                    line.startsWith("B)") || line.startsWith("B.") -> options.add(line.substring(2).trim())
                    line.startsWith("C)") || line.startsWith("C.") -> options.add(line.substring(2).trim())
                    line.startsWith("D)") || line.startsWith("D.") -> options.add(line.substring(2).trim())
                    line.startsWith("CORRECT:", ignoreCase = true) -> {
                        val value = line.substring(8).trim()
                        correctAnswerText = value
                        val upper = value.uppercase()
                        if (upper.contains("A") && !upper.contains("TRUE") && !upper.contains("FALSE")) correctIdx = 0
                        else if (upper.contains("B")) correctIdx = 1
                        else if (upper.contains("C")) correctIdx = 2
                        else if (upper.contains("D")) correctIdx = 3
                        else if (upper.startsWith("TRUE")) correctIdx = 0
                        else if (upper.startsWith("FALSE")) correctIdx = 1
                    }
                    line.startsWith("EXPLANATION:", ignoreCase = true) -> {
                        explanation = line.substring(12).trim()
                    }
                    line.startsWith("MISTAKES:", ignoreCase = true) || line.startsWith("MISTAKE:", ignoreCase = true) -> {
                        mistakes = line.substring(line.indexOf(":") + 1).trim()
                    }
                    line.startsWith("WEAK_CONCEPT:", ignoreCase = true) -> {
                        weakConcept = line.substring(13).trim()
                    }
                    qText.isEmpty() && !line.startsWith("---") && !line.startsWith("OPTIONS") -> {
                        qText = line
                    }
                }
            }

            // Fallback heuristics if type wasn't explicitly tagged
            if (qType == QuestionType.MULTIPLE_CHOICE) {
                if (options.isEmpty() && (qText.contains("_______") || qText.contains("___"))) {
                    qType = QuestionType.FILL_IN_BLANKS
                } else if (options.size == 2 && options[0].equals("True", true)) {
                    qType = QuestionType.TRUE_FALSE
                } else if (options.isEmpty() && correctAnswerText.length > 1) {
                    qType = QuestionType.SHORT_ANSWER
                }
            }

            if (qType == QuestionType.TRUE_FALSE) {
                if (options.isEmpty()) {
                    options.add("True")
                    options.add("False")
                }
            } else if (qType == QuestionType.MULTIPLE_CHOICE) {
                if (options.size < 2) {
                    // Provide fallback options if parsing missed some
                    options.add(correctAnswerText.ifEmpty { "Primary principle" })
                    options.add("Inverted hypothesis")
                    options.add("Secondary exception")
                    options.add("None of the above")
                }
            }

            if (explanation.isEmpty()) {
                explanation = "According to fundamental principles of $topic, this is the accurate formulation."
            }
            if (mistakes.isEmpty()) {
                mistakes = "A common mistake is overlooking boundary conditions or confusing related terminology."
            }
            if (weakConcept.isEmpty()) {
                weakConcept = topic
            }

            questions.add(
                QuizQuestion(
                    id = idGen++,
                    type = qType,
                    question = qText.ifEmpty { "Analyze the core behavior of $topic." },
                    options = options,
                    correctIndex = correctIdx,
                    correctAnswerText = correctAnswerText,
                    explanation = explanation,
                    mistakeAnalysis = mistakes,
                    weakConcept = weakConcept
                )
            )
        }

        return questions.ifEmpty {
            // Fallback sample question if parsing was empty
            listOf(
                QuizQuestion(
                    id = 1L,
                    type = QuestionType.MULTIPLE_CHOICE,
                    question = "What is the primary defining principle of $topic?",
                    options = listOf("Core mechanism and definition", "An unrelated empirical constant", "A secondary experimental artifact", "A disproven historical theory"),
                    correctIndex = 0,
                    correctAnswerText = "A",
                    explanation = "$topic is characterized by its fundamental laws and observed mechanisms.",
                    mistakeAnalysis = "Watch out for confusing historical theories with validated principles.",
                    weakConcept = topic
                )
            )
        }
    }

    private fun parseFlashcards(topic: String, raw: String): List<FlashcardItem> {
        val cards = mutableListOf<FlashcardItem>()
        val blocks = raw.split("---CARD---").filter { it.isNotBlank() }
        var id = 1L
        for (block in blocks) {
            var q = ""
            var a = ""
            var diff = "Medium"
            for (line in block.lines().map { it.trim() }) {
                when {
                    line.startsWith("Q:", ignoreCase = true) -> q = line.substring(2).trim()
                    line.startsWith("A:", ignoreCase = true) -> a = line.substring(2).trim()
                    line.startsWith("DIFF:", ignoreCase = true) -> diff = line.substring(5).trim()
                }
            }
            if (q.isNotBlank() && a.isNotBlank()) {
                cards.add(FlashcardItem(id++, "Study", topic, q, a, diff, 0))
            }
        }
        return cards
    }

    private fun parseRevisionGuide(topic: String, raw: String): QuickRevisionGuide {
        fun extractList(header: String, next: String?): List<String> {
            val start = raw.indexOf(header)
            if (start == -1) return emptyList()
            val contentStart = start + header.length
            val end = if (next != null) raw.indexOf(next, contentStart) else raw.length
            val chunk = if (end != -1) raw.substring(contentStart, end) else raw.substring(contentStart)
            return chunk.lines().map { it.trim().removePrefix("-").trim() }.filter { it.isNotBlank() }
        }

        val subject = raw.lines().firstOrNull { it.contains("[SUBJECT]", true) }
            ?.replace("[SUBJECT]", "", true)?.trim()?.ifEmpty { "General Study" } ?: "General Study"

        val definitions = extractList("[DEFINITIONS]", "[FORMULAS]")
        val formulas = extractList("[FORMULAS]", "[HIGH_YIELD_FACTS]")
        val facts = extractList("[HIGH_YIELD_FACTS]", "[WEAK_SPOTS]")
        val weakSpots = extractList("[WEAK_SPOTS]", null)

        return QuickRevisionGuide(
            topic = topic,
            subject = subject,
            keyDefinitions = definitions.ifEmpty { listOf("$topic: Primary subject concept") },
            coreFormulas = formulas.ifEmpty { listOf("Fundamental equation of $topic") },
            highYieldFacts = facts.ifEmpty { listOf("Most frequently tested area in exams") },
            weakAreaWatchlist = weakSpots.ifEmpty { listOf("Misinterpreting question assumptions") }
        )
    }
}

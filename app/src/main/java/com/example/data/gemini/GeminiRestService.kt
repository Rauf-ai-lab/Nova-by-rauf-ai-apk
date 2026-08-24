package com.example.data.gemini

import com.example.domain.model.ConceptExplanation
import com.example.domain.model.ExamModePack
import com.example.domain.model.FlashcardItem
import com.example.domain.model.OneShotLecture
import com.example.domain.model.OneShotSection
import com.example.domain.model.QuestionType
import com.example.domain.model.QuickRevisionGuide
import com.example.domain.model.QuizQuestion
import com.example.domain.model.QuizSession
import com.example.domain.model.ShortAnswerEvaluation
import com.example.domain.model.SolutionStep
import com.example.domain.model.StepByStepSolution
import com.example.domain.model.StudentProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
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
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
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

        fun buildSystemPrompt(profile: StudentProfile? = null): String {
            val boardContext = if (profile != null) {
                """
STUDENT PROFILE CONTEXT:
- Board: ${profile.boardName} (${profile.state})
- Class / Grade: ${profile.classLevel}
- Current Subject: ${profile.subject}
- Preferred Language: ${profile.language}

PEDAGOGICAL DIRECTIVES:
- Tailor explanations, difficulty, and examples specifically for ${profile.classLevel} students studying under ${profile.boardName}.
- Focus on high-yield curriculum concepts, clear step-by-step logic, active recall, and exam mastery.
- Do NOT copy textbooks verbatim. Generate fresh, original explanations, analogies, and practice problems.
- If language is "Hinglish / Hindi" or "Urdu", explain core technical terms clearly with contextual bilingual explanations while keeping mathematical/scientific notations standard.
- Tone: Young, sharp, confident, motivating, and encouraging. Never be robotic or generic.
                """.trimIndent()
            } else {
                "Tailor explanations for school and board curriculum with active recall and deep conceptual clarity."
            }

            return """You are ZOYA, the intelligent AI Study Companion inside the NOVA BY RAUF Android study platform.
$boardContext

COMMUNICATION RULES:
- ZOYA responds primarily in structured, beautifully readable TEXT.
- Break down concepts intuitively with headings, concise paragraphs, and bullet points.
- Celebrate curiosity, encourage mastery, and guide students through difficulties step by step.
- Focus strictly on academic study, homework help, conceptual doubt-solving, quizzes, revision, and exam preparation."""
        }
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

    suspend fun chatWithZoya(
        apiKey: String,
        userMessage: String,
        history: List<Pair<String, String>> = emptyList(),
        profile: StudentProfile? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentList = mutableListOf<RestContent>()
            for ((role, text) in history.takeLast(6)) {
                contentList.add(RestContent(role = if (role == "user") "user" else "model", parts = listOf(RestPart(text = text))))
            }
            contentList.add(RestContent(role = "user", parts = listOf(RestPart(text = userMessage))))

            val request = GeminiRestRequest(
                contents = contentList,
                systemInstruction = RestContent(parts = listOf(RestPart(text = buildSystemPrompt(profile)))),
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
        depthLevel: String,
        profile: StudentProfile? = null
    ): Result<ConceptExplanation> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Explain the academic concept "$topic" at the "$depthLevel" level for a ${profile?.classLevel ?: "Class 10"} student studying ${profile?.subject ?: "Science"} (${profile?.boardName ?: "Board"}).
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
                systemInstruction = RestContent(parts = listOf(RestPart(text = buildSystemPrompt(profile)))),
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
        subject: String,
        profile: StudentProfile? = null
    ): Result<StepByStepSolution> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Solve this $subject problem step-by-step for a ${profile?.classLevel ?: "Class 10"} student (${profile?.boardName ?: "Board"}).
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
                systemInstruction = RestContent(parts = listOf(RestPart(text = buildSystemPrompt(profile)))),
                generationConfig = RestGenerationConfig(temperature = 0.3f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
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
        questionTypeFilter: String = "All Types",
        profile: StudentProfile? = null
    ): Result<QuizSession> = withContext(Dispatchers.IO) {
        try {
            val typeInstructions = when (questionTypeFilter) {
                "Multiple Choice" -> "Generate ONLY Multiple Choice questions."
                "True / False" -> "Generate ONLY True/False questions."
                "Fill in the Blank" -> "Generate ONLY Fill-in-the-blanks questions (with a clear blank indicated by _______)."
                "Short Answer" -> "Generate ONLY Short Answer conceptual questions."
                else -> "Include a rich, balanced mix of: 1) Multiple Choice, 2) True/False, 3) Fill-in-the-blanks, and 4) Short Answer questions."
            }

            val prompt = """Generate $count academic study quiz questions for "$topic" at difficulty level "$difficulty" for a ${profile?.classLevel ?: "Class 10"} student (${profile?.boardName ?: "Board"}).
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
                systemInstruction = RestContent(parts = listOf(RestPart(text = buildSystemPrompt(profile)))),
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
        studentAnswer: String,
        profile: StudentProfile? = null
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
                systemInstruction = RestContent(parts = listOf(RestPart(text = buildSystemPrompt(profile)))),
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
        count: Int = 5,
        profile: StudentProfile? = null
    ): Result<List<FlashcardItem>> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Generate $count active-recall study flashcards for "$topic" (${profile?.subject ?: "Subject"}, ${profile?.classLevel ?: "Class 10"}, ${profile?.boardName ?: "Board"}).
Format strictly as:
---CARD---
Q: Front question / term to define
A: Back concise, high-impact answer
DIFF: Easy/Medium/Hard"""

            val request = GeminiRestRequest(
                contents = listOf(RestContent(parts = listOf(RestPart(text = prompt)))),
                systemInstruction = RestContent(parts = listOf(RestPart(text = buildSystemPrompt(profile)))),
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
        topic: String,
        profile: StudentProfile? = null
    ): Result<QuickRevisionGuide> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Generate an intensive rapid-fire study revision guide for "$topic" (${profile?.subject ?: "Subject"}, ${profile?.classLevel ?: "Class 10"}, ${profile?.boardName ?: "Board"}).
Format strictly as:
[SUBJECT]
${profile?.subject ?: "General Study"}

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
                systemInstruction = RestContent(parts = listOf(RestPart(text = buildSystemPrompt(profile)))),
                generationConfig = RestGenerationConfig(temperature = 0.5f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            Result.success(parseRevisionGuide(topic, raw))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateOneShotLecture(
        apiKey: String,
        chapterTopic: String,
        profile: StudentProfile
    ): Result<OneShotLecture> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Generate a complete, master-class ONE-SHOT LECTURE for the chapter/topic "$chapterTopic" for a student of ${profile.classLevel} studying ${profile.subject} under ${profile.boardName} (${profile.state}).
Follow these EXACT section tags to structure the full 14-part lecture:

[CHAPTER_OVERVIEW]
Provide a high-level summary of what this entire chapter is about and why it matters in exams and real life.

[LEARNING_OBJECTIVES]
- Objective 1
- Objective 2
- Objective 3

[IMPORTANT_CONCEPTS]
Breakdown of the primary core principles and mechanisms in this chapter.

[DETAILED_EXPLANATION]
Thorough, crystal-clear conceptual walk-through from basic intuition to advanced understanding.

[SIMPLE_EXAMPLES]
- Example 1: with simple breakdown
- Example 2: with simple breakdown

[REAL_WORLD_EXAMPLES]
- Real-world application 1
- Real-world application 2

[IMPORTANT_DEFINITIONS]
- Definition 1: exact statement
- Definition 2: exact statement

[IMPORTANT_FORMULAS]
- Formula/Law 1: equation, variables & units
- Formula/Law 2: equation, variables & units

[COMMON_MISTAKES]
- Trap 1: What students do wrong and how to avoid it
- Trap 2: Confusion between concepts

[FAQS]
Q: Frequently asked question 1?
A: Clear answer.
Q: Frequently asked question 2?
A: Clear answer.

[EXAM_FOCUSED_POINTS]
- High-yield scoring tip 1
- Common 3-mark or 5-mark question trend 2
- Derivation/Diagram reminder 3

[QUICK_REVISION]
Bullet-speed recap of essential points.

[PRACTICE_QUESTIONS]
- Practice Question 1 (Conceptual)
- Practice Question 2 (Numerical / Problem)
- Practice Question 3 (Exam style)

[FINAL_SUMMARY]
Final motivating closing statement by Zoya."""

            val request = GeminiRestRequest(
                contents = listOf(RestContent(parts = listOf(RestPart(text = prompt)))),
                systemInstruction = RestContent(parts = listOf(RestPart(text = buildSystemPrompt(profile)))),
                generationConfig = RestGenerationConfig(temperature = 0.5f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            Result.success(parseOneShotLecture(chapterTopic, profile, raw))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateExamModePack(
        apiKey: String,
        topic: String,
        profile: StudentProfile
    ): Result<ExamModePack> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Generate a high-yield EXAM MODE PACK for "$topic" tailored to ${profile.classLevel} ${profile.subject} for ${profile.boardName} (${profile.state}).
Format strictly with these tags:
[HIGH_PRIORITY_CONCEPTS]
- Concept 1
- Concept 2

[EXPECTED_QUESTIONS]
- Question 1 (Marks trend)
- Question 2 (Marks trend)

[COMMON_TRAPS]
- Trap 1 & Avoidance strategy
- Trap 2 & Avoidance strategy

[FORMULA_SHEET]
- Formula 1 (with conditions)
- Formula 2 (with conditions)

[DEFINITIONS]
- Exact definition 1
- Exact definition 2

[PRACTICE_DRILLS]
- Quick drill 1
- Quick drill 2"""

            val request = GeminiRestRequest(
                contents = listOf(RestContent(parts = listOf(RestPart(text = prompt)))),
                systemInstruction = RestContent(parts = listOf(RestPart(text = buildSystemPrompt(profile)))),
                generationConfig = RestGenerationConfig(temperature = 0.4f)
            )
            val response = api.generateContent(MODEL_FLASH, apiKey, request)
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            fun extractList(header: String, next: String?): List<String> {
                val start = raw.indexOf(header)
                if (start == -1) return emptyList()
                val contentStart = start + header.length
                val end = if (next != null) raw.indexOf(next, contentStart) else raw.length
                val chunk = if (end != -1) raw.substring(contentStart, end) else raw.substring(contentStart)
                return chunk.lines().map { it.trim().removePrefix("-").trim() }.filter { it.isNotBlank() }
            }

            Result.success(
                ExamModePack(
                    topic = topic,
                    subject = profile.subject,
                    board = profile.boardName,
                    classLevel = profile.classLevel,
                    highPriorityConcepts = extractList("[HIGH_PRIORITY_CONCEPTS]", "[EXPECTED_QUESTIONS]").ifEmpty { listOf("Core theoretical foundation of $topic", "Key derivations and applications") },
                    mostExpectedQuestions = extractList("[EXPECTED_QUESTIONS]", "[COMMON_TRAPS]").ifEmpty { listOf("Explain the working mechanism of $topic.", "State and derive the core relationship in $topic.") },
                    commonExamTraps = extractList("[COMMON_TRAPS]", "[FORMULA_SHEET]").ifEmpty { listOf("Forgetting SI units in final answers", "Confusing sign conventions") },
                    formulaCheatSheet = extractList("[FORMULA_SHEET]", "[DEFINITIONS]").ifEmpty { listOf("Standard formula for $topic") },
                    definitionsToMemorize = extractList("[DEFINITIONS]", "[PRACTICE_DRILLS]").ifEmpty { listOf("$topic: Prescribed board definition") },
                    quickPracticeProblems = extractList("[PRACTICE_DRILLS]", null).ifEmpty { listOf("Solve for standard conditions with given constants.") }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseOneShotLecture(chapterTopic: String, profile: StudentProfile, raw: String): OneShotLecture {
        fun extract(header: String, next: String? = null): String {
            val start = raw.indexOf(header)
            if (start == -1) return ""
            val contentStart = start + header.length
            val end = if (next != null) raw.indexOf(next, contentStart) else raw.length
            return if (end != -1) raw.substring(contentStart, end).trim() else raw.substring(contentStart).trim()
        }

        fun extractList(header: String, next: String?): List<String> {
            val chunk = extract(header, next)
            return chunk.lines().map { it.trim().removePrefix("-").removePrefix("•").trim() }.filter { it.isNotBlank() }
        }

        fun extractFaqs(header: String, next: String?): List<Pair<String, String>> {
            val chunk = extract(header, next)
            val list = mutableListOf<Pair<String, String>>()
            val lines = chunk.lines().map { it.trim() }.filter { it.isNotBlank() }
            var currentQ = ""
            var currentA = ""
            for (line in lines) {
                if (line.startsWith("Q:", true)) {
                    if (currentQ.isNotEmpty()) {
                        list.add(currentQ to currentA)
                        currentA = ""
                    }
                    currentQ = line.removePrefix("Q:").removePrefix("q:").trim()
                } else if (line.startsWith("A:", true)) {
                    currentA = line.removePrefix("A:").removePrefix("a:").trim()
                } else if (currentQ.isNotEmpty()) {
                    currentA += " $line"
                }
            }
            if (currentQ.isNotEmpty()) {
                list.add(currentQ to currentA)
            }
            return if (list.isNotEmpty()) list else listOf("What is the main takeaway?" to "Understanding the core mechanism of $chapterTopic thoroughly.")
        }

        val overview = extract("[CHAPTER_OVERVIEW]", "[LEARNING_OBJECTIVES]").ifEmpty { "Comprehensive master-class on $chapterTopic." }
        val objectives = extractList("[LEARNING_OBJECTIVES]", "[IMPORTANT_CONCEPTS]").ifEmpty { listOf("Master fundamental definitions", "Understand step-by-step principles", "Solve key exam problems") }
        val importantConcepts = extract("[IMPORTANT_CONCEPTS]", "[DETAILED_EXPLANATION]").ifEmpty { "Core ideas and relationships that govern $chapterTopic." }
        val detailedExplanation = extract("[DETAILED_EXPLANATION]", "[SIMPLE_EXAMPLES]").ifEmpty { raw }
        val simpleExamples = extractList("[SIMPLE_EXAMPLES]", "[REAL_WORLD_EXAMPLES]").ifEmpty { listOf("Basic textbook example illustrating the concept.") }
        val realWorldExamples = extractList("[REAL_WORLD_EXAMPLES]", "[IMPORTANT_DEFINITIONS]").ifEmpty { listOf("Everyday physical application and technology usage.") }
        val definitions = extractList("[IMPORTANT_DEFINITIONS]", "[IMPORTANT_FORMULAS]").ifEmpty { listOf("$chapterTopic: Standard definition") }
        val formulas = extractList("[IMPORTANT_FORMULAS]", "[COMMON_MISTAKES]").ifEmpty { listOf("Primary governing equations") }
        val mistakes = extractList("[COMMON_MISTAKES]", "[FAQS]").ifEmpty { listOf("Overlooking boundary conditions", "Unit conversion errors") }
        val faqs = extractFaqs("[FAQS]", "[EXAM_FOCUSED_POINTS]")
        val examPoints = extractList("[EXAM_FOCUSED_POINTS]", "[QUICK_REVISION]").ifEmpty { listOf("Frequently asked 3-mark conceptual questions", "Key diagrams and labels to memorize") }
        val quickRevision = extract("[QUICK_REVISION]", "[PRACTICE_QUESTIONS]").ifEmpty { "Quick summary of formulas and definitions." }
        val practiceQuestions = extractList("[PRACTICE_QUESTIONS]", "[FINAL_SUMMARY]").ifEmpty { listOf("Define $chapterTopic and state two key applications.", "Solve a standard numerical problem based on the core formula.") }
        val summary = extract("[FINAL_SUMMARY]").ifEmpty { "Great job mastering $chapterTopic! You're ready to ace your exam." }

        val structuredSections = listOf(
            OneShotSection(1, "Chapter Overview", "High-level orientation", overview),
            OneShotSection(2, "Learning Objectives", "Goals for this session", objectives.joinToString("\n• ", prefix = "• ")),
            OneShotSection(3, "Important Concepts", "Core theoretical foundations", importantConcepts),
            OneShotSection(4, "Detailed Explanation", "Step-by-step breakdown", detailedExplanation),
            OneShotSection(5, "Simple Examples", "Step-by-step illustrations", simpleExamples.joinToString("\n\n")),
            OneShotSection(6, "Real-world Examples", "Practical applications", realWorldExamples.joinToString("\n\n")),
            OneShotSection(7, "Important Definitions", "Precise academic statements", definitions.joinToString("\n• ", prefix = "• ")),
            OneShotSection(8, "Important Formulas", "Equations & Laws", formulas.joinToString("\n• ", prefix = "• ")),
            OneShotSection(9, "Common Mistakes", "Traps to avoid in exams", mistakes.joinToString("\n• ", prefix = "• ")),
            OneShotSection(10, "Frequently Asked Questions", "Conceptual Doubts", faqs.joinToString("\n\n") { "Q: ${it.first}\nA: ${it.second}" }),
            OneShotSection(11, "Exam-Focused Points", "High-yield scoring tips", examPoints.joinToString("\n• ", prefix = "• ")),
            OneShotSection(12, "Quick Revision", "Rapid recap", quickRevision),
            OneShotSection(13, "Practice Questions", "Active recall drills", practiceQuestions.joinToString("\n\n")),
            OneShotSection(14, "Final Summary", "Closing thoughts & mastery", summary)
        )

        return OneShotLecture(
            topic = chapterTopic,
            subject = profile.subject,
            board = profile.boardName,
            classLevel = profile.classLevel,
            chapterOverview = overview,
            learningObjectives = objectives,
            importantConcepts = importantConcepts,
            detailedExplanation = detailedExplanation,
            simpleExamples = simpleExamples,
            realWorldExamples = realWorldExamples,
            importantDefinitions = definitions,
            importantFormulas = formulas,
            commonMistakes = mistakes,
            frequentlyAskedQuestions = faqs,
            examFocusedPoints = examPoints,
            quickRevision = quickRevision,
            practiceQuestions = practiceQuestions,
            finalSummary = summary,
            sections = structuredSections
        )
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

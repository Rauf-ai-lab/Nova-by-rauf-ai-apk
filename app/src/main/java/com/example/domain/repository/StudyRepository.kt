package com.example.domain.repository

import com.example.data.gemini.GeminiRestService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FlashcardEntity
import com.example.data.local.entity.QuizQuestionEntity
import com.example.data.local.entity.RevisionTopicEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.data.storage.SecureApiKeyStorage
import com.example.domain.model.ChatMessage
import com.example.domain.model.ConceptExplanation
import com.example.domain.model.FlashcardItem
import com.example.domain.model.QuestionType
import com.example.domain.model.QuickRevisionGuide
import com.example.domain.model.QuizSession
import com.example.domain.model.ShortAnswerEvaluation
import com.example.domain.model.Speaker
import com.example.domain.model.StepByStepSolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudyRepository(
    private val database: AppDatabase,
    private val secureStorage: SecureApiKeyStorage,
    private val restService: GeminiRestService
) {
    // API Key operations
    fun getApiKey(): String = secureStorage.getApiKey()
    fun hasValidApiKey(): Boolean = secureStorage.isKeyValidated()
    fun saveApiKey(apiKey: String, isValidated: Boolean) = secureStorage.saveApiKey(apiKey, isValidated)
    fun clearApiKey() = secureStorage.clearApiKey()
    fun getMaskedApiKey(): String = secureStorage.getMaskedApiKey()

    suspend fun validateApiKey(apiKey: String): Result<Boolean> = restService.validateApiKey(apiKey)

    // Flashcard local operations
    val allFlashcards: Flow<List<FlashcardEntity>> = database.flashcardDao().getAllFlashcards()

    suspend fun saveFlashcard(card: FlashcardItem) = withContext(Dispatchers.IO) {
        database.flashcardDao().insertFlashcard(
            FlashcardEntity(
                id = card.id,
                subject = card.subject,
                topic = card.topic,
                frontQuestion = card.question,
                backAnswer = card.answer,
                difficulty = card.difficulty,
                masteryLevel = card.masteryLevel
            )
        )
    }

    suspend fun updateFlashcardMastery(cardId: Long, newMastery: Int) = withContext(Dispatchers.IO) {
        val entity = FlashcardEntity(
            id = cardId,
            subject = "Study",
            topic = "General",
            frontQuestion = "",
            backAnswer = "",
            masteryLevel = newMastery,
            lastReviewedAt = System.currentTimeMillis()
        )
        // Update mastery
        database.flashcardDao().updateFlashcard(entity)
    }

    suspend fun deleteFlashcard(id: Long) = withContext(Dispatchers.IO) {
        database.flashcardDao().deleteById(id)
    }

    // Quiz records
    val allQuizRecords: Flow<List<QuizQuestionEntity>> = database.quizDao().getAllQuizRecords()

    suspend fun recordQuizSession(session: QuizSession) = withContext(Dispatchers.IO) {
        val sessionId = java.util.UUID.randomUUID().toString()
        val entities = session.questions.map { q ->
            QuizQuestionEntity(
                quizSessionId = sessionId,
                topic = session.topic,
                question = q.question,
                optionA = q.options.getOrElse(0) { if (q.type == QuestionType.SHORT_ANSWER || q.type == QuestionType.FILL_IN_BLANKS) q.correctAnswerText else "" },
                optionB = q.options.getOrElse(1) { "" },
                optionC = q.options.getOrElse(2) { "" },
                optionD = q.options.getOrElse(3) { "" },
                correctOptionIndex = q.correctIndex,
                explanation = "${q.explanation}\n[Mistake Analysis]: ${q.mistakeAnalysis}\n[Weak Concept]: ${q.weakConcept}",
                userSelectedOption = q.selectedIndex,
                isCorrect = q.isCorrect
            )
        }
        database.quizDao().insertAllQuizQuestions(entities)
    }

    // Study Sessions
    val allSessions: Flow<List<StudySessionEntity>> = database.studySessionDao().getAllSessions()

    suspend fun logSession(topic: String, mode: String, summary: String, durationSec: Long) = withContext(Dispatchers.IO) {
        database.studySessionDao().insertSession(
            StudySessionEntity(
                topic = topic,
                mode = mode,
                summary = summary,
                durationSeconds = durationSec
            )
        )
    }

    // Revision Topics
    val allRevisionTopics: Flow<List<RevisionTopicEntity>> = database.revisionDao().getAllRevisionTopics()

    suspend fun saveRevisionGuide(guide: QuickRevisionGuide) = withContext(Dispatchers.IO) {
        database.revisionDao().insertOrUpdate(
            RevisionTopicEntity(
                topic = guide.topic,
                subject = guide.subject,
                keyPoints = guide.highYieldFacts.joinToString("\n• "),
                formulas = guide.coreFormulas.joinToString("\n• "),
                weakAreas = guide.weakAreaWatchlist.joinToString("\n• "),
                confidenceScore = 70
            )
        )
    }

    // AI Study Generation
    suspend fun askZoya(question: String, history: List<ChatMessage>): Result<String> {
        val mappedHistory = history.map {
            (if (it.sender == Speaker.USER) "user" else "model") to it.text
        }
        return restService.chatWithZoya(getApiKey(), question, mappedHistory)
    }

    suspend fun explainConcept(topic: String, depthLevel: String): Result<ConceptExplanation> {
        return restService.explainConcept(getApiKey(), topic, depthLevel)
    }

    suspend fun solveProblem(problem: String, subject: String): Result<StepByStepSolution> {
        return restService.solveProblemStepByStep(getApiKey(), problem, subject)
    }

    suspend fun generateQuiz(
        topic: String,
        count: Int,
        difficulty: String,
        questionTypeFilter: String = "All Types"
    ): Result<QuizSession> {
        return restService.generateQuiz(getApiKey(), topic, count, difficulty, questionTypeFilter)
    }

    suspend fun evaluateShortAnswer(
        question: String,
        modelAnswer: String,
        studentAnswer: String
    ): Result<ShortAnswerEvaluation> {
        return restService.evaluateShortAnswer(getApiKey(), question, modelAnswer, studentAnswer)
    }

    suspend fun generateFlashcards(topic: String, count: Int): Result<List<FlashcardItem>> {
        val result = restService.generateFlashcards(getApiKey(), topic, count)
        result.onSuccess { cards ->
            cards.forEach { saveFlashcard(it) }
        }
        return result
    }

    suspend fun generateRevision(topic: String): Result<QuickRevisionGuide> {
        val result = restService.generateQuickRevision(getApiKey(), topic)
        result.onSuccess { saveRevisionGuide(it) }
        return result
    }
}

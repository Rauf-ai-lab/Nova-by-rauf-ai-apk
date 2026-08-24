package com.example.domain.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: Speaker,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modeCategory: String = "Voice",
    val isStreaming: Boolean = false
)

enum class Speaker {
    USER,
    ZOYA
}

data class ConceptExplanation(
    val topic: String,
    val depthLevel: String,
    val quickSummary: String,
    val deepExplanation: String,
    val realWorldAnalogy: String,
    val keyPointsAndFormulas: List<String>,
    val commonMistakes: List<String>,
    val quickPracticeDoubt: String
)

data class SolutionStep(
    val stepNumber: Int,
    val title: String,
    val calculationOrAction: String,
    val whyThisStep: String
)

data class StepByStepSolution(
    val problem: String,
    val subject: String,
    val identifiedConcept: String,
    val strategyApproach: String,
    val steps: List<SolutionStep>,
    val finalAnswer: String,
    val verificationNote: String,
    val practiceChallenge: String
)

enum class QuestionType(val displayName: String) {
    MULTIPLE_CHOICE("Multiple Choice"),
    TRUE_FALSE("True / False"),
    FILL_IN_BLANKS("Fill in the Blank"),
    SHORT_ANSWER("Short Answer")
}

data class QuizQuestion(
    val id: Long = 0,
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val question: String,
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val correctAnswerText: String = "",
    val explanation: String,
    val mistakeAnalysis: String = "",
    val weakConcept: String = "",
    var selectedIndex: Int = -1,
    var userTextAnswer: String = "",
    var answered: Boolean = false,
    var isCorrect: Boolean = false,
    var evaluationFeedback: String = ""
)

data class QuizSession(
    val topic: String,
    val difficulty: String,
    val questions: List<QuizQuestion>,
    val questionTypeFilter: String = "All Types"
)

data class ShortAnswerEvaluation(
    val isCorrect: Boolean,
    val scoreGrade: String, // "Full Credit", "Partial Credit", "Needs Review"
    val explanation: String,
    val mistakeAnalysis: String,
    val weakConcept: String
)

data class FlashcardItem(
    val id: Long = 0,
    val subject: String,
    val topic: String,
    val question: String,
    val answer: String,
    val difficulty: String = "Medium",
    val masteryLevel: Int = 0
)

data class QuickRevisionGuide(
    val topic: String,
    val subject: String,
    val keyDefinitions: List<String>,
    val coreFormulas: List<String>,
    val highYieldFacts: List<String>,
    val weakAreaWatchlist: List<String>
)

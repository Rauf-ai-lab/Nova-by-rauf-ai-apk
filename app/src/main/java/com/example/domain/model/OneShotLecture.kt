package com.example.domain.model

data class OneShotSection(
    val sectionNumber: Int,
    val title: String,
    val summary: String,
    val body: String,
    val bulletPoints: List<String> = emptyList(),
    val formulasOrNotes: List<String> = emptyList()
)

data class OneShotLecture(
    val id: String = java.util.UUID.randomUUID().toString(),
    val topic: String,
    val subject: String,
    val board: String,
    val classLevel: String,
    val chapterOverview: String,
    val learningObjectives: List<String>,
    val importantConcepts: String,
    val detailedExplanation: String,
    val simpleExamples: List<String>,
    val realWorldExamples: List<String>,
    val importantDefinitions: List<String>,
    val importantFormulas: List<String>,
    val commonMistakes: List<String>,
    val frequentlyAskedQuestions: List<Pair<String, String>>,
    val examFocusedPoints: List<String>,
    val quickRevision: String,
    val practiceQuestions: List<String>,
    val finalSummary: String,
    val sections: List<OneShotSection> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class ExamModePack(
    val topic: String,
    val subject: String,
    val board: String,
    val classLevel: String,
    val highPriorityConcepts: List<String>,
    val mostExpectedQuestions: List<String>,
    val commonExamTraps: List<String>,
    val formulaCheatSheet: List<String>,
    val definitionsToMemorize: List<String>,
    val quickPracticeProblems: List<String>
)

package com.example.domain.model

sealed interface ZoyaState {
    data object Idle : ZoyaState
    data class Listening(val amplitude: Float = 0f) : ZoyaState
    data object Thinking : ZoyaState
    data class Speaking(val audioAmplitude: Float = 0.5f) : ZoyaState
    data object Disconnected : ZoyaState
    data class Error(val message: String) : ZoyaState
}

enum class StudyMode(val title: String, val subtitle: String) {
    VoiceCompanion("Live Voice", "Real-time voice dialogue with Zoya"),
    ConceptExplainer("Concept Explainer", "Deep academic breakdown from beginner to exam level"),
    StepByStepSolver("Step-by-Step Solver", "Math, Physics & Chemistry logical solutions"),
    QuizArena("Quiz Arena", "Interactive recall challenge with AI feedback"),
    FlashcardDeck("Flashcards", "Active recall flip cards & mastery tracking"),
    QuickRevision("Rapid Revision", "Formulas, definitions & weak area drills"),
    YourBoard("Your Board", "Your study space, built around your board"),
    OneShotLecture("One-Shot Lectures", "Understand an entire chapter in one powerful study session"),
    Settings("System Settings", "API key, voice tuning & device tools")
}

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val topic: String,
    val frontQuestion: String,
    val backAnswer: String,
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val masteryLevel: Int = 0, // 0 = New, 1 = Learning, 2 = Mastered
    val reviewCount: Int = 0,
    val lastReviewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_records")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizSessionId: String,
    val topic: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOptionIndex: Int,
    val explanation: String,
    val userSelectedOption: Int = -1,
    val isCorrect: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val mode: String, // Voice, Concept, ProblemSolving, Quiz, Flashcard, Revision
    val summary: String,
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "revision_topics")
data class RevisionTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val subject: String,
    val keyPoints: String, // Bullet points or newline-separated
    val formulas: String,
    val weakAreas: String,
    val confidenceScore: Int = 50, // 0-100%
    val updatedAt: Long = System.currentTimeMillis()
)

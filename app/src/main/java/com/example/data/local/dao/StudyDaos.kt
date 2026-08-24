package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FlashcardEntity
import com.example.data.local.entity.QuizQuestionEntity
import com.example.data.local.entity.RevisionTopicEntity
import com.example.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY lastReviewedAt DESC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE topic = :topic ORDER BY id ASC")
    fun getFlashcardsByTopic(topic: String): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(card: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_records ORDER BY timestamp DESC")
    fun getAllQuizRecords(): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_records WHERE quizSessionId = :sessionId ORDER BY id ASC")
    fun getQuestionsBySession(sessionId: String): Flow<List<QuizQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestion(question: QuizQuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuizQuestions(questions: List<QuizQuestionEntity>)

    @Update
    suspend fun updateQuizQuestion(question: QuizQuestionEntity)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Query("SELECT COUNT(*) FROM study_sessions")
    fun getSessionCount(): Flow<Int>
}

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revision_topics ORDER BY updatedAt DESC")
    fun getAllRevisionTopics(): Flow<List<RevisionTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(topic: RevisionTopicEntity): Long

    @Query("DELETE FROM revision_topics WHERE id = :id")
    suspend fun deleteById(id: Long)
}

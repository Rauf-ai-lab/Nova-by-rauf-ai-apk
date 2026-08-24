package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.FlashcardDao
import com.example.data.local.dao.QuizDao
import com.example.data.local.dao.RevisionDao
import com.example.data.local.dao.StudySessionDao
import com.example.data.local.entity.FlashcardEntity
import com.example.data.local.entity.QuizQuestionEntity
import com.example.data.local.entity.RevisionTopicEntity
import com.example.data.local.entity.StudySessionEntity

@Database(
    entities = [
        FlashcardEntity::class,
        QuizQuestionEntity::class,
        StudySessionEntity::class,
        RevisionTopicEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun quizDao(): QuizDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun revisionDao(): RevisionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nova_study_vault.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

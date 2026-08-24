package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.StudentProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StudentProfileStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _profileFlow = MutableStateFlow(loadProfile())
    val profileFlow: StateFlow<StudentProfile> = _profileFlow.asStateFlow()

    fun getProfile(): StudentProfile = _profileFlow.value

    fun updateProfile(profile: StudentProfile) {
        prefs.edit()
            .putString(KEY_BOARD_ID, profile.boardId)
            .putString(KEY_BOARD_NAME, profile.boardName)
            .putString(KEY_STATE, profile.state)
            .putString(KEY_CLASS_LEVEL, profile.classLevel)
            .putString(KEY_SUBJECT, profile.subject)
            .putString(KEY_LANGUAGE, profile.language)
            .apply()

        _profileFlow.value = profile
    }

    private fun loadProfile(): StudentProfile {
        return StudentProfile(
            boardId = prefs.getString(KEY_BOARD_ID, "jkbose") ?: "jkbose",
            boardName = prefs.getString(KEY_BOARD_NAME, "JKBOSE") ?: "JKBOSE",
            state = prefs.getString(KEY_STATE, "Jammu & Kashmir") ?: "Jammu & Kashmir",
            classLevel = prefs.getString(KEY_CLASS_LEVEL, "Class 10") ?: "Class 10",
            subject = prefs.getString(KEY_SUBJECT, "Science") ?: "Science",
            language = prefs.getString(KEY_LANGUAGE, "English") ?: "English"
        )
    }

    companion object {
        private const val PREF_NAME = "nova_student_profile_prefs"
        private const val KEY_BOARD_ID = "board_id"
        private const val KEY_BOARD_NAME = "board_name"
        private const val KEY_STATE = "state"
        private const val KEY_CLASS_LEVEL = "class_level"
        private const val KEY_SUBJECT = "subject"
        private const val KEY_LANGUAGE = "language"
    }
}

package com.example

import android.app.Application
import com.example.ai.LiveSessionManager
import com.example.data.gemini.GeminiRestService
import com.example.data.local.AppDatabase
import com.example.data.storage.SecureApiKeyStorage
import com.example.domain.repository.StudyRepository
import com.example.domain.tools.DeviceToolsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NovaApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var secureApiKeyStorage: SecureApiKeyStorage
        private set

    lateinit var database: AppDatabase
        private set

    lateinit var geminiRestService: GeminiRestService
        private set

    lateinit var studyRepository: StudyRepository
        private set

    lateinit var liveSessionManager: LiveSessionManager
        private set

    lateinit var deviceToolsManager: DeviceToolsManager
        private set

    override fun onCreate() {
        super.onCreate()

        secureApiKeyStorage = SecureApiKeyStorage(this)
        database = AppDatabase.getDatabase(this)
        geminiRestService = GeminiRestService()
        studyRepository = StudyRepository(database, secureApiKeyStorage, geminiRestService)
        liveSessionManager = LiveSessionManager(this, applicationScope, studyRepository)
        deviceToolsManager = DeviceToolsManager(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        liveSessionManager.release()
    }
}

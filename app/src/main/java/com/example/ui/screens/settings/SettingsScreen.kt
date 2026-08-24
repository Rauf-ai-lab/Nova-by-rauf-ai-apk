package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.repository.StudyRepository
import com.example.domain.tools.DeviceToolsManager
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.NovaDarkElevated
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ZoyaAmber
import com.example.ui.theme.ZoyaCoral
import com.example.ui.theme.ZoyaCyan
import com.example.ui.theme.ZoyaCyanBright
import com.example.ui.theme.ZoyaElectricBlue
import com.example.ui.theme.ZoyaEmerald
import com.example.ui.theme.ZoyaVioletBright
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: StudyRepository,
    deviceTools: DeviceToolsManager,
    onClose: () -> Unit,
    onKeyCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newKeyInput by remember { mutableStateOf("") }
    var isTestingKey by remember { mutableStateOf(false) }
    var testResultStatus by remember { mutableStateOf<String?>(null) }
    var deviceToolFeedback by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun testAndSaveKey(key: String) {
        val target = key.ifBlank { repository.getApiKey() }
        if (target.isBlank()) {
            testResultStatus = "No key provided to test."
            return
        }

        isTestingKey = true
        testResultStatus = null

        scope.launch {
            val res = repository.validateApiKey(target)
            isTestingKey = false
            res.onSuccess {
                if (key.isNotBlank()) {
                    repository.saveApiKey(key, true)
                }
                testResultStatus = "Gemini Live API Key is Active & Verified!"
            }.onFailure {
                testResultStatus = "Validation Failed: ${it.localizedMessage}"
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = NovaObsidian) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = ZoyaCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "System Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_settings")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // API Key Management Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ZoyaCyan.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = ZoyaCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI API KEY STATUS",
                                style = MaterialTheme.typography.labelMedium.copy(color = ZoyaCyan, fontWeight = FontWeight.Bold)
                            )
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZoyaEmerald, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Stored Key: ${repository.getMaskedApiKey()}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newKeyInput,
                        onValueChange = { newKeyInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("update_api_key_input"),
                        placeholder = { Text("Update with new API Key...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZoyaCyan,
                            unfocusedBorderColor = ZoyaCyan.copy(alpha = 0.3f),
                            focusedContainerColor = NovaDarkElevated,
                            unfocusedContainerColor = NovaDarkElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FuturisticButton(
                            text = if (isTestingKey) "Testing..." else "Test Connection",
                            onClick = { testAndSaveKey(newKeyInput) },
                            icon = Icons.Default.Refresh,
                            modifier = Modifier.weight(1f),
                            enabled = !isTestingKey,
                            testTag = "test_key_btn"
                        )

                        IconButton(
                            onClick = {
                                repository.clearApiKey()
                                onKeyCleared()
                            },
                            modifier = Modifier
                                .background(ZoyaCoral.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .border(1.dp, ZoyaCoral, RoundedCornerShape(12.dp))
                                .size(52.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Key", tint = ZoyaCoral)
                        }
                    }

                    testResultStatus?.let { status ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (status.contains("Verified", true)) ZoyaEmerald else ZoyaCoral,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Modular Device Tools Testing
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ZoyaElectricBlue.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Build, contentDescription = null, tint = ZoyaElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MODULAR DEVICE ASSISTANT TOOLS",
                            style = MaterialTheme.typography.labelMedium.copy(color = ZoyaElectricBlue, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Optional device intent execution separated cleanly from study AI logic.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Open Calculator
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NovaDarkElevated)
                                .border(1.dp, ZoyaCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .clickable {
                                    val res = deviceTools.openApp("com.google.android.calculator")
                                    deviceToolFeedback = res.message
                                }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = ZoyaCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calculator", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
                            }
                        }

                        // WhatsApp
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NovaDarkElevated)
                                .border(1.dp, ZoyaEmerald.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .clickable {
                                    val res = deviceTools.sendWhatsAppMessage("Study Buddy", "Hey, ready for study session?")
                                    deviceToolFeedback = res.message
                                }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = ZoyaEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
                            }
                        }

                        // Gmail
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NovaDarkElevated)
                                .border(1.dp, ZoyaAmber.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .clickable {
                                    val res = deviceTools.sendGmail("", "Study Notes", "Here are today's revision notes from Zoya.")
                                    deviceToolFeedback = res.message
                                }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = ZoyaAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gmail", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
                            }
                        }
                    }

                    deviceToolFeedback?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = msg, style = MaterialTheme.typography.bodyMedium.copy(color = ZoyaCyanBright, fontSize = 12.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Identity & Architecture Info
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NOVA BY RAUF ARCHITECTURE",
                        style = MaterialTheme.typography.labelSmall.copy(color = ZoyaVioletBright, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• AI Companion: ZOYA\n• Core Model: Gemini Live (gemini-2.5-flash-native-audio-preview) & Gemini 3.5 Flash / 3.1 Pro\n• Audio Pipeline: Low-latency PCM16, Dynamic RMS Waveform & AudioTrack Stream\n• Vault: Android Keystore AES-GCM & Room SQLite Database\n• Foreground Voice Service: Android 14+ Microphonic Foreground Service",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 22.sp, fontSize = 13.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

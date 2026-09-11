package com.chotu.assistant

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chotu.assistant.ai.GeminiBrainEngine
import com.chotu.assistant.service.ChotuAccessibilityService
import com.chotu.assistant.service.WakeWordService
import com.chotu.assistant.voice.ChotuVoiceEngine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var voiceEngine: ChotuVoiceEngine
    private val brainEngine = GeminiBrainEngine()

    private lateinit var tvAccessibilityStatus: TextView
    private lateinit var tvConsoleLogs: TextView
    private lateinit var btnEnableAccessibility: Button
    private lateinit var btnTestVoice: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAccessibilityStatus = findViewById(R.id.tvAccessibilityStatus)
        tvConsoleLogs = findViewById(R.id.tvConsoleLogs)
        btnEnableAccessibility = findViewById(R.id.btnEnableAccessibility)
        btnTestVoice = findViewById(R.id.btnTestVoice)

        // Initialize 15-Year-Old Boy Voice Engine
        voiceEngine = ChotuVoiceEngine(this) {
            logMessage("Chotu Voice Engine Ready (Pitch: 1.35x - 15yr Boy Accent)")
            voiceEngine.speak("Pranam Ustad Ji! Main Chotu hoon, bataiye kya hukum hai?")
        }

        // Start Background Wake-Word Service
        val serviceIntent = Intent(this, WakeWordService::class.java)
        startService(serviceIntent)

        btnEnableAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        btnTestVoice.setOnClickListener {
            processSampleCommand("Ustad Ji ka order hai screen padho aur analyze karo.")
        }
    }

    override fun onResume() {
        super.onResume()
        if (ChotuAccessibilityService.instance != null) {
            tvAccessibilityStatus.text = "Accessibility Control: ENABLED (Chotu can read & click screen)"
            tvAccessibilityStatus.setTextColor(getColor(R.color.accent_green))
        } else {
            tvAccessibilityStatus.text = "Accessibility Control: DISABLED (Click button to grant)"
            tvAccessibilityStatus.setTextColor(getColor(R.color.text_subtle))
        }
    }

    private fun processSampleCommand(command: String) {
        logMessage("User (Ustad Ji): $command")

        val screenData = ChotuAccessibilityService.instance?.dumpScreenTextTree() ?: "Accessibility not granted"
        logMessage("Screen Tree Captured: ${screenData.take(120)}...")

        lifecycleScope.launch {
            val response = brainEngine.analyzeCommandAndScreen(command, screenData)
            logMessage("Chotu AI: $response")
            voiceEngine.speak(response)
        }
    }

    private fun logMessage(msg: String) {
        val current = tvConsoleLogs.text.toString()
        tvConsoleLogs.text = "$current\n> $msg"
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceEngine.shutdown()
    }
}
package com.chotu.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class ChotuVoiceEngine(context: Context, private val onInitSuccess: () -> Unit) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN") // Hindi/Hinglish accent
            
            // 15-Year-Old Energetic Boy Voice Configuration
            tts.setPitch(1.35f)      // Higher pitch for young boy voice
            tts.setSpeechRate(1.10f)  // Energetic speech tempo

            isReady = true
            onInitSuccess()
        }
    }

    fun speak(text: String) {
        if (isReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CHOTU_VOICE_ID")
        }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
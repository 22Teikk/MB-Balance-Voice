package com.teikk.mbbalancevoice

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.Locale

class TtsService : AccessibilityService(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    companion object {
        private const val TAG  = "Teikk_tts"
        var isRunning = false
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val amount = intent?.getStringExtra("amount")
            val datetime = intent?.getStringExtra("datetime")
            val memo = intent?.getStringExtra("memo")
            Log.d(TAG, "Ammount: " + amount.toString())
            Log.d(TAG, "MEMO: " + amount.toString())
            // Gọi TTS hoặc phát chuông
            speak("Tài khoản thay đổi $amount vào $datetime. $memo")
        }
    }

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        val filter = IntentFilter("com.teikk.mbbalancevoice.BALANCE_UPDATE")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("vi", "VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Ngôn ngữ không hỗ trợ", Toast.LENGTH_SHORT).show()
            } else {
                speak("Xin chào! Đây là giọng đọc tích hợp.")
            }
        } else {
            Toast.makeText(this, "Khởi tạo TTS thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}